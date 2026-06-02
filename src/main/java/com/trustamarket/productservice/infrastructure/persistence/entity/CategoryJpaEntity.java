package com.trustamarket.productservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.trustamarket.productservice.domain.category.InspectionPolicy;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "p_categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID categoryId;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private CategoryJpaEntity parent;

    @Column(nullable = false)
    private int depth;

    @Column(nullable = false)
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
    public CategoryJpaEntity(UUID categoryId, String name, CategoryJpaEntity parent,
                             int depth, int displayOrder, Integer inspectionThreshold, InspectionPolicy inspectionPolicy, boolean deleted, Instant deletedAt) {
        this.categoryId = categoryId;
        this.name = name;
        this.parent = parent;
        this.depth = depth;
        this.displayOrder = displayOrder;
        this.inspectionThreshold = inspectionThreshold;
        this.inspectionPolicy    = inspectionPolicy;
        this.deleted = deleted;
        this.deletedAt = deletedAt;
    }
}
