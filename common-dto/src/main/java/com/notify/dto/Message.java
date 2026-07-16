package com.notify.dto;

public record Message<T>(String topic, T data) {
}
