package com.neosoft.pijamasbakend.services;

import com.neosoft.pijamasbakend.entities.Producto;
import com.neosoft.pijamasbakend.entities.ProductoImagen;
import com.neosoft.pijamasbakend.repositories.ProductoImagenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ProductoImagenService {

    @Autowired
    ProductoImagenRepository productoImagenRepository;

    @Autowired
    FileService fileService;

    public void guardarImagenesParaProducto(Producto producto, List<MultipartFile> imagenes) throws IOException {
        if (imagenes == null || imagenes.isEmpty()) return;

        String subfolder = "productos/" + producto.getId();
        List<String> rutas = fileService.storeFiles(imagenes, subfolder);

        int posicion = productoImagenRepository.countByProductoId(producto.getId()) + 1;
        for (String ruta : rutas) {
            ProductoImagen img = new ProductoImagen();
            img.setProducto(producto);
            img.setUrl(ruta);
            img.setPosicion(posicion++);
            productoImagenRepository.save(img);
        }
    }

    public void eliminarImagen(ProductoImagen imagen) throws IOException {
        fileService.deleteFile(imagen.getUrl());
        productoImagenRepository.delete(imagen);
    }
}
