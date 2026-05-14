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

        if (product.getSellerId() == null || product.getCategoryId() == null) {
            throw new IllegalArgumentException("판매자 ID와 카테고리 ID는 필수입니다.");
        }

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
                .images(toImageJpaEntities(product.getImages()))
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt());

        if (product.getId() != null) {
            builder.id(product.getId());
        }

        return builder.build();
    }

    // JpaEntity → Domain
    public Product toDomain(ProductJpaEntity entity) {
        if (entity == null) return null;

        return Product.restore(
                entity.getId(),
                entity.getSellerId(),
                entity.getCategoryId(),
                entity.getInspectorId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getSuggestedPrice(),
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
                .map(img -> {
                    // 1. 빌더를 변수로 생성 (상품과 동일한 방식)
                    var imgBuilder = ProductImageJpaEntity.builder()
                            .imageUrl(img.getImageUrl())
                            .sortOrder(img.getSortOrder())
                            .isThumbnail(img.isThumbnail());

                    // 2. 이미지 ID가 있을 때만(즉, 수정 시에만) ID를 세팅
                    if (img.getId() != null) {
                        imgBuilder.id(img.getId());
                    }

                    return imgBuilder.build();
                })
                .collect(Collectors.toList());
    }

    private List<ProductImage> toImageDomains(List<ProductImageJpaEntity> entities) {
        if (entities == null) return new ArrayList<>(); // null 방어 코드

        return entities.stream()
                .map(e -> ProductImage.restore(
                        e.getId(),
                        e.getImageUrl(),
                        e.getSortOrder(),
                        e.isThumbnail(),
                        e.isDeleted(),   
                        e.getDeletedAt()
                ))
                .collect(Collectors.toList());
    }
}
