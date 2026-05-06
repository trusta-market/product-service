package com.trustamarket.productservice.domain.category;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum CategoryThreshold {

    LUXURY("명품/럭셔리",  InspectionPolicy.ALWAYS,       300_000),
    FASHION("패션",        InspectionPolicy.PRICE_BASED,  200_000),
    ELECTRONICS("전자기기", InspectionPolicy.PRICE_BASED, 500_000),
    SPORTS("스포츠/레저",  InspectionPolicy.PRICE_BASED,  300_000),
    HOBBY("취미/수집",     InspectionPolicy.PRICE_BASED,  200_000),
    LIFESTYLE("생활/기타", InspectionPolicy.PRICE_BASED,  200_000),
    DEFAULT("기본",        InspectionPolicy.PRICE_BASED,  100_000);

    private final String           categoryName;
    private final InspectionPolicy inspectionPolicy;
    private final int              threshold;

    public static int getThreshold(String name) {
        return findByName(name).getThreshold();
    }

    public static InspectionPolicy getPolicy(String name) {
        return findByName(name).getInspectionPolicy();
    }

    private static CategoryThreshold findByName(String name) {
        return Arrays.stream(values())
                .filter(v -> v.categoryName.equals(name))
                .findFirst()
                .orElse(DEFAULT);
    }
}
