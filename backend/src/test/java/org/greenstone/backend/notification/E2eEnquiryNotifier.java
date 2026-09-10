package org.greenstone.backend.notification;

import org.greenstone.backend.persistence.entity.AdminUser;
import org.greenstone.backend.persistence.entity.Enquiry;
import org.greenstone.backend.persistence.entity.EnquiryAttachment;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("e2e")
public class E2eEnquiryNotifier implements EnquiryNotifier {
    @Override
    public void sendNewQuoteNotification(Enquiry enquiry, List<EnquiryAttachment> attachments, String reviewToken) {
        // Deliberately empty: browser tests verify the application workflow without an SMTP dependency.
    }

    @Override
    public void sendAssignmentNotification(Enquiry enquiry, AdminUser recipient) {
    }

    @Override
    public void sendFollowUpNotification(Enquiry enquiry, AdminUser recipient, boolean overdue) {
    }

    @Override
    public void sendDailyDigest(AdminUser recipient, long unassignedCount, List<Enquiry> overdueEnquiries) {
    }
}
