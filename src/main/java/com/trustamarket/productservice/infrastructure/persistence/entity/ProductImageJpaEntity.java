package com.trustamarket.productservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "p_product_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductImageJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    // 양방향 관계 — product_id FK를 이 쪽에서 직접 관리
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductJpaEntity product;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private int sortOrder;

    @Column(nullable = false)
    private boolean isThumbnail;

    @Column(nullable = false)
    private boolean isDeleted = false;

    private Instant deletedAt;

    public static ProductImageJpaEntity create(ProductJpaEntity product, UUID id,
                                               String imageUrl, int sortOrder,
                                               boolean isThumbnail, boolean isDeleted,
                                               Instant deletedAt) {
        if (product == null) {
            throw new IllegalArgumentException("ProductImageJpaEntity 생성 시 product는 필수입니다.");
        }
        ProductImageJpaEntity entity = new ProductImageJpaEntity();
        entity.product = product;
        entity.id = id;
        entity.imageUrl = imageUrl;
        entity.sortOrder = sortOrder;
        entity.isThumbnail = isThumbnail;
        entity.isDeleted = isDeleted;
        entity.deletedAt = deletedAt;
        return entity;
    }

    // package-private — 관계 변경 진입점을 ProductJpaEntity.addImage()로 제한
    void assignProduct(ProductJpaEntity product) {
        this.product = product;
    }
}