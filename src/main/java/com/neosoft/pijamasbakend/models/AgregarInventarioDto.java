package com.neosoft.pijamasbakend.models;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AgregarInventarioDto {

    private Integer productoId;
    private Integer tallaId;
    private Integer cantidadAgregada;
    private String observaciones;
    private String email;
    private BigDecimal precioCompra;
    private BigDecimal precioVenta;
    private Integer agregarStock;

}
