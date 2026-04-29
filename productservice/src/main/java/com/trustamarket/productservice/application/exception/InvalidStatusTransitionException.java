package com.trustamarket.productservice.application.exception;

import com.trustamarket.common.exception.BadRequestException;
import com.trustamarket.productservice.application.exception.errorcode.ProductErrorCode;

public class InvalidStatusTransitionException extends BadRequestException {
    public InvalidStatusTransitionException() {
        super(ProductErrorCode.INVALID_STATUS_TRANSITION);
    }
}