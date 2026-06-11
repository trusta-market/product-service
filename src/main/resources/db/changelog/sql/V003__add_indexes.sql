--liquibase formatted sql

--changeset Seungwon-Choi:6
CREATE INDEX IF NOT EXISTS idx_products_seller_id   ON p_products (seller_id);
CREATE INDEX IF NOT EXISTS idx_products_category_id ON p_products (category_id);
CREATE INDEX IF NOT EXISTS idx_products_status      ON p_products (status);

--changeset Seungwon-Choi:7
CREATE INDEX IF NOT EXISTS idx_product_images_product_id ON p_product_images (product_id);
