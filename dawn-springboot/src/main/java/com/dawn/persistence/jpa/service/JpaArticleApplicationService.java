package com.dawn.persistence.jpa.service;

import com.dawn.persistence.jpa.domain.JpaArticle;
import com.dawn.persistence.jpa.domain.JpaArticleStatus;
import com.dawn.persistence.jpa.domain.JpaAuthor;
import com.dawn.persistence.jpa.projection.JpaArticleSummary;
import com.dawn.persistence.jpa.repository.JpaArticleRepository;
import com.dawn.persistence.jpa.repository.JpaAuthorRepository;
import com.dawn.persistence.jpa.specification.JpaArticleSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class JpaArticleApplicationService {

    private final JpaAuthorRepository authorRepository;
    private final JpaArticleRepository articleRepository;

    public JpaArticleApplicationService(
            JpaAuthorRepository authorRepository,
            JpaArticleRepository articleRepository) {
        this.authorRepository = authorRepository;
        this.articleRepository = articleRepository;
    }

    @Transactional
    public Long createAuthor(String name) {
        return authorRepository.save(new JpaAuthor(name)).getId();
    }

    @Transactional
    public UUID createDraft(Long authorId, String title, String content) {
        JpaAuthor author = authorRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("作者不存在: " + authorId));
        return articleRepository.save(author.addDraft(title, content)).getId();
    }

    @Transactional
    public void publish(UUID articleId) {
        JpaArticle article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("文章不存在: " + articleId));
        article.publish();
    }

    public Page<JpaArticle> findByDerivedQuery(
            JpaArticleStatus status,
            String title,
            Pageable pageable) {
        return articleRepository.findByStatusAndTitleContainingIgnoreCase(status, title, pageable);
    }

    public Page<JpaArticleSummary> findSummaries(
            JpaArticleStatus status,
            Pageable pageable) {
        return articleRepository.findSummariesByStatus(status, pageable);
    }

    public Page<JpaArticle> findBySpecification(
            JpaArticleStatus status,
            String title,
            LocalDateTime publishedFrom,
            LocalDateTime publishedTo,
            Pageable pageable) {
        return articleRepository.findAll(
                JpaArticleSpecifications.hasStatus(status)
                        .and(JpaArticleSpecifications.titleContains(title))
                        .and(JpaArticleSpecifications.publishedBetween(publishedFrom, publishedTo)),
                pageable);
    }

    public List<JpaArticle> findWithAuthors(JpaArticleStatus status) {
        return articleRepository.findWithAuthorByStatus(status);
    }

    @Transactional
    public int archiveAllPublished() {
        return articleRepository.bulkTransitionStatus(
                JpaArticleStatus.PUBLISHED,
                JpaArticleStatus.ARCHIVED);
    }
}
