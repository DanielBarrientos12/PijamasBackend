package com.neosoft.pijamasbakend.services;

import com.neosoft.pijamasbakend.entities.Producto;
import com.neosoft.pijamasbakend.entities.ProductoTalla;
import com.neosoft.pijamasbakend.entities.Talla;
import com.neosoft.pijamasbakend.models.ProductoTallaDto;
import com.neosoft.pijamasbakend.repositories.ProductoTallaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoTallaService {

    @Autowired
    private ProductoTallaRepository productoTallaRepo;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private TallaService tallaService;

    public ProductoTalla crearVariante(ProductoTallaDto dto) {
        Producto product = productoService.findById(dto.getProductoId());
        Talla talla = tallaService.findById(dto.getTallaId());
        if (product == null || talla == null) {
            throw new RuntimeException("Producto o talla no encontrados");
        }
        // Evitar duplicados
        boolean exists = productoTallaRepo.existsByProductoIdAndTallaId(dto.getProductoId(), dto.getTallaId());
        if (exists) {
            throw new RuntimeException("La variante ya existe");
        }
        ProductoTalla variante = new ProductoTalla();
        variante.setProducto(product);
        variante.setTalla(talla);
        variante.setPrecioCompra(dto.getPrecioCompra());
        variante.setPrecioVenta(dto.getPrecioVenta());
        variante.setStockActual(0);
        return productoTallaRepo.save(variante);
    }

    public ProductoTalla obtenerVariante(Integer id) {
        return productoTallaRepo.findById(id).orElseThrow(() -> new RuntimeException("Variante no encontrada"));
    }

    public List<ProductoTalla> listarVariantesPorProducto(Integer productoId) {
        return productoTallaRepo.findByProductoId(productoId);
    }

    public ProductoTalla actualizarVariante(Integer id, ProductoTallaDto dto) {
        ProductoTalla existente = obtenerVariante(id);
        existente.setPrecioCompra(dto.getPrecioCompra());
        existente.setPrecioVenta(dto.getPrecioVenta());
        return productoTallaRepo.save(existente);
    }

    public void eliminarVariante(Integer id) {
        ProductoTalla variante = obtenerVariante(id);
        productoTallaRepo.delete(variante);
    }

}
