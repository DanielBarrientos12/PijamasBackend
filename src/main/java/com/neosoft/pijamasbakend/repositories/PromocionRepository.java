package com.neosoft.pijamasbakend.repositories;

import com.neosoft.pijamasbakend.entities.Promocion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PromocionRepository extends JpaRepository<Promocion, Integer> {

    @Query("""
               select p
               from Promocion p
               where p.activo = true
                 and :today between p.fechaInicio and p.fechaFin
            """)
    List<Promocion> findVigentes(@Param("today") LocalDate today);

    Optional<Promocion> findByCodigo(String codigo);

}
