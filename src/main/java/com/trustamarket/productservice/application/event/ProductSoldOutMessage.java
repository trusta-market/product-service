package com.trustamarket.productservice.application.event;

import java.time.Instant;
import java.util.UUID;

// order-service 가 confirm 시 발행하는 이벤트. Kafka topic: order.product.sold-out
public record ProductSoldOutMessage(
        UUID eventId,
        UUID orderId,
        UUID productId,
        Instant soldAt
) {}
