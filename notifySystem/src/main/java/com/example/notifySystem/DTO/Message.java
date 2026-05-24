package com.example.notifySystem.DTO;

public record Message<T>(String topic, T data) {
}
