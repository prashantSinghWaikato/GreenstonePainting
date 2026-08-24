package org.greenstone.backend.storage;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface FileStorageService {
    StoredObject storeEnquiryPhoto(UUID enquiryId, MultipartFile file);
    void delete(String objectKey);
}
