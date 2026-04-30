package com.trustamarket.productservice.application.exception;


import com.trustamarket.common.exception.NotFoundException;
import com.trustamarket.productservice.application.exception.errorcode.ProductErrorCode;
import lombok.Getter;

import java.util.UUID;

@Getter
public class CategoryNotFoundException extends NotFoundException {

    private final ProductErrorCode errorCode;

    public CategoryNotFoundException(ProductErrorCode errorCode) {
        // 부모인 NotFoundException이 ErrorCodeSpec 타입을 지원한다면 직접 넘기는 것이 가장 깔끔합니다.
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
