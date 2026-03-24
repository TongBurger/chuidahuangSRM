-- 炊大皇SRM系统 - PostgreSQL数据库初始化脚本
-- 请使用有权限的PostgreSQL账户（如postgres）执行此脚本

-- 1. 创建数据库
CREATE DATABASE cdh_test
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'zh_CN.UTF-8'
    LC_CTYPE = 'zh_CN.UTF-8'
    TEMPLATE = template0
    CONNECTION LIMIT = -1;

-- 2. 创建用户（如果不存在）
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'srmtest') THEN
        CREATE ROLE srmtest LOGIN PASSWORD 'SRMhw723SIIg';
    END IF;
END
$$;

-- 3. 授权所有权限
GRANT ALL PRIVILEGES ON DATABASE cdh_test TO srmtest;

-- 4. 切换到cdh_test数据库
\c cdh_test

-- 5. 授予schema权限
GRANT ALL ON SCHEMA public TO srmtest;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO srmtest;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO srmtest;

-- 6. 执行表结构和数据初始化

-- ============================================
-- 供应商相关表
-- ============================================

DROP TABLE IF EXISTS srm_supplier CASCADE;
CREATE TABLE srm_supplier (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    category VARCHAR(20) NOT NULL DEFAULT '常规',
    status VARCHAR(20) NOT NULL DEFAULT '合格',
    risk_level VARCHAR(10) NOT NULL DEFAULT '中',
    risk_score INT NOT NULL DEFAULT 50,
    quality_score INT NOT NULL DEFAULT 80,
    delivery_score INT NOT NULL DEFAULT 80,
    cost_score INT NOT NULL DEFAULT 80,
    innovation_score INT NOT NULL DEFAULT 80,
    overall_score NUMERIC(5,2),
    contact VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    address VARCHAR(500),
    annual_spend NUMERIC(15,2) DEFAULT 0,
    order_count INT DEFAULT 0,
    on_time_rate NUMERIC(5,2) DEFAULT 0,
    quality_pass_rate NUMERIC(5,2) DEFAULT 0,
    remark TEXT,
    deleted INT NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(50),
    update_by VARCHAR(50)
);

-- 创建索引
CREATE INDEX idx_supplier_category ON srm_supplier(category);
CREATE INDEX idx_supplier_status ON srm_supplier(status);
CREATE INDEX idx_supplier_risk_level ON srm_supplier(risk_level);

-- 添加表注释
COMMENT ON TABLE srm_supplier IS '供应商表';
COMMENT ON COLUMN srm_supplier.id IS '主键ID';
COMMENT ON COLUMN srm_supplier.name IS '供应商名称';
COMMENT ON COLUMN srm_supplier.code IS '供应商编码';
COMMENT ON COLUMN srm_supplier.category IS '供应商分类';
COMMENT ON COLUMN srm_supplier.status IS '状态';
COMMENT ON COLUMN srm_supplier.risk_level IS '风险等级';
COMMENT ON COLUMN srm_supplier.risk_score IS '风险评分';
COMMENT ON COLUMN srm_supplier.overall_score IS '综合评分';

-- 初始化测试数据
INSERT INTO srm_supplier (name, code, category, status, risk_level, risk_score, quality_score, delivery_score, cost_score, innovation_score, overall_score, contact, phone, email, address, annual_spend, order_count, on_time_rate, quality_pass_rate)
VALUES
('深圳市精诚模具制造有限公司', 'SUP001', '战略', '合格', '低', 15, 95, 92, 88, 90, 91.25, '张伟', '13800138000', 'zhangwei@jingcheng.com', '深圳市宝安区西乡街道', 12500000, 156, 92.3, 98.5),
('东莞市华泰五金制品厂', 'SUP002', '关键', '合格', '中', 45, 88, 85, 92, 75, 85.00, '李明', '13900139000', 'liming@huatai.com', '东莞市长安镇', 5800000, 89, 85.6, 95.2),
('佛山市永盛铝制品有限公司', 'SUP003', '杠杆', '合格', '低', 20, 90, 88, 95, 80, 88.25, '王芳', '13700137000', 'wangfang@yongsheng.com', '佛山市南海区狮山镇', 3200000, 45, 88.9, 96.8);

-- ============================================
-- 订单相关表
-- ============================================

DROP TABLE IF EXISTS srm_order_change_history CASCADE;
DROP TABLE IF EXISTS srm_order_item CASCADE;
DROP TABLE IF EXISTS srm_order CASCADE;

CREATE TABLE srm_order (
    id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL UNIQUE,
    supplier_id BIGINT NOT NULL,
    supplier_name VARCHAR(200),
    order_date DATE NOT NULL,
    delivery_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT '待确认',
    total_amount NUMERIC(15,2) DEFAULT 0,
    erp_order_no VARCHAR(50),
    erp_sync_status VARCHAR(20),
    erp_sync_time TIMESTAMP,
    remark TEXT,
    deleted INT NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(50),
    update_by VARCHAR(50)
);

CREATE INDEX idx_order_supplier_id ON srm_order(supplier_id);
CREATE INDEX idx_order_status ON srm_order(status);
COMMENT ON TABLE srm_order IS '采购订单表';

CREATE TABLE srm_order_item (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    material_code VARCHAR(50) NOT NULL,
    material_name VARCHAR(200) NOT NULL,
    quantity NUMERIC(15,2) NOT NULL,
    unit VARCHAR(20),
    price NUMERIC(15,2) NOT NULL,
    amount NUMERIC(15,2) NOT NULL,
    erp_material_code VARCHAR(50),
    deleted INT NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_order_item_order_id ON srm_order_item(order_id);
COMMENT ON TABLE srm_order_item IS '订单明细表';

CREATE TABLE srm_order_change_history (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    version INT NOT NULL,
    change_date DATE NOT NULL,
    change_type VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    operator VARCHAR(50),
    before_content TEXT,
    after_content TEXT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_order_change_history_order_id ON srm_order_change_history(order_id);
COMMENT ON TABLE srm_order_change_history IS '订单变更历史表';

-- 初始化测试订单数据
INSERT INTO srm_order (order_no, supplier_id, supplier_name, order_date, delivery_date, status, total_amount, create_by)
VALUES
('PO2026031901', 1, '深圳市精诚模具制造有限公司', '2026-03-15', '2026-04-15', '生产中', 285000, '采购员-张三'),
('PO2026031902', 2, '东莞市华泰五金制品厂', '2026-03-18', '2026-03-28', '已发货', 52000, '采购员-李四');

-- ============================================
-- 模具相关表
-- ============================================

DROP TABLE IF EXISTS srm_mold_production_record CASCADE;
DROP TABLE IF EXISTS srm_mold CASCADE;

CREATE TABLE srm_mold (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    category VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT '在用',
    location VARCHAR(200),
    supplier_id BIGINT,
    supplier_name VARCHAR(200),
    original_value NUMERIC(15,2),
    design_life BIGINT NOT NULL,
    current_usage BIGINT DEFAULT 0,
    remaining_life NUMERIC(5,2),
    last_maintenance_date DATE,
    next_maintenance_date DATE,
    drawings TEXT,
    qr_code VARCHAR(100),
    remark TEXT,
    deleted INT NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(50),
    update_by VARCHAR(50)
);

CREATE INDEX idx_mold_supplier_id ON srm_mold(supplier_id);
CREATE INDEX idx_mold_status ON srm_mold(status);
COMMENT ON TABLE srm_mold IS '模具表';

CREATE TABLE srm_mold_production_record (
    id BIGSERIAL PRIMARY KEY,
    mold_id BIGINT NOT NULL,
    order_id VARCHAR(50),
    production_date DATE NOT NULL,
    quantity BIGINT NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_mold_production_record_mold_id ON srm_mold_production_record(mold_id);
COMMENT ON TABLE srm_mold_production_record IS '模具生产记录表';

-- ============================================
-- 仓库相关表
-- ============================================

DROP TABLE IF EXISTS srm_asn_item CASCADE;
DROP TABLE IF EXISTS srm_asn CASCADE;
DROP TABLE IF EXISTS srm_virtual_warehouse CASCADE;

CREATE TABLE srm_virtual_warehouse (
    id BIGSERIAL PRIMARY KEY,
    supplier_id BIGINT NOT NULL,
    supplier_name VARCHAR(200),
    material_code VARCHAR(50) NOT NULL,
    material_name VARCHAR(200) NOT NULL,
    theoretical_qty NUMERIC(15,2) NOT NULL,
    actual_qty NUMERIC(15,2) NOT NULL,
    variance NUMERIC(15,2),
    variance_rate NUMERIC(10,4),
    unit VARCHAR(20),
    wip_qty NUMERIC(15,2) DEFAULT 0,
    defect_qty NUMERIC(15,2) DEFAULT 0,
    finished_qty NUMERIC(15,2) DEFAULT 0,
    last_update_date DATE,
    remark TEXT,
    deleted INT NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_virtual_warehouse_supplier_id ON srm_virtual_warehouse(supplier_id);
COMMENT ON TABLE srm_virtual_warehouse IS '虚拟仓库表';

CREATE TABLE srm_asn (
    id BIGSERIAL PRIMARY KEY,
    asn_no VARCHAR(50) NOT NULL UNIQUE,
    supplier_id BIGINT NOT NULL,
    supplier_name VARCHAR(200),
    order_no VARCHAR(50),
    ship_date DATE NOT NULL,
    estimated_arrival DATE,
    actual_arrival DATE,
    status VARCHAR(20) NOT NULL DEFAULT '待发货',
    logistics_company VARCHAR(100),
    tracking_no VARCHAR(100),
    remark TEXT,
    deleted INT NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(50),
    update_by VARCHAR(50)
);

CREATE INDEX idx_asn_supplier_id ON srm_asn(supplier_id);
CREATE INDEX idx_asn_status ON srm_asn(status);
COMMENT ON TABLE srm_asn IS 'ASN发货通知单表';

CREATE TABLE srm_asn_item (
    id BIGSERIAL PRIMARY KEY,
    asn_id BIGINT NOT NULL,
    material_code VARCHAR(50) NOT NULL,
    material_name VARCHAR(200) NOT NULL,
    quantity NUMERIC(15,2) NOT NULL,
    unit VARCHAR(20),
    barcode VARCHAR(100),
    box_count INT,
    pallet_count INT,
    actual_arrival TIMESTAMP,
    deleted INT NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_asn_item_asn_id ON srm_asn_item(asn_id);
CREATE INDEX idx_asn_item_barcode ON srm_asn_item(barcode);
COMMENT ON TABLE srm_asn_item IS 'ASN明细表';

-- ============================================
-- 用户表
-- ============================================

DROP TABLE IF EXISTS srm_user CASCADE;

CREATE TABLE srm_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(200) NOT NULL,
    name VARCHAR(50) NOT NULL,
    role VARCHAR(50) NOT NULL,
    department VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT '在职',
    phone VARCHAR(20),
    email VARCHAR(100),
    supplier_id BIGINT,
    last_login_time TIMESTAMP,
    deleted INT NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_role ON srm_user(role);
COMMENT ON TABLE srm_user IS '用户表';

-- 初始化管理员用户 (密码: admin123)
-- BCrypt加密后的密码
INSERT INTO srm_user (username, password, name, role, department, status, phone, email)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', '采购员', '采购部', '在职', '13800138000', 'admin@chudahuang.com');

-- ============================================
-- 供应商资质证书表
-- ============================================

DROP TABLE IF EXISTS srm_supplier_certification CASCADE;

CREATE TABLE srm_supplier_certification (
    id BIGSERIAL PRIMARY KEY,
    supplier_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    certificate_no VARCHAR(100),
    type VARCHAR(50),
    valid_until DATE,
    status VARCHAR(20),
    file_path VARCHAR(500),
    alert_days INT DEFAULT 30,
    remark TEXT,
    deleted INT NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_supplier_certification_supplier_id ON srm_supplier_certification(supplier_id);
COMMENT ON TABLE srm_supplier_certification IS '供应商资质证书表';

-- 完成提示
SELECT 'PostgreSQL数据库初始化完成！' AS status;
SELECT COUNT(*) AS supplier_count FROM srm_supplier;
SELECT COUNT(*) AS order_count FROM srm_order;
SELECT COUNT(*) AS mold_count FROM srm_mold;
