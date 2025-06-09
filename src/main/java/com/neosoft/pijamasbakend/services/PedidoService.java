package com.neosoft.pijamasbakend.services;

import com.neosoft.pijamasbakend.entities.*;
import com.neosoft.pijamasbakend.enums.EstadoPedido;
import com.neosoft.pijamasbakend.models.CheckoutItemDTO;
import com.neosoft.pijamasbakend.repositories.AdministrativoRepository;
import com.neosoft.pijamasbakend.repositories.PedidoRepository;
import com.neosoft.pijamasbakend.repositories.ProductoTallaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PedidoService {
    @Autowired
    private PedidoRepository pedidoRepo;
    @Autowired
    private AdministrativoRepository adminRepo;
    @Autowired
    private ProductoTallaRepository productoRepo;

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

    public List<Pedido> listarPedidos(Optional<EstadoPedido> estado) {
        return estado.map(pedidoRepo::findByEstado).orElseGet(pedidoRepo::findAll);
    }

    public Pedido buscarPorIdPedido(Integer idPedido) {
        return pedidoRepo.findById(idPedido).orElseThrow(() -> new EntityNotFoundException("Pedido no existe"));
    }

    @Transactional
    public Pedido marcarEntregado(Integer pedidoId) {
        Pedido p = pedidoRepo.findById(pedidoId).orElseThrow(() -> new EntityNotFoundException("Pedido no existe"));
        p.setEstado(EstadoPedido.ENTREGADO);
        p.setFechaEntrega(LocalDateTime.now());
        return pedidoRepo.save(p);
    }

    @Transactional
    public Pedido marcarEnviado(Integer pedidoId) {
        Pedido p = pedidoRepo.findById(pedidoId).orElseThrow(() -> new EntityNotFoundException("Pedido no existe"));
        p.setEstado(EstadoPedido.ENVIADO);
        p.setFechaEntrega(LocalDateTime.now());
        return pedidoRepo.save(p);
    }


    public List<Pedido> listarPedidosPorCliente(Integer clienteId) {
        return pedidoRepo.findByFacturaClienteId(clienteId);
    }

}

