package org.greenstone.backend.admin.report;
import java.math.BigDecimal;
public record AdminReportSummaryResponse(long activeEnquiries, long acceptedQuotes, long plannedJobs, long scheduledJobs, long inProgressJobs, long completedJobs, long overdueInvoices, BigDecimal acceptedQuoteValue, BigDecimal invoicedValue, BigDecimal collectedRevenue, BigDecimal outstandingRevenue) {}
