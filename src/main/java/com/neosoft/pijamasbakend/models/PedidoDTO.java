package com.neosoft.pijamasbakend.models;

import com.neosoft.pijamasbakend.enums.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class PedidoDTO {
    private Integer id;
    private FacturaDTO factura;
    private AdministrativoDTO responsable;
    private EstadoPedido estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaEntrega;
    private List<PedidoItemDTO> items;
    private BigDecimal totalPedido;
}
