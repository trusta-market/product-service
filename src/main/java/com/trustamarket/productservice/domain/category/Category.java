package com.trustamarket.productservice.domain.category;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class Category {

    private final UUID categoryId;
    private final String name;
    private final Category parent;
    private final int depth;
    private final int displayOrder;
    private final Integer inspectionThreshold;
    private final InspectionPolicy inspectionPolicy;

    private boolean deleted;
    private Instant deletedAt;

    @Builder
    public Category(UUID categoryId, String name, Category parent,
                    int depth, int displayOrder, Integer inspectionThreshold,
                    InspectionPolicy inspectionPolicy,
                    boolean deleted, Instant deletedAt) {
        this.categoryId = categoryId;
        this.name = name;
        this.parent = parent;
        this.depth = depth;
        this.displayOrder = displayOrder;
        this.inspectionThreshold = inspectionThreshold;
        this.inspectionPolicy = inspectionPolicy;
        this.deleted = deleted;
        this.deletedAt = deletedAt;
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