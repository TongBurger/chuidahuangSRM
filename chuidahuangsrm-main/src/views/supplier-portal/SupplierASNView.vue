<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Truck, CheckCircle, X, Eye } from 'lucide-vue-next'
import { useAppStore } from '@/stores/useAppStore'

const router = useRouter()
const appStore = useAppStore()
const supplierId = 'S001'

const isCreateDialogOpen = ref(false)
const isDetailDialogOpen = ref(false)
const selectedASN = ref<any>(null)

// Mock ASN 数据 - 扩展到10条
const mockASNList = [
  {
    id: 'ASN001',
    asnNo: 'ASN2026031901',
    orderId: 'PO004',
    orderNo: 'PO2026031001',
    shipDate: '2026-03-20',
    estimatedArrivalDate: '2026-03-22',
    items: [
      { materialCode: 'MAT006', materialName: '底座组件', quantity: 500, unit: '个' }
    ],
    itemCount: 1,
    status: '在途',
    supplierId: 'S001',
    createdAt: '2026-03-20',
  },
  {
    id: 'ASN002',
    asnNo: 'ASN2026031501',
    orderId: 'PO005',
    orderNo: 'PO2026030501',
    shipDate: '2026-03-16',
    estimatedArrivalDate: '2026-03-18',
    items: [
      { materialCode: 'MAT007', materialName: '控制面板外壳', quantity: 700, unit: '个' }
    ],
    itemCount: 1,
    status: '已收货',
    supplierId: 'S001',
    createdAt: '2026-03-16',
  },
  {
    id: 'ASN003',
    asnNo: 'ASN2026030801',
    orderId: 'PO006',
    orderNo: 'PO2026030201',
    shipDate: '2026-03-09',
    estimatedArrivalDate: '2026-03-12',
    items: [
      { materialCode: 'MAT008', materialName: '散热器支架', quantity: 1200, unit: '个' }
    ],
    itemCount: 1,
    status: '已收货',
    supplierId: 'S001',
    createdAt: '2026-03-09',
  },
  {
    id: 'ASN004',
    asnNo: 'ASN2026030101',
    orderId: 'PO007',
    orderNo: 'PO2026022801',
    shipDate: '2026-03-02',
    estimatedArrivalDate: '2026-03-05',
    items: [
      { materialCode: 'MAT009', materialName: '外壳总成', quantity: 400, unit: '套' },
      { materialCode: 'MAT010', materialName: '连接件', quantity: 800, unit: '个' }
    ],
    itemCount: 2,
    status: '已收货',
    supplierId: 'S001',
    createdAt: '2026-03-02',
  },
  {
    id: 'ASN005',
    asnNo: 'ASN2026022501',
    orderId: 'PO008',
    orderNo: 'PO2026022001',
    shipDate: '2026-02-26',
    estimatedArrivalDate: '2026-02-28',
    items: [
      { materialCode: 'MAT011', materialName: '电机外壳A型', quantity: 600, unit: '个' }
    ],
    itemCount: 1,
    status: '已收货',
    supplierId: 'S001',
    createdAt: '2026-02-26',
  },
  {
    id: 'ASN006',
    asnNo: 'ASN2026021801',
    orderId: 'PO009',
    orderNo: 'PO2026021501',
    shipDate: '2026-02-19',
    estimatedArrivalDate: '2026-02-22',
    items: [
      { materialCode: 'MAT012', materialName: '端盖组件B型', quantity: 1500, unit: '件' },
      { materialCode: 'MAT013', materialName: '密封圈', quantity: 3000, unit: '个' }
    ],
    itemCount: 2,
    status: '已收货',
    supplierId: 'S001',
    createdAt: '2026-02-19',
  },
  {
    id: 'ASN007',
    asnNo: 'ASN2026021201',
    orderId: 'PO010',
    orderNo: 'PO2026021001',
    shipDate: '2026-02-13',
    estimatedArrivalDate: '2026-02-16',
    items: [
      { materialCode: 'MAT014', materialName: '散热片组件', quantity: 2500, unit: '片' }
    ],
    itemCount: 1,
    status: '已收货',
    supplierId: 'S001',
    createdAt: '2026-02-13',
  },
  {
    id: 'ASN008',
    asnNo: 'ASN2026020501',
    orderId: 'PO011',
    orderNo: 'PO2026020101',
    shipDate: '2026-02-06',
    estimatedArrivalDate: '2026-02-09',
    items: [
      { materialCode: 'MAT015', materialName: '控制面板外壳', quantity: 900, unit: '个' },
      { materialCode: 'MAT016', materialName: '按键组件', quantity: 1800, unit: '个' }
    ],
    itemCount: 2,
    status: '已收货',
    supplierId: 'S001',
    createdAt: '2026-02-06',
  },
  {
    id: 'ASN009',
    asnNo: 'ASN2026012801',
    orderId: 'PO012',
    orderNo: 'PO2026012501',
    shipDate: '2026-01-29',
    estimatedArrivalDate: '2026-02-01',
    items: [
      { materialCode: 'MAT017', materialName: '底座支架', quantity: 1100, unit: '个' }
    ],
    itemCount: 1,
    status: '已收货',
    supplierId: 'S001',
    createdAt: '2026-01-29',
  },
  {
    id: 'ASN010',
    asnNo: 'ASN2026012001',
    orderId: 'PO013',
    orderNo: 'PO2026011801',
    shipDate: '2026-01-21',
    estimatedArrivalDate: '2026-01-24',
    items: [
      { materialCode: 'MAT018', materialName: '外壳组件C型', quantity: 750, unit: '套' }
    ],
    itemCount: 1,
    status: '已收货',
    supplierId: 'S001',
    createdAt: '2026-01-21',
  },
]

// ASN 列表
const asnList = ref<any[]>([])

// 创建ASN表单
const newASN = ref({
  orderId: '',
  shipDate: new Date().toISOString().split('T')[0],
  estimatedArrivalDate: '',
  items: [{
    materialCode: '',
    materialName: '',
    quantity: 1,
    unit: '个',
  }]
})

// 可选订单列表（生产中状态的订单）
const availableOrders = ref<any[]>([])

onMounted(() => {
  // 加载ASN数据
  const savedASN = localStorage.getItem('supplier_asn')
  if (savedASN) {
    try {
      const allASN = JSON.parse(savedASN)
      asnList.value = allASN.filter((a: any) => a.supplierId === supplierId)
    } catch {
      asnList.value = mockASNList
    }
  } else {
    asnList.value = mockASNList
  }

  // Mock 可用订单
  availableOrders.value = [
    { id: 'PO002', orderNo: 'PO2026031801' },
    { id: 'PO003', orderNo: 'PO2026031501' },
  ]

  // 加载订单数据
  const savedOrders = localStorage.getItem('orders')
  if (savedOrders) {
    try {
      const allOrders = JSON.parse(savedOrders)
      availableOrders.value = allOrders.filter((o: any) => o.supplierId === supplierId && o.status === '生产中')
    } catch {
      // 保持mock数据
    }
  }

  // 检查是否有从订单页面传来的订单ID
  const queryOrderId = router.currentRoute.value.query.orderId as string
  if (queryOrderId) {
    newASN.value.orderId = queryOrderId
    isCreateDialogOpen.value = true
    loadOrderItems(queryOrderId)
  }
})

// 加载订单物料信息
function loadOrderItems(orderId: string) {
  const savedOrders = localStorage.getItem('orders')
  if (savedOrders) {
    const allOrders = JSON.parse(savedOrders)
    const order = allOrders.find((o: any) => o.id === orderId)
    if (order && order.items) {
      newASN.value.items = order.items.map((item: any) => ({
        materialCode: item.materialCode,
        materialName: item.materialName,
        quantity: item.quantity,
        unit: item.unit,
      }))
    }
  } else {
    // Mock 订单物料
    const mockOrderItems: any = {
      'PO002': [{ materialCode: 'MAT003', materialName: '散热片', quantity: 2000, unit: '片' }],
      'PO003': [{ materialCode: 'MAT005', materialName: '连接器外壳', quantity: 800, unit: '个' }],
    }
    if (mockOrderItems[orderId]) {
      newASN.value.items = mockOrderItems[orderId]
    }
  }
}

// 当选择订单时，加载物料信息
function handleOrderChange() {
  if (newASN.value.orderId) {
    loadOrderItems(newASN.value.orderId)
  }
}

// 添加物料行
function addASNItem() {
  newASN.value.items.push({
    materialCode: '',
    materialName: '',
    quantity: 1,
    unit: '个',
  })
}

// 删除物料行
function removeASNItem(index: number) {
  if (newASN.value.items.length > 1) {
    newASN.value.items.splice(index, 1)
  }
}

// 生成ASN编号
function generateASNNo() {
  const date = new Date()
  const dateStr = `${date.getFullYear()}${String(date.getMonth() + 1).padStart(2, '0')}${String(date.getDate()).padStart(2, '0')}`
  const count = asnList.value.filter(a => a.asnNo.includes(dateStr)).length + 1
  return `ASN${dateStr}${String(count).padStart(2, '0')}`
}

// 获取订单号
function getOrderNo(orderId: string) {
  const order = availableOrders.value.find(o => o.id === orderId)
  return order?.orderNo || orderId
}

// 创建ASN
function handleCreateASN() {
  if (!newASN.value.orderId) {
    appStore.showToast('请选择订单', 'warning')
    return
  }
  if (!newASN.value.shipDate) {
    appStore.showToast('请选择发货日期', 'warning')
    return
  }
  if (newASN.value.items.some(item => !item.materialCode || !item.materialName)) {
    appStore.showToast('请完善物料信息', 'warning')
    return
  }

  // 获取订单信息
  const savedOrders = localStorage.getItem('orders')
  let orderNo = ''
  if (savedOrders) {
    const allOrders = JSON.parse(savedOrders)
    const order = allOrders.find((o: any) => o.id === newASN.value.orderId)
    if (order) {
      orderNo = order.orderNo
    }
  } else {
    // Mock 订单号映射
    const orderNoMap: any = {
      'PO002': 'PO2026031801',
      'PO003': 'PO2026031501',
    }
    orderNo = orderNoMap[newASN.value.orderId] || newASN.value.orderId
  }

  const asn = {
    id: `ASN${Date.now()}`,
    asnNo: generateASNNo(),
    orderId: newASN.value.orderId,
    orderNo: orderNo,
    shipDate: newASN.value.shipDate,
    estimatedArrivalDate: newASN.value.estimatedArrivalDate,
    items: [...newASN.value.items],
    itemCount: newASN.value.items.length,
    status: '在途',
    supplierId,
    createdAt: new Date().toISOString(),
  }

  // 保存到 localStorage
  const savedASN = localStorage.getItem('supplier_asn')
  let allASN = savedASN ? JSON.parse(savedASN) : []
  allASN.push(asn)
  localStorage.setItem('supplier_asn', JSON.stringify(allASN))

  // 更新订单状态
  if (savedOrders) {
    const allOrders = JSON.parse(savedOrders)
    const idx = allOrders.findIndex((o: any) => o.id === newASN.value.orderId)
    if (idx > -1) {
      allOrders[idx].status = '已发货'
      localStorage.setItem('orders', JSON.stringify(allOrders))
    }
  }

  // 更新本地数据
  asnList.value.unshift(asn)
  availableOrders.value = availableOrders.value.filter(o => o.id !== newASN.value.orderId)

  appStore.showToast('ASN创建成功', 'success')
  isCreateDialogOpen.value = false

  // 重置表单
  newASN.value = {
    orderId: '',
    shipDate: new Date().toISOString().split('T')[0],
    estimatedArrivalDate: '',
    items: [{ materialCode: '', materialName: '', quantity: 1, unit: '个' }]
  }
}

// 查看详情
function handleViewDetail(asn: any) {
  selectedASN.value = asn
  isDetailDialogOpen.value = true
}

function getStatusClass(status: string) {
  switch (status) {
    case '已收货': return 'bg-green-100 text-green-800'
    case '在途': return 'bg-blue-100 text-blue-800'
    default: return 'bg-gray-100 text-gray-700'
  }
}
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-semibold text-gray-900">发货管理 (ASN)</h1>
        <p class="text-sm text-gray-500 mt-1">创建和管理预发货通知</p>
      </div>
      <button class="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700" @click="isCreateDialogOpen = true">
        <Plus class="w-4 h-4" />创建ASN
      </button>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">ASN总数</p>
            <p class="text-2xl font-semibold text-gray-900 mt-1">{{ asnList.length }}</p>
          </div>
          <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
            <Truck class="w-6 h-6 text-blue-600" />
          </div>
        </div>
      </div>
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">在途</p>
            <p class="text-2xl font-semibold text-blue-600 mt-1">{{ asnList.filter(a => a.status === '在途').length }}</p>
          </div>
          <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
            <Truck class="w-6 h-6 text-blue-600" />
          </div>
        </div>
      </div>
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">已收货</p>
            <p class="text-2xl font-semibold text-green-600 mt-1">{{ asnList.filter(a => a.status === '已收货').length }}</p>
          </div>
          <div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
            <CheckCircle class="w-6 h-6 text-green-600" />
          </div>
        </div>
      </div>
    </div>

    <div class="bg-white rounded-lg border border-gray-200 overflow-hidden">
      <table class="w-full text-sm">
        <thead class="bg-gray-50">
          <tr>
            <th class="text-left px-4 py-3 font-medium text-gray-700">ASN号</th>
            <th class="text-left px-4 py-3 font-medium text-gray-700">关联订单</th>
            <th class="text-left px-4 py-3 font-medium text-gray-700">发货日期</th>
            <th class="text-left px-4 py-3 font-medium text-gray-700">预计到达</th>
            <th class="text-left px-4 py-3 font-medium text-gray-700">状态</th>
            <th class="text-right px-4 py-3 font-medium text-gray-700">物料数</th>
            <th class="text-left px-4 py-3 font-medium text-gray-700">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="asn in asnList" :key="asn.id" class="border-t border-gray-200">
            <td class="px-4 py-3 font-medium text-gray-900">{{ asn.asnNo }}</td>
            <td class="px-4 py-3 text-gray-900">{{ asn.orderNo }}</td>
            <td class="px-4 py-3 text-gray-900">{{ asn.shipDate }}</td>
            <td class="px-4 py-3 text-gray-900">{{ asn.estimatedArrivalDate || '-' }}</td>
            <td class="px-4 py-3">
              <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="getStatusClass(asn.status)">{{ asn.status }}</span>
            </td>
            <td class="px-4 py-3 text-right text-gray-900">{{ asn.itemCount }}</td>
            <td class="px-4 py-3">
              <button @click="handleViewDetail(asn)" class="px-3 py-1.5 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50 flex items-center gap-1">
                <Eye class="w-3 h-3" />查看详情
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="asnList.length === 0" class="text-center py-12 text-gray-500">
        <Truck class="w-12 h-12 mx-auto mb-4 opacity-50" />
        <p>暂无发货记录</p>
      </div>
    </div>

    <!-- 创建ASN对话框 -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="isCreateDialogOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-black/50" @click="isCreateDialogOpen = false" />
          <div class="relative bg-white rounded-xl shadow-xl w-full max-w-2xl max-h-[80vh] overflow-hidden">
            <div class="flex items-center justify-between p-4 border-b">
              <h3 class="text-lg font-semibold text-gray-900">创建发货通知 (ASN)</h3>
              <button @click="isCreateDialogOpen = false" class="p-2 hover:bg-gray-100 rounded-lg">
                <X class="w-5 h-5" />
              </button>
            </div>
            <div class="p-6 overflow-y-auto max-h-[60vh]">
              <div class="space-y-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">选择订单 *</label>
                  <select v-model="newASN.orderId" @change="handleOrderChange" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
                    <option value="">请选择订单</option>
                    <option v-for="order in availableOrders" :key="order.id" :value="order.id">{{ order.orderNo }}</option>
                  </select>
                </div>
                <div class="grid grid-cols-2 gap-4">
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">发货日期 *</label>
                    <input v-model="newASN.shipDate" type="date" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">预计到达日期</label>
                    <input v-model="newASN.estimatedArrivalDate" type="date" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                  </div>
                </div>

                <div>
                  <div class="flex items-center justify-between mb-2">
                    <label class="block text-sm font-medium text-gray-700">物料明细</label>
                    <button @click="addASNItem" class="text-sm text-blue-600 hover:text-blue-700">+ 添加物料</button>
                  </div>
                  <div class="border border-gray-200 rounded-lg overflow-hidden">
                    <table class="w-full text-sm">
                      <thead class="bg-gray-50">
                        <tr>
                          <th class="text-left px-3 py-2 font-medium text-gray-700">物料编码</th>
                          <th class="text-left px-3 py-2 font-medium text-gray-700">物料名称</th>
                          <th class="text-right px-3 py-2 font-medium text-gray-700">数量</th>
                          <th class="text-left px-3 py-2 font-medium text-gray-700">单位</th>
                          <th class="w-10"></th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="(item, idx) in newASN.items" :key="idx" class="border-t border-gray-200">
                          <td class="px-2 py-2">
                            <input v-model="item.materialCode" placeholder="物料编码" class="w-full px-2 py-1 border border-gray-300 rounded text-sm focus:outline-none focus:ring-1 focus:ring-blue-500" />
                          </td>
                          <td class="px-2 py-2">
                            <input v-model="item.materialName" placeholder="物料名称" class="w-full px-2 py-1 border border-gray-300 rounded text-sm focus:outline-none focus:ring-1 focus:ring-blue-500" />
                          </td>
                          <td class="px-2 py-2">
                            <input v-model.number="item.quantity" type="number" min="1" class="w-full px-2 py-1 border border-gray-300 rounded text-sm text-right focus:outline-none focus:ring-1 focus:ring-blue-500" />
                          </td>
                          <td class="px-2 py-2">
                            <input v-model="item.unit" placeholder="单位" class="w-full px-2 py-1 border border-gray-300 rounded text-sm focus:outline-none focus:ring-1 focus:ring-blue-500" />
                          </td>
                          <td class="px-2 py-2">
                            <button v-if="newASN.items.length > 1" @click="removeASNItem(idx)" class="p-1 text-red-600 hover:bg-red-50 rounded">
                              <X class="w-4 h-4" />
                            </button>
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            </div>
            <div class="flex justify-end gap-3 p-4 border-t bg-gray-50">
              <button @click="isCreateDialogOpen = false" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">取消</button>
              <button @click="handleCreateASN" class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">创建</button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- ASN详情对话框 -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="isDetailDialogOpen && selectedASN" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-black/50" @click="isDetailDialogOpen = false" />
          <div class="relative bg-white rounded-xl shadow-xl w-full max-w-2xl max-h-[80vh] overflow-hidden">
            <div class="flex items-center justify-between p-4 border-b">
              <h3 class="text-lg font-semibold text-gray-900">发货通知详情</h3>
              <button @click="isDetailDialogOpen = false" class="p-2 hover:bg-gray-100 rounded-lg">
                <X class="w-5 h-5" />
              </button>
            </div>
            <div class="p-6 overflow-y-auto max-h-[60vh]">
              <div class="space-y-4">
                <div class="grid grid-cols-2 gap-4">
                  <div>
                    <p class="text-sm text-gray-500">ASN号</p>
                    <p class="font-medium text-gray-900">{{ selectedASN.asnNo }}</p>
                  </div>
                  <div>
                    <p class="text-sm text-gray-500">关联订单</p>
                    <p class="font-medium text-gray-900">{{ selectedASN.orderNo }}</p>
                  </div>
                  <div>
                    <p class="text-sm text-gray-500">发货日期</p>
                    <p class="font-medium text-gray-900">{{ selectedASN.shipDate }}</p>
                  </div>
                  <div>
                    <p class="text-sm text-gray-500">状态</p>
                    <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="getStatusClass(selectedASN.status)">{{ selectedASN.status }}</span>
                  </div>
                </div>

                <div v-if="selectedASN.items && selectedASN.items.length > 0">
                  <p class="text-sm font-medium text-gray-700 mb-2">物料明细</p>
                  <div class="border border-gray-200 rounded-lg overflow-hidden">
                    <table class="w-full text-sm">
                      <thead class="bg-gray-50">
                        <tr>
                          <th class="text-left px-4 py-2 font-medium text-gray-700">物料编码</th>
                          <th class="text-left px-4 py-2 font-medium text-gray-700">物料名称</th>
                          <th class="text-right px-4 py-2 font-medium text-gray-700">发货数量</th>
                          <th class="text-left px-4 py-2 font-medium text-gray-700">单位</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="(item, idx) in selectedASN.items" :key="idx" class="border-t border-gray-200">
                          <td class="px-4 py-3 text-gray-900">{{ item.materialCode }}</td>
                          <td class="px-4 py-3 text-gray-900">{{ item.materialName }}</td>
                          <td class="px-4 py-3 text-right text-gray-900">{{ item.quantity }}</td>
                          <td class="px-4 py-3 text-gray-900">{{ item.unit }}</td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            </div>
            <div class="flex justify-end p-4 border-t bg-gray-50">
              <button @click="isDetailDialogOpen = false" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">关闭</button>
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
