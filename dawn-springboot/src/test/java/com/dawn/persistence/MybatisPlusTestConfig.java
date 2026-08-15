package com.dawn.persistence;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.dawn.config.MybatisPlusConfig;
import com.dawn.handler.MyMetaObjectHandler;
import com.dawn.persistence.mybatis.mapper.MpFeatureRecordMapper;
import com.dawn.persistence.mybatis.service.MpFeatureRecordService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@ImportAutoConfiguration({
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        JdbcTemplateAutoConfiguration.class,
        SqlInitializationAutoConfiguration.class,
        TransactionAutoConfiguration.class,
        MybatisPlusAutoConfiguration.class
})
@MapperScan(basePackageClasses = MpFeatureRecordMapper.class)
@Import({
        MybatisPlusConfig.class,
        MyMetaObjectHandler.class,
        MpFeatureRecordService.class
})
public class MybatisPlusTestConfig {
}
