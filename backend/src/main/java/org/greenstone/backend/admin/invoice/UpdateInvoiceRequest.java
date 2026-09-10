package org.greenstone.backend.admin.invoice;
import jakarta.validation.constraints.*;
import org.greenstone.backend.persistence.entity.InvoiceStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
public record UpdateInvoiceRequest(@NotNull InvoiceStatus status, @NotNull @DecimalMin("0.00") BigDecimal amountPaid, @NotNull LocalDate dueDate, @Size(max=200) String paymentReference, @Size(max=10000) String notes, @NotNull @PositiveOrZero Long version) {}
