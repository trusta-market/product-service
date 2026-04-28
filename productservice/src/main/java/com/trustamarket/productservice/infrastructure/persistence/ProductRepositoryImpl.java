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
    public Optional<Product> findById(UUID id) {
        return productJpaRepository.findById(id)
                .map(productMapper::toDomain);
    }

    @Override
    public Optional<Product> findByIdWithImages(UUID id) {
        // JpaRepository에 정의된 @EntityGraph 메서드 활용
        return productJpaRepository.findById(id)
                .map(productMapper::toDomain);
    }

    @Override
    public Page<Product> findBySellerId(UUID sellerId, Pageable pageable) {
        return productJpaRepository
                .findBySellerIdOrderByCreatedAtDesc(sellerId, pageable)
                .map(productMapper::toDomain);
    }

    @Override
    public Page<Product> findByCategoryId(UUID categoryId, Pageable pageable) {
        // 기본적으로 '판매 중'인 상품만 조회하도록 도메인 정책 반영
        return productJpaRepository
                .findByCategoryIdAndStatusOrderByCreatedAtDesc(
                        categoryId,
                        ProductStatus.ON_SALE,
                        pageable
                )
                .map(productMapper::toDomain);
    }

    @Override
    public List<Product> findTop10ByOrderByCreatedAtDesc() {
        return productJpaRepository.findTop10ByOrderByCreatedAtDesc()
                .stream()
                .map(productMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Product save(Product product) {
        // 도메인 -> 엔티티 변환 후 저장, 다시 도메인으로 변환하여 반환
        ProductJpaEntity jpaEntity = productMapper.toJpaEntity(product);
        ProductJpaEntity savedEntity = productJpaRepository.save(jpaEntity);
        return productMapper.toDomain(savedEntity);
    }

    @Override
    public void deleteById(UUID id) {
        productJpaRepository.deleteById(id);
    }
}