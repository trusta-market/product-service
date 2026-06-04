package com.trustamarket.productservice.presentation.dto.response;

import com.trustamarket.productservice.domain.category.Category;
import com.trustamarket.productservice.domain.category.InspectionPolicy;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CategoryResponse {

    private final UUID categoryId;
    private final String name;
    private final int depth;
    private final int displayOrder;
    private final Integer          inspectionThreshold;
    private final InspectionPolicy inspectionPolicy;
    private final UUID parentId;
    private final String parentName;

    public static CategoryResponse from(Category category) {
        if (category == null) return null;

        return CategoryResponse.builder()
                .categoryId(category.getCategoryId())
                .name(category.getName())
                .depth(category.getDepth())
                .displayOrder(category.getDisplayOrder())
                .inspectionThreshold(category.getInspectionThreshold())
                .inspectionPolicy(category.getEffectivePolicy())
                .parentId(category.getParent() != null ? category.getParent().getCategoryId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .build();
    }
}
