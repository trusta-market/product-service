package com.trustamarket.productservice.application.exception;

import com.trustamarket.common.exception.ForbiddenException;
import com.trustamarket.productservice.application.exception.errorcode.ProductErrorCode;

public class ProductAccessDeniedException extends ForbiddenException {
    public ProductAccessDeniedException() {
        super(ProductErrorCode.PRODUCT_ACCESS_DENIED);
    }
}