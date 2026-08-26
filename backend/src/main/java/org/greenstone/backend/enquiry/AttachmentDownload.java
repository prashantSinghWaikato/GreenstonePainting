package org.greenstone.backend.enquiry;

import org.springframework.core.io.Resource;

public record AttachmentDownload(
        Resource resource,
        String filename,
        String contentType,
        long sizeBytes
) {
}
