package com.trustamarket.productservice.infrastructure.persistence.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QProductImageJpaEntity is a Querydsl query type for ProductImageJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProductImageJpaEntity extends EntityPathBase<ProductImageJpaEntity> {

    private static final long serialVersionUID = 683742421L;

    public static final QProductImageJpaEntity productImageJpaEntity = new QProductImageJpaEntity("productImageJpaEntity");

    public final DateTimePath<java.time.Instant> deletedAt = createDateTime("deletedAt", java.time.Instant.class);

    public final ComparablePath<java.util.UUID> id = createComparable("id", java.util.UUID.class);

    public final StringPath imageUrl = createString("imageUrl");

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    public final BooleanPath isThumbnail = createBoolean("isThumbnail");

    public final NumberPath<Integer> sortOrder = createNumber("sortOrder", Integer.class);

    public QProductImageJpaEntity(String variable) {
        super(ProductImageJpaEntity.class, forVariable(variable));
    }

    public QProductImageJpaEntity(Path<? extends ProductImageJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QProductImageJpaEntity(PathMetadata metadata) {
        super(ProductImageJpaEntity.class, metadata);
    }

}

