package com.neosoft.pijamasbakend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileService {

    @Value("${pijamas.upload-dir:/home/images}")
    private String uploadDir;

    private Path root() { return Paths.get(uploadDir).toAbsolutePath().normalize(); }
    private void ensureDir(Path dir) throws IOException { Files.createDirectories(dir); }

    /** Devuelve extensión (".jpg") o cadena vacía si no hay punto */
    private String extension(String filename) {
        int i = filename.lastIndexOf('.');
        return (i >= 0) ? filename.substring(i) : "";
    }

    public String storeFile(MultipartFile file, String subfolder) throws IOException {
        String ext       = extension(Objects.requireNonNull(file.getOriginalFilename()));
        String safeName  = UUID.randomUUID() + ext;           // ← nombre seguro
        Path   targetDir = root().resolve(subfolder).normalize();
        ensureDir(targetDir);

        Path dest = targetDir.resolve(safeName);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
        }

        return subfolder + "/" + safeName;
    }

    /* ------------------------------------------------------------------ */
    /* Guardar VARIAS imágenes                                            */
    /* ------------------------------------------------------------------ */
    public List<String> storeFiles(List<MultipartFile> files, String subfolder) throws IOException {
        List<String> paths = new ArrayList<>();
        for (MultipartFile f : files) {
            paths.add(storeFile(f, subfolder));
        }
        return paths;
    }

    public Resource load(String relativePath) throws IOException {
        Path file = root().resolve(relativePath).normalize();
        Resource res = new UrlResource(file.toUri());
        if (!res.exists() || !res.isReadable()) {
            throw new FileNotFoundException("Archivo no encontrado: " + relativePath);
        }
        return res;
    }

    public List<Resource> loadFiles(List<String> relativePaths) throws IOException {
        List<Resource> resources = new ArrayList<>();
        for (String path : relativePaths) {
            resources.add(load(path));
        }
        return resources;
    }

}
