package com.trustamarket.productservice.presentation.dto.response;

import com.trustamarket.productservice.domain.product.Product;
import com.trustamarket.productservice.domain.product.ProductGrade;
import com.trustamarket.productservice.domain.product.ProductStatus;
import com.trustamarket.productservice.domain.product.InspectionStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductResponse {

    private final UUID id;
    private final UUID sellerId;
    private final UUID categoryId;
    private final String title;
    private final String description;
    private final int price;
    private final ProductGrade grade;
    private final ProductStatus status;
    private final InspectionStatus inspectionStatus;
    private final boolean isInspectionVerified;
    private final List<ProductImageResponse> images;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static ProductResponse from(Product product) {
        if (product == null) return null;

        return ProductResponse.builder()
                .id(product.getId())
                .sellerId(product.getSellerId())
                .categoryId(product.getCategoryId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .grade(product.getGrade())
                .status(product.getStatus())
                .inspectionStatus(product.getInspectionStatus())
                .isInspectionVerified(product.isInspectionVerified())
                .images(product.getImages() != null ?
                        product.getImages().stream()
                        .filter(img -> !img.isDeleted())
                        .map(ProductImageResponse::from)
                        .collect(Collectors.toList()) : Collections.emptyList()) // Null 방어 추가
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
