package org.greenstone.backend.admin.quote;

import org.greenstone.backend.persistence.entity.Quote;

public interface QuoteNotifier {
    void sendQuote(Quote quote, byte[] pdf, String responseToken);
}
