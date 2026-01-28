# 分布式链路追踪与日志配置

## 概述

Dawn 应用已集成分布式链路追踪功能，通过 Spring Cloud Sleuth 自动为每个请求生成 traceId 和 spanId，并注入到日志中。

## 当前状态

### ✅ 已启用功能

1. **Spring Cloud Sleuth 集成**
   - 版本：2.2.8.RELEASE
   - 自动为每个 HTTP 请求生成唯一的 traceId
   - 为每个处理步骤生成 spanId
   - 自动将 traceId 和 spanId 注入到 MDC（Mapped Diagnostic Context）中

2. **日志格式**
   - 所有日志包含 traceId 和 spanId：`[traceId,spanId]`
   - 日志模式：`%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{traceId:-},%X{spanId:-}] %-5level %logger{36} - %msg%n`
   - 示例：`2026-01-26 02:42:41.033 [http-nio-8080-exec-2] [6ceeae735feba1b8,6ceeae735feba1b8] DEBUG ...`

3. **日志输出**
   - 控制台输出
   - 文件输出（滚动日志）
     - 位置：`logs/dawn.log`
     - 单文件最大：100MB
     - 保留天数：30天
     - 总大小上限：3GB

### ⏸️ 暂未启用功能

**Grafana Loki 日志聚合**
- 状态：配置已就绪，但由于 loki4j 库与当前环境的兼容性问题暂时禁用
- 依赖已添加：loki-logback-appender 1.0.0
- 配置文件：`logback-spring.xml` 中已包含完整的 Loki appender 配置（已注释）
- 问题：`DynamicClassLoadingException: Failed to instantiate type com.github.loki4j.logback.Loki4jAppender`
- 待解决：需要升级 JDK 版本或等待 loki4j 发布兼容 Java 8 的稳定版本

## 使用方法

### 1. 通过 TraceId 追踪请求

所有属于同一个请求的日志将拥有相同的 traceId。你可以使用 traceId 在日志文件中追踪整个请求的处理流程：

```bash
# 查找特定 traceId 的所有日志
grep "6ceeae735feba1b8" logs/dawn.log
```

### 2. 日志示例

```
2026-01-26 02:42:41.033 [http-nio-8080-exec-2] [6ceeae735feba1b8,6ceeae735feba1b8] DEBUG o.s.security.web.FilterChainProxy - /actuator/prometheus at position 1
2026-01-26 02:42:41.040 [http-nio-8080-exec-2] [6ceeae735feba1b8,6ceeae735feba1b8] DEBUG o.s.s.w.a.AnonymousAuthenticationFilter - Populated SecurityContextHolder
```

- `6ceeae735feba1b8` - traceId（请求唯一标识）
- `6ceeae735feba1b8` - spanId（处理步骤标识）

### 3. 在代码中使用

Spring Cloud Sleuth 自动处理 traceId 和 spanId 的生成和传播，无需额外代码。但如果需要手动访问：

```java
import org.slf4j.MDC;

// 获取当前的 traceId
String traceId = MDC.get("traceId");

// 获取当前的 spanId  
String spanId = MDC.get("spanId");

// 在日志中使用（自动包含）
log.info("Processing request"); // 自动包含 [traceId,spanId]
```

## 配置文件

### application.yml

```yaml
spring:
  application:
    name: dawn
  sleuth:
    enabled: true
    sampler:
      probability: 1.0  # 采样率 100%，生产环境可降低
```

### logback-spring.xml

TraceId 和 SpanId 通过 `%X{traceId:-}` 和 `%X{spanId:-}` 注入到日志模式中：

```xml
<property name="LOG_PATTERN" value="%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{traceId:-},%X{spanId:-}] %-5level %logger{36} - %msg%n"/>
```

## 未来规划

1. **Loki 集成**
   - 解决 loki4j 兼容性问题
   - 启用日志推送到 Grafana Loki
   - 实现集中式日志查询和可视化

2. **Zipkin 集成**（可选）
   - 添加 Zipkin 客户端实现可视化链路追踪
   - 配置 Zipkin 服务器收集追踪数据

3. **性能优化**
   - 根据实际负载调整 Sleuth 采样率
   - 优化日志输出性能

## 相关依赖

```xml
<!-- Spring Cloud Sleuth -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-sleuth</artifactId>
    <version>2.2.8.RELEASE</version>
</dependency>

<!-- Loki Logback Appender (暂时禁用) -->
<dependency>
    <groupId>com.github.loki4j</groupId>
    <artifactId>loki-logback-appender</artifactId>
    <version>1.0.0</version>
    <exclusions>
        <exclusion>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-classic</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

## 故障排查

### TraceId 未出现在日志中

1. 检查 Spring Cloud Sleuth 是否启用：`spring.sleuth.enabled=true`
2. 检查 logback-spring.xml 中的日志格式是否包含 `%X{traceId:-}`
3. 确认请求通过 HTTP 进入应用（Sleuth 主要针对 HTTP 请求）

### Loki 相关错误

- 当前 Loki appender 已禁用，不应出现相关错误
- 如需启用，请先升级到 Java 11+ 或等待兼容性修复

## 参考文档

- [Spring Cloud Sleuth Documentation](https://spring.io/projects/spring-cloud-sleuth)
- [Logback Configuration](https://logback.qos.ch/manual/configuration.html)
- [Grafana Loki](https://grafana.com/docs/loki/latest/)
