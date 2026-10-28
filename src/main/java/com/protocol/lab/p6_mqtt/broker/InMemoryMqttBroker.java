package com.protocol.lab.p6_mqtt.broker;

import com.protocol.lab.p6_mqtt.model.MqttPacket;
import com.protocol.lab.p6_mqtt.model.QoSLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

@Slf4j
@Component
public class InMemoryMqttBroker {

    // Subscriptions: TopicFilter -> Tập hợp các Subscriber listener
    private final Map<String, Set<MqttSubscription>> subscriptionTree = new ConcurrentHashMap<>();

    // Retained Messages: Topic -> Packet được lưu trữ cho subscriber mới
    private final Map<String, MqttPacket> retainedMessageStore = new ConcurrentHashMap<>();

    // Last Will and Testament (LWT) store: ClientId -> Will Message
    private final Map<String, MqttPacket> clientLwtStore = new ConcurrentHashMap<>();

    // Active Connected Clients
    private final Set<String> connectedClients = new CopyOnWriteArraySet<>();

    public record MqttSubscription(String clientId, String topicFilter, QoSLevel maxQos, Consumer<MqttPacket> listener) {}

    public synchronized void connectClient(String clientId) {
        connectClient(clientId, null);
    }

    public synchronized void connectClient(String clientId, MqttPacket willMessage) {
        connectedClients.add(clientId);
        if (willMessage != null) {
            clientLwtStore.put(clientId, willMessage);
            log.info("[MQTT Broker] Client '{}' registered with LWT on topic '{}'", clientId, willMessage.getTopic());
        }
        log.info("[MQTT Broker] Client connected: {}", clientId);
    }

    public synchronized void disconnectClient(String clientId) {
        disconnectClient(clientId, false);
    }

    public synchronized void disconnectClient(String clientId, boolean unexpected) {
        connectedClients.remove(clientId);
        subscriptionTree.values().forEach(subSet -> subSet.removeIf(sub -> sub.clientId().equals(clientId)));

        if (unexpected) {
            log.warn("[MQTT Broker] Client '{}' disconnected UNEXPECTEDLY. Checking LWT...", clientId);
            MqttPacket willMessage = clientLwtStore.remove(clientId);
            if (willMessage != null) {
                log.info("[MQTT Broker] Triggering LWT message for client '{}' on topic '{}'", clientId, willMessage.getTopic());
                publish(willMessage);
            }
        } else {
            // Clean disconnect: bỏ qua LWT theo đặc tả MQTT
            clientLwtStore.remove(clientId);
            log.info("[MQTT Broker] Client '{}' cleanly disconnected. Discarded LWT.", clientId);
        }
    }

    public synchronized void simulateUnexpectedDisconnect(String clientId) {
        disconnectClient(clientId, true);
    }

    public void subscribe(String clientId, String topicFilter, QoSLevel qos, Consumer<MqttPacket> listener) {
        connectClient(clientId);
        MqttSubscription subscription = new MqttSubscription(clientId, topicFilter, qos, listener);
        subscriptionTree.computeIfAbsent(topicFilter, k -> new CopyOnWriteArraySet<>()).add(subscription);
        log.info("[MQTT Broker] Client '{}' subscribed to '{}' with QoS {}", clientId, topicFilter, qos.getValue());

        // Kiểm tra gửi ngay Retained message phù hợp với topicFilter
        retainedMessageStore.forEach((topic, packet) -> {
            if (matchesTopic(topicFilter, topic)) {
                log.info("[MQTT Broker] Delivering retained message on topic '{}' to new subscriber '{}'", topic, clientId);
                listener.accept(packet);
            }
        });
    }

    public void unsubscribe(String clientId, String topicFilter) {
        Set<MqttSubscription> subs = subscriptionTree.get(topicFilter);
        if (subs != null) {
            subs.removeIf(s -> s.clientId().equals(clientId));
            if (subs.isEmpty()) {
                subscriptionTree.remove(topicFilter);
            }
        }
        log.info("[MQTT Broker] Client '{}' unsubscribed from '{}'", clientId, topicFilter);
    }

    public void publish(MqttPacket packet) {
        log.info("[MQTT Broker] PUBLISH received from '{}' on topic '{}' (QoS: {}, Retained: {})",
                packet.getSenderClientId(), packet.getTopic(), packet.getQos().getValue(), packet.isRetained());

        // Xử lý Retained Message: Lưu trữ hoặc xóa nếu payload rỗng
        if (packet.isRetained()) {
            if (packet.getPayload() == null || packet.getPayload().isBlank()) {
                retainedMessageStore.remove(packet.getTopic());
                log.info("[MQTT Broker] Cleared retained message on topic '{}'", packet.getTopic());
            } else {
                retainedMessageStore.put(packet.getTopic(), packet);
                log.info("[MQTT Broker] Stored retained message on topic '{}'", packet.getTopic());
            }
        }

        // Tìm kiếm các subscription khớp với Topic
        int deliveredCount = 0;
        for (Map.Entry<String, Set<MqttSubscription>> entry : subscriptionTree.entrySet()) {
            String topicFilter = entry.getKey();
            if (matchesTopic(topicFilter, packet.getTopic())) {
                for (MqttSubscription sub : entry.getValue()) {
                    try {
                        sub.listener().accept(packet);
                        deliveredCount++;
                    } catch (Exception e) {
                        log.error("[MQTT Broker] Error delivering packet to client '{}': {}", sub.clientId(), e.getMessage());
                    }
                }
            }
        }
        log.info("[MQTT Broker] Delivered packet to {} subscribers", deliveredCount);
    }

    public Map<String, MqttPacket> getRetainedMessageStore() {
        return Collections.unmodifiableMap(retainedMessageStore);
    }

    public Set<String> getConnectedClients() {
        return Collections.unmodifiableSet(connectedClients);
    }

    public int getTotalSubscriptions() {
        return subscriptionTree.values().stream().mapToInt(Set::size).sum();
    }

    /**
     * Thuật toán so khớp Wildcard MQTT tiêu chuẩn:
     * '+' khớp đúng 1 level (Single-level wildcard)
     * '#' khớp nhiều level (Multi-level wildcard, bắt buộc ở cuối)
     */
    public static boolean matchesTopic(String filter, String topic) {
        if (filter.equals(topic) || filter.equals("#")) {
            return true;
        }

        String[] filterTokens = filter.split("/");
        String[] topicTokens = topic.split("/");

        for (int i = 0; i < filterTokens.length; i++) {
            String f = filterTokens[i];
            if (f.equals("#")) {
                return true;
            }
            if (i >= topicTokens.length) {
                return false;
            }
            if (!f.equals("+") && !f.equals(topicTokens[i])) {
                return false;
            }
        }

        return filterTokens.length == topicTokens.length;
    }
}
