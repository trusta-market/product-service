# PRD — 카테고리별 임계치 기반 검수 분기

> 작업 시점: 2026-05-04 ~
> 브랜치: `feature/category-inspection-threshold`
> 선행 PR: 없음 (current `dev` 브랜치 기반)

---

## 1. 개요

### 배경
상품 등록 시 가격이 **카테고리별 임계치 이상**이면 **검수(`PENDING_INSPECTION`)** 단계를 거친 후 `ON_SALE`로 전환되어야 함. 현재는 가격/카테고리에 무관하게 `requiresInspection=true` 가 하드코딩되어 분기가 동작하지 않음.

### 목표
`ProductCommandService.create()` 가 카테고리별 임계치를 적용해 검수 분기를 동적으로 결정하도록 수정.

---

## 2. 현재 코드 상태 (버그)

### 위치
`productservice/src/main/java/com/trustamarket/productservice/application/ProductCommandService.java` line 43–51

```java
Product product = Product.create(
    sellerId, categoryId, title, description, price,
    null,   // grade
    true    // ← requiresInspection 하드코딩
);
```

### 미사용 (이미 만들어진 자산)
- `domain.product.ProductDomainService.decideInitialStatus(Product, int)` — 임계치 받아 상태 결정
- `domain.product.Product.requiresInspection(int highValueThreshold)` — `price >= threshold` 판정
- `domain.category.CategoryThreshold` — 카테고리명별 임계치 enum (LUXURY 30만, FASHION 20만 등)

### 결과
모든 상품이 `PENDING_INSPECTION` 으로 등록되거나, 다른 path로 즉시 `ON_SALE` 되는 등 의도와 다른 동작.

---

## 3. 의도된 흐름

```
[상품 등록]
    ↓
카테고리 → 임계치 lookup (CategoryThreshold.getThreshold(name))
    ↓
가격 ≥ 임계치  ────────▶ PENDING_INSPECTION + InspectionStatus.PENDING
    │                          ↓
    │                   (검수자) startInspection → IN_PROGRESS
    │                          ↓
    │                   (검수자) completeInspection(grade) → PASSED + ON_SALE
    │                       또는 failInspection → FAILED (반송)
    │
    └─ 가격 < 임계치  ──▶ ON_SALE 즉시 (InspectionStatus.NONE)
```

### 예시
| 카테고리 | 임계치 | 가격 | 결과 |
|---|---|---|---|
| 명품/럭셔리 | 30만 | 50만 | **PENDING_INSPECTION** |
| 명품/럭셔리 | 30만 | 20만 | ON_SALE |
| 패션 | 20만 | 50만 | **PENDING_INSPECTION** |
| 패션 | 20만 | 10만 | ON_SALE |

---

## 4. 변경 범위

### 4.1. ✅ 이번 PR에서 할 것

**Application 레이어**
- `ProductCommandService.create()` —
  - `categoryRepository.findById()` 결과의 카테고리명을 기반으로 `CategoryThreshold.getThreshold()` 호출
  - `Product.create()` 의 `requiresInspection` 인자에 동적 계산값 (`price >= threshold`) 전달
- `ProductDomainService.decideInitialStatus()` 가 실제로 호출되도록 (또는 같은 로직을 service 안에서 직접 처리 — 편의대로)

**Domain 레이어**
- 변경 없음 (`Product.requiresInspection`, `CategoryThreshold`, `ProductDomainService.decideInitialStatus` 모두 그대로 사용)

**테스트**
- `ProductCommandService` 단위 테스트:
  - 임계치 이상 → `PENDING_INSPECTION`
  - 임계치 미만 → `ON_SALE` 즉시
  - 카테고리별 임계치 다름 (명품/패션 둘 다 검증)
- `ProductDomainService.decideInitialStatus` 단위 테스트 (이미 있다면 유지)

### 4.2. ❌ 이번 PR에서 안 할 것

- **order ↔ product 통신** — order-service 가 주문 생성 시 product 가 `ON_SALE` 인지 검증하는 cross-service 호출. 별도 PR (`product-service` 측 internal API + `order-service` 측 Feign client)
- **카테고리 임계치 DB화** — 현재 enum 하드코딩 유지. 운영 시 카테고리 admin 화면에서 수정 가능하도록 DB 컬럼화는 후속 작업
- **검수자 권한 분리** — `INSPECTOR` role 기반 endpoint 분리는 이미 적용됐다면 유지, 별도 작업 X

---

## 5. 카테고리 ↔ 임계치 매핑 방법 (결정사항)

`CategoryThreshold` enum 의 `categoryName` 과 `Category` 엔티티의 이름 필드가 일치해야 lookup 동작.

**옵션 A**: 카테고리 엔티티의 `name` 필드를 enum `categoryName` 과 정확히 매칭 (string 비교)
- 장점: 코드 변경 작거나 없음
- 단점: 카테고리 이름 오타 시 `DEFAULT(10만)` 폴백 — 조용히 잘못된 임계치 적용

**옵션 B**: `Category` 엔티티에 `categoryThreshold` 컬럼 추가 (enum/int)
- 장점: 명시적
- 단점: DB 스키마 변경 필요

→ **옵션 A 채택 (MVP)**. 추후 옵션 B 마이그레이션 가능. 단, 카테고리 이름이 enum 과 정확히 일치해야 함을 unit 테스트로 가드.

---

## 6. 검증 시나리오

```
[테스트 1] 명품 카테고리 + 50만원
  given: Category(name="명품/럭셔리"), price=500_000
  when:  productCommandService.create(...)
  then:  product.status == PENDING_INSPECTION
         product.inspectionStatus == PENDING

[테스트 2] 명품 카테고리 + 20만원
  given: Category(name="명품/럭셔리"), price=200_000
  when:  productCommandService.create(...)
  then:  product.status == ON_SALE
         product.inspectionStatus == NONE

[테스트 3] 카테고리 이름 미스매치 → DEFAULT 폴백
  given: Category(name="존재하지않는카테고리"), price=50_000
  when:  productCommandService.create(...)
  then:  threshold = 100_000 (DEFAULT)
         price < threshold → ON_SALE
```

수동 검증 (시연용):
1. 명품 카테고리 + 고가 → DBeaver 에서 `p_products.status = PENDING_INSPECTION` 확인
2. 검수자가 `completeInspection(GRADE_A)` 호출 → `status = ON_SALE` 전이 확인

---

## 7. TODO (다음 PR)

- order-service ↔ product-service Feign 통신
  - product-service: `GET /internal/v1/products/{id}` (외부 노출 internal API)
  - order-service: `ProductFeignClient` + `ProductInfoPort` (hexagonal port out)
  - 주문 생성 시 product 가 `ON_SALE` 인지 검증, 가격/이름 snapshot 가져오기
- `CategoryThreshold` DB 마이그레이션 (admin 화면에서 수정 가능)
- 검수 결과 이벤트 (`ProductInspectedEvent`) 가 ON_SALE 전이 시 외부에 발행되는 흐름 점검
