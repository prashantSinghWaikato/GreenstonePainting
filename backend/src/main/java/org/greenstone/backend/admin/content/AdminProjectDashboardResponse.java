package org.greenstone.backend.admin.content;

import java.util.List;

public record AdminProjectDashboardResponse(
        List<AdminProjectSummaryResponse> projects,
        List<ProjectServiceOptionResponse> services
) {
}
