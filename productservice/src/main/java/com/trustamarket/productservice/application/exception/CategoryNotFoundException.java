package com.trustamarket.productservice.application.exception;


import com.trustamarket.common.exception.NotFoundException;
import com.trustamarket.productservice.application.exception.errorcode.ProductErrorCode;

import java.util.UUID;

public class CategoryNotFoundException extends NotFoundException {
    public CategoryNotFoundException(UUID id) {
        super(ProductErrorCode.CATEGORY_NOT_FOUND.getMessage() + " id: " + id);
    }
}
