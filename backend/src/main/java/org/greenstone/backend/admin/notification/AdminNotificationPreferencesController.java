package org.greenstone.backend.admin.notification;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/account/notifications")
public class AdminNotificationPreferencesController {

    private final AdminNotificationPreferencesService service;

    public AdminNotificationPreferencesController(AdminNotificationPreferencesService service) {
        this.service = service;
    }

    @GetMapping
    public AdminNotificationPreferencesResponse find(Authentication authentication) {
        return service.find(authentication.getName());
    }

    @PatchMapping
    public AdminNotificationPreferencesResponse update(
            @Valid @RequestBody UpdateAdminNotificationPreferencesRequest request,
            Authentication authentication
    ) {
        return service.update(authentication.getName(), request);
    }
}
