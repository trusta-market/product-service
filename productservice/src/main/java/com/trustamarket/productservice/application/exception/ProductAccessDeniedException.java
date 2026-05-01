package com.trustamarket.productservice.application.exception;

import com.trustamarket.common.exception.ForbiddenException;
import com.trustamarket.productservice.application.exception.errorcode.ProductErrorCode;
import lombok.Getter;

@Getter
public class ProductAccessDeniedException extends RuntimeException {

    private final ProductErrorCode errorCode;

    public ProductAccessDeniedException(ProductErrorCode errorCode) {
        super(errorCode.getMessage()); // Enum에 정의된 메시지를 부모 클래스로 전달
        this.errorCode = errorCode;
    }
}