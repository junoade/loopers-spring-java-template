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

CREATE INDEX idx_outbox_ready_retry
    ON event_outbox (status, next_retry_at);

CREATE INDEX idx_outbox_processing
    ON event_outbox (status, processing_started_at);