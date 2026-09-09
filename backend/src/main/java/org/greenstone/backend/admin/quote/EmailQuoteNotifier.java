package org.greenstone.backend.admin.quote;

import jakarta.mail.MessagingException;
import org.greenstone.backend.persistence.entity.Quote;
import org.greenstone.backend.web.NotificationDeliveryException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class EmailQuoteNotifier implements QuoteNotifier {

    private final JavaMailSender mailSender;
    private final String sender;
    private final String frontendBaseUrl;

    public EmailQuoteNotifier(
            JavaMailSender mailSender,
            @Value("${app.notification.from}") String sender,
            @Value("${app.frontend-base-url}") String frontendBaseUrl
    ) {
        this.mailSender = mailSender;
        this.sender = sender;
        this.frontendBaseUrl = frontendBaseUrl.replaceAll("/+$", "");
    }

    @Override
    public void sendQuote(Quote quote, byte[] pdf, String responseToken) {
        var responseUrl = frontendBaseUrl + "/quote/?token="
                + URLEncoder.encode(responseToken, StandardCharsets.UTF_8);
        var subject = "Your Greenstone Painting quote " + quote.getQuoteNumber();
        var plain = """
                Kia ora %s,

                Your Greenstone Painting quote is ready.

                Quote: %s
                Total including GST: $%,.2f
                Valid until: %s

                Review and respond securely: %s

                A PDF copy is attached for your records.
                """.formatted(
                quote.getCustomerName(), quote.getQuoteNumber(), quote.getTotal(), quote.getValidUntil(), responseUrl
        );
        var html = """
                <!doctype html><html><body style="margin:0;background:#f1eee8;font-family:Arial,sans-serif;color:#122033">
                <div style="max-width:680px;margin:0 auto;padding:32px 18px">
                  <div style="background:#173f34;color:#fff;padding:24px 28px">
                    <div style="color:#efad46;font-size:12px;font-weight:700;letter-spacing:1.4px;text-transform:uppercase">Greenstone Painting</div>
                    <h1 style="margin:8px 0 0;font-size:25px">Your painting quote is ready</h1>
                  </div>
                  <div style="background:#fff;padding:28px">
                    <p>Kia ora %s,</p><p>Please review your quote for <strong>%s</strong>.</p>
                    <div style="margin:24px 0;padding:18px;background:#eef2f1">
                      <div style="color:#66736d;font-size:12px">TOTAL INCLUDING GST</div>
                      <strong style="font-size:28px">$%,.2f</strong>
                    </div>
                    <a href="%s" style="display:inline-block;padding:14px 20px;background:#0b2a3e;color:#fff;text-decoration:none;font-weight:700">Review and respond</a>
                    <p style="margin-top:24px;color:#66736d;font-size:12px">Quote %s · Valid until %s. A PDF copy is attached.</p>
                  </div>
                </div></body></html>
                """.formatted(
                escape(quote.getCustomerName()), escape(quote.getTitle()), quote.getTotal(), escape(responseUrl),
                escape(quote.getQuoteNumber()), quote.getValidUntil()
        );

        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setTo(quote.getCustomerEmail());
            helper.setFrom(sender);
            helper.setSubject(subject);
            helper.setText(plain, html);
            helper.addAttachment(quote.getQuoteNumber() + ".pdf", new ByteArrayResource(pdf));
            mailSender.send(message);
        } catch (MessagingException | MailException exception) {
            throw new NotificationDeliveryException("The quote is saved, but it could not be emailed to the customer.", exception);
        }
    }

    private String escape(String value) {
        return HtmlUtils.htmlEscape(value == null ? "" : value);
    }
}
