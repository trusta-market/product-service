package com.trustamarket.productservice.infrastructure.kafka;

import java.util.UUID;

public class KafkaTopics {

    public static final String PRODUCT_CREATED_TOPIC         = "product.created";
    public static final String PRODUCT_DELETED_TOPIC         = "product.deleted";
    public static final String INSPECTION_REQUESTED_TOPIC    = "inspection.requested";
    public static final String INSPECTION_PRICE_ACCEPTED_TOPIC = "inspection.price.accepted";
    public static final String INSPECTION_PRICE_REJECTED_TOPIC = "inspection.price.rejected";

    public record ProductCreatedEvent(
            UUID productId,
            UUID sellerId,
            UUID categoryId,
            Long price,
            String inspectionStatus
    ) {}

    public record ProductDeletedEvent(
            UUID productId
    ) {}

    public record InspectionRequestedEvent(
            UUID eventId,
            UUID productId,
            UUID sellerId,
            UUID centerId,
            long originalPriceAmount,
            String currency
    ) {}

    public record InspectionPriceAcceptedEvent(
            UUID eventId,
            UUID productId,
            UUID sellerId,
            Long finalPrice
    ) {}

    public record InspectionPriceRejectedEvent(
            UUID eventId,
            UUID productId,
            UUID sellerId,
            String reason
    ) {}
}