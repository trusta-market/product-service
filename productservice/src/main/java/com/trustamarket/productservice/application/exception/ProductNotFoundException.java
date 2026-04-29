package com.trustamarket.productservice.application.exception;

import com.trustamarket.common.exception.NotFoundException;
import com.trustamarket.productservice.application.exception.errorcode.ProductErrorCode;

import java.util.UUID;

public class ProductNotFoundException extends NotFoundException {
    public ProductNotFoundException(UUID id) {
        super(ProductErrorCode.PRODUCT_NOT_FOUND.getMessage() + " id: " + id);
    }
}
