package com.neosoft.pijamasbakend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FileService {

    @Value("${files.path:/home/obras}")   // “/home/obras” es el valor por defecto
    private String baseDir;




}
