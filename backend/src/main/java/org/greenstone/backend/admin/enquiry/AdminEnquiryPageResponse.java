package org.greenstone.backend.admin.enquiry;

import org.greenstone.backend.persistence.entity.EnquiryStatus;

import java.util.List;
import java.util.Map;

public record AdminEnquiryPageResponse(
        List<AdminEnquirySummaryResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        Map<EnquiryStatus, Long> statusCounts,
        List<AdminServiceFilterResponse> services,
        List<AdminStaffOptionResponse> staff,
        AdminEnquiryMetricsResponse metrics
) {
}
