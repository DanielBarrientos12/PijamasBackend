package com.neosoft.pijamasbakend.models;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PromocionDto {

    private String codigo;
    private String descripcion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Boolean activo;

    public Boolean validarActivo() {
        LocalDate fechaActual = LocalDate.now();
        return fechaFin.isAfter(fechaInicio) && fechaActual.isAfter(fechaInicio) && !fechaActual.isAfter(fechaFin);
    }

}
