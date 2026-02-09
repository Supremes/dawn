# Redis 数据结构与 Java 开发详尽指南

## 目录

1. [Redis 核心数据结构](#redis-核心数据结构)
2. [Redis 高级特性](#redis-高级特性)
3. [Redis 持久化与集群](#redis-持久化与集群)
4. [Java Redis 基础集成](#java-redis-基础集成)
5. [Spring Redis 集成](#spring-redis-集成)
6. [Java Redis 高级模式](#java-redis-高级模式)
7. [生产环境最佳实践](#生产环境最佳实践)

---

## Redis 核心数据结构

### 1. String (字符串)

**底层实现：**
- **SDS (Simple Dynamic String)**: Redis 自定义的字符串结构
- **结构组成：**
  ```c
  struct sdshdr {
      int len;      // 字符串长度
      int free;     // 未使用空间长度
      char buf[];   // 字节数组
  }
  ```

**内存优化特性：**
- **预分配策略**: 当字符串长度小于1MB时，分配2倍长度空间；大于1MB时，额外分配1MB
- **惰性释放**: 缩短字符串时不立即释放内存，避免频繁内存操作
- **二进制安全**: 支持包含空字符的二进制数据

**编码类型：**
- **int**: 整数值，8字节长整型
- **embstr**: 小于44字节的字符串，一次内存分配
- **raw**: 大字符串，两次内存分配

**常用命令与复杂度：**
```bash
SET key value           # O(1) - 设置值
GET key                 # O(1) - 获取值
MSET key1 val1 key2 val2# O(N) - 批量设置
MGET key1 key2          # O(N) - 批量获取
INCR key                # O(1) - 原子递增
DECR key                # O(1) - 原子递减
APPEND key value        # O(1) - 追加字符串
STRLEN key              # O(1) - 获取长度
SETEX key seconds value # O(1) - 设置带过期时间
```

**应用场景：**
- 缓存用户信息、配置数据
- 计数器、分布式ID生成
- 分布式锁实现
- Session共享

### 2. Hash (哈希表)

**底层实现：**
- **ziplist** (压缩列表): 当字段数量少且值较小时使用
- **hashtable** (哈希表): 数据量大时使用，基于链式哈希表

**ziplist 结构：**
```
<zlbytes><zltail><zllen><entry1><entry2>...<entryN><zlend>
```
- 内存连续，减少内存碎片
- 触发条件：`hash-max-ziplist-entries` (512) 和 `hash-max-ziplist-value` (64)

**hashtable 结构：**
- 使用 MurmurHash2 算法
- 渐进式 rehash，避免阻塞
- 负载因子控制扩容和缩容

**常用命令：**
```bash
HSET key field value    # O(1) - 设置字段
HGET key field          # O(1) - 获取字段
HMSET key f1 v1 f2 v2   # O(N) - 批量设置
HMGET key f1 f2         # O(N) - 批量获取
HGETALL key             # O(N) - 获取所有字段
HDEL key field          # O(1) - 删除字段
HEXISTS key field       # O(1) - 判断字段存在
HLEN key                # O(1) - 获取字段数量
HINCRBY key field increment # O(1) - 字段值递增
```

**应用场景：**
- 用户属性存储 (用户ID作key，属性作field)
- 购物车实现 (用户ID作key，商品ID作field，数量作value)
- 配置管理

### 3. List (列表)

**底层实现：**
- **quicklist**: Redis 3.2+ 版本使用，结合了 ziplist 和 linkedlist 优点
- **结构**: 双向链表，每个节点包含一个 ziplist

**quicklist 优化：**
```c
typedef struct quicklist {
    quicklistNode *head;        // 头节点
    quicklistNode *tail;        // 尾节点
    unsigned long count;        // 总元素数量
    unsigned long len;          // 节点数量
} quicklist;

typedef struct quicklistNode {
    struct quicklistNode *prev; // 前驱节点
    struct quicklistNode *next; // 后继节点
    unsigned char *zl;          // ziplist
    unsigned int sz;            // ziplist字节大小
    unsigned int count : 16;    // ziplist元素数量
    unsigned int encoding : 2;  // 编码方式
    unsigned int container : 2; // 容器类型
    unsigned int recompress : 1;// 是否需要重新压缩
} quicklistNode;
```

**配置参数：**
- `list-max-ziplist-size`: 控制每个ziplist的大小
- `list-compress-depth`: 控制压缩深度

**常用命令：**
```bash
LPUSH key element           # O(1) - 左侧插入
RPUSH key element           # O(1) - 右侧插入
LPOP key                    # O(1) - 左侧弹出
RPOP key                    # O(1) - 右侧弹出
LINDEX key index            # O(N) - 根据索引获取
LSET key index element      # O(N) - 根据索引设置
LRANGE key start stop       # O(S+N) - 获取范围元素
LLEN key                    # O(1) - 获取列表长度
LTRIM key start stop        # O(N) - 保留指定范围
BLPOP key timeout           # O(1) - 阻塞左侧弹出
BRPOP key timeout           # O(1) - 阻塞右侧弹出
```

**应用场景：**
- 消息队列实现
- 时间线功能 (微博、朋友圈)
- 最新N个数据缓存
- 栈和队列数据结构实现

### 4. Set (集合)

**底层实现：**
- **intset** (整数集合): 当元素都是整数且数量较少时
- **hashtable**: 通用情况使用

**intset 结构：**
```c
typedef struct intset {
    uint32_t encoding;  // 编码方式 (16位、32位、64位)
    uint32_t length;    // 元素数量
    int8_t contents[];  // 元素数组
} intset;
```

**转换条件：**
- 非整数元素加入时转为 hashtable
- 元素数量超过 `set-max-intset-entries` (512) 时转换

**常用命令：**
```bash
SADD key member             # O(1) - 添加元素
SREM key member             # O(1) - 删除元素
SISMEMBER key member        # O(1) - 判断元素存在
SMEMBERS key                # O(N) - 获取所有元素
SCARD key                   # O(1) - 获取元素数量
SPOP key [count]            # O(1) - 随机弹出元素
SRANDMEMBER key [count]     # O(1) - 随机获取元素
SUNION key1 key2            # O(N) - 并集运算
SINTER key1 key2            # O(N*M) - 交集运算
SDIFF key1 key2             # O(N) - 差集运算
```

**应用场景：**
- 标签系统 (用户标签、文章标签)
- 好友关系、共同好友
- 去重计数
- 抽奖系统

### 5. Sorted Set (有序集合)

**底层实现：**
- **ziplist**: 元素数量少且成员值小时使用
- **skiplist + hashtable**: 跳表与哈希表结合

**跳表 (Skip List) 结构：**
```c
typedef struct zskiplistNode {
    sds ele;                           // 成员对象
    double score;                      // 分值
    struct zskiplistNode *backward;    // 后退指针
    struct zskiplistLevel {
        struct zskiplistNode *forward; // 前进指针
        unsigned long span;            // 跨度
    } level[];                         // 层
} zskiplistNode;

typedef struct zskiplist {
    struct zskiplistNode *header, *tail; // 头尾节点
    unsigned long length;                 // 节点数量
    int level;                           // 最大层数
} zskiplist;
```

**跳表优势：**
- 平均 O(log N) 时间复杂度
- 支持范围查询
- 实现相对简单，不需要复杂的平衡操作

**常用命令：**
```bash
ZADD key score member       # O(log N) - 添加元素
ZREM key member             # O(log N) - 删除元素
ZSCORE key member           # O(1) - 获取分值
ZRANK key member            # O(log N) - 获取排名
ZREVRANK key member         # O(log N) - 获取倒序排名
ZRANGE key start stop       # O(log N + M) - 范围查询
ZREVRANGE key start stop    # O(log N + M) - 倒序范围查询
ZRANGEBYSCORE key min max   # O(log N + M) - 分值范围查询
ZCOUNT key min max          # O(log N) - 计算范围内元素数
ZINCRBY key increment member # O(log N) - 增加分值
```

**应用场景：**
- 排行榜系统 (游戏积分、销量排行)
- 延时任务队列 (时间戳作分值)
- 限流算法实现 (滑动窗口)
- 自动补全功能

---

## Redis 高级特性

### 1. HyperLogLog

**算法原理：**
- 基于概率统计的基数估算算法
- 使用极少内存 (12KB) 估算超大集合的基数
- 误差率约为 0.81%

**底层实现：**
```c
struct hllhdr {
    char magic[4];      // "HYLL" 魔数
    uint8_t encoding;   // 编码类型
    uint8_t notused[3]; // 预留字段
    uint8_t card[8];    // 缓存的基数估算值
    uint8_t registers[HLL_REGISTERS]; // 16384 个桶
};
```

**编码类型：**
- **HLL_DENSE**: 密集编码，使用6位存储每个桶
- **HLL_SPARSE**: 稀疏编码，适用于基数较小的情况
- **HLL_RAW**: 原始编码，未压缩状态

**常用命令：**
```bash
PFADD key element1 element2    # O(1) - 添加元素
PFCOUNT key1 key2              # O(1) - 估算基数
PFMERGE destkey srckey1 srckey2 # O(N) - 合并HLL
```

**应用场景：**
- 网站UV统计 (独立访客数)
- 大数据去重计数
- 实时数据流基数估算

### 2. Bitmap (位图)

**底层实现：**
- 基于 String 类型实现
- 每个位占用1bit，最大支持 2^32 位
- 自动扩展，按需分配内存

**内存优化：**
- 稀疏位图会进行压缩存储
- 连续的0或1会被压缩

**常用命令：**
```bash
SETBIT key offset value        # O(1) - 设置位值
GETBIT key offset              # O(1) - 获取位值
BITCOUNT key [start end]       # O(N) - 统计1的数量
BITPOS key bit [start end]     # O(N) - 查找第一个指定位
BITOP operation destkey key1 key2 # O(N) - 位运算
```

**位运算操作：**
- AND: 按位与
- OR: 按位或  
- XOR: 按位异或
- NOT: 按位非

**应用场景：**
- 用户签到统计
- 在线用户统计
- 权限管理 (每个位代表一个权限)
- 布隆过滤器实现

### 3. Geospatial (地理空间)

**底层实现：**
- 基于 Sorted Set 实现
- 使用 GeoHash 算法编码经纬度
- 将二维坐标转换为一维整数存储

**GeoHash 算法：**
- 将经纬度转换为base32字符串
- 字符串长度越长，精度越高
- 相邻区域的GeoHash前缀相同

**常用命令：**
```bash
GEOADD key longitude latitude member    # O(log N) - 添加地理位置
GEOPOS key member                       # O(log N) - 获取坐标
GEODIST key member1 member2 [unit]      # O(log N) - 计算距离
GEORADIUS key longitude latitude radius unit # O(N+log M) - 范围查询
GEORADIUSBYMEMBER key member radius unit     # O(N+log M) - 成员范围查询
GEOHASH key member                      # O(log N) - 获取GeoHash
```

**距离单位：**
- m: 米
- km: 千米  
- mi: 英里
- ft: 英尺

**应用场景：**
- 附近的人/商家查找
- 地理围栏功能
- 物流配送路径优化
- LBS (Location Based Service) 应用

### 4. Stream (流)

**数据结构：**
- 基于 Radix Tree 实现
- 支持消息持久化
- 提供消费者组功能

**消息ID格式：**
```
<millisecondsTime>-<sequenceNumber>
```

**常用命令：**
```bash
XADD stream ID field value              # O(1) - 添加消息
XREAD [COUNT count] STREAMS stream ID   # O(N) - 读取消息
XRANGE stream start end [COUNT count]   # O(N) - 范围查询
XLEN stream                            # O(1) - 获取长度

# 消费者组
XGROUP CREATE stream group ID          # O(1) - 创建消费者组
XREADGROUP GROUP group consumer STREAMS stream ID # O(M) - 组内消费
XACK stream group ID                   # O(1) - 确认消息
XPENDING stream group [start end count consumer] # O(N) - 待处理消息
```

**消费者组特性：**
- 消息分发：每条消息只被组内一个消费者处理
- 故障转移：支持消息重新分配
- 消息确认：支持消息处理确认机制
- 历史消息：支持从任意位置开始消费

**应用场景：**
- 消息队列系统
- 事件溯源 (Event Sourcing)
- 日志收集与分析
- 实时数据流处理

### 5. Module (模块系统)

**常用模块：**

**RedisJSON:**
```bash
JSON.SET user:1 $ '{"name":"张三","age":25}'
JSON.GET user:1 $.name
JSON.NUMINCRBY user:1 $.age 1
```

**RedisTimeSeries:**
```bash
TS.CREATE temperature:sensor1
TS.ADD temperature:sensor1 * 23.5
TS.RANGE temperature:sensor1 - +
```

**RedisGraph:**
```bash
GRAPH.QUERY social "CREATE (p:Person {name: '张三'})"
GRAPH.QUERY social "MATCH (p:Person) RETURN p.name"
```

**RediSearch:**
```bash
FT.CREATE idx:user ON HASH PREFIX 1 user: SCHEMA name TEXT age NUMERIC
FT.SEARCH idx:user "张三"
```

---

## Redis 持久化与集群

### 1. RDB 持久化

**工作原理：**
- 在指定时间间隔内生成数据集的时间点快照
- fork子进程执行持久化操作，不阻塞主进程
- 使用 Copy-On-Write (COW) 机制优化内存使用

**触发方式：**
```bash
# 自动触发 (redis.conf配置)
save 900 1      # 900秒内至少1个key变化
save 300 10     # 300秒内至少10个key变化  
save 60 10000   # 60秒内至少10000个key变化

# 手动触发
SAVE            # 同步保存，会阻塞主进程
BGSAVE          # 异步保存，fork子进程执行
LASTSAVE        # 获取最后保存时间
```

**RDB文件格式：**
```
REDIS | VERSION | AUX | SELECTDB | DB_DATA | EOF | CHECKSUM
```

**优点：**
- 文件紧凑，适合备份和灾难恢复
- 恢复速度快，直接加载到内存
- 对性能影响小 (fork子进程)

**缺点：**
- 可能丢失最后一次持久化后的数据
- fork进程时可能造成短暂停顿
- 数据量大时fork耗时较长

**配置优化：**
```bash
# RDB相关配置
rdbcompression yes          # 启用压缩
rdbchecksum yes            # 启用校验和
dbfilename dump.rdb        # RDB文件名
dir /var/lib/redis         # 文件保存目录
stop-writes-on-bgsave-error yes # 保存失败时停止写入
```

### 2. AOF 持久化

**工作原理：**
- 记录每个写操作命令到日志文件
- 重启时重放这些命令来恢复数据
- 支持三种同步策略

**同步策略：**
```bash
# appendfsync配置
always          # 每个写命令都同步，最安全但性能差
everysec        # 每秒同步一次，平衡安全性和性能  
no              # 由操作系统决定何时同步，性能最好但可能丢失数据
```

**AOF 重写：**
- 目的：压缩AOF文件大小，提高加载速度
- 原理：fork子进程，遍历内存中的数据库，生成最短命令序列
- 触发条件：
  ```bash
  auto-aof-rewrite-percentage 100   # 文件大小增长100%时触发
  auto-aof-rewrite-min-size 64mb    # 文件至少64MB时才考虑重写
  ```

**手动操作：**
```bash
BGREWRITEAOF    # 手动触发AOF重写
```

**AOF文件格式：**
```
*<argc>\r\n
$<len>\r\n
<command>\r\n
$<len>\r\n
<arg1>\r\n
...
```

**优点：**
- 数据安全性高，最多丢失1秒数据
- AOF文件易读，可手动修复
- 自动重写机制保持文件大小合理

**缺点：**
- 文件通常比RDB大
- 恢复速度比RDB慢
- 某些命令可能有bug (如BRPOPLPUSH)

**混合持久化 (Redis 4.0+)：**
```bash
aof-use-rdb-preamble yes    # 启用混合持久化
```
- AOF重写时使用RDB格式存储快照
- 新的写操作仍使用AOF格式追加
- 结合两种方式的优点

### 3. 主从复制 (Replication)

**复制原理：**
1. **全量复制**：slave首次连接master
   - slave发送PSYNC命令
   - master执行BGSAVE生成RDB
   - master发送RDB文件给slave
   - slave清空自己数据库，载入RDB
   - master发送复制缓冲区数据给slave

2. **增量复制**：slave重连master
   - 基于复制偏移量和复制积压缓冲区
   - master检查请求的偏移量是否在缓冲区内
   - 如果在，发送缓冲区中对应的数据
   - 如果不在，执行全量复制

**关键配置：**
```bash
# 主节点配置
repl-diskless-sync no           # 是否使用无盘复制
repl-diskless-sync-delay 5      # 无盘复制延迟时间
repl-backlog-size 1mb           # 复制积压缓冲区大小
repl-backlog-ttl 3600           # 缓冲区保持时间

# 从节点配置  
replicaof 192.168.1.100 6379    # 指定主节点
replica-read-only yes           # 从节点只读
replica-serve-stale-data yes    # 主从断线时是否响应查询
```

**复制拓扑：**
- **一主多从**: 最常见，读写分离
- **级联复制**: 减轻主节点压力，slave可以有自己的slave
- **环形复制**: 避免单点故障，但配置复杂

### 4. Redis Sentinel (哨兵)

**功能特性：**
- **监控**: 监控主从节点是否正常运行
- **通知**: 通过API通知管理员故障情况
- **故障转移**: 自动进行主从切换
- **配置提供**: 为客户端提供当前主节点信息

**Sentinel 集群：**
- 建议奇数个Sentinel节点 (3、5、7个)
- 通过Raft算法选举领导者
- 领导者负责执行故障转移

**关键配置：**
```bash
# sentinel.conf
sentinel monitor mymaster 192.168.1.100 6379 2
sentinel down-after-milliseconds mymaster 5000
sentinel parallel-syncs mymaster 1  
sentinel failover-timeout mymaster 15000
sentinel auth-pass mymaster mypassword
```

**故障转移流程：**
1. 多个Sentinel检测到主节点下线
2. Sentinel之间协商，超过quorum数量认为主观下线变为客观下线
3. 选举领导者Sentinel执行故障转移
4. 从slave中选择新的master (根据优先级、复制偏移量、运行时间)
5. 让其他slave指向新master
6. 通知客户端主节点变化
7. 将旧master降为slave

### 5. Redis Cluster (集群)

**设计原理：**
- **去中心化**: 无需代理，节点间直接通信
- **数据分片**: 16384个槽位分布在不同节点
- **高可用**: 支持主从复制和故障转移
- **动态扩缩容**: 支持在线增删节点

**槽位分配：**
- 使用CRC16算法计算key的槽位
- 公式：`slot = CRC16(key) % 16384`
- 支持Hash Tag，强制相关key在同一节点

**节点通信：**
- 使用Gossip协议交换节点状态信息
- 每秒随机选择几个节点发送ping消息
- 收到ping后回复pong消息

**集群命令：**
```bash
# 创建集群 (Redis 5.0+)
redis-cli --cluster create 127.0.0.1:7000 127.0.0.1:7001 \
127.0.0.1:7002 127.0.0.1:7003 127.0.0.1:7004 127.0.0.1:7005 \
--cluster-replicas 1

# 集群管理
CLUSTER NODES                   # 查看节点信息
CLUSTER INFO                    # 查看集群状态  
CLUSTER SLOTS                   # 查看槽位分配
CLUSTER ADDSLOTS slot1 slot2    # 添加槽位
CLUSTER DELSLOTS slot1 slot2    # 删除槽位
CLUSTER FAILOVER                # 手动故障转移
```

**数据迁移：**
```bash
# 重新分片
redis-cli --cluster reshard 127.0.0.1:7000
redis-cli --cluster rebalance 127.0.0.1:7000

# 添加节点
redis-cli --cluster add-node new-node existing-node
redis-cli --cluster add-node new-node existing-node --cluster-slave

# 删除节点
redis-cli --cluster del-node host:port node-id
```

**客户端路由：**
- **MOVED**: 槽位已永久迁移到其他节点
- **ASK**: 槽位正在迁移过程中的临时重定向
- 智能客户端会缓存槽位映射，减少重定向

**集群限制：**
- 不支持多数据库，只能使用database 0
- 不支持跨节点的事务和Lua脚本
- 不支持跨节点的批量操作

---

## Java Redis 基础集成

### 1. Jedis 客户端

**依赖配置：**
```xml
<dependency>
    <groupId>redis.clients</groupId>
    <artifactId>jedis</artifactId>
    <version>4.3.1</version>
</dependency>
```

**基础连接：**
```java
// 单机连接
Jedis jedis = new Jedis("localhost", 6379);
jedis.auth("password");  // 如果设置了密码
jedis.select(0);         // 选择数据库

// 使用try-with-resources自动关闭连接
try (Jedis jedis = new Jedis("localhost", 6379)) {
    jedis.set("key", "value");
    String value = jedis.get("key");
}
```

**连接池配置：**
```java
// 连接池配置
JedisPoolConfig poolConfig = new JedisPoolConfig();
poolConfig.setMaxTotal(100);                    // 最大连接数
poolConfig.setMaxIdle(20);                      // 最大空闲连接
poolConfig.setMinIdle(5);                       // 最小空闲连接
poolConfig.setTestOnBorrow(true);               // 获取连接时测试
poolConfig.setTestOnReturn(true);               // 归还连接时测试
poolConfig.setTestWhileIdle(true);              // 空闲时测试
poolConfig.setMaxWaitMillis(3000);              // 获取连接最大等待时间
poolConfig.setTimeBetweenEvictionRunsMillis(30000); // 空闲连接检查间隔

// 创建连接池
JedisPool jedisPool = new JedisPool(poolConfig, "localhost", 6379, 2000, "password");

// 使用连接池
try (Jedis jedis = jedisPool.getResource()) {
    jedis.set("key", "value");
    return jedis.get("key");
}

// 关闭连接池
jedisPool.close();
```

**基本操作示例：**
```java
public class JedisBasicOperations {
    private JedisPool jedisPool;
    
    public JedisBasicOperations(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }
    
    // String 操作
    public void stringOperations() {
        try (Jedis jedis = jedisPool.getResource()) {
            // 基本set/get
            jedis.set("user:1:name", "张三");
            String name = jedis.get("user:1:name");
            
            // 带过期时间
            jedis.setex("session:abc123", 3600, "user_data");
            
            // 批量操作
            jedis.mset("key1", "value1", "key2", "value2");
            List<String> values = jedis.mget("key1", "key2");
            
            // 原子操作
            Long counter = jedis.incr("counter");
            jedis.incrBy("counter", 5);
        }
    }
    
    // Hash 操作
    public void hashOperations() {
        try (Jedis jedis = jedisPool.getResource()) {
            // 设置hash字段
            jedis.hset("user:1", "name", "张三");
            jedis.hset("user:1", "age", "25");
            
            // 批量设置
            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("name", "李四");
            userInfo.put("age", "30");
            userInfo.put("city", "北京");
            jedis.hmset("user:2", userInfo);
            
            // 获取hash值
            String name = jedis.hget("user:1", "name");
            Map<String, String> allInfo = jedis.hgetAll("user:1");
            
            // 原子递增
            jedis.hincrBy("user:1", "loginCount", 1);
        }
    }
    
    // List 操作
    public void listOperations() {
        try (Jedis jedis = jedisPool.getResource()) {
            // 推入元素
            jedis.lpush("messages", "msg1", "msg2", "msg3");
            jedis.rpush("queue", "task1", "task2");
            
            // 弹出元素
            String msg = jedis.lpop("messages");
            String task = jedis.rpop("queue");
            
            // 获取范围元素
            List<String> range = jedis.lrange("messages", 0, -1);
            
            // 阻塞操作
            List<String> result = jedis.blpop(10, "queue"); // 10秒超时
        }
    }
    
    // Set 操作
    public void setOperations() {
        try (Jedis jedis = jedisPool.getResource()) {
            // 添加元素
            jedis.sadd("tags", "java", "redis", "spring");
            jedis.sadd("user:1:tags", "java", "mysql");
            
            // 检查存在
            Boolean exists = jedis.sismember("tags", "java");
            
            // 集合运算
            Set<String> intersection = jedis.sinter("tags", "user:1:tags");
            Set<String> union = jedis.sunion("tags", "user:1:tags");
            
            // 随机元素
            String randomTag = jedis.srandmember("tags");
            Set<String> randomTags = jedis.srandmember("tags", 2);
        }
    }
    
    // Sorted Set 操作
    public void sortedSetOperations() {
        try (Jedis jedis = jedisPool.getResource()) {
            // 添加元素
            jedis.zadd("leaderboard", 100, "player1");
            jedis.zadd("leaderboard", 200, "player2");
            jedis.zadd("leaderboard", 150, "player3");
            
            // 获取排名
            Long rank = jedis.zrank("leaderboard", "player1");        // 正序排名
            Long revRank = jedis.zrevrank("leaderboard", "player1");  // 倒序排名
            
            // 范围查询
            Set<String> topPlayers = jedis.zrevrange("leaderboard", 0, 2); // 前3名
            Set<Tuple> playersWithScores = jedis.zrevrangeWithScores("leaderboard", 0, 2);
            
            // 按分数范围查询
            Set<String> players = jedis.zrangeByScore("leaderboard", 100, 200);
            
            // 增加分数
            jedis.zincrby("leaderboard", 50, "player1");
        }
    }
}
```

### 2. Lettuce 客户端

**依赖配置：**
```xml
<dependency>
    <groupId>io.lettuce</groupId>
    <artifactId>lettuce-core</artifactId>
    <version>6.2.4.RELEASE</version>
</dependency>
```

**基础连接：**
```java
// 创建Redis URI
RedisURI redisUri = RedisURI.Builder
    .redis("localhost")
    .withPort(6379)
    .withPassword("password")
    .withDatabase(0)
    .withTimeout(Duration.ofSeconds(10))
    .build();

// 创建客户端
RedisClient redisClient = RedisClient.create(redisUri);

// 获取连接 (同步)
StatefulRedisConnection<String, String> connection = redisClient.connect();
RedisCommands<String, String> syncCommands = connection.sync();

// 异步连接
RedisAsyncCommands<String, String> asyncCommands = connection.async();

// 响应式连接  
RedisReactiveCommands<String, String> reactiveCommands = connection.reactive();

// 关闭连接
connection.close();
redisClient.shutdown();
```

**连接池配置：**
```java
// 连接池配置
GenericObjectPoolConfig<StatefulRedisConnection<String, String>> poolConfig = 
    new GenericObjectPoolConfig<>();
poolConfig.setMaxTotal(100);
poolConfig.setMaxIdle(20);
poolConfig.setMinIdle(5);
poolConfig.setTestOnBorrow(true);

// 创建连接池
ConnectionPoolSupport.createGenericObjectPool(() -> redisClient.connect(), poolConfig);
```

**同步操作示例：**
```java
public class LettuceBasicOperations {
    private StatefulRedisConnection<String, String> connection;
    private RedisCommands<String, String> syncCommands;
    
    public LettuceBasicOperations(RedisClient redisClient) {
        this.connection = redisClient.connect();
        this.syncCommands = connection.sync();
    }
    
    public void basicOperations() {
        // String 操作
        syncCommands.set("key", "value");
        String value = syncCommands.get("key");
        
        // Hash 操作
        syncCommands.hset("user:1", "name", "张三");
        String name = syncCommands.hget("user:1", "name");
        
        // List 操作
        syncCommands.lpush("queue", "item1", "item2");
        String item = syncCommands.rpop("queue");
        
        // 管道操作
        syncCommands.setAutoFlushCommands(false);
        RedisFuture<String> future1 = syncCommands.set("key1", "value1");
        RedisFuture<String> future2 = syncCommands.set("key2", "value2");
        syncCommands.flushCommands();
        
        // 等待结果
        try {
            String result1 = future1.get();
            String result2 = future2.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        syncCommands.setAutoFlushCommands(true);
    }
}
```

**异步操作示例：**
```java
public class LettuceAsyncOperations {
    private RedisAsyncCommands<String, String> asyncCommands;
    
    public LettuceAsyncOperations(RedisClient redisClient) {
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        this.asyncCommands = connection.async();
    }
    
    public CompletableFuture<String> asyncGet(String key) {
        RedisFuture<String> future = asyncCommands.get(key);
        
        // 转换为CompletableFuture
        return future.toCompletableFuture();
    }
    
    public void asyncOperationsWithCallback() {
        // 设置值并处理回调
        RedisFuture<String> setFuture = asyncCommands.set("async_key", "async_value");
        
        setFuture.thenAccept(result -> {
            System.out.println("Set result: " + result);
            
            // 在设置成功后获取值
            RedisFuture<String> getFuture = asyncCommands.get("async_key");
            getFuture.thenAccept(value -> {
                System.out.println("Got value: " + value);
            });
        });
        
        // 异常处理
        setFuture.exceptionally(throwable -> {
            System.err.println("Error: " + throwable.getMessage());
            return null;
        });
    }
}
```

**响应式操作示例：**
```java
public class LettuceReactiveOperations {
    private RedisReactiveCommands<String, String> reactiveCommands;
    
    public LettuceReactiveOperations(RedisClient redisClient) {
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        this.reactiveCommands = connection.reactive();
    }
    
    public void reactiveOperations() {
        // 单个操作
        Mono<String> setMono = reactiveCommands.set("reactive_key", "reactive_value");
        Mono<String> getMono = reactiveCommands.get("reactive_key");
        
        // 链式操作
        setMono.then(getMono)
               .subscribe(value -> System.out.println("Value: " + value));
        
        // 批量操作
        Flux<String> keys = Flux.just("key1", "key2", "key3");
        Flux<String> values = keys.flatMap(key -> reactiveCommands.get(key));
        
        values.subscribe(
            value -> System.out.println("Value: " + value),
            error -> System.err.println("Error: " + error),
            () -> System.out.println("Complete")
        );
        
        // 发布订阅
        StatefulRedisPubSubConnection<String, String> pubSubConnection = 
            RedisClient.create().connectPubSub();
        RedisPubSubReactiveCommands<String, String> pubSubCommands = 
            pubSubConnection.reactive();
        
        // 订阅
        pubSubCommands.observeChannels()
                     .filter(message -> "channel1".equals(message.getChannel()))
                     .map(ChannelMessage::getMessage)
                     .subscribe(message -> System.out.println("Received: " + message));
        
        // 发布
        reactiveCommands.publish("channel1", "Hello World")
                       .subscribe(subscribers -> System.out.println("Sent to " + subscribers + " subscribers"));
    }
}
```

### 3. 连接管理最佳实践

**连接池优化：**
```java
@Configuration
public class RedisConfig {
    
    @Bean
    public JedisPool jedisPool() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        
        // 连接池大小配置
        poolConfig.setMaxTotal(100);                    // 最大连接数
        poolConfig.setMaxIdle(20);                      // 最大空闲连接
        poolConfig.setMinIdle(5);                       // 最小空闲连接
        
        // 连接验证
        poolConfig.setTestOnBorrow(true);               // 获取时验证
        poolConfig.setTestOnReturn(true);               // 归还时验证
        poolConfig.setTestWhileIdle(true);              // 空闲时验证
        
        // 超时配置
        poolConfig.setMaxWaitMillis(3000);              // 获取连接超时
        poolConfig.setSoftMinEvictableIdleTimeMillis(300000);  // 空闲连接最小生存时间
        poolConfig.setTimeBetweenEvictionRunsMillis(30000);    // 空闲检查间隔
        
        // JMX监控
        poolConfig.setJmxEnabled(true);
        poolConfig.setJmxNamePrefix("redis-pool");
        
        return new JedisPool(poolConfig, "localhost", 6379, 2000, "password", 0);
    }
    
    @Bean  
    public RedisClient lettuceClient() {
        return RedisClient.create("redis://password@localhost:6379/0");
    }
    
    @PreDestroy
    public void cleanup() {
        jedisPool().close();
        lettuceClient().shutdown();
    }
}
```

**异常处理：**
```java
public class RedisService {
    private JedisPool jedisPool;
    
    public String safeGet(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        } catch (JedisConnectionException e) {
            // 连接异常处理
            log.error("Redis connection error for key: " + key, e);
            return null;
        } catch (JedisDataException e) {
            // 数据异常处理
            log.error("Redis data error for key: " + key, e);
            return null;
        } catch (Exception e) {
            // 其他异常
            log.error("Unexpected redis error for key: " + key, e);
            return null;
        }
    }
    
    public String getWithRetry(String key, int maxRetries) {
        for (int i = 0; i < maxRetries; i++) {
            try (Jedis jedis = jedisPool.getResource()) {
                return jedis.get(key);
            } catch (Exception e) {
                if (i == maxRetries - 1) {
                    throw e;  // 最后一次重试失败，抛出异常
                }
                try {
                    Thread.sleep(100 * (i + 1));  // 指数退避
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(ie);
                }
            }
        }
        return null;
    }
}

---

## Spring Redis 集成

### 1. Spring Data Redis 配置

**依赖配置：**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-pool2</artifactId>
</dependency>
```

**application.yml 配置：**
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: password
    database: 0
    timeout: 3000
    lettuce:
      pool:
        max-active: 100    # 最大连接数
        max-idle: 20       # 最大空闲连接
        min-idle: 5        # 最小空闲连接
        max-wait: 3000     # 获取连接最大等待时间
        time-between-eviction-runs: 30000  # 空闲检查间隔
```

**Redis 配置类：**
```java
@Configuration
@EnableCaching
public class RedisConfig {
    
    @Bean
    public LettuceConnectionFactory connectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName("localhost");
        config.setPort(6379);
        config.setPassword("password");
        config.setDatabase(0);
        
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
            .commandTimeout(Duration.ofSeconds(3))
            .poolConfig(getPoolConfig())
            .build();
            
        return new LettuceConnectionFactory(config, clientConfig);
    }
    
    private GenericObjectPoolConfig<StatefulRedisConnection<String, String>> getPoolConfig() {
        GenericObjectPoolConfig<StatefulRedisConnection<String, String>> poolConfig = 
            new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(100);
        poolConfig.setMaxIdle(20);
        poolConfig.setMinIdle(5);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        return poolConfig;
    }
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // 设置序列化器
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }
    
    @Bean
    public StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        return template;
    }
    
    @Bean
    public CacheManager cacheManager(LettuceConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))  // 默认过期时间30分钟
            .disableCachingNullValues()        // 不缓存null值
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
                
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build();
    }
}
```

### 2. RedisTemplate 使用

**基础操作：**
```java
@Service
public class RedisService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    // String 操作
    public void stringOperations() {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        
        // 基本set/get
        ops.set("user:1", new User("张三", 25));
        User user = (User) ops.get("user:1");
        
        // 带过期时间
        ops.set("session:abc", "sessionData", Duration.ofHours(1));
        
        // 批量操作
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        ops.multiSet(map);
        
        List<Object> values = ops.multiGet(Arrays.asList("key1", "key2"));
        
        // 原子操作
        Long counter = ops.increment("counter");
        Long result = ops.increment("counter", 5);
        
        // 仅当key不存在时设置
        Boolean success = ops.setIfAbsent("lock:resource", "locked", Duration.ofMinutes(5));
    }
    
    // Hash 操作
    public void hashOperations() {
        HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
        
        // 单个字段操作
        hashOps.put("user:1", "name", "张三");
        hashOps.put("user:1", "age", 25);
        String name = (String) hashOps.get("user:1", "name");
        
        // 批量操作
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", "李四");
        userMap.put("age", 30);
        userMap.put("city", "北京");
        hashOps.putAll("user:2", userMap);
        
        Map<String, Object> allFields = hashOps.entries("user:2");
        List<Object> fieldValues = hashOps.multiGet("user:2", Arrays.asList("name", "age"));
        
        // 原子递增
        hashOps.increment("user:1", "loginCount", 1);
        
        // 仅当字段不存在时设置
        Boolean success = hashOps.putIfAbsent("user:1", "email", "zhang@example.com");
    }
    
    // List 操作
    public void listOperations() {
        ListOperations<String, Object> listOps = redisTemplate.opsForList();
        
        // 推入元素
        listOps.leftPushAll("messages", "msg1", "msg2", "msg3");
        listOps.rightPush("queue", "task1");
        
        // 弹出元素
        Object leftMsg = listOps.leftPop("messages");
        Object rightTask = listOps.rightPop("queue");
        
        // 阻塞操作
        Object blockedMsg = listOps.leftPop("messages", Duration.ofSeconds(10));
        
        // 范围操作
        List<Object> range = listOps.range("messages", 0, -1);
        listOps.trim("messages", 0, 99);  // 保留前100个元素
        
        // 按值删除
        listOps.remove("messages", 1, "msg2");  // 删除1个"msg2"
    }
    
    // Set 操作
    public void setOperations() {
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        
        // 添加元素
        setOps.add("tags", "java", "redis", "spring");
        setOps.add("user:1:tags", "java", "mysql");
        
        // 检查存在
        Boolean isMember = setOps.isMember("tags", "java");
        
        // 集合运算
        Set<Object> intersection = setOps.intersect("tags", "user:1:tags");
        Set<Object> union = setOps.union("tags", "user:1:tags");
        Set<Object> difference = setOps.difference("tags", "user:1:tags");
        
        // 随机元素
        Object randomTag = setOps.randomMember("tags");
        List<Object> randomTags = setOps.randomMembers("tags", 3);
        
        // 弹出元素
        Object poppedTag = setOps.pop("tags");
    }
    
    // Sorted Set 操作
    public void sortedSetOperations() {
        ZSetOperations<String, Object> zsetOps = redisTemplate.opsForZSet();
        
        // 添加元素
        zsetOps.add("leaderboard", "player1", 100);
        zsetOps.add("leaderboard", "player2", 200);
        zsetOps.add("leaderboard", "player3", 150);
        
        // 批量添加
        Set<ZSetOperations.TypedTuple<Object>> tuples = new HashSet<>();
        tuples.add(new DefaultTypedTuple<>("player4", 180.0));
        tuples.add(new DefaultTypedTuple<>("player5", 120.0));
        zsetOps.add("leaderboard", tuples);
        
        // 获取排名和分数
        Long rank = zsetOps.rank("leaderboard", "player1");
        Long reverseRank = zsetOps.reverseRank("leaderboard", "player1");
        Double score = zsetOps.score("leaderboard", "player1");
        
        // 范围查询
        Set<Object> topPlayers = zsetOps.reverseRange("leaderboard", 0, 2);
        Set<ZSetOperations.TypedTuple<Object>> topPlayersWithScores = 
            zsetOps.reverseRangeWithScores("leaderboard", 0, 2);
        
        // 按分数范围查询
        Set<Object> players = zsetOps.rangeByScore("leaderboard", 100, 200);
        
        // 增加分数
        zsetOps.incrementScore("leaderboard", "player1", 50);
        
        // 计数
        Long count = zsetOps.count("leaderboard", 100, 200);
    }
    
    // 通用操作
    public void generalOperations() {
        // 设置过期时间
        redisTemplate.expire("key", Duration.ofMinutes(30));
        redisTemplate.expireAt("key", Instant.now().plus(Duration.ofHours(1)));
        
        // 获取TTL
        Long ttl = redisTemplate.getExpire("key");
        Long ttlSeconds = redisTemplate.getExpire("key", TimeUnit.SECONDS);
        
        // 删除key
        Boolean deleted = redisTemplate.delete("key");
        Long deletedCount = redisTemplate.delete(Arrays.asList("key1", "key2"));
        
        // 检查key是否存在
        Boolean exists = redisTemplate.hasKey("key");
        Long existsCount = redisTemplate.countExistingKeys(Arrays.asList("key1", "key2"));
        
        // 获取key类型
        DataType type = redisTemplate.type("key");
        
        // 模糊匹配key
        Set<String> keys = redisTemplate.keys("user:*");
    }
}
```

### 3. Spring Cache 注解

**缓存注解使用：**
```java
@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    // 缓存结果，key为user:#{id}
    @Cacheable(value = "users", key = "#id")
    public User findById(Long id) {
        return userRepository.findById(id);
    }
    
    // 自定义key生成策略
    @Cacheable(value = "users", key = "#user.id + ':' + #user.name")
    public User findByIdAndName(User user) {
        return userRepository.findByIdAndName(user.getId(), user.getName());
    }
    
    // 条件缓存，只有当result不为null时才缓存
    @Cacheable(value = "users", key = "#id", condition = "#id > 0", unless = "#result == null")
    public User findByIdConditional(Long id) {
        return userRepository.findById(id);
    }
    
    // 更新缓存
    @CachePut(value = "users", key = "#user.id")
    public User save(User user) {
        return userRepository.save(user);
    }
    
    // 删除缓存
    @CacheEvict(value = "users", key = "#id")
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
    
    // 删除所有缓存
    @CacheEvict(value = "users", allEntries = true)
    public void deleteAll() {
        userRepository.deleteAll();
    }
    
    // 组合注解，先删除再更新
    @Caching(
        evict = @CacheEvict(value = "users", key = "#user.id"),
        put = @CachePut(value = "users", key = "#user.id")
    )
    public User update(User user) {
        return userRepository.save(user);
    }
}
```

**自定义缓存配置：**
```java
@Configuration
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager(LettuceConnectionFactory connectionFactory) {
        // 默认配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .disableCachingNullValues()
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
        
        // 针对不同缓存的个性化配置
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // 用户缓存：1小时过期
        cacheConfigurations.put("users", defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // 商品缓存：10分钟过期
        cacheConfigurations.put("products", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        
        // 配置缓存：永不过期
        cacheConfigurations.put("config", defaultConfig.entryTtl(Duration.ZERO));
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build();
    }
    
    @Bean
    public KeyGenerator customKeyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getSimpleName()).append(".");
            sb.append(method.getName()).append(":");
            for (Object param : params) {
                sb.append(param.toString()).append(",");
            }
            return sb.toString();
        };
    }
}
```

**缓存预热：**
```java
@Component
public class CacheWarmup implements ApplicationRunner {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ProductService productService;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 预热用户缓存
        warmupUserCache();
        
        // 预热商品缓存
        warmupProductCache();
    }
    
    private void warmupUserCache() {
        // 获取热门用户ID列表
        List<Long> hotUserIds = getHotUserIds();
        
        // 并行预热
        hotUserIds.parallelStream().forEach(id -> {
            try {
                userService.findById(id);
            } catch (Exception e) {
                log.warn("Failed to warmup user cache for id: " + id, e);
            }
        });
    }
    
    private void warmupProductCache() {
        // 预热商品缓存逻辑
        List<Long> hotProductIds = getHotProductIds();
        hotProductIds.forEach(id -> productService.findById(id));
    }
}

---

## Java Redis 高级模式

### 1. 分布式锁实现

**基于SET命令的分布式锁：**
```java
@Component
public class RedisDistributedLock {
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    private static final String LOCK_PREFIX = "lock:";
    private static final String UNLOCK_SCRIPT = 
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "return redis.call('del', KEYS[1]) else return 0 end";
    
    /**
     * 获取分布式锁
     * @param key 锁的key
     * @param value 锁的值（通常是UUID）
     * @param expireTime 过期时间（秒）
     * @return 是否获取成功
     */
    public boolean lock(String key, String value, long expireTime) {
        String lockKey = LOCK_PREFIX + key;
        
        // SET key value NX EX expireTime
        Boolean result = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, value, Duration.ofSeconds(expireTime));
            
        return Boolean.TRUE.equals(result);
    }
    
    /**
     * 释放分布式锁
     * @param key 锁的key
     * @param value 锁的值
     * @return 是否释放成功
     */
    public boolean unlock(String key, String value) {
        String lockKey = LOCK_PREFIX + key;
        
        // 使用Lua脚本确保原子性
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(UNLOCK_SCRIPT);
        script.setResultType(Long.class);
        
        Long result = redisTemplate.execute(script, 
            Collections.singletonList(lockKey), value);
            
        return Long.valueOf(1).equals(result);
    }
    
    /**
     * 尝试获取锁，支持重试
     * @param key 锁的key
     * @param value 锁的值
     * @param expireTime 过期时间
     * @param retryTimes 重试次数
     * @param retryInterval 重试间隔（毫秒）
     * @return 是否获取成功
     */
    public boolean tryLock(String key, String value, long expireTime, 
                          int retryTimes, long retryInterval) {
        for (int i = 0; i < retryTimes; i++) {
            if (lock(key, value, expireTime)) {
                return true;
            }
            
            try {
                Thread.sleep(retryInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }
}
```

**基于Redisson的分布式锁：**
```java
@Configuration
public class RedissonConfig {
    
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        
        // 单机配置
        config.useSingleServer()
              .setAddress("redis://localhost:6379")
              .setPassword("password")
              .setConnectionPoolSize(50)
              .setConnectionMinimumIdleSize(10)
              .setIdleConnectionTimeout(10000)
              .setConnectTimeout(10000)
              .setRetryAttempts(3)
              .setRetryInterval(1500);
              
        return Redisson.create(config);
    }
}

@Service
public class RedissonLockService {
    
    @Autowired
    private RedissonClient redissonClient;
    
    /**
     * 可重入锁
     */
    public void reentrantLockExample() {
        RLock lock = redissonClient.getLock("myLock");
        
        try {
            // 尝试获取锁，最多等待10秒，锁定后30秒自动释放
            if (lock.tryLock(10, 30, TimeUnit.SECONDS)) {
                try {
                    // 业务逻辑
                    System.out.println("执行业务逻辑");
                    
                    // 可重入测试
                    nestedMethod();
                    
                } finally {
                    lock.unlock();
                }
            } else {
                System.out.println("获取锁失败");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void nestedMethod() {
        RLock lock = redissonClient.getLock("myLock");
        lock.lock(); // 可重入
        try {
            System.out.println("嵌套方法执行");
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * 读写锁
     */
    public void readWriteLockExample() {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("rwLock");
        
        // 读锁
        RLock readLock = readWriteLock.readLock();
        // 写锁  
        RLock writeLock = readWriteLock.writeLock();
        
        // 读操作
        readLock.lock();
        try {
            System.out.println("读取数据");
        } finally {
            readLock.unlock();
        }
        
        // 写操作
        writeLock.lock();
        try {
            System.out.println("写入数据");
        } finally {
            writeLock.unlock();
        }
    }
    
    /**
     * 信号量
     */
    public void semaphoreExample() {
        RSemaphore semaphore = redissonClient.getSemaphore("mySemaphore");
        
        try {
            // 设置许可证数量
            semaphore.trySetPermits(10);
            
            // 获取许可证
            if (semaphore.tryAcquire(1, 5, TimeUnit.SECONDS)) {
                try {
                    System.out.println("获得许可证，执行业务逻辑");
                } finally {
                    semaphore.release(); // 释放许可证
                }
            } else {
                System.out.println("获取许可证超时");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 倒计时锁存器
     */
    public void countDownLatchExample() {
        RCountDownLatch latch = redissonClient.getCountDownLatch("myLatch");
        
        // 设置计数
        latch.trySetCount(5);
        
        // 等待计数归零
        try {
            latch.await(30, TimeUnit.SECONDS);
            System.out.println("所有任务完成");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
```

### 2. 限流算法实现

**滑动窗口限流：**
```java
@Component
public class SlidingWindowRateLimiter {
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    private static final String SLIDING_WINDOW_SCRIPT =
        "local key = KEYS[1] " +
        "local window = tonumber(ARGV[1]) " +
        "local limit = tonumber(ARGV[2]) " +
        "local current = tonumber(ARGV[3]) " +
        "redis.call('zremrangebyscore', key, 0, current - window) " +
        "local currentCount = redis.call('zcard', key) " +
        "if currentCount < limit then " +
        "    redis.call('zadd', key, current, current) " +
        "    redis.call('expire', key, window) " +
        "    return 1 " +
        "else " +
        "    return 0 " +
        "end";
    
    /**
     * 滑动窗口限流
     * @param key 限流key
     * @param windowSize 窗口大小（秒）
     * @param limit 限制次数
     * @return 是否允许通过
     */
    public boolean isAllowed(String key, int windowSize, int limit) {
        long current = System.currentTimeMillis();
        
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(SLIDING_WINDOW_SCRIPT);
        script.setResultType(Long.class);
        
        Long result = redisTemplate.execute(script, 
            Collections.singletonList("rate_limit:" + key),
            String.valueOf(windowSize * 1000),
            String.valueOf(limit),
            String.valueOf(current));
            
        return Long.valueOf(1).equals(result);
    }
}
```

**令牌桶限流：**
```java
@Component
public class TokenBucketRateLimiter {
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    private static final String TOKEN_BUCKET_SCRIPT =
        "local key = KEYS[1] " +
        "local capacity = tonumber(ARGV[1]) " +
        "local tokens = tonumber(ARGV[2]) " +
        "local interval = tonumber(ARGV[3]) " +
        "local current = tonumber(ARGV[4]) " +
        "local bucket = redis.call('hmget', key, 'tokens', 'last_refill') " +
        "local currentTokens = tonumber(bucket[1]) or capacity " +
        "local lastRefill = tonumber(bucket[2]) or current " +
        "local elapsed = current - lastRefill " +
        "local tokensToAdd = math.floor(elapsed / interval) * tokens " +
        "currentTokens = math.min(capacity, currentTokens + tokensToAdd) " +
        "if currentTokens >= 1 then " +
        "    currentTokens = currentTokens - 1 " +
        "    redis.call('hmset', key, 'tokens', currentTokens, 'last_refill', current) " +
        "    redis.call('expire', key, capacity * interval / 1000) " +
        "    return 1 " +
        "else " +
        "    redis.call('hmset', key, 'tokens', currentTokens, 'last_refill', current) " +
        "    redis.call('expire', key, capacity * interval / 1000) " +
        "    return 0 " +
        "end";
    
    /**
     * 令牌桶限流
     * @param key 限流key
     * @param capacity 桶容量
     * @param refillRate 填充速率（每秒）
     * @return 是否允许通过
     */
    public boolean isAllowed(String key, int capacity, double refillRate) {
        long current = System.currentTimeMillis();
        long interval = (long) (1000 / refillRate); // 每个令牌的时间间隔
        
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(TOKEN_BUCKET_SCRIPT);
        script.setResultType(Long.class);
        
        Long result = redisTemplate.execute(script,
            Collections.singletonList("token_bucket:" + key),
            String.valueOf(capacity),
            "1", // 每次填充1个令牌
            String.valueOf(interval),
            String.valueOf(current));
            
        return Long.valueOf(1).equals(result);
    }
}
```

### 3. 缓存策略实现

**多级缓存：**
```java
@Component
public class MultiLevelCache {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // 本地缓存（Caffeine）
    private final Cache<String, Object> localCache = Caffeine.newBuilder()
        .maximumSize(10000)
        .expireAfterWrite(Duration.ofMinutes(10))
        .recordStats()
        .build();
    
    /**
     * 多级缓存获取
     */
    public Object get(String key) {
        // L1: 本地缓存
        Object value = localCache.getIfPresent(key);
        if (value != null) {
            return value;
        }
        
        // L2: Redis缓存
        value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            localCache.put(key, value);
            return value;
        }
        
        return null;
    }
    
    /**
     * 多级缓存设置
     */
    public void put(String key, Object value, Duration ttl) {
        // 同时设置本地缓存和Redis缓存
        localCache.put(key, value);
        redisTemplate.opsForValue().set(key, value, ttl);
    }
    
    /**
     * 多级缓存删除
     */
    public void evict(String key) {
        localCache.invalidate(key);
        redisTemplate.delete(key);
    }
    
    /**
     * 缓存穿透防护（空值缓存）
     */
    public Object getWithNullCache(String key, Supplier<Object> loader) {
        Object value = get(key);
        
        if (value != null) {
            // 检查是否是空值标记
            if ("NULL".equals(value)) {
                return null;
            }
            return value;
        }
        
        // 从数据源加载
        value = loader.get();
        
        if (value != null) {
            put(key, value, Duration.ofHours(1));
        } else {
            // 缓存空值，防止缓存穿透
            put(key, "NULL", Duration.ofMinutes(5));
        }
        
        return value;
    }
    
    /**
     * 缓存击穿防护（分布式锁）
     */
    public Object getWithMutex(String key, Supplier<Object> loader) {
        Object value = get(key);
        if (value != null) {
            return value;
        }
        
        String lockKey = "lock:" + key;
        String lockValue = UUID.randomUUID().toString();
        
        // 尝试获取锁
        Boolean lockAcquired = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, lockValue, Duration.ofSeconds(10));
            
        if (Boolean.TRUE.equals(lockAcquired)) {
            try {
                // 再次检查缓存
                value = get(key);
                if (value != null) {
                    return value;
                }
                
                // 从数据源加载
                value = loader.get();
                if (value != null) {
                    put(key, value, Duration.ofHours(1));
                }
                
                return value;
            } finally {
                // 释放锁
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
                redisScript.setScriptText(script);
                redisScript.setResultType(Long.class);
                redisTemplate.execute(redisScript, Collections.singletonList(lockKey), lockValue);
            }
        } else {
            // 未获取到锁，等待片刻后重试
            try {
                Thread.sleep(100);
                return getWithMutex(key, loader);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return loader.get();
            }
        }
    }
}
```

### 4. 发布订阅模式

**基于RedisTemplate的发布订阅：**
```java
@Configuration
public class RedisPubSubConfig {
    
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            LettuceConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        
        // 添加消息监听器
        container.addMessageListener(new UserMessageListener(), new ChannelTopic("user-events"));
        container.addMessageListener(new OrderMessageListener(), new ChannelTopic("order-events"));
        
        // 模式订阅
        container.addMessageListener(new PatternMessageListener(), new PatternTopic("system.*"));
        
        return container;
    }
}

@Component
public class UserMessageListener implements MessageListener {
    
    private static final Logger logger = LoggerFactory.getLogger(UserMessageListener.class);
    
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
        String body = new String(message.getBody());
        
        logger.info("Received message from channel {}: {}", channel, body);
        
        try {
            // 解析消息
            UserEvent event = JSON.parseObject(body, UserEvent.class);
            
            // 处理事件
            handleUserEvent(event);
            
        } catch (Exception e) {
            logger.error("Error processing message", e);
        }
    }
    
    private void handleUserEvent(UserEvent event) {
        switch (event.getType()) {
            case "USER_REGISTERED":
                handleUserRegistered(event);
                break;
            case "USER_LOGIN":
                handleUserLogin(event);
                break;
            default:
                logger.warn("Unknown event type: {}", event.getType());
        }
    }
}

@Component
public class MessagePublisher {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    public void publishUserEvent(UserEvent event) {
        String message = JSON.toJSONString(event);
        redisTemplate.convertAndSend("user-events", message);
    }
    
    public void publishOrderEvent(OrderEvent event) {
        String message = JSON.toJSONString(event);
        redisTemplate.convertAndSend("order-events", message);
    }
    
    public void publishSystemNotification(String type, String message) {
        String channel = "system." + type;
        redisTemplate.convertAndSend(channel, message);
    }
}
```

### 5. Redis事务与管道

**事务操作：**
```java
@Service
public class RedisTransactionService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 基本事务操作
     */
    public void basicTransaction() {
        redisTemplate.execute(new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                operations.multi(); // 开始事务
                
                operations.opsForValue().set("key1", "value1");
                operations.opsForValue().set("key2", "value2");
                operations.opsForHash().put("hash1", "field1", "value1");
                
                return operations.exec(); // 提交事务
            }
        });
    }
    
    /**
     * 带WATCH的事务（乐观锁）
     */
    public boolean transferBalance(String fromAccount, String toAccount, double amount) {
        return redisTemplate.execute(new SessionCallback<Boolean>() {
            @Override
            public Boolean execute(RedisOperations operations) throws DataAccessException {
                String fromKey = "account:" + fromAccount;
                String toKey = "account:" + toAccount;
                
                // 监视账户余额
                operations.watch(fromKey);
                
                // 检查余额
                Double fromBalance = (Double) operations.opsForValue().get(fromKey);
                if (fromBalance == null || fromBalance < amount) {
                    operations.unwatch();
                    return false;
                }
                
                // 开始事务
                operations.multi();
                operations.opsForValue().set(fromKey, fromBalance - amount);
                operations.opsForValue().increment(toKey, amount);
                
                // 执行事务
                List<Object> results = operations.exec();
                return results != null && !results.isEmpty();
            }
        });
    }
    
    /**
     * 管道操作
     */
    public void pipelineOperations() {
        List<Object> results = redisTemplate.executePipelined(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                StringRedisConnection stringRedisConn = (StringRedisConnection) connection;
                
                for (int i = 0; i < 1000; i++) {
                    stringRedisConn.set("key" + i, "value" + i);
                }
                
                return null;
            }
        });
        
        System.out.println("Pipeline results size: " + results.size());
    }
    
    /**
     * Lua脚本执行
     */
    public Long executeAtomicIncrement(String key, long increment, long max) {
        String script = 
            "local current = redis.call('get', KEYS[1]) " +
            "if current == false then current = 0 else current = tonumber(current) end " +
            "if current + ARGV[1] <= tonumber(ARGV[2]) then " +
            "    return redis.call('incrby', KEYS[1], ARGV[1]) " +
            "else " +
            "    return current " +
            "end";
            
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(script);
        redisScript.setResultType(Long.class);
        
        return redisTemplate.execute(redisScript, 
            Collections.singletonList(key),
            String.valueOf(increment),
            String.valueOf(max));
    }
}
```

### 6. 性能优化技巧

**连接池优化：**
```java
@Configuration
public class RedisPerformanceConfig {
    
    @Bean
    public LettuceConnectionFactory optimizedConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName("localhost");
        config.setPort(6379);
        
        // 连接池配置优化
        GenericObjectPoolConfig<StatefulRedisConnection<String, String>> poolConfig = 
            new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(200);          // 根据并发量调整
        poolConfig.setMaxIdle(50);            // 保持足够的空闲连接
        poolConfig.setMinIdle(20);            // 预热连接池
        poolConfig.setTestOnBorrow(false);    // 关闭借用时测试，提高性能
        poolConfig.setTestOnReturn(false);    // 关闭归还时测试
        poolConfig.setTestWhileIdle(true);    // 保留空闲时测试
        poolConfig.setBlockWhenExhausted(true); // 连接耗尽时阻塞
        poolConfig.setMaxWaitMillis(3000);    // 最大等待时间
        
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
            .commandTimeout(Duration.ofSeconds(5))        // 命令超时
            .shutdownTimeout(Duration.ofSeconds(100))     // 关闭超时
            .poolConfig(poolConfig)
            .build();
            
        return new LettuceConnectionFactory(config, clientConfig);
    }
    
    /**
     * 批量操作优化
     */
    @Component
    public static class RedisBatchOperations {
        
        @Autowired
        private RedisTemplate<String, Object> redisTemplate;
        
        /**
         * 批量设置（使用管道）
         */
        public void batchSet(Map<String, Object> keyValues) {
            redisTemplate.executePipelined(new RedisCallback<Object>() {
                @Override
                public Object doInRedis(RedisConnection connection) throws DataAccessException {
                    keyValues.forEach((key, value) -> {
                        redisTemplate.opsForValue().set(key, value);
                    });
                    return null;
                }
            });
        }
        
        /**
         * 批量获取（使用MGET）
         */
        public List<Object> batchGet(Collection<String> keys) {
            return redisTemplate.opsForValue().multiGet(keys);
        }
        
        /**
         * 批量删除
         */
        public void batchDelete(Collection<String> keys) {
            // 分批删除，避免单次删除过多key
            List<String> keyList = new ArrayList<>(keys);
            int batchSize = 1000;
            
            for (int i = 0; i < keyList.size(); i += batchSize) {
                int end = Math.min(i + batchSize, keyList.size());
                List<String> batch = keyList.subList(i, end);
                redisTemplate.delete(batch);
            }
        }
    }
}

---

## 生产环境最佳实践

### 1. 监控与告警

**Redis监控指标：**
```java
@Component
public class RedisMonitor {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private MeterRegistry meterRegistry;
    
    /**
     * 连接池监控
     */
    @EventListener
    @Async
    public void monitorConnectionPool() {
        LettuceConnectionFactory factory = (LettuceConnectionFactory) redisTemplate.getConnectionFactory();
        
        // 监控连接池状态
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            RedisConnection connection = factory.getConnection();
            Properties info = connection.info("clients");
            
            // 记录连接数指标
            Gauge.builder("redis.connected_clients")
                 .description("Redis connected clients count")
                 .register(meterRegistry, info, props -> 
                     Double.parseDouble(props.getProperty("connected_clients", "0")));
                     
        } catch (Exception e) {
            Counter.builder("redis.connection_errors")
                   .description("Redis connection errors")
                   .register(meterRegistry)
                   .increment();
        } finally {
            sample.stop(Timer.builder("redis.connection_time")
                           .description("Redis connection time")
                           .register(meterRegistry));
        }
    }
    
    /**
     * 内存使用监控
     */
    @Scheduled(fixedRate = 30000) // 每30秒执行一次
    public void monitorMemoryUsage() {
        try (RedisConnection connection = redisTemplate.getConnectionFactory().getConnection()) {
            Properties info = connection.info("memory");
            
            // 已使用内存
            long usedMemory = Long.parseLong(info.getProperty("used_memory", "0"));
            Gauge.builder("redis.memory.used")
                 .description("Redis used memory in bytes")
                 .register(meterRegistry, usedMemory, Double::valueOf);
            
            // 内存使用率
            long maxMemory = Long.parseLong(info.getProperty("maxmemory", "0"));
            if (maxMemory > 0) {
                double memoryUsageRatio = (double) usedMemory / maxMemory;
                Gauge.builder("redis.memory.usage_ratio")
                     .description("Redis memory usage ratio")
                     .register(meterRegistry, memoryUsageRatio, Double::valueOf);
                     
                // 内存使用率告警
                if (memoryUsageRatio > 0.8) {
                    sendAlert("Redis memory usage high: " + String.format("%.2f%%", memoryUsageRatio * 100));
                }
            }
            
            // 内存碎片率
            long usedMemoryRss = Long.parseLong(info.getProperty("used_memory_rss", "0"));
            double fragmentationRatio = (double) usedMemoryRss / usedMemory;
            Gauge.builder("redis.memory.fragmentation_ratio")
                 .description("Redis memory fragmentation ratio")
                 .register(meterRegistry, fragmentationRatio, Double::valueOf);
                 
        } catch (Exception e) {
            log.error("Failed to monitor Redis memory usage", e);
        }
    }
    
    private void sendAlert(String message) {
        // 发送告警通知（邮件、钉钉、Slack等）
        log.warn("Redis Alert: {}", message);
    }
}
```

### 2. 容灾与备份策略

**自动备份配置：**
```java
@Configuration
@EnableScheduling
public class RedisBackupConfig {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Value("${redis.backup.path}")
    private String backupPath;
    
    /**
     * 定期备份Redis数据
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void scheduledBackup() {
        performBackup();
    }
    
    public void performBackup() {
        try (RedisConnection connection = redisTemplate.getConnectionFactory().getConnection()) {
            
            // 触发BGSAVE
            connection.bgSave();
            
            // 等待备份完成
            boolean backupCompleted = waitForBackupCompletion(connection);
            
            if (backupCompleted) {
                // 复制RDB文件到备份目录
                copyRdbFile();
                
                // 清理旧备份文件
                cleanupOldBackups();
                
                log.info("Redis backup completed successfully");
            } else {
                log.error("Redis backup failed or timed out");
            }
            
        } catch (Exception e) {
            log.error("Redis backup failed", e);
        }
    }
    
    private boolean waitForBackupCompletion(RedisConnection connection) {
        int maxWaitTime = 300; // 最多等待5分钟
        int waited = 0;
        
        while (waited < maxWaitTime) {
            try {
                Properties info = connection.info("persistence");
                String rdbBgsaveInProgress = info.getProperty("rdb_bgsave_in_progress");
                
                if ("0".equals(rdbBgsaveInProgress)) {
                    return true; // 备份完成
                }
                
                Thread.sleep(1000); // 等待1秒
                waited++;
                
            } catch (Exception e) {
                log.error("Error checking backup status", e);
                return false;
            }
        }
        
        return false; // 超时
    }
    
    private void copyRdbFile() {
        try {
            String sourceFile = "/var/lib/redis/dump.rdb";
            String targetFile = backupPath + "/dump_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".rdb";
                
            Files.copy(Paths.get(sourceFile), Paths.get(targetFile));
            
        } catch (Exception e) {
            log.error("Failed to copy RDB file", e);
        }
    }
    
    private void cleanupOldBackups() {
        try {
            Path backupDir = Paths.get(backupPath);
            long cutoffTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7); // 保留7天
            
            Files.list(backupDir)
                 .filter(path -> path.getFileName().toString().startsWith("dump_"))
                 .filter(path -> {
                     try {
                         return Files.getLastModifiedTime(path).toMillis() < cutoffTime;
                     } catch (Exception e) {
                         return false;
                     }
                 })
                 .forEach(path -> {
                     try {
                         Files.delete(path);
                         log.info("Deleted old backup: {}", path.getFileName());
                     } catch (Exception e) {
                         log.error("Failed to delete old backup: " + path.getFileName(), e);
                     }
                 });
                 
        } catch (Exception e) {
            log.error("Failed to cleanup old backups", e);
        }
    }
}
```

---

## 总结

本文档详细介绍了Redis的核心数据结构、底层实现原理以及Java开发中的各种使用模式和最佳实践。从基础的数据类型操作到高级的分布式锁、限流算法，从简单的客户端使用到复杂的集群部署，涵盖了Redis开发的各个方面。

### 关键要点回顾：

1. **数据结构选择**：根据业务场景选择合适的数据结构，理解底层实现有助于优化性能
2. **客户端选择**：Jedis适合简单场景，Lettuce适合高并发和响应式编程
3. **连接管理**：合理配置连接池，避免连接泄漏和资源浪费
4. **缓存策略**：多级缓存、缓存穿透/击穿/雪崩防护是必须考虑的问题
5. **分布式锁**：理解各种实现方式的优缺点，选择适合的方案
6. **监控告警**：建立完善的监控体系，及时发现和解决问题
7. **安全配置**：生产环境必须考虑认证、网络安全和数据加密
8. **容灾备份**：制定完善的备份和恢复策略，确保数据安全

通过掌握这些知识和技能，可以在Java项目中更好地使用Redis，构建高性能、高可用的应用系统。
```
