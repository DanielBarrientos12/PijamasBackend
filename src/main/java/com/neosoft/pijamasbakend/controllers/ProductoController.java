package com.neosoft.pijamasbakend.controllers;

import com.neosoft.pijamasbakend.entities.Producto;
import com.neosoft.pijamasbakend.models.ProductoDto;
import com.neosoft.pijamasbakend.models.ProductoResponseDto;
import com.neosoft.pijamasbakend.services.ProductoService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @PostMapping
    public ResponseEntity<?> createProducto(@ModelAttribute ProductoDto dto) {
        try {
            Producto creado = productoService.createProducto(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al procesar imágenes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error inesperado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProducto(@PathVariable Integer id, @ModelAttribute ProductoDto dto) {
        try {
            Producto actualizado = productoService.updateProducto(id, dto);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al procesar imágenes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductoById(@PathVariable Integer id) {
        try {
            ProductoResponseDto dto = productoService.findById(id);
            return ResponseEntity.ok(dto);
        } catch (EntityNotFoundException e) {
            return notFound(e);
        } catch (Exception e) {
            return internalError("Error inesperado: " + e.getMessage());
        }
    }

    @GetMapping("/todos")
    public ResponseEntity<List<ProductoResponseDto>> listarTodosProductos() {
        List<ProductoResponseDto> respuesta = productoService.getAllProductos();
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/activos")
    public ResponseEntity<List<ProductoResponseDto>> listarActivos() {
        List<ProductoResponseDto> activos = productoService.getAllActiveProductos();
        return ResponseEntity.ok(activos);
    }

    @GetMapping("/categoria/{id}")
    public ResponseEntity<List<ProductoResponseDto>> listarPorCategoria(@PathVariable Integer id) {
        List<ProductoResponseDto> productoResponseDtoList = productoService.getProductosPorCategoria(id);
        return ResponseEntity.ok(productoResponseDtoList);
    }

    private ResponseEntity<Map<String, String>> notFound(Exception e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    private ResponseEntity<Map<String, String>> internalError(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

}
