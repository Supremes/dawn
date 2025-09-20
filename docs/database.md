# MySQL数据库梳理

Job表单

- t_job: 存储了后台job的详细信息
- t_job_log：存储了每次job执行的日志信息

User表单

- t_user_auth:用户的auth信息（用户名及密码信息）
- t_user_info:用户的基本信息
- t_user_role: 用户userid和roleid, 支持一对多，即一个用户对应多个角色

Role表单

- t_role: 角色的详细信息（分为admin、test、user）
- t_role_menu: 关联表单 role - menu
- t_role_resource: 关联表单 role - resource

Menu表单:

- t_menu: 

Resource表单：

- t_resource：存放各个api的访问权限及请求方式，比如/admin/comments接口，请求方式是GET



# RabbitMQ

## Exchange类型详解

`FanoutExchange` 是RabbitMQ中的一种交换机类型。让我详细介绍各种Exchange的区别：

### 1. FanoutExchange（扇形交换机）

#### 特点
- **广播模式**：将消息发送到所有绑定的队列
- **忽略routing key**：不关心routing key的值
- **速度最快**：因为不需要路由判断

#### 使用场景
```java
// 你的代码中使用的就是这种
@Bean
public FanoutExchange maxWellExchange() {
    return new FanoutExchange(MAXWELL_EXCHANGE, true, false);
}
```

#### 典型应用
- 日志收集系统
- 实时数据广播
- 系统通知

### 2. DirectExchange（直连交换机）

#### 特点
- **精确匹配**：routing key必须完全匹配
- **点对点通信**：一条消息只发送到匹配的队列
- **默认交换机**：RabbitMQ的默认交换机类型

#### 配置示例
````java
@Bean
public DirectExchange directExchange() {
    return new DirectExchange("direct.exchange", true, false);
}

@Bean
public Binding bindingDirect() {
    return BindingBuilder.bind(queue())
            .to(directExchange())
            .with("user.register");  // routing key
}
````

#### 典型应用
- 任务分发
- 邮件发送
- 用户注册流程

### 3. TopicExchange（主题交换机）

#### 特点
- **模式匹配**：支持通配符路由
- **`*`**：匹配一个词
- **`#`**：匹配零个或多个词
- **灵活性最高**

#### 配置示例
````java
@Bean
public TopicExchange topicExchange() {
    return new TopicExchange("topic.exchange", true, false);
}

@Bean
public Binding bindingTopic1() {
    return BindingBuilder.bind(queue1())
            .to(topicExchange())
            .with("user.*");  // 匹配 user.register, user.login 等
}

@Bean
public Binding bindingTopic2() {
    return BindingBuilder.bind(queue2())
            .to(topicExchange())
            .with("order.#");  // 匹配 order.create, order.pay.success 等
}
````

#### 典型应用
- 日志分级处理
- 事件分类处理
- 微服务间通信

### 4. HeadersExchange（头部交换机）

#### 特点
- **基于消息头匹配**：根据消息的headers属性路由
- **支持多条件匹配**：可以设置all或any匹配模式
- **性能较低**：因为需要解析消息头

#### 配置示例
````java
@Bean
public HeadersExchange headersExchange() {
    return new HeadersExchange("headers.exchange", true, false);
}

@Bean
public Binding bindingHeaders() {
    return BindingBuilder.bind(queue())
            .to(headersExchange())
            .whereAll(Map.of("format", "pdf", "type", "report"))
            .match();
}
````

#### 典型应用
- 复杂路由需求
- 基于消息属性的分发
- 多维度消息过滤

### Exchange类型对比

| Exchange类型 | 路由方式            | 性能 | 复杂度 | 使用场景     |
| ------------ | ------------------- | ---- | ------ | ------------ |
| **Fanout**   | 广播到所有队列      | 最高 | 最简单 | 发布/订阅    |
| **Direct**   | 精确匹配routing key | 高   | 简单   | 点对点通信   |
| **Topic**    | 模式匹配routing key | 中等 | 中等   | 灵活路由     |
| **Headers**  | 基于消息头匹配      | 最低 | 最复杂 | 复杂条件路由 |

### 实施建议

基于你的配置，如果需要更灵活的路由，可以考虑：

#### 改为Topic Exchange
````java
@Bean
public TopicExchange maxWellExchange() {
    return new TopicExchange(MAXWELL_EXCHANGE, true, false);
}

@Bean
public Binding bindingArticleTopic() {
    return BindingBuilder.bind(articleQueue())
            .to(maxWellExchange())
            .with("maxwell.article.*");
}
````

#### 添加Direct Exchange用于精确路由
````java
@Bean
public DirectExchange emailDirectExchange() {
    return new DirectExchange("email.direct.exchange", true, false);
}

@Bean
public Binding bindingEmailDirect() {
    return BindingBuilder.bind(emailQueue())
            .to(emailDirectExchange())
            .with("email.send");
}
````

#### 选择建议
- **简单广播**：用Fanout
- **精确路由**：用Direct  
- **灵活路由**：用Topic
- **复杂条件**：用Headers



## 死信队列 + TTL - 延迟队列

- **让“TTL队列”没有消费者，TTL 过期后自动转入“有消费者”的死信队列**

  1. 消息发送到 `ttl_exchange`，进入 `ttl_queue`。
  2. 消息在 `ttl_queue` 中存活 10 秒（TTL）。
  3. 消息过期后，自动被转移到死信交换机 `dead_letter_exchange`。
  4. 死信交换机根据 routing key 转发到 `dead_letter_queue`。
  5. `DeadLetterConsumer` 消费死信队列中的消息。

- Spring AMQP 使用自动确认（Auto Ack），但处理死信队列时建议手动确认。

  确认过程中，需要涉及到几个概念：

  1. **channel**： 是客户端和服务端之间的通信通道，**每个连接可以创建多个channel**。Consumer通过channel监听消息队列，channel是**非线程安全**的，Spring AMQP默认为每个监听器使用独立的channel
  2. **deliveryTag**：每次消息投递给Consumer时，会分配一个deliveryTag，该信息用于消息确认（ack）或者拒绝（nack）。同一个channel中的deliveryTag是唯一的
  3. 消费者在处理完消息后，需要告诉RabbitMQ消息是否处理成功，RabbitMQ根据确认结果决定是否删除消息或者重新投递。



## TODO

替换成kafka

  3. 消费者在处理完消息后，需要告诉RabbitMQ消息是否处理成功，RabbitMQ根据确认结果决定是否删除消息或者重新投递。
