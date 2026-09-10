package org.greenstone.backend.notification;

import jakarta.mail.MessagingException;
import org.greenstone.backend.persistence.entity.Enquiry;
import org.greenstone.backend.persistence.entity.EnquiryAttachment;
import org.greenstone.backend.persistence.entity.AdminUser;
import org.greenstone.backend.web.NotificationDeliveryException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Profile("!e2e")
public class EmailEnquiryNotifier implements EnquiryNotifier {

    private final JavaMailSender mailSender;
    private final String recipient;
    private final String sender;
    private final String publicBaseUrl;
    private final String frontendBaseUrl;

    public EmailEnquiryNotifier(
            JavaMailSender mailSender,
            @Value("${app.notification.to}") String recipient,
            @Value("${app.notification.from}") String sender,
            @Value("${app.public-base-url}") String publicBaseUrl,
            @Value("${app.frontend-base-url}") String frontendBaseUrl
    ) {
        this.mailSender = mailSender;
        this.recipient = recipient;
        this.sender = sender;
        this.publicBaseUrl = publicBaseUrl.replaceAll("/+$", "");
        this.frontendBaseUrl = frontendBaseUrl.replaceAll("/+$", "");
    }

    @Override
    public void sendNewQuoteNotification(Enquiry enquiry, List<EnquiryAttachment> attachments, String reviewToken) {
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setTo(recipient);
            helper.setFrom(sender);
            helper.setReplyTo(enquiry.getEmail());
            helper.setSubject("New quote request " + reference(enquiry) + " · " + singleLine(enquiry.getService().getTitle()));
            helper.setText(plainText(enquiry, attachments, reviewToken), htmlText(enquiry, attachments, reviewToken));
            mailSender.send(message);
        } catch (MessagingException | MailException exception) {
            throw new NotificationDeliveryException("The quote was saved, but the team notification could not be sent.", exception);
        }
    }

    @Override
    public void sendAssignmentNotification(Enquiry enquiry, AdminUser recipient) {
        var subject = "Enquiry " + reference(enquiry) + " assigned to you";
        var actionUrl = adminEnquiryUrl();
        var plain = """
                Hi %s,

                %s %s's %s enquiry has been assigned to you.
                Priority: %s
                Follow-up: %s

                Open the enquiry: %s
                """.formatted(
                recipient.getDisplayName(), enquiry.getFirstName(), enquiry.getLastName(), serviceTitle(enquiry),
                enquiry.getPriority().name(), displayDate(enquiry.getFollowUpAt()), actionUrl
        );
        sendStaffMessage(recipient.getEmail(), subject, plain,
                workflowHtml("Enquiry assigned", "A customer enquiry is now assigned to you.", enquiry, actionUrl));
    }

    @Override
    public void sendFollowUpNotification(Enquiry enquiry, AdminUser recipient, boolean overdue) {
        var heading = overdue ? "Follow-up overdue" : "Follow-up due";
        var subject = heading + " · enquiry " + reference(enquiry);
        var actionUrl = adminEnquiryUrl();
        var plain = """
                Hi %s,

                Your follow-up for %s %s is %s.
                Enquiry: %s
                Service: %s
                Follow-up: %s

                Open the enquiry: %s
                """.formatted(
                recipient.getDisplayName(), enquiry.getFirstName(), enquiry.getLastName(),
                overdue ? "overdue" : "due", reference(enquiry), serviceTitle(enquiry),
                displayDate(enquiry.getFollowUpAt()), actionUrl
        );
        sendStaffMessage(recipient.getEmail(), subject, plain,
                workflowHtml(heading, "A scheduled customer follow-up needs attention.", enquiry, actionUrl));
    }

    @Override
    public void sendDailyDigest(AdminUser recipient, long unassignedCount, List<Enquiry> overdueEnquiries) {
        var actionUrl = adminEnquiryUrl();
        var lines = new StringBuilder();
        for (var enquiry : overdueEnquiries) {
            lines.append("\n- ").append(reference(enquiry)).append(" · ")
                    .append(enquiry.getFirstName()).append(' ').append(enquiry.getLastName())
                    .append(" · ").append(displayDate(enquiry.getFollowUpAt()));
        }
        if (overdueEnquiries.isEmpty()) lines.append("\n- None");
        var plain = """
                Hi %s,

                Greenstone Painting daily enquiry summary:
                Unassigned active enquiries: %d
                Your overdue follow-ups: %d
                %s

                Open the enquiry inbox: %s
                """.formatted(recipient.getDisplayName(), unassignedCount, overdueEnquiries.size(), lines, actionUrl);
        var html = """
                <h1 style="margin:0 0 12px">Daily enquiry summary</h1>
                <p style="color:#56646d">Your Greenstone workflow snapshot.</p>
                <div style="display:flex;gap:12px;margin:24px 0">
                  <div style="padding:16px;background:#eef2f1"><strong style="font-size:24px">%d</strong><br>Unassigned</div>
                  <div style="padding:16px;background:#f6ded4"><strong style="font-size:24px">%d</strong><br>Your overdue follow-ups</div>
                </div>
                <a href="%s" style="display:inline-block;padding:12px 18px;background:#0b2a3e;color:#fff;text-decoration:none">Open enquiry inbox</a>
                """.formatted(unassignedCount, overdueEnquiries.size(), escape(actionUrl));
        sendStaffMessage(recipient.getEmail(), "Greenstone daily enquiry summary", plain, emailShell(html));
    }

    private void sendStaffMessage(String to, String subject, String plain, String html) {
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setTo(to);
            helper.setFrom(sender);
            helper.setSubject(singleLine(subject));
            helper.setText(plain, html);
            mailSender.send(message);
        } catch (MessagingException | MailException exception) {
            throw new NotificationDeliveryException("The staff notification could not be sent.", exception);
        }
    }

    private String workflowHtml(String heading, String introduction, Enquiry enquiry, String actionUrl) {
        return emailShell("""
                <h1 style="margin:0 0 12px">%s</h1>
                <p style="color:#56646d">%s</p>
                <table style="width:100%%;border-collapse:collapse;margin:22px 0">
                  <tr><td style="padding:7px 12px 7px 0;color:#66736d">Reference</td><td><strong>%s</strong></td></tr>
                  <tr><td style="padding:7px 12px 7px 0;color:#66736d">Customer</td><td>%s %s</td></tr>
                  <tr><td style="padding:7px 12px 7px 0;color:#66736d">Service</td><td>%s</td></tr>
                  <tr><td style="padding:7px 12px 7px 0;color:#66736d">Priority</td><td>%s</td></tr>
                  <tr><td style="padding:7px 12px 7px 0;color:#66736d">Follow-up</td><td>%s</td></tr>
                </table>
                <a href="%s" style="display:inline-block;padding:12px 18px;background:#0b2a3e;color:#fff;text-decoration:none">Open enquiry inbox</a>
                """.formatted(
                escape(heading), escape(introduction), reference(enquiry), escape(enquiry.getFirstName()),
                escape(enquiry.getLastName()), escape(serviceTitle(enquiry)), enquiry.getPriority().name(),
                escape(displayDate(enquiry.getFollowUpAt())), escape(actionUrl)
        ));
    }

    private String emailShell(String content) {
        return """
                <!doctype html><html><body style="margin:0;background:#f1eee8;font-family:Arial,sans-serif;color:#122033">
                <div style="max-width:680px;margin:0 auto;padding:32px 18px">
                  <div style="background:#173f34;color:#fff;padding:18px 24px"><strong>Greenstone Painting</strong></div>
                  <div style="background:#fff;padding:28px">%s</div>
                </div></body></html>
                """.formatted(content);
    }

    private String adminEnquiryUrl() {
        return frontendBaseUrl + "/admin/?section=enquiries";
    }

    private String serviceTitle(Enquiry enquiry) {
        return enquiry.getService() == null ? "painting" : enquiry.getService().getTitle();
    }

    private String displayDate(java.time.OffsetDateTime value) {
        if (value == null) return "Not scheduled";
        return value.atZoneSameInstant(java.time.ZoneId.of("Pacific/Auckland"))
                .format(java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a"));
    }

    private String plainText(Enquiry enquiry, List<EnquiryAttachment> attachments, String reviewToken) {
        var photoLinks = new StringBuilder();
        for (var attachment : attachments) {
            photoLinks.append("\n- ").append(attachment.getOriginalFilename()).append(": ")
                    .append(photoUrl(enquiry, attachment, reviewToken));
        }
        if (attachments.isEmpty()) photoLinks.append("\nNo project photos were supplied.");

        return """
                New Greenstone Painting quote request

                Reference: %s
                Customer: %s %s
                Email: %s
                Phone: %s
                Property: %s
                Service: %s

                Project description:
                %s

                Project photos:%s

                Reply to this email to contact the customer.
                """.formatted(
                reference(enquiry), enquiry.getFirstName(), enquiry.getLastName(), enquiry.getEmail(),
                enquiry.getPhone(), enquiry.getPropertyAddress(), enquiry.getService().getTitle(),
                enquiry.getMessage(), photoLinks
        );
    }

    private String htmlText(Enquiry enquiry, List<EnquiryAttachment> attachments, String reviewToken) {
        var photoLinks = new StringBuilder();
        if (attachments.isEmpty()) {
            photoLinks.append("<p style=\"color:#66736d\">No project photos were supplied.</p>");
        } else {
            photoLinks.append("<ul>");
            for (var attachment : attachments) {
                photoLinks.append("<li style=\"margin:8px 0\"><a href=\"")
                        .append(HtmlUtils.htmlEscape(photoUrl(enquiry, attachment, reviewToken)))
                        .append("\">")
                        .append(HtmlUtils.htmlEscape(attachment.getOriginalFilename()))
                        .append("</a></li>");
            }
            photoLinks.append("</ul>");
        }

        return """
                <!doctype html><html><body style="margin:0;background:#f1eee8;font-family:Arial,sans-serif;color:#122033">
                <div style="max-width:680px;margin:0 auto;padding:32px 18px">
                  <div style="background:#173f34;color:#fff;padding:24px 28px;border-radius:12px 12px 0 0">
                    <div style="color:#efad46;font-size:12px;font-weight:700;letter-spacing:1.4px;text-transform:uppercase">Greenstone Painting</div>
                    <h1 style="margin:8px 0 0;font-size:25px">New quote request</h1>
                    <p style="margin:8px 0 0;color:#c7d8d2">Reference %s</p>
                  </div>
                  <div style="background:#fff;padding:28px;border-radius:0 0 12px 12px">
                    <table style="width:100%%;border-collapse:collapse;font-size:14px">
                      <tr><td style="padding:7px 12px 7px 0;color:#66736d">Customer</td><td style="padding:7px 0;font-weight:700">%s %s</td></tr>
                      <tr><td style="padding:7px 12px 7px 0;color:#66736d">Email</td><td style="padding:7px 0"><a href="mailto:%s">%s</a></td></tr>
                      <tr><td style="padding:7px 12px 7px 0;color:#66736d">Phone</td><td style="padding:7px 0">%s</td></tr>
                      <tr><td style="padding:7px 12px 7px 0;color:#66736d">Property</td><td style="padding:7px 0">%s</td></tr>
                      <tr><td style="padding:7px 12px 7px 0;color:#66736d">Service</td><td style="padding:7px 0">%s</td></tr>
                    </table>
                    <h2 style="margin:26px 0 8px;font-size:17px">Project description</h2>
                    <p style="margin:0;white-space:pre-wrap;line-height:1.6">%s</p>
                    <h2 style="margin:26px 0 8px;font-size:17px">Project photos</h2>
                    %s
                    <p style="margin:28px 0 0;padding-top:18px;border-top:1px solid #e3e8e5;color:#66736d;font-size:12px">Photo links expire after 30 days. Reply to this email to contact the customer.</p>
                  </div>
                </div></body></html>
                """.formatted(
                reference(enquiry), escape(enquiry.getFirstName()), escape(enquiry.getLastName()),
                escape(enquiry.getEmail()), escape(enquiry.getEmail()), escape(enquiry.getPhone()),
                escape(enquiry.getPropertyAddress()), escape(enquiry.getService().getTitle()),
                escape(enquiry.getMessage()), photoLinks
        );
    }

    private String photoUrl(Enquiry enquiry, EnquiryAttachment attachment, String reviewToken) {
        return publicBaseUrl + "/api/enquiries/" + enquiry.getId() + "/attachments/" + attachment.getId()
                + "?token=" + URLEncoder.encode(reviewToken, StandardCharsets.UTF_8);
    }

    private String reference(Enquiry enquiry) {
        return enquiry.getId().toString().substring(0, 8).toUpperCase();
    }

    private String escape(String value) {
        return HtmlUtils.htmlEscape(value == null ? "" : value);
    }

    private String singleLine(String value) {
        return value == null ? "" : value.replaceAll("[\\r\\n]+", " ").trim();
    }
}
