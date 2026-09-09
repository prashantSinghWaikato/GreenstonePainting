package org.greenstone.backend.notification;

import org.greenstone.backend.persistence.entity.Enquiry;
import org.greenstone.backend.persistence.entity.EnquiryAttachment;
import org.greenstone.backend.persistence.entity.AdminUser;

import java.util.List;

public interface EnquiryNotifier {
    void sendNewQuoteNotification(Enquiry enquiry, List<EnquiryAttachment> attachments, String reviewToken);
    void sendAssignmentNotification(Enquiry enquiry, AdminUser recipient);
    void sendFollowUpNotification(Enquiry enquiry, AdminUser recipient, boolean overdue);
    void sendDailyDigest(AdminUser recipient, long unassignedCount, List<Enquiry> overdueEnquiries);
}
