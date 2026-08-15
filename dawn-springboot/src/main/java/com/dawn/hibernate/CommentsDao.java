package com.dawn.hibernate;

import com.dawn.entity.Comment;
import com.dawn.model.vo.CommentVO;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CommentsDao extends GenericDao<Comment, Integer> {
    public CommentsDao() {
        super(Comment.class);
    }

    /*
        80% 场景：JPA API (EntityManager / JPQL / Criteria) —— 简洁 + 可移植。
        20% 特殊性能或 Hibernate 扩展：unwrap Session / 使用特有 API。
    保持 GenericDao 基于 JPA；在具体 DAO 中按需“下钻” Hibernate。

    什么时候需要直接用 Hibernate API
        做批量插入/更新（用 Session 的 batch + flush/clear 微调）。
        想利用二级缓存精准控制（查询 hint、缓存区域名）。
        使用 StatelessSession 规避一级缓存开销。
        需要 Scroll/流式处理巨大结果集。
        使用某些 JPA 未覆盖的方言特性或统计信息。
     */
    public Integer getCommentsCount(CommentVO commentVO) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Comment> root = cq.from(Comment.class);
        List<Predicate> predicates = new ArrayList<>();

        if (commentVO.getTopicId() != null) {
            predicates.add(cb.equal(root.get("topicId"), commentVO.getTopicId()));
        }
        if (commentVO.getType() != null) {
            predicates.add(cb.equal(root.get("type"), commentVO.getType()));
        }

        // 例如：只统计顶级评论（根据你的业务可删除这段）
         predicates.add(cb.isNull(root.get("parentId")));

        // 过滤删除或未审核，根据业务字段（示例：isDelete = 0, isReview = 1）
        predicates.add(cb.equal(root.get("isDelete"), 0));
        // 如果需要只统计已审核
        predicates.add(cb.equal(root.get("isReview"), 1));

        cq.select(cb.count(root)).where(predicates.toArray(new Predicate[0]));
        Long count = em.createQuery(cq).getSingleResult();
        return count.intValue();
    }

    public Integer getCommentsCountUsingHibernateAPI(CommentVO commentVO) {
        Session session = em.unwrap(Session.class);
        StringBuilder hql = new StringBuilder("""
                select count(comment)
                from Comment comment
                where comment.isDelete = 0
                  and comment.isReview = 1
                  and comment.parentId is null
                """);
        if (commentVO.getType() != null) {
            hql.append(" and comment.type = :type");
        }
        if (commentVO.getTopicId() != null) {
            hql.append(" and comment.topicId = :topicId");
        }
        Query<Long> query = session.createQuery(hql.toString(), Long.class);
        if (commentVO.getType() != null) {
            query.setParameter("type", commentVO.getType());
        }
        if (commentVO.getTopicId() != null) {
            query.setParameter("topicId", commentVO.getTopicId());
        }
        return query.getSingleResult().intValue();
    }
}
