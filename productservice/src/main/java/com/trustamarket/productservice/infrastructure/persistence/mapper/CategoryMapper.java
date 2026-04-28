package com.trustamarket.productservice.infrastructure.persistence.mapper;

import com.trustamarket.productservice.domain.category.Category;
import com.trustamarket.productservice.infrastructure.persistence.entity.CategoryJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toDomain(CategoryJpaEntity entity) {
        if (entity == null) return null;

        return Category.builder()
                .id(entity.getId())
                .name(entity.getName())
                .parent(toDomainSummary(entity.getParent()))
                .depth(entity.getDepth())
                .displayOrder(entity.getDisplayOrder())
                .highValueThreshold(entity.getHighValueThreshold())
                .build();
    }
    private Category toDomainSummary(CategoryJpaEntity entity) {
        if (entity == null) return null;

        return Category.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }

    public CategoryJpaEntity toJpaEntity(Category category) {
        if (category == null) return null;

        CategoryJpaEntity parentEntity = null;
        if (category.getParent() != null) {
            parentEntity = CategoryJpaEntity.builder()
                    .id(category.getParent().getId())
                    .build();
        }

        return CategoryJpaEntity.builder()
                .id(category.getId())
                .name(category.getName())
                .parent(parentEntity)
                .depth(category.getDepth())
                .displayOrder(category.getDisplayOrder())
                .highValueThreshold(category.getHighValueThreshold())
                .build();
    }
}
