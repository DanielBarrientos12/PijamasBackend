package com.neosoft.pijamasbakend.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "agregar_inventario")
public class AgregarInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_talla_id", nullable = false)
    private ProductoTalla productoTalla;

    @Column(name = "cantidad_agregada", nullable = false)
    private Integer cantidadAgregada;

    @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP")
    private LocalDateTime fecha;

    @Lob
    @Column(columnDefinition = "text")
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "administrativo_id", nullable = false)
    private Administrativo administrativo;
}
