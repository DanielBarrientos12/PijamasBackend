package com.neosoft.pijamasbakend.services;

import com.neosoft.pijamasbakend.models.ProductoDto;
import com.neosoft.pijamasbakend.entities.Producto;
import com.neosoft.pijamasbakend.repositories.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    public Producto createProducto(ProductoDto productoDto){

        return null;
    }


}
