package com.neosoft.pijamasbakend.services;

import com.neosoft.pijamasbakend.entities.*;
import com.neosoft.pijamasbakend.enums.EstadoFactura;
import com.neosoft.pijamasbakend.models.*;
import com.neosoft.pijamasbakend.repositories.FacturaProductoRepository;
import com.neosoft.pijamasbakend.repositories.FacturaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacturaService {

    private final FacturaRepository facturaRepo;
    private final FacturaProductoRepository fpRepo;
    private final ProductoTallaService ptService;
    private final PromocionService promoService;
    private final WompiClient wompi;

    @Transactional
    public CheckoutResponse checkout(CheckoutRequest req, Cliente cliente) {
        log.info("Iniciando checkout para cliente: {}", cliente.getId());

        try {
            // 1. Crear factura base
            Factura factura = crearFacturaBase(cliente);

            // 2. Procesar items y calcular totales
            procesarItemsYTotales(factura, req);

            // 3. Persistir factura con detalles
            facturaRepo.save(factura);
            fpRepo.saveAll(factura.getDetalles());

            // 4. Procesar pago con Wompi (fuera de transacción)
            procesarPagoWompi(factura, req);

            log.info("Checkout completado exitosamente. Factura: {}", factura.getReferencia());

            return new CheckoutResponse(factura.getReferencia(),
                    factura.getWompiPaymentUrl(),
                    factura.getEstado().name(),
                    factura.getId()
            );

        } catch (Exception e) {
            log.error("Error en checkout para cliente {}: {}", cliente.getId(), e.getMessage(), e);
            throw e;
        }
    }

    public List<Factura> obtenerFacturas() {
        return facturaRepo.findAll();
    }

    public Factura obtenerFactura(Integer id) {
        return facturaRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Factura no encontrada"));
    }

    @Transactional
    public void actualizarDesdeWebhook(String referencia, String status, String authCode) {
        log.info("Actualizando factura desde webhook. Ref: {}, Status: {}", referencia, status);

        Factura factura = facturaRepo.findByReferencia(referencia)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada: " + referencia));

        actualizarEstadoFactura(factura, status, authCode);
        facturaRepo.save(factura);

        log.info("Factura {} actualizada a estado: {}", referencia, factura.getEstado());
    }

    @Scheduled(fixedDelayString = "${wompi.poll-ms}")
    @Transactional
    public void sincronizarFacturasPendientes() {
        log.debug("Iniciando sincronización de facturas pendientes");

        List<Factura> facturasPendientes = facturaRepo.findByEstado(EstadoFactura.PENDIENTE);

        if (facturasPendientes.isEmpty()) {
            log.debug("No hay facturas pendientes para sincronizar");
            return;
        }

        log.info("Sincronizando {} facturas pendientes", facturasPendientes.size());

        facturasPendientes.forEach(this::sincronizarFacturaIndividual);
    }

    private Factura crearFacturaBase(Cliente cliente) {
        Factura factura = new Factura();
        factura.setCliente(cliente);
        factura.setReferencia(generarReferencia());
        factura.setEstado(EstadoFactura.CREADA);
        return factura;
    }

    private void procesarItemsYTotales(Factura factura, CheckoutRequest req) {
        BigDecimal totalBruto = BigDecimal.ZERO;
        BigDecimal totalDescuento = BigDecimal.ZERO;
        List<FacturaProducto> detalles = new ArrayList<>();

        for (CheckoutItemDTO item : req.items()) {
            ResultadoProcesamiento resultado = procesarItemIndividual(item, factura);

            totalBruto = totalBruto.add(resultado.subtotal);
            totalDescuento = totalDescuento.add(resultado.descuentoSubtotal);
            detalles.add(resultado.facturaProducto);
        }

        // Configurar totales de la factura
        BigDecimal costoEnvio = BigDecimal.valueOf(0);
        BigDecimal totalNeto = totalBruto.subtract(totalDescuento).add(costoEnvio);

        factura.setMetodoPago(req.metodoPago());
        factura.setTotalBruto(totalBruto);
        factura.setEnvio(costoEnvio);
        factura.setTotalDescuento(totalDescuento);
        factura.setTotalNeto(totalNeto);
        factura.getDetalles().addAll(detalles);
    }

    private ResultadoProcesamiento procesarItemIndividual(CheckoutItemDTO item, Factura factura) {
        // 1. Obtener variante y validar stock
        ProductoTalla variante = ptService.getByProductoIdAndTallaId(
                item.productoId(), item.tallaId());

        validarStockDisponible(variante, item.cantidad());

        // 2. Calcular precios base
        BigDecimal precioUnitario = variante.getPrecioVenta();
        BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(item.cantidad()));

        // 3. Calcular descuentos
        Optional<PromocionProducto> promocion = promoService.mejorPromocion(item.productoId());
        BigDecimal porcentajeDescuento = promocion
                .map(PromocionProducto::getDescuento)
                .orElse(BigDecimal.ZERO);

        BigDecimal descuentoUnitario = calcularDescuentoUnitario(precioUnitario, porcentajeDescuento);
        BigDecimal descuentoSubtotal = descuentoUnitario.multiply(BigDecimal.valueOf(item.cantidad()));

        // 4. Crear línea de factura
        FacturaProducto facturaProducto = crearLineaFactura(
                factura, variante, promocion, item.cantidad(),
                precioUnitario, descuentoUnitario, porcentajeDescuento
        );

        // 5. Descontar stock
        descontarStock(variante, item.cantidad());

        return new ResultadoProcesamiento(subtotal, descuentoSubtotal, facturaProducto);
    }

    private void validarStockDisponible(ProductoTalla variante, int cantidadSolicitada) {
        if (variante.getStockActual() < cantidadSolicitada) {
            throw new IllegalArgumentException(
                    String.format("Stock insuficiente para %s - talla %s. Disponible: %d, Solicitado: %d",
                            variante.getProducto().getNombre(),
                            variante.getTalla().getNombre(),
                            variante.getStockActual(),
                            cantidadSolicitada)
            );
        }
    }

    private BigDecimal calcularDescuentoUnitario(BigDecimal precio, BigDecimal porcentaje) {
        return precio.multiply(porcentaje)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private FacturaProducto crearLineaFactura(Factura factura, ProductoTalla variante,
                                              Optional<PromocionProducto> promocion, int cantidad,
                                              BigDecimal precioUnitario, BigDecimal descuentoUnitario, BigDecimal porcentajeDescuento) {

        FacturaProducto fp = new FacturaProducto();
        fp.setFactura(factura);
        fp.setProducto(variante.getProducto());
        promocion.ifPresent(pp -> fp.setPromocion(pp.getPromocion()));
        fp.setPorcentajeDescuento(porcentajeDescuento);
        fp.setCantidad(cantidad);
        fp.setPrecioUnit(precioUnitario);
        fp.setDescuento(descuentoUnitario);

        return fp;
    }

    private void descontarStock(ProductoTalla variante, int cantidad) {
        variante.setStockActual(variante.getStockActual() - cantidad);
        ptService.guardarVariante(variante);

        log.debug("Stock descontado para {} - talla {}: {} unidades",
                variante.getProducto().getNombre(),
                variante.getTalla().getNombre(),
                cantidad);
    }

    private void procesarPagoWompi(Factura factura, CheckoutRequest req) {
        try {

            TransactionResponse response = crearTransaccionWompi(factura, req);
            actualizarFacturaConRespuestaWompi(factura, response);

            log.info("Transacción Wompi creada exitosamente. ID: {}, Status: {}",
                    response.data().id(), response.data().status());

        } catch (Exception e) {
            log.error("Error procesando pago Wompi para factura {}: {}",
                    factura.getReferencia(), e.getMessage(), e);
            throw new RuntimeException("Error procesando pago con Wompi", e);
        }
    }

    private TransactionResponse crearTransaccionWompi(Factura factura, CheckoutRequest req) {
        TransactionRequest request = construirSolicitudTransaccion(factura, req);
        Long sourceId = extraerSourceId(req);

        return wompi.createTransaction(request, sourceId);
    }

    private TransactionRequest construirSolicitudTransaccion(Factura factura, CheckoutRequest req) {
        long centavos = convertirPesosACentavos(factura.getTotalNeto());
        Map<String, Object> metodoPago = wompi.buildPaymentMethod(req.metodoPagoDetail());
        Long sourceId = extraerSourceId(req);

        // Formato exigido para compretar Transaccion en wompi
        // 1) Creamos el shipping_address a partir del Cliente
        Map<String, Object> shippingAddress = new HashMap<>();
        shippingAddress.put("address_line_1", factura.getCliente().getDireccion() + " " + factura.getCliente().getBarrio());
        shippingAddress.put("city", factura.getCliente().getCiudad());
        shippingAddress.put("region", factura.getCliente().getDepartamento());
        shippingAddress.put("country", "CO");
        shippingAddress.put("phone_number", factura.getCliente().getTelefono());

        // 2) Creamos el customer_data
        Map<String, Object> customerData = new HashMap<>();
        customerData.put("full_name", factura.getCliente().getNombre() + " " + factura.getCliente().getApellido());
        customerData.put("email", factura.getCliente().getEmail());

        // 3) Construimos y devolvemos el TransactionRequest completo
        return new TransactionRequest(
                req.acceptanceToken(),
                centavos,
                "COP",
                wompi.buildSignature(centavos, factura.getReferencia()),
                factura.getCliente().getEmail(),
                metodoPago,
                sourceId,
                req.redirectUrl(),
                factura.getReferencia(),
                Instant.now().plusSeconds(900).toString(),
                customerData,
                shippingAddress
        );
    }

    private void actualizarFacturaConRespuestaWompi(Factura factura, TransactionResponse response) {
        factura.setWompiId(response.data().id());
        factura.setWompiStatus(response.data().status());
        factura.setWompiPaymentUrl(extraerUrlPago(response));
        factura.setEstado(EstadoFactura.PENDIENTE);

        facturaRepo.save(factura);
    }

    private long convertirPesosACentavos(BigDecimal pesos) {
        return pesos.movePointRight(2)
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }

    private Long extraerSourceId(CheckoutRequest req) {
        Number n = (Number) req.metodoPagoDetail().get("paymentSourceId");
        return n != null ? n.longValue() : null;
    }

    private String extraerUrlPago(TransactionResponse response) {
        Map<String, Object> metodoPago = response.data().payment_method();
        if (metodoPago == null) {
            return response.data().redirect_url().toString();
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> extra = (Map<String, Object>) metodoPago.get("extra");

        return Optional.ofNullable(extra)
                .map(ex -> (String) ex.getOrDefault("async_payment_url",
                        ex.getOrDefault("qr_image",
                                ex.getOrDefault("url",
                                        response.data().redirect_url().toString()))))
                .orElse(response.data().redirect_url().toString());
    }

    private void actualizarEstadoFactura(Factura factura, String wompiStatus, String authCode) {
        factura.setWompiStatus(wompiStatus);
        factura.setWompiAuthorizationCode(authCode);

        EstadoFactura nuevoEstado = mapearEstadoWompiAInterno(wompiStatus);
        factura.setEstado(nuevoEstado);

        if (nuevoEstado == EstadoFactura.PAGADA) {
            factura.setFechaPago(Instant.now());
        }

        log.debug("Estado de factura {} actualizado de {} a {}",
                factura.getReferencia(), factura.getWompiStatus(), wompiStatus);
    }

    private void sincronizarFacturaIndividual(Factura factura) {
        try {
            log.debug("Sincronizando factura: {}", factura.getReferencia());

            TransactionResponse response = wompi.getTransaction(factura.getWompiId());
            String nuevoStatus = response.data().status();

            if (!nuevoStatus.equals(factura.getWompiStatus())) {
                log.info("Estado actualizado para factura {}. {} -> {}",
                        factura.getReferencia(), factura.getWompiStatus(), nuevoStatus);

                actualizarEstadoFactura(factura, nuevoStatus, null);
                facturaRepo.save(factura);
            }

        } catch (Exception e) {
            log.error("Error sincronizando factura {}: {}",
                    factura.getReferencia(), e.getMessage());
        }
    }

    private EstadoFactura mapearEstadoWompiAInterno(String wompiStatus) {
        return switch (wompiStatus) {
            case "APPROVED" -> EstadoFactura.PAGADA;
            case "DECLINED" -> EstadoFactura.RECHAZADA;
            case "VOIDED" -> EstadoFactura.CANCELADA;
            default -> EstadoFactura.PENDIENTE;
        };
    }

    private String generarReferencia() {
        String fecha = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String codigo = RandomStringUtils.randomAlphanumeric(6).toUpperCase();
        return "FAC-" + fecha + "-" + codigo;
    }

    private record ResultadoProcesamiento(
            BigDecimal subtotal,
            BigDecimal descuentoSubtotal,
            FacturaProducto facturaProducto
    ) {
    }
}
