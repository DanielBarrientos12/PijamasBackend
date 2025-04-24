package com.neosoft.pijamasbakend.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "factura_revision")
public class FacturaRevision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "factura_id", nullable = false)
    private Factura factura;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Administrativo usuario;

    @Lob
    @Column(columnDefinition = "text")   // Mapea exactamente a "text"
    private String comentario;

    @NotNull
    @Size(max = 10)
    @ColumnDefault("'PENDIENTE'")
    @Column(name = "estado", nullable = false, length = 10)
    private String estado;

    @NotNull
    @CreationTimestamp
    @Column(name = "fecha_revision", nullable = false)
    private LocalDateTime fechaRevision;
}