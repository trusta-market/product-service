package com.trustamarket.productservice.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trustamarket.productservice.application.event.kafka.ProductCreatedEvent;
import com.trustamarket.productservice.application.exception.CategoryNotFoundException;
import com.trustamarket.productservice.application.exception.ProductException;
import com.trustamarket.productservice.application.exception.errorcode.ProductErrorCode;
import com.trustamarket.productservice.application.port.OutboxEventRepository;
import com.trustamarket.productservice.infrastructure.kafka.KafkaTopics;
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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductCommandServiceTest {

    @Mock ProductRepository productRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock ProductDomainService productDomainService;
    @Mock OutboxEventRepository outboxEventRepository;
    @Mock ObjectMapper objectMapper;

    @InjectMocks ProductCommandService service;

    private Category category(String name, Integer threshold) {
        return Category.builder()
                .categoryId(UUID.randomUUID())
                .name(name)
                .depth(0)
                .displayOrder(0)
                .inspectionThreshold(threshold)
                .build();
    }

    @Test
    @DisplayName("고가 (가격 ≥ 임계치) — PENDING_INSPECTION + InspectionStatus.PENDING")
    void highValue_goesToInspection() throws JsonProcessingException {
        UUID sellerId = UUID.randomUUID();
        Category cat = category("명품/럭셔리", 300_000);
        when(categoryRepository.findById(cat.getCategoryId())).thenReturn(Optional.of(cat));
        when(productDomainService.requiresInspection(cat, 500_000L)).thenReturn(true);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"event\":\"product-created\"}");

        Product result = service.create(sellerId, "에르메스 가방", "설명", 500_000L, cat.getCategoryId(), null);

        assertThat(result.getStatus()).isEqualTo(ProductStatus.PENDING_INSPECTION);
        assertThat(result.getInspectionStatus()).isEqualTo(InspectionStatus.PENDING);

        // outbox 저장 계약 검증 — ProductCreatedEvent 직렬화 후 PRODUCT_CREATED_TOPIC 으로 1회 저장돼야 함
        verify(objectMapper, times(1)).writeValueAsString(any(ProductCreatedEvent.class));
        verify(outboxEventRepository, times(1))
                .save(eq(KafkaTopics.PRODUCT_CREATED_TOPIC), eq("{\"event\":\"product-created\"}"));
    }

    @Test
    @DisplayName("저가 (가격 < 임계치) — ON_SALE 즉시 + InspectionStatus.NONE")
    void lowValue_goesToSaleImmediately() {
        UUID sellerId = UUID.randomUUID();
        Category cat = category("명품/럭셔리", 300_000);
        when(categoryRepository.findById(cat.getCategoryId())).thenReturn(Optional.of(cat));
        when(productDomainService.requiresInspection(cat, 200_000L)).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product result = service.create(sellerId, "보세 가방", "설명", 200_000L, cat.getCategoryId(), null);

        assertThat(result.getStatus()).isEqualTo(ProductStatus.ON_SALE);
        assertThat(result.getInspectionStatus()).isEqualTo(InspectionStatus.NONE);
    }

    @Test
    @DisplayName("패션 카테고리 (임계치 20만) + 30만원 → PENDING_INSPECTION + PENDING")
    void fashionCategory_priceAboveThreshold() {
        UUID sellerId = UUID.randomUUID();
        Category cat = category("패션", 200_000);
        when(categoryRepository.findById(cat.getCategoryId())).thenReturn(Optional.of(cat));
        when(productDomainService.requiresInspection(cat, 300_000L)).thenReturn(true);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product result = service.create(sellerId, "디자이너 자켓", "설명", 300_000L, cat.getCategoryId(), null);

        assertThat(result.getStatus()).isEqualTo(ProductStatus.PENDING_INSPECTION);
        assertThat(result.getInspectionStatus()).isEqualTo(InspectionStatus.PENDING);
    }

    @Test
    @DisplayName("패션 카테고리 (임계치 20만) + 10만원 → ON_SALE + NONE")
    void fashionCategory_priceBelowThreshold() {
        UUID sellerId = UUID.randomUUID();
        Category cat = category("패션", 200_000);
        when(categoryRepository.findById(cat.getCategoryId())).thenReturn(Optional.of(cat));
        when(productDomainService.requiresInspection(cat, 100_000L)).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product result = service.create(sellerId, "기본 티셔츠", "설명", 100_000L, cat.getCategoryId(), null);

        assertThat(result.getStatus()).isEqualTo(ProductStatus.ON_SALE);
        assertThat(result.getInspectionStatus()).isEqualTo(InspectionStatus.NONE);
    }

    @Test
    @DisplayName("경계값 — 가격이 임계치와 정확히 같으면 PENDING_INSPECTION + PENDING (>= 비교)")
    void priceEqualsThreshold_goesToInspection() {
        UUID sellerId = UUID.randomUUID();
        Category cat = category("명품/럭셔리", 300_000);
        when(categoryRepository.findById(cat.getCategoryId())).thenReturn(Optional.of(cat));
        when(productDomainService.requiresInspection(cat, 300_000L)).thenReturn(true);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product result = service.create(sellerId, "딱 임계치 상품", "설명", 300_000L, cat.getCategoryId(), null);

        assertThat(result.getStatus()).isEqualTo(ProductStatus.PENDING_INSPECTION);
        assertThat(result.getInspectionStatus()).isEqualTo(InspectionStatus.PENDING);
    }

    @Test
    @DisplayName("price null → InvalidPriceException (ErrorCode: INVALID_PRICE)")
    void priceNull_throws() {
        UUID sellerId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        // validatePrice 가 카테고리 조회 전에 실행되므로 categoryRepository stub 불필요

        assertThatThrownBy(() ->
                service.create(sellerId, "title", "desc", null, categoryId, null)
        )
                .isInstanceOf(ProductException.class)
                .extracting(e -> ((ProductException) e).getErrorCode())
                .isEqualTo(ProductErrorCode.INVALID_PRICE);

        // 카테고리 조회가 호출되지 않았음을 명시적으로 검증 (회귀 방지)
        verify(categoryRepository, never()).findById(any());
    }

    @Test
    @DisplayName("price 음수 → InvalidPriceException (ErrorCode: INVALID_PRICE)")
    void priceNegative_throws() {
        UUID sellerId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        // validatePrice 가 카테고리 조회 전에 실행되므로 categoryRepository stub 불필요

        assertThatThrownBy(() ->
                service.create(sellerId, "title", "desc", -1L, categoryId, null)
        )
                .isInstanceOf(ProductException.class)
                .extracting(e -> ((ProductException) e).getErrorCode())
                .isEqualTo(ProductErrorCode.INVALID_PRICE);

        verify(categoryRepository, never()).findById(any());
    }

    @Test
    @DisplayName("카테고리 없으면 CategoryNotFoundException")
    void categoryNotFound() {
        UUID sellerId = UUID.randomUUID();
        UUID missingId = UUID.randomUUID();
        when(categoryRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.create(sellerId, "title", "desc", 50_000L, missingId, null)
        ).isInstanceOf(CategoryNotFoundException.class);

        // 실패 경로에서는 outbox 이벤트가 저장되면 안 됨
        verify(outboxEventRepository, never()).save(anyString(), anyString());
    }

    @Test
    @DisplayName("상품 등록 성공 시 ProductCreatedEvent 가 outbox 에 저장된다 (저장 계약 전용)")
    void create_savesProductCreatedOutboxEvent() throws JsonProcessingException {
        UUID sellerId = UUID.randomUUID();
        Category cat = category("패션", 200_000);
        when(categoryRepository.findById(cat.getCategoryId())).thenReturn(Optional.of(cat));
        when(productDomainService.requiresInspection(cat, 100_000L)).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        when(objectMapper.writeValueAsString(any(ProductCreatedEvent.class)))
                .thenReturn("{\"productId\":\"x\"}");

        service.create(sellerId, "기본 티셔츠", "설명", 100_000L, cat.getCategoryId(), null);

        verify(objectMapper, times(1)).writeValueAsString(any(ProductCreatedEvent.class));
        verify(outboxEventRepository, times(1))
                .save(eq(KafkaTopics.PRODUCT_CREATED_TOPIC), eq("{\"productId\":\"x\"}"));
    }

    @Test
    @DisplayName("이벤트 직렬화 실패 시 IllegalStateException 으로 전환된다")
    void create_serializationFailure_throwsIllegalState() throws JsonProcessingException {
        UUID sellerId = UUID.randomUUID();
        Category cat = category("패션", 200_000);
        when(categoryRepository.findById(cat.getCategoryId())).thenReturn(Optional.of(cat));
        when(productDomainService.requiresInspection(cat, 100_000L)).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        when(objectMapper.writeValueAsString(any(ProductCreatedEvent.class)))
                .thenThrow(new JsonProcessingException("boom") {});

        assertThatThrownBy(() ->
                service.create(sellerId, "기본 티셔츠", "설명", 100_000L, cat.getCategoryId(), null)
        ).isInstanceOf(IllegalStateException.class);

        verify(outboxEventRepository, never()).save(anyString(), anyString());
    }
}