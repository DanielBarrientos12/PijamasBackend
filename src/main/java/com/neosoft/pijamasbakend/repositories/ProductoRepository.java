package com.neosoft.pijamasbakend.repositories;

import com.neosoft.pijamasbakend.entities.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    List<Producto> findBySubcategoriaCategoriaIdAndActivoTrue(Integer categoriaId);
    List<Producto> findByActivoTrue();
    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    @Query("SELECT DISTINCT p FROM Producto p LEFT JOIN FETCH p.imagenes")
    List<Producto> findAllConImagenes();

}
