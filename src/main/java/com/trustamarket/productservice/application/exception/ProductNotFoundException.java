package com.trustamarket.productservice.application.exception;

import com.trustamarket.common.exception.NotFoundException;
import com.trustamarket.productservice.application.exception.errorcode.ProductErrorCode;
import lombok.Getter;


@Getter
public class ProductNotFoundException extends NotFoundException {
    private final ProductErrorCode errorCode;

    public ProductNotFoundException(ProductErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
