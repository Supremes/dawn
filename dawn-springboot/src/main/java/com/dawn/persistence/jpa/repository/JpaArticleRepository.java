package com.dawn.persistence.jpa.repository;

import com.dawn.persistence.jpa.domain.JpaArticle;
import com.dawn.persistence.jpa.domain.JpaArticleStatus;
import com.dawn.persistence.jpa.projection.JpaArticleSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaArticleRepository extends JpaRepository<JpaArticle, UUID>,
        JpaSpecificationExecutor<JpaArticle> {

    Optional<JpaArticle> findFirstByTitleIgnoreCase(String title);

    Page<JpaArticle> findByStatusAndTitleContainingIgnoreCase(
            JpaArticleStatus status,
            String title,
            Pageable pageable);

    @Query("""
            select new com.dawn.persistence.jpa.projection.JpaArticleSummary(
                article.id, article.title, article.author.name, article.status)
            from JpaFeatureArticle article
            where article.status = :status
            """)
    Page<JpaArticleSummary> findSummariesByStatus(
            @Param("status") JpaArticleStatus status,
            Pageable pageable);

    @EntityGraph(attributePaths = "author")
    @Query("""
            select article
            from JpaFeatureArticle article
            where article.status = :status
            order by article.createdAt desc
            """)
    List<JpaArticle> findWithAuthorByStatus(@Param("status") JpaArticleStatus status);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update JpaFeatureArticle article
            set article.status = :targetStatus,
                article.version = article.version + 1,
                article.updatedAt = current_timestamp
            where article.status = :sourceStatus
            """)
    int bulkTransitionStatus(
            @Param("sourceStatus") JpaArticleStatus sourceStatus,
            @Param("targetStatus") JpaArticleStatus targetStatus);
}
