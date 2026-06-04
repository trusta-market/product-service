package com.trustamarket.productservice.presentation.dto.response;

import com.trustamarket.productservice.domain.product.ProductImage;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE) // 빌더 외의 직접 생성을 제한
public class ProductImageResponse {

    private final UUID imageId;
    private final String imageUrl;
    private final int sortOrder;
    private final boolean isThumbnail;

    public static ProductImageResponse from(ProductImage image) {
        // null 방어 코드 추가 (선택 사항)
        if (image == null) return null;

        return ProductImageResponse.builder()
                .imageId(image.getImageId())
                .imageUrl(image.getImageUrl())
                .sortOrder(image.getSortOrder())
                .isThumbnail(image.isThumbnail())
                .build();
    }
}
