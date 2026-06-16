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
    INVALID_STATUS_TRANSITION("PRODUCT_004", HttpStatus.BAD_REQUEST, "유효하지 않은 상태 전이입니다.", null),

    IMAGE_NOT_FOUND("PRODUCT_005", HttpStatus.NOT_FOUND, "존재하지 않거나 이미 삭제된 이미지입니다.", null),
    IMAGE_COUNT_EXCEEDED("PRODUCT_006", HttpStatus.BAD_REQUEST, "이미지는 최대 10장까지 등록 가능합니다.", null),
    // ProductErrorCode.java에 추가
    INVALID_IMAGE_URL("PRODUCT_007", HttpStatus.BAD_REQUEST, "이미지 URL은 필수입니다.", "imageUrl"),
    INVALID_INSPECTION_STATUS("PRODUCT_008", HttpStatus.BAD_REQUEST, "검수 중인 상품만 검수 결과를 받을 수 있습니다.", null),
    INVALID_PRICE("PRODUCT_009", HttpStatus.BAD_REQUEST, "가격은 필수이며 0 이상이어야 합니다.", "price");

    private final String code;
    private final HttpStatus status;
    private final String message;
    private final String field;
}
