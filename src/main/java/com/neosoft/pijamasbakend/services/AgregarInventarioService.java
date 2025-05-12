package com.neosoft.pijamasbakend.services;

import com.neosoft.pijamasbakend.entities.AgregarInventario;
import com.neosoft.pijamasbakend.entities.ProductoTalla;
import com.neosoft.pijamasbakend.models.AgregarInventarioDto;
import com.neosoft.pijamasbakend.models.ProductoTallaDto;
import com.neosoft.pijamasbakend.repositories.AgregarInventarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AgregarInventarioService {

    @Autowired
    private ProductoTallaService productoTallaService;

    @Autowired
    private AgregarInventarioRepository inventarioRepo;

    @Autowired
    private AdministrativoService administrativoService;

    @Transactional
    public void createInventario(AgregarInventarioDto dto) {
        // 1. Crear variante si no existe
        ProductoTalla variante;
        if (productoTallaService.existsByProductoIdAndTallaId(dto.getProductoId(), dto.getTallaId())) {
            variante = productoTallaService.getByProductoIdAndTallaId(dto.getProductoId(), dto.getTallaId());
        } else {
            ProductoTallaDto ptDto = new ProductoTallaDto();
            ptDto.setProductoId(dto.getProductoId());
            ptDto.setTallaId(dto.getTallaId());
            ptDto.setPrecioCompra(dto.getPrecioCompra());
            ptDto.setPrecioVenta(dto.getPrecioVenta());
            ptDto.setAgregarStock(0);
            variante = productoTallaService.crearVariante(ptDto);
        }

        // 2. Ajustar stock actual
        int nuevoStock = variante.getStockActual() + dto.getCantidadAgregada();
        variante.setStockActual(nuevoStock);
        productoTallaService.guardarVariante(variante);

        // 3. Registrar en inventario
        AgregarInventario registro = new AgregarInventario();
        registro.setProductoTalla(variante);
        registro.setCantidadAgregada(dto.getCantidadAgregada());
        registro.setObservaciones(dto.getObservaciones());
        registro.setAdministrativo(administrativoService.findByEmail(dto.getEmail()));
        registro.setFecha(LocalDateTime.now());

        inventarioRepo.save(registro);
    }

}
