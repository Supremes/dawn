package com.dawn.persistence.hibernate;

import com.dawn.persistence.jpa.domain.JpaArticle;
import com.dawn.persistence.jpa.domain.JpaArticleStatus;
import com.dawn.persistence.jpa.domain.JpaAuthor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.hibernate.query.SelectionQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Repository
public class HibernateArticleOperations {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<HibernateArticleSnapshot> streamByStatus(
            JpaArticleStatus status,
            int fetchSize) {
        requirePositive(fetchSize, "fetchSize 必须大于 0");
        Session session = entityManager.unwrap(Session.class);
        SelectionQuery<JpaArticle> query = session.createSelectionQuery("""
                select article
                from JpaFeatureArticle article
                join fetch article.author
                where article.status = :status
                order by article.title
                """, JpaArticle.class);
        query.setParameter("status", status);
        query.setFetchSize(fetchSize);
        query.setReadOnly(true);
        try (Stream<JpaArticle> stream = query.getResultStream()) {
            return stream.map(this::toSnapshot).toList();
        }
    }

    @Transactional(readOnly = true)
    public long countWithNativeSql(JpaArticleStatus status) {
        Session session = entityManager.unwrap(Session.class);
        Number count = (Number) session.createNativeQuery("""
                        SELECT COUNT(*)
                        FROM t_jpa_feature_article
                        WHERE status = :status
                        """)
                .setParameter("status", status.name())
                .getSingleResult();
        return count.longValue();
    }

    @Transactional(readOnly = true)
    public List<HibernateArticleSnapshot> findCacheableByStatus(JpaArticleStatus status) {
        Session session = entityManager.unwrap(Session.class);
        SelectionQuery<JpaArticle> query = session.createSelectionQuery("""
                select article
                from JpaFeatureArticle article
                join fetch article.author
                where article.status = :status
                order by article.title
                """, JpaArticle.class);
        query.setParameter("status", status);
        query.setCacheable(true);
        query.setCacheRegion("jpa-feature-article-query");
        return query.getResultList().stream().map(this::toSnapshot).toList();
    }

    @Transactional
    public int insertDraftsInBatches(
            Long authorId,
            List<HibernateArticleDraft> drafts,
            int batchSize) {
        requirePositive(batchSize, "batchSize 必须大于 0");
        if (drafts == null || drafts.isEmpty()) {
            return 0;
        }

        Session session = entityManager.unwrap(Session.class);
        session.setJdbcBatchSize(batchSize);
        JpaAuthor author = session.getReference(JpaAuthor.class, authorId);
        for (int index = 0; index < drafts.size(); index++) {
            HibernateArticleDraft draft = drafts.get(index);
            session.persist(JpaArticle.draft(author, draft.title(), draft.content()));
            if ((index + 1) % batchSize == 0) {
                session.flush();
                session.clear();
                if (index + 1 < drafts.size()) {
                    author = session.getReference(JpaAuthor.class, authorId);
                }
            }
        }
        session.flush();
        session.clear();
        return drafts.size();
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<HibernateArticleSnapshot> findWithStatelessSession(JpaArticleStatus status) {
        SessionFactory sessionFactory = entityManager.getEntityManagerFactory()
                .unwrap(SessionFactory.class);
        try (StatelessSession session = sessionFactory.openStatelessSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                List<Object[]> rows = session.createQuery("""
                                select article.id, article.title, article.author.name, article.status
                                from JpaFeatureArticle article
                                where article.status = :status
                                order by article.title
                                """, Object[].class)
                        .setParameter("status", status)
                        .getResultList();
                List<HibernateArticleSnapshot> snapshots = new ArrayList<>(rows.size());
                for (Object[] row : rows) {
                    snapshots.add(new HibernateArticleSnapshot(
                            (java.util.UUID) row[0],
                            (String) row[1],
                            (String) row[2],
                            (JpaArticleStatus) row[3]));
                }
                transaction.commit();
                return snapshots;
            } catch (RuntimeException | Error exception) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw exception;
            }
        }
    }

    private HibernateArticleSnapshot toSnapshot(JpaArticle article) {
        return new HibernateArticleSnapshot(
                article.getId(),
                article.getTitle(),
                article.getAuthor().getName(),
                article.getStatus());
    }

    private static void requirePositive(int value, String message) {
        if (value <= 0) {
            throw new IllegalArgumentException(message);
        }
    }
}
