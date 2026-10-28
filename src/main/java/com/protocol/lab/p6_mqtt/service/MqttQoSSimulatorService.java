package com.protocol.lab.p6_mqtt.service;

import com.protocol.lab.p6_mqtt.model.MqttPacket;
import com.protocol.lab.p6_mqtt.model.QoSLevel;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class MqttQoSSimulatorService {

    public enum PacketType {
        CONNECT, CONNACK, PUBLISH, PUBACK, PUBREC, PUBREL, PUBCOMP, DISCONNECT
    }

    @Data
    @Builder
    public static class QoSFlowStep {
        private String stepId;
        private PacketType packetType;
        private String from;
        private String to;
        private String details;
        private Instant timestamp;
    }

    private final List<QoSFlowStep> flowHistory = new CopyOnWriteArrayList<>();

    /**
     * Mô phỏng luồng bắt tay theo từng cấp độ QoS trong MQTT v3.1.1 / v5.0:
     * - QoS 0: At most once (PUBLISH đơn hướng, không ACK).
     * - QoS 1: At least once (PUBLISH -> PUBACK).
     * - QoS 2: Exactly once (4-step handshake: PUBLISH -> PUBREC -> PUBREL -> PUBCOMP).
     */
    public List<QoSFlowStep> simulatePublish(MqttPacket packet) {
        List<QoSFlowStep> steps = new ArrayList<>();
        String client = packet.getSenderClientId() != null ? packet.getSenderClientId() : "client-unknown";
        String broker = "In-Memory MQTT Broker";
        String packetId = packet.getPacketId() != null ? packet.getPacketId() : UUID.randomUUID().toString().substring(0, 8);

        // Step 1: Client gửi PUBLISH
        QoSFlowStep step1 = QoSFlowStep.builder()
                .stepId(UUID.randomUUID().toString())
                .packetType(PacketType.PUBLISH)
                .from(client)
                .to(broker)
                .details(String.format("PUBLISH (ID: %s, Topic: '%s', QoS: %d, Retain: %b)",
                        packetId, packet.getTopic(), packet.getQos().getValue(), packet.isRetained()))
                .timestamp(Instant.now())
                .build();
        steps.add(step1);
        flowHistory.add(step1);

        if (packet.getQos() == QoSLevel.AT_LEAST_ONCE) {
            // QoS 1: Broker phản hồi PUBACK
            QoSFlowStep step2 = QoSFlowStep.builder()
                    .stepId(UUID.randomUUID().toString())
                    .packetType(PacketType.PUBACK)
                    .from(broker)
                    .to(client)
                    .details(String.format("PUBACK (PacketId: %s acknowledged)", packetId))
                    .timestamp(Instant.now())
                    .build();
            steps.add(step2);
            flowHistory.add(step2);
        } else if (packet.getQos() == QoSLevel.EXACTLY_ONCE) {
            // QoS 2: Bắt tay 4 bước
            // 2. Broker -> Client: PUBREC (Publish Received)
            QoSFlowStep step2 = QoSFlowStep.builder()
                    .stepId(UUID.randomUUID().toString())
                    .packetType(PacketType.PUBREC)
                    .from(broker)
                    .to(client)
                    .details(String.format("PUBREC (PacketId: %s received, holding message)", packetId))
                    .timestamp(Instant.now())
                    .build();
            steps.add(step2);
            flowHistory.add(step2);

            // 3. Client -> Broker: PUBREL (Publish Release)
            QoSFlowStep step3 = QoSFlowStep.builder()
                    .stepId(UUID.randomUUID().toString())
                    .packetType(PacketType.PUBREL)
                    .from(client)
                    .to(broker)
                    .details(String.format("PUBREL (PacketId: %s release ordered by client)", packetId))
                    .timestamp(Instant.now())
                    .build();
            steps.add(step3);
            flowHistory.add(step3);

            // 4. Broker -> Client: PUBCOMP (Publish Complete)
            QoSFlowStep step4 = QoSFlowStep.builder()
                    .stepId(UUID.randomUUID().toString())
                    .packetType(PacketType.PUBCOMP)
                    .from(broker)
                    .to(client)
                    .details(String.format("PUBCOMP (PacketId: %s transaction completed, message released to subscribers)", packetId))
                    .timestamp(Instant.now())
                    .build();
            steps.add(step4);
            flowHistory.add(step4);
        }

        log.info("[MQTT QoS] Executed {} flow for packetId {} with {} steps", packet.getQos(), packetId, steps.size());
        return steps;
    }

    public List<QoSFlowStep> getFlowHistory() {
        return List.copyOf(flowHistory);
    }

    public void clearHistory() {
        flowHistory.clear();
    }
}
