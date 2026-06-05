package com.trustamarket.productservice.application.port;

public interface OutboxEventRepository {
    void save(String topic, String payload);
}
