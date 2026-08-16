package org.greenstone.backend;

import org.greenstone.backend.persistence.entity.Enquiry;
import org.greenstone.backend.persistence.entity.EnquiryStatus;
import org.greenstone.backend.persistence.entity.EnquiryType;
import org.greenstone.backend.persistence.entity.ServiceOffering;
import org.greenstone.backend.persistence.repository.EnquiryRepository;
import org.greenstone.backend.persistence.repository.ServiceOfferingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class BackendApplicationTests {

    @Autowired
    private ServiceOfferingRepository serviceOfferingRepository;

    @Autowired
    private EnquiryRepository enquiryRepository;

    @Test
    void contextLoads() {
    }

    @Test
    @Transactional
    void persistsAQuoteEnquiryForAService() {
        var service = new ServiceOffering(
                "interior-painting",
                "Interior Painting",
                "Professional interior painting for homes and businesses.",
                "Preparation, painting, and finishing for interior spaces.",
                1
        );
        serviceOfferingRepository.save(service);

        var enquiry = new Enquiry(
                EnquiryType.QUOTE_REQUEST,
                "Aroha",
                "Williams",
                "aroha@example.com",
                "Please quote repainting a three-bedroom home."
        );
        enquiry.setService(service);
        enquiry.setPhone("021 555 0100");
        enquiryRepository.saveAndFlush(enquiry);

        assertThat(enquiry.getId()).isNotNull();
        assertThat(enquiryRepository.countByStatus(EnquiryStatus.NEW)).isEqualTo(1);
        assertThat(serviceOfferingRepository.findBySlug("interior-painting")).contains(service);
    }
}
