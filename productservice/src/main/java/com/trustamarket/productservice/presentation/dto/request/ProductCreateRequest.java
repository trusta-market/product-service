package com.trustamarket.productservice.presentation.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor // 테스트 및 빌더 사용을 위해 추가
public class ProductCreateRequest {

    @NotBlank(message = "상품명은 필수입니다.")
    @Size(max = 100, message = "상품명은 100자 이내여야 합니다.")
    private String title;

    private String description;

    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
    private Long price;

    @NotNull(message = "카테고리는 필수입니다.")
    private UUID categoryId;

    // 이미지 관련 검증을 위해 추가
    @Size(max = 10, message = "이미지는 최대 10개까지 등록 가능합니다.")
    private List<String> imageUrls;
}
