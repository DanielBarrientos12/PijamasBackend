package com.neosoft.pijamasbakend.repositories;

import com.neosoft.pijamasbakend.entities.ProductoTalla;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoTallaRepository extends JpaRepository<ProductoTalla, Integer> {

    // Verifica existencia de la variante
    boolean existsByProductoIdAndTallaId(Integer productoId, Integer tallaId);

    // Recupera la variante concreta (para update y delete)
    Optional<ProductoTalla> findByProductoIdAndTallaId(Integer productoId, Integer tallaId);

    // Lista todas las variantes de un producto
    List<ProductoTalla> findByProductoId(Integer productoId);

}
