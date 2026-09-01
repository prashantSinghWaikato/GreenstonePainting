package org.greenstone.backend.security;

public record CsrfTokenResponse(String headerName, String token) {
}
