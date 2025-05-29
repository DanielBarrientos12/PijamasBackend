package com.neosoft.pijamasbakend.controllers;

import com.neosoft.pijamasbakend.models.RestablecerPassDto;
import com.neosoft.pijamasbakend.models.SolicitarResetDto;
import com.neosoft.pijamasbakend.models.VerificarCodigoDto;
import com.neosoft.pijamasbakend.services.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/password-reset")
public class PasswordResetController {

    @Autowired
    private PasswordResetService service;

    @PostMapping("/request")
    public ResponseEntity<Void> solicitar(@RequestBody SolicitarResetDto dto) {
        service.solicitarCodigo(dto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PostMapping("/verify")
    public ResponseEntity<Void> verificar(@RequestBody VerificarCodigoDto dto) {
        service.verificarCodigo(dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset")
    public ResponseEntity<Void> reset(@RequestBody RestablecerPassDto dto) {
        service.restablecer(dto);
        return ResponseEntity.noContent().build();
    }

}
