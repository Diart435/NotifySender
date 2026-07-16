package com.notify.processor.enums;

public enum NotificationStatus {
    PENDING,
    PROCESSING,
    SENT,
    DELIVERED,
    RETRY,
    FAILED;
    public static NotificationStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        for (NotificationStatus notify : NotificationStatus.values()) {
            if (notify.name().equalsIgnoreCase(value)) {
                return notify;
            }
        }

        throw new IllegalArgumentException("Unknown notify status: " + value);
    }
}
