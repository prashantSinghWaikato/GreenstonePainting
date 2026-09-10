package org.greenstone.backend.admin.job;
import org.greenstone.backend.persistence.entity.InvoiceStatus;
import java.math.BigDecimal;
import java.util.UUID;
public record AdminJobInvoiceSummary(UUID id, String invoiceNumber, InvoiceStatus status, BigDecimal total, BigDecimal amountPaid, BigDecimal balanceDue) {}
