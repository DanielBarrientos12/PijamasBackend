package com.neosoft.pijamasbakend.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClienteDTO {
    private Integer id;
    private String nombre;
    private String apellido;
    private String numeroDocumento;
    private String email;
    private String telefono;
    private String direccionEnvio;
    private String codigoPostal;
}
