package com.trustamarket.productservice.application.event;

import com.trustamarket.productservice.domain.product.Product;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ProductCreatedEvent {
    private final Product product;
}
