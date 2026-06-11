package com.trustamarket.productservice.application.event.kafka;

import java.util.UUID;

public record InspectionPriceRejectedEvent(
        UUID eventId,
        UUID productId,
        UUID sellerId,
        String reason
) {}
