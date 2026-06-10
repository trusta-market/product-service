package com.trustamarket.productservice.application.event.kafka;

import java.util.UUID;

public record InspectionPriceAcceptedEvent(
        UUID eventId,
        UUID productId,
        UUID sellerId,
        Long finalPrice
) {}
