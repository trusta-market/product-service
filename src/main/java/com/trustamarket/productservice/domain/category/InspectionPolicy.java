package com.trustamarket.productservice.domain.category;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InspectionPolicy {

    ALWAYS("항상 검수"),
    PRICE_BASED("금액 기준 검수"),
    NEVER("검수 없음");

    private final String description;
}
