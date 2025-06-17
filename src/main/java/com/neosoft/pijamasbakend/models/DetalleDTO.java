package com.neosoft.pijamasbakend.models;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DetalleDTO {
    private Integer id;
    private ProductoResponseDto producto;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
}
