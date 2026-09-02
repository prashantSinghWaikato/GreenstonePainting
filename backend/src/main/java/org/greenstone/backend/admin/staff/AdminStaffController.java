package org.greenstone.backend.admin.staff;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/staff")
public class AdminStaffController {

    private final AdminStaffService staffService;

    public AdminStaffController(AdminStaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping
    public AdminStaffDashboardResponse dashboard() {
        return staffService.dashboard();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminStaffResponse create(
            @Valid @RequestBody CreateAdminStaffRequest request,
            Authentication authentication
    ) {
        return staffService.create(request, authentication.getName());
    }

    @PatchMapping("/{staffId}/enabled")
    public AdminStaffResponse setEnabled(
            @PathVariable UUID staffId,
            @Valid @RequestBody SetAdminStaffEnabledRequest request,
            Authentication authentication
    ) {
        return staffService.setEnabled(staffId, request, authentication.getName());
    }
}
