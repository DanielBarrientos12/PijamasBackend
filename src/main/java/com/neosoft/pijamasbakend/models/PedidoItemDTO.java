package com.neosoft.pijamasbakend.models;

import com.neosoft.pijamasbakend.utils.ImagenData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PedidoItemDTO {
    private Integer id;
    private Integer productoId;
    private String productoNombre;
    private List<ImagenData> imagenes;
    private String subcategoria;
    private String talla;
    private BigDecimal precioVenta;
    private Integer cantidad;
    private BigDecimal subtotal;
}
