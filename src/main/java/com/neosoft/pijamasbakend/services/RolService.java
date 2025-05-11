package com.neosoft.pijamasbakend.services;

import com.neosoft.pijamasbakend.entities.Rol;
import com.neosoft.pijamasbakend.repositories.RolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RolService {

    @Autowired
    private RolRepository rolRepo;

    public List<Rol> getAllRoles() {
        return rolRepo.findAll();
    }

}
