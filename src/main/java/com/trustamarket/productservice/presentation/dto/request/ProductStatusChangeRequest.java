package com.trustamarket.productservice.presentation.dto.request;

import com.trustamarket.productservice.domain.product.ProductStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor // 빌더 및 테스트 코드 작성을 위해 추가
public class ProductStatusChangeRequest {

    @NotNull(message = "변경할 상태는 필수입니다.")
    private ProductStatus status;
}
