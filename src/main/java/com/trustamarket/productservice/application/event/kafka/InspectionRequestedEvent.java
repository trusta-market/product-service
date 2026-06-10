package com.trustamarket.productservice.application.event.kafka;

import java.util.UUID;

public record InspectionRequestedEvent(
        UUID eventId,
        UUID productId,
        UUID sellerId,
        UUID centerId,
        long originalPriceAmount,
        String currency
) {}
