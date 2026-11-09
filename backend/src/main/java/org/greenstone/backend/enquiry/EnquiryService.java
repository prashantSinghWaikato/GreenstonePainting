package org.greenstone.backend.enquiry;

import org.greenstone.backend.notification.EnquiryNotifier;
import org.greenstone.backend.persistence.entity.ContactPreference;
import org.greenstone.backend.persistence.entity.Enquiry;
import org.greenstone.backend.persistence.entity.EnquiryType;
import org.greenstone.backend.persistence.repository.EnquiryRepository;
import org.greenstone.backend.persistence.repository.ServiceOfferingRepository;
import org.greenstone.backend.web.NotificationDeliveryException;
import org.greenstone.backend.web.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class EnquiryService {

    private final EnquiryRepository enquiryRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;
    private final EnquiryNotifier enquiryNotifier;

    public EnquiryService(
            EnquiryRepository enquiryRepository,
            ServiceOfferingRepository serviceOfferingRepository,
            EnquiryNotifier enquiryNotifier
    ) {
        this.enquiryRepository = enquiryRepository;
        this.serviceOfferingRepository = serviceOfferingRepository;
        this.enquiryNotifier = enquiryNotifier;
    }

    @Transactional
    public EnquiryResponse createQuoteEnquiry(CreateEnquiryRequest request) {
        var service = serviceOfferingRepository.findBySlugAndActiveTrue(request.serviceSlug())
                .orElseThrow(() -> new ResourceNotFoundException("Selected service is not available."));

        var enquiry = new Enquiry(
                EnquiryType.QUOTE_REQUEST,
                request.firstName().trim(),
                request.lastName().trim(),
                request.email().trim(),
                request.message().trim()
        );
        enquiry.setPhone(request.phone().trim());
        enquiry.setPropertyAddress(request.propertyAddress().trim());
        enquiry.setContactPreference(ContactPreference.EITHER);
        enquiry.setService(service);
        enquiry.setCompletedAt(OffsetDateTime.now(ZoneOffset.UTC));

        var saved = enquiryRepository.saveAndFlush(enquiry);
        var notificationSent = true;
        try {
            enquiryNotifier.sendNewQuoteNotification(saved);
            saved.setNotificationSentAt(OffsetDateTime.now(ZoneOffset.UTC));
        } catch (NotificationDeliveryException exception) {
            notificationSent = false;
        }

        return new EnquiryResponse(saved.getId(), saved.getStatus(), saved.getCreatedAt(), notificationSent);
    }
}
