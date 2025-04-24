package com.neosoft.pijamasbakend.repositories;

import com.neosoft.pijamasbakend.entities.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {
}
