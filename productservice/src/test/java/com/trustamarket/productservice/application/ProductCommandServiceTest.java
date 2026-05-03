package com.trustamarket.productservice.application;

import com.trustamarket.productservice.application.exception.CategoryNotFoundException;
import com.trustamarket.productservice.domain.category.Category;
import com.trustamarket.productservice.domain.category.CategoryRepository;
import com.trustamarket.productservice.domain.product.InspectionStatus;
import com.trustamarket.productservice.domain.product.Product;
import com.trustamarket.productservice.domain.product.ProductDomainService;
import com.trustamarket.productservice.domain.product.ProductRepository;
import com.trustamarket.productservice.domain.product.ProductStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

// 카테고리별 임계치 기반 검수 분기 검증.
// 정공 흐름: 가격 ≥ 임계치(고가) → PENDING_INSPECTION, 가격 < 임계치(저가) → ON_SALE 즉시.
@ExtendWith(MockitoExtension.class)
class ProductCommandServiceTest {

    @Mock ProductRepository productRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock ProductDomainService productDomainService;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks ProductCommandService service;

    private Category category(String name, Integer threshold) {
        return Category.builder()
                .id(UUID.randomUUID())
                .name(name)
                .depth(0)
                .displayOrder(0)
                .highValueThreshold(threshold)
                .build();
    }

    @Test
    @DisplayName("고가 (가격 ≥ 임계치) — PENDING_INSPECTION + InspectionStatus.PENDING")
    void highValue_goesToInspection() {
        UUID sellerId = UUID.randomUUID();
        Category cat = category("명품/럭셔리", 300_000);
        when(categoryRepository.findById(cat.getId())).thenReturn(Optional.of(cat));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product result = service.create(sellerId, "에르메스 가방", "설명", 500_000, cat.getId());

        assertThat(result.getStatus()).isEqualTo(ProductStatus.PENDING_INSPECTION);
        assertThat(result.getInspectionStatus()).isEqualTo(InspectionStatus.PENDING);
    }

    @Test
    @DisplayName("저가 (가격 < 임계치) — ON_SALE 즉시 + InspectionStatus.NONE")
    void lowValue_goesToSaleImmediately() {
        UUID sellerId = UUID.randomUUID();
        Category cat = category("명품/럭셔리", 300_000);
        when(categoryRepository.findById(cat.getId())).thenReturn(Optional.of(cat));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product result = service.create(sellerId, "보세 가방", "설명", 200_000, cat.getId());

        assertThat(result.getStatus()).isEqualTo(ProductStatus.ON_SALE);
        assertThat(result.getInspectionStatus()).isEqualTo(InspectionStatus.NONE);
    }

    @Test
    @DisplayName("패션 카테고리 (임계치 20만) + 30만원 → PENDING_INSPECTION")
    void fashionCategory_priceAboveThreshold() {
        UUID sellerId = UUID.randomUUID();
        Category cat = category("패션", 200_000);
        when(categoryRepository.findById(cat.getId())).thenReturn(Optional.of(cat));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product result = service.create(sellerId, "디자이너 자켓", "설명", 300_000, cat.getId());

        assertThat(result.getStatus()).isEqualTo(ProductStatus.PENDING_INSPECTION);
    }

    @Test
    @DisplayName("패션 카테고리 (임계치 20만) + 10만원 → ON_SALE")
    void fashionCategory_priceBelowThreshold() {
        UUID sellerId = UUID.randomUUID();
        Category cat = category("패션", 200_000);
        when(categoryRepository.findById(cat.getId())).thenReturn(Optional.of(cat));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product result = service.create(sellerId, "기본 티셔츠", "설명", 100_000, cat.getId());

        assertThat(result.getStatus()).isEqualTo(ProductStatus.ON_SALE);
    }

    @Test
    @DisplayName("경계값 — 가격이 임계치와 정확히 같으면 PENDING_INSPECTION (>= 비교)")
    void priceEqualsThreshold_goesToInspection() {
        UUID sellerId = UUID.randomUUID();
        Category cat = category("명품/럭셔리", 300_000);
        when(categoryRepository.findById(cat.getId())).thenReturn(Optional.of(cat));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product result = service.create(sellerId, "딱 임계치 상품", "설명", 300_000, cat.getId());

        assertThat(result.getStatus()).isEqualTo(ProductStatus.PENDING_INSPECTION);
    }

    @Test
    @DisplayName("카테고리 없으면 CategoryNotFoundException")
    void categoryNotFound() {
        UUID sellerId = UUID.randomUUID();
        UUID missingId = UUID.randomUUID();
        when(categoryRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.create(sellerId, "title", "desc", 50_000, missingId)
        ).isInstanceOf(CategoryNotFoundException.class);
    }
}
