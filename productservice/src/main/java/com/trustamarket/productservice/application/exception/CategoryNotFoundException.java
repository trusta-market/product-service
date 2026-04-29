package com.trustamarket.productservice.application.exception;


import com.trustamarket.common.exception.NotFoundException;
import com.trustamarket.productservice.application.exception.errorcode.ProductErrorCode;

public class CategoryNotFoundException extends NotFoundException {
    public CategoryNotFoundException(Long id) {
        super(ProductErrorCode.CATEGORY_NOT_FOUND.getMessage() + " id: " + id);
    }
}
