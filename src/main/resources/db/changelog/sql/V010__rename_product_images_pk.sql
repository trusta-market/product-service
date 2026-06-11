--liquibase formatted sql

--changeset product-service:10

ALTER TABLE p_product_images RENAME COLUMN id TO image_id;
