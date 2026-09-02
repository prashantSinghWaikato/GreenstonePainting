package org.greenstone.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.greenstone.backend.persistence.repository.AdminUserRepository;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AdminAccountStatusFilter extends OncePerRequestFilter {

    private final AdminUserRepository repository;

    public AdminAccountStatusFilter(AdminUserRepository repository) {
        this.repository = repository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (request.getRequestURI().startsWith("/api/admin/")
                && authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            var active = repository.findByEmailIgnoreCase(authentication.getName())
                    .map(admin -> admin.isEnabled())
                    .orElse(false);
            if (!active) {
                var session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("{\"message\":\"This staff account is no longer active.\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
