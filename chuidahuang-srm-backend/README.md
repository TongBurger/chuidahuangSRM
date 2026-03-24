# 炊大皇 SRM 系统后端

炊大皇供应商关系管理系统后端服务，基于 Spring Boot 3 + MyBatis Plus 构建。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.0 | 基础框架 |
| MyBatis Plus | 3.5.5 | ORM框架 |
| MySQL | 8.0+ | 数据库 |
| Druid | 1.2.20 | 数据库连接池 |
| Knife4j | 4.3.0 | API文档 |
| Hutool | 5.8.23 | 工具类库 |
| Lombok | - | 简化代码 |

## 项目结构

```
chuidahuang-srm-backend/
├── src/main/java/com/chudahuang/srm/
│   ├── SrmApplication.java          # 启动类
│   ├── common/                       # 通用类
│   │   ├── Result.java               # 统一响应结果
│   │   └── PageResult.java           # 分页结果
│   ├── config/                       # 配置类
│   │   ├── CorsConfig.java           # 跨域配置
│   │   ├── Knife4jConfig.java        # API文档配置
│   │   ├── MyMetaObjectHandler.java  # 字段自动填充
│   │   └── MybatisPlusConfig.java    # MyBatis Plus配置
│   ├── controller/                   # 控制器层
│   │   ├── DashboardController.java  # 工作台
│   │   ├── MoldController.java       # 模具管理
│   │   ├── OrderController.java      # 订单协同
│   │   ├── SupplierController.java   # 供应商管理
│   │   └── WarehouseController.java  # 仓储管理
│   ├── dto/                          # 数据传输对象
│   ├── entity/                       # 实体类
│   ├── mapper/                       # 数据访问层
│   └── service/                      # 业务逻辑层
└── src/main/resources/
    ├── application.yml                # 应用配置
    └── db/schema.sql                  # 数据库初始化脚本
```

## 快速启动

### 前置要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+

### 1. 数据库初始化

```bash
# 连接到MySQL
mysql -h 10.220.5.164 -u srmtest -p

# 执行初始化脚本
source D:\2026工作内容\4.0 采购部信息\外部公司SRM项目\Demo代码\chuidahuang-srm-backend\src\main\resources\db\schema.sql
```

### 2. 修改配置

编辑 `src/main/resources/application.yml`，确认数据库连接信息：

```yaml
spring:
  datasource:
    druid:
      url: jdbc:mysql://10.220.5.164:3306/SRMtest?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
      username: srmtest
      password: SRMhw723SIIg
```

### 3. 编译运行

```bash
# 进入项目目录
cd D:\2026工作内容\4.0 采购部信息\外部公司SRM项目\Demo代码\chuidahuang-srm-backend

# 编译项目
mvn clean install

# 运行项目
mvn spring-boot:run
```

### 4. 访问服务

- 服务地址: http://localhost:8080/api
- API文档: http://localhost:8080/api/doc.html

## API接口说明

### 工作台
- `GET /api/dashboard` - 获取工作台统计数据

### 供应商管理
- `GET /api/suppliers` - 分页查询供应商列表
- `GET /api/suppliers/{id}` - 获取供应商详情
- `POST /api/suppliers` - 新增供应商
- `PUT /api/suppliers/{id}` - 更新供应商
- `DELETE /api/suppliers/{id}` - 删除供应商
- `GET /api/suppliers/statistics` - 获取供应商统计信息

### 订单协同
- `GET /api/orders` - 分页查询订单列表
- `GET /api/orders/{id}` - 获取订单详情
- `POST /api/orders` - 创建订单
- `PUT /api/orders/{id}` - 更新订单
- `DELETE /api/orders/{id}` - 删除订单
- `POST /api/orders/{id}/confirm` - 供应商确认订单
- `PUT /api/orders/{id}/status` - 更新订单状态
- `POST /api/orders/{id}/sync-erp` - 同步到ERP
- `GET /api/orders/statistics` - 获取订单统计信息

### 模具管理
- `GET /api/molds` - 分页查询模具列表
- `GET /api/molds/{id}` - 获取模具详情
- `POST /api/molds` - 创建模具
- `PUT /api/molds/{id}` - 更新模具
- `DELETE /api/molds/{id}` - 删除模具
- `POST /api/molds/{id}/usage` - 更新模具使用次数
- `POST /api/molds/{id}/maintenance` - 安排维保
- `POST /api/molds/{id}/maintenance/complete` - 完成维保
- `GET /api/molds/statistics` - 获取模具统计信息
- `GET /api/molds/maintenance-alerts` - 获取需要维保预警的模具

### 仓储管理
- `GET /api/warehouse/virtual` - 分页查询虚拟仓库库存
- `GET /api/warehouse/virtual/{id}` - 获取虚拟仓库详情
- `PUT /api/warehouse/virtual/{id}` - 更新虚拟仓库库存
- `GET /api/warehouse/asn` - 分页查询ASN列表
- `GET /api/warehouse/asn/{id}` - 获取ASN详情
- `POST /api/warehouse/asn` - 创建ASN
- `PUT /api/warehouse/asn/{id}/status` - 更新ASN状态
- `GET /api/warehouse/asn/{id}/print-labels` - 打印ASN标签
- `POST /api/warehouse/asn/{id}/scan-receipt` - 扫码收货
- `GET /api/warehouse/reconciliation` - 生成物料核销报告
- `GET /api/warehouse/statistics` - 获取仓库统计信息

## 数据库设计

### 核心表结构

| 表名 | 说明 |
|------|------|
| srm_supplier | 供应商表 |
| srm_supplier_certification | 供应商资质证书表 |
| srm_order | 订单表 |
| srm_order_item | 订单明细表 |
| srm_order_change_history | 订单变更历史表 |
| srm_mold | 模具表 |
| srm_mold_production_record | 模具生产记录表 |
| srm_virtual_warehouse | 虚拟仓库表 |
| srm_asn | ASN发货通知单表 |
| srm_asn_item | ASN明细表 |
| srm_user | 用户表 |

## 前后端对接

前端项目路径：`D:\2026工作内容\4.0 采购部信息\外部公司SRM项目\Demo代码\chuidahuangsrm-main`

前端需要将Mock数据替换为API调用，示例：

```typescript
// 替换前
import { mockSuppliers } from '@/data/mockData'

// 替换后
import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api'

export async function getSuppliers(params: any) {
  const response = await axios.get(`${API_BASE_URL}/suppliers`, { params })
  return response.data
}
```

## 开发规范

### 响应格式

所有接口统一使用以下响应格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1710900000000
}
```

### 分页格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "current": 1,
    "size": 10,
    "total": 100,
    "pages": 10,
    "records": []
  },
  "timestamp": 1710900000000
}
```

## 许可证

Apache License 2.0
