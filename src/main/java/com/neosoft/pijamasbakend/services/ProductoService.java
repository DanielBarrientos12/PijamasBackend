package com.neosoft.pijamasbakend.services;

import com.neosoft.pijamasbakend.entities.Producto;
import com.neosoft.pijamasbakend.entities.ProductoTalla;
import com.neosoft.pijamasbakend.entities.Subcategoria;
import com.neosoft.pijamasbakend.models.AgregarInventarioDto;
import com.neosoft.pijamasbakend.models.ProductoDto;
import com.neosoft.pijamasbakend.models.ProductoResponseDto;
import com.neosoft.pijamasbakend.repositories.ProductoRepository;
import com.neosoft.pijamasbakend.utils.ImagenData;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private AgregarInventarioService agregarInventarioService;

    @Autowired
    private ProductoTallaService productoTallaService;

    @Autowired
    private FileService fileService;

    @Autowired
    private PromocionService promocionService;

    @Transactional
    public Producto createProducto(ProductoDto dto) throws IOException {
        // 1. Crear Producto
        Subcategoria subcat = subcategoriaService.findById(dto.getSubcategoriaId());
        if (subcat == null) throw new RuntimeException("Subcategoría no encontrada.");

        Producto producto = new Producto();
        producto.setNombre(dto.getNombre());
        producto.setSubcategoria(subcat);
        producto.setDescripcion(dto.getDescripcion());
        producto.setActivo(dto.getActivo() != null ? dto.getActivo() : Boolean.TRUE);
        producto.setFechaCreacion(LocalDate.now());
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

    // Metodo grande que se usa al crear el inventario del producto
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

    @Transactional
    public Producto updateProducto(Integer id, ProductoDto dto) throws IOException {
        // 1. Recuperar producto o lanzar excepción si no existe
        Producto producto = productoRepo.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Producto no encontrado con id: " + id)
                );

        // 2. Actualizar campos del producto (solo los no nulos)
        if (dto.getNombre() != null) producto.setNombre(dto.getNombre());
        if (dto.getDescripcion() != null) producto.setDescripcion(dto.getDescripcion());
        if (dto.getActivo() != null) producto.setActivo(dto.getActivo());

        producto = productoRepo.save(producto);

        // 3. Reemplazar imágenes si llegan nuevas
        if ((dto.getImagenes() != null && !dto.getImagenes().isEmpty())
                || (dto.getImagenesEliminadas() != null && !dto.getImagenesEliminadas().isEmpty())) {
            productoImagenService.reemplazarImagenes(
                    producto,
                    dto.getImagenes(),             // nuevas
                    dto.getImagenesEliminadas()    // a borrar en base al id
            );
        }

        // 4. Manejar actualización de precios en la variante (talla)
        if (dto.getTallaId() != null && (dto.getPrecioCompra() != null || dto.getPrecioVenta() != null)) {

            boolean existeVar = productoTallaService
                    .existsByProductoIdAndTallaId(producto.getId(), dto.getTallaId());

            if (existeVar) {
                // Variante existente entonces actualizamos los precios de ser necesario
                ProductoTalla variante = productoTallaService.getByProductoIdAndTallaId(producto.getId(), dto.getTallaId());
                if (dto.getPrecioCompra() != null) variante.setPrecioCompra(dto.getPrecioCompra());
                if (dto.getPrecioVenta() != null) variante.setPrecioVenta(dto.getPrecioVenta());
                productoTallaService.guardarVariante(variante);
            }
        }

        return producto;
    }

    public ProductoResponseDto findById(Integer id) {
        Producto prod = productoRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        return mapToDto(prod);
    }

    public List<ProductoResponseDto> findByNombre(String nombre) {
        return productoRepo.findByNombreContainingIgnoreCase(nombre).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Para administradores:
    public List<ProductoResponseDto> getAllProductos() {
        return productoRepo.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Para clientes:
    public List<ProductoResponseDto> getAllActiveProductos() {
        return productoRepo.findByActivoTrue().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    // Para filtrar por categoria
    public List<ProductoResponseDto> getProductosPorCategoria(Integer categoriaId) {
        List<Producto> productos = productoRepo
                .findBySubcategoriaCategoriaIdAndActivoTrue(categoriaId);

        return productos.stream()
                .map(this::mapToDto)
                .toList();
    }

    private ProductoResponseDto mapToDto(Producto prod) {
        ProductoResponseDto dto = new ProductoResponseDto();
        dto.setId(prod.getId());
        dto.setNombre(prod.getNombre());
        dto.setDescripcion(prod.getDescripcion());
        dto.setActivo(prod.getActivo());
        dto.setFechaCreacion(prod.getFechaCreacion());
        dto.setSubcategoria(prod.getSubcategoria());

        List<ImagenData> imgs = prod.getImagenes().stream()
                .map(img -> {
                    byte[] data;
                    try {
                        data = fileService.loadFile(img.getUrl());
                    } catch (IOException e) {
                        throw new UncheckedIOException("Error leyendo imagen " + img.getUrl(), e);
                    }
                    String nombreArchivo = Paths.get(img.getUrl()).getFileName().toString();
                    return new ImagenData(img.getPosicion(), nombreArchivo, data);
                })
                .collect(Collectors.toList());
        dto.setImagenes(imgs);

        // Tallas: traemos directamente la lista de entidades
        List<ProductoTalla> variantes = productoTallaService.listarVariantesPorProducto(prod.getId());

        // Se busca el mejor % de descuento vigente para el producto
        BigDecimal pctDesc = promocionService.mejorDescuento(prod.getId());

        // Se calcula los valores promoción en cada variante, 0 sin rebaja
        for (ProductoTalla v : variantes) {
            v.setPorcentajeDescuento(pctDesc);
            BigDecimal precioFinal = v.getPrecioVenta();

            if (pctDesc.compareTo(BigDecimal.ZERO) > 0) {
                precioFinal = precioFinal.subtract(precioFinal.multiply(pctDesc)
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            }
            v.setPrecioConDescuento(precioFinal);
        }

        dto.setVariante(variantes);
        return dto;
    }

}
