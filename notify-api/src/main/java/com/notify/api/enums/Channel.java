package com.notify.api.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Channel {
    EMAIL,
    SMS,
    PUSH;

    @JsonCreator
    public static Channel fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        for (Channel channel : Channel.values()) {
            if (channel.name().equalsIgnoreCase(value)) {
                return channel;
            }
        }

        throw new IllegalArgumentException("Unknown channel: " + value);
    }
}
