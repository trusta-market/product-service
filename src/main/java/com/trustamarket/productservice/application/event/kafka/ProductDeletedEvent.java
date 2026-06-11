package com.trustamarket.productservice.application.event.kafka;

import java.util.UUID;

public record ProductDeletedEvent(
        UUID productId
) {}
