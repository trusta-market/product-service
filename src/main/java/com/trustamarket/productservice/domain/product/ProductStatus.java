package com.trustamarket.productservice.domain.product;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Consumer;

@Getter
@RequiredArgsConstructor
public enum ProductStatus {

    PENDING_INSPECTION("검수대기",   product -> {}),
    INSPECTION_REJECTED("검수반려",  Product::resubmitForInspection),
    ON_SALE("판매중",                product -> {}),
    RESERVED("예약중",               Product::reserve),
    SOLD_OUT("판매완료",             Product::completeSale);

    private final String            description;
    private final Consumer<Product> action;
    private Set<ProductStatus>      allowedFrom;   // 지연 초기화

    // enum 상수가 모두 초기화된 뒤에 실행 — 순환 참조 문제 없음
    static {
        PENDING_INSPECTION .allowedFrom = EnumSet.noneOf(ProductStatus.class);
        INSPECTION_REJECTED.allowedFrom = EnumSet.of(PENDING_INSPECTION);
        ON_SALE            .allowedFrom = EnumSet.of(PENDING_INSPECTION, RESERVED);
        RESERVED           .allowedFrom = EnumSet.of(ON_SALE);
        SOLD_OUT           .allowedFrom = EnumSet.of(RESERVED, ON_SALE);
    }

    public boolean canTransitionFrom(ProductStatus current) {
        return allowedFrom.contains(current);
    }

    public void execute(Product product) {
        action.accept(product);
    }

    public boolean isAvailableForOrder() {
        return this == ON_SALE;
    }
}
