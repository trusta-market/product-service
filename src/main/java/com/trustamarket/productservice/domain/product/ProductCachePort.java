package com.trustamarket.productservice.domain.product;

import java.util.List;
import java.util.Optional;

/**
 * Product 관련 cache 의 도메인 port.
 * 구현체 (Caffeine / Redis 등) 는 infrastructure layer 에서 adapter 로 제공.
 * 도메인은 cache 의 존재만 알고 구현 방식은 모름.
 */
public interface ProductCachePort {

    /** 최신 상품 목록 cache 조회. 없으면 empty. */
    Optional<List<Product>> getLatest();

    /** 최신 상품 목록 cache 저장. TTL 은 adapter 가 결정. */
    void putLatest(List<Product> products);

    /** 최신 상품 목록 cache 무효화 (product 생성/수정 시). */
    void evictLatest();
}
