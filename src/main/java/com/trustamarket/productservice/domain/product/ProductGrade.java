package com.trustamarket.productservice.domain.product;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductGrade {

    S("S"),
    A("A"),
    B("B"),
    C("C");


    private final String description;
}
