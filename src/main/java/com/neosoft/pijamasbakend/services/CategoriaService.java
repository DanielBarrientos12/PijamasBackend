package com.neosoft.pijamasbakend.services;

import com.neosoft.pijamasbakend.models.CategoriaDto;
import com.neosoft.pijamasbakend.entities.Categoria;
import com.neosoft.pijamasbakend.repositories.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categRepo;

    public Categoria createCategoria(CategoriaDto categoriaDto){
        Categoria categoria = new Categoria();
        categoria.setNombre(categoriaDto.getNombre());
        return categRepo.save(categoria);
    }

    public Categoria findById(int id){
        return categRepo.findById(id).orElse(null);
    }

    public List<Categoria> getAllCategorias(){
        return categRepo.findAll();
    }

    public Categoria updateCategoria(int id, CategoriaDto categoriaDto){
        Categoria categoria = findById(id);
        categoria.setNombre(categoriaDto.getNombre());
        return categRepo.save(categoria);
    }

    public void deleteCategoria(int id){
        if(!categRepo.existsById(id)){
            throw new RuntimeException("No existe categoria con id: " + id + " para eliminarla.");
        }
        if (categRepo.existsById(id)) {
            throw new IllegalStateException("No se puede eliminar: hay productos asociados");
        }
        categRepo.deleteById(id);
    }

}
