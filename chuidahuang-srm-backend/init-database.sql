-- 炊大皇SRM系统 - 数据库初始化脚本
-- 请使用有权限的MySQL账户（如root）执行此脚本

-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS SRMtest DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 2. 创建用户（如果不存在）
-- 注意：MySQL 8.0+ 使用 CREATE USER 语法
CREATE USER IF NOT EXISTS 'srmtest'@'%' IDENTIFIED BY 'SRMhw723SIIg';

-- 3. 授权所有权限
GRANT ALL PRIVILEGES ON SRMtest.* TO 'srmtest'@'%';

-- 4. 刷新权限
FLUSH PRIVILEGES;

-- 5. 切换到SRMtest数据库
USE SRMtest;

-- 6. 执行表结构和数据初始化（以下内容来自schema.sql）

-- ============================================
-- 供应商相关表
-- ============================================

DROP TABLE IF EXISTS `srm_supplier`;
CREATE TABLE `srm_supplier` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(200) NOT NULL COMMENT '供应商名称',
    `code` VARCHAR(50) NOT NULL COMMENT '供应商编码',
    `category` VARCHAR(20) NOT NULL DEFAULT '常规' COMMENT '供应商分类',
    `status` VARCHAR(20) NOT NULL DEFAULT '合格' COMMENT '状态',
    `risk_level` VARCHAR(10) NOT NULL DEFAULT '中' COMMENT '风险等级',
    `risk_score` INT NOT NULL DEFAULT 50 COMMENT '风险评分',
    `quality_score` INT NOT NULL DEFAULT 80 COMMENT '质量评分',
    `delivery_score` INT NOT NULL DEFAULT 80 COMMENT '交付评分',
    `cost_score` INT NOT NULL DEFAULT 80 COMMENT '成本评分',
    `innovation_score` INT NOT NULL DEFAULT 80 COMMENT '创新评分',
    `overall_score` DECIMAL(5,2) COMMENT '综合评分',
    `contact` VARCHAR(50) COMMENT '联系人',
    `phone` VARCHAR(20) COMMENT '联系电话',
    `email` VARCHAR(100) COMMENT '联系邮箱',
    `address` VARCHAR(500) COMMENT '地址',
    `annual_spend` DECIMAL(15,2) DEFAULT 0 COMMENT '年度采购额',
    `order_count` INT DEFAULT 0 COMMENT '订单数量',
    `on_time_rate` DECIMAL(5,2) DEFAULT 0 COMMENT '准时交付率',
    `quality_pass_rate` DECIMAL(5,2) DEFAULT 0 COMMENT '质量合格率',
    `remark` TEXT COMMENT '备注',
    `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(50) COMMENT '创建人',
    `update_by` VARCHAR(50) COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_category` (`category`),
    KEY `idx_status` (`status`),
    KEY `idx_risk_level` (`risk_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商表';

-- 初始化测试数据
INSERT INTO `srm_supplier` (`name`, `code`, `category`, `status`, `risk_level`, `risk_score`, `quality_score`, `delivery_score`, `cost_score`, `innovation_score`, `overall_score`, `contact`, `phone`, `email`, `address`, `annual_spend`, `order_count`, `on_time_rate`, `quality_pass_rate`)
VALUES
('深圳市精诚模具制造有限公司', 'SUP001', '战略', '合格', '低', 15, 95, 92, 88, 90, 91.25, '张伟', '13800138000', 'zhangwei@jingcheng.com', '深圳市宝安区西乡街道', 12500000, 156, 92.3, 98.5),
('东莞市华泰五金制品厂', 'SUP002', '关键', '合格', '中', 45, 88, 85, 92, 75, 85.00, '李明', '13900139000', 'liming@huatai.com', '东莞市长安镇', 5800000, 89, 85.6, 95.2),
('佛山市永盛铝制品有限公司', 'SUP003', '杠杆', '合格', '低', 20, 90, 88, 95, 80, 88.25, '王芳', '13700137000', 'wangfang@yongsheng.com', '佛山市南海区狮山镇', 3200000, 45, 88.9, 96.8);

-- ============================================
-- 订单相关表
-- ============================================

DROP TABLE IF EXISTS `srm_order_change_history`;
DROP TABLE IF EXISTS `srm_order_item`;
DROP TABLE IF EXISTS `srm_order`;

CREATE TABLE `srm_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号',
    `supplier_id` BIGINT NOT NULL COMMENT '供应商ID',
    `supplier_name` VARCHAR(200) COMMENT '供应商名称',
    `order_date` DATE NOT NULL COMMENT '订单日期',
    `delivery_date` DATE COMMENT '交货日期',
    `status` VARCHAR(20) NOT NULL DEFAULT '待确认' COMMENT '订单状态',
    `total_amount` DECIMAL(15,2) DEFAULT 0 COMMENT '订单总金额',
    `erp_order_no` VARCHAR(50) COMMENT 'ERP订单号',
    `erp_sync_status` VARCHAR(20) COMMENT 'U8同步状态',
    `erp_sync_time` DATETIME COMMENT 'U8同步时间',
    `remark` TEXT COMMENT '备注',
    `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(50) COMMENT '创建人',
    `update_by` VARCHAR(50) COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_supplier_id` (`supplier_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单表';

CREATE TABLE `srm_order_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `material_code` VARCHAR(50) NOT NULL COMMENT '物料编码',
    `material_name` VARCHAR(200) NOT NULL COMMENT '物料名称',
    `quantity` DECIMAL(15,2) NOT NULL COMMENT '数量',
    `unit` VARCHAR(20) COMMENT '单位',
    `price` DECIMAL(15,2) NOT NULL COMMENT '单价',
    `amount` DECIMAL(15,2) NOT NULL COMMENT '金额',
    `erp_material_code` VARCHAR(50) COMMENT 'ERP物料编码',
    `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

CREATE TABLE `srm_order_change_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `version` INT NOT NULL COMMENT '版本号',
    `change_date` DATE NOT NULL COMMENT '变更日期',
    `change_type` VARCHAR(50) NOT NULL COMMENT '变更类型',
    `description` VARCHAR(500) COMMENT '变更描述',
    `operator` VARCHAR(50) COMMENT '操作人',
    `before_content` TEXT COMMENT '变更前内容',
    `after_content` TEXT COMMENT '变更后内容',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单变更历史表';

-- 初始化测试订单数据
INSERT INTO `srm_order` (`order_no`, `supplier_id`, `supplier_name`, `order_date`, `delivery_date`, `status`, `total_amount`, `create_by`)
VALUES
('PO2026031901', 1, '深圳市精诚模具制造有限公司', '2026-03-15', '2026-04-15', '生产中', 285000, '采购员-张三'),
('PO2026031902', 2, '东莞市华泰五金制品厂', '2026-03-18', '2026-03-28', '已发货', 52000, '采购员-李四');

-- ============================================
-- 模具相关表
-- ============================================

DROP TABLE IF EXISTS `srm_mold_production_record`;
DROP TABLE IF EXISTS `srm_mold`;

CREATE TABLE `srm_mold` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `code` VARCHAR(50) NOT NULL COMMENT '模具编号',
    `name` VARCHAR(200) NOT NULL COMMENT '模具名称',
    `category` VARCHAR(50) COMMENT '模具分类',
    `status` VARCHAR(20) NOT NULL DEFAULT '在用' COMMENT '状态',
    `location` VARCHAR(200) COMMENT '存放位置',
    `supplier_id` BIGINT COMMENT '供应商ID',
    `supplier_name` VARCHAR(200) COMMENT '供应商名称',
    `original_value` DECIMAL(15,2) COMMENT '模具原值',
    `design_life` BIGINT NOT NULL COMMENT '设计寿命',
    `current_usage` BIGINT DEFAULT 0 COMMENT '当前使用次数',
    `remaining_life` DECIMAL(5,2) COMMENT '剩余寿命',
    `last_maintenance_date` DATE COMMENT '上次维保日期',
    `next_maintenance_date` DATE COMMENT '下次维保日期',
    `drawings` TEXT COMMENT '图纸文件',
    `qr_code` VARCHAR(100) COMMENT '二维码',
    `remark` TEXT COMMENT '备注',
    `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(50) COMMENT '创建人',
    `update_by` VARCHAR(50) COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_supplier_id` (`supplier_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模具表';

CREATE TABLE `srm_mold_production_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `mold_id` BIGINT NOT NULL COMMENT '模具ID',
    `order_id` VARCHAR(50) COMMENT '生产订单号',
    `production_date` DATE NOT NULL COMMENT '生产日期',
    `quantity` BIGINT NOT NULL COMMENT '生产数量',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_mold_id` (`mold_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模具生产记录表';

-- ============================================
-- 仓库相关表
-- ============================================

DROP TABLE IF EXISTS `srm_asn_item`;
DROP TABLE IF EXISTS `srm_asn`;
DROP TABLE IF EXISTS `srm_virtual_warehouse`;

CREATE TABLE `srm_virtual_warehouse` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `supplier_id` BIGINT NOT NULL COMMENT '供应商ID',
    `supplier_name` VARCHAR(200) COMMENT '供应商名称',
    `material_code` VARCHAR(50) NOT NULL COMMENT '物料编码',
    `material_name` VARCHAR(200) NOT NULL COMMENT '物料名称',
    `theoretical_qty` DECIMAL(15,2) NOT NULL COMMENT '理论发料数量',
    `actual_qty` DECIMAL(15,2) NOT NULL COMMENT '实际消耗数量',
    `variance` DECIMAL(15,2) COMMENT '差异数量',
    `variance_rate` DECIMAL(10,4) COMMENT '差异率',
    `unit` VARCHAR(20) COMMENT '单位',
    `wip_qty` DECIMAL(15,2) DEFAULT 0 COMMENT '在制品数量',
    `defect_qty` DECIMAL(15,2) DEFAULT 0 COMMENT '残次品数量',
    `finished_qty` DECIMAL(15,2) DEFAULT 0 COMMENT '成品数量',
    `last_update_date` DATE COMMENT '最后更新日期',
    `remark` TEXT COMMENT '备注',
    `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_supplier_id` (`supplier_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='虚拟仓库表';

CREATE TABLE `srm_asn` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `asn_no` VARCHAR(50) NOT NULL COMMENT 'ASN编号',
    `supplier_id` BIGINT NOT NULL COMMENT '供应商ID',
    `supplier_name` VARCHAR(200) COMMENT '供应商名称',
    `order_no` VARCHAR(50) COMMENT '关联订单号',
    `ship_date` DATE NOT NULL COMMENT '发货日期',
    `estimated_arrival` DATE COMMENT '预计到达日期',
    `actual_arrival` DATE COMMENT '实际到达日期',
    `status` VARCHAR(20) NOT NULL DEFAULT '待发货' COMMENT '状态',
    `logistics_company` VARCHAR(100) COMMENT '物流公司',
    `tracking_no` VARCHAR(100) COMMENT '物流单号',
    `remark` TEXT COMMENT '备注',
    `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(50) COMMENT '创建人',
    `update_by` VARCHAR(50) COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_asn_no` (`asn_no`),
    KEY `idx_supplier_id` (`supplier_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ASN发货通知单表';

CREATE TABLE `srm_asn_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `asn_id` BIGINT NOT NULL COMMENT 'ASN ID',
    `material_code` VARCHAR(50) NOT NULL COMMENT '物料编码',
    `material_name` VARCHAR(200) NOT NULL COMMENT '物料名称',
    `quantity` DECIMAL(15,2) NOT NULL COMMENT '数量',
    `unit` VARCHAR(20) COMMENT '单位',
    `barcode` VARCHAR(100) COMMENT '条码',
    `box_count` INT COMMENT '箱数',
    `pallet_count` INT COMMENT '托盘数',
    `actual_arrival` DATETIME COMMENT '实际收货时间',
    `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_asn_id` (`asn_id`),
    KEY `idx_barcode` (`barcode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ASN明细表';

-- ============================================
-- 用户表
-- ============================================

DROP TABLE IF EXISTS `srm_user`;

CREATE TABLE `srm_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(200) NOT NULL COMMENT '密码',
    `name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `role` VARCHAR(50) NOT NULL COMMENT '角色',
    `department` VARCHAR(100) COMMENT '部门',
    `status` VARCHAR(20) NOT NULL DEFAULT '在职' COMMENT '状态',
    `phone` VARCHAR(20) COMMENT '手机号',
    `email` VARCHAR(100) COMMENT '邮箱',
    `supplier_id` BIGINT COMMENT '供应商ID',
    `last_login_time` DATETIME COMMENT '最后登录时间',
    `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 初始化管理员用户 (密码: admin123)
INSERT INTO `srm_user` (`username`, `password`, `name`, `role`, `department`, `status`, `phone`, `email`)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', '采购员', '采购部', '在职', '13800138000', 'admin@chudahuang.com');

-- ============================================
-- 供应商资质证书表
-- ============================================

DROP TABLE IF EXISTS `srm_supplier_certification`;

CREATE TABLE `srm_supplier_certification` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `supplier_id` BIGINT NOT NULL COMMENT '供应商ID',
    `name` VARCHAR(200) NOT NULL COMMENT '证书名称',
    `certificate_no` VARCHAR(100) COMMENT '证书编号',
    `type` VARCHAR(50) COMMENT '证书类型',
    `valid_until` DATE COMMENT '有效期至',
    `status` VARCHAR(20) COMMENT '状态',
    `file_path` VARCHAR(500) COMMENT '证书文件路径',
    `alert_days` INT DEFAULT 30 COMMENT '预警天数',
    `remark` TEXT COMMENT '备注',
    `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_supplier_id` (`supplier_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商资质证书表';

-- 完成提示
SELECT '数据库初始化完成！' AS '状态';
SELECT COUNT(*) AS '供应商数量' FROM srm_supplier;
SELECT COUNT(*) AS '订单数量' FROM srm_order;
SELECT COUNT(*) AS '模具数量' FROM srm_mold;
