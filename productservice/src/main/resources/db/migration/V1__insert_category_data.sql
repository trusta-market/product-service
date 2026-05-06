-- 대분류 등록
INSERT INTO p_categories (id, name, parent_id, depth, display_order, inspection_threshold, inspection_policy)
VALUES
    (gen_random_uuid(), '명품/럭셔리', NULL, 0, 1, NULL, NULL),
    (gen_random_uuid(), '패션',        NULL, 0, 2, NULL, NULL),
    (gen_random_uuid(), '전자기기',    NULL, 0, 3, NULL, NULL),
    (gen_random_uuid(), '스포츠/레저', NULL, 0, 4, NULL, NULL),
    (gen_random_uuid(), '취미/수집',   NULL, 0, 5, NULL, NULL),
    (gen_random_uuid(), '생활/기타',   NULL, 0, 6, NULL, NULL);

-- =============================================
-- 명품/럭셔리 중분류 (null → enum ALWAYS fallback)
-- =============================================
INSERT INTO p_categories (id, name, parent_id, depth, display_order, inspection_threshold, inspection_policy)
SELECT gen_random_uuid(), '명품 가방', id, 1, 1, NULL, NULL FROM p_categories WHERE name = '명품/럭셔리';
INSERT INTO p_categories (id, name, parent_id, depth, display_order, inspection_threshold, inspection_policy)
SELECT gen_random_uuid(), '명품 지갑', id, 1, 2, NULL, NULL FROM p_categories WHERE name = '명품/럭셔리';
INSERT INTO p_categories (id, name, parent_id, depth, display_order, inspection_threshold, inspection_policy)
SELECT gen_random_uuid(), '명품 시계', id, 1, 3, NULL, NULL FROM p_categories WHERE name = '명품/럭셔리';

-- =============================================
-- 패션 중분류 (null → enum PRICE_BASED 200,000 fallback)
-- =============================================
INSERT INTO p_categories (id, name, parent_id, depth, display_order, inspection_threshold, inspection_policy)
SELECT gen_random_uuid(), '스니커즈/운동화', id, 1, 1, NULL, NULL FROM p_categories WHERE name = '패션';
INSERT INTO p_categories (id, name, parent_id, depth, display_order, inspection_threshold, inspection_policy)
SELECT gen_random_uuid(), '일반 의류', id, 1, 2, NULL, NULL FROM p_categories WHERE name = '패션';
INSERT INTO p_categories (id, name, parent_id, depth, display_order, inspection_threshold, inspection_policy)
SELECT gen_random_uuid(), '일반 가방', id, 1, 3, NULL, NULL FROM p_categories WHERE name = '패션';
INSERT INTO p_categories (id, name, parent_id, depth, display_order, inspection_threshold, inspection_policy)
SELECT gen_random_uuid(), '일반 신발', id, 1, 4, NULL, NULL FROM p_categories WHERE name = '패션';

-- =============================================
-- 전자기기 중분류 (null → enum PRICE_BASED 500,000 fallback)
-- =============================================
INSERT INTO p_categories (id, name, parent_id, depth, display_order, inspection_threshold, inspection_policy)
SELECT gen_random_uuid(), '스마트폰', id, 1, 1, NULL, NULL FROM p_categories WHERE name = '전자기기';
INSERT INTO p_categories (id, name, parent_id, depth, display_order, inspection_threshold, inspection_policy)
SELECT gen_random_uuid(), '노트북/태블릿', id, 1, 2, NULL, NULL FROM p_categories WHERE name = '전자기기';
INSERT INTO p_categories (id, name, parent_id, depth, display_order, inspection_threshold, inspection_policy)
SELECT gen_random_uuid(), '이어폰/헤드폰', id, 1, 3, NULL, NULL FROM p_categories WHERE name = '전자기기';
INSERT INTO p_categories (id, name, parent_id, depth, display_order, inspection_threshold, inspection_policy)
SELECT gen_random_uuid(), '스마트워치', id, 1, 4, NULL, NULL FROM p_categories WHERE name = '전자기기';

-- =============================================
-- 스포츠/레저 중분류 (null → enum PRICE_BASED 300,000 fallback)
-- =============================================
INSERT INTO p_categories (id, name, parent_id, depth, display_order, inspection_threshold, inspection_policy)
SELECT gen_random_uuid(), '골프용품', id, 1, 1, NULL, NULL FROM p_categories WHERE name = '스포츠/레저';
INSERT INTO p_categories (id, name, parent_id, depth, display_order, inspection_threshold, inspection_policy)
SELECT gen_random_uuid(), '자전거', id, 1, 2, NULL, NULL FROM p_categories WHERE name = '스포츠/레저';
INSERT INTO p_categories (id, name, parent_id, depth, display_order, inspection_threshold, inspection_policy)
SELECT gen_random_uuid(), '캠핑용품', id, 1, 3, NULL, NULL FROM p_categories WHERE name = '스포츠/레저';

-- =============================================
-- 취미/수집 중분류 (예술품/그림만 ALWAYS, 나머지 null → PRICE_BASED 200,000 fallback)
-- =============================================
INSERT INTO p_categories (id, name, parent_id, depth, display_order, inspection_threshold, inspection_policy)
SELECT gen_random_uuid(), '피규어/프라모델', id, 1, 1, NULL, NULL FROM p_categories WHERE name = '취미/수집';
INSERT INTO p_categories (id, name, parent_id, depth, display_order, inspection_threshold, inspection_policy)
SELECT gen_random_uuid(), '한정판/굿즈', id, 1, 2, NULL, NULL FROM p_categories WHERE name = '취미/수집';
INSERT INTO p_categories (id, name, parent_id, depth, display_order, inspection_threshold, inspection_policy)
SELECT gen_random_uuid(), '트레이딩 카드', id, 1, 3, NULL, NULL FROM p_categories WHERE name = '취미/수집';
INSERT INTO p_categories (id, name, parent_id, depth, display_order, inspection_threshold, inspection_policy)
SELECT gen_random_uuid(), '예술품/그림', id, 1, 4, NULL, 'ALWAYS' FROM p_categories WHERE name = '취미/수집';

-- =============================================
-- 생활/기타 중분류 (식품/건강·도서/잡지→NEVER, 티켓/상품권→ALWAYS, 나머지 null → PRICE_BASED 200,000 fallback)
-- =============================================
INSERT INTO p_categories (id, name, parent_id, depth, display_order, inspection_threshold, inspection_policy)
SELECT gen_random_uuid(), '가구/인테리어', id, 1, 1, NULL, NULL FROM p_categories WHERE name = '생활/기타';
INSERT INTO p_categories (id, name, parent_id, depth, display_order, inspection_threshold, inspection_policy)
SELECT gen_random_uuid(), '식품/건강', id, 1, 2, NULL, 'NEVER' FROM p_categories WHERE name = '생활/기타';
INSERT INTO p_categories (id, name, parent_id, depth, display_order, inspection_threshold, inspection_policy)
SELECT gen_random_uuid(), '티켓/상품권', id, 1, 3, NULL, 'ALWAYS' FROM p_categories WHERE name = '생활/기타';
INSERT INTO p_categories (id, name, parent_id, depth, display_order, inspection_threshold, inspection_policy)
SELECT gen_random_uuid(), '도서/잡지', id, 1, 4, NULL, 'NEVER' FROM p_categories WHERE name = '생활/기타';