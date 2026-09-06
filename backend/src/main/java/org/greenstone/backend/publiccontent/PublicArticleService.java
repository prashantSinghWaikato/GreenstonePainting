package org.greenstone.backend.publiccontent;

import org.greenstone.backend.enquiry.AttachmentDownload;
import org.greenstone.backend.persistence.entity.BlogArticle;
import org.greenstone.backend.persistence.entity.PublicationStatus;
import org.greenstone.backend.persistence.repository.BlogArticleRepository;
import org.greenstone.backend.storage.FileStorageService;
import org.greenstone.backend.web.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PublicArticleService {

    private final BlogArticleRepository articleRepository;
    private final FileStorageService storageService;

    public PublicArticleService(BlogArticleRepository articleRepository, FileStorageService storageService) {
        this.articleRepository = articleRepository;
        this.storageService = storageService;
    }

    @Transactional(readOnly = true)
    public List<PublicArticleResponse> articles() {
        return articleRepository.findAllByStatusOrderByPublishedAtDesc(PublicationStatus.PUBLISHED).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PublicArticleResponse article(String slug) {
        return toResponse(findPublished(slug));
    }

    @Transactional(readOnly = true)
    public AttachmentDownload image(String slug) {
        var article = findPublished(slug);
        var key = article.getFeaturedImageObjectKey();
        if (key == null || key.startsWith("static:")) {
            throw new ResourceNotFoundException("This image is served by the website.");
        }
        return new AttachmentDownload(
                storageService.load(key), article.getFeaturedImageFilename(),
                article.getFeaturedImageContentType(), article.getFeaturedImageSizeBytes()
        );
    }

    private BlogArticle findPublished(String slug) {
        return articleRepository.findBySlugAndStatus(slug, PublicationStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Article was not found."));
    }

    private PublicArticleResponse toResponse(BlogArticle article) {
        var imageUrl = article.getFeaturedImageObjectKey() == null ? null
                : article.getFeaturedImageObjectKey().startsWith("static:")
                ? article.getFeaturedImageObjectKey().substring("static:".length())
                : "/api/articles/" + article.getSlug() + "/image";
        return new PublicArticleResponse(
                article.getSlug(), "/" + article.getSlug() + "/", article.getTitle(), article.getShortTitle(),
                article.getTopic(), article.getExcerpt(), article.getBody(), article.getReadTimeMinutes(),
                imageUrl, article.getFeaturedImageAlt(), article.getPublishedAt()
        );
    }
}
