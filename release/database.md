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