-- SET SESSION cte_max_recursion_depth = 100000;

-- 1) numbers 테이블 (유틸)
CREATE TABLE IF NOT EXISTS numbers (
    n INT PRIMARY KEY
);
INSERT IGNORE INTO numbers (n)
SELECT
    a.n + 10 * b.n + 100 * c.n + 1000 * d.n + 1 AS n
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
WHERE
    a.n + 10 * b.n + 100 * c.n + 1000 * d.n < 100000;

-- 2) Users 10,000 명 생성
INSERT INTO users (user_id, user_name, description, email, birth_date, gender, point, created_at, updated_at)
SELECT
    CONCAT('User_', n) AS user_id,
    CONCAT('User_', n) AS user_name,
    CONCAT('User_Descrption_', n) AS description,
    CONCAT('user_', n, '@gmail.com') AS email,
    CONCAT('1997-09-28') AS birth_date,
    CASE
        WHEN RAND() < 0.5 THEN 'M'
        ELSE 'W'
    END AS gender,
    FLOOR(RAND() * 100000) AS point,
    NOW() - INTERVAL (n % 365) DAY AS created_at,
    NOW() - INTERVAL (n % 365) DAY AS updated_at
FROM numbers
WHERE n <= 10000;

-- 3) brand 생성 (1~100)
INSERT INTO brand (name, description, status, created_at, updated_at)
SELECT
    CONCAT('Brand_', n),
    CONCAT('Description_', n),
    'REGISTERED',
    NOW() - INTERVAL (n % 365) DAY AS created_at,
    NOW() - INTERVAL (n % 365) DAY AS updated_at
FROM numbers
WHERE n <= 100;

-- 4) product 100,000개 생성
INSERT INTO product (name, category, price, status, brand_id, created_at, updated_at)
SELECT
    CONCAT('Product_', n) AS name,
    CASE
        WHEN RAND() < 0.2 THEN '옷'
        WHEN RAND() < 0.4 THEN '신발'
        WHEN RAND() < 0.6 THEN '모자'
        WHEN RAND() < 0.8 THEN '가전'
        ELSE '기타'
        END AS category,
    FLOOR(RAND() * 100000)    AS price,
    CASE WHEN n % 20 = 0 THEN 'STOP_SELLING' ELSE 'ON_SALE' END AS status,
    ((n - 1) % 100) + 1   AS brand_id,
    NOW() - INTERVAL (n % 365) DAY AS created_at,
    NOW() - INTERVAL (n % 365) DAY AS updated_at
FROM numbers
WHERE n <= 100000;

-- 5) product_like 300,000개 생성
INSERT INTO product_like (user_id, product_id, created_at)
SELECT
    ((n - 1) % 10000) + 1  AS user_id,
    ((n - 1) % 150) + 1 AS product_id,
    NOW() - INTERVAL (n % 180) DAY AS created_at
FROM numbers
WHERE n <= 300000;