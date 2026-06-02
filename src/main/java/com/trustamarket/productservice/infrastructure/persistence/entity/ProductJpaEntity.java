package com.trustamarket.productservice.infrastructure.persistence.entity;

import com.trustamarket.productservice.domain.product.InspectionStatus;
import com.trustamarket.productservice.domain.product.ProductGrade;
import com.trustamarket.productservice.domain.product.ProductStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "p_products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProductJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(nullable = false)
    private UUID sellerId;

    @Column(nullable = false)
    private UUID categoryId;

    @Column
    private UUID inspectorId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Long price;

    @Column
    private Long suggestedPrice;

    @Enumerated(EnumType.STRING)
    private ProductGrade grade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InspectionStatus inspectionStatus;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<ProductImageJpaEntity> images = new ArrayList<>();

    @Column(nullable = false)
    private boolean deleted = false;

    @Column
    private Instant deletedAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @Builder
    public ProductJpaEntity(UUID id, UUID sellerId, UUID categoryId, UUID inspectorId, String title,
                            String description, Long price, Long suggestedPrice, ProductGrade grade,
                            ProductStatus status, InspectionStatus inspectionStatus,
                            List<ProductImageJpaEntity> images,  boolean deleted, Instant deletedAt,
                            Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.sellerId = sellerId;
        this.categoryId = categoryId;
        this.inspectorId = inspectorId;
        this.title = title;
        this.description = description;
        this.price = price;
        this.suggestedPrice = suggestedPrice;
        this.grade = grade;
        this.status = status;
        this.inspectionStatus = inspectionStatus;
        if (images != null) {
            this.images = new ArrayList<>(images);
            this.images.forEach(img -> img.assignProduct(this));
        }
        this.deleted = deleted;
        this.deletedAt = deletedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void addImage(ProductImageJpaEntity image) {
        image.assignProduct(this);
        this.images.add(image);
    }
}