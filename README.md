# Dawn-Server 后端项目文档

**Dawn-Server** 是一个基于 **Spring Boot 3 + Java 17** 构建的现代化博客/CMS 后端系统。它采用前后端分离架构，集成了主流的中间件与监控体系，并使用了策略模式设计核心功能（如上传、搜索、登录），具备良好的扩展性与维护性。

## 🛠 技术栈 (Tech Stack)

### 核心框架

* **开发语言**: Java 17
* **Web 框架**: Spring Boot 3.4.2
* **持久层框架**: MyBatis-Plus 3.5.5、Spring Data JPA 3.4、Hibernate 6.6
* **安全框架**: Spring Security 6 (配合 JWT 实现无状态认证)
* **API 文档**: Knife4j 4.5.0 (基于 OpenAPI 3 / SpringDoc)

### 中间件 & 基础设施

* **数据库**: MySQL 8.x
* **缓存**: Redis (Lettuce 客户端)
* **消息队列**: RabbitMQ (用于邮件发送、日志解耦、流量削峰)
* **搜索引擎**: Elasticsearch (可选，支持高亮分词搜索)
* **对象存储**: MinIO (自建) 或 Aliyun OSS (阿里云)
* **定时任务**: Quartz

### 监控与日志

* **监控指标**: Spring Boot Actuator + Micrometer (Prometheus)
* **链路追踪**: Zipkin / Brave (用于分布式请求追踪)
* **日志聚合**: Loki (Loki-Logback-Appender)

---

## ✨ 核心特性 (Features)

* **策略模式设计**:
* **文件上传**: 支持通过配置切换本地/MinIO/OSS上传策略。
* **搜索服务**: 支持 MySQL Like 查询或 Elasticsearch 全文检索策略切换。
* **社交登录**: 封装了 QQ、微博等第三方登录策略。


* **完善的权限管理**: 基于 RBAC 模型（用户-角色-菜单），支持动态权限控制 `CustomAuthorizationManager`。
* **全方位日志审计**:
* **操作日志**: AOP 切面自动记录用户敏感操作。
* **异常日志**: 全局异常捕获并记录系统异常。


* **丰富的内容模块**: 文章管理、标签/分类、说说（微动态）、相册管理、留言板、友链等。

---

## 📂 项目结构 (Project Structure)

```text
com.dawn
├── annotation    // 自定义注解 (如 @OptLog, @AccessLimit)
├── aspect        // AOP 切面 (日志记录、执行时间统计)
├── config        // 配置类 (Security, Swagger, Redis, RabbitMQ等)
├── controller    // 控制器层 (API 接口)
├── entity        // 数据库实体类
├── enums         // 枚举常量
├── event         // Spring 事件 (用于解耦日志等业务)
├── exception     // 全局异常处理
├── filter        // 过滤器 (JWT认证)
├── handler       // 处理器 (权限校验、MP自动填充)
├── mapper        // DAO 层接口
├── model         // DTO / VO 模型
├── quartz        // 定时任务逻辑
├── service       // 业务逻辑接口与实现
├── strategy      // 策略模式实现 (Search, Upload, Login)
└── util          // 工具类 (IP, JWT, File等)

```

---

## 🚀 快速开始 (Getting Started)

### 1. 环境准备

* JDK 17+
* Maven 3.8+
* MySQL 8.0+
* Redis 6.0+
* RabbitMQ 3.8+
* (可选) Elasticsearch 7.x, MinIO

### 2. 数据库初始化

1. 创建数据库 `dawn`。
2. 导入 SQL 脚本（位于 `release/config/mysql/dawn.sql` 或项目根目录下的 SQL 文件）。

### 3. 修改配置

打开 `src/main/resources/application.yml`，修改以下关键配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/dawn?serverTimezone=Asia/Shanghai
    username: root
    password: your_password
  data:
    redis:
      host: localhost
      password: your_redis_password
  rabbitmq:
    host: localhost
    password: your_rabbitmq_password

# 文件上传模式 (minio 或 oss)
upload:
  mode: minio 
  minio:
    endpoint: http://localhost:9000
    accessKey: your_access_key
    secretKey: your_secret_key

# 搜索模式 (mysql 或 elasticsearch)
search:
  mode: mysql

```

### 4. 启动项目

运行 `DawnSpringbootApplication` 的 `main` 方法，或者使用 Maven 命令：

```bash
mvn clean package
java -jar target/dawn-springboot-1.0.jar

```

启动成功后，访问 API 文档：`http://localhost:8080/doc.html`

---

## 🧪 持久层特性示例

项目提供彼此隔离的 Spring Data JPA/Hibernate 与 MyBatis-Plus 示例模型，避免两个
Persistence Context 同时修改同一张业务表。示例覆盖 Repository、Specification、
EntityGraph、审计、乐观锁、Hibernate 原生 API、批处理、二级缓存、逻辑删除、
枚举映射、分页和防全表更新/删除。

```bash
# 运行全部持久层特性测试
./dawn-springboot/scripts/test-persistence-features.sh all

# 也可以按框架运行
./dawn-springboot/scripts/test-persistence-features.sh jpa
./dawn-springboot/scripts/test-persistence-features.sh hibernate
./dawn-springboot/scripts/test-persistence-features.sh mybatis-plus
```

详细特性矩阵与使用边界见
[`docs/learning/persistence-frameworks.md`](docs/learning/persistence-frameworks.md)。

---

## 🐳 Docker 部署 或 使用脚本一键部署

项目提供了完整的 Docker 支持，位于 `release` 目录下。

1. **进入发布目录**:
```bash
cd release
cd release/scripts
```


2. **构建并启动服务**:
使用 Docker Compose 一键启动所有依赖服务（MySQL, Redis, RabbitMQ, ES 等）和后端应用。
```bash
docker-compose up -d --build nginx
./backend_restart.sh/ps1
```


*参考文件*: `release/docker-compose.yaml`

---

## 📝 注意事项

1. **Java 版本**: 项目强制依赖 Java 17，请确保本地开发环境和部署环境 JDK 版本正确。
2. **Elasticsearch**: 如果不使用 ES，请务必在 `application.yml` 中将 `search.mode` 设置为 `mysql`，否则启动可能会报错连接不上 ES。
3. **RabbitMQ**: 消息队列是必须的组件，用于处理异步日志和邮件通知，启动前请确保 RabbitMQ 服务正常。