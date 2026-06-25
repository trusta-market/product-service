package com.trustamarket.productservice.domain.product;

import com.trustamarket.productservice.application.exception.InvalidImageUrlException;
import com.trustamarket.productservice.application.exception.errorcode.ProductErrorCode;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "imageId")
public class ProductImage {

    private UUID imageId;
    private String imageUrl;
    private int sortOrder;
    private boolean isThumbnail;

    // Soft Delete를 위한 필드 추가
    private boolean isDeleted = false;
    private Instant deletedAt;

    private ProductImage(UUID imageId, String imageUrl, int sortOrder, boolean isThumbnail, boolean isDeleted, Instant deletedAt) {
        validate(imageUrl);
        this.imageId = imageId;
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
        this.isThumbnail = isThumbnail;
        this.isDeleted = isDeleted;
        this.deletedAt = deletedAt;
    }


    // 사진 신규 등록
    public static ProductImage create(Product product, String imageUrl, int sortOrder, boolean isThumbnail) {
        return new ProductImage(null, imageUrl, sortOrder, isThumbnail, false, null);
    }

    // db에 저장된 정보를 가져와서 객체로 만들때 사용
    public static ProductImage of(UUID id, String imageUrl, int sortOrder, boolean isThumbnail) {
        return new ProductImage(id, imageUrl, sortOrder, isThumbnail,false, null);
    }

    // db 엔티티로부터 도메인 객체를 restore하기 위한 메서드
    public static ProductImage restore(UUID id, String imageUrl, int sortOrder, boolean isThumbnail, boolean isDeleted, Instant deletedAt) {
        return new ProductImage(id, imageUrl, sortOrder, isThumbnail,isDeleted, deletedAt);
    }

    // Soft Delete 실행 메서드 추가
    public void delete() {
        this.isDeleted = true;
        this.deletedAt = Instant.now();
    }

    // 사진 순서 변경
    public void changeSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    // 대표이미지 변경
    public void markAsThumbnail() {
        this.isThumbnail = true;
    }

    // 대표이미지 설정을 취소
    public void unmarkThumbnail() {
        this.isThumbnail = false;
    }

    // 이미지 url 존재 여부 확인
    private void validate(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new InvalidImageUrlException(ProductErrorCode.INVALID_IMAGE_URL);
        }
    }
}