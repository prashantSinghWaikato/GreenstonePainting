package org.greenstone.backend.admin.job;

public record AdminJobMetricsResponse(long planned, long scheduled, long inProgress, long completed) {}
