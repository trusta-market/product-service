CREATE TABLE IF NOT EXISTS p_categories (
                                            id                   UUID         NOT NULL,
                                            name                 VARCHAR(255),
    parent_id            UUID,
    depth                INTEGER      NOT NULL,
    display_order        INTEGER      NOT NULL,
    inspection_threshold INTEGER,
    inspection_policy    VARCHAR(20),
    PRIMARY KEY (id),
    CONSTRAINT p_categories_inspection_policy_check
    CHECK (inspection_policy IN ('ALWAYS', 'PRICE_BASED', 'NEVER')),
    CONSTRAINT fk_categories_parent
    FOREIGN KEY (parent_id) REFERENCES p_categories(id),
    CONSTRAINT uq_category_name_per_parent
    UNIQUE (parent_id, name)
    );

-- 대분류(parent_id IS NULL)의 이름 중복 방지를 위한 부분 유니크 인덱스
-- 표준 UNIQUE 제약은 NULL을 여러 개 허용하기 때문에 별도 처리 필요
CREATE UNIQUE INDEX IF NOT EXISTS uq_root_category_name
    ON p_categories (name)
    WHERE parent_id IS NULL;

CREATE TABLE IF NOT EXISTS p_products (
                                          id                UUID         NOT NULL,
                                          seller_id         UUID         NOT NULL,
                                          category_id       UUID         NOT NULL,
                                          inspector_id      UUID,
                                          title             VARCHAR(100) NOT NULL,
    description       TEXT,
    price             INTEGER      NOT NULL,
    grade             VARCHAR(20),
    status            VARCHAR(30),
    inspection_status VARCHAR(30),
    created_at        TIMESTAMP,
    updated_at        TIMESTAMP,
    PRIMARY KEY (id),
    -- 존재하지 않는 카테고리를 참조하는 상품 생성 방지
    CONSTRAINT fk_products_category
    FOREIGN KEY (category_id) REFERENCES p_categories(id)
    );

CREATE TABLE IF NOT EXISTS p_product_images (
                                                id           UUID    NOT NULL,
                                                product_id   UUID,
                                                image_url    VARCHAR(500),
    sort_order   INTEGER NOT NULL,
    is_thumbnail BOOLEAN NOT NULL,
    is_deleted   BOOLEAN NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_images_product
    FOREIGN KEY (product_id) REFERENCES p_products(id)
    );