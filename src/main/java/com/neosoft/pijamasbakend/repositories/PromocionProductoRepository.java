package com.neosoft.pijamasbakend.repositories;

import com.neosoft.pijamasbakend.entities.PromocionProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;


public interface PromocionProductoRepository extends JpaRepository<PromocionProducto, Integer> {

    @Query("""
               select pp
               from PromocionProducto pp
               join pp.promocion pr
               where pp.producto.id = :productoId
                 and pr.activo = true
                 and :today between pr.fechaInicio and pr.fechaFin
            """)
    List<PromocionProducto> findAplicables(@Param("productoId") Integer productoId,
                                           @Param("today") LocalDate today);

}
