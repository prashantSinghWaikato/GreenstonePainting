package org.greenstone.backend.security;

public record AdminSessionResponse(String email, String displayName, String role) {
}
