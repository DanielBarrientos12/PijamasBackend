package com.neosoft.pijamasbakend.models;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductoDto {

    private String nombre;
    private Integer subcategoriaId;
    private String descripcion;
    private String genero;
    private Boolean activo;
    List<MultipartFile> imagenes;
    List<Integer> imagenesEliminadas;
    private Integer tallaId;
    private BigDecimal precioCompra;
    private BigDecimal precioVenta;
    private Integer agregarStock;
    private String observaciones;

}
