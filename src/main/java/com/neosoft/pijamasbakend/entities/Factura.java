package com.neosoft.pijamasbakend.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Entity
@Table(name = "factura")
public class Factura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    private String referencia;

    @Column(name = "metodo_pago")
    private String metodoPago;

    @Column(name = "total_bruto", precision = 12, scale = 2)
    private BigDecimal totalBruto;

    @Column(precision = 12, scale = 2)
    private BigDecimal envio;

    @Column(name = "total_descuento", precision = 12, scale = 2)
    private BigDecimal totalDescuento;

    @Column(name = "total_neto", precision = 12, scale = 2)
    private BigDecimal totalNeto;

    @Column(name = "fecha_registro")
    private Instant fechaRegistro;

    @Column(name = "fecha_pago")
    private Instant fechaPago;

    @Column(name = "wompi_id")
    private String wompiId;

    @Column(name = "wompi_status")
    private String wompiStatus;

    @Column(name = "wompi_payment_url")
    private String wompiPaymentUrl;

    @Column(name = "wompi_authorization_code")
    private String wompiAuthorizationCode;

}