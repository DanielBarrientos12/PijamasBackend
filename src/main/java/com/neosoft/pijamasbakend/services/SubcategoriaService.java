package com.neosoft.pijamasbakend.services;

import com.neosoft.pijamasbakend.entities.Categoria;
import com.neosoft.pijamasbakend.entities.Subcategoria;
import com.neosoft.pijamasbakend.models.SubcategoriaDto;
import com.neosoft.pijamasbakend.repositories.SubcategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubcategoriaService {

    @Autowired
    private SubcategoriaRepository subcategoriaRepository;

    @Autowired
    private CategoriaService categoriaService;

    public Subcategoria createSubcategoria(int id, SubcategoriaDto subcategoriaDto) {
        Subcategoria subcategoria = new Subcategoria();
        Categoria categoria = categoriaService.findById(id);

        subcategoria.setNombre(subcategoriaDto.getNombre());
        subcategoria.setCategoria(categoria);
        subcategoria.setDescripcion(subcategoriaDto.getDescripcion());
        return subcategoriaRepository.save(subcategoria);
    }

    public Subcategoria findById(int id) {
        return subcategoriaRepository.findById(id).orElse(null);
    }

    public List<Subcategoria> getAllSubcategorias() {
        return subcategoriaRepository.findAll();
    }

    public Subcategoria updateSubcategoria(int id, SubcategoriaDto subcategoriaDto) {
        Subcategoria subcategoria = findById(id);
        subcategoria.setNombre(subcategoriaDto.getNombre());
        subcategoria.setDescripcion(subcategoriaDto.getDescripcion());
        return subcategoriaRepository.save(subcategoria);
    }

    public void deleteSubcategoria(int id) {
        if (!subcategoriaRepository.existsById(id)) {
            throw new RuntimeException("No existe subcategoria con id: " + id + " para eliminarla.");
        }
        if (subcategoriaRepository.existsById(id)) {
            throw new IllegalStateException("No se puede eliminar: hay productos asociados");
        }
        subcategoriaRepository.deleteById(id);
    }

}
