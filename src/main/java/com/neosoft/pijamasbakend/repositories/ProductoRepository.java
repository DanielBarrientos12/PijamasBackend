package com.neosoft.pijamasbakend.repositories;

import com.neosoft.pijamasbakend.entities.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    Page<Producto> findByActivoTrue(Pageable pageable);

}
