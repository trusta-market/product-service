package com.trustamarket.productservice.application.exception;

import com.trustamarket.productservice.application.exception.errorcode.ProductErrorCode;
import lombok.Getter;

@Getter
public class ImageNotFoundException extends RuntimeException {
    private final ProductErrorCode errorCode;

    public ImageNotFoundException(ProductErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}