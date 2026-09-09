package org.greenstone.backend.admin.quote;

import org.greenstone.backend.persistence.entity.AdminUser;
import org.greenstone.backend.persistence.entity.EnquiryStatus;
import org.greenstone.backend.persistence.entity.Quote;
import org.greenstone.backend.persistence.entity.QuoteActivity;
import org.greenstone.backend.persistence.entity.QuoteActivityType;
import org.greenstone.backend.persistence.entity.QuoteItem;
import org.greenstone.backend.persistence.entity.QuoteItemCategory;
import org.greenstone.backend.persistence.entity.QuoteStatus;
import org.greenstone.backend.persistence.repository.AdminUserRepository;
import org.greenstone.backend.persistence.repository.EnquiryRepository;
import org.greenstone.backend.persistence.repository.QuoteActivityRepository;
import org.greenstone.backend.persistence.repository.QuoteItemRepository;
import org.greenstone.backend.persistence.repository.QuoteRepository;
import org.greenstone.backend.web.QuoteValidationException;
import org.greenstone.backend.web.ResourceNotFoundException;
import org.greenstone.backend.web.WorkflowConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class AdminQuoteService {

    private static final BigDecimal GST_RATE = new BigDecimal("0.1500");
    private static final ZoneId BUSINESS_TIME_ZONE = ZoneId.of("Pacific/Auckland");
    private static final String DEFAULT_TERMS = """
            This quote is valid until the date shown. Work outside the agreed scope will be discussed and approved before proceeding. Project timing is subject to weather, site access, and final colour or product selection.
            """.trim();

    private final QuoteRepository quoteRepository;
    private final QuoteItemRepository itemRepository;
    private final QuoteActivityRepository activityRepository;
    private final EnquiryRepository enquiryRepository;
    private final AdminUserRepository userRepository;
    private final QuotePdfService pdfService;
    private final QuoteNotifier notifier;

    public AdminQuoteService(
            QuoteRepository quoteRepository,
            QuoteItemRepository itemRepository,
            QuoteActivityRepository activityRepository,
            EnquiryRepository enquiryRepository,
            AdminUserRepository userRepository,
            QuotePdfService pdfService,
            QuoteNotifier notifier
    ) {
        this.quoteRepository = quoteRepository;
        this.itemRepository = itemRepository;
        this.activityRepository = activityRepository;
        this.enquiryRepository = enquiryRepository;
        this.userRepository = userRepository;
        this.pdfService = pdfService;
        this.notifier = notifier;
    }

    @Transactional
    public List<AdminQuoteSummaryResponse> list(UUID enquiryId) {
        if (!enquiryRepository.existsById(enquiryId)) throw new ResourceNotFoundException("Enquiry was not found.");
        return quoteRepository.findAllByEnquiryIdOrderByRevisionNumberDesc(enquiryId).stream()
                .map(this::expireIfNeeded)
                .map(this::summary)
                .toList();
    }

    @Transactional
    public AdminQuoteDetailResponse create(UUID enquiryId, String actorEmail) {
        var existing = quoteRepository.findFirstByEnquiryIdOrderByRevisionNumberDesc(enquiryId);
        if (existing.isPresent()) return detail(expireIfNeeded(existing.get()));

        var enquiry = enquiryRepository.findById(enquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Enquiry was not found."));
        var actor = actor(actorEmail);
        var quote = new Quote(enquiry, actor, quoteNumber(), 1);
        quote.setCustomerName((enquiry.getFirstName() + " " + enquiry.getLastName()).trim());
        quote.setCustomerEmail(enquiry.getEmail());
        quote.setPropertyAddress(enquiry.getPropertyAddress());
        quote.setTitle((enquiry.getService() == null ? "Painting" : enquiry.getService().getTitle()) + " proposal");
        quote.setScope(enquiry.getMessage());
        quote.setTerms(DEFAULT_TERMS);
        quote.setValidUntil(LocalDate.now(BUSINESS_TIME_ZONE).plusDays(30));
        quote.setGstRate(GST_RATE);
        quote.touchContent(OffsetDateTime.now(ZoneOffset.UTC));
        quoteRepository.saveAndFlush(quote);

        itemRepository.save(new QuoteItem(
                quote, QuoteItemCategory.LABOUR, "Painting services described in the scope above",
                BigDecimal.ONE, "project", BigDecimal.ZERO, false, 0
        ));
        activityRepository.save(new QuoteActivity(quote, actor, QuoteActivityType.CREATED, "Draft quote created."));
        return detail(quote);
    }

    @Transactional(readOnly = true)
    public AdminQuoteDetailResponse find(UUID quoteId) {
        return detail(quote(quoteId));
    }

    @Transactional
    public AdminQuoteDetailResponse update(UUID quoteId, SaveQuoteRequest request, String actorEmail) {
        var quote = quote(quoteId);
        requireDraft(quote);
        if (quote.getVersion() != request.version()) {
            throw new WorkflowConflictException("This quote was updated by another staff member. Reload it before saving again.");
        }
        validateDates(request);
        var actor = actor(actorEmail);

        quote.setCustomerName(request.customerName().trim());
        quote.setCustomerEmail(request.customerEmail().trim().toLowerCase(Locale.ROOT));
        quote.setPropertyAddress(normalize(request.propertyAddress()));
        quote.setTitle(request.title().trim());
        quote.setScope(request.scope().trim());
        quote.setTerms(request.terms().trim());
        quote.setValidUntil(request.validUntil());
        quote.setEstimatedStartDate(request.estimatedStartDate());
        quote.setEstimatedEndDate(request.estimatedEndDate());
        quote.setUpdatedBy(actor);
        quote.touchContent(OffsetDateTime.now(ZoneOffset.UTC));

        itemRepository.deleteAllByQuoteId(quoteId);
        itemRepository.flush();
        var items = saveItems(quote, request.items());
        applyTotals(quote, items);
        quoteRepository.flush();
        activityRepository.save(new QuoteActivity(quote, actor, QuoteActivityType.UPDATED, "Draft quote updated."));
        return detail(quote);
    }

    @Transactional
    public AdminQuoteDetailResponse createRevision(UUID quoteId, String actorEmail) {
        var previous = expireIfNeeded(quote(quoteId));
        if (previous.getStatus() == QuoteStatus.DRAFT) return detail(previous);
        if (previous.getStatus() == QuoteStatus.ACCEPTED || previous.getStatus() == QuoteStatus.SUPERSEDED) {
            throw new WorkflowConflictException("An accepted or superseded quote cannot be revised.");
        }
        var latest = quoteRepository.findFirstByEnquiryIdOrderByRevisionNumberDesc(previous.getEnquiry().getId()).orElseThrow();
        if (!latest.getId().equals(previous.getId())) {
            throw new WorkflowConflictException("A newer quote revision already exists.");
        }
        var actor = actor(actorEmail);
        previous.setStatus(QuoteStatus.SUPERSEDED);
        previous.setUpdatedBy(actor);
        previous.getEnquiry().setStatus(EnquiryStatus.IN_REVIEW);

        var revisionNumber = previous.getRevisionNumber() + 1;
        var baseNumber = previous.getQuoteNumber().replaceFirst("-R\\d+$", "");
        var revision = new Quote(previous.getEnquiry(), actor, baseNumber + "-R" + revisionNumber, revisionNumber);
        copy(previous, revision);
        revision.setStatus(QuoteStatus.DRAFT);
        revision.touchContent(OffsetDateTime.now(ZoneOffset.UTC));
        quoteRepository.saveAndFlush(revision);

        var previousItems = itemRepository.findAllByQuoteIdOrderByDisplayOrderAsc(previous.getId());
        for (var item : previousItems) {
            itemRepository.save(new QuoteItem(
                    revision, item.getCategory(), item.getDescription(), item.getQuantity(), item.getUnit(),
                    item.getUnitPrice(), item.isOptional(), item.getDisplayOrder()
            ));
        }
        activityRepository.save(new QuoteActivity(
                revision, actor, QuoteActivityType.REVISION_CREATED,
                "Revision " + revisionNumber + " created from " + previous.getQuoteNumber() + "."
        ));
        return detail(revision);
    }

    @Transactional
    public AdminQuoteDetailResponse send(UUID quoteId, String actorEmail) {
        var quote = quote(quoteId);
        requireDraft(quote);
        if (quote.getTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new QuoteValidationException("Add at least one priced, included item before sending the quote.");
        }
        if (quote.getValidUntil().isBefore(LocalDate.now(BUSINESS_TIME_ZONE))) {
            throw new QuoteValidationException("Choose a valid-until date that is today or later.");
        }

        var items = itemRepository.findAllByQuoteIdOrderByDisplayOrderAsc(quoteId);
        var pdf = pdfService.generate(quote, items);
        var responseToken = UUID.randomUUID() + "." + UUID.randomUUID();
        notifier.sendQuote(quote, pdf, responseToken);

        var now = OffsetDateTime.now(ZoneOffset.UTC);
        quote.setResponseTokenHash(hash(responseToken));
        quote.setResponseTokenExpiresAt(now.plusDays(90));
        quote.setSentAt(now);
        quote.setStatus(QuoteStatus.SENT);
        var actor = actor(actorEmail);
        quote.setUpdatedBy(actor);
        quote.getEnquiry().setStatus(EnquiryStatus.QUOTED);
        activityRepository.save(new QuoteActivity(
                quote, actor, QuoteActivityType.SENT, "Quote emailed to " + quote.getCustomerEmail() + "."
        ));
        quoteRepository.flush();
        return detail(quote);
    }

    @Transactional(readOnly = true)
    public byte[] adminPdf(UUID quoteId) {
        var quote = quote(quoteId);
        return pdfService.generate(quote, itemRepository.findAllByQuoteIdOrderByDisplayOrderAsc(quoteId));
    }

    @Transactional
    public PublicQuoteResponse publicFind(String token) {
        return publicResponse(publicQuote(token));
    }

    @Transactional
    public byte[] publicPdf(String token) {
        var quote = publicQuote(token);
        return pdfService.generate(quote, itemRepository.findAllByQuoteIdOrderByDisplayOrderAsc(quote.getId()));
    }

    @Transactional
    public PublicQuoteResponse respond(String token, QuoteDecisionRequest request) {
        var quote = publicQuote(token);
        if (quote.getStatus() == QuoteStatus.ACCEPTED || quote.getStatus() == QuoteStatus.DECLINED) {
            var sameDecision = request.decision() == QuoteDecision.ACCEPT && quote.getStatus() == QuoteStatus.ACCEPTED
                    || request.decision() == QuoteDecision.DECLINE && quote.getStatus() == QuoteStatus.DECLINED;
            if (sameDecision) return publicResponse(quote);
            throw new WorkflowConflictException("A response has already been recorded for this quote.");
        }
        if (quote.getStatus() != QuoteStatus.SENT) {
            throw new WorkflowConflictException("This quote is no longer open for a response.");
        }

        var now = OffsetDateTime.now(ZoneOffset.UTC);
        if (request.decision() == QuoteDecision.ACCEPT) {
            quote.setStatus(QuoteStatus.ACCEPTED);
            quote.setAcceptedAt(now);
            quote.getEnquiry().setStatus(EnquiryStatus.WON);
            activityRepository.save(new QuoteActivity(
                    quote, null, QuoteActivityType.ACCEPTED, "Customer accepted the quote."
            ));
        } else {
            quote.setStatus(QuoteStatus.DECLINED);
            quote.setDeclinedAt(now);
            quote.setDeclineReason(normalize(request.reason()));
            quote.getEnquiry().setStatus(EnquiryStatus.LOST);
            activityRepository.save(new QuoteActivity(
                    quote, null, QuoteActivityType.DECLINED,
                    quote.getDeclineReason() == null ? "Customer declined the quote." : "Customer declined the quote and left feedback."
            ));
        }
        quoteRepository.flush();
        return publicResponse(quote);
    }

    private List<QuoteItem> saveItems(Quote quote, List<QuoteItemRequest> requests) {
        for (var request : requests) {
            if (request.description().isBlank() || request.unit().isBlank()) {
                throw new QuoteValidationException("Every quote item needs a description and unit.");
            }
        }
        var order = new int[]{0};
        return requests.stream().map(request -> itemRepository.save(new QuoteItem(
                quote,
                request.category(),
                request.description().trim(),
                request.quantity().setScale(2, RoundingMode.HALF_UP),
                request.unit().trim(),
                request.unitPrice().setScale(2, RoundingMode.HALF_UP),
                request.optional() || request.category() == QuoteItemCategory.OPTIONAL,
                order[0]++
        ))).toList();
    }

    private void applyTotals(Quote quote, List<QuoteItem> items) {
        var subtotal = items.stream().filter(item -> !item.isOptional())
                .map(QuoteItem::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        var optional = items.stream().filter(QuoteItem::isOptional)
                .map(QuoteItem::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        var gst = subtotal.multiply(GST_RATE).setScale(2, RoundingMode.HALF_UP);
        quote.setSubtotal(subtotal);
        quote.setGstAmount(gst);
        quote.setTotal(subtotal.add(gst).setScale(2, RoundingMode.HALF_UP));
        quote.setOptionalTotal(optional);
    }

    private void validateDates(SaveQuoteRequest request) {
        if (request.estimatedStartDate() != null && request.estimatedEndDate() != null
                && request.estimatedEndDate().isBefore(request.estimatedStartDate())) {
            throw new QuoteValidationException("Estimated completion cannot be before the estimated start date.");
        }
    }

    private void copy(Quote source, Quote target) {
        target.setCustomerName(source.getCustomerName());
        target.setCustomerEmail(source.getCustomerEmail());
        target.setPropertyAddress(source.getPropertyAddress());
        target.setTitle(source.getTitle());
        target.setScope(source.getScope());
        target.setTerms(source.getTerms());
        target.setGstRate(source.getGstRate());
        target.setSubtotal(source.getSubtotal());
        target.setGstAmount(source.getGstAmount());
        target.setTotal(source.getTotal());
        target.setOptionalTotal(source.getOptionalTotal());
        target.setValidUntil(LocalDate.now(BUSINESS_TIME_ZONE).plusDays(30));
        target.setEstimatedStartDate(source.getEstimatedStartDate());
        target.setEstimatedEndDate(source.getEstimatedEndDate());
    }

    private AdminUser actor(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Staff account is no longer available."));
    }

    private Quote quote(UUID id) {
        return quoteRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Quote was not found."));
    }

    private void requireDraft(Quote quote) {
        if (quote.getStatus() != QuoteStatus.DRAFT) {
            throw new WorkflowConflictException("Sent quotes are locked. Create a new revision to make changes.");
        }
    }

    private Quote expireIfNeeded(Quote quote) {
        if (quote.getStatus() == QuoteStatus.SENT
                && quote.getValidUntil().isBefore(LocalDate.now(BUSINESS_TIME_ZONE))) {
            quote.setStatus(QuoteStatus.EXPIRED);
        }
        return quote;
    }

    private Quote publicQuote(String token) {
        if (token == null || token.isBlank()) {
            throw new ResourceNotFoundException("This quote link is invalid or has expired.");
        }
        var quote = quoteRepository.findByResponseTokenHash(hash(token))
                .orElseThrow(() -> new ResourceNotFoundException("This quote link is invalid or has expired."));
        if (quote.getResponseTokenExpiresAt() == null
                || quote.getResponseTokenExpiresAt().isBefore(OffsetDateTime.now(ZoneOffset.UTC))) {
            throw new ResourceNotFoundException("This quote link is invalid or has expired.");
        }
        return expireIfNeeded(quote);
    }

    private PublicQuoteResponse publicResponse(Quote quote) {
        var items = itemRepository.findAllByQuoteIdOrderByDisplayOrderAsc(quote.getId()).stream()
                .map(item -> new QuoteItemResponse(
                        item.getId(), item.getCategory(), item.getDescription(), item.getQuantity(), item.getUnit(),
                        item.getUnitPrice(), item.getLineTotal(), item.isOptional()
                )).toList();
        return new PublicQuoteResponse(
                quote.getQuoteNumber(), quote.getRevisionNumber(), quote.getStatus(), quote.getCustomerName(),
                quote.getPropertyAddress(), quote.getTitle(), quote.getScope(), quote.getTerms(), quote.getSubtotal(),
                quote.getGstAmount(), quote.getTotal(), quote.getOptionalTotal(), quote.getValidUntil(),
                quote.getEstimatedStartDate(), quote.getEstimatedEndDate(), items
        );
    }

    private AdminQuoteSummaryResponse summary(Quote quote) {
        return new AdminQuoteSummaryResponse(
                quote.getId(), quote.getQuoteNumber(), quote.getRevisionNumber(), quote.getStatus(),
                quote.getTotal(), quote.getValidUntil(), quote.getUpdatedAt(), quote.getSentAt()
        );
    }

    private AdminQuoteDetailResponse detail(Quote quote) {
        var items = itemRepository.findAllByQuoteIdOrderByDisplayOrderAsc(quote.getId()).stream()
                .map(item -> new QuoteItemResponse(
                        item.getId(), item.getCategory(), item.getDescription(), item.getQuantity(), item.getUnit(),
                        item.getUnitPrice(), item.getLineTotal(), item.isOptional()
                )).toList();
        var activities = activityRepository.findAllByQuoteIdOrderByCreatedAtDesc(quote.getId()).stream()
                .map(activity -> new QuoteActivityResponse(
                        activity.getId(), activity.getType(), activity.getSummary(),
                        activity.getActor() == null ? "Customer" : activity.getActor().getDisplayName(),
                        activity.getCreatedAt()
                )).toList();
        return new AdminQuoteDetailResponse(
                quote.getId(), quote.getEnquiry().getId(), quote.getQuoteNumber(), quote.getRevisionNumber(),
                quote.getVersion(), quote.getStatus(), quote.getCustomerName(), quote.getCustomerEmail(),
                quote.getPropertyAddress(), quote.getTitle(), quote.getScope(), quote.getTerms(), quote.getGstRate(),
                quote.getSubtotal(), quote.getGstAmount(), quote.getTotal(), quote.getOptionalTotal(),
                quote.getValidUntil(), quote.getEstimatedStartDate(), quote.getEstimatedEndDate(), quote.getSentAt(),
                quote.getAcceptedAt(), quote.getDeclinedAt(), quote.getDeclineReason(), quote.getCreatedAt(),
                quote.getUpdatedAt(), items, activities
        );
    }

    private String quoteNumber() {
        return "GS-" + LocalDate.now(BUSINESS_TIME_ZONE).getYear() + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase(Locale.ROOT);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    static String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable.", exception);
        }
    }
}
