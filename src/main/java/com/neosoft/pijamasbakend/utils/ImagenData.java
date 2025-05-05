package com.neosoft.pijamasbakend.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ImagenData {
    private int posicion;
    private String fileName;
    private byte[] data;
}