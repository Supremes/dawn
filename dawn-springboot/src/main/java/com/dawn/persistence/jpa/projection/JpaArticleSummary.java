package com.dawn.persistence.jpa.projection;

import com.dawn.persistence.jpa.domain.JpaArticleStatus;

import java.util.UUID;

public record JpaArticleSummary(
        UUID id,
        String title,
        String authorName,
        JpaArticleStatus status) {
}
