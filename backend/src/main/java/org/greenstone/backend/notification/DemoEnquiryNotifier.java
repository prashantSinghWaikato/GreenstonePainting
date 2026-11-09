package org.greenstone.backend.notification;

import org.greenstone.backend.persistence.entity.Enquiry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!e2e")
@ConditionalOnProperty(name = "app.mail.delivery-enabled", havingValue = "false")
public class DemoEnquiryNotifier implements EnquiryNotifier {

    @Override
    public void sendNewQuoteNotification(Enquiry enquiry) {
        // The enquiry remains stored even when email delivery is disabled.
    }
}
