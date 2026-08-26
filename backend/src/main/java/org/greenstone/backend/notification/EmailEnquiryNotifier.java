package org.greenstone.backend.notification;

import jakarta.mail.MessagingException;
import org.greenstone.backend.persistence.entity.Enquiry;
import org.greenstone.backend.persistence.entity.EnquiryAttachment;
import org.greenstone.backend.web.NotificationDeliveryException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class EmailEnquiryNotifier implements EnquiryNotifier {

    private final JavaMailSender mailSender;
    private final String recipient;
    private final String sender;
    private final String publicBaseUrl;

    public EmailEnquiryNotifier(
            JavaMailSender mailSender,
            @Value("${app.notification.to}") String recipient,
            @Value("${app.notification.from}") String sender,
            @Value("${app.public-base-url}") String publicBaseUrl
    ) {
        this.mailSender = mailSender;
        this.recipient = recipient;
        this.sender = sender;
        this.publicBaseUrl = publicBaseUrl.replaceAll("/+$", "");
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
