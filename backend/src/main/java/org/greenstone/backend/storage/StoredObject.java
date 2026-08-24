package org.greenstone.backend.storage;

public record StoredObject(String objectKey, String contentType, long sizeBytes) {
}
