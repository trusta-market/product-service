package com.trustamarket.productservice.domain.product;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductStatus {

    PENDING_INSPECTION("검수대기"),
    INSPECTION_REJECTED("검수반려"),   // 추가 — 검수 불합격 후 판매자 반송 대기
    ON_SALE("판매중"),
    RESERVED("예약중"),
    SOLD_OUT("판매완료");

    private final String description;

    public boolean isAvailableForOrder() {
        return this == ON_SALE;
    }
}
