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

    public ProductJpaEntity toJpaEntity(Product product) {
        if (product == null) return null;
        if (product.getSellerId() == null || product.getCategoryId() == null)
            throw new IllegalArgumentException("판매자 ID와 카테고리 ID는 필수입니다.");

        var builder = ProductJpaEntity.builder()
                .sellerId(product.getSellerId())
                .categoryId(product.getCategoryId())
                .inspectorId(product.getInspectorId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .suggestedPrice(product.getSuggestedPrice())
                .grade(product.getGrade())
                .status(product.getStatus())
                .inspectionStatus(product.getInspectionStatus())
                .deleted(product.isDeleted())
                .deletedAt(product.getDeletedAt())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt());

        if (product.getId() != null) builder.id(product.getId());

        ProductJpaEntity jpaEntity = builder.build();
        toImageJpaEntities(product.getImages(), jpaEntity).forEach(jpaEntity::addImage);
        return jpaEntity;
    }

    public Product toDomain(ProductJpaEntity entity) {
        if (entity == null) return null;
        return Product.restore(
                entity.getId(), entity.getSellerId(), entity.getCategoryId(), entity.getInspectorId(),
                entity.getTitle(), entity.getDescription(), entity.getPrice(), entity.getSuggestedPrice(),
                entity.getGrade(), entity.getStatus(), entity.getInspectionStatus(),
                toImageDomains(entity.getImages()),
                entity.getCreatedAt(), entity.getUpdatedAt(),
                entity.isDeleted(), entity.getDeletedAt()
        );
    }

    private List<ProductImageJpaEntity> toImageJpaEntities(List<ProductImage> images, ProductJpaEntity product) {
        if (images == null) return new ArrayList<>();
        return images.stream()
                .map(img -> ProductImageJpaEntity.create(product, img.getId(), img.getImageUrl(),
                        img.getSortOrder(), img.isThumbnail(), img.isDeleted(), img.getDeletedAt()))
                .collect(Collectors.toList());
    }

    private List<ProductImage> toImageDomains(List<ProductImageJpaEntity> entities) {
        if (entities == null) return new ArrayList<>();
        return entities.stream()
                .map(e -> ProductImage.restore(e.getId(), e.getImageUrl(), e.getSortOrder(),
                        e.isThumbnail(), e.isDeleted(), e.getDeletedAt()))
                .collect(Collectors.toList());
    }
}