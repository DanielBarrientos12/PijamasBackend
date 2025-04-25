package com.neosoft.pijamasbakend.controllers;

import com.neosoft.pijamasbakend.entities.Producto;
import com.neosoft.pijamasbakend.models.ProductoDto;
import com.neosoft.pijamasbakend.services.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @PostMapping
    public ResponseEntity<Producto> create(@ModelAttribute ProductoDto dto) throws IOException {
        Producto creado = productoService.createProducto(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("{id}")
    public ResponseEntity<Producto> update(@PathVariable int id,@ModelAttribute ProductoDto dto) throws IOException {
        Producto actualizado = productoService.updateProducto(id, dto);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/imagenes/{imagenId}")
    public void deleteImage(@PathVariable int imagenId) throws IOException {
        productoService.deleteProductoImagen(imagenId);
    }



}
