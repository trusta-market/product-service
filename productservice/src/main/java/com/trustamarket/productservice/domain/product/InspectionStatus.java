package com.trustamarket.productservice.domain.product;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor // 생성자를 직접 안 써도 되게 Lombok이 도와줘요!
public enum InspectionStatus {

    NONE("검수 없음"),

    PENDING("검수 대기"),

    IN_PROGRESS("검수 중"),

    PASSED("검수 완료"),

    FAILED("검수 불합격");

    private final String description;

    // 상세페이지에서 "검수 완료" 표시 여부
    public boolean isVerified() {
        return this == PASSED;
    }
}
