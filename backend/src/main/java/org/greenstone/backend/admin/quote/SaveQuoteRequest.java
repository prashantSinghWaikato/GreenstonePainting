package org.greenstone.backend.admin.quote;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record SaveQuoteRequest(
        @NotBlank @Size(max = 200) String customerName,
        @NotBlank @Email @Size(max = 254) String customerEmail,
        @Size(max = 300) String propertyAddress,
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 10000) String scope,
        @NotBlank @Size(max = 10000) String terms,
        @NotNull @FutureOrPresent LocalDate validUntil,
        LocalDate estimatedStartDate,
        LocalDate estimatedEndDate,
        @NotEmpty @Size(max = 30) List<@Valid QuoteItemRequest> items,
        @NotNull @PositiveOrZero Long version
) {
}
