package com.neosoft.pijamasbakend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class FileService {

    @Value("${pijamas.upload-dir}")
    private String uploadDir;

    private Path rootLocation;

    @PostConstruct
    public void init() throws IOException {
        rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(rootLocation); // asegurarse de que exista
    }

    public String storeFile(MultipartFile file, String subfolder) throws IOException {
        String originalFilename = Path.of(Objects.requireNonNull(file.getOriginalFilename())).getFileName().toString();
        Path targetFolder = rootLocation.resolve(subfolder);
        Files.createDirectories(targetFolder);

        Path destinationFile = targetFolder.resolve(originalFilename).normalize().toAbsolutePath();

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        }

        return Paths.get(subfolder).resolve(originalFilename).toString().replace("\\", "/");
    }

    public List<String> storeFiles(List<MultipartFile> files, String subfolder) throws IOException {
        List<String> paths = new ArrayList<>();
        for (MultipartFile file : files) {
            paths.add(storeFile(file, subfolder));
        }
        return paths;
    }

    public byte[] loadFile(String filename) throws IOException {
        Path file = rootLocation.resolve(filename);
        return Files.readAllBytes(file);
    }

    public List<byte[]> loadFiles(List<String> filenames) throws IOException {
        List<byte[]> contents = new ArrayList<>();
        for (String filename : filenames) {
            contents.add(loadFile(filename));
        }
        return contents;
    }

    public void deleteFile(String filename) throws IOException {
        Path file = rootLocation.resolve(filename);
        Files.deleteIfExists(file);

        Path parent = file.getParent();
        if (parent != null && Files.exists(parent) && Files.isDirectory(parent)) {
            boolean isEmpty = Files.list(parent).findAny().isEmpty();
            if (isEmpty) {
                Files.delete(parent);
            }
        }
    }

    public void deleteFiles(List<String> filenames) throws IOException {
        for (String filename : filenames) {
            deleteFile(filename);
        }
    }
}