package com.trustamarket.productservice.domain.product;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProduct is a Querydsl query type for Product
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProduct extends EntityPathBase<Product> {

    private static final long serialVersionUID = -1134893422L;

    public static final QProduct product = new QProduct("product");

    public final ComparablePath<java.util.UUID> categoryId = createComparable("categoryId", java.util.UUID.class);

    public final DateTimePath<java.time.Instant> createdAt = createDateTime("createdAt", java.time.Instant.class);

    public final StringPath description = createString("description");

    public final EnumPath<ProductGrade> grade = createEnum("grade", ProductGrade.class);

    public final ComparablePath<java.util.UUID> id = createComparable("id", java.util.UUID.class);

    public final ListPath<ProductImage, QProductImage> images = this.<ProductImage, QProductImage>createList("images", ProductImage.class, QProductImage.class, PathInits.DIRECT2);

    public final EnumPath<InspectionStatus> inspectionStatus = createEnum("inspectionStatus", InspectionStatus.class);

    public final ComparablePath<java.util.UUID> inspectorId = createComparable("inspectorId", java.util.UUID.class);

    public final NumberPath<Long> price = createNumber("price", Long.class);

    public final ComparablePath<java.util.UUID> sellerId = createComparable("sellerId", java.util.UUID.class);

    public final EnumPath<ProductStatus> status = createEnum("status", ProductStatus.class);

    public final NumberPath<Long> suggestedPrice = createNumber("suggestedPrice", Long.class);

    public final StringPath title = createString("title");

    public final DateTimePath<java.time.Instant> updatedAt = createDateTime("updatedAt", java.time.Instant.class);

    public QProduct(String variable) {
        super(Product.class, forVariable(variable));
    }

    public QProduct(Path<? extends Product> path) {
        super(path.getType(), path.getMetadata());
    }

    public QProduct(PathMetadata metadata) {
        super(Product.class, metadata);
    }

}

