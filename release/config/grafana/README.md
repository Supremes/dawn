# Dawn Business Metrics - Grafana Dashboard

本目录包含 Dawn 应用的自定义业务监控 Grafana Dashboard 配置文件。

## 📊 Dashboard 概览

### Dawn Business Metrics Dashboard
**文件**: `dawn-business-metrics-dashboard.json`

监控 Dawn 应用的核心业务指标，基于 `ExecutionTimeAspect` 暴露的 Prometheus metrics。

#### 包含的 Panels

1. **Overall Request Rate (req/s)** - 总体请求速率（每秒）
   - 总请求数、成功请求数、失败请求数的实时速率对比
   
2. **Success Rate (%)** - 成功率仪表盘
   - 实时显示请求成功率百分比
   - 阈值：<95% 红色，95-99% 黄色，>99% 绿色

3. **Error Rate (%)** - 错误率仪表盘
   - 实时显示请求错误率百分比
   - 阈值：<1% 绿色，1-5% 黄色，>5% 红色

4. **Top 10 API Endpoints by Request Rate** - 请求量最高的10个API端点
   - 按 URI 分组，显示请求速率排名

5. **Exception Types Distribution** - 异常类型分布饼图
   - 按异常类型（exception 标签）统计异常数量

6. **Top 15 Controller Methods by Total Requests** - 请求量最高的15个Controller方法
   - 按方法名（method_name 标签）排序的柱状图

7. **Success vs Error Requests** - 成功vs失败请求对比
   - 1分钟时间窗口内成功和失败请求的堆叠趋势图

8. **统计数字面板** (6个Stat面板)
   - Total Requests - 总请求数
   - Successful Requests - 成功请求数
   - Failed Requests - 失败请求数
   - Active API Endpoints - 活跃的API端点数
   - Controller Methods - Controller方法总数
   - Exception Types - 异常类型总数

## 🚀 快速开始

### 前置条件

确保以下服务已启动（通过 `docker-compose.yaml` 启动）：

```bash
cd /mnt/f/work/code/dawn/release
docker-compose up -d prometheus grafana dawn-app
```

- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000
- **Dawn App Metrics**: http://localhost:8080/actuator/prometheus

### 导入 Dashboard

#### 方法 1: 通过 Grafana UI 导入

1. 访问 Grafana: http://localhost:3000
2. 使用默认凭据登录:
   - 用户名: `admin`
   - 密码: `admin` (首次登录会要求修改密码)
3. 点击左侧菜单 **Dashboards** → **Import**
4. 点击 **Upload JSON file**，选择 `dawn-business-metrics-dashboard.json`
5. 选择 Prometheus 数据源（如果尚未配置，见下方说明）
6. 点击 **Import**

#### 方法 2: 通过 API 导入

```bash
# 需要先获取 Grafana API Key 或使用 admin 凭据
curl -X POST http://admin:admin@localhost:3000/api/dashboards/db \
  -H "Content-Type: application/json" \
  -d @dawn-business-metrics-dashboard.json
```

### 配置 Prometheus 数据源

如果 Grafana 中尚未配置 Prometheus 数据源：

1. 在 Grafana 中，点击 **Configuration** (齿轮图标) → **Data sources**
2. 点击 **Add data source**
3. 选择 **Prometheus**
4. 配置:
   - **Name**: `prometheus` (必须为小写，与 dashboard JSON 中的 uid 匹配)
   - **URL**: `http://prometheus:9090` (Docker 网络内部) 或 `http://localhost:9090` (本地访问)
   - 其他保持默认
5. 点击 **Save & Test**

## 📈 使用的 Metrics

Dashboard 使用以下自定义 Prometheus metrics (由 `ExecutionTimeAspect` 暴露):

### 1. request_count_total
- **类型**: Counter
- **描述**: Controller 请求总数
- **标签**:
  - `application`: dawn
  - `status`: all

### 2. request_ok_count_total
- **类型**: Counter
- **描述**: Controller 成功请求总数
- **标签**:
  - `application`: dawn
  - `method_name`: Controller 方法名 (如: listArticles)
  - `uri`: 请求 URI (如: /api/articles)
  - `status`: success

### 3. request_exception_count_total
- **类型**: Counter
- **描述**: Controller 异常请求总数
- **标签**:
  - `application`: dawn
  - `method_name`: Controller 方法名
  - `exception`: 异常类型 (如: NullPointerException)
  - `status`: error

## 🔍 常用 PromQL 查询示例

### 请求速率
```promql
# 总请求速率 (每秒)
rate(request_count_total{application="dawn"}[1m])

# 成功请求速率
rate(request_ok_count_total{application="dawn"}[1m])

# 失败请求速率
rate(request_exception_count_total{application="dawn"}[1m])
```

### 成功率和错误率
```promql
# 成功率 (%)
100 * sum(rate(request_ok_count_total{application="dawn"}[5m])) / sum(rate(request_count_total{application="dawn"}[5m]))

# 错误率 (%)
100 * sum(rate(request_exception_count_total{application="dawn"}[5m])) / sum(rate(request_count_total{application="dawn"}[5m]))
```

### 按 URI 分组
```promql
# Top 10 API 端点
topk(10, sum by(uri) (rate(request_ok_count_total{application="dawn"}[1m])))

# 特定 URI 的请求速率
sum(rate(request_ok_count_total{application="dawn", uri="/api/articles"}[1m]))
```

### 按方法名分组
```promql
# Top 15 方法
topk(15, sum by(method_name) (request_ok_count_total{application="dawn"}))
```

### 异常统计
```promql
# 按异常类型统计
sum by(exception) (request_exception_count_total{application="dawn"})

# 特定异常的速率
rate(request_exception_count_total{application="dawn", exception="NullPointerException"}[5m])
```

## ⚙️ Dashboard 配置说明

- **刷新间隔**: 5秒（可在右上角调整）
- **时间范围**: 默认最近15分钟（可调整）
- **时区**: 浏览器时区
- **UID**: `dawn-business-metrics`
- **标签**: `dawn`, `business-metrics`, `custom-metrics`

## 🛠️ 自定义与扩展

### 添加新的 Panel

1. 在 Grafana UI 中编辑 Dashboard
2. 点击右上角 **Add** → **Visualization**
3. 选择 Prometheus 数据源
4. 编写 PromQL 查询
5. 配置可视化类型和样式
6. 保存 Dashboard 并导出 JSON（覆盖现有文件）

### 添加告警

在任意 Panel 中配置告警:

1. 编辑 Panel → **Alert** 标签页
2. 创建告警规则，例如:
   - **条件**: 错误率 > 5%
   - **评估间隔**: 1分钟
   - **持续时间**: 5分钟
3. 配置通知渠道 (Slack, Email, Webhook 等)

### 添加变量 (Variables)

可以添加变量实现动态过滤，例如:

- **$uri**: 从 `label_values(request_ok_count_total, uri)` 获取所有 URI
- **$method**: 从 `label_values(request_ok_count_total, method_name)` 获取所有方法名

然后在 PromQL 中使用: `rate(request_ok_count_total{uri=~"$uri"}[1m])`

## 📝 增强建议

当前 Dashboard 基于 Counter 类型的 metrics。为了获得更全面的监控，建议在 `ExecutionTimeAspect` 中添加:

### 1. 响应时间监控 (Timer/Histogram)

在 `ExecutionTimeAspect` 中添加 `@Around` advice:

```java
@Around("executionPointCut()")
public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
    Timer.Sample sample = Timer.start(meterRegistry);
    try {
        Object result = joinPoint.proceed();
        sample.stop(Timer.builder("request.duration")
            .description("请求执行时间")
            .tag("method_name", joinPoint.getSignature().getName())
            .tag("status", "success")
            .register(meterRegistry));
        return result;
    } catch (Exception e) {
        sample.stop(Timer.builder("request.duration")
            .tag("status", "error")
            .register(meterRegistry));
        throw e;
    }
}
```

然后可以查询:
- P50: `histogram_quantile(0.5, sum(rate(request_duration_seconds_bucket[5m])) by (le))`
- P95: `histogram_quantile(0.95, ...)`
- P99: `histogram_quantile(0.99, ...)`

### 2. 业务指标监控

在业务逻辑中添加自定义 metrics:

```java
// 文章浏览量
Counter.builder("article.views")
    .tag("article_id", articleId.toString())
    .register(meterRegistry)
    .increment();

// 用户注册数
Counter.builder("user.registrations")
    .register(meterRegistry)
    .increment();

// 缓存命中率
Gauge.builder("cache.hit.ratio", cache, c -> c.getHitRatio())
    .register(meterRegistry);
```

## 🐛 故障排查

### Dashboard 显示 "No Data"

1. 检查 Prometheus 是否正在抓取数据:
   ```bash
   curl http://localhost:9090/api/v1/targets
   ```

2. 检查 Dawn 应用是否暴露 metrics:
   ```bash
   curl http://localhost:8080/actuator/prometheus | grep request_count
   ```

3. 检查 Prometheus 配置:
   ```bash
   cat /mnt/f/work/code/dawn/release/config/prometheus.yml
   ```

4. 确认 Grafana 数据源配置正确且可连接

### Metrics 名称不匹配

Prometheus 会将 `.` 转换为 `_`，并在 Counter 后添加 `_total` 后缀:

- `request.count` → `request_count_total`
- `request.ok.count` → `request_ok_count_total`

### 标签过滤不生效

确保使用正确的标签名和值:
```promql
# 正确
request_ok_count_total{application="dawn"}

# 错误 (application 是自动添加的标签)
request_ok_count_total{app="dawn"}
```

## 📚 相关文档

- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)
- [Micrometer Documentation](https://micrometer.io/docs)
- [PromQL Cheat Sheet](https://promlabs.com/promql-cheat-sheet/)

## 🤝 贡献

如果你添加了新的 metrics 或改进了 Dashboard，请:

1. 更新 `ExecutionTimeAspect.java` 或相关 Aspect
2. 导出更新后的 Dashboard JSON
3. 更新此 README 文档
4. 提交 Pull Request

## 📄 许可

与 Dawn 项目保持一致的许可协议。
