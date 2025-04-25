package com.neosoft.pijamasbakend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;

@Service
public class FileService {

    @Value("${pijamas.upload-dir:/home/images}")
    private String uploadDir;

    private Path root() { return Paths.get(uploadDir).toAbsolutePath().normalize(); }
    private void ensureDir(Path dir) throws IOException { Files.createDirectories(dir); }

    private String extension(String filename) {
        int i = filename.lastIndexOf('.');
        return (i >= 0) ? filename.substring(i).toLowerCase(Locale.ROOT) : "";
    }

    private void validateInsideRoot(Path path) {
        if (!path.normalize().startsWith(root()))
            throw new SecurityException("Intento de path-traversal detectado: " + path);
    }

    public String storeFile(MultipartFile file, String subfolder) throws IOException {
        String ext       = extension(Objects.requireNonNull(file.getOriginalFilename()));
        String safeName  = UUID.randomUUID() + ext;
        Path   targetDir = root().resolve(subfolder).normalize();
        ensureDir(targetDir);

        Path dest = targetDir.resolve(safeName);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
        }
        return subfolder + "/" + safeName;
    }

    public List<String> storeFiles(List<MultipartFile> files, String subfolder) throws IOException {
        List<String> paths = new ArrayList<>(files.size());
        for (MultipartFile f : files) {
            paths.add(storeFile(f, subfolder));
        }
        return paths;
    }

    public Resource load(String relativePath) throws IOException {
        Path file = root().resolve(relativePath).normalize();
        validateInsideRoot(file);
        Resource res = new UrlResource(file.toUri());
        if (!res.exists() || !res.isReadable()) {
            throw new FileNotFoundException("Archivo no encontrado: " + relativePath);
        }
        return res;
    }

    public List<Resource> loadFiles(List<String> relativePaths) throws IOException {
        List<Resource> resources = new ArrayList<>(relativePaths.size());
        for (String path : relativePaths) {
            resources.add(load(path));
        }
        return resources;
    }

    public void deleteFile(String relativePath) throws IOException {
        Path file = root().resolve(relativePath).normalize();
        validateInsideRoot(file);

        Files.deleteIfExists(file);
        cleanupEmptyDirs(file.getParent());
    }

    public void deleteFiles(List<String> relativePaths) throws IOException {
        for (String path : relativePaths) {
            deleteFile(path);
        }
    }

    private void cleanupEmptyDirs(Path dir) throws IOException {
        if (dir == null || dir.equals(root())) return;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            if (!stream.iterator().hasNext()) {          // si está vacío se borra
                Files.delete(dir);
                cleanupEmptyDirs(dir.getParent());
            }
        }
    }
}