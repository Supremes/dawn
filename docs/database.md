# MySQL数据库梳理

Job表单

- t_job: 存储了后台job的详细信息
- t_job_log：存储了每次job执行的日志信息

User表单

- t_user_auth:用户的auth信息（用户名及密码信息）
- t_user_info:用户的基本信息
- t_user_role: 用户userid和roleid

Role表单

- t_role: 角色的详细信息（分为admin、test、user）
- t_role_menu: 关联表单 role - menu
- t_role_resource: 关联表单 role - resource

Menu表单:

- t_menu: 

Resource表单：

- t_resource：存放各个api的访问权限及请求方式，比如/admin/comments接口，请求方式是GET



# RabbitMQ

## TODO

替换成kafka

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