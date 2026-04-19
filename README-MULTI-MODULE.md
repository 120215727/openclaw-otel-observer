# 🦐 Otel Observer - 多模块版本

OpenClaw 可观测性后台 - 告别黑盒，让你的 OpenClaw 像水晶一样透明！

## 📦 模块架构

```
otel-observer/
├── otel-observer-parent/        # 父POM (聚合模块)
├── otel-observer-common/        # 公共模块
│   ├── ES文档定义 (Document)
│   ├── ES Repository接口
│   └── 通用工具类
├── otel-observer-receiver/      # 接收模块
│   ├── 接收OTLP数据 (traces/metrics/logs)
│   ├── 接收Session/Message数据 (HTTP API)
│   ├── 存储到Elasticsearch
│   └── Web UI展示
└── otel-observer-collector/     # 采集模块
    ├── 监控本地OpenClaw session文件
    ├── 解析JSONL格式的session/message
    ├── 增量同步到Receiver
    └── 断点续传状态管理
```

## 🚀 快速开始

### 前置要求

- Java 17+
- Maven 3.8+
- Elasticsearch 8.x (或使用docker-compose启动)

### 1. 编译项目

```bash
cd otel-observer
mvn clean install -DskipTests
```

### 2. 启动Elasticsearch

```bash
docker-compose up -d elasticsearch
```

### 3. 启动Receiver模块

```bash
cd otel-observer-receiver
mvn spring-boot:run
```

或运行编译好的jar：

```bash
java -jar otel-observer-receiver/target/otel-observer-receiver-1.0.0.jar
```

Receiver默认端口: **10333**

### 4. 启动Collector模块

```bash
cd otel-observer-collector
mvn spring-boot:run
```

或运行编译好的jar：

```bash
java -jar otel-observer-collector/target/otel-observer-collector-1.0.0.jar
```

Collector默认端口: **10334**

## 🔧 配置说明

### Receiver配置 (application.properties)

```properties
# 服务端口
server.port=10333

# Elasticsearch连接
spring.elasticsearch.uris=http://localhost:9200
```

### Collector配置 (application.properties)

```properties
# 服务端口
server.port=10334

# OpenClaw agents目录
collector.agents-path=${user.home}/.openclaw/agents

# Receiver服务地址
collector.receiver-url=http://localhost:10333

# 扫描间隔 (毫秒)
collector.scan-interval-ms=30000

# 状态文件路径
collector.state-path=data/collector-state.json

# 是否启用自动扫描
collector.auto-scan-enabled=true
```

## 📡 API接口

### Receiver - Session数据上传

**POST** `/api/v1/sessions`

```json
{
  "agentId": "main",
  "agentName": "main",
  "sessionId": "5dd4f9f5-ee48-4957-8ce9-1d4a6e3603df",
  "events": [
    {
      "rawJson": "{\"type\":\"session\",\"id\":\"...\",\"timestamp\":\"...\"}"
    },
    {
      "rawJson": "{\"type\":\"message\",\"id\":\"...\",\"timestamp\":\"...\"}"
    }
  ]
}
```

### Receiver - OTLP数据接收 (保持原有)

- `POST /v1/traces` - 接收Trace数据
- `POST /v1/metrics` - 接收Metric数据  
- `POST /v1/logs` - 接收Log数据

## 📊 Elasticsearch索引

| 索引名 | 说明 |
|--------|------|
| `otel-traces` | OTEL链路追踪数据 |
| `otel-metrics` | OTEL指标数据 |
| `otel-logs` | OTEL日志数据 |
| `oc-sessions` | OpenClaw Session数据 |
| `oc-session-events` | OpenClaw Session事件数据 (含message) |

## 🎯 数据结构设计

### SessionDocument (会话文档)

参考OtelTraceDocument设计，包含：
- `id` - ES文档ID
- `created_at` / `updated_at` - 时间戳
- `client_ip` - 客户端IP
- `raw_data` - 原始JSON
- `session_id` / `agent_id` - 会话和Agent标识
- `message_count` / `event_count` - 统计信息
- 等等...

### SessionEventDocument (事件文档)

存储所有类型的session事件：
- `type="session"` - 会话开始
- `type="message"` - 消息 (user/assistant/toolResult)
- `type="model_change"` - 模型变更
- `type="thinking_level_change"` - 思考级别变更
- `type="custom"` - 自定义事件

## 🔄 Collector工作流程

1. **扫描** - 定期扫描 `~/.openclaw/agents/*/sessions/` 目录
2. **检测** - 检查文件的lastModified和size判断是否有更新
3. **读取** - 从上次读取位置开始读取新行
4. **上传** - 将新事件通过HTTP POST发送到Receiver
5. **保存状态** - 记录已同步位置，支持断点续传

## 📝 开发说明

### 模块依赖关系

```
otel-observer-collector
    ↓ depends on
otel-observer-common
    ↑ depends on
otel-observer-receiver
```

### 添加新功能

1. **公共功能** → 在 `otel-observer-common` 中添加
2. **接收API** → 在 `otel-observer-receiver` 中添加
3. **采集逻辑** → 在 `otel-observer-collector` 中添加

## 🛠️ 技术栈

- **Spring Boot 3.2** - Web框架
- **Spring Data Elasticsearch** - ES操作
- **WebClient** - HTTP客户端 (Collector)
- **@Scheduled** - 定时任务 (Collector)
- **Lombok** - 简化代码
- **Elasticsearch 8.x** - 搜索引擎和存储

## 📄 License

MIT License
