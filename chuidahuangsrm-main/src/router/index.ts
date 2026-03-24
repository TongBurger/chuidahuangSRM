import { createRouter, createWebHashHistory } from 'vue-router'
import { useAppStore } from '@/stores/useAppStore'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { title: '登录', requiresAuth: false },
    },
    // 内部用户路由
    {
      path: '/',
      component: () => import('@/components/layout/DashboardLayout.vue'),
      meta: { requiresAuth: true, userType: 'internal' },
      children: [
        {
          path: '',
          name: 'dashboard',
          component: () => import('@/views/DashboardView.vue'),
          meta: { title: '工作台' },
        },
        {
          path: 'suppliers',
          name: 'suppliers',
          component: () => import('@/views/supplier/SupplierListView.vue'),
          meta: { title: '供应商管理' },
        },
        {
          path: 'suppliers/:id',
          name: 'supplier-detail',
          component: () => import('@/views/supplier/SupplierDetailView.vue'),
          meta: { title: '供应商详情' },
        },
        {
          path: 'orders',
          name: 'orders',
          component: () => import('@/views/order/OrderListView.vue'),
          meta: { title: '订单协同' },
        },
        {
          path: 'orders/:id',
          name: 'order-detail',
          component: () => import('@/views/order/OrderDetailView.vue'),
          meta: { title: '订单详情' },
        },
        {
          path: 'molds',
          name: 'molds',
          component: () => import('@/views/mold/MoldManagementView.vue'),
          meta: { title: '模具管理' },
        },
        {
          path: 'molds/:id',
          name: 'mold-detail',
          component: () => import('@/views/mold/MoldDetailView.vue'),
          meta: { title: '模具详情' },
        },
        {
          path: 'warehouse',
          name: 'warehouse',
          component: () => import('@/views/WarehouseView.vue'),
          meta: { title: '代加工仓储' },
        },
        {
          path: 'sourcing',
          name: 'sourcing',
          component: () => import('@/views/SourcingView.vue'),
          meta: { title: '战略寻源' },
        },
        {
          path: 'finance',
          name: 'finance',
          component: () => import('@/views/FinanceView.vue'),
          meta: { title: '财务对账' },
        },
        {
          path: 'users',
          name: 'users',
          component: () => import('@/views/UserManagementView.vue'),
          meta: { title: '用户管理' },
        },
        {
          path: 'supplier-registrations',
          name: 'supplier-registrations',
          component: () => import('@/views/supplier/SupplierRegistrationManagementView.vue'),
          meta: { title: '供应商注册审核' },
        },
        {
          path: 'risk-alerts',
          name: 'risk-alerts',
          component: () => import('@/views/RiskAlertsView.vue'),
          meta: { title: '风险预警' },
        },
        {
          path: ':pathMatch(.*)*',
          name: 'not-found',
          component: () => import('@/views/NotFoundView.vue'),
          meta: { title: '页面不存在' },
        },
      ],
    },
    // 供应商门户路由
    {
      path: '/supplier-portal',
      component: () => import('@/components/layout/SupplierPortalLayout.vue'),
      meta: { requiresAuth: true, userType: 'supplier' },
      children: [
        {
          path: '',
          name: 'supplier-portal',
          component: () => import('@/views/supplier-portal/SupplierPortalView.vue'),
          meta: { title: '供应商门户' },
        },
        {
          path: 'orders',
          name: 'supplier-portal-orders',
          component: () => import('@/views/supplier-portal/SupplierOrdersView.vue'),
          meta: { title: '我的订单' },
        },
        {
          path: 'asn',
          name: 'supplier-portal-asn',
          component: () => import('@/views/supplier-portal/SupplierASNView.vue'),
          meta: { title: '发货管理' },
        },
        {
          path: 'inventory',
          name: 'supplier-portal-inventory',
          component: () => import('@/views/supplier-portal/SupplierInventoryView.vue'),
          meta: { title: '库存管理' },
        },
        {
          path: 'reconciliation',
          name: 'supplier-portal-reconciliation',
          component: () => import('@/views/supplier-portal/SupplierReconciliationView.vue'),
          meta: { title: '财务对账' },
        },
        {
          path: 'invoice',
          name: 'supplier-portal-invoice',
          component: () => import('@/views/supplier-portal/SupplierInvoiceView.vue'),
          meta: { title: '发票管理' },
        },
        {
          path: 'performance',
          name: 'supplier-portal-performance',
          component: () => import('@/views/supplier-portal/SupplierPerformanceView.vue'),
          meta: { title: '绩效报告' },
        },
        {
          path: 'profile',
          name: 'supplier-portal-profile',
          component: () => import('@/views/supplier-portal/SupplierProfileView.vue'),
          meta: { title: '企业信息' },
        },
      ],
    },
    {
      path: '/supplier-register',
      name: 'supplier-register',
      component: () => import('@/views/supplier-portal/SupplierRegisterView.vue'),
      meta: { title: '供应商注册', requiresAuth: false },
    },
  ],
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const appStore = useAppStore()

  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 炊大皇 SRM` : '炊大皇 SRM'

  // 检查是否需要登录
  const requiresAuth = to.meta.requiresAuth !== false

  if (requiresAuth && !appStore.isLoggedIn) {
    // 需要登录但未登录，跳转到登录页
    next({ name: 'login', query: { redirect: to.fullPath } })
  } else if (to.name === 'login' && appStore.isLoggedIn) {
    // 已登录用户访问登录页，根据角色跳转
    if (appStore.currentUser.role === '供应商业务员') {
      next({ name: 'supplier-portal' })
    } else {
      next({ name: 'dashboard' })
    }
  } else {
    next()
  }
})

export default router
