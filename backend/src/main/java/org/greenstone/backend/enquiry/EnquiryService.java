package org.greenstone.backend.enquiry;

import org.greenstone.backend.persistence.entity.ContactPreference;
import org.greenstone.backend.persistence.entity.Enquiry;
import org.greenstone.backend.persistence.entity.EnquiryAttachment;
import org.greenstone.backend.persistence.entity.EnquiryType;
import org.greenstone.backend.persistence.repository.EnquiryAttachmentRepository;
import org.greenstone.backend.persistence.repository.EnquiryRepository;
import org.greenstone.backend.persistence.repository.ServiceOfferingRepository;
import org.greenstone.backend.storage.FileStorageService;
import org.greenstone.backend.web.ResourceNotFoundException;
import org.greenstone.backend.web.UploadValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class EnquiryService {

    private final EnquiryRepository enquiryRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;
    private final EnquiryAttachmentRepository attachmentRepository;
    private final FileStorageService fileStorageService;

    public EnquiryService(
            EnquiryRepository enquiryRepository,
            ServiceOfferingRepository serviceOfferingRepository,
            EnquiryAttachmentRepository attachmentRepository,
            FileStorageService fileStorageService
    ) {
        this.enquiryRepository = enquiryRepository;
        this.serviceOfferingRepository = serviceOfferingRepository;
        this.attachmentRepository = attachmentRepository;
        this.fileStorageService = fileStorageService;
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

        var uploadToken = UUID.randomUUID() + "." + UUID.randomUUID();
        var uploadExpiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(24);
        enquiry.setUploadTokenHash(hashUploadToken(uploadToken));
        enquiry.setUploadTokenExpiresAt(uploadExpiresAt);

        var saved = enquiryRepository.save(enquiry);
        return new EnquiryResponse(saved.getId(), saved.getStatus(), saved.getCreatedAt(), uploadToken, uploadExpiresAt);
    }

    @Transactional
    public EnquiryAttachmentResponse addAttachment(UUID enquiryId, String uploadToken, MultipartFile file) {
        var enquiry = enquiryRepository.findByIdForAttachmentUpload(enquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Quote request was not found."));

        if (!isValidUploadToken(enquiry, uploadToken)) {
            throw new ResourceNotFoundException("Photo upload access is invalid or has expired.");
        }
        if (attachmentRepository.countByEnquiryId(enquiryId) >= 4) {
            throw new UploadValidationException("A quote request can include up to 4 photos.");
        }

        var storedObject = fileStorageService.storeEnquiryPhoto(enquiryId, file);
        var originalFilename = safeOriginalFilename(file.getOriginalFilename());
        try {
            var attachment = attachmentRepository.saveAndFlush(new EnquiryAttachment(
                    enquiry,
                    storedObject.objectKey(),
                    originalFilename,
                    storedObject.contentType(),
                    storedObject.sizeBytes()
            ));
            return new EnquiryAttachmentResponse(
                    attachment.getId(),
                    attachment.getOriginalFilename(),
                    attachment.getContentType(),
                    attachment.getSizeBytes(),
                    attachment.getCreatedAt()
            );
        } catch (RuntimeException exception) {
            fileStorageService.delete(storedObject.objectKey());
            throw exception;
        }
    }

    private boolean isValidUploadToken(Enquiry enquiry, String uploadToken) {
        if (uploadToken == null || uploadToken.isBlank() || enquiry.getUploadTokenHash() == null
                || enquiry.getUploadTokenExpiresAt() == null
                || enquiry.getUploadTokenExpiresAt().isBefore(OffsetDateTime.now(ZoneOffset.UTC))) {
            return false;
        }
        return MessageDigest.isEqual(
                enquiry.getUploadTokenHash().getBytes(StandardCharsets.US_ASCII),
                hashUploadToken(uploadToken).getBytes(StandardCharsets.US_ASCII)
        );
    }

    private String safeOriginalFilename(String originalFilename) {
        var filename = originalFilename == null || originalFilename.isBlank() ? "project-photo" : originalFilename;
        filename = filename.replaceAll("[\\x00-\\x1f\\x7f]", "_");
        filename = filename.substring(Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\')) + 1);
        return filename.length() <= 255 ? filename : filename.substring(filename.length() - 255);
    }

    static String hashUploadToken(String token) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable.", exception);
        }
    }
}
