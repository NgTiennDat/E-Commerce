-- V2__insert_seed_data.sql

-- Insert into category (id tự tăng)
INSERT INTO category (
    name, description, slug, image_url, icon, is_active, display_order,
    created_by, created_at, updated_by, updated_at, is_deleted
)
VALUES
    ('Keyboards',   'Computer Keyboards',   'keyboards',   'https://example.com/cat-keyboards.png',   'keyboard',  b'1', 1, 'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0'),
    ('Monitors',    'Computer Monitors',    'monitors',    'https://example.com/cat-monitors.png',    'monitor',   b'1', 2, 'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0'),
    ('Screens',     'Display Screens',      'screens',     'https://example.com/cat-screens.png',     'screen',    b'1', 3, 'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0'),
    ('Mice',        'Computer Mice',        'mice',        'https://example.com/cat-mice.png',        'mouse',     b'1', 4, 'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0'),
    ('Accessories', 'Computer Accessories', 'accessories', 'https://example.com/cat-accessories.png', 'accessory', b'1', 5, 'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0');


-- Insert products for the 'Keyboards' category
INSERT INTO products (
    sku, name, short_description, description,
    available_quantity, price,
    discount_percent, image_url, brand,
    rating, rating_count, is_featured, is_new, status, category_id,
    created_by, created_at, updated_by, updated_at, is_deleted
)
VALUES
    ('KB-MECH-001', 'Mechanical Keyboard 1', 'Mechanical keyboard with RGB lighting',
     'Mechanical keyboard with RGB lighting',
     10, 99.99,
     10, 'https://example.com/prod-mech-kb-1.png', 'Generic',
     4.5, 12, b'1', b'1', 'ACTIVE',
     (SELECT id FROM category WHERE name = 'Keyboards'),
     'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0'),

    ('KB-WL-001', 'Wireless Compact Keyboard 1', 'Wireless compact keyboard',
     'Wireless compact keyboard',
     15, 79.99,
     5, 'https://example.com/prod-wireless-kb-1.png', 'Generic',
     4.2, 8, b'0', b'1', 'ACTIVE',
     (SELECT id FROM category WHERE name = 'Keyboards'),
     'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0'),

    ('KB-GAME-001', 'Gaming Keyboard 1', 'Backlit gaming keyboard with customizable keys',
     'Backlit gaming keyboard with customizable keys',
     20, 129.99,
     15, 'https://example.com/prod-gaming-kb-1.png', 'Generic',
     4.8, 20, b'1', b'1', 'ACTIVE',
     (SELECT id FROM category WHERE name = 'Keyboards'),
     'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0'),

    ('KB-ERGO-001', 'Ergonomic Keyboard 1', 'Mechanical keyboard with wrist rest',
     'Mechanical keyboard with wrist rest',
     25, 109.99,
     0, 'https://example.com/prod-ergonomic-kb-1.png', 'Generic',
     4.4, 9, b'0', b'0', 'ACTIVE',
     (SELECT id FROM category WHERE name = 'Keyboards'),
     'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0'),

    ('KB-COMBO-001', 'Wireless Combo 1', 'Wireless keyboard and mouse combo',
     'Wireless keyboard and mouse combo',
     18, 69.99,
     0, 'https://example.com/prod-wireless-combo-1.png', 'Generic',
     4.0, 6, b'0', b'0', 'ACTIVE',
     (SELECT id FROM category WHERE name = 'Keyboards'),
     'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0'),

    ('KB-MECH-002', 'Mechanical Keyboard 2', 'Mechanical keyboard with PBT keycaps',
     'High-quality mechanical keyboard with PBT keycaps and hot-swap switches.',
     20, 119.99,
     5, 'https://example.com/prod-mech-kb-2.png', 'PremiumBrand',
     4.7, 18, b'1', b'1', 'ACTIVE',
     (SELECT id FROM category WHERE name = 'Keyboards'),
     'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0'),

    ('KB-WL-002', 'Wireless Low-profile Keyboard', 'Slim wireless low-profile keyboard',
     'Slim, low-profile wireless keyboard with silent switches and multi-device support.',
     35, 89.99,
     0, 'https://example.com/prod-wireless-kb-2.png', 'Generic',
     4.3, 9, b'0', b'1', 'ACTIVE',
     (SELECT id FROM category WHERE name = 'Keyboards'),
     'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0'),

    ('KB-60PCT-001', '60% Gaming Keyboard', 'Compact 60% gaming keyboard',
     'Compact 60% mechanical gaming keyboard with RGB and detachable USB-C cable.',
     18, 99.49,
     10, 'https://example.com/prod-60pct-kb-1.png', 'EsportsBrand',
     4.6, 22, b'1', b'0', 'ACTIVE',
     (SELECT id FROM category WHERE name = 'Keyboards'),
     'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0');


-- Insert products for the 'Monitors' category
INSERT INTO products (
    sku, name, short_description, description,
    available_quantity, price,
    discount_percent, image_url, brand,
    rating, rating_count, is_featured, is_new, status, category_id,
    created_by, created_at, updated_by, updated_at, is_deleted
)
VALUES
    ('MON-4K-001', '4K Monitor 1', '27-inch IPS monitor with 4K resolution',
     '27-inch IPS monitor with 4K resolution',
     30, 399.99,
     10, 'https://example.com/prod-4k-monitor-1.png', 'Generic',
     4.6, 15, b'1', b'1', 'ACTIVE',
     (SELECT id FROM category WHERE name = 'Monitors'),
     'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0'),

    ('MON-UW-001', 'Ultra-wide Gaming Monitor 1', 'Ultra-wide gaming monitor with HDR support',
     'Ultra-wide gaming monitor with HDR support',
     25, 499.99,
     5, 'https://example.com/prod-ultrawide-monitor-1.png', 'Generic',
     4.7, 10, b'1', b'1', 'ACTIVE',
     (SELECT id FROM category WHERE name = 'Monitors'),
     'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0'),

    ('MON-OFFICE-001', 'Office Monitor 1', '24-inch LED monitor for office use',
     '24-inch LED monitor for office use',
     22, 179.99,
     0, 'https://example.com/prod-office-monitor-1.png', 'Generic',
     4.1, 5, b'0', b'0', 'ACTIVE',
     (SELECT id FROM category WHERE name = 'Monitors'),
     'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0'),

    ('MON-CURVE-001', 'Curved Monitor 1', '32-inch curved monitor with AMD FreeSync',
     '32-inch curved monitor with AMD FreeSync',
     28, 329.99,
     8, 'https://example.com/prod-curved-monitor-1.png', 'Generic',
     4.5, 11, b'0', b'0', 'ACTIVE',
     (SELECT id FROM category WHERE name = 'Monitors'),
     'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0'),

    ('MON-PORT-001', 'Portable Monitor 1', 'Portable USB-C monitor for laptops',
     'Portable USB-C monitor for laptops',
     35, 249.99,
     0, 'https://example.com/prod-portable-monitor-1.png', 'Generic',
     4.3, 7, b'0', b'1', 'ACTIVE',
     (SELECT id FROM category WHERE name = 'Monitors'),
     'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0'),

    ('MON-2K-001', '2K Office Monitor', '27-inch 2K IPS office monitor',
     '27-inch 2K IPS office monitor with thin bezels and height-adjustable stand.',
     40, 259.99,
     0, 'https://example.com/prod-2k-monitor-1.png', 'OfficePro',
     4.4, 13, b'0', b'0', 'ACTIVE',
     (SELECT id FROM category WHERE name = 'Monitors'),
     'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0'),

    ('MON-144-001', '144Hz Gaming Monitor', '24-inch 144Hz gaming monitor',
     '24-inch Full HD gaming monitor with 144Hz refresh rate and 1ms response time.',
     25, 219.99,
     8, 'https://example.com/prod-144hz-monitor-1.png', 'EsportsBrand',
     4.8, 30, b'1', b'1', 'ACTIVE',
     (SELECT id FROM category WHERE name = 'Monitors'),
     'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0'),

    ('MON-DUAL-001', 'Dual Monitor Bundle', 'Dual 24-inch monitor bundle',
     'Bundle of two 24-inch IPS monitors optimized for dual-screen productivity setups.',
     10, 349.99,
     12, 'https://example.com/prod-dual-monitor-bundle-1.png', 'Generic',
     4.5, 17, b'0', b'1', 'ACTIVE',
     (SELECT id FROM category WHERE name = 'Monitors'),
     'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0');


-- Insert products for the 'Screens' category
INSERT INTO products (
    sku, name, short_description, description,
    available_quantity, price,
    discount_percent, image_url, brand,
    rating, rating_count, is_featured, is_new, status, category_id,
    created_by, created_at, updated_by, updated_at, is_deleted
)
VALUES
    ('SCR-OLED-001', 'Curved OLED Gaming Screen 1', 'Curved OLED gaming screen with 240Hz refresh rate',
     'Curved OLED gaming screen with 240Hz refresh rate',
     15, 799.99,
     10, 'https://example.com/prod-oled-screen-1.png', 'Generic',
     4.9, 18, b'1', b'1', 'ACTIVE',
     (SELECT id FROM category WHERE name = 'Screens'),
     'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0'),

    ('SCR-QLED-001', 'QLED Monitor 1', 'Flat QLED monitor with 1440p resolution',
     'Flat QLED monitor with 1440p resolution',
     18, 599.99,
     5, 'https://example.com/prod-qled-monitor-1.png', 'Generic',
     4.6, 12, b'0', b'1', 'ACTIVE',
     (SELECT id FROM category WHERE name = 'Screens'),
     'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0'),

    ('SCR-TOUCH-001', 'Touch Screen Display 1', '27-inch touch screen display for creative work',
     '27-inch touch screen display for creative work',
     22, 699.99,
     0, 'https://example.com/prod-touch-screen-1.png', 'Generic',
     4.4, 9, b'0', b'0', 'ACTIVE',
     (SELECT id FROM category WHERE name = 'Screens'),
     'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0'),

    ('SCR-4K-001', 'Ultra-slim 4K HDR Display 1', 'Ultra-slim 4K HDR display for multimedia',
     'Ultra-slim 4K HDR display for multimedia',
     20, 449.99,
     0, 'https://example.com/prod-4k-hdr-display-1.png', 'Generic',
     4.3, 7, b'0', b'0', 'ACTIVE',
     (SELECT id FROM category WHERE name = 'Screens'),
     'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0'),

    ('SCR-PROJ-001', 'Gaming Projector 1', 'Gaming projector with low input lag',
     'Gaming projector with low input lag',
     25, 899.99,
     12, 'https://example.com/prod-gaming-projector-1.png', 'Generic',
     4.8, 14, b'1', b'1', 'ACTIVE',
     (SELECT id FROM category WHERE name = 'Screens'),
     'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0'),

    ('SCR-ULTRA-WIDE-002', 'Ultra-wide Productivity Screen', 'Ultra-wide screen for multitasking',
     '34-inch ultra-wide screen optimized for multitasking with built-in KVM switch.',
     14, 749.99,
     10, 'https://example.com/prod-ultrawide-screen-2.png', 'ProDisplay',
     4.6, 11, b'1', b'1', 'ACTIVE',
     (SELECT id FROM category WHERE name = 'Screens'),
     'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0'),

    ('SCR-CREATIVE-001', 'Color-Accurate Creator Screen', '4K screen for creators',
     '27-inch 4K screen with 99% AdobeRGB coverage for content creators.',
     16, 829.99,
     7, 'https://example.com/prod-creative-screen-1.png', 'CreativeBrand',
     4.7, 9, b'0', b'1', 'ACTIVE',
     (SELECT id FROM category WHERE name = 'Screens'),
     'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0'),

    ('SCR-PORTABLE-002', 'Portable Touch Screen', '15.6-inch portable touch screen',
     '15.6-inch portable touch screen with USB-C and built-in speakers.',
     28, 289.99,
     5, 'https://example.com/prod-portable-screen-2.png', 'Generic',
     4.3, 8, b'0', b'0', 'ACTIVE',
     (SELECT id FROM category WHERE name = 'Screens'),
     'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0');


-- Insert products for the 'Mice' category
INSERT INTO products (
    sku, name, short_description, description,
    available_quantity, price,
    discount_percent, image_url, brand,
    rating, rating_count, is_featured, is_new, status, category_id,
    created_by, created_at, updated_by, updated_at, is_deleted
)
VALUES
    ('MOUSE-RGB-001', 'RGB Gaming Mouse 1', 'Wireless gaming mouse with customizable RGB lighting',
     'Wireless gaming mouse with customizable RGB lighting',
     30, 59.99,
     5, 'https://example.com/prod-rgb-mouse-1.png', 'Generic',
     4.7, 16, b'1', b'1', 'ACTIVE',
     (SELECT id FROM category WHERE name = 'Mice'),
     'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0'),

    ('MOUSE-ERG-001', 'Ergonomic Wired Mouse 1', 'Ergonomic wired mouse for productivity',
     'Ergonomic wired mouse for productivity',
     28, 29.99,
     0, 'https://example.com/prod-erg-mouse-1.png', 'Generic',
     4.3, 10, b'0', b'0', 'ACTIVE',
     (SELECT id FROM category WHERE name = 'Mice'),
     'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0'),

    ('MOUSE-AMB-001', 'Ambidextrous Gaming Mouse 1', 'Ambidextrous gaming mouse with high DPI',
     'Ambidextrous gaming mouse with high DPI',
     32, 69.99,
     8, 'https://example.com/prod-ambidextrous-mouse-1.png', 'Generic',
     4.5, 11, b'0', b'1', 'ACTIVE',
     (SELECT id FROM category WHERE name = 'Mice'),
     'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0'),

    ('MOUSE-TRAVEL-001', 'Travel Mouse 1', 'Travel-sized compact mouse for laptops',
     'Travel-sized compact mouse for laptops',
     26, 19.99,
     0, 'https://example.com/prod-travel-mouse-1.png', 'Generic',
     4.0, 5, b'0', b'0', 'ACTIVE',
     (SELECT id FROM category WHERE name = 'Mice'),
     'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0'),

    ('MOUSE-VERT-001', 'Vertical Ergonomic Mouse 1', 'Vertical ergonomic mouse for reduced strain',
     'Vertical ergonomic mouse for reduced strain',
     35, 39.99,
     0, 'https://example.com/prod-vertical-mouse-1.png', 'Generic',
     4.2, 7, b'0', b'0', 'ACTIVE',
     (SELECT id FROM category WHERE name = 'Mice'),
     'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0'),

    ('MOUSE-OFFICE-001', 'Silent Office Mouse', 'Silent-click wireless office mouse',
     'Wireless office mouse with silent clicks and 18-month battery life.',
     40, 24.99,
     0, 'https://example.com/prod-office-mouse-1.png', 'OfficePro',
     4.2, 9, b'0', b'0', 'ACTIVE',
     (SELECT id FROM category WHERE name = 'Mice'),
     'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0'),

    ('MOUSE-PRO-001', 'Pro Esports Mouse', 'Lightweight esports gaming mouse',
     'Ultra-lightweight esports gaming mouse with 26k DPI sensor and paracord cable.',
     18, 79.99,
     10, 'https://example.com/prod-esports-mouse-1.png', 'EsportsBrand',
     4.9, 25, b'1', b'1', 'ACTIVE',
     (SELECT id FROM category WHERE name = 'Mice'),
     'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0'),

    ('MOUSE-BT-001', 'Multi-device Bluetooth Mouse', 'Bluetooth mouse for 3 devices',
     'Compact Bluetooth mouse that can pair with up to 3 devices and switch instantly.',
     32, 34.99,
     5, 'https://example.com/prod-bt-mouse-1.png', 'Generic',
     4.3, 12, b'0', b'1', 'ACTIVE',
     (SELECT id FROM category WHERE name = 'Mice'),
     'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0');


-- Insert products for the 'Accessories' category
INSERT INTO products (
    sku, name, short_description, description,
    available_quantity, price,
    discount_percent, image_url, brand,
    rating, rating_count, is_featured, is_new, status, category_id,
    created_by, created_at, updated_by, updated_at, is_deleted
)
VALUES
    ('ACC-STAND-001', 'Adjustable Laptop Stand 1', 'Adjustable laptop stand with cooling fan',
     'Adjustable laptop stand with cooling fan',
     25, 34.99,
     0, 'https://example.com/prod-laptop-stand-1.png', 'Generic',
     4.1, 6, b'0', b'0', 'ACTIVE',
     (SELECT id FROM category WHERE name = 'Accessories'),
     'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0'),

    ('ACC-CHARGE-001', 'Wireless Charging Pad 1', 'Wireless charging pad for smartphones',
     'Wireless charging pad for smartphones',
     20, 24.99,
     0, 'https://example.com/prod-wireless-charger-1.png', 'Generic',
     4.2, 8, b'0', b'1', 'ACTIVE',
     (SELECT id FROM category WHERE name = 'Accessories'),
     'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0'),

    ('ACC-STAND-RGB-001', 'RGB Headset Stand 1', 'Gaming headset stand with RGB lighting',
     'Gaming headset stand with RGB lighting',
     28, 49.99,
     5, 'https://example.com/prod-rgb-headset-stand-1.png', 'Generic',
     4.5, 11, b'1', b'1', 'ACTIVE',
     (SELECT id FROM category WHERE name = 'Accessories'),
     'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0'),

    ('ACC-KEYPAD-001', 'Bluetooth Keypad 1', 'Bluetooth mechanical keypad for tablets',
     'Bluetooth mechanical keypad for tablets',
     22, 39.99,
     0, 'https://example.com/prod-bt-keypad-1.png', 'Generic',
     4.3, 7, b'0', b'0', 'ACTIVE',
     (SELECT id FROM category WHERE name = 'Accessories'),
     'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0'),

    ('ACC-CASE-001', 'External Hard Drive Enclosure 1', 'External hard drive enclosure with USB-C',
     'External hard drive enclosure with USB-C',
     30, 29.99,
     0, 'https://example.com/prod-hdd-enclosure-1.png', 'Generic',
     4.1, 5, b'0', b'0', 'ACTIVE',
     (SELECT id FROM category WHERE name = 'Accessories'),
     'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0'),

    ('ACC-HUB-001', 'USB-C Docking Hub', '7-in-1 USB-C docking hub',
     '7-in-1 USB-C docking hub with HDMI, USB 3.0, SD card and PD charging.',
     45, 49.99,
     0, 'https://example.com/prod-usbc-hub-1.png', 'Generic',
     4.4, 13, b'0', b'1', 'ACTIVE',
     (SELECT id FROM category WHERE name = 'Accessories'),
     'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0'),

    ('ACC-WRIST-REST-001', 'Memory Foam Wrist Rest', 'Keyboard memory foam wrist rest',
     'Ergonomic memory foam wrist rest for mechanical and full-size keyboards.',
     30, 19.99,
     0, 'https://example.com/prod-wrist-rest-1.png', 'Generic',
     4.5, 10, b'0', b'0', 'ACTIVE',
     (SELECT id FROM category WHERE name = 'Accessories'),
     'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0'),

    ('ACC-MAT-001', 'XL Gaming Mouse Pad', 'XL RGB gaming mouse pad',
     'Extended-size RGB gaming mouse pad with anti-slip rubber base.',
     35, 29.99,
     10, 'https://example.com/prod-gaming-mat-1.png', 'EsportsBrand',
     4.6, 14, b'1', b'1', 'ACTIVE',
     (SELECT id FROM category WHERE name = 'Accessories'),
     'SYSTEM', NOW(), 'SYSTEM', NOW(), b'0');

