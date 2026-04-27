package com.trustamarket.productservice.domain.category;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum CategoryThreshold {
    LUXURY("명품/럭셔리", 300000),
    FASHION("패션", 200000),
    ELECTRONICS("전자기기", 500000),
    SPORTS("스포츠/레저", 300000),
    HOBBY("취미/수집", 200000),
    LIFESTYLE("생활/기타", 200000),
    DEFAULT("기본", 100000);

    private final String categoryName;
    private final int threshold;

    public static int getThreshold(String name) {
        return Arrays.stream(values())
                .filter(v -> v.categoryName.equals(name))
                .findFirst()
                .orElse(CategoryThreshold.DEFAULT)
                .getThreshold();
    }
}
