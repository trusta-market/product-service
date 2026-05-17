package com.trustamarket.productservice.application.event;

import java.util.UUID;

public record ProductReservedMessage(
        UUID eventId,
        UUID orderId,
        UUID productId,
        UUID sellerId,
        UUID buyerId,
        String orderType
) {}