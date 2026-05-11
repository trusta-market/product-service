-- 대분류 등록
INSERT INTO p_categories (id, name, parent_id, depth, display_order, inspection_threshold, inspection_policy)
VALUES
    (gen_random_uuid(), '명품/럭셔리', NULL, 0, 1, NULL, NULL),
    (gen_random_uuid(), '패션',        NULL, 0, 2, NULL, NULL),
    (gen_random_uuid(), '전자기기',    NULL, 0, 3, NULL, NULL),
    (gen_random_uuid(), '스포츠/레저', NULL, 0, 4, NULL, NULL),
    (gen_random_uuid(), '취미/수집',   NULL, 0, 5, NULL, NULL),
    (gen_random_uuid(), '생활/기타',   NULL, 0, 6, NULL, NULL);

WITH parent AS (
    SELECT id FROM p_categories
    WHERE name = '명품/럭셔리' AND depth = 0 AND parent_id IS NULL
)
INSERT INTO p_categories (id, name, parent_id, depth, display_order, inspection_threshold, inspection_policy)
SELECT gen_random_uuid(), '명품 가방', parent.id, 1, 1, NULL::INTEGER, NULL::VARCHAR FROM parent UNION ALL
SELECT gen_random_uuid(), '명품 지갑', parent.id, 1, 2, NULL::INTEGER, NULL::VARCHAR FROM parent UNION ALL
SELECT gen_random_uuid(), '명품 시계', parent.id, 1, 3, NULL::INTEGER, NULL::VARCHAR FROM parent;

WITH parent AS (
    SELECT id FROM p_categories
    WHERE name = '패션' AND depth = 0 AND parent_id IS NULL
)
INSERT INTO p_categories (id, name, parent_id, depth, display_order, inspection_threshold, inspection_policy)
SELECT gen_random_uuid(), '스니커즈/운동화', parent.id, 1, 1, NULL::INTEGER, NULL::VARCHAR FROM parent UNION ALL
SELECT gen_random_uuid(), '일반 의류',       parent.id, 1, 2, NULL::INTEGER, NULL::VARCHAR FROM parent UNION ALL
SELECT gen_random_uuid(), '일반 가방',       parent.id, 1, 3, NULL::INTEGER, NULL::VARCHAR FROM parent UNION ALL
SELECT gen_random_uuid(), '일반 신발',       parent.id, 1, 4, NULL::INTEGER, NULL::VARCHAR FROM parent;

WITH parent AS (
    SELECT id FROM p_categories
    WHERE name = '전자기기' AND depth = 0 AND parent_id IS NULL
)
INSERT INTO p_categories (id, name, parent_id, depth, display_order, inspection_threshold, inspection_policy)
SELECT gen_random_uuid(), '스마트폰',      parent.id, 1, 1, NULL::INTEGER, NULL::VARCHAR FROM parent UNION ALL
SELECT gen_random_uuid(), '노트북/태블릿', parent.id, 1, 2, NULL::INTEGER, NULL::VARCHAR FROM parent UNION ALL
SELECT gen_random_uuid(), '이어폰/헤드폰', parent.id, 1, 3, NULL::INTEGER, NULL::VARCHAR FROM parent UNION ALL
SELECT gen_random_uuid(), '스마트워치',    parent.id, 1, 4, NULL::INTEGER, NULL::VARCHAR FROM parent;

WITH parent AS (
    SELECT id FROM p_categories
    WHERE name = '스포츠/레저' AND depth = 0 AND parent_id IS NULL
)
INSERT INTO p_categories (id, name, parent_id, depth, display_order, inspection_threshold, inspection_policy)
SELECT gen_random_uuid(), '골프용품', parent.id, 1, 1, NULL::INTEGER, NULL::VARCHAR FROM parent UNION ALL
SELECT gen_random_uuid(), '자전거',   parent.id, 1, 2, NULL::INTEGER, NULL::VARCHAR FROM parent UNION ALL
SELECT gen_random_uuid(), '캠핑용품', parent.id, 1, 3, NULL::INTEGER, NULL::VARCHAR FROM parent;

WITH parent AS (
    SELECT id FROM p_categories
    WHERE name = '취미/수집' AND depth = 0 AND parent_id IS NULL
)
INSERT INTO p_categories (id, name, parent_id, depth, display_order, inspection_threshold, inspection_policy)
SELECT gen_random_uuid(), '피규어/프라모델', parent.id, 1, 1, NULL::INTEGER, NULL::VARCHAR     FROM parent UNION ALL
SELECT gen_random_uuid(), '한정판/굿즈',     parent.id, 1, 2, NULL::INTEGER, NULL::VARCHAR     FROM parent UNION ALL
SELECT gen_random_uuid(), '트레이딩 카드',   parent.id, 1, 3, NULL::INTEGER, NULL::VARCHAR     FROM parent UNION ALL
SELECT gen_random_uuid(), '예술품/그림',     parent.id, 1, 4, NULL::INTEGER, 'ALWAYS'::VARCHAR FROM parent;

WITH parent AS (
    SELECT id FROM p_categories
    WHERE name = '생활/기타' AND depth = 0 AND parent_id IS NULL
)
INSERT INTO p_categories (id, name, parent_id, depth, display_order, inspection_threshold, inspection_policy)
SELECT gen_random_uuid(), '가구/인테리어', parent.id, 1, 1, NULL::INTEGER, NULL::VARCHAR     FROM parent UNION ALL
SELECT gen_random_uuid(), '식품/건강',     parent.id, 1, 2, NULL::INTEGER, 'NEVER'::VARCHAR  FROM parent UNION ALL
SELECT gen_random_uuid(), '티켓/상품권',   parent.id, 1, 3, NULL::INTEGER, 'ALWAYS'::VARCHAR FROM parent UNION ALL
SELECT gen_random_uuid(), '도서/잡지',     parent.id, 1, 4, NULL::INTEGER, 'NEVER'::VARCHAR  FROM parent;
