package com.dawn.persistence;

import com.dawn.persistence.jpa.domain.JpaArticle;
import com.dawn.persistence.jpa.domain.JpaArticleStatus;
import com.dawn.persistence.jpa.domain.JpaAuthor;
import com.dawn.persistence.jpa.projection.JpaArticleSummary;
import com.dawn.persistence.jpa.repository.JpaArticleRepository;
import com.dawn.persistence.jpa.repository.JpaAuthorRepository;
import com.dawn.persistence.jpa.service.JpaArticleApplicationService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.RollbackException;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = PersistenceJpaTestConfig.class)
@ActiveProfiles("persistence-test")
class SpringDataJpaFeaturesTest {

    @Autowired
    private JpaArticleApplicationService articleService;

    @Autowired
    private JpaArticleRepository articleRepository;

    @Autowired
    private JpaAuthorRepository authorRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void cleanDatabase() {
        articleRepository.deleteAllInBatch();
        authorRepository.deleteAllInBatch();
        entityManagerFactory.unwrap(SessionFactory.class).getCache().evictAllRegions();
    }

    @Test
    void supportsDerivedQueriesAndDtoProjection() {
        UUID publishedId = createPublishedArticle("Spring Data 查询", "派生查询正文");
        createDraftArticle("尚未发布", "草稿正文");

        Page<JpaArticle> result = articleService.findByDerivedQuery(
                JpaArticleStatus.PUBLISHED,
                "spring data",
                PageRequest.of(0, 10));
        Page<JpaArticleSummary> summaries = articleService.findSummaries(
                JpaArticleStatus.PUBLISHED,
                PageRequest.of(0, 10));

        assertThat(result).extracting(JpaArticle::getId).containsExactly(publishedId);
        assertThat(summaries).singleElement().satisfies(summary -> {
            assertThat(summary.id()).isEqualTo(publishedId);
            assertThat(summary.title()).isEqualTo("Spring Data 查询");
            assertThat(summary.authorName()).startsWith("author-");
            assertThat(summary.status()).isEqualTo(JpaArticleStatus.PUBLISHED);
        });
    }

    @Test
    void composesSpecificationsWithOptionalFilters() {
        createPublishedArticle("Specification 深入", "正文");
        createPublishedArticle("普通文章", "正文");

        Page<JpaArticle> result = articleService.findBySpecification(
                JpaArticleStatus.PUBLISHED,
                "specification",
                LocalDateTime.now().minusMinutes(1),
                LocalDateTime.now().plusMinutes(1),
                PageRequest.of(0, 10));

        assertThat(result).extracting(JpaArticle::getTitle)
                .containsExactly("Specification 深入");
    }

    @Test
    void entityGraphLoadsAuthorsWithoutNPlusOneQueries() {
        for (int index = 0; index < 3; index++) {
            createPublishedArticle("EntityGraph " + index, "正文");
        }
        entityManager.clear();
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        List<JpaArticle> articles = articleService.findWithAuthors(JpaArticleStatus.PUBLISHED);
        assertThat(articles).extracting(article -> article.getAuthor().getName()).hasSize(3);
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
    }

    @Test
    void auditingPopulatesTimestampsAndUpdatesVersion() {
        UUID articleId = createDraftArticle("审计字段", "正文");
        JpaArticle initial = articleRepository.findById(articleId).orElseThrow();
        long initialVersion = initial.getVersion();

        transactionTemplate.executeWithoutResult(status -> {
            JpaArticle article = articleRepository.findById(articleId).orElseThrow();
            article.rename("审计字段已更新");
        });

        JpaArticle updated = articleRepository.findById(articleId).orElseThrow();
        assertThat(initial.getCreatedAt()).isNotNull();
        assertThat(initial.getUpdatedAt()).isNotNull();
        assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(initial.getUpdatedAt());
        assertThat(updated.getVersion()).isGreaterThan(initialVersion);
    }

    @Test
    void optimisticLockRejectsStaleJpaUpdates() {
        UUID articleId = createDraftArticle("乐观锁", "正文");
        EntityManager first = entityManagerFactory.createEntityManager();
        EntityManager second = entityManagerFactory.createEntityManager();
        try {
            first.getTransaction().begin();
            second.getTransaction().begin();
            JpaArticle current = first.find(JpaArticle.class, articleId);
            JpaArticle stale = second.find(JpaArticle.class, articleId);
            current.rename("第一次更新");
            first.getTransaction().commit();
            stale.rename("过期更新");

            assertThatThrownBy(() -> second.getTransaction().commit())
                    .isInstanceOfAny(RollbackException.class, OptimisticLockException.class);
        } finally {
            if (first.getTransaction().isActive()) {
                first.getTransaction().rollback();
            }
            if (second.getTransaction().isActive()) {
                second.getTransaction().rollback();
            }
            first.close();
            second.close();
        }
    }

    @Test
    void bulkUpdateTransitionsStatusAndIncrementsVersion() {
        UUID firstId = createPublishedArticle("批量更新一", "正文");
        UUID secondId = createPublishedArticle("批量更新二", "正文");
        long firstVersion = articleRepository.findById(firstId).orElseThrow().getVersion();

        int updated = articleService.archiveAllPublished();

        assertThat(updated).isEqualTo(2);
        assertThat(articleRepository.findAllById(List.of(firstId, secondId)))
                .allSatisfy(article -> assertThat(article.getStatus()).isEqualTo(JpaArticleStatus.ARCHIVED));
        assertThat(articleRepository.findById(firstId).orElseThrow().getVersion())
                .isEqualTo(firstVersion + 1);
    }

    private UUID createPublishedArticle(String title, String content) {
        UUID articleId = createDraftArticle(title, content);
        articleService.publish(articleId);
        return articleId;
    }

    private UUID createDraftArticle(String title, String content) {
        Long authorId = articleService.createAuthor("author-" + UUID.randomUUID());
        return articleService.createDraft(authorId, title, content);
    }
}

