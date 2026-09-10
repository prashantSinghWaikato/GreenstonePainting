package org.greenstone.backend.admin.invoice;
import org.greenstone.backend.persistence.entity.InvoiceStatus;
import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;
public record AdminInvoiceResponse(UUID id, UUID jobId, String jobNumber, String invoiceNumber, long version, InvoiceStatus status, String customerName, String customerEmail, String propertyAddress, BigDecimal subtotal, BigDecimal gstAmount, BigDecimal total, BigDecimal amountPaid, BigDecimal balanceDue, LocalDate dueDate, OffsetDateTime sentAt, OffsetDateTime paidAt, String paymentReference, String notes, OffsetDateTime createdAt) {}
