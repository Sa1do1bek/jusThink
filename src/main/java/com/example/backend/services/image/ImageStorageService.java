package com.example.backend.services.image;

import com.example.backend.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class ImageStorageService {

    @Value("${app.storage.root}")
    private String root;

    @Value("${app.storage.images}")
    private String imageDir;

    public String save(UUID id, MultipartFile file) {
        validate(file);
        try {
            Path dir = Paths.get(root, imageDir);
            Files.createDirectories(dir);

            String filename = id + ".webp";
            Path target = dir.resolve(filename);

            Files.copy(
                    file.getInputStream(),
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return filename;

        } catch (IOException e) {
            throw new RuntimeException("Image upload failed", e);
        }
    }

    public void delete(String path) {
        if (path == null) return;

        try {
            Files.deleteIfExists(Paths.get(root).resolve(path));
        } catch (IOException e) {
            throw new RuntimeException("Image delete failed", e);
        }
    }

    private void validate(MultipartFile file) {
        if (file.isEmpty())
            throw new IllegalArgumentException("Empty file");

        if (!Objects.requireNonNull(file.getContentType()).startsWith("image/"))
            throw new IllegalArgumentException("Invalid image type");
    }

    public Optional<FileSystemResource> loadImage(String filename) {
        if (filename.contains("..")) return Optional.empty();

        Path filePath = Paths.get(root, imageDir).resolve(filename);
        FileSystemResource resource = new FileSystemResource(filePath);

        if (!resource.exists() || !resource.isReadable()) {
            throw new ResourceNotFoundException("Image with " + filename + " file name doesn't exist!");
        }

        return Optional.of(resource);
    }
}