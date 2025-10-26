package com.dawn.hibernate;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public abstract class GenericDao<T, ID> {
//    @Autowired
//    private SessionFactory sessionFactory;

    // 注入一个事务型的 EntityManager
    // Spring 会自动管理其生命周期和事务绑定
    @PersistenceContext
    protected EntityManager em;
    private Class<T> clazz;

    protected GenericDao(Class<T> clazz) {
        this.clazz = clazz;
    }

    public Optional<T> findById(ID id) {
        return Optional.ofNullable(em.find(clazz, id));
    }

    public List<T> findAll() {
        String jpql = "select e from " + clazz.getSimpleName() + " e";
        return em.createQuery(jpql, clazz).getResultList();
    }

    @Transactional
    public T save(T entity) {
        em.persist(entity);
        return entity;
    }

    @Transactional
    public T update(T entity) {
        return em.merge(entity);
    }

    @Transactional
    public void delete(T entity) {
        // 处理可能处于游离 (detached) 状态的实体。
        /*
         实体变为游离的常见场景：
           1. 上一次事务结束 / EntityManager 已关闭。
           2. 实体序列化发送到客户端后又回传（跨层往返）。
           3. 调用了 em.detach(entity)、em.clear() 或 em.close()。
           4. 放入缓存 / HttpSession，之后在新的持久化上下文中再次使用。
           5. 通过 MQ / RPC（JSON 等）序列化再反序列化。
           6. 在一个线程中加载，在另一个线程中使用（EntityManager 线程绑定）。
         
         说明：em.remove(em.contains(entity) ? entity : em.merge(entity))
           - remove() 只能作用于“托管” (Managed) 实体。
           - 若实体是游离的，通过 merge() 得到托管副本再删除。
         
         生命周期简述：Transient(瞬态) -> Managed(托管) -> Detached(游离) -> Removed(删除，flush/commit 时执行物理删除)。
        */
        em.remove(em.contains(entity) ? entity : em.merge(entity));
    }

    public void deleteById(ID id) {
//        em.remove(em.find(clazz, id));
        findById(id).ifPresent(this::delete);
    }
}
