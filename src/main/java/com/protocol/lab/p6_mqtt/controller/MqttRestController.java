package com.protocol.lab.p6_mqtt.controller;

import com.protocol.lab.p6_mqtt.broker.InMemoryMqttBroker;
import com.protocol.lab.p6_mqtt.model.MqttPacket;
import com.protocol.lab.p6_mqtt.model.QoSLevel;
import com.protocol.lab.p6_mqtt.service.MqttQoSSimulatorService;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@RestController
@RequestMapping("/api/mqtt")
@RequiredArgsConstructor
public class MqttRestController {

    private final InMemoryMqttBroker broker;
    private final MqttQoSSimulatorService qosSimulatorService;

    // Buffer tin nhắn nhận được qua web subscriber demo
    private final List<MqttPacket> receivedMessagesBuffer = new CopyOnWriteArrayList<>();

    @Data
    public static class PublishRequest {
        private String clientId = "iot-sensor-01";
        private String topic = "sensors/living-room/temperature";
        private String payload = "{\"temp\": 28.5, \"humidity\": 65}";
        private int qos = 1;
        private boolean retained = false;
    }

    @Data
    public static class ConnectRequest {
        private String clientId = "iot-device-01";
        private String willTopic = "sensors/status";
        private String willPayload = "{\"status\": \"OFFLINE\", \"reason\": \"LWT trigger\"}";
        private int willQos = 1;
    }

    @PostMapping("/publish")
    public ResponseEntity<Map<String, Object>> publish(@RequestBody PublishRequest request) {
        QoSLevel qosLevel = QoSLevel.fromValue(request.getQos());
        MqttPacket packet = MqttPacket.builder()
                .packetId(UUID.randomUUID().toString().substring(0, 8))
                .senderClientId(request.getClientId())
                .topic(request.getTopic())
                .payload(request.getPayload())
                .qos(qosLevel)
                .retained(request.isRetained())
                .build();

        // 1. Chạy QoS handshake simulation
        List<MqttQoSSimulatorService.QoSFlowStep> flowSteps = qosSimulatorService.simulatePublish(packet);

        // 2. Broker phân phối tin nhắn
        broker.publish(packet);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("packet", packet);
        response.put("qosFlow", flowSteps);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/connect")
    public ResponseEntity<Map<String, Object>> connect(@RequestBody ConnectRequest req) {
        MqttPacket willMessage = null;
        if (req.getWillTopic() != null && !req.getWillTopic().isBlank()) {
            willMessage = MqttPacket.builder()
                    .packetId(UUID.randomUUID().toString().substring(0, 8))
                    .senderClientId(req.getClientId())
                    .topic(req.getWillTopic())
                    .payload(req.getWillPayload())
                    .qos(QoSLevel.fromValue(req.getWillQos()))
                    .retained(true)
                    .build();
        }
        broker.connectClient(req.getClientId(), willMessage);
        return ResponseEntity.ok(Map.of("success", true, "message", "Client connected: " + req.getClientId()));
    }

    @PostMapping("/disconnect")
    public ResponseEntity<Map<String, Object>> disconnect(@RequestParam String clientId,
                                                          @RequestParam(defaultValue = "false") boolean unexpected) {
        if (unexpected) {
            broker.simulateUnexpectedDisconnect(clientId);
        } else {
            broker.disconnectClient(clientId);
        }
        return ResponseEntity.ok(Map.of("success", true, "unexpected", unexpected));
    }

    @PostMapping("/subscribe")
    public ResponseEntity<Map<String, Object>> subscribe(@RequestParam String clientId,
                                                         @RequestParam String topicFilter,
                                                         @RequestParam(defaultValue = "0") int qos) {
        broker.subscribe(clientId, topicFilter, QoSLevel.fromValue(qos), packet -> {
            receivedMessagesBuffer.add(packet);
            if (receivedMessagesBuffer.size() > 50) {
                receivedMessagesBuffer.remove(0);
            }
        });
        return ResponseEntity.ok(Map.of("success", true, "subscribedTopic", topicFilter));
    }

    @GetMapping("/messages")
    public ResponseEntity<List<MqttPacket>> getReceivedMessages() {
        return ResponseEntity.ok(List.copyOf(receivedMessagesBuffer));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("connectedClients", broker.getConnectedClients());
        stats.put("totalSubscriptions", broker.getTotalSubscriptions());
        stats.put("retainedMessages", broker.getRetainedMessageStore());
        stats.put("qosFlowHistory", qosSimulatorService.getFlowHistory());
        return ResponseEntity.ok(stats);
    }
}
