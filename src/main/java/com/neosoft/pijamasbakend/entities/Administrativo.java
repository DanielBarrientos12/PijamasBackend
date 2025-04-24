package com.neosoft.pijamasbakend.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

// ---------- Administrativo ----------
@Data
@Entity
@Table(name = "administrativo")
public class Administrativo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nombre;
    private String apellido;

    @Column(nullable = false, unique = true)
    private String email;

    private String telefono;

    @Column(name = "hashed_password", nullable = false)
    private String hashedPassword;

    @Column(name = "last_update")
    private LocalDateTime lastUpdate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;
}
