package org.greenstone.backend.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class LocalFileStorageService implements FileStorageService {

    private final Path rootDirectory;

    public LocalFileStorageService(@Value("${app.content.directory}") String contentDirectory) {
        rootDirectory = Path.of(contentDirectory).toAbsolutePath().normalize();
    }

    @Override
    public Resource load(String objectKey) {
        var path = resolveObjectKey(objectKey);
        if (!Files.isRegularFile(path)) {
            throw new IllegalStateException("The requested image is unavailable.");
        }
        return new FileSystemResource(path);
    }

    private Path resolveObjectKey(String objectKey) {
        var resolved = rootDirectory.resolve(objectKey).normalize();
        if (!resolved.startsWith(rootDirectory)) {
            throw new IllegalArgumentException("Invalid object key.");
        }
        return resolved;
    }
}
