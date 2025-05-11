package com.neosoft.pijamasbakend.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "mensaje_chat")
public class MensajeChat {


  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;


  @ManyToOne
  @JoinColumn(name = "chat_id")
  private Chat chat;

  @ManyToOne
  @JoinColumn(name = "administrativo_id")
  private Administrativo administrativo;

  @Column(name = "es_cliente")
  private Boolean esCliente = false;

  private String contenido;

  @Column(name = "fecha_envio")
  private OffsetDateTime fechaEnvio;

}