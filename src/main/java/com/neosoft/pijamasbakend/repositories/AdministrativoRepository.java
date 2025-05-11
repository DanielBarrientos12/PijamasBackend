package com.neosoft.pijamasbakend.repositories;

import com.neosoft.pijamasbakend.entities.Administrativo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdministrativoRepository extends JpaRepository<Administrativo, Integer> {

    Optional<Administrativo> findByEmail(String email);

}
