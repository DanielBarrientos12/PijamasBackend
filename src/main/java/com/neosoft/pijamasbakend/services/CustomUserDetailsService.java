package com.neosoft.pijamasbakend.services;

import com.neosoft.pijamasbakend.repositories.AdministrativoRepository;
import com.neosoft.pijamasbakend.repositories.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private ClienteRepository clienteRepo;

    @Autowired
    private AdministrativoRepository adminRepo;

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        return clienteRepo.findByEmail(email)
                .map(c -> User.withUsername(c.getEmail())
                        .password(c.getHashedPassword())
                        .roles(c.getRol().toUpperCase())
                        .build()
                )
                .or(() -> adminRepo.findByEmail(email).map(a ->
                        User.withUsername(a.getEmail())
                                .password(a.getHashedPassword())
                                .roles(a.getRol().getNombre().toUpperCase())
                                .build()
                ))
                .orElseThrow(() ->
                        new UsernameNotFoundException("No se encontró usuario con email: " + email)
                );
    }
}
