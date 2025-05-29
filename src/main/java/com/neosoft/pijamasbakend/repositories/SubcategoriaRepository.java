package com.neosoft.pijamasbakend.repositories;

import com.neosoft.pijamasbakend.entities.Subcategoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubcategoriaRepository extends JpaRepository<Subcategoria, Integer> {
    List<Subcategoria> findByCategoriaId(Integer categoriaId);
}
