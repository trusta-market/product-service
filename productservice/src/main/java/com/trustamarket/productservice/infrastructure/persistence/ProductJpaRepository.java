package com.trustamarket.productservice.infrastructure.persistence;

import com.trustamarket.productservice.domain.product.InspectionStatus;
import com.trustamarket.productservice.domain.product.Product;
import com.trustamarket.productservice.domain.product.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {


     // 상품 상세 조회 시 이미지 목록까지 한 번에 가져오기 (N+1 방지)
    @EntityGraph(attributePaths = {"images"})
    Optional<Product> findById(UUID id);


     // 특정 판매자의 상품 목록 조회 (최신순 페이징)
    Page<Product> findBySellerIdOrderByCreatedAtDesc(UUID sellerId, Pageable pageable);


     // 특정 카테고리의 판매 중인 상품 목록 조회
    Page<Product> findByCategoryIdAndStatusOrderByCreatedAtDesc(
            UUID categoryId,
            ProductStatus status,
            Pageable pageable
    );


     // 검수가 필요한 상품 목록 조회 (관리자용)
    @Query("SELECT p FROM Product p WHERE p.inspectionStatus = :inspectionStatus")
    Page<Product> findByInspectionStatus(
            @Param("inspectionStatus") InspectionStatus inspectionStatus,
            Pageable pageable
    );


     // 특정 상태의 상품 개수 확인
    long countByStatus(ProductStatus status);
}
