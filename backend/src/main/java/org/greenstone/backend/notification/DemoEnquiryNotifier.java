package org.greenstone.backend.notification;

import org.greenstone.backend.persistence.entity.AdminUser;
import org.greenstone.backend.persistence.entity.Enquiry;
import org.greenstone.backend.persistence.entity.EnquiryAttachment;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("!e2e")
@ConditionalOnProperty(name = "app.mail.delivery-enabled", havingValue = "false")
public class DemoEnquiryNotifier implements EnquiryNotifier {

    @Override
    public void sendNewQuoteNotification(Enquiry enquiry, List<EnquiryAttachment> attachments, String reviewToken) {
        // Demo deployments retain enquiries in the admin inbox without sending customer data by email.
    }

    @Override
    public void sendAssignmentNotification(Enquiry enquiry, AdminUser recipient) {
        // Workflow email is intentionally disabled for the free demo.
    }

    @Override
    public void sendFollowUpNotification(Enquiry enquiry, AdminUser recipient, boolean overdue) {
        // Workflow email is intentionally disabled for the free demo.
    }

    @Override
    public void sendDailyDigest(AdminUser recipient, long unassignedCount, List<Enquiry> overdueEnquiries) {
        // Workflow email is intentionally disabled for the free demo.
    }
}
