package org.greenstone.backend.admin.quote;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.greenstone.backend.persistence.entity.QuoteItemCategory;

import java.math.BigDecimal;

public record QuoteItemRequest(
        @NotNull QuoteItemCategory category,
        @NotBlank @Size(max = 500) String description,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 8, fraction = 2) BigDecimal quantity,
        @NotBlank @Size(max = 40) String unit,
        @NotNull @DecimalMin(value = "0.00") @Digits(integer = 10, fraction = 2) BigDecimal unitPrice,
        boolean optional
) {
}
