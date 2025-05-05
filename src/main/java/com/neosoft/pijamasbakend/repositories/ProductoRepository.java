package com.neosoft.pijamasbakend.repositories;

import com.neosoft.pijamasbakend.entities.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    List<Producto> findByActivoTrue();

    @Query("SELECT DISTINCT p FROM Producto p LEFT JOIN FETCH p.imagenes")
    List<Producto> findAllConImagenes();

}
