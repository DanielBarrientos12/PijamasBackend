package com.neosoft.pijamasbakend.services;

import com.neosoft.pijamasbakend.entities.Administrativo;
import com.neosoft.pijamasbakend.entities.AgregarInventario;
import com.neosoft.pijamasbakend.entities.ProductoTalla;
import com.neosoft.pijamasbakend.models.AgregarInventarioDto;
import com.neosoft.pijamasbakend.repositories.AgregarInventarioRepository;
import com.neosoft.pijamasbakend.repositories.ProductoTallaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDateTime;

@Service
public class AgregarInventarioService {

    @Autowired
    private ProductoTallaRepository productoTallaRepo;

    @Autowired
    private AgregarInventarioRepository inventarioRepo;

    /**
    public AgregarInventario createInventario(AgregarInventarioDto dto) {
        // 1. Recuperar variante (producto + talla)
        ProductoTalla variante = productoTallaRepo.findByProductoIdAndTallaId(
                dto.getProductoId(), dto.getTallaId()
        ).orElseThrow(() -> new RuntimeException("Variante no encontrada"));

        // 2. Ajustar stock_actual
        int nuevoStock = variante.getStockActual() + dto.getCantidadAgregada();
        variante.setStockActual(nuevoStock);
        productoTallaRepo.save(variante);

        // 3. Registrar histórico en agregar_inventario
        AgregarInventario registro = new AgregarInventario();
        registro.setProducto(variante.getProducto());
        registro.setCantidadAgregada(dto.getCantidadAgregada());
        registro.setFecha(LocalDateTime.now());
        registro.setObservaciones(dto.getObservaciones());
        //registro.setAdministrativoId(dto.getAdministrativoId()); corregir para poder guardar un administrativo
        inventarioRepo.save(registro);

        return registro;
    }

    public AgregarInventario findById(Integer id) {
        return inventarioRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Registro de inventario no encontrado"));
    }

    public List<AgregarInventario> getAllInventarios() {
        return inventarioRepo.findAll();
    }

    public AgregarInventario updateInventario(Integer id, AgregarInventarioDto dto) {
        AgregarInventario registro = findById(id);
        // Solo actualizamos observaciones y cantidad
        int delta = dto.getCantidadAgregada() - registro.getCantidadAgregada();
        registro.setCantidadAgregada(dto.getCantidadAgregada());
        registro.setObservaciones(dto.getObservaciones());
        inventarioRepo.save(registro);

        // Ajuste de stock en variante
        ProductoTalla variante = productoTallaRepo.findById(registro.getProductoTalla().getId())
                .orElseThrow(() -> new RuntimeException("Variante no encontrada al actualizar"));
        variante.setStockActual(variante.getStockActual() + delta);
        productoTallaRepo.save(variante);

        return registro;
    }

    public void deleteInventario(Integer id) {
        AgregarInventario registro = findById(id);
        // Revertir stock en variante
        ProductoTalla variante = productoTallaRepo.findById(registro.getProductoTalla().getId())
                .orElseThrow(() -> new RuntimeException("Variante no encontrada al eliminar"));
        variante.setStockActual(variante.getStockActual() - registro.getCantidadAgregada());
        productoTallaRepo.save(variante);

        inventarioRepo.delete(registro);
    }

     **/
}
