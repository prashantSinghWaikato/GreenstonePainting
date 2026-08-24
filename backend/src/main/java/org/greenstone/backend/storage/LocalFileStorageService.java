package org.greenstone.backend.storage;

import org.greenstone.backend.web.UploadValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp",
            "image/heic", ".heic",
            "image/heif", ".heif"
    );

    private final Path rootDirectory;

    public LocalFileStorageService(@Value("${app.upload.directory}") String uploadDirectory) {
        rootDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
    }

    @Override
    public StoredObject storeEnquiryPhoto(UUID enquiryId, MultipartFile file) {
        validate(file);
        var contentType = file.getContentType().toLowerCase();
        var objectKey = "enquiries/" + enquiryId + "/" + UUID.randomUUID() + EXTENSIONS.get(contentType);
        var destination = resolveObjectKey(objectKey);

        try {
            Files.createDirectories(destination.getParent());
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            return new StoredObject(objectKey, contentType, file.getSize());
        } catch (IOException exception) {
            throw new IllegalStateException("The photo could not be stored.", exception);
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            Files.deleteIfExists(resolveObjectKey(objectKey));
        } catch (IOException ignored) {
            // Best-effort cleanup if database persistence fails after a file is written.
        }
    }

    private void validate(MultipartFile file) {
        if (file.isEmpty()) {
            throw new UploadValidationException("Please choose a photo to upload.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new UploadValidationException("Each photo must be 5 MB or smaller.");
        }
        var contentType = file.getContentType();
        if (contentType == null || !EXTENSIONS.containsKey(contentType.toLowerCase())) {
            throw new UploadValidationException("Use a JPEG, PNG, WebP, HEIC, or HEIF photo.");
        }
        if (!hasExpectedSignature(file, contentType.toLowerCase())) {
            throw new UploadValidationException("The selected file does not appear to be a valid photo.");
        }
    }

    private boolean hasExpectedSignature(MultipartFile file, String contentType) {
        try (var input = file.getInputStream()) {
            var header = input.readNBytes(16);
            return switch (contentType) {
                case "image/jpeg" -> startsWith(header, new byte[]{(byte) 0xff, (byte) 0xd8, (byte) 0xff});
                case "image/png" -> startsWith(header, new byte[]{(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a});
                case "image/webp" -> asciiAt(header, 0, "RIFF") && asciiAt(header, 8, "WEBP");
                case "image/heic", "image/heif" -> asciiAt(header, 4, "ftyp") && isHeifBrand(header);
                default -> false;
            };
        } catch (IOException exception) {
            throw new UploadValidationException("The selected photo could not be read.");
        }
    }

    private boolean startsWith(byte[] value, byte[] prefix) {
        return value.length >= prefix.length && Arrays.equals(Arrays.copyOf(value, prefix.length), prefix);
    }

    private boolean asciiAt(byte[] value, int offset, String expected) {
        var bytes = expected.getBytes(StandardCharsets.US_ASCII);
        return value.length >= offset + bytes.length
                && Arrays.equals(Arrays.copyOfRange(value, offset, offset + bytes.length), bytes);
    }

    private boolean isHeifBrand(byte[] header) {
        if (header.length < 12) return false;
        var brand = new String(header, 8, 4, StandardCharsets.US_ASCII);
        return brand.equals("mif1") || brand.equals("msf1") || brand.startsWith("hei") || brand.startsWith("hev");
    }

    private Path resolveObjectKey(String objectKey) {
        var resolved = rootDirectory.resolve(objectKey).normalize();
        if (!resolved.startsWith(rootDirectory)) {
            throw new IllegalArgumentException("Invalid object key.");
        }
        return resolved;
    }
}
