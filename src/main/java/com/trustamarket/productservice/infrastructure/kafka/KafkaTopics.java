package com.trustamarket.productservice.infrastructure.kafka;

import java.util.UUID;

public class KafkaTopics {

    public static final String PRODUCT_CREATED_TOPIC         = "product.created";
    public static final String PRODUCT_DELETED_TOPIC         = "product.deleted";
    public static final String INSPECTION_REQUESTED_TOPIC    = "inspection.requested";
    public static final String INSPECTION_PRICE_ACCEPTED_TOPIC = "inspection.price.accepted";
    public static final String INSPECTION_PRICE_REJECTED_TOPIC = "inspection.price.rejected";

}