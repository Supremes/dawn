package com.dawn.persistence;

import com.dawn.persistence.hibernate.HibernateArticleDraft;
import com.dawn.persistence.hibernate.HibernateArticleOperations;
import com.dawn.persistence.hibernate.HibernateArticleSnapshot;
import com.dawn.persistence.jpa.domain.JpaArticleStatus;
import com.dawn.persistence.jpa.domain.JpaAuthor;
import com.dawn.persistence.jpa.repository.JpaArticleRepository;
import com.dawn.persistence.jpa.repository.JpaAuthorRepository;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = PersistenceJpaTestConfig.class)
@ActiveProfiles("persistence-test")
class HibernateFeaturesTest {

    @Autowired
    private HibernateArticleOperations operations;

    @Autowired
    private JpaArticleRepository articleRepository;

    @Autowired
    private JpaAuthorRepository authorRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @BeforeEach
    void cleanDatabase() {
        articleRepository.deleteAllInBatch();
        authorRepository.deleteAllInBatch();
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        sessionFactory.getCache().evictAllRegions();
        sessionFactory.getStatistics().clear();
    }

    @Test
    void sessionHqlStreamsRowsWithFetchSize() {
        Long authorId = createAuthor("HQL 作者");
        operations.insertDraftsInBatches(
                authorId,
                List.of(
                        new HibernateArticleDraft("HQL 一", "正文"),
                        new HibernateArticleDraft("HQL 二", "正文")),
                2);

        List<HibernateArticleSnapshot> result = operations.streamByStatus(
                JpaArticleStatus.DRAFT,
                10);

        assertThat(result).extracting(HibernateArticleSnapshot::title)
                .containsExactly("HQL 一", "HQL 二");
        assertThat(result).extracting(HibernateArticleSnapshot::authorName)
                .containsOnly("HQL 作者");
    }

    @Test
    void nativeSqlCountsRowsByStatus() {
        Long authorId = createAuthor("Native 作者");
        operations.insertDraftsInBatches(
                authorId,
                List.of(
                        new HibernateArticleDraft("Native 一", "正文"),
                        new HibernateArticleDraft("Native 二", "正文"),
                        new HibernateArticleDraft("Native 三", "正文")),
                2);

        assertThat(operations.countWithNativeSql(JpaArticleStatus.DRAFT)).isEqualTo(3);
        assertThat(operations.countWithNativeSql(JpaArticleStatus.PUBLISHED)).isZero();
    }

    @Test
    void jdbcBatchFlushAndClearPersistsEveryDraft() {
        Long authorId = createAuthor("Batch 作者");
        List<HibernateArticleDraft> drafts = java.util.stream.IntStream.rangeClosed(1, 12)
                .mapToObj(index -> new HibernateArticleDraft("Batch " + index, "正文 " + index))
                .toList();

        int inserted = operations.insertDraftsInBatches(authorId, drafts, 5);

        assertThat(inserted).isEqualTo(12);
        assertThat(articleRepository.count()).isEqualTo(12);
    }

    @Test
    void statelessSessionReadsSnapshotsWithoutManagedEntities() {
        Long authorId = createAuthor("Stateless 作者");
        operations.insertDraftsInBatches(
                authorId,
                List.of(new HibernateArticleDraft("Stateless", "正文")),
                1);

        List<HibernateArticleSnapshot> result = operations.findWithStatelessSession(
                JpaArticleStatus.DRAFT);

        assertThat(result).singleElement().satisfies(snapshot -> {
            assertThat(snapshot.title()).isEqualTo("Stateless");
            assertThat(snapshot.authorName()).isEqualTo("Stateless 作者");
        });
    }

    @Test
    void secondLevelCacheRecordsHitsAcrossSessions() {
        Long authorId = createAuthor("二级缓存作者");
        operations.insertDraftsInBatches(
                authorId,
                List.of(new HibernateArticleDraft("二级缓存文章", "正文")),
                1);
        var articleId = articleRepository.findAll().get(0).getId();
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        sessionFactory.getCache().evictAllRegions();
        Statistics statistics = sessionFactory.getStatistics();
        statistics.clear();

        loadArticleInNewEntityManager(articleId);
        loadArticleInNewEntityManager(articleId);

        assertThat(statistics.getSecondLevelCachePutCount()).isGreaterThanOrEqualTo(1);
        assertThat(statistics.getSecondLevelCacheHitCount()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void queryCacheRecordsPutAndHitStatistics() {
        Long authorId = createAuthor("查询缓存作者");
        operations.insertDraftsInBatches(
                authorId,
                List.of(new HibernateArticleDraft("查询缓存文章", "正文")),
                1);
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        assertThat(operations.findCacheableByStatus(JpaArticleStatus.DRAFT)).hasSize(1);
        assertThat(operations.findCacheableByStatus(JpaArticleStatus.DRAFT)).hasSize(1);

        assertThat(statistics.getQueryCachePutCount()).isGreaterThanOrEqualTo(1);
        assertThat(statistics.getQueryCacheHitCount()).isGreaterThanOrEqualTo(1);
    }

    private Long createAuthor(String name) {
        return authorRepository.saveAndFlush(new JpaAuthor(name)).getId();
    }

    private void loadArticleInNewEntityManager(java.util.UUID articleId) {
        var entityManager = entityManagerFactory.createEntityManager();
        try {
            entityManager.getTransaction().begin();
            assertThat(entityManager.find(com.dawn.persistence.jpa.domain.JpaArticle.class, articleId))
                    .isNotNull();
            entityManager.getTransaction().commit();
        } finally {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            entityManager.close();
        }
    }
}
