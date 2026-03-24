<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Plus, CheckCircle, Clock, Package, Edit, X, Truck } from 'lucide-vue-next'
import { mockOrders } from '@/data/mockData'
import { useAppStore } from '@/stores/useAppStore'

const router = useRouter()
const appStore = useAppStore()
const searchQuery = ref('')
const statusFilter = ref('all')

// 模拟当前供应商的订单（假设是 S001）
const supplierId = 'S001'

// Mock 订单数据
const mockSupplierOrders = [
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
]

const orders = ref<any[]>([])

// 订单详情对话框
const selectedOrder = ref<any>(null)
const isOrderDetailOpen = ref(false)

onMounted(() => {
  const saved = localStorage.getItem('orders')
  if (saved) {
    try {
      const allOrders = JSON.parse(saved)
      orders.value = allOrders.filter((o: any) => o.supplierId === supplierId)
    } catch {
      orders.value = mockSupplierOrders
    }
  } else {
    orders.value = mockSupplierOrders
  }
})

const filteredOrders = computed(() =>
  orders.value.filter((order) => {
    const matchesSearch =
      order.orderNo.toLowerCase().includes(searchQuery.value.toLowerCase())
    const matchesStatus = statusFilter.value === 'all' || order.status === statusFilter.value
    return matchesSearch && matchesStatus
  })
)

function getStatusClass(status: string) {
  switch (status) {
    case '待确认': return 'bg-orange-100 text-orange-800'
    case '生产中': return 'bg-blue-100 text-blue-800'
    case '已发货': return 'bg-purple-100 text-purple-800'
    case '已收货': return 'bg-green-100 text-green-800'
    case '已完成': return 'bg-gray-100 text-gray-800'
    default: return 'bg-gray-100 text-gray-700'
  }
}

function handleConfirmOrder(orderId: string) {
  const order = orders.value.find(o => o.id === orderId)
  if (order) {
    order.status = '生产中'
    // 更新 localStorage
    const saved = localStorage.getItem('orders')
    if (saved) {
      const allOrders = JSON.parse(saved)
      const idx = allOrders.findIndex((o: any) => o.id === orderId)
      if (idx > -1) {
        allOrders[idx].status = '生产中'
        localStorage.setItem('orders', JSON.stringify(allOrders))
      }
    }
    appStore.showToast('订单已确认', 'success')
  }
}

// 查看订单详情
function handleViewDetail(order: any) {
  selectedOrder.value = order
  isOrderDetailOpen.value = true
}

// 关闭详情对话框
function closeOrderDetail() {
  isOrderDetailOpen.value = false
  selectedOrder.value = null
}

// 创建发货单 - 跳转到ASN页面
function handleCreateASN(order: any) {
  router.push({ path: '/supplier-portal/asn', query: { orderId: order.id } })
}
</script>

<template>
  <div class="space-y-6">
    <!-- 页面标题 -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-semibold text-gray-900">我的订单</h1>
        <p class="text-sm text-gray-500 mt-1">查看和管理收到的采购订单</p>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">待确认</p>
            <p class="text-2xl font-semibold text-orange-600 mt-1">{{ orders.filter((o) => o.status === '待确认').length }}</p>
          </div>
          <div class="w-12 h-12 bg-orange-100 rounded-lg flex items-center justify-center">
            <Clock class="w-6 h-6 text-orange-600" />
          </div>
        </div>
      </div>
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">生产中</p>
            <p class="text-2xl font-semibold text-blue-600 mt-1">{{ orders.filter((o) => o.status === '生产中').length }}</p>
          </div>
          <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
            <Package class="w-6 h-6 text-blue-600" />
          </div>
        </div>
      </div>
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">已发货</p>
            <p class="text-2xl font-semibold text-purple-600 mt-1">{{ orders.filter((o) => o.status === '已发货').length }}</p>
          </div>
          <div class="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center">
            <Edit class="w-6 h-6 text-purple-600" />
          </div>
        </div>
      </div>
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">已完成</p>
            <p class="text-2xl font-semibold text-green-600 mt-1">{{ orders.filter((o) => o.status === '已完成').length }}</p>
          </div>
          <div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
            <CheckCircle class="w-6 h-6 text-green-600" />
          </div>
        </div>
      </div>
    </div>

    <!-- 搜索筛选 -->
    <div class="bg-white rounded-lg border border-gray-200 p-6">
      <div class="flex flex-col md:flex-row gap-4">
        <div class="flex-1 relative">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input v-model="searchQuery" placeholder="搜索订单号..." class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
        </div>
        <select v-model="statusFilter" class="w-full md:w-40 px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
          <option value="all">全部状态</option>
          <option value="待确认">待确认</option>
          <option value="生产中">生产中</option>
          <option value="已发货">已发货</option>
          <option value="已完成">已完成</option>
        </select>
      </div>
    </div>

    <!-- 订单列表 -->
    <div class="space-y-4">
      <div v-for="order in filteredOrders" :key="order.id" class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex items-start justify-between mb-4">
          <div class="flex-1">
            <div class="flex items-center gap-3 mb-2">
              <h3 class="font-semibold text-gray-900">{{ order.orderNo }}</h3>
              <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="getStatusClass(order.status)">{{ order.status }}</span>
            </div>
            <p class="text-sm text-gray-500">订单日期: {{ order.orderDate }}</p>
            <p class="text-sm text-gray-500">交货日期: {{ order.deliveryDate }}</p>
          </div>
          <div class="text-right">
            <p class="text-2xl font-semibold text-gray-900">¥{{ order.totalAmount.toLocaleString() }}</p>
          </div>
        </div>

        <!-- 物料明细 -->
        <div class="border border-gray-200 rounded-lg overflow-hidden mb-4">
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
              <tr v-for="(item, idx) in order.items" :key="idx" class="border-t border-gray-200">
                <td class="px-4 py-3 text-gray-900">{{ item.materialCode }}</td>
                <td class="px-4 py-3 text-gray-900">{{ item.materialName }}</td>
                <td class="px-4 py-3 text-right text-gray-900">{{ item.quantity }} {{ item.unit }}</td>
                <td class="px-4 py-3 text-right text-gray-900">¥{{ item.price.toLocaleString() }}</td>
                <td class="px-4 py-3 text-right font-medium text-gray-900">¥{{ item.amount.toLocaleString() }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 操作按钮 -->
        <div class="flex gap-2">
          <button @click="handleViewDetail(order)" class="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">查看详情</button>
          <button v-if="order.status === '待确认'" @click="handleConfirmOrder(order.id)" class="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">确认订单</button>
          <button v-if="order.status === '生产中'" @click="handleCreateASN(order)" class="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700 flex items-center justify-center gap-2">
            <Truck class="w-4 h-4" />创建发货单
          </button>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="filteredOrders.length === 0" class="bg-white rounded-lg border border-gray-200 py-12">
        <div class="text-center text-gray-500">
          <Package class="w-12 h-12 mx-auto mb-4 opacity-50" />
          <p>暂无订单</p>
        </div>
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
                    <p class="font-medium text-gray-900">{{ selectedOrder.orderNo }}</p>
                  </div>
                  <div>
                    <p class="text-sm text-gray-500">订单状态</p>
                    <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="getStatusClass(selectedOrder.status)">{{ selectedOrder.status }}</span>
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

                <div>
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
                          <td class="px-4 py-3 text-right text-gray-900">¥{{ item.price.toLocaleString() }}</td>
                          <td class="px-4 py-3 text-right font-medium text-gray-900">¥{{ item.amount.toLocaleString() }}</td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                </div>

                <div class="flex justify-between items-center pt-4 border-t">
                  <span class="text-gray-600">订单总额</span>
                  <span class="text-2xl font-bold text-gray-900">¥{{ selectedOrder.totalAmount.toLocaleString() }}</span>
                </div>
              </div>
            </div>
            <div class="flex justify-end gap-3 p-4 border-t bg-gray-50">
              <button @click="closeOrderDetail" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">关闭</button>
              <button v-if="selectedOrder.status === '待确认'" @click="handleConfirmOrder(selectedOrder.id); closeOrderDetail()" class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">确认订单</button>
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
