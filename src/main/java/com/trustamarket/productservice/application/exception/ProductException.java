package com.trustamarket.productservice.application.exception;

import com.trustamarket.common.exception.BadRequestException;
import com.trustamarket.productservice.application.exception.errorcode.ProductErrorCode;
import lombok.Getter;

@Getter
public class ProductException extends BadRequestException {
    private final ProductErrorCode errorCode;

    public ProductException(ProductErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
}
