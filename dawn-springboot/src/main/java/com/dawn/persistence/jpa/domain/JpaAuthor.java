package com.dawn.persistence.jpa.domain;

import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity(name = "JpaFeatureAuthor")
@Table(name = "t_jpa_feature_author")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class JpaAuthor extends JpaAuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 20)
    private List<JpaArticle> articles = new ArrayList<>();

    protected JpaAuthor() {
    }

    public JpaAuthor(String name) {
        this.name = requireText(name, "作者名称不能为空");
    }

    public JpaArticle addDraft(String title, String content) {
        JpaArticle article = JpaArticle.draft(this, title, content);
        articles.add(article);
        return article;
    }

    public void rename(String name) {
        this.name = requireText(name, "作者名称不能为空");
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<JpaArticle> getArticles() {
        return Collections.unmodifiableList(articles);
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}
