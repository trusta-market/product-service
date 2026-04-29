package com.trustamarket.productservice.application.exception;

import com.trustamarket.common.exception.NotFoundException;
import com.trustamarket.productservice.application.exception.errorcode.ProductErrorCode;

public class ProductNotFoundException extends NotFoundException {
    public ProductNotFoundException(Long id) {
        super(ProductErrorCode.PRODUCT_NOT_FOUND.getMessage() + " id: " + id);
    }
}
