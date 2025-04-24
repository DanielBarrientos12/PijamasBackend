package com.neosoft.pijamasbakend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductoDto {

    private String nombre;
    private Integer subcategoriaId;
    private String descripcion;
    private String genero;
    private Integer tallaId;
    private BigDecimal precioCompra;
    private BigDecimal precioVenta;

    // los campos fecha de cracion, activo y stock actual se modifican aotomaticamente o
    // el servicio de ingresar inventario

}
