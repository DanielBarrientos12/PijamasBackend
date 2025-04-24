package com.neosoft.pijamasbakend.repositories;

import com.neosoft.pijamasbakend.entities.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
}
