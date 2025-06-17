package com.neosoft.pijamasbakend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VentasPorMesDto {
    private Integer mes;
    private BigDecimal total;
}
