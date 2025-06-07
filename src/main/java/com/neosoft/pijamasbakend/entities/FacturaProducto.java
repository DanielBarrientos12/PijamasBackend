package com.neosoft.pijamasbakend.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "factura_producto")
public class FacturaProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "factura_id", nullable = false)
    private Factura factura;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promocion_id")
    private Promocion promocion;

    @Column(name = "pct_desc", precision = 5, scale = 2)
    private BigDecimal porcentajeDescuento;

    @Min(1)
    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unit", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioUnit;

    @Column(name = "descuento", precision = 10, scale = 2, nullable = false)
    private BigDecimal descuento = BigDecimal.ZERO;

}
