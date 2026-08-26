package org.greenstone.backend;

import org.greenstone.backend.notification.EmailEnquiryNotifier;
import org.greenstone.backend.persistence.entity.Enquiry;
import org.greenstone.backend.persistence.entity.ServiceOffering;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailEnquiryNotifierTests {

    @Test
    void createsAReplyableHtmlNotificationWithoutUsingAnExternalMailServer() throws Exception {
        var mailSender = mock(JavaMailSender.class);
        var message = new JavaMailSenderImpl().createMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(message);

        var service = mock(ServiceOffering.class);
        when(service.getTitle()).thenReturn("Interior Painting");
        var enquiry = mock(Enquiry.class);
        when(enquiry.getId()).thenReturn(UUID.fromString("12345678-1234-1234-1234-123456789abc"));
        when(enquiry.getFirstName()).thenReturn("Aroha");
        when(enquiry.getLastName()).thenReturn("Williams");
        when(enquiry.getEmail()).thenReturn("aroha@example.com");
        when(enquiry.getPhone()).thenReturn("021 555 0100");
        when(enquiry.getPropertyAddress()).thenReturn("29 Example Street, Hamilton");
        when(enquiry.getMessage()).thenReturn("Please quote our interior repaint.");
        when(enquiry.getService()).thenReturn(service);

        var notifier = new EmailEnquiryNotifier(
                mailSender,
                "quotes@greenstone.local",
                "no-reply@greenstone.local",
                "http://localhost:8080"
        );
        notifier.sendNewQuoteNotification(enquiry, List.of(), "review-token");

        verify(mailSender).send(message);
        message.saveChanges();
        assertThat(message.getSubject()).isEqualTo("New quote request 12345678 · Interior Painting");
        assertThat(message.getReplyTo()[0].toString()).contains("aroha@example.com");
        assertThat(message.getContentType()).contains("multipart/mixed");
    }
}
