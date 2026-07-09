-- 1. 创建数据�?CREATE DATABASE IF NOT EXISTS `lichuan_tea_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `lichuan_tea_db`;

-- 2. 创建表结�?
-- 用户�?CREATE TABLE IF NOT EXISTS `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '用户�?,
  `password` varchar(100) NOT NULL COMMENT '密码',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机�?,
  `role` varchar(20) NOT NULL DEFAULT 'USER' COMMENT '角色: USER, ADMIN',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户�?;

-- 商品�?CREATE TABLE IF NOT EXISTS `products` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '商品名称',
  `description` text COMMENT '商品详情',
  `product_story` text COMMENT 'product story',
  `price` decimal(10,2) NOT NULL COMMENT '价格',
  `stock` int(11) NOT NULL DEFAULT '0' COMMENT '库存',
  `cover_img` varchar(255) DEFAULT NULL COMMENT '封面�?,
  `category` varchar(50) DEFAULT NULL COMMENT '分类',
  `origin` varchar(100) DEFAULT NULL COMMENT '产地',
  `farmer_name` varchar(50) DEFAULT NULL COMMENT '农户姓名',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品�?;

-- 订单�?CREATE TABLE IF NOT EXISTS `orders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_no` varchar(64) NOT NULL COMMENT '订单�?,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金�?,
  `status` varchar(20) NOT NULL COMMENT '状�? WAIT_PAY, WAIT_SHIP, SHIPPED, DONE',
  `address` varchar(255) DEFAULT NULL COMMENT '收货地址',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单�?;

-- 订单明细�?CREATE TABLE IF NOT EXISTS `order_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) NOT NULL COMMENT '订单ID',
  `product_id` bigint(20) DEFAULT NULL COMMENT '商品ID',
  `product_name` varchar(100) DEFAULT NULL COMMENT '商品名称快照',
  `price` decimal(10,2) NOT NULL COMMENT '购买单价',
  `quantity` int(11) NOT NULL COMMENT '购买数量',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细�?;

-- 3. 初始数据

-- 用户数据 (密码�?123456 的明文，因为本项目未引入 Security 加密)
-- 如果未来引入 Security，请�?'123456' 替换�?'$2a$10$...'
INSERT INTO `users` (`id`, `username`, `password`, `nickname`, `phone`, `role`) VALUES
(1, 'admin', '123456', '系统管理�?, '13800138000', 'ADMIN'),
(2, 'user', '123456', '测试用户', '13900139000', 'USER');

-- 商品数据 (5条真实利川红茶数�?
INSERT INTO `products` (`name`, `description`, `price`, `stock`, `cover_img`, `category`, `origin`, `farmer_name`) VALUES
('利川红·冷后浑(特级)', '采用核心产区鲜叶，传统工艺发酵，茶汤冷却后出现浅褐色浑浊，俗称“冷后浑”，是优质红茶的标志。口感鲜爽甘醇，花果香浓郁�?, 268.00, 100, 'https://placehold.co/400x400?text=Cold+After+Muddy', '冷后�?, '利川市毛坝镇', '王大�?),
('利川红·玛瑙红(一�?', '汤色红艳明亮，似玛瑙般晶莹剔透。滋味醇厚，回甘明显，适合日常品饮�?, 128.00, 200, 'https://placehold.co/400x400?text=Agate+Red', '玛瑙�?, '利川市沙溪乡', '沙溪茶叶合作�?),
('利川红·金�?礼盒�?', '精选单芽制作，金毫显露，外形美观。礼盒包装，高端大气，馈赠亲友首选�?, 388.00, 50, 'https://placehold.co/400x400?text=Gold+Red+Gift', '礼盒', '利川市忠路镇', '李大�?),
('利川红·高山有机袋泡茶', '来自海拔1200米高山茶园，有机种植，方便快捷，适合办公室冲泡�?, 58.00, 500, 'https://placehold.co/400x400?text=Tea+Bag', '袋泡�?, '利川市文斗镇', '文斗生态农�?),
('利川红·野生古树红�?, '采自百年以上野生茶树，内含物质丰富，耐冲泡，具有独特的山野气韵�?, 588.00, 20, 'https://placehold.co/400x400?text=Ancient+Tree', '古树�?, '利川市柏杨坝�?, '赵老三');

-- 轮播图数�?(本项目轮播图暂为前端硬编码，若需数据库化可扩�?banner �?
-- 此处仅作为数据预留，如果未来需要动态轮播图，可创建 banners �?-- CREATE TABLE `banners` ...

-- 4. �ۺ���������˿� / �˻��˿� / ������
CREATE TABLE IF NOT EXISTS `after_sales_requests` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) NOT NULL COMMENT '��������ID',
  `order_no` varchar(64) DEFAULT NULL COMMENT '�����ſ���',
  `user_id` bigint(20) NOT NULL COMMENT '�������û�ID',
  `type` varchar(30) NOT NULL COMMENT 'REFUND, RETURN_REFUND, EXCHANGE',
  `status` varchar(30) NOT NULL COMMENT 'PENDING_REVIEW, APPROVED, REJECTED, PROCESSING, COMPLETED',
  `reason` varchar(255) NOT NULL COMMENT '����ԭ��',
  `description` text COMMENT '��������',
  `contact_name` varchar(100) DEFAULT NULL COMMENT '��ϵ��',
  `contact_phone` varchar(30) DEFAULT NULL COMMENT '��ϵ��ʽ',
  `review_remark` varchar(255) DEFAULT NULL COMMENT '��˱�ע',
  `process_remark` varchar(255) DEFAULT NULL COMMENT '������ע',
  `handled_by` bigint(20) DEFAULT NULL COMMENT '�������û�ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_after_sales_order` (`order_id`),
  KEY `idx_after_sales_user` (`user_id`),
  KEY `idx_after_sales_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='�ۺ������';






-- farmer story management
CREATE TABLE IF NOT EXISTS `farmer_story` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `farmer_name` varchar(100) NOT NULL COMMENT '茶农姓名',
  `region` varchar(100) NOT NULL COMMENT '所在地区',
  `image_url` varchar(255) DEFAULT NULL COMMENT '图片地址',
  `tagline` varchar(120) DEFAULT NULL COMMENT '一句话标签',
  `summary` text COMMENT '简介摘要',
  `content` text COMMENT '详细故事内容',
  `sort_order` int(11) NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态: 1启用 0禁用',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_farmer_story_status` (`status`),
  KEY `idx_farmer_story_sort` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='茶农故事';
