# Issue
## Admin 
- 刷新首页也会发起登录请求, 调用report
## User

## Common
- actuator health 检查失败, status 为down。 
  - application.yml配置的service，actuator都会去做health check，确保每个service是可连接的状态

- 优化JwtAuthenticationTokenFilter,确保非监控上报等接口，需要authtoken
- 


# Todo
## 集成Grafana
好的，這是一份詳細的指南，將引導您如何在 Java 1.8 和 Spring Boot 2.3.7 的服務中集成 Grafana，以實現應用程式的監控和可視化。

### **核心架構**

整個集成流程的核心架構如下：

1.  **Spring Boot Actuator**: 在您的 Spring Boot 應用中，它會生成並暴露各種運行時的指標（Metrics），例如 JVM 性能、CPU 使用率、HTTP 請求統計等。
2.  **Micrometer**: Actuator 內部使用的一個監控指標門面庫。它將 Spring Boot 的指標轉換為 Prometheus 能夠識別的格式。
3.  **Prometheus**: 一個開源的監控和警報系統。它會定期從您的 Spring Boot 應用的指定端點（Endpoint）上 "抓取" (scrape) 指標數據，並將其存儲在自己的時序數據庫（Time-Series Database）中。
4.  **Grafana**: 一個開源的可視化平台。它將 Prometheus 作為數據源，通過強大的查詢語言（PromQL）從 Prometheus 獲取數據，並將其呈現在可自定義的儀表盤（Dashboard）上。

**流程圖:**
`[Spring Boot App] -> [Actuator Exposes /actuator/prometheus Endpoint] -> [Prometheus Scrapes Metrics] -> [Grafana Queries Prometheus & Displays Dashboards]`

-----

### **第一步：配置 Spring Boot 應用程式 (2.3.7)**

您的應用程式需要添加依賴並修改配置，以暴露 Prometheus 格式的指標。

#### 1\. 添加 Maven 依賴

在您的 `pom.xml` 文件中，確保您有以下兩個依賴：

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>

    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-registry-prometheus</artifactId>
    </dependency>

    </dependencies>
```

#### 2\. 修改 application.properties

在 `src/main/resources/application.properties` 文件中，添加以下配置來暴露 Prometheus 端點：

```properties
# 設置應用名稱，這個名稱會作為標籤出現在 Prometheus 中
spring.application.name=my-springboot-app

# Actuator 管理端口配置
management.endpoints.web.exposure.include=health,info,prometheus

# 啟用 prometheus 端點
management.endpoint.prometheus.enabled=true

# (可選) 為指標添加全局標籤，方便在 Grafana 中篩選
management.metrics.tags.application=${spring.application.name}
```

  * `management.endpoints.web.exposure.include`: 這個配置項決定了哪些 Actuator 端點可以通過 HTTP 訪問。我們明確地包含了 `prometheus`。

#### 3\. 驗證 Spring Boot 應用

現在，運行您的 Spring Boot 應用程式。啟動後，訪問以下 URL：

`http://localhost:8080/actuator/prometheus`

如果配置成功，您應該會看到大量的文本指標數據，格式類似於：

```
# HELP jvm_memory_used_bytes The amount of used memory in bytes.
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{application="my-springboot-app",area="heap",id="G1 Survivor Space",} 2490368.0
jvm_memory_used_bytes{application="my-springboot-app",area="heap",id="G1 Old Gen",} 5.44768E7
# ... 更多指標
```

這表明您的應用程式已經準備好被 Prometheus 抓取了。

-----

### **第二步：使用 Docker 設置 Prometheus 和 Grafana**

為了簡化部署，我們強烈建議使用 Docker 和 Docker Compose 來運行 Prometheus 和 Grafana。

#### 1\. 創建 docker-compose.yml

在您的項目根目錄（或任何您喜歡的位置）創建一個名為 `docker-compose.yml` 的文件：

```yaml
version: '3.7'

services:
  # Prometheus 服務
  prometheus:
    image: prom/prometheus:v2.45.0 # 使用一個穩定的版本
    container_name: prometheus
    ports:
      - "9090:9090" # 暴露 Prometheus 的 Web UI 端口
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml # 掛載配置文件
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
    networks:
      - monitor-net

  # Grafana 服務
  grafana:
    image: grafana/grafana:9.5.3 # 使用一個穩定的版本
    container_name: grafana
    ports:
      - "3000:3000" # 暴露 Grafana 的 Web UI 端口
    restart: unless-stopped
    volumes:
      - grafana-storage:/var/lib/grafana # 持久化 Grafana 數據
    depends_on:
      - prometheus
    networks:
      - monitor-net

volumes:
  grafana-storage:

networks:
  monitor-net:
```

#### 2\. 創建 prometheus.yml

在與 `docker-compose.yml` 相同的目錄下，創建一個名為 `prometheus.yml` 的文件。這是 Prometheus 的配置文件，它告訴 Prometheus 去哪裡抓取指標。

```yaml
global:
  scrape_interval: 15s # 每 15 秒抓取一次指標

scrape_configs:
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

  - job_name: 'spring-boot-app' # 為您的 Spring Boot 應用定義一個 job
    metrics_path: '/actuator/prometheus' # 指標端點的路徑
    scrape_interval: 5s # 可以為特定 job 覆蓋全局抓取間隔
    static_configs:
      # 這裡的 targets 指向您 Spring Boot 應用的地址
      # 如果 Docker 和 Spring Boot 運行在同一台宿主機上，
      # 使用 'host.docker.internal' 來讓 Docker 容器訪問宿主機
      - targets: ['host.docker.internal:8080']
```

**重要提示**: `host.docker.internal` 是 Docker 提供的一個特殊 DNS 名稱，它解析為宿主機的內部 IP 地址。這是在 Docker 容器內訪問宿主機上運行的服務（如您的 Spring Boot 應用）的最佳方式。

#### 3\. 啟動監控堆棧

確保您的 Spring Boot 應用正在運行。然後，在包含 `docker-compose.yml` 的目錄中，打開終端並運行：

```bash
docker-compose up -d
```

這將在後台下載並啟動 Prometheus 和 Grafana 容器。

#### 4\. 驗證 Prometheus

1.  打開瀏覽器，訪問 `http://localhost:9090`。您應該能看到 Prometheus 的 Web UI。
2.  點擊頂部導航欄的 "Status" -\> "Targets"。
3.  您應該能看到兩個 Target：`prometheus` 和 `spring-boot-app`。如果 `spring-boot-app` 的 "State" 是 **UP**，那麼恭喜您，Prometheus 已經成功地從您的應用中抓取指標了！

-----

### **第三步：配置 Grafana 並創建儀表盤**

現在數據已經在 Prometheus 中了，最後一步是在 Grafana 中將其可視化。

#### 1\. 登錄 Grafana

1.  打開瀏覽器，訪問 `http://localhost:3000`。
2.  默認的用戶名是 `admin`，密碼也是 `admin`。
3.  首次登錄後，系統會提示您修改密碼。

#### 2\. 添加 Prometheus 數據源

1.  在左側菜單欄中，點擊齒輪圖標 (Configuration) -\> "Data Sources"。
2.  點擊 "Add data source" 按鈕。
3.  從列表中選擇 "Prometheus"。
4.  在 "HTTP" 部分的 "URL" 字段中，輸入 `http://prometheus:9090`。
      * **注意**: 這裡我們使用 `prometheus` 作為主機名，而不是 `localhost`。因為 Grafana 和 Prometheus 運行在同一個 Docker 網絡 (`monitor-net`) 中，它們可以通過服務名直接通信。
5.  點擊頁面底部的 "Save & test"。如果看到 "Data source is working" 的綠色提示，說明連接成功。

#### 3\. 導入一個預製的 Spring Boot 儀表盤（推薦）

手動創建儀表盤可能很耗時。社區已經創建了許多優秀的 Spring Boot 儀表盤。

1.  在左側菜單欄中，點擊加號圖標 (Create) -\> "Import"。
2.  在 "Import via grafana.com" 字段中，輸入一個儀表盤 ID。對於 Spring Boot 2，以下是一些廣受歡迎的選擇：
      * **12900** (JVM (Micrometer))
      * **4701** (Spring Boot 2.1 Statistics)
      * **9964** (Spring Boot Statistics)
3.  點擊 "Load"。
4.  在下一個頁面中，確保在 "Prometheus" 數據源下拉菜單中選擇您剛剛添加的 Prometheus 數據源。
5.  點擊 "Import"。

片刻之後，您應該能看到一個充滿數據的儀表盤，顯示了您應用的 JVM 內存、CPU、線程、日誌事件和 HTTP 請求等各種詳細信息！

### **總結**

恭喜您！您已成功地將 Spring Boot 應用與 Prometheus 和 Grafana 集成。這個監控堆棧功能強大且可擴展，是現代雲原生應用開發中不可或缺的一部分。

**後續步驟建議：**

  * **自定義指標**: 使用 Micrometer 的 `MeterRegistry` 在您的代碼中創建自定義指標（例如，業務邏輯的計數器、計時器等）。
  * **設置警報**: 在 Grafana 中為關鍵指標（如錯誤率、高延遲）配置警報規則，以便在問題發生時及時收到通知。
  * **探索 PromQL**: 學習 Prometheus 查詢語言 (PromQL)，以創建更複雜、更有洞察力的自定義圖表。