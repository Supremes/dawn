# Dawn-Springboot 技术架构文档

## 1. 项目概述

`dawn-springboot` 是一个基于前后端分离架构的后端服务，采用最新的 Spring Boot 3 生态体系。它集成了多种主流中间件，具备高可扩展性的策略模式设计，并实现了完善的权限控制、日志审计和系统监控功能。

## 2. 技术选型 (Tech Stack)

### 2.1 核心框架

* **开发语言**: Java 17
* **Web 框架**: Spring Boot 3.4.2
* **ORM 框架**: MyBatis Plus 3.5.5 (配合 MySQL)
* **API 文档**: Knife4j 4.5.0 (基于 OpenAPI 3 / SpringDoc)

### 2.2 中间件与基础设施

* **数据库**: MySQL 8.x
* **缓存**: Redis (Lettuce 客户端) + Spring Cache
* **消息队列**: RabbitMQ (用于解耦业务，如邮件发送、日志处理等)
* **搜索引擎**: Elasticsearch (可选，通过策略模式切换)
* **对象存储**: MinIO (自建) 或 Aliyun OSS (云服务)

### 2.3 安全与监控

* **安全框架**: Spring Security 6.x + JWT
* **监控体系**: Spring Boot Actuator + Micrometer (Prometheus) + Grafana Loki (日志聚合) + Zipkin/Brave (链路追踪)
* **定时任务**: Quartz

---

## 3. 系统架构设计

### 3.1 逻辑分层

系统遵循经典的 MVC 分层架构，并结合了 DDD（领域驱动设计）的一些思想：

1. **接入层 (Controller)**: 处理 HTTP 请求，参数校验，统一响应封装。
2. **业务层 (Service)**: 核心业务逻辑，利用 AOP 处理非功能性需求（如日志、限流）。
3. **持久层 (Mapper)**: 基于 MyBatis Plus 进行数据交互。
4. **基础设施层 (Infrastructure)**: 包含文件上传、搜索服务、消息队列等外部依赖的具体实现。

### 3.2 关键架构特性

#### A. 策略模式实现的模块解耦

项目大量使用**策略模式 (Strategy Pattern)** 来隔离具体的实现细节，使得系统可以根据配置灵活切换底层服务。

* **文件上传**: 定义了 `UploadStrategy` 接口，实现了 `MinioUploadStrategyImpl` 和 `OssUploadStrategyImpl`。通过 `application.yml` 中的 `upload.mode` 动态选择。
* **搜索服务**: 定义了 `SearchStrategy` 接口，支持 `MySqlSearchStrategyImpl` (简单的 Like 查询) 和 `EsSearchStrategyImpl` (Elasticsearch 全文检索)。

#### B. 异步驱动与解耦

利用 **Spring Event** 和 **RabbitMQ** 实现业务解耦：

* **操作日志**: 使用 AOP 切面 `OperationLogAspect` 拦截请求，并通过 `ApplicationContext.publishEvent` 发布 `OperationLogEvent`，实现日志记录与业务逻辑的完全解耦。
* **消息消费**: 定义了多个 Consumer（如 `CommentNoticeConsumer`, `MaxWellConsumer`），通过 RabbitMQ 处理耗时操作。

---

## 4. 核心功能模块详解

### 4.1 安全认证模块 (Security)

项目采用了 **Spring Security 6.x** 的最新写法（`SecurityFilterChain`），摒弃了过时的 `WebSecurityConfigurerAdapter`。

* **无状态认证**: 禁用 Session (`SessionCreationPolicy.STATELESS`)，完全基于 JWT。
* **动态权限控制**: 自定义了 `CustomAuthorizationManager`，替代了传统的拦截器模式，实现了更细粒度的动态 RBAC 权限管理。
* **过滤器链**: `JwtAuthenticationTokenFilter` 被插入到 `UsernamePasswordAuthenticationFilter` 之前，用于解析 Token 并设置上下文。

### 4.2 日志审计模块

通过 **AOP (Aspect Oriented Programming)** 实现全自动的操作日志记录。

* **注解驱动**: 自定义注解 `@OptLog` 标记在 Controller 方法上。
* **切面处理**: `OperationLogAspect` 解析方法参数、请求 IP、操作类型，并异步入库。

### 4.3 运维监控模块

项目构建了完整的可观测性体系：

* **指标监控**: 暴露 Prometheus 端点 (`/actuator/prometheus`)，配合 Grafana 监控 JVM、线程池、HTTP 请求等指标。
* **链路追踪**: 集成了 `micrometer-tracing-bridge-brave`，为日志添加 TraceID 和 SpanID，便于分布式环境下的故障排查。
* **日志聚合**: 使用 `loki-logback-appender` 直接将日志推送到 Loki。

---

## 5. 设计模式应用总结

| 模式名称 | 应用场景 | 涉及文件 |
| --- | --- | --- |
| **Strategy (策略模式)** | 文件上传 (MinIO/OSS)、搜索 (MySQL/ES)、登录 (QQ/微博等) | `UploadStrategy.java`, `SearchStrategy.java` |
| **AOP (代理模式)** | 操作日志记录、接口限流、异常日志 | `OperationLogAspect.java` |
| **Observer (观察者模式)** | 业务解耦，日志异步处理 | `OperationLogEvent`, `OperationLogAspect` |
| **Template (模板方法)** | 策略实现的基类封装 | `AbstractUploadStrategyImpl` (推测存在) |

## 6. 部署建议

根据 `pom.xml` 的构建配置和依赖：

1. **环境要求**: 必须安装 JDK 17。
2. **配置隔离**: 使用 `application-dev.yml` 和 `application-prod.yml` 分离开发与生产配置。
3. **容器化**: 建议使用 Docker 部署，配合 MySQL, Redis, RabbitMQ, MinIO 的 Docker 镜像编排。