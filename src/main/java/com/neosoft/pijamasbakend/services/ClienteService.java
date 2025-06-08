package com.neosoft.pijamasbakend.services;

import com.neosoft.pijamasbakend.entities.Cliente;
import com.neosoft.pijamasbakend.repositories.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Cliente createCliente(Cliente cliente) {
        String passwordEncoded = passwordEncoder.encode(cliente.getHashedPassword());
        cliente.setHashedPassword(passwordEncoded);
        cliente.setRol("CLIENTE");
        return clienteRepository.save(cliente);
    }

    public Cliente findById(int id) {
        return clienteRepository.findById(id).orElse(null);
    }

    public Cliente findByEmail(String email) {
        return clienteRepository.findByEmail(email).orElse(null);
    }

    public List<Cliente> getAllClientes() {
        return clienteRepository.findAll();
    }

}
