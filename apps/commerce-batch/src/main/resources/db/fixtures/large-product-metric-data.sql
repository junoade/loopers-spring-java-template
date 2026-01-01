-- PRODUCT_METRICS bulk dummy data
-- 범위: [2025-01-01, 2026-01-31]
DELETE FROM product_metrics;

INSERT INTO product_metrics (
    product_id,
    metrics_date,
    metrics_type,
    `count`,
    created_at,
    updated_at
)
SELECT
    p.product_id,
    DATE_FORMAT(DATE_ADD('2025-01-01', INTERVAL n.n DAY), '%Y%m%d') AS metrics_date,
    m.metrics_type,
    CASE m.metrics_type
        WHEN 'VIEW'            THEN FLOOR(50 + RAND() * 500)
        WHEN 'LIKE'            THEN FLOOR(5 + RAND() * 80)
        WHEN 'ORDER_SUCCESS'   THEN FLOOR(RAND() * 20)
        WHEN 'PAYMENT_SUCCESS' THEN FLOOR(RAND() * 20)
        END AS `count`,
    NOW(),
    NOW()
FROM
    -- n = 0..395 생성 (최대 999까지 가능)
    (
        SELECT (a.d + 10*b.d + 100*c.d) AS n
        FROM (SELECT 0 d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
              UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) a
                 CROSS JOIN (SELECT 0 d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
                             UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) b
                 CROSS JOIN (SELECT 0 d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
                             UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) c
    ) n
        CROSS JOIN
    (SELECT 1 AS product_id UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5) p
        CROSS JOIN
    (SELECT 'VIEW' AS metrics_type
     UNION ALL SELECT 'LIKE'
     UNION ALL SELECT 'ORDER_SUCCESS'
     UNION ALL SELECT 'PAYMENT_SUCCESS') m
WHERE
    n.n BETWEEN 0 AND 395;