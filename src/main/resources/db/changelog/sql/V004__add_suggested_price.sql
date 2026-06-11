--liquibase formatted sql

--changeset Seungwon-Choi:8
ALTER TABLE p_products ADD COLUMN IF NOT EXISTS suggested_price BIGINT;
