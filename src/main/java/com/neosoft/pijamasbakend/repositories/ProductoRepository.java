package com.neosoft.pijamasbakend.repositories;

import com.neosoft.pijamasbakend.entities.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    List<Producto> findByActivoTrue();

}
