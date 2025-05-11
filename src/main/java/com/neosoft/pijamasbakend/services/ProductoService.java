package com.neosoft.pijamasbakend.services;

import com.neosoft.pijamasbakend.entities.Producto;
import com.neosoft.pijamasbakend.entities.Subcategoria;
import com.neosoft.pijamasbakend.models.ProductoDto;
import com.neosoft.pijamasbakend.models.ProductoResponseDto;
import com.neosoft.pijamasbakend.repositories.ProductoRepository;
import com.neosoft.pijamasbakend.utils.ImagenData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepo;

    @Autowired
    private SubcategoriaService subcategoriaService;

    @Autowired
    private ProductoImagenService productoImagenService;

    @Autowired
    private FileService fileService;

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

    public List<ProductoResponseDto> getProductosConImagenesData() {
        return productoRepo.findAllConImagenes().stream().map(prod -> {
            ProductoResponseDto dto = new ProductoResponseDto();

            dto.setId(prod.getId());
            dto.setNombre(prod.getNombre());
            dto.setDescripcion(prod.getDescripcion());
            dto.setGenero(prod.getGenero());
            dto.setActivo(prod.getActivo());
            dto.setFechaCreacion(prod.getFechaCreacion());
            dto.setSubcategoriaId(prod.getSubcategoria().getId());

            List<ImagenData> imgs = prod.getImagenes()
                    .stream()
                    .map(img -> {
                        byte[] data;
                        try {
                            data = fileService.loadFile(img.getUrl());
                        } catch (IOException e) {
                            throw new UncheckedIOException("Error leyendo imagen " + img.getUrl(), e);
                        }
                        String nombreArchivo = Paths
                                .get(img.getUrl())
                                .getFileName()
                                .toString();
                        return new ImagenData(img.getPosicion(), nombreArchivo, data);
                    })
                    .collect(Collectors.toList());

            dto.setImagenes(imgs);

            return dto;
        }).collect(Collectors.toList());
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
