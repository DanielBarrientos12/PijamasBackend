package com.neosoft.pijamasbakend.services;

import com.neosoft.pijamasbakend.entities.Administrativo;
import com.neosoft.pijamasbakend.repositories.AdministrativoRepository;
import com.neosoft.pijamasbakend.repositories.RolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdministrativoService {

    @Autowired
    private AdministrativoRepository adminRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Administrativo> getAllAdministrativos() {
        return adminRepo.findAll();
    }

    @Transactional
    public Administrativo saveAdministrativo(Administrativo administrativo) {
        String passwordEncoded = passwordEncoder.encode(administrativo.getHashedPassword());
        administrativo.setHashedPassword(passwordEncoded);
        administrativo.setLastUpdate(LocalDateTime.now());
        return adminRepo.save(administrativo);
    }

    public Administrativo findById(int id) {
        return adminRepo.findById(id).orElse(null);
    }

    public Administrativo findByEmail(String email) {
        return adminRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("No existe administrativo con email: " + email));
    }

    public Administrativo updateAdministrativo(int id, Administrativo administrativo) {
        Administrativo admin = findById(id);
        admin.setNombre(administrativo.getNombre());
        admin.setApellido(administrativo.getApellido());
        return admin;
    }

}
