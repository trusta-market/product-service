package com.trustamarket.productservice.infrastructure.persistence;

import com.trustamarket.productservice.domain.product.ProductStatus;
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

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"images"})
    Optional<ProductJpaEntity> findById(@NonNull UUID id);

    // 외부 노출용 — 소프트 삭제 제외
    @EntityGraph(attributePaths = {"images"})
    Optional<ProductJpaEntity> findByIdAndDeletedFalse(UUID id);

    // 최신 상품 — 소프트 삭제 제외 + ON_SALE만
    @EntityGraph(attributePaths = {"images"})
    List<ProductJpaEntity> findTop10ByDeletedFalseAndStatusOrderByCreatedAtDesc(ProductStatus status);

    // 판매자별 — 소프트 삭제 제외
    @EntityGraph(attributePaths = {"images"})
    Page<ProductJpaEntity> findBySellerIdAndDeletedFalseOrderByCreatedAtDesc(UUID sellerId, Pageable pageable);

    // 카테고리별 — 소프트 삭제 제외 + ON_SALE만
    @EntityGraph(attributePaths = {"images"})
    Page<ProductJpaEntity> findByCategoryIdAndStatusAndDeletedFalseOrderByCreatedAtDesc(
            UUID categoryId, ProductStatus status, Pageable pageable);
}