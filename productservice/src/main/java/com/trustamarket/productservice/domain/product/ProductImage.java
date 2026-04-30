package com.trustamarket.productservice.domain.product;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    private String imageUrl;
    private int sortOrder;
    private boolean isThumbnail;

    // Soft Delete를 위한 필드 추가
    private boolean isDeleted = false;
    private LocalDateTime deletedAt;

    private ProductImage(UUID id, String imageUrl, int sortOrder, boolean isThumbnail) {
        validate(imageUrl);
        this.id = id;
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
        this.isThumbnail = isThumbnail;
    }


    // 사진 신규 등록
    public static ProductImage create(String imageUrl, int sortOrder, boolean isThumbnail) {
        return new ProductImage(null, imageUrl, sortOrder, isThumbnail);
    }

    // db에 저장된 정보를 가져와서 객체로 만들때 사용
    public static ProductImage of(UUID id, String imageUrl, int sortOrder, boolean isThumbnail) {
        return new ProductImage(id, imageUrl, sortOrder, isThumbnail);
    }

    // db 엔티티로부터 도메인 객체를 restore하기 위한 메서드
    public static ProductImage restore(UUID id, String imageUrl, int sortOrder, boolean isThumbnail) {
        return new ProductImage(id, imageUrl, sortOrder, isThumbnail);
    }

    // Soft Delete 실행 메서드 추가
    public void delete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
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
            throw new IllegalArgumentException("이미지 URL은 비어있을 수 없습니다.");
        }
    }
}
