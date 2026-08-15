package com.dawn.persistence.hibernate;

import com.dawn.persistence.jpa.domain.JpaArticleStatus;

import java.util.UUID;

public record HibernateArticleSnapshot(
        UUID id,
        String title,
        String authorName,
        JpaArticleStatus status) {
}
