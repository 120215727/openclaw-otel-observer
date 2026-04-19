# 🦐 Otel Observer

OpenClaw Observability Backend - Say goodbye to black boxes, make your OpenClaw as transparent as crystal!

## Table of Contents

- [Project Overview](#project-overview)
- [Features](#features)
- [Architecture](#architecture)
- [Quick Start](#quick-start)
- [Module Description](#module-description)
- [Configuration Guide](#configuration-guide)
- [API Documentation](#api-documentation)
- [Tech Stack](#tech-stack)

## Project Overview

Otel Observer is an observability platform designed specifically for OpenClaw, providing complete collection, storage, and visualization capabilities for Traces, Metrics, Logs, and Session conversation data.

### Core Capabilities

- **OTLP Data Reception**: Compatible with OpenTelemetry protocol, receiving Traces/Metrics/Logs
- **Session Management**: Collect and display OpenClaw conversation sessions and message history
- **Multi-module Architecture**: Supports independent deployment of Receiver and Collector, as well as All-in-One one-click startup
- **Elasticsearch Storage**: Efficient full-text search and time-series data storage
- **Web UI Visualization**: Intuitive data analysis and query interface

## Features

### 📡 Data Reception

- OTLP protocol support (JSON + Protobuf)
- HTTP/JSON format
- gRPC/Protobuf format
- Session file scanning and uploading

### 💬 Session Management

- Session list view
- Complete conversation history
- Token consumption statistics
- Cost calculation
- Message details and tool calls

### 🎨 Web UI

- Dashboard statistics overview
- Session detail page
- Traces distributed tracing
- Metrics charts
- Logs query

### 🔍 Data Processing

- Asynchronous data processing queue
- Incremental file scanning
- State synchronization management
- Automatic retry mechanism

## Architecture

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
│  │  (Data Reception)│◄─────┤  (File Scanner)  │        │
│  │                  │      │                  │        │
│  │ - OTLP Endpoints │      │ - Session Scan   │        │
│  │ - Session API    │      │ - Incremental Sync│       │
│  │ - Web UI         │      │ - State Management│       │
│  └────────┬─────────┘      └──────────────────┘        │
│           │                                               │
│           ▼                                               │
│  ┌──────────────────┐                                    │
│  │  Elasticsearch   │                                    │
│  │  (Data Storage)  │                                    │
│  └──────────────────┘                                    │
└─────────────────────────────────────────────────────────┘
```

### Module Division

| Module | Description |
|--------|-------------|
| `otel-observer-common` | Common data models, DTOs, ES Repository |
| `otel-observer-receiver` | Data reception service, Web UI, REST API |
| `otel-observer-collector` | Session file scanner and collector |
| `otel-observer-all` | All-in-One integration module |

## Quick Start

### Environment Requirements

- **Java**: 17+
- **Elasticsearch**: 8.x (running on localhost:9200)
- **Maven**: 3.8+

### 1. Start Elasticsearch

```bash
# Using Docker
docker run -d \
  --name elasticsearch \
  -p 9200:9200 \
  -e "discovery.type=single-node" \
  -e "xpack.security.enabled=false" \
  docker.elastic.co/elasticsearch/elasticsearch:8.11.0
```

### 2. Build the Project

```bash
mvn clean package -DskipTests
```

### 3. Start All-in-One Service

```bash
cd otel-observer-all
mvn spring-boot:run
```

Or run the packaged JAR:

```bash
java -jar otel-observer-all/target/otel-observer-all-1.0.0.jar
```

### 4. Access Web UI

Open browser and visit: http://localhost:10333

### 5. Configure OpenClaw to Send Data

Set environment variables to let OpenClaw send OTLP data to Otel Observer:

```bash
export OTEL_EXPORTER_OTLP_ENDPOINT="http://localhost:10333"
export OTEL_SERVICE_NAME="openclaw"
```

Then restart OpenClaw Gateway:

```bash
openclaw gateway restart
```

## Module Description

### otel-observer-common

Common module, containing:
- Elasticsearch document models
- DTOs and response classes
- Repository interfaces
- Enums and constants

### otel-observer-receiver

Receiver service module, containing:
- OTLP reception controllers (`/v1/traces`, `/v1/metrics`, `/v1/logs`)
- Session API controllers
- Web UI (Thymeleaf templates)
- Data processing services
- Elasticsearch configuration

**Standalone Startup:**
```bash
cd otel-observer-receiver
mvn spring-boot:run
```

### otel-observer-collector

Collector module, containing:
- Session file scanner
- File state management
- Incremental synchronization logic
- Session data upload client

**Standalone Startup:**
```bash
cd otel-observer-collector
mvn spring-boot:run
```

### otel-observer-all

All-in-One module, integrating Receiver and Collector:
- Provides both data reception and file scanning capabilities
- Automatically performs initial scan on startup
- Suitable for single-machine deployment scenarios

## Configuration Guide

### Configuration File Locations

Configuration files for each module:
- `otel-observer-all/src/main/resources/application.properties`
- `otel-observer-receiver/src/main/resources/application.properties`
- `otel-observer-collector/src/main/resources/application.properties`

### Core Configuration Items

#### Server Configuration

```properties
# Service port
server.port=10333
server.address=0.0.0.0
```

#### Elasticsearch Configuration

```properties
# ES connection address
spring.elasticsearch.uris=http://localhost:9200

# Timeout settings
spring.elasticsearch.socket-timeout=30s
spring.elasticsearch.connection-timeout=10s
```

#### Collector Configuration

```properties
# OpenClaw agents directory path
collector.agents-path=${user.home}/.openclaw/agents

# Receiver address (collector sends data here)
collector.receiver-url=http://localhost:10333

# Scan interval (milliseconds)
collector.scan-interval-ms=30000

# State file path
collector.state-path=data/collector-state.json

# Enable automatic scanning
collector.auto-scan-enabled=true
```

#### Data Processing Configuration

```properties
# Raw data processing interval (milliseconds)
rawdata.processor.interval-ms=5000
```

## API Documentation

### OTLP Endpoints

| Endpoint | Method | Content-Type | Description |
|----------|--------|--------------|-------------|
| `/v1/traces` | POST | `application/json` or `application/x-protobuf` | Receive Trace data |
| `/v1/metrics` | POST | `application/json` or `application/x-protobuf` | Receive Metric data |
| `/v1/logs` | POST | `application/json` or `application/x-protobuf` | Receive Log data |

### Session API

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/sessions` | GET | Get Session list (paginated) |
| `/api/sessions/{sessionId}` | GET | Get single Session details |
| `/api/sessions/{sessionId}/messages` | GET | Get Session message list |
| `/api/sessions/upload` | POST | Upload Session data |
| `/api/stats` | GET | Get statistics data |
| `/api/activity` | GET | Get recent activity |

### Web UI Pages

| Path | Description |
|------|-------------|
| `/` | Dashboard home |
| `/sessions` | Session list |
| `/sessions/{sessionId}` | Session details |
| `/traces` | Traces view |
| `/metrics` | Metrics charts |
| `/logs` | Logs query |

## Tech Stack

| Technology | Version | Description |
|------------|---------|-------------|
| Spring Boot | 3.2.x | Web application framework |
| Spring Data Elasticsearch | 3.2.x | ES data access |
| Elasticsearch | 8.x | Search engine and data storage |
| Thymeleaf | 3.2.x | Server-side template engine |
| OpenTelemetry Proto | 1.0.0-alpha | OTLP protocol definition |
| gRPC | 1.60.x | Protobuf communication support |
| Lombok | 1.18.x | Code simplification |
| Maven | 3.8+ | Build tool |
| Java | 17+ | Programming language |

## Project Structure

```
otel-observer/
├── pom.xml                                    # Parent POM
├── otel-observer-common/                     # Common module
│   └── src/main/java/com/openclaw/observer/
│       ├── common/                            # Common utilities and constants
│       ├── document/                          # ES document models
│       ├── dto/                               # Data transfer objects
│       └── repository/                        # ES Repository
├── otel-observer-receiver/                   # Receiver service module
│   └── src/main/java/com/openclaw/observer/
│       ├── controller/                        # REST controllers
│       ├── service/                           # Business services
│       └── resources/
│           ├── application.properties         # Configuration file
│           └── templates/                     # Thymeleaf templates
├── otel-observer-collector/                  # Collector module
│   └── src/main/java/com/openclaw/observer/collector/
│       ├── service/                           # Collection services
│       ├── model/                             # Collector models
│       └── config/                            # Collector configuration
└── otel-observer-all/                         # All-in-One module
    └── src/main/java/com/openclaw/observer/
        └── OtelObserverAllApplication.java   # Main application entry
```

## Development Guide

### Local Development

1. Ensure Elasticsearch is running on localhost:9200
2. Clone the project and import into IDE
3. Run `OtelObserverAllApplication` main class

### Adding New ES Repository

Add in the `otel-observer-common` module:

```java
public interface MyDocumentEsRepository 
    extends ElasticsearchRepository<MyDocument, String> {
}
```

### Adding New API Endpoints

Add in `ApiController` of the `otel-observer-receiver` module.

## Troubleshooting

### Elasticsearch Connection Failed

Check:
- Is ES running: `curl http://localhost:9200`
- Is `spring.elasticsearch.uris` in configuration correct
- Firewall or network connection

### Session Files Not Scanned

Check:
- Is `collector.agents-path` configuration pointing to the correct OpenClaw agents directory
- Are directory permissions correct
- Check application logs for scanning-related errors

### OTLP Data Not Showing

Check:
- Is OpenClaw's OTEL_EXPORTER_OTLP_ENDPOINT environment variable correct
- Is Content-Type correct (JSON or Protobuf)
- Check Receiver logs for data reception records

## Contributing

Issues and Pull Requests are welcome!

## License

MIT License
