package com.neosoft.pijamasbakend.models;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductoTallaDto {

    private Integer productoId;
    private Integer tallaId;
    private BigDecimal precioCompra;
    private BigDecimal precioVenta;
    private Integer stockActual;

}
