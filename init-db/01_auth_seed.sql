-- =============================================================================
-- 01_auth_seed.sql
-- Seed data cho auth-service
-- Chạy sau khi Hibernate tạo schema (ddl-auto: update)
-- =============================================================================

USE `auth`;

-- -----------------------------------------------------------------------------
-- Roles
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO roles (code, name, description, is_deleted, created_at, updated_at)
VALUES
    ('CUSTOMER',          'Customer',          'Người mua sắm trên hệ thống',    false, NOW(), NOW()),
    ('SELLER',            'Seller',            'Chủ shop bán hàng',              false, NOW(), NOW()),
    ('ADMIN',             'Administrator',     'Quản trị toàn hệ thống',         false, NOW(), NOW()),
    ('STAFF_SUPPORT',     'Support Staff',     'Nhân viên chăm sóc khách hàng',  false, NOW(), NOW()),
    ('INVENTORY_MANAGER', 'Inventory Manager', 'Quản lý kho',                    false, NOW(), NOW()),
    ('DELIVERY_MANAGER',  'Delivery Manager',  'Quản lý vận chuyển',             false, NOW(), NOW()),
    ('PAYMENT_MANAGER',   'Payment Manager',   'Quản lý thanh toán',             false, NOW(), NOW());

-- -----------------------------------------------------------------------------
-- Permissions
-- Format path: dùng Ant pattern, ví dụ /api/v1/products/** hoặc /api/v1/products/{id}
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO permissions (code, name, description, path, http_method, is_deleted, created_at, updated_at)
VALUES
    -- Product (public read, admin write)
    ('PRODUCT_LIST',           'View product list',       '', '/api/v1/products',                    'GET',    false, NOW(), NOW()),
    ('PRODUCT_VIEW',           'View product detail',     '', '/api/v1/products/**',                 'GET',    false, NOW(), NOW()),
    ('PRODUCT_CREATE',         'Create product',          '', '/api/v1/admin/products/add-product',  'POST',   false, NOW(), NOW()),
    ('PRODUCT_UPDATE',         'Update product',          '', '/api/v1/admin/products/**',           'PATCH',  false, NOW(), NOW()),
    ('PRODUCT_DELETE',         'Delete product',          '', '/api/v1/admin/products/**',           'DELETE', false, NOW(), NOW()),
    ('PRODUCT_STATUS',         'Update product status',   '', '/api/v1/admin/products/**/status',    'PATCH',  false, NOW(), NOW()),

    -- Category
    ('CATEGORY_LIST',          'View category list',      '', '/api/v1/categories',                  'GET',    false, NOW(), NOW()),
    ('CATEGORY_VIEW',          'View category detail',    '', '/api/v1/categories/**',               'GET',    false, NOW(), NOW()),
    ('CATEGORY_CREATE',        'Create category',         '', '/api/v1/admin/categories',            'POST',   false, NOW(), NOW()),
    ('CATEGORY_UPDATE',        'Update category',         '', '/api/v1/admin/categories/**',         'PATCH',  false, NOW(), NOW()),
    ('CATEGORY_DELETE',        'Delete category',         '', '/api/v1/admin/categories/**',         'DELETE', false, NOW(), NOW()),

    -- Order
    ('ORDER_CREATE',           'Create order',            '', '/api/v1/orders',                      'POST',   false, NOW(), NOW()),
    ('ORDER_LIST',             'View own orders',         '', '/api/v1/orders',                      'GET',    false, NOW(), NOW()),
    ('ORDER_VIEW',             'View order detail',       '', '/api/v1/orders/**',                   'GET',    false, NOW(), NOW()),
    ('ORDER_CANCEL',           'Cancel order',            '', '/api/v1/orders/**/cancel',            'PUT',    false, NOW(), NOW()),

    -- Cart
    ('CART_VIEW',              'View cart',               '', '/api/v1/cart',                        'GET',    false, NOW(), NOW()),
    ('CART_ADD',               'Add item to cart',        '', '/api/v1/cart/items',                  'POST',   false, NOW(), NOW()),
    ('CART_UPDATE',            'Update cart item',        '', '/api/v1/cart/items/**',               'PUT',    false, NOW(), NOW()),
    ('CART_REMOVE',            'Remove cart item',        '', '/api/v1/cart/items/**',               'DELETE', false, NOW(), NOW()),
    ('CART_CLEAR',             'Clear cart',              '', '/api/v1/cart',                        'DELETE', false, NOW(), NOW()),

    -- Customer
    ('CUSTOMER_VIEW',          'View customer profile',   '', '/api/v1/customer/**',                 'GET',    false, NOW(), NOW()),
    ('CUSTOMER_UPDATE',        'Update customer profile', '', '/api/v1/customer/**',                 'PUT',    false, NOW(), NOW()),

    -- Payment
    ('PAYMENT_VIEW',           'View payment status',     '', '/api/v1/payments/**',                 'GET',    false, NOW(), NOW()),
    ('PAYMENT_CREATE',         'Create payment',          '', '/api/v1/payments',                    'POST',   false, NOW(), NOW()),

    -- Inventory (admin)
    ('INVENTORY_VIEW',         'View inventory',          '', '/api/v1/inventory/**',                'GET',    false, NOW(), NOW()),
    ('INVENTORY_UPDATE',       'Update inventory',        '', '/api/v1/inventory/**',                'PATCH',  false, NOW(), NOW()),

    -- User profile
    ('USER_PROFILE_VIEW',      'View own profile',        '', '/api/v1/user/profile',                'GET',    false, NOW(), NOW()),
    ('USER_PROFILE_UPDATE',    'Update own profile',      '', '/api/v1/user/profile',                'PATCH',  false, NOW(), NOW()),

    -- Admin user management
    ('ADMIN_USER_CREATE',      'Admin create user',       '', '/api/v1/admin/users',                 'POST',   false, NOW(), NOW());

-- -----------------------------------------------------------------------------
-- Role-Permission mapping
-- -----------------------------------------------------------------------------

-- CUSTOMER: mua hàng, xem sản phẩm, quản lý cart, xem đơn hàng của mình
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'CUSTOMER'
  AND p.code IN (
    'PRODUCT_LIST', 'PRODUCT_VIEW',
    'CATEGORY_LIST', 'CATEGORY_VIEW',
    'ORDER_CREATE', 'ORDER_LIST', 'ORDER_VIEW', 'ORDER_CANCEL',
    'CART_VIEW', 'CART_ADD', 'CART_UPDATE', 'CART_REMOVE', 'CART_CLEAR',
    'CUSTOMER_VIEW', 'CUSTOMER_UPDATE',
    'PAYMENT_VIEW', 'PAYMENT_CREATE',
    'USER_PROFILE_VIEW', 'USER_PROFILE_UPDATE'
);

-- SELLER: quản lý sản phẩm của mình + quyền CUSTOMER
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'SELLER'
  AND p.code IN (
    'PRODUCT_LIST', 'PRODUCT_VIEW', 'PRODUCT_CREATE', 'PRODUCT_UPDATE', 'PRODUCT_DELETE', 'PRODUCT_STATUS',
    'CATEGORY_LIST', 'CATEGORY_VIEW',
    'ORDER_LIST', 'ORDER_VIEW',
    'INVENTORY_VIEW', 'INVENTORY_UPDATE',
    'USER_PROFILE_VIEW', 'USER_PROFILE_UPDATE'
);

-- ADMIN: toàn quyền
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'ADMIN';

-- INVENTORY_MANAGER: quản lý kho
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'INVENTORY_MANAGER'
  AND p.code IN (
    'PRODUCT_LIST', 'PRODUCT_VIEW',
    'INVENTORY_VIEW', 'INVENTORY_UPDATE',
    'ORDER_LIST', 'ORDER_VIEW',
    'USER_PROFILE_VIEW'
);

-- PAYMENT_MANAGER: quản lý thanh toán
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'PAYMENT_MANAGER'
  AND p.code IN (
    'PAYMENT_VIEW', 'PAYMENT_CREATE',
    'ORDER_LIST', 'ORDER_VIEW',
    'USER_PROFILE_VIEW'
);

-- STAFF_SUPPORT: hỗ trợ khách hàng
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'STAFF_SUPPORT'
  AND p.code IN (
    'PRODUCT_LIST', 'PRODUCT_VIEW',
    'CATEGORY_LIST', 'CATEGORY_VIEW',
    'ORDER_LIST', 'ORDER_VIEW',
    'CUSTOMER_VIEW',
    'PAYMENT_VIEW',
    'USER_PROFILE_VIEW'
);

-- -----------------------------------------------------------------------------
-- Default admin user
-- Password: Admin@2024 (BCrypt encoded)
-- ĐỔI PASSWORD NGAY SAU KHI DEPLOY LẦN ĐẦU
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO users (username, password, email, is_account_non_locked, is_account_non_expired, is_enabled, is_deleted, created_at, updated_at)
VALUES (
    'admin',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'admin@ecommerce.com',
    true, true, true, false,
    NOW(), NOW()
);

-- Gán role ADMIN cho admin user
INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'admin' AND r.code = 'ADMIN';

-- Tạo UserInfo cho admin
INSERT IGNORE INTO user_infos (user_id, first_name, last_name, is_deleted, created_at, updated_at)
SELECT u.id, 'System', 'Admin', false, NOW(), NOW()
FROM users u WHERE u.username = 'admin';
