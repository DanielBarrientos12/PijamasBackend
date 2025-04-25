package com.neosoft.pijamasbakend.services;

import com.neosoft.pijamasbakend.entities.ProductoImagen;
import com.neosoft.pijamasbakend.entities.Subcategoria;
import com.neosoft.pijamasbakend.entities.Talla;
import com.neosoft.pijamasbakend.models.ProductoDto;
import com.neosoft.pijamasbakend.entities.Producto;
import com.neosoft.pijamasbakend.repositories.ProductoImagenRepository;
import com.neosoft.pijamasbakend.repositories.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository        productoRepo;
    @Autowired
    private SubcategoriaService       subcatService;
    @Autowired
    private TallaService              tallaService;
    @Autowired
    private ProductoImagenRepository imagenRepo;
    @Autowired
    private FileService               fileService;

    public Producto createProducto(ProductoDto dto) throws IOException {

        Talla talla = tallaService.findById(dto.getTallaId());
        Subcategoria subcat = subcatService.findById(dto.getSubcategoriaId());

        Producto product = new Producto();
        product.setNombre(dto.getNombre());
        product.setSubcategoria(subcat);
        product.setDescripcion(dto.getDescripcion());
        product.setGenero(dto.getGenero());
        product.setTalla(talla);
        product.setPrecioCompra(dto.getPrecioCompra());
        product.setPrecioVenta(dto.getPrecioVenta());
        product.setStockActual(0);
        product.setActivo(true);
        product.setFechaCreacion(LocalDate.now());

        productoRepo.save(product);

        if (dto.getImagenes() != null && !dto.getImagenes().isEmpty()) {

            List<String> rutas = fileService.storeFiles(dto.getImagenes(),"productos/" + product.getId());

            int posBase = 1;// portada = 1
            for (String ruta : rutas) {
                ProductoImagen img = new ProductoImagen();
                img.setProducto(product);
                img.setUrl(ruta);
                img.setPosicion(posBase++);
                imagenRepo.save(img);
            }
        }
        return product;
    }

    public Producto updateProducto(Integer id, ProductoDto dto) throws IOException {

        Producto product = productoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto con id: " + id + " no encontrado."));
        Talla talla = tallaService.findById(dto.getTallaId());
        Subcategoria subcat = subcatService.findById(dto.getSubcategoriaId());

        product.setNombre(dto.getNombre());
        product.setSubcategoria(subcat);
        product.setDescripcion(dto.getDescripcion());
        product.setGenero(dto.getGenero());
        product.setTalla(talla);
        product.setPrecioCompra(dto.getPrecioCompra());
        product.setPrecioVenta(dto.getPrecioVenta());
        product.setActivo(dto.getActivo());

        // si llegan nuevas imágenes, anexa al final:
        if (dto.getImagenes() != null && !dto.getImagenes().isEmpty()) {
            int posBase = imagenRepo.countByProductoId(id) + 1;
            List<String> rutas = fileService.storeFiles(dto.getImagenes(), "productos/" + id);
            for (String ruta : rutas) {
                ProductoImagen img = new ProductoImagen();
                img.setProducto(product);
                img.setUrl(ruta);
                img.setPosicion(posBase++);
                imagenRepo.save(img);
            }
        }
        return product;
    }

    public void deleteProductoImagen(Integer imagenId) throws IOException {
        ProductoImagen img = imagenRepo.findById(imagenId)
                .orElseThrow(() -> new RuntimeException("Imagen con id: " + imagenId + " no encontrada."));
        fileService.deleteFile(img.getUrl());         // borra archivo físico
        imagenRepo.delete(img);                       // borra registro
    }

    public List<Producto> getAllProductos(){
        return productoRepo.findAll();
    }

    public List<Producto> getAllActiveProductos() {
        return productoRepo.findByActivoTrue();
    }


}