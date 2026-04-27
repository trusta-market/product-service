package com.trustamarket.productservice.domain.category;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {
    @Id // 3. PK(기본키) 설정도 확인 필요
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    private int depth;
    private int displayOrder;
    private Integer highValueThreshold;  // 카테고리별 고가 기준 금액

    @Builder
    public Category(UUID id, String name, Category parent,
                    int depth, int displayOrder, Integer highValueThreshold) {
        this.id = id;
        this.name = name;
        this.parent = parent;
        this.depth = depth;
        this.displayOrder = displayOrder;
        this.highValueThreshold = highValueThreshold;
    }

    public int getEffectiveThreshold() {
        if (this.highValueThreshold != null) {
            return this.highValueThreshold;
        }
        return CategoryThreshold.getThreshold(this.name);
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

