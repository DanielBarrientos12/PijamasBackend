package com.neosoft.pijamasbakend.services;

import com.neosoft.pijamasbakend.dto.TallaDto;
import com.neosoft.pijamasbakend.entities.Talla;
import com.neosoft.pijamasbakend.repositories.TallaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TallaService {

    @Autowired
    private TallaRepository tallaRepository;

    public Talla createTalla(TallaDto tallaDto){
        Talla talla = new Talla();
        talla.setNombre(tallaDto.getNombre());

        return tallaRepository.save(talla);
    }

    public Talla findById(int id){
        return tallaRepository.findById(id).orElse(null);
    }

    public List<Talla> getAllTallas(){
        return tallaRepository.findAll();
    }

    public Talla updateTalla(int id, TallaDto tallaDto){
        Talla talla = findById(id);
        talla.setNombre(tallaDto.getNombre());
        return tallaRepository.save(talla);
    }

}
