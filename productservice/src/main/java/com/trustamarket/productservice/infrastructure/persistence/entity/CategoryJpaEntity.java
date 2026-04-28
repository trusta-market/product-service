package com.trustamarket.productservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "p_categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private CategoryJpaEntity parent;

    @Column(nullable = false)
    private int depth;

    @Column(nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private Integer highValueThreshold;

    @Builder
    public CategoryJpaEntity(UUID id, String name, CategoryJpaEntity parent,
                             int depth, int displayOrder, Integer highValueThreshold) {
        this.id = id;
        this.name = name;
        this.parent = parent;
        this.depth = depth;
        this.displayOrder = displayOrder;
        this.highValueThreshold = highValueThreshold;
    }
}
