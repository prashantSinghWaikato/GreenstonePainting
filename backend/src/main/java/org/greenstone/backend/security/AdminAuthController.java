package org.greenstone.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.greenstone.backend.persistence.entity.AdminUser;
import org.greenstone.backend.persistence.repository.AdminUserRepository;
import org.greenstone.backend.web.ResourceNotFoundException;
import org.greenstone.backend.web.AdminAccountLockedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;
    private final AdminUserRepository adminUserRepository;
    private final AdminLoginSecurityService loginSecurityService;

    public AdminAuthController(
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository,
            SessionAuthenticationStrategy sessionAuthenticationStrategy,
            AdminUserRepository adminUserRepository,
            AdminLoginSecurityService loginSecurityService
    ) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
        this.adminUserRepository = adminUserRepository;
        this.loginSecurityService = loginSecurityService;
    }

    @GetMapping("/csrf")
    public CsrfTokenResponse csrf(CsrfToken csrfToken) {
        return new CsrfTokenResponse(csrfToken.getHeaderName(), csrfToken.getToken());
    }

    @PostMapping("/login")
    public AdminSessionResponse login(
            @Valid @RequestBody AdminLoginRequest loginRequest,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        var token = UsernamePasswordAuthenticationToken.unauthenticated(
                loginRequest.email().trim(),
                loginRequest.password()
        );
        token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        org.springframework.security.core.Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(token);
        } catch (LockedException exception) {
            throw new AdminAccountLockedException();
        } catch (org.springframework.security.core.AuthenticationException exception) {
            if (loginSecurityService.recordFailedLogin(loginRequest.email().trim())) {
                throw new AdminAccountLockedException();
            }
            throw exception;
        }

        loginSecurityService.recordSuccessfulLogin(authentication.getName());

        sessionAuthenticationStrategy.onAuthentication(authentication, request, response);
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        return toResponse(findAdmin(authentication.getName()));
    }

    @GetMapping("/me")
    public AdminSessionResponse currentSession(Authentication authentication) {
        return toResponse(findAdmin(authentication.getName()));
    }

    private AdminUser findAdmin(String email) {
        return adminUserRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Admin account is no longer available."));
    }

    private AdminSessionResponse toResponse(AdminUser admin) {
        return new AdminSessionResponse(admin.getEmail(), admin.getDisplayName(), admin.getRole());
    }
}
