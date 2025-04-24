package com.neosoft.pijamasbakend.controllers;

import com.neosoft.pijamasbakend.dto.CategoriaDto;
import com.neosoft.pijamasbakend.entities.Categoria;
import com.neosoft.pijamasbakend.services.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @GetMapping
    public List<Categoria> getAllCategorias(){
        return categoriaService.getAllCategorias();
    }

    @GetMapping("/{id}")
    public Categoria findById(@PathVariable int id){
        return categoriaService.findById(id);
    }

    @PostMapping
    public Categoria createCategoria(@RequestBody CategoriaDto categoriaDto){
        return categoriaService.createCategoria(categoriaDto);
    }

    @PutMapping("/{id}")
    public Categoria updateCategoria(@PathVariable int id, @RequestBody CategoriaDto categoriaDto){
        return categoriaService.updateCategoria(id, categoriaDto);
    }

}
