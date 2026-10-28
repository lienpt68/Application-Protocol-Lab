# MQTT Protocol Lab (`p6_mqtt`)

Giao thức truyền thông Publish/Subscribe nhẹ, tối ưu hóa cho mạng IoT, thiết bị nhúng và mạng di động chập chờn.

---

## 1. Các Đặc Tính Cốt Lõi Đã Triển Khai
- **Hierarchical Topic Routing & Wildcards**: Hỗ trợ Single-level (`+`) và Multi-level (`#`).
- **QoS Level Handshake**:
  - `QoS 0` (At most once): Fire & forget.
  - `QoS 1` (At least once): `PUBLISH` $\rightarrow$ `PUBACK`.
  - `QoS 2` (Exactly once): Bắt tay 4 bước `PUBLISH` $\rightarrow$ `PUBREC` $\rightarrow$ `PUBREL` $\rightarrow$ `PUBCOMP`.
- **Retained Messages**: Lưu trữ tin nhắn mới nhất trên topic, tự động gửi ngay cho Subscriber mới.
- **Last Will and Testament (LWT)**: Đăng ký thông điệp di chúc khi CONNECT; tự động phát tán khi Broker phát hiện client mất kết nối đột ngột.

---

## 2. Copy-Paste cURL Test Payloads

### 2.1 Publish Tin Nhắn IoT (QoS 1)
```bash
curl -X POST http://localhost:8080/api/mqtt/publish \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "iot-sensor-01",
    "topic": "sensors/living-room/temperature",
    "payload": "{\"temperature\": 28.5, \"humidity\": 65}",
    "qos": 1,
    "retained": false
  }'
```

### 2.2 Publish Retained Message (Lưu trữ trạng thái)
```bash
curl -X POST http://localhost:8080/api/mqtt/publish \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "config-manager",
    "topic": "home/living-room/light",
    "payload": "{\"power\": \"ON\", \"brightness\": 80}",
    "qos": 1,
    "retained": true
  }'
```

### 2.3 Đăng Ký Subscribe Topic Wildcard
```bash
curl -X POST "http://localhost:8080/api/mqtt/subscribe?clientId=mobile-app&topicFilter=sensors/%2B/temperature&qos=1"
```

### 2.4 Mô Phỏng Ngắt Kết Nối Đột Ngột Kích Hoạt LWT
```bash
# 1. Kết nối đăng ký LWT
curl -X POST http://localhost:8080/api/mqtt/connect \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "edge-gateway",
    "willTopic": "home/alerts",
    "willPayload": "{\"alert\": \"GATEWAY_DISCONNECTED\", \"level\": \"CRITICAL\"}",
    "willQos": 1
  }'

# 2. Rớt mạng đột ngột (Unexpected Drop)
curl -X POST "http://localhost:8080/api/mqtt/disconnect?clientId=edge-gateway&unexpected=true"
```

### 2.5 Kiểm Tra Thống Kê Broker & Lịch Sử QoS Flow
```bash
curl http://localhost:8080/api/mqtt/stats
```
