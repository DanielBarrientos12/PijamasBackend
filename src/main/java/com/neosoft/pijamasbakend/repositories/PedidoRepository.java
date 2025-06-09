package com.neosoft.pijamasbakend.repositories;

import com.neosoft.pijamasbakend.entities.Pedido;
import com.neosoft.pijamasbakend.enums.EstadoPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
    List<Pedido> findByEstado(EstadoPedido estado);
    List<Pedido> findByFacturaClienteId(Integer clienteId);
}
