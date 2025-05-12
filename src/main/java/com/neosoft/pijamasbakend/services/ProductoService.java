package com.neosoft.pijamasbakend.services;

import com.neosoft.pijamasbakend.entities.Producto;
import com.neosoft.pijamasbakend.entities.Subcategoria;
import com.neosoft.pijamasbakend.models.AgregarInventarioDto;
import com.neosoft.pijamasbakend.models.ProductoDto;
import com.neosoft.pijamasbakend.models.ProductoResponseDto;
import com.neosoft.pijamasbakend.repositories.ProductoRepository;
import com.neosoft.pijamasbakend.utils.ImagenData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
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
    private AgregarInventarioService agregarInventarioService;

    @Autowired
    private FileService fileService;

    @Transactional
    public Producto createProducto(ProductoDto dto) throws IOException {
        // 1. Crear Producto
        Subcategoria subcat = subcategoriaService.findById(dto.getSubcategoriaId());
        if (subcat == null) throw new RuntimeException("Subcategoría no encontrada.");
        
        Producto producto = new Producto();
        producto.setNombre(dto.getNombre());
        producto.setSubcategoria(subcat);
        producto.setDescripcion(dto.getDescripcion());
        producto.setGenero(dto.getGenero());
        producto.setActivo(dto.getActivo() != null ? dto.getActivo() : Boolean.TRUE);
        producto = productoRepo.save(producto);

        // 2. Guardar imágenes
        productoImagenService.guardarImagenesParaProducto(producto, dto.getImagenes());

        // 3. Obtener el email del Administrativo actual
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = (auth.getPrincipal() instanceof UserDetails ud)
                ? ud.getUsername()
                : auth.getPrincipal().toString();

        // 4. Llamar a inventario (que internamente creará variante + stock)
        if (dto.getTallaId() != null && dto.getPrecioCompra() != null && dto.getPrecioVenta() != null) {
            AgregarInventarioDto invDto = getAgregarInventarioDto(dto, producto, email);
            agregarInventarioService.createInventario(invDto);
        }

        return producto;
    }

    private static AgregarInventarioDto getAgregarInventarioDto(ProductoDto dto, Producto producto, String email) {
        AgregarInventarioDto invDto = new AgregarInventarioDto();
        invDto.setProductoId(producto.getId());
        invDto.setTallaId(dto.getTallaId());
        invDto.setPrecioCompra(dto.getPrecioCompra());
        invDto.setPrecioVenta(dto.getPrecioVenta());
        invDto.setCantidadAgregada(dto.getAgregarStock() != null ? dto.getAgregarStock() : 0);
        invDto.setObservaciones(
                dto.getObservaciones() != null
                        ? dto.getObservaciones()
                        : "Stock inicial al crear producto"
        );
        invDto.setEmail(email);
        return invDto;
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

    public List<Producto> getAllActiveProductos() {
        return productoRepo.findByActivoTrue();
    }

    public Producto findById(Integer id) {
        return productoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto con id: " + id + " no encontrado."));
    }
}
