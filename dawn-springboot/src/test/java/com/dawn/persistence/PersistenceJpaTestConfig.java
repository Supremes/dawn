package com.dawn.persistence;

import com.dawn.persistence.hibernate.HibernateArticleOperations;
import com.dawn.persistence.jpa.domain.JpaArticle;
import com.dawn.persistence.jpa.repository.JpaArticleRepository;
import com.dawn.persistence.jpa.service.JpaArticleApplicationService;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration(proxyBeanMethods = false)
@ImportAutoConfiguration({
        DataSourceAutoConfiguration.class,
        JdbcTemplateAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        TransactionAutoConfiguration.class
})
@EntityScan(basePackageClasses = JpaArticle.class)
@EnableJpaRepositories(basePackageClasses = JpaArticleRepository.class)
@EnableJpaAuditing
@Import({
        JpaArticleApplicationService.class,
        HibernateArticleOperations.class
})
public class PersistenceJpaTestConfig {
}
