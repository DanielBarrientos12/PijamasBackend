package com.neosoft.pijamasbakend.models;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductoTallaDto {

    private Integer productoId;
    private Integer tallaId;
    private BigDecimal precioCompra;
    private BigDecimal precioVenta;
    private Integer agregarStock;

}
