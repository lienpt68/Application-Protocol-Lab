package com.protocol.lab.p6_mqtt.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MqttPacket {
    private String packetId;
    private String topic;
    private String payload;
    private QoSLevel qos;
    private boolean retained;
    private boolean duplicate;
    private String senderClientId;
    
    @Builder.Default
    private Instant timestamp = Instant.now();
}
