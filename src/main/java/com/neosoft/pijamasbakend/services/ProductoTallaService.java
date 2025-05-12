package com.neosoft.pijamasbakend.services;

import com.neosoft.pijamasbakend.entities.Producto;
import com.neosoft.pijamasbakend.entities.ProductoTalla;
import com.neosoft.pijamasbakend.entities.Talla;
import com.neosoft.pijamasbakend.models.ProductoTallaDto;
import com.neosoft.pijamasbakend.repositories.ProductoRepository;
import com.neosoft.pijamasbakend.repositories.ProductoTallaRepository;
import com.neosoft.pijamasbakend.repositories.TallaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductoTallaService {

    @Autowired
    private ProductoTallaRepository productoTallaRepo;

    @Autowired
    private ProductoRepository productoRepo;

    @Autowired
    private TallaRepository tallaRepo;

    public ProductoTalla crearVariante(ProductoTallaDto dto) {
        Producto producto = productoRepo.findById(dto.getProductoId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Producto no encontrado con id: " + dto.getProductoId()));
        Talla talla = tallaRepo.findById(dto.getTallaId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Talla no encontrada con id: " + dto.getTallaId()));

        ProductoTalla variante = new ProductoTalla();
        variante.setProducto(producto);
        variante.setTalla(talla);
        variante.setPrecioCompra(dto.getPrecioCompra());
        variante.setPrecioVenta(dto.getPrecioVenta());
        variante.setStockActual(dto.getAgregarStock() != null ? dto.getAgregarStock() : 0);

        return productoTallaRepo.save(variante);
    }

    public ProductoTalla getByProductoIdAndTallaId(Integer productoId, Integer tallaId) {
        return productoTallaRepo.findByProductoIdAndTallaId(productoId, tallaId)
                .orElseThrow(() -> new RuntimeException(
                        "Variante no encontrada para productoId: " + productoId + " y tallaId: " + tallaId));
    }

    public void guardarVariante(ProductoTalla variante) {
        productoTallaRepo.save(variante);
    }

    public ProductoTalla actualizarVariante(Integer id, ProductoTallaDto dto) {
        ProductoTalla existente = productoTallaRepo.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Variante no encontrada con id: " + id));
        existente.setPrecioCompra(dto.getPrecioCompra());
        existente.setPrecioVenta(dto.getPrecioVenta());
        return productoTallaRepo.save(existente);
    }


}
