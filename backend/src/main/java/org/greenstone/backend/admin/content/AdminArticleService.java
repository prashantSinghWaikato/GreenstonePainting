package org.greenstone.backend.admin.content;

import org.greenstone.backend.enquiry.AttachmentDownload;
import org.greenstone.backend.persistence.entity.AdminUser;
import org.greenstone.backend.persistence.entity.BlogArticle;
import org.greenstone.backend.persistence.entity.BlogArticleActivity;
import org.greenstone.backend.persistence.entity.BlogArticleActivityType;
import org.greenstone.backend.persistence.entity.PublicationStatus;
import org.greenstone.backend.persistence.repository.AdminUserRepository;
import org.greenstone.backend.persistence.repository.BlogArticleActivityRepository;
import org.greenstone.backend.persistence.repository.BlogArticleRepository;
import org.greenstone.backend.storage.FileStorageService;
import org.greenstone.backend.web.ResourceNotFoundException;
import org.greenstone.backend.web.UploadValidationException;
import org.greenstone.backend.web.WorkflowConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.UUID;

@Service
public class AdminArticleService {

    private final BlogArticleRepository articleRepository;
    private final BlogArticleActivityRepository activityRepository;
    private final AdminUserRepository userRepository;
    private final FileStorageService storageService;

    public AdminArticleService(
            BlogArticleRepository articleRepository,
            BlogArticleActivityRepository activityRepository,
            AdminUserRepository userRepository,
            FileStorageService storageService
    ) {
        this.articleRepository = articleRepository;
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
        this.storageService = storageService;
    }

    @Transactional(readOnly = true)
    public java.util.List<AdminArticleSummaryResponse> list() {
        return articleRepository.findAllByOrderByUpdatedAtDesc().stream().map(this::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public AdminArticleDetailResponse detail(UUID articleId) {
        return toDetail(findArticle(articleId));
    }

    @Transactional
    public AdminArticleDetailResponse create(SaveAdminArticleRequest request, String actorEmail) {
        var actor = findActor(actorEmail);
        var article = new BlogArticle(
                uniqueSlug(request.title()), clean(request.title()), clean(request.shortTitle()), clean(request.topic()),
                clean(request.excerpt()), clean(request.body()), request.readTimeMinutes()
        );
        article = articleRepository.saveAndFlush(article);
        activityRepository.save(new BlogArticleActivity(
                article, actor, BlogArticleActivityType.CREATED,
                actor.getDisplayName() + " created the draft article “" + article.getTitle() + "”."
        ));
        return toDetail(article);
    }

    @Transactional
    public AdminArticleDetailResponse update(UUID articleId, SaveAdminArticleRequest request, String actorEmail) {
        var article = findArticle(articleId);
        verifyVersion(article, request.version());
        requireDraft(article, "Unpublish this article before editing its public content.");
        article.setTitle(clean(request.title()));
        article.setShortTitle(clean(request.shortTitle()));
        article.setTopic(clean(request.topic()));
        article.setExcerpt(clean(request.excerpt()));
        article.setBody(clean(request.body()));
        article.setReadTimeMinutes(request.readTimeMinutes());
        articleRepository.flush();
        var actor = findActor(actorEmail);
        activityRepository.save(new BlogArticleActivity(
                article, actor, BlogArticleActivityType.UPDATED,
                actor.getDisplayName() + " updated the article content."
        ));
        return toDetail(article);
    }

    @Transactional
    public AdminArticleDetailResponse setPublication(
            UUID articleId,
            SetProjectPublicationRequest request,
            String actorEmail
    ) {
        if (request.status() != PublicationStatus.DRAFT && request.status() != PublicationStatus.PUBLISHED) {
            throw new WorkflowConflictException("Articles can only be moved between Draft and Published here.");
        }
        var article = findArticle(articleId);
        verifyVersion(article, request.version());
        if (article.getStatus() == request.status()) return toDetail(article);
        if (request.status() == PublicationStatus.PUBLISHED && article.getFeaturedImageObjectKey() == null) {
            throw new WorkflowConflictException("Add a featured image before publishing this article.");
        }
        var actor = findActor(actorEmail);
        var published = request.status() == PublicationStatus.PUBLISHED;
        article.setStatus(request.status());
        article.setPublishedAt(published ? OffsetDateTime.now(ZoneOffset.UTC) : null);
        articleRepository.flush();
        activityRepository.save(new BlogArticleActivity(
                article, actor,
                published ? BlogArticleActivityType.PUBLISHED : BlogArticleActivityType.UNPUBLISHED,
                actor.getDisplayName() + (published ? " published the article." : " returned the article to Draft.")
        ));
        return toDetail(article);
    }

    @Transactional
    public AdminArticleDetailResponse setImage(UUID articleId, MultipartFile file, String altText, String actorEmail) {
        var article = findArticle(articleId);
        requireDraft(article, "Unpublish this article before changing its featured image.");
        validateAlt(altText);
        var stored = storageService.storeBlogImage(articleId, file);
        var oldObjectKey = article.getFeaturedImageObjectKey();
        try {
            var filename = safeFilename(file.getOriginalFilename());
            article.setFeaturedImage(stored.objectKey(), altText.trim(), filename, stored.contentType(), stored.sizeBytes());
            articleRepository.flush();
            var actor = findActor(actorEmail);
            activityRepository.save(new BlogArticleActivity(
                    article, actor, BlogArticleActivityType.IMAGE_CHANGED,
                    actor.getDisplayName() + " updated the featured image."
            ));
            if (oldObjectKey != null && !oldObjectKey.startsWith("static:")) storageService.delete(oldObjectKey);
            return toDetail(article);
        } catch (RuntimeException exception) {
            storageService.delete(stored.objectKey());
            throw exception;
        }
    }

    @Transactional
    public AdminArticleDetailResponse removeImage(UUID articleId, String actorEmail) {
        var article = findArticle(articleId);
        requireDraft(article, "Unpublish this article before changing its featured image.");
        if (article.getFeaturedImageObjectKey() == null) return toDetail(article);
        var objectKey = article.getFeaturedImageObjectKey();
        article.clearFeaturedImage();
        articleRepository.flush();
        if (!objectKey.startsWith("static:")) storageService.delete(objectKey);
        var actor = findActor(actorEmail);
        activityRepository.save(new BlogArticleActivity(
                article, actor, BlogArticleActivityType.IMAGE_REMOVED,
                actor.getDisplayName() + " removed the featured image."
        ));
        return toDetail(article);
    }

    @Transactional(readOnly = true)
    public AttachmentDownload loadImage(UUID articleId) {
        var article = findArticle(articleId);
        if (article.getFeaturedImageObjectKey() == null || article.getFeaturedImageObjectKey().startsWith("static:")) {
            throw new ResourceNotFoundException("This image is served by the website.");
        }
        return new AttachmentDownload(
                storageService.load(article.getFeaturedImageObjectKey()),
                article.getFeaturedImageFilename(), article.getFeaturedImageContentType(), article.getFeaturedImageSizeBytes()
        );
    }

    private AdminArticleSummaryResponse toSummary(BlogArticle article) {
        return new AdminArticleSummaryResponse(
                article.getId(), article.getSlug(), article.getTitle(), article.getTopic(), article.getStatus(),
                imageUrl(article, true), article.getPublishedAt(), article.getUpdatedAt(), article.getVersion()
        );
    }

    private AdminArticleDetailResponse toDetail(BlogArticle article) {
        var activities = activityRepository.findTop30ByArticleIdOrderByCreatedAtDesc(article.getId()).stream()
                .map(activity -> new AdminArticleActivityResponse(
                        activity.getId(), activity.getActivityType(), activity.getSummary(),
                        activity.getActor().getDisplayName(), activity.getCreatedAt()
                )).toList();
        return new AdminArticleDetailResponse(
                article.getId(), article.getSlug(), article.getTitle(), article.getShortTitle(), article.getTopic(),
                article.getExcerpt(), article.getBody(), article.getReadTimeMinutes(), article.getStatus(),
                imageUrl(article, true), article.getFeaturedImageAlt(), article.getFeaturedImageFilename(),
                article.getFeaturedImageSizeBytes(), article.getPublishedAt(), article.getCreatedAt(),
                article.getUpdatedAt(), article.getVersion(), activities
        );
    }

    private String imageUrl(BlogArticle article, boolean admin) {
        var key = article.getFeaturedImageObjectKey();
        if (key == null) return null;
        if (key.startsWith("static:")) return key.substring("static:".length());
        return admin
                ? "/api/admin/content/articles/" + article.getId() + "/image/file"
                : "/api/articles/" + article.getSlug() + "/image";
    }

    private BlogArticle findArticle(UUID articleId) {
        return articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article was not found."));
    }

    private AdminUser findActor(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Staff account is no longer available."));
    }

    private void verifyVersion(BlogArticle article, long version) {
        if (article.getVersion() != version) {
            throw new WorkflowConflictException("This article changed in another session. Reload it before continuing.");
        }
    }

    private void requireDraft(BlogArticle article, String message) {
        if (article.getStatus() != PublicationStatus.DRAFT) throw new WorkflowConflictException(message);
    }

    private void validateAlt(String altText) {
        if (altText == null || altText.isBlank() || altText.trim().length() > 250) {
            throw new UploadValidationException("Describe the featured image in 250 characters or fewer.");
        }
    }

    private String safeFilename(String filename) {
        var value = filename == null || filename.isBlank() ? "article-image" : filename.trim();
        return value.length() > 255 ? value.substring(value.length() - 255) : value;
    }

    private String uniqueSlug(String title) {
        var base = clean(title).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
        if (base.isBlank()) base = "article";
        if (base.length() > 150) base = base.substring(0, 150).replaceAll("-$", "");
        var slug = base;
        while (articleRepository.findBySlug(slug).isPresent()) {
            slug = base + "-" + UUID.randomUUID().toString().substring(0, 6);
        }
        return slug;
    }

    private String clean(String value) { return value == null ? "" : value.trim(); }
}
