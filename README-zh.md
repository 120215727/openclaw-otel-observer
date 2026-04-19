# 🦐 Otel Observer

OpenClaw 可观测性后台 - 告别黑盒，让你的 OpenClaw 像水晶一样透明！

## 目录

- [项目概述](#项目概述)
- [功能特性](#功能特性)
- [架构设计](#架构设计)
- [快速开始](#快速开始)
- [模块说明](#模块说明)
- [配置指南](#配置指南)
- [API 文档](#api-文档)
- [技术栈](#技术栈)

## 项目概述

Otel Observer 是一个专为 OpenClaw 设计的可观测性平台，提供完整的 Traces、Metrics、Logs 以及 Session 会话数据的收集、存储和可视化能力。

### 核心能力

- **OTLP 数据接收**: 兼容 OpenTelemetry 协议，接收 Traces/Metrics/Logs
- **Session 会话管理**: 收集并展示 OpenClaw 的对话会话和消息历史
- **多模块架构**: 支持独立部署 Receiver 和 Collector，也支持 All-in-One 一键启动
- **Elasticsearch 存储**: 高效的全文检索和时序数据存储
- **Web UI 可视化**: 直观的数据分析和查询界面

## 功能特性

### 📡 数据接收

- OTLP 协议支持 (JSON + Protobuf)
- HTTP/JSON 格式
- gRPC/Protobuf 格式
- Session 文件扫描和上传

### 💬 Session 管理

- 会话列表查看
- 完整对话历史
- Token 消耗统计
- 费用计算
- 消息详情和工具调用

### 🎨 Web UI

- 仪表板统计概览
- 会话详情页
- Traces 链路追踪
- Metrics 指标图表
- Logs 日志查询

### 🔍 数据处理

- 异步数据处理队列
- 增量文件扫描
- 状态同步管理
- 自动重试机制

## 架构设计

```
┌─────────────────┐
│   OpenClaw      │
│  (OTLP Exporter)│
└────────┬────────┘
         │ OTLP/gRPC
         ▼
┌─────────────────────────────────────────────────────────┐
│                    Otel Observer                         │
├─────────────────────────────────────────────────────────┤
│  ┌──────────────────┐      ┌──────────────────┐        │
│  │  Receiver        │      │  Collector       │        │
│  │  (数据接收)      │◄─────┤  (文件扫描)      │        │
│  │                  │      │                  │        │
│  │ - OTLP Endpoints │      │ - Session 扫描   │        │
│  │ - Session API    │      │ - 增量同步       │        │
│  │ - Web UI         │      │ - 状态管理       │        │
│  └────────┬─────────┘      └──────────────────┘        │
│           │                                               │
│           ▼                                               │
│  ┌──────────────────┐                                    │
│  │  Elasticsearch   │                                    │
│  │  (数据存储)      │                                    │
│  └──────────────────┘                                    │
└─────────────────────────────────────────────────────────┘
```

### 模块划分

| 模块 | 说明 |
|------|------|
| `otel-observer-common` | 通用数据模型、DTO、ES Repository |
| `otel-observer-receiver` | 数据接收服务、Web UI、REST API |
| `otel-observer-collector` | Session 文件扫描和采集器 |
| `otel-observer-all` | All-in-One 整合模块 |

## 快速开始

### 环境要求

- **Java**: 17+
- **Elasticsearch**: 8.x (运行在 localhost:9200)
- **Maven**: 3.8+

### 1. 启动 Elasticsearch

```bash
# 使用 Docker
docker run -d \
  --name elasticsearch \
  -p 9200:9200 \
  -e "discovery.type=single-node" \
  -e "xpack.security.enabled=false" \
  docker.elastic.co/elasticsearch/elasticsearch:8.11.0
```

### 2. 编译项目

```bash
mvn clean package -DskipTests
```

### 3. 启动 All-in-One 服务

```bash
cd otel-observer-all
mvn spring-boot:run
```

或者运行打包后的 JAR:

```bash
java -jar otel-observer-all/target/otel-observer-all-1.0.0.jar
```

### 4. 访问 Web UI

打开浏览器访问: http://localhost:10333

### 5. 配置 OpenClaw 发送数据

设置环境变量让 OpenClaw 发送 OTLP 数据到 Otel Observer:

```bash
export OTEL_EXPORTER_OTLP_ENDPOINT="http://localhost:10333"
export OTEL_SERVICE_NAME="openclaw"
```

然后重启 OpenClaw Gateway:

```bash
openclaw gateway restart
```

## 模块说明

### otel-observer-common

公共模块，包含:
- Elasticsearch 文档模型
- DTO 和响应类
- Repository 接口
- 枚举和常量

### otel-observer-receiver

接收服务模块，包含:
- OTLP 接收控制器 (`/v1/traces`, `/v1/metrics`, `/v1/logs`)
- Session API 控制器
- Web UI (Thymeleaf 模板)
- 数据处理服务
- Elasticsearch 配置

**独立启动:**
```bash
cd otel-observer-receiver
mvn spring-boot:run
```

### otel-observer-collector

采集器模块，包含:
- Session 文件扫描器
- 文件状态管理
- 增量同步逻辑
- Session 数据上传客户端

**独立启动:**
```bash
cd otel-observer-collector
mvn spring-boot:run
```

### otel-observer-all

All-in-One 模块，整合 Receiver 和 Collector:
- 同时提供数据接收和文件扫描能力
- 启动时自动执行初始扫描
- 适用于单机部署场景

## 配置指南

### 配置文件位置

各模块的配置文件:
- `otel-observer-all/src/main/resources/application.properties`
- `otel-observer-receiver/src/main/resources/application.properties`
- `otel-observer-collector/src/main/resources/application.properties`

### 核心配置项

#### Server 配置

```properties
# 服务端口
server.port=10333
server.address=0.0.0.0
```

#### Elasticsearch 配置

```properties
# ES 连接地址
spring.elasticsearch.uris=http://localhost:9200

# 超时设置
spring.elasticsearch.socket-timeout=30s
spring.elasticsearch.connection-timeout=10s
```

#### Collector 配置

```properties
# OpenClaw agents 目录路径
collector.agents-path=${user.home}/.openclaw/agents

# Receiver 地址 (collector 发送数据到此)
collector.receiver-url=http://localhost:10333

# 扫描间隔 (毫秒)
collector.scan-interval-ms=30000

# 状态文件路径
collector.state-path=data/collector-state.json

# 是否启用自动扫描
collector.auto-scan-enabled=true
```

#### 数据处理配置

```properties
# 原始数据处理间隔 (毫秒)
rawdata.processor.interval-ms=5000
```

## API 文档

### OTLP 端点

| 端点 | 方法 | Content-Type | 说明 |
|------|------|--------------|------|
| `/v1/traces` | POST | `application/json` 或 `application/x-protobuf` | 接收 Trace 数据 |
| `/v1/metrics` | POST | `application/json` 或 `application/x-protobuf` | 接收 Metric 数据 |
| `/v1/logs` | POST | `application/json` 或 `application/x-protobuf` | 接收 Log 数据 |

### Session API

| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/sessions` | GET | 获取 Session 列表 (分页) |
| `/api/sessions/{sessionId}` | GET | 获取单个 Session 详情 |
| `/api/sessions/{sessionId}/messages` | GET | 获取 Session 的消息列表 |
| `/api/sessions/upload` | POST | 上传 Session 数据 |
| `/api/stats` | GET | 获取统计数据 |
| `/api/activity` | GET | 获取最近活动 |

### Web UI 页面

| 路径 | 说明 |
|------|------|
| `/` | 仪表板首页 |
| `/sessions` | Session 列表 |
| `/sessions/{sessionId}` | Session 详情 |
| `/traces` | Traces 查看 |
| `/metrics` | Metrics 图表 |
| `/logs` | Logs 查询 |

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.x | Web 应用框架 |
| Spring Data Elasticsearch | 3.2.x | ES 数据访问 |
| Elasticsearch | 8.x | 搜索引擎和数据存储 |
| Thymeleaf | 3.2.x | 服务端模板引擎 |
| OpenTelemetry Proto | 1.0.0-alpha | OTLP 协议定义 |
| gRPC | 1.60.x | Protobuf 通信支持 |
| Lombok | 1.18.x | 代码简化 |
| Maven | 3.8+ | 构建工具 |
| Java | 17+ | 编程语言 |

## 项目结构

```
otel-observer/
├── pom.xml                                    # Parent POM
├── otel-observer-common/                     # 公共模块
│   └── src/main/java/com/openclaw/observer/
│       ├── common/                            # 公共工具和常量
│       ├── document/                          # ES 文档模型
│       ├── dto/                               # 数据传输对象
│       └── repository/                        # ES Repository
├── otel-observer-receiver/                   # 接收服务模块
│   └── src/main/java/com/openclaw/observer/
│       ├── controller/                        # REST 控制器
│       ├── service/                           # 业务服务
│       └── resources/
│           ├── application.properties         # 配置文件
│           └── templates/                     # Thymeleaf 模板
├── otel-observer-collector/                  # 采集器模块
│   └── src/main/java/com/openclaw/observer/collector/
│       ├── service/                           # 采集服务
│       ├── model/                             # 采集器模型
│       └── config/                            # 采集器配置
└── otel-observer-all/                         # All-in-One 模块
    └── src/main/java/com/openclaw/observer/
        └── OtelObserverAllApplication.java   # 主应用入口
```

## 开发说明

### 本地开发

1. 确保 Elasticsearch 运行在 localhost:9200
2. 克隆项目并导入 IDE
3. 运行 `OtelObserverAllApplication` 主类

### 添加新的 ES Repository

在 `otel-observer-common` 模块中添加:

```java
public interface MyDocumentEsRepository 
    extends ElasticsearchRepository<MyDocument, String> {
}
```

### 添加新的 API 端点

在 `otel-observer-receiver` 模块的 `ApiController` 中添加。

## 故障排查

### Elasticsearch 连接失败

检查:
- ES 是否启动: `curl http://localhost:9200`
- 配置中的 `spring.elasticsearch.uris` 是否正确
- 防火墙或网络连接

### Session 文件未扫描

检查:
- `collector.agents-path` 配置是否指向正确的 OpenClaw agents 目录
- 目录权限是否正确
- 查看应用日志中的扫描相关错误

### OTLP 数据未显示

检查:
- OpenClaw 的 OTEL_EXPORTER_OTLP_ENDPOINT 环境变量是否正确
- Content-Type 是否正确 (JSON 或 Protobuf)
- 查看 Receiver 日志中的数据接收记录

## 贡献

欢迎提交 Issue 和 Pull Request！

## License

MIT License
