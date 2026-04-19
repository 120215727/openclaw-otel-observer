# Otel Observer Protocol Buffers 支持更改

## 修改内容

### 1. pom.xml
- 添加了 `protobuf-java-util` 依赖 (版本 3.24.4)
- 更新了 Spring Boot 版本到 3.2.5
- 更新了 Lombok 版本到 1.18.34

### 2. OtlpReceiverController.java
- 添加了 Protocol Buffers 格式支持
- 自动检测 Content-Type 来判断格式
- 支持将 pb 格式转换为 JSON 进行处理

## 功能说明

现在服务支持两种格式：
1. **JSON 格式** (默认)
   - Content-Type: application/json
   - 保持原有逻辑不变

2. **Protocol Buffers 格式**
   - Content-Type: application/x-protobuf 或 application/protobuf
   - 自动解析为 OTLP proto 对象
   - 转换为 JSON 后继续后续处理

## 使用方法

OpenClaw 的 diagnostics-otel 插件发送 pb 格式数据时，服务会自动识别并处理。
