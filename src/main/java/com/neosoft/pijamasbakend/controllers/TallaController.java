package com.neosoft.pijamasbakend.controllers;

import com.neosoft.pijamasbakend.models.TallaDto;
import com.neosoft.pijamasbakend.entities.Talla;
import com.neosoft.pijamasbakend.services.TallaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tallas")
public class TallaController {

    @Autowired
    private TallaService tallaService;

    @PostMapping
    public ResponseEntity<Talla> createTalla(@RequestBody TallaDto tallaDto) {
        Talla talla = tallaService.createTalla(tallaDto);
        return ResponseEntity.ok(talla);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Talla> findById(@PathVariable int id) {
        Talla talla = tallaService.findById(id);
        return ResponseEntity.ok(talla);
    }

    @GetMapping
    public ResponseEntity<Iterable<Talla>> getAllTallas() {
        return ResponseEntity.ok(tallaService.getAllTallas());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Talla> updateTalla(@PathVariable int id, @RequestBody TallaDto tallaDto) {
        Talla talla = tallaService.updateTalla(id, tallaDto);
        return ResponseEntity.ok(talla);
    }


}
