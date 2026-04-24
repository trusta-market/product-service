package com.trustamarket.productservice.domain.category;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {
    private Long id;
    private String name;
    private Category parent;
    private int depth;
    private int displayOrder;
    private int highValueThreshold;  // 카테고리별 고가 기준 금액

    @Builder
    public Category(Long id, String name, Category parent,
                    int depth, int displayOrder, int highValueThreshold) {
        this.id = id;
        this.name = name;
        this.parent = parent;
        this.depth = depth;
        this.displayOrder = displayOrder;
        this.highValueThreshold = highValueThreshold;
    }

    // 최상위 카테고리 여부 확인
    public boolean isRoot() {
        return this.parent == null;
    }

    // 하위 카테고리 여부 확인
    public boolean isSubCategory() {
        return this.parent != null;
    }
}

