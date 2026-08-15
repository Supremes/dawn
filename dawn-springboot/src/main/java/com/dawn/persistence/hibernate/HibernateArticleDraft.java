package com.dawn.persistence.hibernate;

public record HibernateArticleDraft(String title, String content) {

    public HibernateArticleDraft {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("文章标题不能为空");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("文章内容不能为空");
        }
    }
}
