package com.trustamarket.productservice.presentation.controller;

import com.trustamarket.productservice.application.ProductCommandService;
import com.trustamarket.productservice.application.ProductQueryService;
import com.trustamarket.productservice.domain.product.Product;
import com.trustamarket.productservice.domain.product.ProductGrade;
import com.trustamarket.productservice.presentation.dto.request.ProductCreateRequest;
import com.trustamarket.productservice.presentation.dto.request.ProductStatusChangeRequest;
import com.trustamarket.productservice.presentation.dto.request.ProductUpdateRequest;
import com.trustamarket.productservice.presentation.dto.response.ProductResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductCommandService productCommandService;
    private final ProductQueryService productQueryService;


     // 상품 등록
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(
            @RequestHeader("X-User-Id") UUID sellerId,
            @Valid @RequestBody ProductCreateRequest request
    ) {
        Product product = productCommandService.create(
                sellerId,
                request.getTitle(),
                request.getDescription(),
                request.getPrice(),
                request.getCategoryId(),
                request.getImageUrls()
        );
        return ProductResponse.from(product);
    }


     //상품 단건 조회
    @GetMapping("/{productId}")
    public ProductResponse findById(@PathVariable UUID productId) {
        return ProductResponse.from(productQueryService.findById(productId));
    }


     // 판매자별 상품 목록 조회
    @GetMapping("/seller/{sellerId}")
    public Page<ProductResponse> findBySellerId(
            @PathVariable UUID sellerId,
            Pageable pageable
    ) {
        return productQueryService.findBySellerId(sellerId, pageable)
                .map(ProductResponse::from);
    }


     // 카테고리별 상품 목록 조회
    @GetMapping("/category/{categoryId}")
    public Page<ProductResponse> findByCategoryId(
            @PathVariable UUID categoryId,
            Pageable pageable
    ) {
        return productQueryService.findByCategoryId(categoryId, pageable)
                .map(ProductResponse::from);
    }


     // 최신 상품 목록 조회
    @GetMapping("/latest")
    public List<ProductResponse> findLatest() {
        return productQueryService.findLatest().stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
    }


     //상품 수정
    @PutMapping("/{productId}")
    public ProductResponse update(
            @PathVariable UUID productId,
            @RequestHeader("X-User-Id") UUID sellerId,
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        Product product = productCommandService.update(
                productId,
                sellerId,
                request.getTitle(),
                request.getDescription(),
                request.getPrice(),
                request.getCategoryId(),
                request.getImageUrls()
        );
        return ProductResponse.from(product);
    }


     //상품 삭제
    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID productId,
            @RequestHeader("X-User-Id") UUID sellerId
    ) {
        productCommandService.deleteProduct(productId, sellerId);
    }


    //상품 상태 변경
    @PatchMapping("/{productId}/status")
    public ProductResponse changeStatus(
            @PathVariable UUID productId,
            @RequestHeader("X-User-Id") UUID sellerId,
            @Valid @RequestBody ProductStatusChangeRequest request
    ) {
        return ProductResponse.from(
                productCommandService.changeStatus(productId, request.getStatus(), sellerId)
        );
    }


    //검수 시작 (검수자 권한 필요 기능)
    @PatchMapping("/{productId}/inspection/start")
    public ProductResponse startInspection(@PathVariable UUID productId) {
        return ProductResponse.from(productCommandService.startInspection(productId));
    }


    //검수 완료 및 등급 확정
    @PatchMapping("/{productId}/inspection/complete")
    public ProductResponse completeInspection(
            @PathVariable UUID productId,
            @RequestParam ProductGrade grade
    ) {
        return ProductResponse.from(productCommandService.completeInspection(productId, grade));
    }


    //검수 불합격 처리
    @PatchMapping("/{productId}/inspection/fail")
    public ProductResponse failInspection(@PathVariable UUID productId) {
        return ProductResponse.from(productCommandService.failInspection(productId));
    }
}
