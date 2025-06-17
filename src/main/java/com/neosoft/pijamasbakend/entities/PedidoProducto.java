package com.neosoft.pijamasbakend.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "pedido_producto")
public class PedidoProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne
    @JoinColumn(name = "talla_id")
    private Talla productoTalla;

    @Column(nullable = false)
    private Integer cantidad;

}