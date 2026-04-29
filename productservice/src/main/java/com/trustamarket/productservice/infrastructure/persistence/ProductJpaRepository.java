package com.trustamarket.productservice.infrastructure.persistence;

import com.trustamarket.productservice.infrastructure.persistence.entity.ProductJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, UUID> {

    @EntityGraph(attributePaths = {"images"})
    List<ProductJpaEntity> findTop10ByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"images"})
    Page<ProductJpaEntity> findBySellerIdOrderByCreatedAtDesc(UUID sellerId, Pageable pageable);

    @EntityGraph(attributePaths = {"images"})
    Page<ProductJpaEntity> findByCategoryIdAndStatusOrderByCreatedAtDesc(
            UUID categoryId,
            com.trustamarket.productservice.domain.product.ProductStatus status,
            Pageable pageable
    );

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"images"})
    Optional<ProductJpaEntity> findById(@NonNull UUID id);
}