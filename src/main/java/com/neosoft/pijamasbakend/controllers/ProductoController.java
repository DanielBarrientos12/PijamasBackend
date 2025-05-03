package com.neosoft.pijamasbakend.controllers;

import com.neosoft.pijamasbakend.entities.Producto;
import com.neosoft.pijamasbakend.models.ProductoDto;
import com.neosoft.pijamasbakend.services.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @PostMapping
    public ResponseEntity<Producto> createProducto(@ModelAttribute ProductoDto dto) throws IOException {
        Producto creado = productoService.createProducto(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> updateProducto(@PathVariable Integer id,@ModelAttribute ProductoDto dto) throws IOException {
        Producto actualizado = productoService.updateProducto(id, dto);
        return ResponseEntity.ok(actualizado);
    }

    @GetMapping
    public ResponseEntity<List<Producto>> getAllProductos() {
        List<Producto> lista = productoService.getAllProductos();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/activos")
    public ResponseEntity<List<Producto>> getActiveProductos() {
        List<Producto> activos = productoService.getAllActiveProductos();
        return ResponseEntity.ok(activos);
    }
}
