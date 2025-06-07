package com.neosoft.pijamasbakend.repositories;

import com.neosoft.pijamasbakend.entities.Factura;
import com.neosoft.pijamasbakend.enums.EstadoFactura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FacturaRepository extends JpaRepository<Factura, Integer> {

    Optional<Factura> findByReferencia(String ref);
    List<Factura> findByEstado(EstadoFactura estado);

}
