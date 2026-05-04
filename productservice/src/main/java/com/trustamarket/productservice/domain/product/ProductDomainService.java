package com.trustamarket.productservice.domain.product;

import com.trustamarket.productservice.application.exception.InvalidStatusTransitionException;
import com.trustamarket.productservice.application.exception.errorcode.ProductErrorCode;
import com.trustamarket.productservice.domain.category.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductDomainService {

    // 카테고리 정책(ALWAYS/PRICE_BASED/NEVER) + 가격으로 검수 필요 여부 판단
    public boolean requiresInspection(Category category, int price) {
        return category.requiresInspection(price);
    }

    public void validateGrade(ProductGrade grade) {
        if (grade == null) throw new IllegalArgumentException("상품 등급은 필수입니다.");
    }

    public void validateStatusTransition(ProductStatus current, ProductStatus next) {
        boolean valid = switch (current) {
            case PENDING_INSPECTION  -> next == ProductStatus.ON_SALE;
            case INSPECTION_REJECTED -> next == ProductStatus.PENDING_INSPECTION; // 추가
            case ON_SALE             -> next == ProductStatus.RESERVED;
            case RESERVED            -> next == ProductStatus.SOLD_OUT || next == ProductStatus.ON_SALE;
            case SOLD_OUT            -> false;
        };
        if (!valid) {
            throw new InvalidStatusTransitionException(ProductErrorCode.INVALID_STATUS_TRANSITION);
        }
    }
}