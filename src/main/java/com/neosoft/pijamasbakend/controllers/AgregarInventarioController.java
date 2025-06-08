package com.neosoft.pijamasbakend.controllers;

import com.neosoft.pijamasbakend.models.AgregarInventarioDto;
import com.neosoft.pijamasbakend.repositories.AdministrativoRepository;
import com.neosoft.pijamasbakend.services.AgregarInventarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping
@RestController("api/inventario")
public class AgregarInventarioController {

    @Autowired
    private AgregarInventarioService agregarInventarioService;

    @Autowired
    private AdministrativoRepository administrativoRepository;

    @PostMapping
    public ResponseEntity<?> agregarInventario(@RequestBody AgregarInventarioDto inventario) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        inventario.setEmail(email);

        agregarInventarioService.createInventario(inventario);

        return ResponseEntity.ok().build();
    }


}
