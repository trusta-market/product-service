package com.trustamarket.productservice.presentation.controller;

import com.trustamarket.productservice.application.ProductImageAppService;
import com.trustamarket.productservice.domain.product.Product;
import com.trustamarket.productservice.presentation.dto.request.ProductImageReorderRequest;
import com.trustamarket.productservice.presentation.dto.response.ProductImageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products/{productId}/images")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageAppService productImageAppService;

    // 이미지 업로드
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public List<ProductImageResponse> addImage(
            @PathVariable UUID productId,
            @RequestHeader("X-User-Id") UUID sellerId,
            @RequestPart("file") MultipartFile file
    ) {
        Product product = productImageAppService.addImage(productId, sellerId, file);
        return product.getImages().stream()
                .filter(img -> !img.isDeleted())
                .map(ProductImageResponse::from)
                .collect(Collectors.toList());
    }

    // 이미지 삭제
    @DeleteMapping("/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeImage(
            @PathVariable UUID productId,
            @RequestHeader("X-User-Id") UUID sellerId,
            @PathVariable UUID imageId
    ) {
        productImageAppService.removeImage(productId, sellerId, imageId);
    }

    // 대표 이미지 변경
    @PatchMapping("/{imageId}/thumbnail")
    public List<ProductImageResponse> changeThumbnail(
            @PathVariable UUID productId,
            @RequestHeader("X-User-Id") UUID sellerId,
            @PathVariable UUID imageId
    ) {
        Product product = productImageAppService.changeThumbnail(productId, sellerId, imageId);
        return product.getImages().stream()
                .filter(img -> !img.isDeleted())
                .map(ProductImageResponse::from)
                .collect(Collectors.toList());
    }

    // 이미지 순서 변경
    @PatchMapping("/reorder")
    public List<ProductImageResponse> reorderImages(
            @PathVariable UUID productId,
            @RequestHeader("X-User-Id") UUID sellerId,
            @Valid @RequestBody ProductImageReorderRequest request
    ) {
        Product product = productImageAppService.reorderImages(productId, sellerId, request.getImageIds());
        return product.getImages().stream()
                .filter(img -> !img.isDeleted())
                .map(ProductImageResponse::from)
                .collect(Collectors.toList());
    }
}
