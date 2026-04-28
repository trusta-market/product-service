package com.trustamarket.productservice.infrastructure.persistence;

import com.trustamarket.productservice.domain.product.InspectionStatus;
import com.trustamarket.productservice.domain.product.Product;
import com.trustamarket.productservice.domain.product.ProductStatus;
import com.trustamarket.productservice.infrastructure.persistence.entity.ProductJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, UUID> {

    @EntityGraph(attributePaths = {"images"})
    List<ProductJpaEntity> findTop10ByOrderByCreatedAtDesc();

    Page<ProductJpaEntity> findBySellerIdOrderByCreatedAtDesc(UUID sellerId, Pageable pageable);

    Page<ProductJpaEntity> findByCategoryIdAndStatusOrderByCreatedAtDesc(
            UUID categoryId,
            com.trustamarket.productservice.domain.product.ProductStatus status,
            Pageable pageable
    );
}