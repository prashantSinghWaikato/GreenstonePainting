package org.greenstone.backend.admin.job;

import java.util.List;

public record AdminJobOverviewResponse(AdminJobMetricsResponse metrics, List<AdminJobSummaryResponse> upcoming) {}
