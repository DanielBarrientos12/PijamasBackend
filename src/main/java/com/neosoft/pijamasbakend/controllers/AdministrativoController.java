package com.neosoft.pijamasbakend.controllers;

import com.neosoft.pijamasbakend.entities.Administrativo;
import com.neosoft.pijamasbakend.services.AdministrativoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/administrativos")
public class AdministrativoController {

    @Autowired
    private AdministrativoService administrativoService;

    @GetMapping
    public List<Administrativo> getAllAdministrativos() {
        return administrativoService.getAllAdministrativos();
    }

    @PostMapping
    public ResponseEntity<Administrativo> createAdministrativo(@RequestBody Administrativo administrativo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(administrativoService.saveAdministrativo(administrativo));
    }

}
