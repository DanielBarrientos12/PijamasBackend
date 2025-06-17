package com.neosoft.pijamasbakend.services;

import com.neosoft.pijamasbakend.entities.*;
import com.neosoft.pijamasbakend.enums.EstadoPedido;
import com.neosoft.pijamasbakend.models.*;
import com.neosoft.pijamasbakend.repositories.AdministrativoRepository;
import com.neosoft.pijamasbakend.repositories.PedidoRepository;
import com.neosoft.pijamasbakend.repositories.ProductoTallaRepository;
import com.neosoft.pijamasbakend.utils.ImagenData;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PedidoService {
    @Autowired
    private PedidoRepository pedidoRepo;
    @Autowired
    private AdministrativoRepository adminRepo;
    @Autowired
    private ProductoTallaRepository productoRepo;
    @Autowired
    private ProductoTallaService productoTallaService;
    @Autowired
    private FileService fileService;

    @Transactional
    public void crearPedidoDesdeFactura(Factura factura, List<CheckoutItemDTO> itemsDto) {
        // 1) asignar responsable (el gerente id=2)
        Administrativo gerente = adminRepo.findById(2)
                .orElseThrow(() -> new IllegalStateException("Gerente no encontrado"));

        Pedido pedido = new Pedido();
        pedido.setFactura(factura);
        pedido.setResponsable(gerente);

        // 2) mapear cada DTO a un PedidoProducto
        for (CheckoutItemDTO dto : itemsDto) {
            PedidoProducto pp = new PedidoProducto();
            pp.setPedido(pedido);

            // cargo la variante
            ProductoTalla variante = productoRepo.findByProductoIdAndTallaId(dto.productoId(), dto.tallaId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto no existe"));
            pp.setProducto(variante.getProducto());
            pp.setProductoTalla(variante.getTalla());
            pp.setCantidad(dto.cantidad());
            pedido.getItems().add(pp);
        }

        pedidoRepo.save(pedido);
    }

    public List<PedidoDTO> listarPedidos(Optional<EstadoPedido> estado) {
        List<Pedido> pedidos = estado
                .map(pedidoRepo::findByEstado)
                .orElseGet(pedidoRepo::findAll);
        return pedidos.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public PedidoDTO buscarPorIdPedido(Integer idPedido) {
        Pedido p = pedidoRepo.findById(idPedido)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no existe"));
        return toDTO(p);
    }

    @Transactional
    public PedidoDTO marcarEntregado(Integer pedidoId) {
        Pedido p = pedidoRepo.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no existe"));
        p.setEstado(EstadoPedido.ENTREGADO);
        p.setFechaEntrega(LocalDateTime.now());
        return toDTO(pedidoRepo.save(p));
    }

    @Transactional
    public PedidoDTO marcarEnviado(Integer pedidoId) {
        Pedido p = pedidoRepo.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no existe"));
        p.setEstado(EstadoPedido.ENVIADO);
        p.setFechaEntrega(LocalDateTime.now());
        return toDTO(pedidoRepo.save(p));
    }

    public List<PedidoDTO> listarPedidosPorCliente(Integer clienteId) {
        List<Pedido> pedidos = pedidoRepo.findByFacturaClienteId(clienteId);
        return pedidos.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private PedidoDTO toDTO(Pedido p) {
        Cliente c = p.getFactura().getCliente();
        String direccion = c.getDireccion() + " " + c.getBarrio() + " " + c.getCiudad() + " " + c.getDepartamento() + ".";
        ClienteDTO cliente = new ClienteDTO(
                c.getId(),
                c.getNombre(),
                c.getApellido(),
                c.getNumeroDocumento(),
                c.getEmail(),
                c.getTelefono(),
                direccion,
                c.getCodigoPostal()
        );

        // 2) Administrativo
        Administrativo a = p.getResponsable();
        AdministrativoDTO admin = new AdministrativoDTO(a.getId(), a.getNombre(), a.getApellido());

        // 3) Factura
        Factura f = p.getFactura();
        FacturaDTO fact = new FacturaDTO(
                f.getId(),
                f.getReferencia(),
                f.getMetodoPago(),
                f.getTotalNeto(),
                f.getEstado(),
                f.getFechaRegistro(),
                f.getFechaPago()
        );

        // 4) Items de pedido
        List<PedidoItemDTO> items = p.getItems().stream().map(pp -> {
            Producto prod = pp.getProducto();

            List<ImagenData> imgs = prod.getImagenes().stream()
                    .map(img -> {
                        byte[] data;
                        try {
                            data = fileService.loadFile(img.getUrl());
                        } catch (IOException e) {
                            throw new UncheckedIOException("Error leyendo imagen " + img.getUrl(), e);
                        }
                        String nombreArchivo = Paths.get(img.getUrl()).getFileName().toString();
                        return new ImagenData(img.getPosicion(), nombreArchivo, data);
                    })
                    .toList();

            // Variante y precio
            ProductoTalla pt = productoTallaService
                    .getByProductoIdAndTallaId(prod.getId(), pp.getProductoTalla().getId());
            BigDecimal precio = pt.getPrecioVenta();
            BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(pp.getCantidad()));

            return new PedidoItemDTO(
                    pp.getId(),
                    prod.getId(),
                    prod.getNombre(),
                    imgs,
                    prod.getSubcategoria().getNombre(),
                    pp.getProductoTalla().getNombre(),
                    precio,
                    pp.getCantidad(),
                    subtotal
            );
        }).collect(Collectors.toList());

        // 5) Total del pedido
        BigDecimal total = items.stream()
                .map(PedidoItemDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PedidoDTO(
                p.getId(),
                fact,
                admin,
                p.getEstado(),
                p.getFechaCreacion(),
                p.getFechaEntrega(),
                items,
                total
        );
    }
}

