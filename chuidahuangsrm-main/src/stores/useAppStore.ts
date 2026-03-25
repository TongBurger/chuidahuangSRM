import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface CurrentUser {
  id: string
  name: string
  role: string
  department: string
  avatar: string
  companyName?: string
}

export interface Toast {
  id: string
  message: string
  type: 'success' | 'error' | 'warning' | 'info'
}

export const useAppStore = defineStore('app', () => {
  type Language = 'zh-CN' | 'en-US'
  type Theme = 'light' | 'dark'

  const language = ref<Language>('zh-CN')
  const theme = ref<Theme>('light')

  const messages: Record<Language, Record<string, string>> = {
    'zh-CN': {
      'system.name': '炊大皇 SRM',
      'theme.light': '浅色',
      'theme.dark': '深色',
      'lang.zh': '中文',
      'lang.en': 'English',
      'nav.dashboard': '工作台',
      'nav.suppliers': '供应商管理',
      'nav.orders': '订单协同',
      'nav.molds': '模具管理',
      'nav.warehouse': '代加工送货管理',
      'nav.sourcing': '战略寻源',
      'nav.finance': '财务对账',
      'nav.riskAlerts': '风险预警',
      'nav.users': '用户管理',
      'nav.supplierRegistrations': '供应商注册审核',
      'nav.portal': '供应商门户',
      'page.dashboard.title': '工作台',
      'page.suppliers.title': '供应商管理',
      'page.orders.title': '订单协同',
      'page.molds.title': '模具管理',
      'page.warehouse.title': '代加工仓储管理',
      'page.sourcing.title': '战略寻源',
      'page.finance.title': '财务对账',
      'page.riskAlerts.title': '风险预警',
      'page.users.title': '用户管理',
      'page.supplierRegistrations.title': '供应商注册审核',
      'btn.goRegister': '前往注册',
      'btn.addUser': '添加用户',
      'btn.addSupplier': '新增供应商',
      'btn.createOrder': '创建订单',
      'btn.addMold': '新增模具',
      'btn.createSourcingProject': '创建寻源项目',
      'btn.grid': '网格',
      'btn.list': '列表',
      'btn.viewDetail': '查看详情',
      'btn.performanceReview': '绩效评价',
      'btn.confirmOrder': '确认订单',
      'btn.confirmReceipt': '确认收货',
      'btn.scheduleMaintenance': '安排维保',
      'btn.createAsn': '创建ASN',
      'btn.printLabel': '打印标签',
      'btn.scanReceipt': '扫码收货',
      'btn.generateReconciliation': '生成核销单',
      'btn.quoteComparison': '报价对比',
      'btn.finalizeAward': '定标',
      'btn.viewStatementDetail': '查看明细',
      'btn.downloadStatement': '下载对账单',
      'btn.pushSupplierConfirm': '推送供应商确认',
      'btn.initiatePayment': '发起付款申请',
      'btn.dark': '深色',
      'btn.light': '浅色',
      'pending.title': '待办事项',
      'pending.total': '项待处理',
      'pending.noItems': '暂无待办事项',
      'pending.pendingOrders': '待确认订单',
      'pending.pendingMolds': '待维保模具',
      'pending.pendingReconciliations': '待对账单据',
      'pending.totalPending': '总计待办',
      'pending.viewAllPending': '查看全部待办事项',
      'pending.collapse': '收起',
      'common.search': '搜索',
      'common.filter': '筛选',
      'common.status': '状态',
      'common.category': '分类',
      'common.risk': '风险',
      'common.amount': '金额',
      'common.action': '操作',
      'common.noneFound': '未找到符合条件的数据',
      'supplier.total': '供应商总数',
      'supplier.strategic': '战略供应商',
      'supplier.lowRisk': '低风险',
      'supplier.annualSpendYuan': '年度支出(元)',
      'supplier.allCategory': '全部分类',
      'supplier.allRisk': '全部风险',
      'supplier.searchPlaceholder': '搜索供应商名称或编码...',
      'supplier.table.name': '供应商',
      'supplier.table.code': '编码',
      'supplier.table.category': '分类',
      'supplier.table.risk': '风险',
      'supplier.table.annualSpend': '年度支出',
      'supplier.table.orderCount': '订单数',
      'supplier.table.onTimeRate': '准时率',
      'supplier.metric.quality': '质量',
      'supplier.metric.delivery': '交付',
      'supplier.metric.cost': '成本',
      'supplier.metric.innovation': '创新',
      'order.total': '订单总数',
      'order.inProduction': '生产中',
      'order.shipped': '已发货',
      'order.completed': '已完成',
      'order.searchPlaceholder': '搜索订单号或供应商...',
      'order.allStatus': '全部状态',
      'order.table.orderNo': '订单号',
      'order.table.supplier': '供应商',
      'order.table.status': '状态',
      'order.table.orderDate': '订单日期',
      'order.table.deliveryDate': '交货日期',
      'order.table.amount': '金额',
      'order.table.itemCount': '物料数',
      'order.empty': '未找到符合条件的订单',
      'mold.total': '模具总数',
      'mold.active': '在用模具',
      'mold.maintenance': '维保中',
      'mold.alert': '寿命预警',
      'mold.searchPlaceholder': '搜索模具编码或名称...',
      'mold.allStatus': '全部状态',
      'mold.table.code': '模具编码',
      'mold.table.name': '模具名称',
      'mold.table.status': '状态',
      'mold.table.life': '寿命',
      'mold.table.supplier': '供应商',
      'mold.table.nextMaintenance': '下次维保',
      'mold.empty': '未找到符合条件的模具',
      'warehouse.kpi.virtualCount': '虚拟仓数量',
      'warehouse.kpi.inTransitAsn': '在途ASN',
      'warehouse.kpi.pendingReceipt': '待收货',
      'warehouse.kpi.varianceAlert': '差异预警',
      'warehouse.allSupplier': '全部供应商',
      'warehouse.table.materialCode': '物料编码',
      'warehouse.table.materialName': '物料名称',
      'warehouse.table.supplier': '供应商',
      'warehouse.table.theoreticalQty': '理论库存',
      'warehouse.table.actualQty': '实际库存',
      'warehouse.table.varianceRate': '差异率',
      'warehouse.table.updateTime': '更新时间',
      'warehouse.asn.title': 'ASN预发货通知',
      'warehouse.asn.table.asnNo': 'ASN号',
      'warehouse.asn.table.orderNo': '关联订单',
      'warehouse.asn.table.shipDate': '发货日期',
      'warehouse.asn.table.eta': '预计到达',
      'warehouse.asn.table.itemRows': '物料行数',
      'warehouse.recon.title': '物料核销报告',
      'warehouse.recon.table.requiredQty': '应耗数量',
      'warehouse.recon.table.actualQty': '实耗数量',
      'warehouse.recon.table.lossRate': '损耗率',
      'sourcing.kpi.activeProjects': '进行中项目',
      'sourcing.kpi.suppliers': '参与供应商',
      'sourcing.kpi.targetAmount': '目标金额',
      'sourcing.kpi.avgSaving': '平均节约率',
      'sourcing.table.id': '项目编号',
      'sourcing.table.name': '项目名称',
      'sourcing.table.category': '分类',
      'sourcing.table.status': '状态',
      'sourcing.table.targetAmount': '目标金额',
      'sourcing.table.bestPrice': '最优报价',
      'sourcing.table.savingRate': '节约率',
      'sourcing.table.deadline': '截止日期',
      'finance.kpi.monthlyRecon': '本月对账',
      'finance.kpi.unpaid': '待付金额',
      'finance.kpi.paid': '已付金额',
      'finance.kpi.invoiceCount': '发票数量',
      'finance.table.id': '对账单号',
      'finance.table.supplier': '供应商',
      'finance.table.period': '对账期间',
      'finance.table.orderCount': '订单数',
      'finance.table.totalAmount': '对账总额',
      'finance.table.paidAmount': '已付金额',
      'finance.table.unpaidAmount': '待付金额',
      'finance.table.status': '状态',
      // 供应商门户
      'supplier.portal.home': '工作台',
      'supplier.portal.orders': '我的订单',
      'supplier.portal.asn': '发货管理',
      'supplier.portal.inventory': '库存管理',
      'supplier.portal.reconciliation': '财务对账',
      'supplier.portal.invoice': '发票管理',
      'supplier.portal.performance': '绩效报告',
      'supplier.portal.profile': '企业信息',
    },
    'en-US': {
      'system.name': 'Chuidahuang SRM',
      'theme.light': 'Light',
      'theme.dark': 'Dark',
      'lang.zh': 'Chinese',
      'lang.en': 'English',
      'nav.dashboard': 'Dashboard',
      'nav.suppliers': 'Suppliers',
      'nav.orders': 'Orders',
      'nav.molds': 'Molds',
      'nav.warehouse': 'Warehouse',
      'nav.sourcing': 'Sourcing',
      'nav.finance': 'Finance',
      'nav.riskAlerts': 'Risk Alerts',
      'nav.users': 'Users',
      'nav.portal': 'Supplier Portal',
      'page.dashboard.title': 'Dashboard',
      'page.suppliers.title': 'Supplier Management',
      'page.orders.title': 'Order Collaboration',
      'page.molds.title': 'Mold Management',
      'page.warehouse.title': 'Processing Warehouse',
      'page.sourcing.title': 'Strategic Sourcing',
      'page.finance.title': 'Finance Reconciliation',
      'page.riskAlerts.title': 'Risk Alerts',
      'page.users.title': 'User Management',
      'btn.goRegister': 'Go Register',
      'btn.addUser': 'Add User',
      'btn.addSupplier': 'Add Supplier',
      'btn.createOrder': 'Create Order',
      'btn.addMold': 'Add Mold',
      'btn.createSourcingProject': 'Create Sourcing Project',
      'btn.grid': 'Grid',
      'btn.list': 'List',
      'btn.viewDetail': 'View Details',
      'btn.performanceReview': 'Performance Review',
      'btn.confirmOrder': 'Confirm Order',
      'btn.confirmReceipt': 'Confirm Receipt',
      'btn.scheduleMaintenance': 'Schedule Maintenance',
      'btn.createAsn': 'Create ASN',
      'btn.printLabel': 'Print Label',
      'btn.scanReceipt': 'Scan Receipt',
      'btn.generateReconciliation': 'Generate Reconciliation',
      'btn.quoteComparison': 'Quote Comparison',
      'btn.finalizeAward': 'Finalize Award',
      'btn.viewStatementDetail': 'View Details',
      'btn.downloadStatement': 'Download Statement',
      'btn.pushSupplierConfirm': 'Push Supplier Confirm',
      'btn.initiatePayment': 'Initiate Payment',
      'btn.dark': 'Dark',
      'btn.light': 'Light',
      'pending.title': 'Pending Items',
      'pending.total': ' pending items',
      'pending.noItems': 'No pending items',
      'pending.pendingOrders': 'Pending Orders',
      'pending.pendingMolds': 'Pending Molds',
      'pending.pendingReconciliations': 'Pending Reconciliations',
      'pending.totalPending': 'Total Pending',
      'pending.viewAllPending': 'View All Pending Items',
      'pending.collapse': 'Collapse',
      'common.search': 'Search',
      'common.filter': 'Filter',
      'common.status': 'Status',
      'common.category': 'Category',
      'common.risk': 'Risk',
      'common.amount': 'Amount',
      'common.action': 'Action',
      'common.noneFound': 'No data found',
      'supplier.total': 'Total Suppliers',
      'supplier.strategic': 'Strategic Suppliers',
      'supplier.lowRisk': 'Low Risk',
      'supplier.annualSpendYuan': 'Annual Spend (CNY)',
      'supplier.allCategory': 'All Categories',
      'supplier.allRisk': 'All Risks',
      'supplier.searchPlaceholder': 'Search supplier name or code...',
      'supplier.table.name': 'Supplier',
      'supplier.table.code': 'Code',
      'supplier.table.category': 'Category',
      'supplier.table.risk': 'Risk',
      'supplier.table.annualSpend': 'Annual Spend',
      'supplier.table.orderCount': 'Orders',
      'supplier.table.onTimeRate': 'On-time Rate',
      'supplier.metric.quality': 'Quality',
      'supplier.metric.delivery': 'Delivery',
      'supplier.metric.cost': 'Cost',
      'supplier.metric.innovation': 'Innovation',
      'order.total': 'Total Orders',
      'order.inProduction': 'In Production',
      'order.shipped': 'Shipped',
      'order.completed': 'Completed',
      'order.searchPlaceholder': 'Search order no or supplier...',
      'order.allStatus': 'All Status',
      'order.table.orderNo': 'Order No',
      'order.table.supplier': 'Supplier',
      'order.table.status': 'Status',
      'order.table.orderDate': 'Order Date',
      'order.table.deliveryDate': 'Delivery Date',
      'order.table.amount': 'Amount',
      'order.table.itemCount': 'Items',
      'order.empty': 'No matching orders',
      'mold.total': 'Total Molds',
      'mold.active': 'Active Molds',
      'mold.maintenance': 'In Maintenance',
      'mold.alert': 'Life Alert',
      'mold.searchPlaceholder': 'Search mold code or name...',
      'mold.allStatus': 'All Status',
      'mold.table.code': 'Mold Code',
      'mold.table.name': 'Mold Name',
      'mold.table.status': 'Status',
      'mold.table.life': 'Life',
      'mold.table.supplier': 'Supplier',
      'mold.table.nextMaintenance': 'Next Maintenance',
      'mold.empty': 'No matching molds',
      'warehouse.kpi.virtualCount': 'Virtual Warehouses',
      'warehouse.kpi.inTransitAsn': 'In-transit ASN',
      'warehouse.kpi.pendingReceipt': 'Pending Receipt',
      'warehouse.kpi.varianceAlert': 'Variance Alerts',
      'warehouse.allSupplier': 'All Suppliers',
      'warehouse.table.materialCode': 'Material Code',
      'warehouse.table.materialName': 'Material Name',
      'warehouse.table.supplier': 'Supplier',
      'warehouse.table.theoreticalQty': 'Theoretical Qty',
      'warehouse.table.actualQty': 'Actual Qty',
      'warehouse.table.varianceRate': 'Variance Rate',
      'warehouse.table.updateTime': 'Updated At',
      'warehouse.asn.title': 'ASN Notice',
      'warehouse.asn.table.asnNo': 'ASN No',
      'warehouse.asn.table.orderNo': 'Order No',
      'warehouse.asn.table.shipDate': 'Ship Date',
      'warehouse.asn.table.eta': 'ETA',
      'warehouse.asn.table.itemRows': 'Item Rows',
      'warehouse.recon.title': 'Material Reconciliation',
      'warehouse.recon.table.requiredQty': 'Required Qty',
      'warehouse.recon.table.actualQty': 'Actual Qty',
      'warehouse.recon.table.lossRate': 'Loss Rate',
      'sourcing.kpi.activeProjects': 'Active Projects',
      'sourcing.kpi.suppliers': 'Participating Suppliers',
      'sourcing.kpi.targetAmount': 'Target Amount',
      'sourcing.kpi.avgSaving': 'Avg Saving Rate',
      'sourcing.table.id': 'Project ID',
      'sourcing.table.name': 'Project Name',
      'sourcing.table.category': 'Category',
      'sourcing.table.status': 'Status',
      'sourcing.table.targetAmount': 'Target Amount',
      'sourcing.table.bestPrice': 'Best Price',
      'sourcing.table.savingRate': 'Saving Rate',
      'sourcing.table.deadline': 'Deadline',
      'finance.kpi.monthlyRecon': 'Monthly Recon',
      'finance.kpi.unpaid': 'Unpaid Amount',
      'finance.kpi.paid': 'Paid Amount',
      'finance.kpi.invoiceCount': 'Invoice Count',
      'finance.table.id': 'Statement ID',
      'finance.table.supplier': 'Supplier',
      'finance.table.period': 'Period',
      'finance.table.orderCount': 'Orders',
      'finance.table.totalAmount': 'Total Amount',
      'finance.table.paidAmount': 'Paid Amount',
      'finance.table.unpaidAmount': 'Unpaid Amount',
      'finance.table.status': 'Status',
      // 供应商门户
      'supplier.portal.home': 'Dashboard',
      'supplier.portal.orders': 'My Orders',
      'supplier.portal.asn': 'Shipment',
      'supplier.portal.reconciliation': 'Reconciliation',
      'supplier.portal.invoice': 'Invoice',
      'supplier.portal.performance': 'Performance',
      'supplier.portal.profile': 'Company Info',
    },
  }

  const currentUser = ref<CurrentUser>({
    id: 'U001',
    name: '张三',
    role: '采购员',
    department: '采购部',
    avatar: '张',
  })

  const sidebarOpen = ref(false)

  const toasts = ref<Toast[]>([])

  const isLoggedIn = ref(false)

  const token = ref<string | null>(null)

  function applyTheme(nextTheme: Theme) {
    theme.value = nextTheme
    const root = document.documentElement
    if (nextTheme === 'dark') {
      root.classList.add('dark')
    } else {
      root.classList.remove('dark')
    }
    localStorage.setItem('app-theme', nextTheme)
  }

  function initializePreferences() {
    const savedLanguage = localStorage.getItem('app-language') as Language | null
    const savedTheme = localStorage.getItem('app-theme') as Theme | null
    if (savedLanguage === 'zh-CN' || savedLanguage === 'en-US') {
      language.value = savedLanguage
    }
    if (savedTheme === 'light' || savedTheme === 'dark') {
      applyTheme(savedTheme)
    } else {
      applyTheme('light')
    }
  }

  function setSidebarOpen(open: boolean) {
    sidebarOpen.value = open
  }

  function toggleSidebar() {
    sidebarOpen.value = !sidebarOpen.value
  }

  function login(user: CurrentUser) {
    currentUser.value = user
    isLoggedIn.value = true
  }

  function logout() {
    isLoggedIn.value = false
    token.value = null
    localStorage.removeItem('auth-token')
    localStorage.removeItem('user-info')
    currentUser.value = {
      id: '',
      name: '',
      role: '',
      department: '',
      avatar: '',
    }
  }

  function initializeAuth() {
    const savedToken = localStorage.getItem('auth-token')
    const savedUserInfo = localStorage.getItem('user-info')

    if (savedToken && savedUserInfo) {
      try {
        const userInfo = JSON.parse(savedUserInfo)
        currentUser.value = {
          id: String(userInfo.userId),
          name: userInfo.name,
          role: userInfo.role,
          department: userInfo.department,
          avatar: userInfo.avatar || userInfo.name?.charAt(0) || '用',
        }
        token.value = savedToken
        isLoggedIn.value = true
      } catch (e) {
        // Token或用户信息无效，清除
        logout()
      }
    }
  }

  function setLanguage(nextLanguage: Language) {
    language.value = nextLanguage
    localStorage.setItem('app-language', nextLanguage)
  }

  function toggleLanguage() {
    setLanguage(language.value === 'zh-CN' ? 'en-US' : 'zh-CN')
  }

  function setTheme(nextTheme: Theme) {
    applyTheme(nextTheme)
  }

  function toggleTheme() {
    applyTheme(theme.value === 'light' ? 'dark' : 'light')
  }

  function t(key: string): string {
    return messages[language.value][key] || key
  }

  function showToast(message: string, type: 'success' | 'error' | 'warning' | 'info' = 'info') {
    const id = Date.now().toString()
    toasts.value.push({ id, message, type })
    setTimeout(() => {
      toasts.value = toasts.value.filter(toast => toast.id !== id)
    }, 3000)
  }

  function removeToast(id: string) {
    toasts.value = toasts.value.filter(toast => toast.id !== id)
  }

  return {
    currentUser,
    language,
    theme,
    sidebarOpen,
    toasts,
    isLoggedIn,
    token,
    t,
    setSidebarOpen,
    toggleSidebar,
    login,
    logout,
    initializeAuth,
    setLanguage,
    toggleLanguage,
    setTheme,
    toggleTheme,
    initializePreferences,
    showToast,
    removeToast,
  }
})
