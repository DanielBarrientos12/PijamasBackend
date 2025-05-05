package com.neosoft.pijamasbakend.models;

import com.neosoft.pijamasbakend.utils.ImagenData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductoResponseDto {

    private Integer id;
    private String nombre;
    private String descripcion;
    private String genero;
    private Boolean activo;
    private LocalDate fechaCreacion;
    private Integer subcategoriaId;
    private List<ImagenData> imagenes;

}
