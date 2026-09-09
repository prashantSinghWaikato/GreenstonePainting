package org.greenstone.backend.admin.quote;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record QuoteDecisionRequest(
        @NotNull QuoteDecision decision,
        @Size(max = 1000) String reason
) {
}
