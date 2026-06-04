package com.trustamarket.productservice.infrastructure.persistence.mapper;

import com.trustamarket.productservice.domain.category.Category;
import com.trustamarket.productservice.infrastructure.persistence.entity.CategoryJpaEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CategoryMapper {

    public Category toDomain(CategoryJpaEntity entity) {
        if (entity == null) return null;
        return Category.builder()
                .categoryId(entity.getCategoryId())
                .name(entity.getName())
                .parent(toDomainSummary(entity.getParent()))
                .depth(entity.getDepth())
                .displayOrder(entity.getDisplayOrder())
                .inspectionThreshold(entity.getInspectionThreshold())
                .inspectionPolicy(entity.getInspectionPolicy())
                .deleted(entity.isDeleted())         // JpaEntity → Domain 전달
                .deletedAt(entity.getDeletedAt())    // JpaEntity → Domain 전달
                .build();
    }

    private Category toDomainSummary(CategoryJpaEntity entity) {
        if (entity == null) return null;
        return Category.builder()
                .categoryId(entity.getCategoryId())
                .name(entity.getName())
                .build();
    }

    public CategoryJpaEntity toJpaEntity(Category category) {
        if (category == null) return null;
        CategoryJpaEntity parentEntity = null;
        if (category.getParent() != null) {
            UUID parentId = category.getParent().getCategoryId();
            if (parentId == null) throw new IllegalArgumentException("부모 카테고리 엔티티를 생성하려면 반드시 ID가 필요합니다.");
            parentEntity = CategoryJpaEntity.builder().categoryId(parentId).build();
        }
        return CategoryJpaEntity.builder()
                .categoryId(category.getCategoryId())
                .name(category.getName())
                .parent(parentEntity)
                .depth(category.getDepth())
                .displayOrder(category.getDisplayOrder())
                .inspectionThreshold(category.getInspectionThreshold())
                .inspectionPolicy(category.getInspectionPolicy())
                .deleted(category.isDeleted())       // Domain → JpaEntity 전달
                .deletedAt(category.getDeletedAt())  // Domain → JpaEntity 전달
                .build();
    }
}
