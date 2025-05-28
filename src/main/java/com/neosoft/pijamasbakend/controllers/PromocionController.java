package com.neosoft.pijamasbakend.controllers;

import com.neosoft.pijamasbakend.entities.Promocion;
import com.neosoft.pijamasbakend.models.PromocionDto;
import com.neosoft.pijamasbakend.models.PromocionProductoDto;
import com.neosoft.pijamasbakend.services.PromocionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promociones")
public class PromocionController {

    @Autowired
    private PromocionService promocionService;

    @PostMapping
    public ResponseEntity<Promocion> crear(@RequestBody PromocionDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(promocionService.crear(dto));
    }

    @PostMapping("/{idpromocion}/productos")
    public ResponseEntity<Void> asignar(@PathVariable Integer idpromocion, @RequestBody List<PromocionProductoDto> productos) {
        promocionService.asignarProductos(idpromocion, productos);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Promocion> actualizar(@PathVariable Integer id, @RequestBody PromocionDto dto) {
        return ResponseEntity.ok(promocionService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        promocionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<Promocion> todas() {
        return promocionService.todas();
    }

}
