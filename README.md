# Product Service

## 개요

중고거래 플랫폼 **Trusta Market**의 상품 도메인을 담당하는 마이크로서비스입니다.
상품 등록/조회/수정/삭제, 이미지 업로드(S3), 카테고리별 검수 정책 적용,
검수 서비스(inspection-service)와의 Kafka 기반 이벤트 연동을 처리합니다.

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.5 |
| ORM | Spring Data JPA (Hibernate 6), QueryDSL 5.1 |
| Database | PostgreSQL 17, Flyway (마이그레이션) |
| Messaging | Apache Kafka |
| Storage | AWS S3 (ap-northeast-2) |
| Security | Spring Security (Gateway 헤더 기반 인증) |
| Service Discovery | Spring Cloud Eureka Client |
| Config | Spring Cloud Config (Config Server) |
| Build | Gradle |

---

## 아키텍처

레이어드 아키텍처 + 도메인 중심 설계를 따릅니다.

```
presentation      → Controller, DTO (Request/Response)
application       → Service, UseCase, Event Listener, Port Interface
domain            → Product, ProductImage, Category, 도메인 정책
infrastructure    → JPA, Kafka, S3 구현체
```

---

## 주요 기능

- **상품 CRUD**: 상품 등록/조회/수정/삭제
- **이미지 관리**: S3 업로드, 썸네일 지정, 순서 변경, Soft Delete
- **카테고리 검수 정책**: ALWAYS / PRICE_BASED / NEVER 정책으로 검수 필요 여부 자동 판단
- **검수 연동**: inspection-service와 Kafka 이벤트로 검수 요청/결과 수신
- **상품 상태 관리**: ON_SALE → RESERVED → SOLD_OUT 등 상태 전이

---

## 상품 상태 흐름

```
상품 등록
  │
  ├─ 검수 불필요 → ON_SALE (즉시 판매 가능)
  │
  └─ 검수 필요 → PENDING_INSPECTION
                    │
                    ▼ (검수 신청)
               IN_PROGRESS
                    │
                    ▼ (검수 완료 이벤트 수신)
               PRICE_SUGGESTED (등급 + 제안가격 저장)
                    │
          ┌─────────┴─────────┐
          │ 판매자 수락         │ 판매자 거절
          ▼                   ▼
       ON_SALE            INSPECTION_REJECTED
    (검수완료 뱃지 ✅)        (재검수 신청 가능)
```

---

## Kafka 이벤트 흐름

### product-service → inspection-service

| 토픽 | 발행 시점 | 페이로드 |
|------|-----------|----------|
| `product.inspection-requested` | 판매자 검수 신청 시 | productId, sellerId, originalPrice, currency, inspectionType |

### inspection-service → product-service

| 토픽 | 수신 시점 | 페이로드 |
|------|-----------|----------|
| `inspection.completed` | 검수 완료 시 | productId, inspectorId, grade, suggestedPriceAmount |

### order-service → product-service

| 토픽 | 수신 시점 | 페이로드 |
|------|-----------|----------|
| `order.product.sold-out` | 주문 확정 시 | productId |

---

## 카테고리 검수 정책

| 정책 | 설명 | 해당 카테고리 예시 |
|------|------|------------------|
| `ALWAYS` | 금액 무관 무조건 검수 | 예술품/그림, 티켓/상품권 |
| `PRICE_BASED` | 기준 금액 이상 시 검수 | 패션(200,000원), 전자기기(500,000원) |
| `NEVER` | 검수 없음 | 식품/건강, 도서/잡지 |

---

## API 엔드포인트

### 상품

| Method | Path | 설명 |
|--------|------|------|
| `POST` | `/api/products` | 상품 등록 |
| `GET` | `/api/products/{productId}` | 상품 단건 조회 |
| `GET` | `/api/products/seller/{sellerId}` | 판매자별 상품 조회 |
| `GET` | `/api/products/category/{categoryId}` | 카테고리별 상품 조회 |
| `GET` | `/api/products/latest` | 최신 상품 조회 |
| `PUT` | `/api/products/{productId}` | 상품 수정 |
| `DELETE` | `/api/products/{productId}` | 상품 삭제 |
| `PATCH` | `/api/products/{productId}/status` | 상품 상태 변경 |
| `POST` | `/api/products/{productId}/inspection` | 검수 신청 |
| `POST` | `/api/products/{productId}/inspection-result` | 검수 결과 수락/거절 |

### 이미지

| Method | Path | 설명 |
|--------|------|------|
| `POST` | `/api/products/{productId}/images` | 이미지 업로드 (multipart/form-data) |
| `DELETE` | `/api/products/{productId}/images/{imageId}` | 이미지 삭제 |
| `PATCH` | `/api/products/{productId}/images/{imageId}/thumbnail` | 대표 이미지 변경 |
| `PATCH` | `/api/products/{productId}/images/reorder` | 이미지 순서 변경 |

### 카테고리

| Method | Path | 설명 |
|--------|------|------|
| `GET` | `/api/categories` | 전체 카테고리 조회 |
| `GET` | `/api/categories/root` | 루트 카테고리 조회 |
| `GET` | `/api/categories/{categoryId}/sub` | 하위 카테고리 조회 |

---

## 요청 헤더

Gateway를 통해 주입되는 사용자 인증 헤더입니다.
로컬 개발 환경에서는 `trusta.security.trust-gateway-headers: true` 설정 시 직접 헤더를 전달할 수 있습니다.

| Header | 설명 |
|--------|------|
| `X-User-UUID` | 사용자 UUID |
| `X-User-Email` | 이메일 |
| `X-User-Name` | 이름 (URL 인코딩) |
| `X-User-Role` | 권한 (예: ROLE_USER) |
| `X-User-Slack-Id` | 슬랙 ID |
| `X-User-Enabled` | 활성 여부 (true/false) |

---

## 실행 순서

서비스 기동 전 아래 인프라가 먼저 실행되어야 합니다.

```
1. PostgreSQL
2. Kafka (+ Zookeeper)
3. Eureka Server
4. Config Server
5. product-service
```

### 환경변수

IntelliJ Run/Debug Configurations → Environment variables에 아래 값을 설정합니다.

| 변수 | 설명 |
|------|------|
| `AWS_ACCESS_KEY` | AWS Access Key |
| `AWS_SECRET_KEY` | AWS Secret Key |
| `AWS_S3_BUCKET` | S3 버킷명 (productservice-s3-bucket-kr) |
| `INTERNAL_SECRET` | 내부 서비스 간 인증 시크릿 |

### DB 마이그레이션

Flyway를 사용하며, 서비스 기동 시 자동으로 마이그레이션이 실행됩니다.

```
V1__create_tables.sql       — 테이블 생성
V2__insert_category_data.sql — 카테고리 초기 데이터
V3__add_indexes.sql          — 인덱스 추가
V4__add_suggested_price.sql  — suggested_price 컬럼 추가
```

---

## S3 이미지 업로드

- **버킷**: `productservice-s3-bucket-kr`
- **리전**: `ap-northeast-2` (서울)
- **저장 경로**: `products/{UUID}.{확장자}`
- **허용 확장자**: jpg, jpeg, png, webp
- **최대 파일 크기**: 10MB

---

## 연관 서비스

| 서비스 | 연동 방식 | 설명 |
|--------|-----------|------|
| inspection-service | Kafka | 검수 요청 발행 / 검수 완료 결과 수신 |
| order-service | Kafka | 주문 확정 이벤트 수신 → SOLD_OUT 전환 |
| gateway | HTTP Header | 사용자 인증 정보 주입 |
| eureka-server | HTTP | 서비스 등록 및 디스커버리 |
| config-server | HTTP | 환경별 설정 주입 |