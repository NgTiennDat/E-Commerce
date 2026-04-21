-- =============================================================================
-- 00_create_databases.sql
-- Tạo tất cả databases cần thiết
-- File này chạy tự động khi MySQL container khởi tạo lần đầu
-- =============================================================================

CREATE DATABASE IF NOT EXISTS `auth`
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS `product`
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS `orders`
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS `payment`
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS `inventory`
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
