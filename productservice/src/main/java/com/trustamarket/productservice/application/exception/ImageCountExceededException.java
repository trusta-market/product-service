package com.trustamarket.productservice.application.exception;

import com.trustamarket.productservice.application.exception.errorcode.ProductErrorCode;
import lombok.Getter;

@Getter
public class ImageCountExceededException extends RuntimeException {

    private final ProductErrorCode errorCode;

    public ImageCountExceededException(ProductErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
