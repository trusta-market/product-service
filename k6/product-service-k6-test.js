/**
 * k6 Load Test Script - product-service
 *
 * 실행 방법:
 *   k6 run product-service-k6-test.js
 *
 * 환경변수로 서버 주소 지정:
 *   k6 run -e BASE_URL=http://localhost:8080 product-service-k6-test.js
 *
 * HTML 리포트 출력:
 *   k6 run --out json=result.json product-service-k6-test.js
 */

import http from "k6/http";
import { check, group, sleep } from "k6";
import { Trend, Rate, Counter } from "k6/metrics";

// ──────────────────────────────────────────
// 환경 설정
// ──────────────────────────────────────────
const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";

// 게이트웨이가 신뢰하는 헤더를 직접 주입 (trust-gateway-headers: true 일 때 사용)
// 실제 셀러 UUID로 교체하세요.
const SELLER_ID = __ENV.SELLER_ID || "00000000-0000-0000-0000-000000000001";

// 테스트에 사용할 카테고리 UUID (DB에 존재하는 값으로 교체하세요)
const CATEGORY_ID = __ENV.CATEGORY_ID || "00000000-0000-0000-0000-000000000010";

// 게이트웨이 헤더 (common 모듈의 SecurityUtil 이 X-User-Id 헤더를 읽음)
const AUTH_HEADERS = {
  "Content-Type": "application/json",
  "X-User-Id": SELLER_ID,
  "X-User-Role": "SELLER",
};

// ──────────────────────────────────────────
// 커스텀 메트릭
// ──────────────────────────────────────────
const createDuration = new Trend("product_create_duration", true);
const getDuration    = new Trend("product_get_duration",    true);
const listDuration   = new Trend("product_list_duration",   true);
const errorRate      = new Rate("error_rate");
const createdCount   = new Counter("products_created");

// ──────────────────────────────────────────
// 부하 시나리오 설정
// ──────────────────────────────────────────
export const options = {
  scenarios: {
    // 1) 점진적 부하 증가 (Ramp-up)
    ramp_up: {
      executor: "ramping-vus",
      startVUs: 1,
      stages: [
        { duration: "30s", target: 10 }, // 30초 동안 10명까지 증가
        { duration: "1m",  target: 10 }, // 1분간 10명 유지
        { duration: "20s", target: 0  }, // 20초 동안 0명으로 감소
      ],
      gracefulRampDown: "10s",
    },
  },
  thresholds: {
    // 전체 요청의 95%가 500ms 이내 응답
    http_req_duration: ["p(95)<500"],
    // 에러율 5% 미만
    error_rate: ["rate<0.05"],
    // 상품 생성 API 95%가 1초 이내
    product_create_duration: ["p(95)<1000"],
    // 상품 조회 API 95%가 300ms 이내
    product_get_duration: ["p(95)<300"],
  },
};

// ──────────────────────────────────────────
// 헬퍼 함수
// ──────────────────────────────────────────
function checkResponse(res, tag) {
  const ok = res.status >= 200 && res.status < 300;
  errorRate.add(!ok);
  check(res, {
    [`[${tag}] status 2xx`]: (r) => ok,
  });
  return ok;
}

// ──────────────────────────────────────────
// 기본 시나리오 (VU당 반복 실행)
// ──────────────────────────────────────────
export default function () {

  // ── 1. 상품 생성 ──────────────────────────
  let createdProductId = null;

  group("상품 생성 (POST /api/products)", () => {
    const payload = JSON.stringify({
      title: `테스트 상품 ${Date.now()}`,
      description: "k6 부하 테스트용 상품입니다.",
      price: 15000,
      categoryId: CATEGORY_ID,
      imageUrls: [],
    });

    const res = http.post(`${BASE_URL}/api/products`, payload, {
      headers: AUTH_HEADERS,
      tags: { name: "product_create" },
    });

    createDuration.add(res.timings.duration);

    if (checkResponse(res, "상품 생성") && res.body) {
      try {
        const body = JSON.parse(res.body);
        createdProductId = body.productId;
        createdCount.add(1);
      } catch (_) {}
    }
  });

  sleep(0.5);

  // ── 2. 상품 단건 조회 ─────────────────────
  if (createdProductId) {
    group("상품 단건 조회 (GET /api/products/:id)", () => {
      const res = http.get(`${BASE_URL}/api/products/${createdProductId}`, {
        headers: AUTH_HEADERS,
        tags: { name: "product_get" },
      });

      getDuration.add(res.timings.duration);
      checkResponse(res, "상품 단건 조회");
    });
  }

  sleep(0.5);

  // ── 3. 최신 상품 목록 조회 ────────────────
  group("최신 상품 목록 (GET /api/products/latest)", () => {
    const res = http.get(`${BASE_URL}/api/products/latest`, {
      headers: AUTH_HEADERS,
      tags: { name: "product_list_latest" },
    });

    listDuration.add(res.timings.duration);
    checkResponse(res, "최신 상품 목록");
  });

  sleep(0.5);

  // ── 4. 셀러별 상품 목록 조회 ──────────────
  group("셀러별 상품 목록 (GET /api/products/seller/:id)", () => {
    const res = http.get(
      `${BASE_URL}/api/products/seller/${SELLER_ID}?page=0&size=10`,
      {
        headers: AUTH_HEADERS,
        tags: { name: "product_list_seller" },
      }
    );

    listDuration.add(res.timings.duration);
    checkResponse(res, "셀러별 상품 목록");
  });

  sleep(0.5);

  // ── 5. 카테고리별 상품 목록 조회 ──────────
  group("카테고리별 상품 목록 (GET /api/products/category/:id)", () => {
    const res = http.get(
      `${BASE_URL}/api/products/category/${CATEGORY_ID}?page=0&size=10`,
      {
        headers: AUTH_HEADERS,
        tags: { name: "product_list_category" },
      }
    );

    listDuration.add(res.timings.duration);
    checkResponse(res, "카테고리별 상품 목록");
  });

  sleep(0.5);

  // ── 6. 상품 수정 ──────────────────────────
  if (createdProductId) {
    group("상품 수정 (PUT /api/products/:id)", () => {
      const payload = JSON.stringify({
        title: `수정된 상품 ${Date.now()}`,
        description: "수정된 설명입니다.",
        price: 20000,
        categoryId: CATEGORY_ID,
        imageUrls: [],
      });

      const res = http.put(
        `${BASE_URL}/api/products/${createdProductId}`,
        payload,
        {
          headers: AUTH_HEADERS,
          tags: { name: "product_update" },
        }
      );

      checkResponse(res, "상품 수정");
    });

    sleep(0.5);

    // ── 7. 상품 삭제 ────────────────────────
    group("상품 삭제 (DELETE /api/products/:id)", () => {
      const res = http.del(
        `${BASE_URL}/api/products/${createdProductId}`,
        null,
        {
          headers: AUTH_HEADERS,
          tags: { name: "product_delete" },
        }
      );

      checkResponse(res, "상품 삭제");
    });
  }

  sleep(1);
}

// ──────────────────────────────────────────
// 카테고리 API 전용 시나리오 (읽기 전용)
// ──────────────────────────────────────────
export function categoryScenario() {
  group("카테고리 전체 조회 (GET /api/categories)", () => {
    const res = http.get(`${BASE_URL}/api/categories`, {
      tags: { name: "category_list" },
    });
    checkResponse(res, "카테고리 전체 조회");
  });

  sleep(0.3);

  group("루트 카테고리 조회 (GET /api/categories/root)", () => {
    const res = http.get(`${BASE_URL}/api/categories/root`, {
      tags: { name: "category_root" },
    });
    checkResponse(res, "루트 카테고리 조회");
  });

  sleep(1);
}
