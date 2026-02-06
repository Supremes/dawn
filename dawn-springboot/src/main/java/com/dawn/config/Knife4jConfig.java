package com.dawn.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Knife4j 配置类（支持 Spring Boot 3）
 * 使用 OpenAPI 3.0 规范
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // 创建服务器列表
        List<Server> servers = new ArrayList<>();
        servers.add(new Server().url("http://localhost:8080").description("本地开发环境"));
        
        // 创建联系人信息
        Contact contact = new Contact()
                .name("Supremes")
                .email("1909925152@qq.com");
        
        // 创建许可证信息
        License license = new License()
                .name("Apache 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0.html");
        
        // 创建 API 信息
        Info info = new Info()
                .title("Dawn 项目 API 文档")
                .description("Dawn 项目的 RESTful API 文档，使用 Knife4j 增强")
                .version("1.0")
                .contact(contact)
                .license(license);
        
        // 创建并返回 OpenAPI 对象
        return new OpenAPI()
                .info(info)
                .servers(servers);
    }
}