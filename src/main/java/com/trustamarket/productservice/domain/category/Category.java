package com.trustamarket.productservice.domain.category;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "p_categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {
    @Id // 3. PK(기본키) 설정도 확인 필요
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID categoryId;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    private int depth;
    private int displayOrder;
    @Column(name = "inspection_threshold")
    private Integer inspectionThreshold;

    @Enumerated(EnumType.STRING)
    @Column(name = "inspection_policy", length = 20)
    private InspectionPolicy inspectionPolicy;

    @Column(nullable = false)
    private boolean deleted = false;

    @Column
    private Instant deletedAt;

    @Builder
    public Category(UUID categoryId, String name, Category parent,
                    int depth, int displayOrder, Integer inspectionThreshold, InspectionPolicy inspectionPolicy) {
        this.categoryId = categoryId;
        this.name = name;
        this.parent = parent;
        this.depth = depth;
        this.displayOrder = displayOrder;
        this.inspectionThreshold = inspectionThreshold;
        this.inspectionPolicy    = inspectionPolicy;
        this.deleted = false;
    }

    public void softDelete() {
        this.deleted = true;
        this.deletedAt = Instant.now();
    }

    public InspectionPolicy getEffectivePolicy() {
        return this.inspectionPolicy != null
                ? this.inspectionPolicy
                : CategoryThreshold.getPolicy(this.name);
    }

    public int getEffectiveThreshold() {
        return this.inspectionThreshold != null
                ? this.inspectionThreshold
                : CategoryThreshold.getThreshold(this.name);
    }
    public boolean requiresInspection(Long price) {
        return switch (getEffectivePolicy()) {
            case ALWAYS      -> true;
            case NEVER       -> false;
            case PRICE_BASED -> price >= getEffectiveThreshold();
        };
    }

    public boolean isRoot() { return this.parent == null; }
    public boolean isSubCategory() { return this.parent != null; }
}

