package com.trustamarket.productservice.infrastructure.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

// JpaRepository처럼 기본 CRUD 자동 제공
// <ProductDocument, String>: 문서 타입, id 타입
public interface ProductSearchRepository
        extends ElasticsearchRepository<ProductDocument, String> {
}
