package com.neosoft.pijamasbakend.repositories;

import com.neosoft.pijamasbakend.entities.ProductoImagen;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoImagenRepository extends JpaRepository<ProductoImagen, Integer> {
    int countByProductoId(Integer productoId);
}
