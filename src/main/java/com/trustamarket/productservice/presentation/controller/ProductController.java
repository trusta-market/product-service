package com.trustamarket.productservice.presentation.controller;

import com.trustamarket.common.util.SecurityUtil;
import com.trustamarket.productservice.application.ProductCommandService;
import com.trustamarket.productservice.application.ProductQueryService;
import com.trustamarket.productservice.domain.product.Product;
import com.trustamarket.productservice.domain.product.ProductGrade;
import com.trustamarket.productservice.presentation.dto.request.InspectionRequestDto;
import com.trustamarket.productservice.presentation.dto.request.InspectionResultRequest;
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

    @PostMapping("/{productId}/inspection")
    @ResponseStatus(HttpStatus.OK)
    public void requestInspection(
            @PathVariable UUID productId,
            @Valid @RequestBody InspectionRequestDto request
    ) {
        UUID sellerId = SecurityUtil.getCurrentUserIdOrThrow();
        productCommandService.requestInspection(productId, sellerId, request.getCenterId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(
            @Valid @RequestBody ProductCreateRequest request
    ) {
        UUID sellerId = SecurityUtil.getCurrentUserIdOrThrow();
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

    @GetMapping("/{productId}")
    public ProductResponse findById(@PathVariable UUID productId) {
        return ProductResponse.from(productQueryService.findById(productId));
    }

    @GetMapping("/seller/{sellerId}")
    public Page<ProductResponse> findBySellerId(
            @PathVariable UUID sellerId,
            Pageable pageable
    ) {
        return productQueryService.findBySellerId(sellerId, pageable)
                .map(ProductResponse::from);
    }

    @GetMapping("/category/{categoryId}")
    public Page<ProductResponse> findByCategoryId(
            @PathVariable UUID categoryId,
            Pageable pageable
    ) {
        return productQueryService.findByCategoryId(categoryId, pageable)
                .map(ProductResponse::from);
    }

    @GetMapping("/latest")
    public List<ProductResponse> findLatest() {
        return productQueryService.findLatest().stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
    }

    @PutMapping("/{productId}")
    public ProductResponse update(
            @PathVariable UUID productId,
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        UUID sellerId = SecurityUtil.getCurrentUserIdOrThrow();
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

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID productId) {
        UUID sellerId = SecurityUtil.getCurrentUserIdOrThrow();
        productCommandService.deleteProduct(productId, sellerId);
    }

    @PatchMapping("/{productId}/status")
    public ProductResponse changeStatus(
            @PathVariable UUID productId,
            @Valid @RequestBody ProductStatusChangeRequest request
    ) {
        UUID sellerId = SecurityUtil.getCurrentUserIdOrThrow();
        return ProductResponse.from(
                productCommandService.changeStatus(productId, request.getStatus(), sellerId)
        );
    }

    @PatchMapping("/{productId}/inspection/start")
    public ProductResponse startInspection(@PathVariable UUID productId) {
        UUID inspectorId = SecurityUtil.getCurrentUserIdOrThrow();
        return ProductResponse.from(productCommandService.startInspection(productId, inspectorId));
    }

    @PatchMapping("/{productId}/inspection/complete")
    public ProductResponse completeInspection(
            @PathVariable UUID productId,
            @RequestParam ProductGrade grade
    ) {
        UUID inspectorId = SecurityUtil.getCurrentUserIdOrThrow();
        return ProductResponse.from(productCommandService.completeInspection(productId, grade, inspectorId));
    }

    @PatchMapping("/{productId}/inspection/fail")
    public ProductResponse failInspection(@PathVariable UUID productId) {
        UUID inspectorId = SecurityUtil.getCurrentUserIdOrThrow();
        return ProductResponse.from(productCommandService.failInspection(productId, inspectorId));
    }

    @PostMapping("/{productId}/inspection-result")
    public ProductResponse respondToInspectionResult(
            @PathVariable UUID productId,
            @Valid @RequestBody InspectionResultRequest request
    ) {
        UUID sellerId = SecurityUtil.getCurrentUserIdOrThrow();
        Product product = Boolean.TRUE.equals(request.getAccepted())
                ? productCommandService.acceptInspectionResult(productId, sellerId)
                : productCommandService.rejectInspectionResult(productId, sellerId, request.getReason());
        return ProductResponse.from(product);
    }
}
