package com.neosoft.pijamasbakend.repositories;

import com.neosoft.pijamasbakend.entities.ProductoTalla;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoTallaRepository extends JpaRepository<ProductoTalla, Integer> {

    boolean existsByProductoIdAndTallaId(Integer productoId, Integer tallaId);
    Optional<ProductoTalla> findByProductoIdAndTallaId(Integer productoId, Integer tallaId);

}
