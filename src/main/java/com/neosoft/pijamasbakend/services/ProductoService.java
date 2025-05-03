package com.neosoft.pijamasbakend.services;

import com.neosoft.pijamasbakend.entities.Producto;
import com.neosoft.pijamasbakend.entities.Subcategoria;
import com.neosoft.pijamasbakend.models.ProductoDto;
import com.neosoft.pijamasbakend.repositories.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepo;

    @Autowired
    private SubcategoriaService subcategoriaService;

    @Autowired
    private ProductoImagenService productoImagenService;

    public Producto createProducto(ProductoDto dto) throws IOException {
        Subcategoria subcat = subcategoriaService.findById(dto.getSubcategoriaId());
        if (subcat == null) {
            throw new RuntimeException("Subcategoría con id " + dto.getSubcategoriaId() + " no encontrada.");
        }

        Producto producto = new Producto();
        producto.setNombre(dto.getNombre());
        producto.setSubcategoria(subcat);
        producto.setDescripcion(dto.getDescripcion());
        producto.setGenero(dto.getGenero());
        producto.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        producto.setFechaCreacion(LocalDate.now());

        productoRepo.save(producto);

        productoImagenService.guardarImagenesParaProducto(producto, dto.getImagenes());

        return producto;
    }

    public Producto updateProducto(Integer id, ProductoDto dto) throws IOException {
        Producto producto = productoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto con id: " + id + " no encontrado."));
        Subcategoria subcat = subcategoriaService.findById(dto.getSubcategoriaId());
        if (subcat == null) {
            throw new RuntimeException("Subcategoría con id " + dto.getSubcategoriaId() + " no encontrada.");
        }

        producto.setNombre(dto.getNombre());
        producto.setSubcategoria(subcat);
        producto.setDescripcion(dto.getDescripcion());
        producto.setGenero(dto.getGenero());
        producto.setActivo(dto.getActivo() != null ? dto.getActivo() : producto.getActivo());

        productoRepo.save(producto);

        productoImagenService.guardarImagenesParaProducto(producto, dto.getImagenes());

        return producto;
    }

    public List<Producto> getAllProductos() {
        return productoRepo.findAll();
    }

    public List<Producto> getAllActiveProductos() {
        return productoRepo.findByActivoTrue();
    }

    public Producto findById(Integer id) {
        return productoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto con id: " + id + " no encontrado."));
    }
}
