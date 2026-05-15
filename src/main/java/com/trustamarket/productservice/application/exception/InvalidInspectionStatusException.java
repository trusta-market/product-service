package com.trustamarket.productservice.application.exception;

import com.trustamarket.common.exception.BadRequestException;
import com.trustamarket.productservice.application.exception.errorcode.ProductErrorCode;
import lombok.Getter;

@Getter
public class InvalidInspectionStatusException extends BadRequestException {
    private final ProductErrorCode errorCode;

    public InvalidInspectionStatusException(ProductErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
}
