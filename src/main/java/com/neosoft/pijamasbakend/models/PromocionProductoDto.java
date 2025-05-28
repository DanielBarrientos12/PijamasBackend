package com.neosoft.pijamasbakend.models;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PromocionProductoDto {

    private Integer productoId;
    private BigDecimal descuento;

}
