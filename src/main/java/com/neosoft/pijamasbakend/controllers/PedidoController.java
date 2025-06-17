package com.neosoft.pijamasbakend.controllers;

import com.neosoft.pijamasbakend.enums.EstadoPedido;
import com.neosoft.pijamasbakend.models.PedidoDTO;
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
    public ResponseEntity<List<PedidoDTO>> listarPedidos(
            @RequestParam Optional<EstadoPedido> estado) {
        List<PedidoDTO> lista = pedidoService.listarPedidos(estado);
        if (lista.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoDTO> buscarPorId(@PathVariable("id") Integer id) {
        try {
            PedidoDTO dto = pedidoService.buscarPorIdPedido(id);
            return ResponseEntity.ok(dto);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}/entregar")
    public ResponseEntity<PedidoDTO> marcarEntregado(@PathVariable("id") Integer id) {
        try {
            PedidoDTO dto = pedidoService.marcarEntregado(id);
            return ResponseEntity.ok(dto);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}/enviar")
    public ResponseEntity<PedidoDTO> marcarEnviado(@PathVariable("id") Integer id) {
        try {
            PedidoDTO dto = pedidoService.marcarEnviado(id);
            return ResponseEntity.ok(dto);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<PedidoDTO>> listarPorCliente(
            @PathVariable("clienteId") Integer clienteId) {
        List<PedidoDTO> lista = pedidoService.listarPedidosPorCliente(clienteId);
        if (lista.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(lista);
    }
}
