package com.neosoft.pijamasbakend.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdministrativoDTO {
    private Integer id;
    private String nombre;
    private String apellido;
}
