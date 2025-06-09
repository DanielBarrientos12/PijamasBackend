package com.neosoft.pijamasbakend.controllers;

import com.neosoft.pijamasbakend.enums.EstadoPedido;
import com.neosoft.pijamasbakend.entities.Pedido;
import com.neosoft.pijamasbakend.services.PedidoService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @GetMapping
    public ResponseEntity<List<Pedido>> listarPedidos(@RequestParam Optional<EstadoPedido> estado) {
        List<Pedido> lista = pedidoService.listarPedidos(estado);
        if (lista.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pedido> buscarPorId(@PathVariable("id") Integer id) {
        try {
            Pedido p = pedidoService.buscarPorIdPedido(id);
            return ResponseEntity.ok(p);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}/entregar")
    public ResponseEntity<Pedido> marcarEntregado(@PathVariable("id") Integer id) {
        try {
            Pedido p = pedidoService.marcarEntregado(id);
            return ResponseEntity.ok(p);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}/enviar")
    public ResponseEntity<Pedido> marcarEnviado(@PathVariable("id") Integer id) {
        try {
            Pedido p = pedidoService.marcarEnviado(id);
            return ResponseEntity.ok(p);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<Pedido>> listarPorCliente(@PathVariable("clienteId") Integer clienteId) {
        List<Pedido> lista = pedidoService.listarPedidosPorCliente(clienteId);
        if (lista.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(lista);
    }
}
