package com.trustamarket.productservice.infrastructure.persistence.mapper;

import com.trustamarket.productservice.domain.product.Product;
import com.trustamarket.productservice.domain.product.ProductImage;
import com.trustamarket.productservice.infrastructure.persistence.entity.ProductImageJpaEntity;
import com.trustamarket.productservice.infrastructure.persistence.entity.ProductJpaEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductMapper {

    // Domain → JpaEntity
    public ProductJpaEntity toJpaEntity(Product product) {
        if (product == null) return null;

        return ProductJpaEntity.builder()
                .id(product.getId())
                .sellerId(product.getSellerId())
                .categoryId(product.getCategoryId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice()) // Integer 타입 대응
                .grade(product.getGrade())
                .status(product.getStatus())
                .inspectionStatus(product.getInspectionStatus())
                .images(toImageJpaEntities(product.getImages()))
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    // JpaEntity → Domain
    public Product toDomain(ProductJpaEntity entity) {
        if (entity == null) return null;

        return Product.restore(
                entity.getId(),
                entity.getSellerId(),
                entity.getCategoryId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getGrade(),
                entity.getStatus(),
                entity.getInspectionStatus(),
                toImageDomains(entity.getImages()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private List<ProductImageJpaEntity> toImageJpaEntities(List<ProductImage> images) {
        if (images == null) return new ArrayList<>(); // null 방어 코드

        return images.stream()
                .map(img -> ProductImageJpaEntity.builder()
                        .id(img.getId())
                        .imageUrl(img.getImageUrl())
                        .sortOrder(img.getSortOrder())
                        .isThumbnail(img.isThumbnail())
                        .build())
                .collect(Collectors.toList());
    }

    private List<ProductImage> toImageDomains(List<ProductImageJpaEntity> entities) {
        if (entities == null) return new ArrayList<>(); // null 방어 코드

        return entities.stream()
                .map(e -> ProductImage.restore(
                        e.getId(),
                        e.getImageUrl(),
                        e.getSortOrder(),
                        e.isThumbnail()
                ))
                .collect(Collectors.toList());
    }
}
