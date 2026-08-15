package com.dawn.persistence.jpa.specification;

import com.dawn.persistence.jpa.domain.JpaArticle;
import com.dawn.persistence.jpa.domain.JpaArticleStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public final class JpaArticleSpecifications {

    private JpaArticleSpecifications() {
    }

    public static Specification<JpaArticle> hasStatus(JpaArticleStatus status) {
        return (root, query, criteriaBuilder) -> status == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<JpaArticle> titleContains(String keyword) {
        return (root, query, criteriaBuilder) -> keyword == null || keyword.isBlank()
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        "%" + keyword.toLowerCase() + "%");
    }

    public static Specification<JpaArticle> publishedBetween(
            LocalDateTime start,
            LocalDateTime end) {
        return (root, query, criteriaBuilder) -> {
            if (start == null && end == null) {
                return criteriaBuilder.conjunction();
            }
            if (start == null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("publishedAt"), end);
            }
            if (end == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("publishedAt"), start);
            }
            return criteriaBuilder.between(root.get("publishedAt"), start, end);
        };
    }
}
