package com.trustamarket.productservice.domain.product;

import com.trustamarket.productservice.application.exception.InvalidStatusTransitionException;
import com.trustamarket.productservice.application.exception.errorcode.ProductErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductDomainService {

    // 상품 등록 시 초기 상태 결정
    public ProductStatus decideInitialStatus(Product product, int highValueThreshold) {
        return product.requiresInspection(highValueThreshold)
                ? ProductStatus.PENDING_INSPECTION
                : ProductStatus.ON_SALE;
    }

    public void validateGrade(ProductGrade grade) {
        if (grade == null) {
            throw new IllegalArgumentException("상품 등급은 필수입니다.");
        }
    }

    // 상태 전이 검증
    public void validateStatusTransition(ProductStatus current, ProductStatus next) {
        boolean valid = switch (current) {
            case PENDING_INSPECTION -> next == ProductStatus.ON_SALE;
            case ON_SALE            -> next == ProductStatus.RESERVED
                                    || next == ProductStatus.SOLD_OUT;
            case RESERVED           -> next == ProductStatus.SOLD_OUT
                                    || next == ProductStatus.ON_SALE;
            case SOLD_OUT           -> false;
        };
        if (!valid) {
            throw new InvalidStatusTransitionException(ProductErrorCode.INVALID_STATUS_TRANSITION);
        }
    }
}