package org.greenstone.backend.admin.staff;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/account")
public class AdminAccountController {

    private final AdminStaffService staffService;

    public AdminAccountController(AdminStaffService staffService) {
        this.staffService = staffService;
    }

    @PatchMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @Valid @RequestBody ChangeAdminPasswordRequest request,
            Authentication authentication
    ) {
        staffService.changePassword(request, authentication.getName());
    }
}
