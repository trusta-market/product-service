package com.trustamarket.productservice.application.port;

import com.trustamarket.productservice.domain.product.Product;

import java.util.UUID;

public interface ProductEventPublishPort {
    void publishProductCreated(Product product);
    void publishProductDeleted(UUID productId);

    // 검수 요청 — product-service → inspection-service, delivery-service
    void publishInspectionRequested(UUID productId, UUID sellerId, UUID centerId, long originalPriceAmount, String currency);

    // 판매자 수락 — product-service → inspection-service
    void publishInspectionPriceAccepted(Product product);

    // 판매자 거절 — product-service → inspection-service
    void publishInspectionPriceRejected(Product product, String reason);
}
