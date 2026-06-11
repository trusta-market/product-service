package com.trustamarket.productservice.application.event.kafka;


import java.util.UUID;

public record ProductCreatedEvent(
        UUID productId,
        UUID sellerId,
        UUID categoryId,
        Long price,
        String inspectionStatus
) {}
