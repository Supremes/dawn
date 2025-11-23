

# SpringBoot 默认的ObjectMapper

默认使用 Jackson `ObjectMapper` 实例来序列化和反序列化 JSON 格式的响应与请求。

默认情况下，Spring Boot 禁用了以下功能：

- `MapperFeature.DEFAULT_VIEW_INCLUSION`
- `DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES`
- `SerializationFeature.WRITE_DATES_AS_TIMESTAMPS`





## 自定义ObjectMapper

### 1. Application Properties 和自定义 Jackson Module

配置 mapper 的最简单方法是通过 `application.properties`。

配置的大致结构如下：

```properties
spring.jackson.<category_name>.<feature_name>=true,false
```

举例来说，禁用 `SerializationFeature.WRITE_DATES_AS_TIMESTAMPS`：

```properties
spring.jackson.serialization.write-dates-as-timestamps=false
```

除了上述功能类别，还可以配置 “属性包含”（property inclusion）：

```properties
spring.jackson.default-property-inclusion=always, non_null, non_absent, non_default, non_empty
```

> 完整的配置属性，可以参考 [中文文档](https://springdoc.cn/spring-boot/application-properties.html#application-properties.json.spring.jackson.constructor-detector)。

配置属性是最简单的方法。这种方法的缺点是无法自定义高级选项，例如为 `LocalDateTime` 自定义日期格式。

此时，根据上述配置会得到如下结果：

```json
{
  "brand": "Lavazza",
  "date": "2020-11-16T10:35:34.593"
}
```

可以使用自定义日期格式注册一个新的 `JavaTimeModule` 来实现 “自定义日期格式” 目标：

```java
@Configuration
@PropertySource("classpath:coffee.properties")
public class CoffeeRegisterModuleConfig {

    @Bean
    public Module javaTimeModule() {
        JavaTimeModule module = new JavaTimeModule();
        module.addSerializer(LOCAL_DATETIME_SERIALIZER);
        return module;
    }
}
```

配置属性文件 `coffee.properties` 如下：

```properties
spring.jackson.default-property-inclusion=non_null
```

Spring Boot 会自动注册任何 `com.fasterxml.jackson.databind.Module` 类型的 Bean。下面是配置的最终结果：

```json
{
  "brand": "Lavazza",
  "date": "16-11-2020 10:43"
}
```

### 2. `Jackson2ObjectMapperBuilderCustomizer`

该函数式接口用于创建配置 Bean。

这些配置 Bean 应用于通过 `Jackson2ObjectMapperBuilder` 创建的默认 `ObjectMapper`：

```java
@Bean
public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
    return builder -> builder.serializationInclusion(JsonInclude.Include.NON_NULL)
      .serializers(LOCAL_DATETIME_SERIALIZER);
}
```

配置 Bean 以特定顺序应用，可以使用 `@Order` 注解控制该顺序。如果想从不同的配置或模块中配置 `ObjectMapper`，那么这种优雅的方法非常适合。



## 覆盖默认ObjectMapper

如果想完全控制配置，有几个方式可以禁用自动配置，只允许应用自定义的配置。

### 1、`ObjectMapper`

覆盖默认配置的最简单方法是定义一个 `ObjectMapper` Bean 并将其标记为 `@Primary`：

```java
@Bean
@Primary
public ObjectMapper objectMapper() {
    JavaTimeModule module = new JavaTimeModule();
    module.addSerializer(LOCAL_DATETIME_SERIALIZER);
    return new ObjectMapper()
      .setSerializationInclusion(JsonInclude.Include.NON_NULL)
      .registerModule(module);
}
```

当我们想完全控制序列化过程，又不想允许外部配置时，可以使用这种方法。

### 2、`Jackson2ObjectMapperBuilder`

另一种简单的方法是定义一个 `Jackson2ObjectMapperBuilder` Bean。

实际上，Spring Boot 在构建 `ObjectMapper` 时默认使用该 Builder，并会自动选择已定义的 Builder：

```java
@Bean
public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
    return new Jackson2ObjectMapperBuilder().serializers(LOCAL_DATETIME_SERIALIZER)
      .serializationInclusion(JsonInclude.Include.NON_NULL);
}
```

默认情况下，它配置两个选项：

- 禁用 `MapperFeature.DEFAULT_VIEW_INCLUSION`
- 禁用 `DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES`

根据 `Jackson2ObjectMapperBuilder` [文档](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/http/converter/json/Jackson2ObjectMapperBuilder.html)，如果 classpath 上存在某些模块（module），它也会注册这些模块：

- jackson-datatype-jdk8：支持其他 Java 8 类型，如 `Optional`
- jackson-datatype-jsr310：支持 Java 8 Date 和 Time API 类型
- jackson-datatype-joda：支持 Joda-Time 类型
- jackson-module-kotlin：支持 Kotlin 类和数据类

这种方法的优势在于，`Jackson2ObjectMapperBuilder` 提供了一种简单直观的方法来构 `ObjectMapper`。

### 3、`MappingJackson2HttpMessageConverter`

定义一个类型为 `MappingJackson2HttpMessageConverter` 的 Bean，Spring Boot 就会自动使用它：

```java
@Bean
public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
    Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder().serializers(LOCAL_DATETIME_SERIALIZER)
      .serializationInclusion(JsonInclude.Include.NON_NULL);
    return new MappingJackson2HttpMessageConverter(builder.build());
}
```

关于 `HttpMessageConverter` 的更多内容，可以参阅 [中文文档](https://springdoc.cn/spring-boot-customize-jackson-objectmapper/spring/web.html#mvc-config-message-converters)。