package org.greenstone.backend.notification;

import org.greenstone.backend.persistence.entity.Enquiry;

public interface EnquiryNotifier {
    void sendNewQuoteNotification(Enquiry enquiry);
}
