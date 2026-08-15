package com.dawn.persistence.jpa.domain;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "JpaFeatureArticle")
@Table(name = "t_jpa_feature_article")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@DynamicUpdate
public class JpaArticle extends JpaAuditedEntity {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private JpaAuthor author;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JpaArticleStatus status;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    protected JpaArticle() {
    }

    private JpaArticle(JpaAuthor author, String title, String content) {
        this.author = author;
        this.title = requireText(title, "文章标题不能为空");
        this.content = requireText(content, "文章内容不能为空");
        this.status = JpaArticleStatus.DRAFT;
    }

    public static JpaArticle draft(JpaAuthor author, String title, String content) {
        if (author == null) {
            throw new IllegalArgumentException("文章作者不能为空");
        }
        return new JpaArticle(author, title, content);
    }

    public void publish() {
        status = JpaArticleStatus.PUBLISHED;
        publishedAt = LocalDateTime.now();
    }

    public void archive() {
        status = JpaArticleStatus.ARCHIVED;
    }

    public void rename(String title) {
        this.title = requireText(title, "文章标题不能为空");
    }

    public UUID getId() {
        return id;
    }

    public JpaAuthor getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public JpaArticleStatus getStatus() {
        return status;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}
