package com.trustamarket.productservice.infrastructure.persistence.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCategoryJpaEntity is a Querydsl query type for CategoryJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCategoryJpaEntity extends EntityPathBase<CategoryJpaEntity> {

    private static final long serialVersionUID = -972761277L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCategoryJpaEntity categoryJpaEntity = new QCategoryJpaEntity("categoryJpaEntity");

    public final NumberPath<Integer> depth = createNumber("depth", Integer.class);

    public final NumberPath<Integer> displayOrder = createNumber("displayOrder", Integer.class);

    public final ComparablePath<java.util.UUID> id = createComparable("id", java.util.UUID.class);

    public final EnumPath<com.trustamarket.productservice.domain.category.InspectionPolicy> inspectionPolicy = createEnum("inspectionPolicy", com.trustamarket.productservice.domain.category.InspectionPolicy.class);

    public final NumberPath<Integer> inspectionThreshold = createNumber("inspectionThreshold", Integer.class);

    public final StringPath name = createString("name");

    public final QCategoryJpaEntity parent;

    public QCategoryJpaEntity(String variable) {
        this(CategoryJpaEntity.class, forVariable(variable), INITS);
    }

    public QCategoryJpaEntity(Path<? extends CategoryJpaEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCategoryJpaEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCategoryJpaEntity(PathMetadata metadata, PathInits inits) {
        this(CategoryJpaEntity.class, metadata, inits);
    }

    public QCategoryJpaEntity(Class<? extends CategoryJpaEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.parent = inits.isInitialized("parent") ? new QCategoryJpaEntity(forProperty("parent"), inits.get("parent")) : null;
    }

}

