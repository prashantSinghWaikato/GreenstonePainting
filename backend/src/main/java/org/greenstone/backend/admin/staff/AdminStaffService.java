package org.greenstone.backend.admin.staff;

import org.greenstone.backend.persistence.entity.AdminAccountActivity;
import org.greenstone.backend.persistence.entity.AdminAccountActivityType;
import org.greenstone.backend.persistence.entity.AdminRole;
import org.greenstone.backend.persistence.entity.AdminUser;
import org.greenstone.backend.persistence.repository.AdminAccountActivityRepository;
import org.greenstone.backend.persistence.repository.AdminUserRepository;
import org.greenstone.backend.security.PasswordPolicy;
import org.greenstone.backend.web.AccountSecurityException;
import org.greenstone.backend.web.ResourceNotFoundException;
import org.greenstone.backend.web.StaffAccountConflictException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
public class AdminStaffService {

    private final AdminUserRepository userRepository;
    private final AdminAccountActivityRepository activityRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminStaffService(
            AdminUserRepository userRepository,
            AdminAccountActivityRepository activityRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public AdminStaffDashboardResponse dashboard() {
        var staff = userRepository.findAllByOrderByDisplayNameAsc().stream().map(this::toResponse).toList();
        var activities = activityRepository.findTop50ByOrderByCreatedAtDesc().stream()
                .map(activity -> new AdminAccountActivityResponse(
                        activity.getId(),
                        activity.getActivityType(),
                        activity.getSummary(),
                        activity.getActor().getDisplayName(),
                        activity.getTarget().getDisplayName(),
                        activity.getCreatedAt()
                ))
                .toList();
        return new AdminStaffDashboardResponse(staff, activities);
    }

    @Transactional
    public AdminStaffResponse create(CreateAdminStaffRequest request, String actorEmail) {
        PasswordPolicy.validate(request.temporaryPassword());
        var normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            throw new StaffAccountConflictException("A staff account already exists for this email address.");
        }
        var actor = findByEmail(actorEmail);
        var staff = userRepository.saveAndFlush(new AdminUser(
                normalizedEmail,
                passwordEncoder.encode(request.temporaryPassword()),
                request.displayName(),
                request.role()
        ));
        activityRepository.save(new AdminAccountActivity(
                actor,
                staff,
                AdminAccountActivityType.ACCOUNT_CREATED,
                actor.getDisplayName() + " created the " + roleLabel(staff.getRole()) + " account for " + staff.getDisplayName() + "."
        ));
        return toResponse(staff);
    }

    @Transactional
    public AdminStaffResponse setEnabled(
            UUID staffId,
            SetAdminStaffEnabledRequest request,
            String actorEmail
    ) {
        var actor = findByEmail(actorEmail);
        var staff = userRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff account was not found."));
        if (staff.getVersion() != request.version()) {
            throw new StaffAccountConflictException("This staff account changed in another session. Reload the latest team list.");
        }
        if (!request.enabled() && staff.getId().equals(actor.getId())) {
            throw new AccountSecurityException("You cannot deactivate your own account.");
        }
        if (!request.enabled() && staff.getRole() == AdminRole.OWNER
                && userRepository.countByRoleAndEnabledTrue(AdminRole.OWNER) <= 1) {
            throw new AccountSecurityException("At least one active Owner account is required.");
        }
        if (staff.isEnabled() == request.enabled()) {
            return toResponse(staff);
        }

        staff.setEnabled(request.enabled());
        try {
            userRepository.flush();
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException exception) {
            throw new StaffAccountConflictException("This staff account changed in another session. Reload the latest team list.");
        }
        var activityType = request.enabled()
                ? AdminAccountActivityType.ACCOUNT_ACTIVATED
                : AdminAccountActivityType.ACCOUNT_DEACTIVATED;
        var action = request.enabled() ? "activated" : "deactivated";
        activityRepository.save(new AdminAccountActivity(
                actor,
                staff,
                activityType,
                actor.getDisplayName() + " " + action + " " + staff.getDisplayName() + "'s account."
        ));
        return toResponse(staff);
    }

    @Transactional
    public void changePassword(ChangeAdminPasswordRequest request, String email) {
        PasswordPolicy.validate(request.newPassword());
        var user = findByEmail(email);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new AccountSecurityException("Current password is incorrect.");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new AccountSecurityException("Choose a new password that is different from your current password.");
        }
        user.changePassword(passwordEncoder.encode(request.newPassword()));
        userRepository.flush();
        activityRepository.save(new AdminAccountActivity(
                user,
                user,
                AdminAccountActivityType.PASSWORD_CHANGED,
                user.getDisplayName() + " changed their password."
        ));
    }

    private AdminUser findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Staff account is no longer available."));
    }

    private AdminStaffResponse toResponse(AdminUser staff) {
        return new AdminStaffResponse(
                staff.getId(),
                staff.getEmail(),
                staff.getDisplayName(),
                staff.getRole(),
                staff.isEnabled(),
                staff.getLastLoginAt(),
                staff.getPasswordChangedAt(),
                staff.isLocked() ? staff.getLockedUntil() : null,
                staff.getCreatedAt(),
                staff.getVersion()
        );
    }

    private String roleLabel(AdminRole role) {
        return role == AdminRole.OWNER ? "Owner" : "Staff";
    }
}
