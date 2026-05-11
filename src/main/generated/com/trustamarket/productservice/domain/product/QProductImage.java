package com.trustamarket.productservice.domain.product;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QProductImage is a Querydsl query type for ProductImage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProductImage extends EntityPathBase<ProductImage> {

    private static final long serialVersionUID = 1675632105L;

    public static final QProductImage productImage = new QProductImage("productImage");

    public final DateTimePath<java.time.Instant> deletedAt = createDateTime("deletedAt", java.time.Instant.class);

    public final ComparablePath<java.util.UUID> id = createComparable("id", java.util.UUID.class);

    public final StringPath imageUrl = createString("imageUrl");

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    public final BooleanPath isThumbnail = createBoolean("isThumbnail");

    public final NumberPath<Integer> sortOrder = createNumber("sortOrder", Integer.class);

    public QProductImage(String variable) {
        super(ProductImage.class, forVariable(variable));
    }

    public QProductImage(Path<? extends ProductImage> path) {
        super(path.getType(), path.getMetadata());
    }

    public QProductImage(PathMetadata metadata) {
        super(ProductImage.class, metadata);
    }

}

