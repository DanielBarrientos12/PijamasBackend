package com.neosoft.pijamasbakend.models;

import com.neosoft.pijamasbakend.enums.EstadoFactura;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
public class FacturaDTO {
    private Integer id;
    private String referencia;
    private String metodoPago;
    private BigDecimal totalNeto;
    private EstadoFactura estado;
    private Instant fechaRegistro;
    private Instant fechaPago;
}
