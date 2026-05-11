package com.trustamarket.productservice.infrastructure.persistence.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProductJpaEntity is a Querydsl query type for ProductJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProductJpaEntity extends EntityPathBase<ProductJpaEntity> {

    private static final long serialVersionUID = -745569268L;

    public static final QProductJpaEntity productJpaEntity = new QProductJpaEntity("productJpaEntity");

    public final ComparablePath<java.util.UUID> categoryId = createComparable("categoryId", java.util.UUID.class);

    public final DateTimePath<java.time.Instant> createdAt = createDateTime("createdAt", java.time.Instant.class);

    public final StringPath description = createString("description");

    public final EnumPath<com.trustamarket.productservice.domain.product.ProductGrade> grade = createEnum("grade", com.trustamarket.productservice.domain.product.ProductGrade.class);

    public final ComparablePath<java.util.UUID> id = createComparable("id", java.util.UUID.class);

    public final ListPath<ProductImageJpaEntity, QProductImageJpaEntity> images = this.<ProductImageJpaEntity, QProductImageJpaEntity>createList("images", ProductImageJpaEntity.class, QProductImageJpaEntity.class, PathInits.DIRECT2);

    public final EnumPath<com.trustamarket.productservice.domain.product.InspectionStatus> inspectionStatus = createEnum("inspectionStatus", com.trustamarket.productservice.domain.product.InspectionStatus.class);

    public final NumberPath<Long> price = createNumber("price", Long.class);

    public final ComparablePath<java.util.UUID> sellerId = createComparable("sellerId", java.util.UUID.class);

    public final EnumPath<com.trustamarket.productservice.domain.product.ProductStatus> status = createEnum("status", com.trustamarket.productservice.domain.product.ProductStatus.class);

    public final NumberPath<Long> suggestedPrice = createNumber("suggestedPrice", Long.class);

    public final StringPath title = createString("title");

    public final DateTimePath<java.time.Instant> updatedAt = createDateTime("updatedAt", java.time.Instant.class);

    public QProductJpaEntity(String variable) {
        super(ProductJpaEntity.class, forVariable(variable));
    }

    public QProductJpaEntity(Path<? extends ProductJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QProductJpaEntity(PathMetadata metadata) {
        super(ProductJpaEntity.class, metadata);
    }

}

