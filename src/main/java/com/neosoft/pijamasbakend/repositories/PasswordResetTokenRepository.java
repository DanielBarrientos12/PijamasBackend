package com.neosoft.pijamasbakend.repositories;

import com.neosoft.pijamasbakend.entities.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {

    // Para buscar un token vigente y sin usar
    Optional<PasswordResetToken> findByEmailAndCodeAndUsedFalse(String email, String code);

    // Sirve para eliminar cualquier token previo cuando el usuario solicita uno nuevo
    void deleteByEmail(String email);

}
