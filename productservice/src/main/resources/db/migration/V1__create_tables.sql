-- p_categories 테이블 생성
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
    FOREIGN KEY (parent_id) REFERENCES p_categories(id)
    );

-- p_products 테이블 생성
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
    PRIMARY KEY (id)
    );

-- p_product_images 테이블 생성
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