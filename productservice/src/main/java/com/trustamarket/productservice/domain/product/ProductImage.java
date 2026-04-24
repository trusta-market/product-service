package com.trustamarket.productservice.domain.product;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String imageUrl;
    private int sortOrder;
    private boolean isThumbnail;

    private ProductImage(Long id, String imageUrl, int sortOrder, boolean isThumbnail) {
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
    public static ProductImage of(Long id, String imageUrl, int sortOrder, boolean isThumbnail) {
        return new ProductImage(id, imageUrl, sortOrder, isThumbnail);
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
