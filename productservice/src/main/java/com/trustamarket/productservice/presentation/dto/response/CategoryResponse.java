package com.trustamarket.productservice.presentation.dto.response;

import com.trustamarket.productservice.domain.category.Category;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CategoryResponse {

    private final UUID id;
    private final String name;
    private final int depth;
    private final int displayOrder;
    private final Integer highValueThreshold;
    private final UUID parentId;
    private final String parentName;

    public static CategoryResponse from(Category category) {
        if (category == null) return null;

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .depth(category.getDepth())
                .displayOrder(category.getDisplayOrder())
                .highValueThreshold(category.getHighValueThreshold())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .build();
    }
}
