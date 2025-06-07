package com.neosoft.pijamasbakend.services;

import com.neosoft.pijamasbakend.entities.*;
import com.neosoft.pijamasbakend.enums.EstadoFactura;
import com.neosoft.pijamasbakend.models.CheckoutItemDTO;
import com.neosoft.pijamasbakend.models.CheckoutRequest;
import com.neosoft.pijamasbakend.models.CheckoutResponse;
import com.neosoft.pijamasbakend.repositories.FacturaProductoRepository;
import com.neosoft.pijamasbakend.repositories.FacturaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FacturaService {

    private final FacturaRepository facturaRepo;
    private final FacturaProductoRepository fpRepo;
    private final ProductoTallaService ptService;
    private final PromocionService promoService;
    private final WompiClient wompi;

    @Transactional
    public CheckoutResponse checkout(CheckoutRequest req, Cliente cli) {

        Factura f = nuevaFactura(cli);

        BigDecimal bruto = BigDecimal.ZERO;
        BigDecimal descTot = BigDecimal.ZERO;
        List<FacturaProducto> lineas = new ArrayList<>();

        for (CheckoutItemDTO item : req.items()) {

            /* ---------- variante y stock ---------- */
            ProductoTalla var = ptService.getByProductoIdAndTallaId(
                    item.productoId(), item.tallaId());

            if (var.getStockActual() < item.cantidad()) {
                throw new IllegalArgumentException("Sin stock para " +
                        var.getProducto().getNombre() + " talla " + var.getTalla().getNombre());
            }

            /* ---------- precios y descuentos ---------- */
            BigDecimal precioUnit = var.getPrecioVenta();
            BigDecimal subtotal = precioUnit.multiply(BigDecimal.valueOf(item.cantidad()));
            bruto = bruto.add(subtotal);

            // obtenemos la promo si existe
            Optional<PromocionProducto> promoOpt =
                    promoService.mejorPromocion(item.productoId());

            BigDecimal pctDesc = promoOpt
                    .map(PromocionProducto::getDescuento)
                    .orElse(BigDecimal.ZERO);

            BigDecimal descUnit = precioUnit.multiply(pctDesc)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            BigDecimal subDesc = descUnit.multiply(BigDecimal.valueOf(item.cantidad()));
            descTot = descTot.add(subDesc);

            /* ---------- línea factura ---------- */
            FacturaProducto fp = new FacturaProducto();
            fp.setFactura(f);
            fp.setProducto(var.getProducto());
            promoOpt.ifPresent(pp -> fp.setPromocion(pp.getPromocion()));
            fp.setPorcentajeDescuento(pctDesc);
            fp.setCantidad(item.cantidad());
            fp.setPrecioUnit(precioUnit);
            fp.setDescuento(descUnit);
            lineas.add(fp);

            /* ---------- descontar stock ---------- */
            var.setStockActual(var.getStockActual() - item.cantidad());
            ptService.guardarVariante(var);
        }

        /* ---------- totales ---------- */
        BigDecimal neto = bruto.subtract(descTot).add(req.envio());

        f.setMetodoPago(req.metodoPago());
        f.setTotalBruto(bruto);
        f.setEnvio(req.envio());
        f.setTotalDescuento(descTot);
        f.setTotalNeto(neto);
        f.getDetalles().addAll(lineas);
        facturaRepo.save(f);
        fpRepo.saveAll(lineas);

        /* ---------- llamada a Wompi ---------- */
        long cents = neto.multiply(BigDecimal.valueOf(100)).longValueExact();
        var wompiResp = wompi.createTx(f, req, cents);

        f.setWompiId(wompiResp.data().id());
        f.setWompiStatus(wompiResp.data().status());
        f.setWompiPaymentUrl(extractUrl(wompiResp));
        f.setEstado(EstadoFactura.PENDIENTE);
        facturaRepo.save(f);

        return new CheckoutResponse(
                f.getId(), f.getReferencia(),
                f.getWompiPaymentUrl(), f.getWompiStatus());
    }

    /* ======= 2. Webhook / sincronización ======= */
    @Transactional
    public void actualizarDesdeWebhook(String ref, String status, String auth) {
        Factura f = facturaRepo.findByReferencia(ref).orElseThrow();
        f.setWompiStatus(status);
        f.setWompiAuthorizationCode(auth);
        switch (status) {
            case "APPROVED" -> f.setEstado(EstadoFactura.PAGADA);
            case "DECLINED" -> f.setEstado(EstadoFactura.RECHAZADA);
            case "VOIDED" -> f.setEstado(EstadoFactura.CANCELADA);
        }
    }

    @Scheduled(fixedDelayString = "${wompi.poll-ms:600000}")
    @Transactional
    public void syncPendientes() {
        facturaRepo.findByEstado(EstadoFactura.PENDIENTE).forEach(f -> {
            var tx = wompi.getTx(f.getWompiId());
            if (!tx.data().status().equals(f.getWompiStatus())) {
                actualizarDesdeWebhook(f.getReferencia(), tx.data().status(), null);
            }
        });
    }

    /* ======= 3. Helpers ======= */
    private Factura nuevaFactura(Cliente cli) {
        Factura f = new Factura();
        f.setCliente(cli);
        f.setReferencia(generarReferencia());
        f.setEstado(EstadoFactura.CREADA);
        return facturaRepo.save(f);
    }

    private String generarReferencia() {
        String fecha = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String rand = RandomStringUtils.randomAlphanumeric(6).toUpperCase();
        return "FAC-" + fecha + "-" + rand;
    }

    private String extractUrl(WompiClient.TxResp r) {
        Map<String,Object> pm = r.data().pm();
        if (pm == null) return r.data().redirectUrl();

        return Optional.ofNullable(pm.get("extra"))
                .filter(Map.class::isInstance)
                .map(obj -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> extra = (Map<String, Object>) obj;
                    return Optional.ofNullable((String) extra.get("async_payment_url"))
                            .orElse(Optional.ofNullable((String) extra.get("qr_image"))
                                    .orElse(Optional.ofNullable((String) extra.get("url"))
                                            .orElse(r.data().redirectUrl())));
                })
                .orElse(r.data().redirectUrl());
    }


}
