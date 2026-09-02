package org.greenstone.backend.admin.staff;

import java.util.List;

public record AdminStaffDashboardResponse(
        List<AdminStaffResponse> staff,
        List<AdminAccountActivityResponse> activities
) {
}
