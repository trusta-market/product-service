package com.trustamarket.productservice.presentation.dto.response;

import com.trustamarket.productservice.domain.product.Product;
import com.trustamarket.productservice.domain.product.ProductStatus;

import java.util.UUID;

// internal API (`GET /internal/v1/products/{id}`) 응답.
// 다른 서비스(주로 order-service)가 주문 생성 시 product 검증/snapshot 용도로 호출.
// 외부 노출 ProductResponse 와 다르게 imageUrls / inspectionStatus 등 상세 X — 주문 도메인이 필요한 최소 필드만.
public record ProductInfoResponse(
        UUID id,
        UUID sellerId,
        String title,
        Long price,
        ProductStatus status
) {
    public static ProductInfoResponse from(Product product) {
        return new ProductInfoResponse(
                product.getId(),
                product.getSellerId(),
                product.getTitle(),
                product.getPrice(),
                product.getStatus()
        );
    }
}
