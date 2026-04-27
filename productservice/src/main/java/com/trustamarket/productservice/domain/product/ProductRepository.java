package com.trustamarket.productservice.domain.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, Long> {


    @EntityGraph(attributePaths = {"images"})
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdWithImages(@Param("id") Long id);

    // 특정 판매자의 모든 판매 목록
    Page<Product> findBySellerId(UUID sellerId, Pageable pageable);

    //특정 카테고리 판매 목록
    Page<Product> findByCategoryId(UUID categoryId, Pageable pageable);

    // 최근 등록일 기준 조회
    @EntityGraph(attributePaths = {"images"})
    List<Product> findTop10ByOrderByCreatedAtDesc();
}