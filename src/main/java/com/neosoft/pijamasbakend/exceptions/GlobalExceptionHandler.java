// src/main/java/com/neosoft/pijamasbakend/exceptions/GlobalExceptionHandler.java
package com.neosoft.pijamasbakend.exceptions;

import com.neosoft.pijamasbakend.models.ApiError;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        // Captura el mensaje detallado que viene de PostgreSQL
        String rootMsg = ex.getMostSpecificCause().getMessage().toLowerCase();
        String userMsg = "Error de integridad de datos";

        if (rootMsg.contains("uq_cliente_email")) {
            userMsg = "El correo ya está registrado, pruebe con otro";
        } else if (rootMsg.contains("uq_cliente_numero_documento")) {
            userMsg = "El número de documento ya está registrado, pruebe con otro";
        } else if (rootMsg.contains("uq_administrativo_email")) {
            userMsg = "El correo del administrativo ya está registrado, pruebe con otro";
        }

        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                userMsg,
                LocalDateTime.now()
        );
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }
}
