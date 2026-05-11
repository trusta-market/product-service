package com.trustamarket.productservice.application.port;

import com.trustamarket.productservice.domain.product.Product;

import java.util.UUID;

public interface ProductSearchPort {
    void index(Product product);
    void delete(UUID id);
}
