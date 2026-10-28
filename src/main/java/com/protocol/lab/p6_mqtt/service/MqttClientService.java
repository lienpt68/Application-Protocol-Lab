package com.protocol.lab.p6_mqtt.service;

import com.protocol.lab.p6_mqtt.broker.InMemoryMqttBroker;
import com.protocol.lab.p6_mqtt.model.MqttPacket;
import com.protocol.lab.p6_mqtt.model.QoSLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class MqttClientService {

    private final InMemoryMqttBroker broker;

    public void publishMessage(String senderClientId, String topic, String payload, QoSLevel qos, boolean retained) {
        MqttPacket packet = MqttPacket.builder()
                .packetId(UUID.randomUUID().toString().substring(0, 8))
                .senderClientId(senderClientId)
                .topic(topic)
                .payload(payload)
                .qos(qos)
                .retained(retained)
                .timestamp(Instant.now())
                .build();

        broker.publish(packet);
    }

    public void subscribeTopic(String clientId, String topicFilter, QoSLevel qos, Consumer<MqttPacket> listener) {
        broker.subscribe(clientId, topicFilter, qos, listener);
    }

    public void unsubscribeTopic(String clientId, String topicFilter) {
        broker.unsubscribe(clientId, topicFilter);
    }

    public void disconnect(String clientId) {
        broker.disconnectClient(clientId);
    }
}
