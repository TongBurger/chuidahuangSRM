<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Package, FileText, DollarSign, TrendingUp, Clock, CheckCircle, X, Truck, AlertCircle, Warehouse } from 'lucide-vue-next'
import { useAppStore } from '@/stores/useAppStore'

const router = useRouter()
const appStore = useAppStore()

// 当前供应商ID（从登录用户信息获取）
const supplierId = 'S001'

const supplierInfo = ref({
  name: '深圳市精诚模具制造有限公司',
  code: 'SUP001',
  contactPerson: '张伟',
  overallScore: 91.25,
  level: '战略供应商',
})

// Mock 订单数据 - 扩展到10条
const mockOrders = [
  {
    id: 'PO001',
    orderNo: 'PO2026031901',
    supplierId: 'S001',
    orderDate: '2026-03-19',
    deliveryDate: '2026-04-15',
    status: '待确认',
    totalAmount: 285000,
    items: [
      { materialCode: 'MAT001', materialName: '电机外壳', quantity: 500, unit: '个', price: 250, amount: 125000 },
      { materialCode: 'MAT002', materialName: '端盖组件', quantity: 1000, unit: '件', price: 160, amount: 160000 },
    ]
  },
  {
    id: 'PO002',
    orderNo: 'PO2026031801',
    supplierId: 'S001',
    orderDate: '2026-03-18',
    deliveryDate: '2026-04-10',
    status: '生产中',
    totalAmount: 456000,
    items: [
      { materialCode: 'MAT003', materialName: '散热片', quantity: 2000, unit: '片', price: 40, amount: 80000 },
      { materialCode: 'MAT004', materialName: '外壳组件', quantity: 1000, unit: '套', price: 376, amount: 376000 },
    ]
  },
  {
    id: 'PO003',
    orderNo: 'PO2026031501',
    supplierId: 'S001',
    orderDate: '2026-03-15',
    deliveryDate: '2026-04-01',
    status: '生产中',
    totalAmount: 328000,
    items: [
      { materialCode: 'MAT005', materialName: '连接器外壳', quantity: 800, unit: '个', price: 410, amount: 328000 },
    ]
  },
  {
    id: 'PO004',
    orderNo: 'PO2026031001',
    supplierId: 'S001',
    orderDate: '2026-03-10',
    deliveryDate: '2026-03-25',
    status: '已发货',
    totalAmount: 215000,
    items: [
      { materialCode: 'MAT006', materialName: '底座组件', quantity: 500, unit: '个', price: 430, amount: 215000 },
    ]
  },
  {
    id: 'PO005',
    orderNo: 'PO2026030501',
    supplierId: 'S001',
    orderDate: '2026-03-05',
    deliveryDate: '2026-03-18',
    status: '已完成',
    totalAmount: 392000,
    items: [
      { materialCode: 'MAT007', materialName: '控制面板外壳', quantity: 700, unit: '个', price: 560, amount: 392000 },
    ]
  },
  {
    id: 'PO006',
    orderNo: 'PO2026030201',
    supplierId: 'S001',
    orderDate: '2026-03-02',
    deliveryDate: '2026-03-15',
    status: '已完成',
    totalAmount: 178000,
    items: [
      { materialCode: 'MAT008', materialName: '散热器支架', quantity: 1200, unit: '个', price: 148.33, amount: 178000 },
    ]
  },
  {
    id: 'PO007',
    orderNo: 'PO2026022801',
    supplierId: 'S001',
    orderDate: '2026-02-28',
    deliveryDate: '2026-03-10',
    status: '已完成',
    totalAmount: 524000,
    items: [
      { materialCode: 'MAT009', materialName: '外壳总成', quantity: 400, unit: '套', price: 1310, amount: 524000 },
    ]
  },
  {
    id: 'PO008',
    orderNo: 'PO2026022501',
    supplierId: 'S001',
    orderDate: '2026-02-25',
    deliveryDate: '2026-03-08',
    status: '已完成',
    totalAmount: 298000,
    items: [
      { materialCode: 'MAT010', materialName: '电机外壳A型', quantity: 600, unit: '个', price: 496.67, amount: 298000 },
    ]
  },
  {
    id: 'PO009',
    orderNo: 'PO2026022001',
    supplierId: 'S001',
    orderDate: '2026-02-20',
    deliveryDate: '2026-03-05',
    status: '已完成',
    totalAmount: 412000,
    items: [
      { materialCode: 'MAT011', materialName: '端盖组件B型', quantity: 1500, unit: '件', price: 274.67, amount: 412000 },
    ]
  },
  {
    id: 'PO010',
    orderNo: 'PO2026021501',
    supplierId: 'S001',
    orderDate: '2026-02-15',
    deliveryDate: '2026-02-28',
    status: '已完成',
    totalAmount: 335000,
    items: [
      { materialCode: 'MAT012', materialName: '散热片组件', quantity: 2500, unit: '片', price: 134, amount: 335000 },
    ]
  },
]

const myOrders = ref<any[]>([])

// Mock 对账单数据 - 扩展到10条
const pendingReconciliations = ref([
  { id: 'RC001', period: '2026年3月', amount: 857000, orderCount: 5, status: '待确认' },
  { id: 'RC002', period: '2026年2月', amount: 1256000, orderCount: 12, status: '已结清' },
  { id: 'RC003', period: '2026年1月', amount: 980000, orderCount: 8, status: '已结清' },
  { id: 'RC004', period: '2025年12月', amount: 1580000, orderCount: 15, status: '已结清' },
  { id: 'RC005', period: '2025年11月', amount: 1120000, orderCount: 10, status: '部分支付' },
  { id: 'RC006', period: '2025年10月', amount: 965000, orderCount: 9, status: '已结清' },
  { id: 'RC007', period: '2025年9月', amount: 1340000, orderCount: 11, status: '已结清' },
  { id: 'RC008', period: '2025年8月', amount: 785000, orderCount: 7, status: '已结清' },
  { id: 'RC009', period: '2025年7月', amount: 1420000, orderCount: 13, status: '已结清' },
  { id: 'RC010', period: '2025年6月', amount: 890000, orderCount: 8, status: '已结清' },
])

const scoreItems = [
  { label: '质量评分', score: 95, color: 'bg-blue-600' },
  { label: '交付评分', score: 92, color: 'bg-orange-600' },
  { label: '成本评分', score: 88, color: 'bg-purple-600' },
  { label: '创新评分', score: 90, color: 'bg-green-600' },
]

// 订单详情对话框
const selectedOrder = ref<any>(null)
const isOrderDetailOpen = ref(false)

// 加载订单数据
onMounted(() => {
  const saved = localStorage.getItem('orders')
  if (saved) {
    try {
      const allOrders = JSON.parse(saved)
      myOrders.value = allOrders.filter((o: any) => o.supplierId === supplierId)
    } catch {
      myOrders.value = mockOrders
    }
  } else {
    myOrders.value = mockOrders
  }
})

// 待确认订单
const pendingOrders = computed(() => myOrders.value.filter((o) => o.status === '待确认'))

// 待处理对账单
const pendingReconciliationsList = computed(() => pendingReconciliations.value.filter(r => r.status === '待确认'))

// 快捷功能操作
function handleQuickAction(action: string) {
  switch (action) {
    case '创建ASN':
      router.push('/supplier-portal/asn')
      break
    case '上传发票':
      router.push('/supplier-portal/invoice')
      break
    case '查看对账单':
      router.push('/supplier-portal/reconciliation')
      break
    case '绩效报告':
      router.push('/supplier-portal/performance')
      break
    case '库存管理':
      router.push('/supplier-portal/inventory')
      break
  }
}

// 确认订单
function handleConfirmOrder(order: any) {
  const saved = localStorage.getItem('orders')
  if (saved) {
    const allOrders = JSON.parse(saved)
    const idx = allOrders.findIndex((o: any) => o.id === order.id)
    if (idx > -1) {
      allOrders[idx].status = '生产中'
      localStorage.setItem('orders', JSON.stringify(allOrders))
      // 更新本地数据
      const localIdx = myOrders.value.findIndex(o => o.id === order.id)
      if (localIdx > -1) {
        myOrders.value[localIdx].status = '生产中'
      }
      appStore.showToast('订单已确认', 'success')
    }
  } else {
    // 如果没有localStorage数据，直接更新本地
    const localIdx = myOrders.value.findIndex(o => o.id === order.id)
    if (localIdx > -1) {
      myOrders.value[localIdx].status = '生产中'
      appStore.showToast('订单已确认', 'success')
    }
  }
}

// 创建发货单
function handleCreateASN(order: any) {
  // 跳转到ASN页面，并传递订单ID
  router.push({ path: '/supplier-portal/asn', query: { orderId: order.id } })
}

// 查看订单详情
function handleViewOrderDetail(order: any) {
  selectedOrder.value = order
  isOrderDetailOpen.value = true
}

// 关闭详情对话框
function closeOrderDetail() {
  isOrderDetailOpen.value = false
  selectedOrder.value = null
}

// 跳转到订单页面
function goToOrders() {
  router.push('/supplier-portal/orders')
}

// 跳转到对账单页面
function goToReconciliation() {
  router.push('/supplier-portal/reconciliation')
}
</script>

<template>
  <div class="space-y-6">
    <!-- 欢迎信息 -->
    <div class="bg-gradient-to-r from-blue-600 to-blue-800 rounded-lg p-6 text-white">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-semibold mb-2">欢迎回来，{{ supplierInfo.contactPerson }}</h1>
          <p class="text-blue-100">{{ supplierInfo.name }}</p>
          <div class="flex items-center gap-4 mt-3">
            <span class="px-3 py-1 bg-white text-blue-600 rounded-full text-xs font-medium">{{ supplierInfo.level }}</span>
            <span class="text-sm">供应商编码: {{ supplierInfo.code }}</span>
          </div>
        </div>
        <div class="text-center">
          <div class="text-4xl font-bold mb-2">{{ supplierInfo.overallScore }}</div>
          <div class="text-sm text-blue-100">综合评分</div>
        </div>
      </div>
    </div>

    <!-- 快速统计 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <div class="bg-white rounded-lg border border-gray-200 p-6 cursor-pointer hover:shadow-md transition-shadow" @click="goToOrders">
        <div class="flex items-center justify-between"><div><p class="text-sm text-gray-500">待确认订单</p><p class="text-2xl font-semibold text-gray-900 mt-1">{{ pendingOrders.length }}</p></div><div class="w-12 h-12 bg-orange-100 rounded-lg flex items-center justify-center"><Clock class="w-6 h-6 text-orange-600" /></div></div>
      </div>
      <div class="bg-white rounded-lg border border-gray-200 p-6 cursor-pointer hover:shadow-md transition-shadow" @click="goToOrders">
        <div class="flex items-center justify-between"><div><p class="text-sm text-gray-500">生产中订单</p><p class="text-2xl font-semibold text-gray-900 mt-1">{{ myOrders.filter((o) => o.status === '生产中').length }}</p></div><div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center"><Package class="w-6 h-6 text-blue-600" /></div></div>
      </div>
      <div class="bg-white rounded-lg border border-gray-200 p-6 cursor-pointer hover:shadow-md transition-shadow" @click="goToReconciliation">
        <div class="flex items-center justify-between"><div><p class="text-sm text-gray-500">待对账金额</p><p class="text-2xl font-semibold text-gray-900 mt-1">¥{{ (pendingReconciliationsList.reduce((sum, r) => sum + r.amount, 0) / 10000).toFixed(1) }}万</p></div><div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center"><DollarSign class="w-6 h-6 text-green-600" /></div></div>
      </div>
      <div class="bg-white rounded-lg border border-gray-200 p-6 cursor-pointer hover:shadow-md transition-shadow" @click="goToOrders">
        <div class="flex items-center justify-between"><div><p class="text-sm text-gray-500">本月订单</p><p class="text-2xl font-semibold text-gray-900 mt-1">{{ myOrders.length }}</p></div><div class="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center"><FileText class="w-6 h-6 text-purple-600" /></div></div>
      </div>
    </div>

    <!-- 待办事项 -->
    <div class="bg-white rounded-lg border border-gray-200 p-6">
      <h3 class="text-base font-semibold text-gray-900 flex items-center gap-2 mb-4">
        <AlertCircle class="w-5 h-5" />待办事项
      </h3>
      <div class="space-y-3">
        <!-- 待确认订单 -->
        <div v-for="order in pendingOrders.slice(0, 3)" :key="order.id" class="flex items-center justify-between p-4 bg-orange-50 rounded-lg">
          <div class="flex items-center gap-4">
            <div class="w-10 h-10 bg-orange-200 rounded-full flex items-center justify-center">
              <FileText class="w-5 h-5 text-orange-700" />
            </div>
            <div>
              <p class="font-medium text-gray-900">订单待确认</p>
              <p class="text-sm text-gray-600">订单号: {{ order.orderNo || order.id }} · 金额: ¥{{ order.totalAmount?.toLocaleString() || order.amount?.toLocaleString() }}</p>
            </div>
          </div>
          <button @click="handleConfirmOrder(order)" class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">立即确认</button>
        </div>
        <!-- 待确认对账单 -->
        <div v-for="recon in pendingReconciliationsList.slice(0, 3)" :key="recon.id" class="flex items-center justify-between p-4 bg-blue-50 rounded-lg">
          <div class="flex items-center gap-4">
            <div class="w-10 h-10 bg-blue-200 rounded-full flex items-center justify-center">
              <DollarSign class="w-5 h-5 text-blue-700" />
            </div>
            <div>
              <p class="font-medium text-gray-900">对账单待确认</p>
              <p class="text-sm text-gray-600">{{ recon.period }} · {{ recon.orderCount }}笔订单 · ¥{{ recon.amount.toLocaleString() }}</p>
            </div>
          </div>
          <button @click="goToReconciliation" class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">查看对账单</button>
        </div>
        <!-- 生产中订单提醒 -->
        <div v-if="myOrders.filter((o) => o.status === '生产中').length > 0" class="flex items-center justify-between p-4 bg-purple-50 rounded-lg">
          <div class="flex items-center gap-4">
            <div class="w-10 h-10 bg-purple-200 rounded-full flex items-center justify-center">
              <Package class="w-5 h-5 text-purple-700" />
            </div>
            <div>
              <p class="font-medium text-gray-900">生产中订单提醒</p>
              <p class="text-sm text-gray-600">您有 {{ myOrders.filter((o) => o.status === '生产中').length }} 个订单正在生产中，请按时交付</p>
            </div>
          </div>
          <button @click="goToOrders" class="px-4 py-2 border border-purple-300 text-purple-700 rounded-lg text-sm hover:bg-purple-100">查看详情</button>
        </div>
        <!-- 空状态 -->
        <div v-if="pendingOrders.length === 0 && pendingReconciliationsList.length === 0 && myOrders.filter((o) => o.status === '生产中').length === 0" class="text-center py-8 text-gray-500">
          <CheckCircle class="w-12 h-12 mx-auto mb-2 opacity-50" />
          <p>暂无待办事项</p>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- 我的订单 -->
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-base font-semibold text-gray-900">我的订单</h3>
          <button @click="goToOrders" class="px-3 py-1.5 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">查看全部</button>
        </div>
        <div class="space-y-3">
          <div v-for="order in myOrders.slice(0, 4)" :key="order.id" class="border border-gray-200 rounded-lg p-4">
            <div class="flex items-center justify-between mb-3">
              <div class="flex items-center gap-2">
                <span class="font-medium text-gray-900">{{ order.orderNo || order.id }}</span>
                <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="order.status === '生产中' ? 'bg-blue-100 text-blue-800' : order.status === '待确认' ? 'bg-orange-100 text-orange-800' : order.status === '已发货' ? 'bg-purple-100 text-purple-800' : 'bg-gray-100 text-gray-700'">{{ order.status }}</span>
              </div>
              <span class="font-semibold text-gray-900">¥{{ (order.totalAmount || order.amount || 0).toLocaleString() }}</span>
            </div>
            <div class="grid grid-cols-2 gap-4 text-sm text-gray-600">
              <div><span class="text-gray-500">订单日期:</span> {{ order.orderDate }}</div>
              <div><span class="text-gray-500">交货日期:</span> {{ order.deliveryDate }}</div>
            </div>
            <div class="mt-3 pt-3 border-t flex gap-2">
              <button @click="handleViewOrderDetail(order)" class="flex-1 px-3 py-1.5 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">查看详情</button>
              <button v-if="order.status === '待确认'" @click="handleConfirmOrder(order)" class="flex-1 px-3 py-1.5 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">确认订单</button>
              <button v-if="order.status === '生产中'" @click="handleCreateASN(order)" class="flex-1 px-3 py-1.5 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">创建发货单</button>
            </div>
          </div>
          <div v-if="myOrders.length === 0" class="text-center py-8 text-gray-500">
            <Package class="w-12 h-12 mx-auto mb-2 opacity-50" />
            <p>暂无订单</p>
          </div>
        </div>
      </div>

      <!-- 绩效评分 -->
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <h3 class="text-base font-semibold text-gray-900 flex items-center gap-2 mb-4">
          <TrendingUp class="w-5 h-5" />绩效评分
        </h3>
        <div class="text-center py-4">
          <div class="inline-flex items-center justify-center w-24 h-24 rounded-full bg-gradient-to-br from-blue-600 to-blue-800 mb-2">
            <span class="text-3xl font-bold text-white">{{ supplierInfo.overallScore }}</span>
          </div>
          <p class="text-sm text-gray-500">综合得分</p>
          <span class="inline-block mt-2 px-2 py-0.5 bg-blue-600 text-white rounded-full text-xs font-medium">{{ supplierInfo.level }}</span>
        </div>
        <div class="space-y-3">
          <div v-for="item in scoreItems" :key="item.label">
            <div class="flex items-center justify-between text-sm mb-2">
              <span class="text-gray-600">{{ item.label }}</span>
              <span class="font-medium text-gray-900">{{ item.score }}</span>
            </div>
            <div class="h-2 bg-gray-200 rounded-full overflow-hidden">
              <div class="h-full transition-all" :class="item.color" :style="{ width: `${item.score}%` }" />
            </div>
          </div>
        </div>
        <div class="mt-4 p-3 bg-green-50 rounded-lg">
          <p class="text-sm text-green-800"><strong>优势：</strong>质量管理体系完善，产品合格率高</p>
        </div>
        <div class="p-3 bg-orange-50 rounded-lg mt-2">
          <p class="text-sm text-orange-800"><strong>建议：</strong>可以进一步优化成本控制，提升价格竞争力</p>
        </div>
      </div>
    </div>

    <!-- 快捷功能 -->
    <div class="bg-white rounded-lg border border-gray-200 p-6">
      <h3 class="text-base font-semibold text-gray-900 mb-4">快捷功能</h3>
      <div class="grid grid-cols-2 md:grid-cols-5 gap-4">
        <button v-for="item in [{ icon: Package, label: '创建ASN', action: '创建ASN' }, { icon: FileText, label: '上传发票', action: '上传发票' }, { icon: DollarSign, label: '查看对账单', action: '查看对账单' }, { icon: TrendingUp, label: '绩效报告', action: '绩效报告' }, { icon: Warehouse, label: '库存管理', action: '库存管理' }]" :key="item.label" @click="handleQuickAction(item.action)" class="h-20 flex flex-col items-center justify-center gap-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-blue-50 hover:border-blue-300 hover:text-blue-600 transition-colors">
          <component :is="item.icon" class="w-6 h-6" />
          <span>{{ item.label }}</span>
        </button>
      </div>
    </div>

    <!-- 订单详情对话框 -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="isOrderDetailOpen && selectedOrder" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-black/50" @click="closeOrderDetail" />
          <div class="relative bg-white rounded-xl shadow-xl w-full max-w-2xl max-h-[80vh] overflow-hidden">
            <div class="flex items-center justify-between p-4 border-b">
              <h3 class="text-lg font-semibold text-gray-900">订单详情</h3>
              <button @click="closeOrderDetail" class="p-2 hover:bg-gray-100 rounded-lg">
                <X class="w-5 h-5" />
              </button>
            </div>
            <div class="p-6 overflow-y-auto max-h-[60vh]">
              <div class="space-y-4">
                <div class="grid grid-cols-2 gap-4">
                  <div>
                    <p class="text-sm text-gray-500">订单号</p>
                    <p class="font-medium text-gray-900">{{ selectedOrder.orderNo || selectedOrder.id }}</p>
                  </div>
                  <div>
                    <p class="text-sm text-gray-500">订单状态</p>
                    <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="selectedOrder.status === '生产中' ? 'bg-blue-100 text-blue-800' : selectedOrder.status === '待确认' ? 'bg-orange-100 text-orange-800' : selectedOrder.status === '已发货' ? 'bg-purple-100 text-purple-800' : 'bg-gray-100 text-gray-700'">{{ selectedOrder.status }}</span>
                  </div>
                  <div>
                    <p class="text-sm text-gray-500">订单日期</p>
                    <p class="font-medium text-gray-900">{{ selectedOrder.orderDate }}</p>
                  </div>
                  <div>
                    <p class="text-sm text-gray-500">交货日期</p>
                    <p class="font-medium text-gray-900">{{ selectedOrder.deliveryDate }}</p>
                  </div>
                </div>

                <div v-if="selectedOrder.items && selectedOrder.items.length > 0">
                  <p class="text-sm font-medium text-gray-700 mb-2">物料明细</p>
                  <div class="border border-gray-200 rounded-lg overflow-hidden">
                    <table class="w-full text-sm">
                      <thead class="bg-gray-50">
                        <tr>
                          <th class="text-left px-4 py-2 font-medium text-gray-700">物料编码</th>
                          <th class="text-left px-4 py-2 font-medium text-gray-700">物料名称</th>
                          <th class="text-right px-4 py-2 font-medium text-gray-700">数量</th>
                          <th class="text-right px-4 py-2 font-medium text-gray-700">单价</th>
                          <th class="text-right px-4 py-2 font-medium text-gray-700">金额</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="(item, idx) in selectedOrder.items" :key="idx" class="border-t border-gray-200">
                          <td class="px-4 py-3 text-gray-900">{{ item.materialCode }}</td>
                          <td class="px-4 py-3 text-gray-900">{{ item.materialName }}</td>
                          <td class="px-4 py-3 text-right text-gray-900">{{ item.quantity }} {{ item.unit }}</td>
                          <td class="px-4 py-3 text-right text-gray-900">¥{{ item.price?.toLocaleString() }}</td>
                          <td class="px-4 py-3 text-right font-medium text-gray-900">¥{{ item.amount?.toLocaleString() }}</td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                </div>

                <div class="flex justify-between items-center pt-4 border-t">
                  <span class="text-gray-600">订单总额</span>
                  <span class="text-2xl font-bold text-gray-900">¥{{ (selectedOrder.totalAmount || selectedOrder.amount || 0).toLocaleString() }}</span>
                </div>
              </div>
            </div>
            <div class="flex justify-end gap-3 p-4 border-t bg-gray-50">
              <button @click="closeOrderDetail" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">关闭</button>
              <button v-if="selectedOrder.status === '待确认'" @click="handleConfirmOrder(selectedOrder); closeOrderDetail()" class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">确认订单</button>
              <button v-if="selectedOrder.status === '生产中'" @click="handleCreateASN(selectedOrder); closeOrderDetail()" class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700 flex items-center gap-2">
                <Truck class="w-4 h-4" />创建发货单
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<style scoped>
.modal-enter-active, .modal-leave-active {
  transition: opacity 0.2s ease;
}
.modal-enter-from, .modal-leave-to {
  opacity: 0;
}
</style>
