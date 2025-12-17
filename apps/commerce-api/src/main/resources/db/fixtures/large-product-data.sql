-- 1) numbers 테이블 (1 ~ 1,000,000까지)
CREATE TABLE IF NOT EXISTS numbers (
    n INT PRIMARY KEY
);

INSERT IGNORE INTO numbers (n)
SELECT
    a.n
        + 10      * b.n
        + 100     * c.n
        + 1000    * d.n
        + 10000   * e.n
        + 100000  * f.n
        + 1       AS n
FROM
    (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
     UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) a
        CROSS JOIN
    (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
     UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) b
        CROSS JOIN
    (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
     UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) c
        CROSS JOIN
    (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
     UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) d
        CROSS JOIN
    (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
     UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) e
        CROSS JOIN
    (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
     UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) f
WHERE
    a.n
        + 10      * b.n
        + 100     * c.n
        + 1000    * d.n
        + 10000   * e.n
        + 100000  * f.n < 1000000;


-- 2) Users 100,000 명 생성
INSERT INTO users (user_id, user_name, description, email, birth_date, gender, point, created_at, updated_at)
SELECT
    CONCAT('User_', n)                  AS user_id,
    CONCAT('User_', n)                  AS user_name,
    CONCAT('User_Descrption_', n)       AS description,
    CONCAT('user_', n, '@gmail.com')    AS email,
    '1997-09-28'                        AS birth_date,
    CASE WHEN RAND() < 0.5 THEN 'M' ELSE 'W' END AS gender,
    FLOOR(RAND() * 100000)              AS point,
    NOW() - INTERVAL (n % 365) DAY      AS created_at,
    NOW() - INTERVAL (n % 365) DAY      AS updated_at
FROM numbers
WHERE n <= 100000;   -- ✅ 10만명


-- 3) Brand 10,000 개 생성
INSERT INTO brand (name, description, status, created_at, updated_at)
SELECT
    CONCAT('Brand_', n),
    CONCAT('Description_', n),
    'REGISTERED',
    NOW() - INTERVAL (n % 365) DAY AS created_at,
    NOW() - INTERVAL (n % 365) DAY AS updated_at
FROM numbers
WHERE n <= 10000;


-- 4) Product 1,000,000 개 생성
INSERT INTO product (name, category, stock, price, status, brand_id, created_at, updated_at)
SELECT
    CONCAT('Product_', n) AS name,
    CASE
        WHEN RAND() < 0.2 THEN '옷'
        WHEN RAND() < 0.4 THEN '신발'
        WHEN RAND() < 0.6 THEN '모자'
        WHEN RAND() < 0.8 THEN '가전'
        ELSE '기타'
        END AS category,
    FLOOR(RAND() * 100000)             AS stock,
    FLOOR(RAND() * 100000)             AS price,
    CASE WHEN n % 20 = 0 THEN 'STOP_SELLING' ELSE 'ON_SALE' END AS status,
    ((n - 1) % 10000) + 1              AS brand_id,
    NOW() - INTERVAL (n % 365) DAY     AS created_at,
    NOW() - INTERVAL (n % 365) DAY     AS updated_at
FROM numbers
WHERE n <= 1000000;  -- ✅ 100만개


-- 5) Product Like 5,000,000 개 생성
-- numbers(1~1,000,000) × 5 해서 500만개
--SET @top_hot := 100;         -- 최상위 상품 100개
--SET @popular_max := 10000;   -- 그 다음 인기 상품 1만개
--SET @total_products := 1000000;

INSERT IGNORE INTO product_like (user_id, product_id, created_at)
SELECT
    ((n.n - 1) % 100000) + 1 AS user_id,
    CASE
    WHEN RAND() < 0.4 THEN
    -- 최상위 100개에 40%
    FLOOR(RAND() * 100) + 1
    WHEN RAND() < 0.8 THEN
    -- 다음 1만개(101~10000)에 40%
    FLOOR(RAND() * (10000 - 100)) + (100 + 1)
    ELSE
    -- 나머지에 20%
    FLOOR(RAND() * (1000000 - 10000)) + (10000 + 1)
END AS product_id,
    NOW() - INTERVAL (n.n % 180) DAY AS created_at
FROM numbers n
JOIN (
    SELECT 1 AS k UNION ALL
    SELECT 2 UNION ALL
    SELECT 3 UNION ALL
    SELECT 4 UNION ALL
    SELECT 5
) x ON 1 = 1
WHERE n.n <= 1000000;

-- 6) 쿠폰 테스트 데이터 INSERT 시작 / Coupon 100개 생성
INSERT INTO coupons (
    coupon_code,
    name,
    discount_type,
    discount_value,
    status,
    created_at,
    updated_at
)
SELECT
    CONCAT('COUPON_', n) AS coupon_code,
    CONCAT('샘플 쿠폰 ', n) AS name,
    CASE
        WHEN RAND() < 0.5 THEN 'FIXED'
        ELSE 'PERCENT'
        END AS discount_type,
    -- FIXED in [1000,10000]
    -- PERCENT in [5%,20%]
    CASE
        WHEN RAND() < 0.5 THEN FLOOR(RAND() * 9000) + 1000
        ELSE FLOOR(RAND() * 16) + 5
        END AS discount_value,
    -- 퍼센트 할인 쿠폰일 경우 최대 할인 3000~20000, 아니면 NULL
    'ACTIVE' AS status,
    NOW() AS created_at,
    NOW() AS updated_at
FROM numbers
WHERE n <= 100;

-- 7) 10만명 유저에게 쿠폰 1개씩 랜덤 발급
INSERT INTO assigned_coupons (
    coupon_id,
    user_pk_id,
    status,
    issued_at
)
SELECT
    ((n % 100) + 1) AS coupon_id,  -- 쿠폰 100개 중 하나
    n AS user_pk_id,              -- [1, 100000]
    'ISSUED' AS status,
    NOW() - INTERVAL FLOOR(RAND() * 30) DAY AS issued_at
FROM numbers
WHERE n <= 100000;