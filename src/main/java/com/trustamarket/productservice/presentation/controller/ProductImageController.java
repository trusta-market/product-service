package com.trustamarket.productservice.presentation.controller;

import com.trustamarket.common.response.CommonResponse;
import com.trustamarket.common.util.SecurityUtil;
import com.trustamarket.productservice.application.ProductImageAppService;
import com.trustamarket.productservice.domain.product.Product;
import com.trustamarket.productservice.domain.product.ProductImage;
import com.trustamarket.productservice.presentation.dto.request.ProductImageReorderRequest;
import com.trustamarket.productservice.presentation.dto.response.ProductImageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products/{productId}/images")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageAppService productImageAppService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public List<ProductImageResponse> addImage(
            @PathVariable UUID productId,
            @RequestPart("file") MultipartFile file
    ) {
        UUID sellerId = SecurityUtil.getCurrentUserIdOrThrow();
        Product product = productImageAppService.addImage(productId, sellerId, file);
        return toActiveImageResponses(product);
    }

    @DeleteMapping("/{imageId}")
    public CommonResponse<Void> removeImage(
            @PathVariable UUID productId,
            @PathVariable UUID imageId
    ) {
        UUID sellerId = SecurityUtil.getCurrentUserIdOrThrow();
        productImageAppService.removeImage(productId, sellerId, imageId);
        return CommonResponse.of(HttpStatus.OK.value(), null);
    }

    @PatchMapping("/{imageId}/thumbnail")
    public List<ProductImageResponse> changeThumbnail(
            @PathVariable UUID productId,
            @PathVariable UUID imageId
    ) {
        UUID sellerId = SecurityUtil.getCurrentUserIdOrThrow();
        Product product = productImageAppService.changeThumbnail(productId, sellerId, imageId);
        return toActiveImageResponses(product);
    }

    @PatchMapping("/reorder")
    public List<ProductImageResponse> reorderImages(
            @PathVariable UUID productId,
            @Valid @RequestBody ProductImageReorderRequest request
    ) {
        UUID sellerId = SecurityUtil.getCurrentUserIdOrThrow();
        Product product = productImageAppService.reorderImages(productId, sellerId, request.getImageIds());
        return toActiveImageResponses(product);
    }

    private List<ProductImageResponse> toActiveImageResponses(Product product) {
        List<ProductImage> images = product.getImages() == null ? Collections.emptyList() : product.getImages();
        return images.stream()
                .filter(img -> !img.isDeleted())
                .map(ProductImageResponse::from)
                .collect(Collectors.toList());
    }
}
