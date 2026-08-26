package org.greenstone.backend.notification;

import org.greenstone.backend.persistence.entity.Enquiry;
import org.greenstone.backend.persistence.entity.EnquiryAttachment;

import java.util.List;

public interface EnquiryNotifier {
    void sendNewQuoteNotification(Enquiry enquiry, List<EnquiryAttachment> attachments, String reviewToken);
}
