package org.greenstone.backend.admin.notification;

import org.greenstone.backend.persistence.repository.AdminUserRepository;
import org.greenstone.backend.web.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminNotificationPreferencesService {

    private final AdminUserRepository userRepository;

    public AdminNotificationPreferencesService(AdminUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public AdminNotificationPreferencesResponse find(String email) {
        return response(findUser(email));
    }

    @Transactional
    public AdminNotificationPreferencesResponse update(
            String email,
            UpdateAdminNotificationPreferencesRequest request
    ) {
        var user = findUser(email);
        user.setNotificationPreferences(
                request.assignmentNotificationsEnabled(),
                request.followUpNotificationsEnabled(),
                request.dailyDigestEnabled()
        );
        userRepository.flush();
        return response(user);
    }

    private org.greenstone.backend.persistence.entity.AdminUser findUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Staff account is no longer available."));
    }

    private AdminNotificationPreferencesResponse response(
            org.greenstone.backend.persistence.entity.AdminUser user
    ) {
        return new AdminNotificationPreferencesResponse(
                user.isAssignmentNotificationsEnabled(),
                user.isFollowUpNotificationsEnabled(),
                user.isDailyDigestEnabled()
        );
    }
}
