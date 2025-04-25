package com.neosoft.pijamasbakend.services;

import com.neosoft.pijamasbakend.entities.Producto;
import com.neosoft.pijamasbakend.entities.Subcategoria;
import com.neosoft.pijamasbakend.entities.ProductoImagen;
import com.neosoft.pijamasbakend.models.ProductoDto;
import com.neosoft.pijamasbakend.repositories.ProductoRepository;
import com.neosoft.pijamasbakend.repositories.ProductoImagenRepository;
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
    private ProductoImagenRepository imagenRepo;

    @Autowired
    private FileService fileService;

    public Producto createProducto(ProductoDto dto) throws IOException {
        Subcategoria subcat = subcategoriaService.findById(dto.getSubcategoriaId());
        if (subcat == null) {
            throw new RuntimeException("Subcategoría con id " + dto.getSubcategoriaId() + " no encontrada.");
        }

        Producto product = new Producto();
        product.setNombre(dto.getNombre());
        product.setSubcategoria(subcat);
        product.setDescripcion(dto.getDescripcion());
        product.setGenero(dto.getGenero());
        product.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        product.setFechaCreacion(LocalDate.now());

        productoRepo.save(product);

        if (dto.getImagenes() != null && !dto.getImagenes().isEmpty()) {
            List<String> rutas = fileService.storeFiles(dto.getImagenes(), "productos/" + product.getId());
            int posicion = 1;
            for (String ruta : rutas) {
                ProductoImagen img = new ProductoImagen();
                img.setProducto(product);
                img.setUrl(ruta);
                img.setPosicion(posicion++);
                imagenRepo.save(img);
            }
        }

        return product;
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

        if (dto.getImagenes() != null && !dto.getImagenes().isEmpty()) {
            int posicion = imagenRepo.countByProductoId(id) + 1;
            List<String> rutas = fileService.storeFiles(dto.getImagenes(), "productos/" + id);
            for (String ruta : rutas) {
                ProductoImagen img = new ProductoImagen();
                img.setProducto(producto);
                img.setUrl(ruta);
                img.setPosicion(posicion++);
                imagenRepo.save(img);
            }
        }

        return producto;
    }

    public void deleteProductoImagen(Integer imagenId) throws IOException {
        ProductoImagen img = imagenRepo.findById(imagenId)
                .orElseThrow(() -> new RuntimeException("Imagen con id: " + imagenId + " no encontrada."));
        fileService.deleteFile(img.getUrl());
        imagenRepo.delete(img);
    }

    public List<Producto> getAllProductos() {
        return productoRepo.findAll();
    }

    public List<Producto> getAllActiveProductos() {
        return productoRepo.findByActivoTrue();
    }

    public Producto findById(Integer id) {
        return productoRepo.findById(id).orElse(null);
    }

}