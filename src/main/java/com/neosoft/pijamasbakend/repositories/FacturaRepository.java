package com.neosoft.pijamasbakend.repositories;

import com.neosoft.pijamasbakend.entities.Factura;
import com.neosoft.pijamasbakend.enums.EstadoFactura;
import com.neosoft.pijamasbakend.models.VentasPorMesDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FacturaRepository extends JpaRepository<Factura, Integer> {
    List<Factura> findByClienteId(Integer clienteId);
    Optional<Factura> findByReferencia(String ref);
    List<Factura> findByEstado(EstadoFactura estado);

    @Query("""
      SELECT new com.neosoft.pijamasbakend.models.VentasPorMesDto(
        MONTH(f.fechaRegistro),
        SUM(f.totalNeto)
      )
      FROM Factura f
      WHERE f.estado = com.neosoft.pijamasbakend.enums.EstadoFactura.PAGADA
      GROUP BY MONTH(f.fechaRegistro)
      ORDER BY MONTH(f.fechaRegistro)
    """)
    List<VentasPorMesDto> findVentasMensualesPagadas();
}
