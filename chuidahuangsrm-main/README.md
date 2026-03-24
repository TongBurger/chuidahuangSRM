# 炊大皇 SRM - 供应商关系管理系统

基于 Vue 3 开发的炊大皇供应商关系管理系统（SRM），严格遵循 Figma 原型设计。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue 3 | ^3.4.0 | 渐进式 JavaScript 框架 |
| Vite | ^5.1.4 | 极速前端构建工具 |
| TypeScript | ^5.3.3 | 静态类型支持 |
| Vue Router | ^4.3.0 | 官方路由管理 |
| Pinia | ^2.1.7 | 状态管理 |
| Tailwind CSS | ^3.4.1 | 原子化 CSS 框架 |
| lucide-vue-next | ^0.344.0 | 图标库 |
| ESLint + Prettier | - | 代码规范 |

## 快速启动

### 前置要求

- Node.js >= 18.0.0
- npm >= 9.0.0

### 安装依赖

```bash
cd srm-vue
npm install
```

### 启动开发服务器

```bash
npm run dev
```

访问 [http://localhost:5173](http://localhost:5173)

### 构建生产版本

```bash
npm run build
```

### 预览生产构建

```bash
npm run preview
```

### 代码检查与格式化

```bash
# ESLint 检查
npm run lint

# Prettier 格式化
npm run format
```

## 项目目录结构

```
srm-vue/
├── public/                  # 静态资源
│   └── favicon.svg
├── src/
│   ├── assets/
│   │   └── main.css         # 全局样式 (Tailwind CSS)
│   ├── components/
│   │   └── layout/
│   │       └── DashboardLayout.vue  # 主布局（侧边栏 + 顶部栏）
│   ├── data/
│   │   └── mockData.ts      # Mock 数据（供应商、订单、模具等）
│   ├── router/
│   │   └── index.ts         # Vue Router 路由配置
│   ├── stores/
│   │   └── useAppStore.ts   # Pinia 状态管理（用户、侧边栏）
│   ├── types/
│   │   └── index.ts         # TypeScript 类型定义
│   ├── views/
│   │   ├── DashboardView.vue          # 工作台
│   │   ├── NotFoundView.vue           # 404页面
│   │   ├── WarehouseView.vue          # 代加工仓储管理
│   │   ├── SourcingView.vue           # 战略寻源
│   │   ├── FinanceView.vue            # 财务对账
│   │   ├── UserManagementView.vue     # 用户管理
│   │   ├── supplier/
│   │   │   ├── SupplierListView.vue   # 供应商列表
│   │   │   └── SupplierDetailView.vue # 供应商详情
│   │   ├── order/
│   │   │   ├── OrderListView.vue      # 订单列表
│   │   │   └── OrderDetailView.vue    # 订单详情
│   │   ├── mold/
│   │   │   ├── MoldManagementView.vue # 模具管理
│   │   │   └── MoldDetailView.vue     # 模具详情
│   │   └── supplier-portal/
│   │       ├── SupplierPortalView.vue    # 供应商门户
│   │       └── SupplierRegisterView.vue  # 供应商注册
│   ├── App.vue              # 根组件
│   └── main.ts              # 入口文件
├── index.html               # HTML 模板
├── vite.config.ts           # Vite 配置
├── tailwind.config.js       # Tailwind CSS 配置
├── tsconfig.json            # TypeScript 配置
├── .eslintrc.cjs            # ESLint 配置
└── .prettierrc              # Prettier 配置
```

## 页面路由

| 路由路径 | 页面 | 说明 |
|---------|------|------|
| `/` | 工作台 | 关键指标、风险预警、供应商绩效排名 |
| `/suppliers` | 供应商管理 | 供应商列表、搜索筛选、新增供应商 |
| `/suppliers/:id` | 供应商详情 | 360°供应商画像、资质证书、绩效历史 |
| `/orders` | 订单协同 | 订单列表、物料明细、全流程追踪 |
| `/orders/:id` | 订单详情 | 订单信息、进度追踪、变更历史 |
| `/molds` | 模具管理 | 模具列表、寿命预警、维保管理 |
| `/molds/:id` | 模具详情 | 一模一码、寿命追踪、技术文档 |
| `/warehouse` | 代加工仓储 | 虚拟仓库、ASN管理、物料核销 |
| `/sourcing` | 战略寻源 | 寻源项目、报价对比、成本分析 |
| `/finance` | 财务对账 | 对账单、发票管理、付款记录 |
| `/users` | 用户管理 | 用户列表、RBAC权限控制 |
| `/supplier-portal` | 供应商门户 | 供应商视角：订单、对账、绩效 |
| `/supplier-register` | 供应商注册 | 4步骤注册流程 |

## 功能模块说明

### 工作台（Dashboard）
- 关键指标卡片（供应商总数、在途订单、模具状态、风险预警）
- 风险预警中心（资质即将到期、模具寿命预警）
- 供应商绩效 TOP5 排名
- 近期订单动态
- 供应商注册入口

### 供应商管理
- 供应商列表（搜索、分类、风险等级筛选）
- 供应商 360° 画像（质量/交付/成本/创新评分）
- 资质证书管理（有效/即将到期/已过期）
- 风险评估矩阵
- 绩效评价历史

### 订单协同
- 采购订单管理（与 ERP 用友U8 深度集成）
- 订单全流程追踪（待确认→生产中→已发货→已收货→已入库→已完成）
- 物料明细表
- 订单变更历史（版本记录）

### 模具管理
- 一模一码管理（二维码打印）
- 模具寿命追踪（设计寿命/已使用/剩余）
- 智能维保提醒（寿命 <15% 预警）
- 技术文档管理（CAD/3D图纸）

### 代加工仓储
- 虚拟仓库库存（理论库存 vs 实际库存）
- 差异预警（差异率 >2% 提醒）
- ASN 预发货通知管理
- 物料核销报告

### 战略寻源
- 寻源项目管理
- 多供应商报价对比
- 成本拆解分析（精细化核价）

### 财务对账
- 自动对账单生成
- 电子发票管理（OCR识别）
- 付款记录追踪
- 财务与 ERP 闭环

### 供应商门户
- 供应商自助注册（4步骤表单）
- 供应商视角工作台
- 订单确认与 ASN 创建
- 绩效评分查看

## 响应式设计

- **移动端** (< 768px)：侧边栏抽屉式，手势关闭，统计卡片单列
- **平板端** (768px - 1024px)：侧边栏隐藏，顶部菜单按钮触发
- **桌面端** (≥ 1024px)：侧边栏固定显示（宽 256px），内容区左偏移

## 状态管理（Pinia）

`useAppStore` 管理以下全局状态：

- `currentUser` - 当前登录用户信息（姓名、角色、部门）
- `sidebarOpen` - 移动端侧边栏展开状态
- `isLoggedIn` - 登录状态

## 代码规范

项目遵循 ESLint + Prettier 规范：

- 使用 `<script setup>` 语法（Composition API）
- TypeScript 严格模式
- 单引号、无分号、100字符行宽
