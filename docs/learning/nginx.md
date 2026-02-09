Nginx 的 `location` 指令支持多种匹配方式，**优先级从高到低如下**：

| 优先级 | 匹配方式              | 语法                       | 说明                                   |
| ------ | --------------------- | -------------------------- | -------------------------------------- |
| 1️⃣ 最高 | **精确匹配**          | `location = /path`         | 完全相等才匹配，优先级最高             |
| 2️⃣      | **前缀匹配（带 ^~）** | `location ^~ /path`        | 前缀匹配，但匹配成功后**不再检查正则** |
| 3️⃣      | **正则匹配**          | `location ~ /path` 或 `~*` | 区分大小写或不区分，按**书写顺序**匹配 |
| 4️⃣ 最低 | **普通前缀匹配**      | `location /path`           | 前缀匹配，但会继续检查正则             |

> ✅ **结论**：`=` > `^~` > `~` / `~*` > 普通前缀



项目中使用到的配置：

- 精确匹配: location = /api
- 前缀匹配: location  ^~ /api/
- 普通前缀匹配: location /



```java
        location / {        
            root   /usr/local/vue/blog;
            index  index.html index.htm; 
            try_files $uri $uri/ /index.html;    
        }

        # API requests - exact match for /api and prefix match for /api/, since /api is initiated by frontend to fetch websiteConfig
        location = /api {
            proxy_pass http://dawn:8080/;
            proxy_set_header   Host             $host;
            proxy_set_header   X-Real-IP        $remote_addr;                        
            proxy_set_header   X-Forwarded-For  $proxy_add_x_forwarded_for;
        }
        
        location ^~ /api/ {        
            proxy_pass http://dawn:8080/;
            proxy_set_header   Host             $host;
            proxy_set_header   X-Real-IP        $remote_addr;                        
            proxy_set_header   X-Forwarded-For  $proxy_add_x_forwarded_for;
        }
```

