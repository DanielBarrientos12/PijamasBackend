package com.neosoft.pijamasbakend.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "talla")
public class Talla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nombre;

}