package org.greenstone.backend.storage;

import org.springframework.core.io.Resource;

public interface FileStorageService {
    Resource load(String objectKey);
}
