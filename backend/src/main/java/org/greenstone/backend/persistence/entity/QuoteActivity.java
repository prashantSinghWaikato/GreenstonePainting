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
@Table(name = "quote_activities")
public class QuoteActivity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quote_id", nullable = false)
    private Quote quote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_admin_id")
    private AdminUser actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 30)
    private QuoteActivityType type;

    @Column(nullable = false, length = 500)
    private String summary;

    protected QuoteActivity() {
    }

    public QuoteActivity(Quote quote, AdminUser actor, QuoteActivityType type, String summary) {
        this.quote = quote;
        this.actor = actor;
        this.type = type;
        this.summary = summary;
    }

    public AdminUser getActor() { return actor; }
    public QuoteActivityType getType() { return type; }
    public String getSummary() { return summary; }
}
