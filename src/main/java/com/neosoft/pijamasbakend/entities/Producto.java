package com.neosoft.pijamasbakend.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nombre;

    @ManyToOne
    @JoinColumn(name = "subcategoria_id")
    private Subcategoria subcategoria;

    @Column(name = "descripcion", columnDefinition = "text")
    private String descripcion;

    private String genero;

    private Boolean activo;

    @Column(name = "ruta_imagen")
    private String rutaImagen;

    @Column(name = "fecha_creacion")
    private LocalDate fechaCreacion;

}