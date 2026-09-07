package org.greenstone.backend.storage;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import java.util.UUID;

public interface FileStorageService {
    StoredObject storeEnquiryPhoto(UUID enquiryId, MultipartFile file);
    StoredObject storeProjectImage(UUID projectId, MultipartFile file);
    StoredObject storeBlogImage(UUID articleId, MultipartFile file);
    StoredObject storeServiceImage(UUID serviceId, MultipartFile file);
    Resource load(String objectKey);
    void delete(String objectKey);
}
