package com.trustamarket.productservice.infrastructure.persistence;

import com.trustamarket.productservice.domain.product.Product;
import com.trustamarket.productservice.domain.product.ProductRepository;
import com.trustamarket.productservice.domain.product.ProductStatus;
import com.trustamarket.productservice.infrastructure.persistence.entity.ProductJpaEntity;
import com.trustamarket.productservice.infrastructure.persistence.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;
    private final ProductMapper productMapper;

    @Override
    public Optional<Product> findById(UUID productId) {
        // 내부 시스템 조회 — deleted 무관 (이벤트 처리 등)
        return productJpaRepository.findById(productId).map(productMapper::toDomain);
    }

    @Override
    public Optional<Product> findByIdWithImages(UUID productId) {
        // 외부 노출용 — 소프트 삭제 제외
        return productJpaRepository.findByProductIdAndDeletedFalse(productId).map(productMapper::toDomain);
    }

    @Override
    public Page<Product> findBySellerId(UUID sellerId, Pageable pageable) {
        return productJpaRepository
                .findBySellerIdAndDeletedFalseOrderByCreatedAtDesc(sellerId, pageable)
                .map(productMapper::toDomain);
    }

    @Override
    public Page<Product> findByCategoryId(UUID categoryId, Pageable pageable) {
        return productJpaRepository
                .findByCategoryIdAndStatusAndDeletedFalseOrderByCreatedAtDesc(categoryId, ProductStatus.ON_SALE, pageable)
                .map(productMapper::toDomain);
    }

    @Override
    public List<Product> findTop10ByOrderByCreatedAtDesc() {
        return productJpaRepository
                .findTop10ByDeletedFalseAndStatusOrderByCreatedAtDesc(ProductStatus.ON_SALE)
                .stream().map(productMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public Product save(Product product) {
        ProductJpaEntity jpaEntity = productMapper.toJpaEntity(product);
        return productMapper.toDomain(productJpaRepository.save(jpaEntity));
    }
}