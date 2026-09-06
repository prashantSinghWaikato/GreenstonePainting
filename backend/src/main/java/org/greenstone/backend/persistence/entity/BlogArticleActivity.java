package org.greenstone.backend.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "blog_article_activities")
public class BlogArticleActivity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false)
    private BlogArticle article;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_admin_id", nullable = false)
    private AdminUser actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 40)
    private BlogArticleActivityType activityType;

    @Column(nullable = false, length = 500)
    private String summary;

    protected BlogArticleActivity() {
    }

    public BlogArticleActivity(BlogArticle article, AdminUser actor, BlogArticleActivityType activityType, String summary) {
        this.article = article;
        this.actor = actor;
        this.activityType = activityType;
        this.summary = summary;
    }

    public BlogArticleActivityType getActivityType() { return activityType; }
    public AdminUser getActor() { return actor; }
    public String getSummary() { return summary; }
}
