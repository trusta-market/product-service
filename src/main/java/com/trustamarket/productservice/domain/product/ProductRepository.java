package com.trustamarket.productservice.domain.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    Optional<Product> findById(UUID productId);
    Optional<Product> findByIdWithImages(UUID productId);
    Page<Product> findBySellerId(UUID sellerId, Pageable pageable);
    Page<Product> findByCategoryId(UUID categoryId, Pageable pageable);
    List<Product> findTop10ByOrderByCreatedAtDesc();
    Product save(Product product);
}