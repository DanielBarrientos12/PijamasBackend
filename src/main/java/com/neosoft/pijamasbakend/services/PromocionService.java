package com.neosoft.pijamasbakend.services;

import com.neosoft.pijamasbakend.entities.Producto;
import com.neosoft.pijamasbakend.entities.Promocion;
import com.neosoft.pijamasbakend.entities.PromocionProducto;
import com.neosoft.pijamasbakend.models.PromocionDto;
import com.neosoft.pijamasbakend.models.PromocionProductoDto;
import com.neosoft.pijamasbakend.repositories.ProductoRepository;
import com.neosoft.pijamasbakend.repositories.PromocionProductoRepository;
import com.neosoft.pijamasbakend.repositories.PromocionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class PromocionService {

    @Autowired
    private PromocionRepository promoRepo;
    @Autowired
    private PromocionProductoRepository ppRepo;
    @Autowired
    private ProductoRepository productoRepo;

    @Transactional
    public Promocion crear(PromocionDto dto) {
        Promocion p = new Promocion();
        p.setCodigo(dto.getCodigo());
        p.setDescripcion(dto.getDescripcion());
        p.setFechaInicio(dto.getFechaInicio());
        p.setFechaFin(dto.getFechaFin());
        p.setActivo(dto.validarActivo());
        return promoRepo.save(p);
    }

    @Transactional
    public Promocion actualizar(Integer id, PromocionDto dto) {
        Promocion p = promoRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Promexoción no existe"));
        p.setCodigo(dto.getCodigo());
        p.setDescripcion(dto.getDescripcion());
        p.setFechaInicio(dto.getFechaInicio());
        p.setFechaFin(dto.getFechaFin());
        p.setActivo(dto.getActivo());
        return promoRepo.save(p);
    }

    @Transactional
    public void eliminar(Integer id) {
        promoRepo.deleteById(id);
    }

    @Transactional
    public void asignarProductos(Integer promoId, List<PromocionProductoDto> lista) {
        Promocion promo = promoRepo.findById(promoId)
                .orElseThrow(() -> new EntityNotFoundException("Promoción no existe"));

        for (PromocionProductoDto dto : lista) {
            Producto producto = productoRepo.findById(dto.getProductoId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto no existe"));

            PromocionProducto pp = new PromocionProducto();
            pp.setPromocion(promo);
            pp.setProducto(producto);
            pp.setDescuento(dto.getDescuento());
            ppRepo.save(pp);
        }
    }

    // Mayor % de descuento vigente para un producto
    public BigDecimal mejorDescuento(Integer productoId) {
        return ppRepo.findAplicables(productoId, LocalDate.now()).stream()
                .map(PromocionProducto::getDescuento)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    public List<Promocion> todas() {
        return promoRepo.findAll();
    }

}
