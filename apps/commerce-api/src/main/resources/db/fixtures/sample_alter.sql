UPDATE product p
    JOIN (
    SELECT product_id, COUNT(*) AS cnt
    FROM product_like
    GROUP BY product_id
    ) pl ON pl.product_id = p.id
    SET p.like_count = pl.cnt;
ALTER TABLE product
    ADD INDEX idx_product_price (price);

ALTER TABLE product
    ADD INDEX idx_product_like_count (like_count DESC);

create index idx_outbox_status on order_event_outbox(status);