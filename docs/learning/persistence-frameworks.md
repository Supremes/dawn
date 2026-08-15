# 持久层框架示例与测试

## 职责边界

Hibernate 是项目的 JPA Provider，Spring Data JPA 是建立在 JPA/Hibernate 之上的
Repository 抽象，因此项目实际采用两条数据访问路线：

| 路线 | 适用场景 | 示例表 |
|---|---|---|
| Spring Data JPA + Hibernate | 聚合建模、实体关系、脏检查、缓存和并发控制 | `t_jpa_feature_author`、`t_jpa_feature_article` |
| MyBatis-Plus | 精确 SQL、动态条件、报表查询、批量 CRUD | `t_mp_feature_record` |

两条路线使用同一个数据源和 Spring 事务管理，但不应在同一事务中修改同一张表。
JPA 一级缓存可能保留旧状态，MyBatis-Plus 的直接 SQL 不会自动同步该缓存。

## 特性矩阵

| 框架 | 已实现特性 | 主要入口 |
|---|---|---|
| Spring Data JPA | 派生查询、JPQL DTO Projection、分页、Specification、EntityGraph、Auditing、`@Version`、版本化批量更新 | `JpaArticleRepository`、`JpaArticleApplicationService` |
| Hibernate | `Session`、HQL、Native SQL、流式读取、JDBC Batch、`flush/clear`、`StatelessSession`、二级缓存、Query Cache、Statistics | `HibernateArticleOperations` |
| MyBatis-Plus | `BaseMapper`、Lambda Wrapper、分页、`IService` 批处理、字段自动填充、`@EnumValue`、`@TableLogic`、`@Version`、`BlockAttackInnerInterceptor` | `MpFeatureRecordMapper`、`MpFeatureRecordService` |

生产 MySQL 表结构位于 `release/config/mysql/dawn.sql`。测试使用 H2 的 MySQL
兼容模式，不依赖本地 MySQL、Redis 或消息队列。

## 运行测试

在仓库根目录执行：

```bash
./dawn-springboot/scripts/test-persistence-features.sh all
```

按框架执行：

```bash
./dawn-springboot/scripts/test-persistence-features.sh jpa
./dawn-springboot/scripts/test-persistence-features.sh hibernate
./dawn-springboot/scripts/test-persistence-features.sh mybatis-plus
```

脚本参数非法时返回退出码 `2`；任一测试失败时透传 Maven 的非零退出码，适合直接
接入 CI。

## Hibernate 缓存与统计

生产环境默认关闭 Hibernate Statistics、二级缓存和 Query Cache。确认实体读写模式
与缓存一致性要求后，可通过环境变量按需开启：

```bash
export HIBERNATE_GENERATE_STATISTICS=true
export HIBERNATE_SECOND_LEVEL_CACHE=true
export HIBERNATE_QUERY_CACHE=true
```

缓存实现使用 Caffeine JCache。Query Cache 只应应用于读多写少、参数集合稳定的查询；
频繁更新的数据不应仅为了命中率而开启缓存。

## 安全约束

`BlockAttackInnerInterceptor` 会阻止没有 `WHERE` 的物理 `UPDATE`/`DELETE`。确需清空
数据时必须写出可审计的条件，或通过受控迁移脚本执行。逻辑删除自身会生成
`deleted = 0` 条件，不能把它当作全表操作保护机制。

JPQL 批量更新绕过实体生命周期和一级缓存。本示例显式递增 `version`、更新时间，
并启用 `clearAutomatically`；其他批量语句也必须遵循相同约束。
