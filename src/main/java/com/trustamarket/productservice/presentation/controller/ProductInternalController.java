package com.trustamarket.productservice.presentation.controller;

import com.trustamarket.productservice.application.ProductQueryService;
import com.trustamarket.productservice.presentation.dto.response.ProductInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

// 다른 서비스(MSA 내부)가 호출하는 internal API.
// Gateway 라우팅 대상이 아니라 service-to-service 직접 호출 (Feign).
// 인증은 common 의 LoginFilter 가 X-User-* 헤더 신뢰 + FeignConfig 가 헤더 자동 전파로 보장.
@RestController
@RequestMapping("/internal/v1/products")
@RequiredArgsConstructor
public class ProductInternalController {

    private final ProductQueryService productQueryService;

    // 상품 단건 조회 — order-service 가 주문 생성 시 product 검증/snapshot 에 사용
    @GetMapping("/{productId}")
    public ProductInfoResponse getProductInfo(@PathVariable UUID productId) {
        return ProductInfoResponse.from(productQueryService.findById(productId));
    }
}
