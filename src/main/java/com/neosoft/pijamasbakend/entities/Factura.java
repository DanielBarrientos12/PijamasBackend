package com.neosoft.pijamasbakend.entities;

import com.neosoft.pijamasbakend.enums.EstadoFactura;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "factura")
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(nullable = false, unique = true)
    private String referencia;

    @Column(name = "metodo_pago", length = 80, nullable = false)
    private String metodoPago;

    @Column(name = "total_bruto", precision = 12, scale = 2)
    private BigDecimal totalBruto;

    @Column(precision = 12, scale = 2)
    private BigDecimal envio;

    @Column(name = "total_descuento", precision = 12, scale = 2)
    private BigDecimal totalDescuento;

    @Column(name = "total_neto", precision = 12, scale = 2)
    private BigDecimal totalNeto;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private Instant fechaRegistro = Instant.now();

    private Instant fechaPago;

    // ---- Campos Wompi ----
    @Column(name = "wompi_id", unique = true, length = 120)
    private String wompiId;

    @Column(name = "wompi_status", length = 20)
    private String wompiStatus;

    @Column(name = "wompi_payment_url")
    private String wompiPaymentUrl;

    @Column(name = "wompi_authorization_code")
    private String wompiAuthorizationCode;

    // ---- Enum estado ----
    @Enumerated(EnumType.STRING)
    @Column(length = 15, nullable = false)
    private EstadoFactura estado = EstadoFactura.CREADA;

    // ---- Relación con detalles ----
    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL)
    private List<FacturaProducto> detalles = new ArrayList<>();
}
