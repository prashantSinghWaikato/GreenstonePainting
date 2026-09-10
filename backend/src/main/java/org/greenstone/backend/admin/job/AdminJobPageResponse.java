package org.greenstone.backend.admin.job;

import org.greenstone.backend.admin.enquiry.AdminStaffOptionResponse;
import java.util.List;

public record AdminJobPageResponse(List<AdminJobSummaryResponse> items, List<AdminStaffOptionResponse> staff, AdminJobMetricsResponse metrics) {}
