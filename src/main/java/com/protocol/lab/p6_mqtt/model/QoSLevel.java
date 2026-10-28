package com.protocol.lab.p6_mqtt.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QoSLevel {
    AT_MOST_ONCE(0, "QoS 0: Fire and forget (Không xác nhận, tối đa 1 lần)"),
    AT_LEAST_ONCE(1, "QoS 1: Acknowledged delivery (Có PUBACK, ít nhất 1 lần)"),
    EXACTLY_ONCE(2, "QoS 2: Assured delivery (4-step handshake, đúng 1 lần duy nhất)");

    private final int value;
    private final String description;

    public static QoSLevel fromValue(int value) {
        for (QoSLevel level : values()) {
            if (level.value == value) {
                return level;
            }
        }
        return AT_MOST_ONCE;
    }
}
