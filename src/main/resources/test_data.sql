-- ============================================
-- 分布式商品库存与秒杀系统 - 完整测试数据
-- ============================================

-- 使用数据库
USE distributed_system;

-- ============================================
-- 1. 用户表测试数据
-- ============================================
-- 密码都是 123456（使用BCrypt加密）
INSERT INTO user (username, password, email) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', 'admin@example.com'),
('zhangsan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', 'zhangsan@example.com'),
('lisi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', 'lisi@example.com'),
('wangwu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', 'wangwu@example.com'),
('zhaoliu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', 'zhaoliu@example.com'),
('sunqi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', 'sunqi@example.com'),
('zhouba', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', 'zhouba@example.com'),
('wujiu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', 'wujiu@example.com'),
('zhengshi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', 'zhengshi@example.com'),
('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', 'testuser@example.com');

-- ============================================
-- 2. 商品表测试数据
-- ============================================
INSERT INTO product (name, description, price, stock, seckill_stock) VALUES
('iPhone 15 Pro Max', '苹果最新款旗舰手机，A17 Pro芯片，钛金属边框，256GB存储', 9999.00, 100, 10),
('MacBook Pro 14英寸', 'M3 Pro芯片，18GB内存，512GB固态硬盘，Liquid Retina XDR显示屏', 14999.00, 50, 5),
('iPad Pro 12.9英寸', 'M2芯片，128GB存储，Liquid Retina XDR显示屏，支持Apple Pencil Pro', 8999.00, 80, 8),
('AirPods Pro 2', '主动降噪，自适应通透模式，空间音频，MagSafe充电盒', 1899.00, 200, 20),
('Apple Watch Ultra 2', '钛金属表壳，100米防水，双频GPS，3000尼特亮度', 6499.00, 60, 6),
('Sony WH-1000XM5', '索尼旗舰降噪耳机，30小时续航，多点连接', 2699.00, 120, 12),
('Nintendo Switch OLED', '7英寸OLED屏幕，64GB存储，红蓝Joy-Con', 2599.00, 150, 15),
('PlayStation 5', '索尼次世代游戏主机，825GB SSD，DualSense无线控制器', 3899.00, 40, 4),
('Samsung Galaxy S24 Ultra', '骁龙8 Gen3，2亿像素相机，钛金属框架，256GB', 9699.00, 90, 9),
('Dell XPS 13 Plus', '13.4英寸OLED触控屏，i7-1360P，16GB内存，512GB SSD', 10999.00, 70, 7);

-- ============================================
-- 3. 库存表测试数据
-- ============================================
INSERT INTO inventory (product_id, stock, seckill_stock) VALUES
(1, 100, 10),
(2, 50, 5),
(3, 80, 8),
(4, 200, 20),
(5, 60, 6),
(6, 120, 12),
(7, 150, 15),
(8, 40, 4),
(9, 90, 9),
(10, 70, 7);

-- ============================================
-- 4. 订单表测试数据（主数据库）
-- ============================================
INSERT INTO `order` (id, user_id, product_id, quantity, total_price, status, create_time, update_time) VALUES
(1000000000000000001, 2, 1, 1, 9999.00, 1, '2026-04-10 10:00:00', '2026-04-10 10:00:00'),
(1000000000000000002, 2, 4, 2, 3798.00, 1, '2026-04-11 14:30:00', '2026-04-11 14:30:00'),
(1000000000000000003, 3, 2, 1, 14999.00, 0, '2026-04-12 09:15:00', '2026-04-12 09:15:00'),
(1000000000000000004, 4, 7, 1, 2599.00, 2, '2026-04-13 16:45:00', '2026-04-13 16:50:00'),
(1000000000000000005, 5, 6, 1, 2699.00, 1, '2026-04-14 11:20:00', '2026-04-14 11:20:00');

-- ============================================
-- 5. 支付记录表测试数据
-- ============================================
INSERT INTO payment (order_id, amount, status, create_time, update_time) VALUES
(1000000000000000001, 9999.00, 1, '2026-04-10 10:01:00', '2026-04-10 10:01:00'),
(1000000000000000002, 3798.00, 1, '2026-04-11 14:31:00', '2026-04-11 14:31:00'),
(1000000000000000003, 14999.00, 0, '2026-04-12 09:16:00', '2026-04-12 09:16:00'),
(1000000000000000005, 2699.00, 1, '2026-04-14 11:21:00', '2026-04-14 11:21:00');

-- ============================================
-- 测试数据说明
-- ============================================
-- 用户账号（密码都是：123456）：
-- admin / 123456 (管理员)
-- zhangsan / 123456
-- lisi / 123456
-- wangwu / 123456
-- zhaoliu / 123456
-- sunqi / 123456
-- zhouba / 123456
-- wujiu / 123456
-- zhengshi / 123456
-- testuser / 123456

-- 商品列表（10个商品，都有秒杀库存）：
-- 1. iPhone 15 Pro Max - ¥9999 - 库存100 - 秒杀库存10
-- 2. MacBook Pro 14英寸 - ¥14999 - 库存50 - 秒杀库存5
-- 3. iPad Pro 12.9英寸 - ¥8999 - 库存80 - 秒杀库存8
-- 4. AirPods Pro 2 - ¥1899 - 库存200 - 秒杀库存20
-- 5. Apple Watch Ultra 2 - ¥6499 - 库存60 - 秒杀库存6
-- 6. Sony WH-1000XM5 - ¥2699 - 库存120 - 秒杀库存12
-- 7. Nintendo Switch OLED - ¥2599 - 库存150 - 秒杀库存15
-- 8. PlayStation 5 - ¥3899 - 库存40 - 秒杀库存4
-- 9. Samsung Galaxy S24 Ultra - ¥9699 - 库存90 - 秒杀库存9
-- 10. Dell XPS 13 Plus - ¥10999 - 库存70 - 秒杀库存7

-- 订单状态：
-- 0 - 待支付
-- 1 - 已支付
-- 2 - 已取消

-- 支付状态：
-- 0 - 待支付
-- 1 - 支付成功
-- 2 - 支付失败
