package com.neosoft.pijamasbakend.services;

import com.neosoft.pijamasbakend.dto.TallaDto;
import com.neosoft.pijamasbakend.entities.Categoria;
import com.neosoft.pijamasbakend.entities.Talla;
import com.neosoft.pijamasbakend.repositories.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;


}
