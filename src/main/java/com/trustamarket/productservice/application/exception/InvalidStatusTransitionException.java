package com.trustamarket.productservice.application.exception;

import com.trustamarket.common.exception.BadRequestException;
import com.trustamarket.productservice.application.exception.errorcode.ProductErrorCode;
import lombok.Getter;

@Getter
public class InvalidStatusTransitionException extends BadRequestException {
    private final ProductErrorCode errorCode;

    public InvalidStatusTransitionException(ProductErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
}