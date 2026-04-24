package com.trustamarket.productservice.domain.product;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductStatus {

    PENDING_INSPECTION("검수대기"),
    ON_SALE("판매중"),
    RESERVED("예약중"),
    SOLD_OUT("판매완료");

    private final String description;

    // 상품구매가능한지 확인
    public boolean isAvailableForOrder() {
        return this == ON_SALE;
    }
}
