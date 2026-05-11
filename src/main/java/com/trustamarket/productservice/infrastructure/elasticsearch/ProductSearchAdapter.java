package com.trustamarket.productservice.infrastructure.elasticsearch;

import com.trustamarket.productservice.application.port.ProductSearchPort;
import com.trustamarket.productservice.domain.product.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductSearchAdapter implements ProductSearchPort {

    private final ProductSearchRepository productSearchRepository;

    @Override
    public void index(Product product) {
        try {
            ProductDocument document = ProductDocument.builder()
                    .id(product.getId().toString())
                    .title(product.getTitle())
                    .price(product.getPrice())
                    .status(product.getStatus().name())
                    .grade(product.getGrade() != null
                            ? product.getGrade().name() : null)
                    .categoryId(product.getCategoryId().toString())
                    .sellerId(product.getSellerId().toString())
                    .inspectionStatus(product.getInspectionStatus().name())
                    .build();

            productSearchRepository.save(document);
            log.info("ES 인덱싱 완료 - productId: {}", product.getId());
        } catch (Exception e) {
            log.error("ES 인덱싱 실패 - productId: {}", product.getId(), e);
        }
    }

    @Override
    public void delete(UUID id) {          // String → UUID 로 수정
        try {
            productSearchRepository.deleteById(id.toString()); // ES는 String id 사용
            log.info("ES 인덱스 삭제 완료 - productId: {}", id);
        } catch (Exception e) {
            log.error("ES 인덱스 삭제 실패 - productId: {}", id, e);
        }
    }
}