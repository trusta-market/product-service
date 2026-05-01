package com.trustamarket.productservice.application.port;

import com.trustamarket.productservice.domain.product.Product;

import java.util.UUID;

public interface ProductEventPublishPort {
    void publishProductCreated(Product product);
    void publishProductDeleted(UUID productId);
}
