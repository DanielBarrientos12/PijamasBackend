package com.neosoft.pijamasbakend.entities;

import com.neosoft.pijamasbakend.enums.EstadoPedido;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Cabecera de un pedido asociado a una factura.
 */
@Entity
@Data
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id", nullable = false)
    private Factura factura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id", nullable = false)
    private Administrativo responsable;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoProducto> items = new ArrayList<>();

}