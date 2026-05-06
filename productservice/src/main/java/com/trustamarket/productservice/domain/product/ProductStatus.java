package com.trustamarket.productservice.domain.product;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.function.Consumer;

@Getter
@RequiredArgsConstructor
public enum ProductStatus {

    PENDING_INSPECTION("검수대기",
            Set.of(),
            product -> {}
    ),
    INSPECTION_REJECTED("검수반려",
            Set.of(PENDING_INSPECTION),
            Product::resubmitForInspection
    ),
    ON_SALE("판매중",
            Set.of(PENDING_INSPECTION),  // INSPECTION_REJECTED → resubmit 경우
            product -> {}
    ),
    RESERVED("예약중",
            Set.of(ON_SALE),
            Product::reserve
    ),
    SOLD_OUT("판매완료",
            Set.of(RESERVED),
            Product::completeSale
    );

    private final String             description;
    private final Set<ProductStatus> allowedFrom;   // 이 상태로 전이 가능한 이전 상태들
    private final Consumer<Product>  action;        // 전이 시 실행할 도메인 메서드

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
