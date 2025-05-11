package com.neosoft.pijamasbakend.controllers;

import com.neosoft.pijamasbakend.entities.Cliente;
import com.neosoft.pijamasbakend.services.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @PostMapping("/register")
    public ResponseEntity<Cliente> registerCliente(@RequestBody Cliente cliente) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.createCliente(cliente));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> findById(@PathVariable int id) {
        return clienteService.findById(id) != null ? ResponseEntity
                .ok(clienteService.findById(id)) : ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<Iterable<Cliente>> getAllClientes() {
        return ResponseEntity.ok(clienteService.getAllClientes());
    }

}
