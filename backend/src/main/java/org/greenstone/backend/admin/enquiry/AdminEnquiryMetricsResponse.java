package org.greenstone.backend.admin.enquiry;

public record AdminEnquiryMetricsResponse(
        long unassigned,
        long dueToday,
        long overdue,
        long mine
) {
}
