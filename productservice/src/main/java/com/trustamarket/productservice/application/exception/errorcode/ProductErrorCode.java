package com.trustamarket.productservice.application.exception.errorcode;

import com.trustamarket.common.exception.ErrorCodeSpec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements ErrorCodeSpec {

    PRODUCT_NOT_FOUND("PRODUCT_001", HttpStatus.NOT_FOUND, "존재하지 않는 상품입니다.", null),
    CATEGORY_NOT_FOUND("PRODUCT_002", HttpStatus.NOT_FOUND, "존재하지 않는 카테고리입니다.", null),
    PRODUCT_ACCESS_DENIED("PRODUCT_003", HttpStatus.FORBIDDEN, "해당 상품에 대한 권한이 없습니다.", null),
    INVALID_STATUS_TRANSITION("PRODUCT_004", HttpStatus.BAD_REQUEST, "유효하지 않은 상태 전이입니다.", null);

    private final String code;
    private final HttpStatus status;
    private final String message;
    private final String field;
}
