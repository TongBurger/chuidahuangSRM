  <script setup lang="ts">
  import { ref, computed, onMounted } from 'vue'
  import { RouterLink } from 'vue-router'
  import { Search, Plus, Package, LayoutGrid, List, XCircle, X as XIcon } from 'lucide-vue-next'
  import { mockOrders, mockSuppliers } from '@/data/mockData'
  import type { Order } from '@/types'
  import { useAppStore } from '@/stores/useAppStore'
  import ExportButton from '@/components/ExportButton.vue'

  // 导出配置
  const exportOptions = computed(() => ({
    title: '订单列表',
    columns: [
      { key: 'orderNo', label: '订单编号' },
      { key: 'supplierName', label: '供应商名称' },
      { key: 'orderDate', label: '订单日期' },
      { key: 'deliveryDate', label: '交货日期' },
      { key: 'status', label: '订单状态' },
      { key: 'totalAmount', label: '订单金额', formatter: (v: number) => `¥${v.toLocaleString()}` },
      { key: 'items', label: '物料数量', formatter: (items: any[]) => `${items.length} 种物料` },
    ],
    data: filteredOrders.value
  }))

  const searchQuery = ref('')
  const statusFilter = ref('all')
  const viewMode = ref<'grid' | 'list'>('list')
  const appStore = useAppStore()

  // 对话框状态
  const isCreateDialogOpen = ref(false)
  const formErrors = ref<Record<string, string>>({})

  // 订单列表，优先从 localStorage 读取
  const orders = ref<Order[]>([])

  // 初始化订单数据
  onMounted(() => {
    const saved = localStorage.getItem('orders')
    if (saved) {
      try {
        orders.value = JSON.parse(saved)
      } catch {
        orders.value = [...mockOrders]
      }
    } else {
      orders.value = [...mockOrders]
    }
  })

  // 获取供应商列表（从 localStorage 或 mockSuppliers）
  const suppliers = ref(mockSuppliers)
  onMounted(() => {
    const savedSuppliers = localStorage.getItem('suppliers')
    if (savedSuppliers) {
      try {
        suppliers.value = JSON.parse(savedSuppliers)
      } catch {
        suppliers.value = mockSuppliers
      }
    }
  })

  // 新订单表单数据
  const newOrder = ref({
    supplierId: '',
    orderDate: new Date().toISOString().split('T')[0],
    deliveryDate: '',
    items: [{
      materialCode: '',
      materialName: '',
      quantity: 1,
      unit: '个',
      price: 0,
    }]
  })

  const filteredOrders = computed(() =>
    orders.value.filter((order) => {
      const matchesSearch =
        order.orderNo.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
        order.supplierName.toLowerCase().includes(searchQuery.value.toLowerCase())
      const matchesStatus = statusFilter.value === 'all' || order.status === statusFilter.value
      return matchesSearch && matchesStatus
    })
  )

  function getStatusClass(status: string) {
    switch (status) {
      case '已完成': return 'bg-green-100 text-green-800'
      case '已发货': case '已收货': return 'bg-blue-100 text-blue-800'
      default: return 'bg-gray-100 text-gray-700'
    }
  }

  function getSupplierName(id: string) {
    const supplier = suppliers.value.find(s => s.id === id)
    return supplier?.name || ''
  }

  function openCreateDialog() {
    formErrors.value = {}
    newOrder.value = {
      supplierId: '',
      orderDate: new Date().toISOString().split('T')[0],
      deliveryDate: '',
      items: [{
        materialCode: '',
        materialName: '',
        quantity: 1,
        unit: '个',
        price: 0,
      }]
    }
    isCreateDialogOpen.value = true
  }

  function addOrderItem() {
    newOrder.value.items.push({
      materialCode: '',
      materialName: '',
      quantity: 1,
      unit: '个',
      price: 0,
    })
  }

  function removeOrderItem(index: number) {
    if (newOrder.value.items.length > 1) {
      newOrder.value.items.splice(index, 1)
    }
  }

  function calculateItemAmount(item: any) {
    return item.quantity * item.price
  }

  function calculateTotalAmount() {
    return newOrder.value.items.reduce((sum, item) => sum + calculateItemAmount(item), 0)
  }

  function handleCreateOrder() {
    // 清空之前的错误
    formErrors.value = {}

    // 表单验证
    if (!newOrder.value.supplierId) {
      formErrors.value.supplierId = '请选择供应商'
      return
    }
    if (!newOrder.value.deliveryDate) {
      formErrors.value.deliveryDate = '请选择交货日期'
      return
    }

    // 验证物料明细
    for (let i = 0; i < newOrder.value.items.length; i++) {
      const item = newOrder.value.items[i]
      if (!item.materialCode.trim()) {
        formErrors.value[`item-${i}-code`] = '请输入物料编码'
        return
      }
      if (!item.materialName.trim()) {
        formErrors.value[`item-${i}-name`] = '请输入物料名称'
        return
      }
      if (item.quantity <= 0) {
        formErrors.value[`item-${i}-quantity`] = '数量必须大于0'
        return
      }
      if (item.price <= 0) {
        formErrors.value[`item-${i}-price`] = '单价必须大于0'
        return
      }
    }

    // 创建订单对象
    const order: Order = {
      id: `O${String(orders.value.length + 1).padStart(3, '0')}`,
      orderNo: `PO${new Date().toISOString().slice(0, 10).replace(/-/g, '')}${String(orders.value.length + 1).padStart(2, '0')}`,
      supplierId: newOrder.value.supplierId,
      supplierName: getSupplierName(newOrder.value.supplierId),
      orderDate: newOrder.value.orderDate,
      deliveryDate: newOrder.value.deliveryDate,
      status: '待确认',
      totalAmount: calculateTotalAmount(),
      items: newOrder.value.items.map(item => ({
        ...item,
        amount: calculateItemAmount(item)
      })),
      changeHistory: [{
        version: 1,
        changeDate: newOrder.value.orderDate,
        changeType: '创建订单',
        description: '初始订单创建',
        operator: `${appStore.currentUser.role}-${appStore.currentUser.name}`,
      }]
    }

    // 添加到列表
    orders.value.push(order)

    // 保存到 localStorage
    localStorage.setItem('orders', JSON.stringify(orders.value))

    // 显示成功提示
    appStore.showToast('订单创建成功', 'success')

    // 关闭对话框
    isCreateDialogOpen.value = false
  }

  // ERP同步功能
  const isERPSyncDialogOpen = ref(false)
  const erpConfig = ref({
    serverUrl: '',
    apiKey: '',
    syncDate: new Date().toISOString().split('T')[0],
  })

  function handleSyncFromERP() {
    isERPSyncDialogOpen.value = true
  }

  function handleExecuteSync() {
    if (!erpConfig.value.serverUrl || !erpConfig.value.apiKey) {
      appStore.showToast('请先配置ERP连接信息', 'warning')
      return
    }

    appStore.showToast('正在从用友U8同步订单...', 'info')
    setTimeout(() => {
      // 模拟从ERP同步的订单
      const syncedOrder = {
        id: `O${String(orders.value.length + 1).padStart(3, '0')}`,
        orderNo: `U8-${new Date().getTime()}`,
        supplierId: suppliers.value[0]?.id || 'S001',
        supplierName: suppliers.value[0]?.name || '同步供应商',
        orderDate: new Date().toISOString().split('T')[0],
        deliveryDate: '',
        status: '待确认',
        totalAmount: Math.floor(Math.random() * 500000) + 100000,
        items: [
          {
            materialCode: 'U8-MAT001',
            materialName: 'U8同步物料',
            quantity: 100,
            unit: '个',
            price: 500,
            amount: 50000
          }
        ],
        changeHistory: [{
          version: 1,
          changeDate: new Date().toISOString().split('T')[0],
          changeType: 'ERP同步',
          description: '从用友U8系统同步',
          operator: 'ERP系统',
        }],
      }

      orders.value.push(syncedOrder)
      localStorage.setItem('orders', JSON.stringify(orders.value))

      appStore.showToast(`成功同步 1 条订单`, 'success')
      isERPSyncDialogOpen.value = false
    }, 1500)
  }

  // 订单变更功能
  const isChangeDialogOpen = ref(false)
  const selectedOrderForChange = ref<any>(null)
  const changeForm = ref({
    changeReason: '',
    newDeliveryDate: '',
    itemChanges: [] as any[],
  })

  function openChangeDialog(order: any) {
    selectedOrderForChange.value = order
    changeForm.value = {
      changeReason: '',
      newDeliveryDate: order.deliveryDate,
      itemChanges: order.items.map((item: any) => ({ ...item })),
    }
    isChangeDialogOpen.value = true
  }

  function handleSubmitChange() {
    if (!changeForm.value.changeReason.trim()) {
      appStore.showToast('请输入变更原因', 'warning')
      return
    }

    const orderIdx = orders.value.findIndex(o => o.id === selectedOrderForChange.value!.id)
    if (orderIdx > -1) {
      const order = orders.value[orderIdx]
      const oldVersion = order.changeHistory.length + 1

      // 更新订单信息
      if (changeForm.value.newDeliveryDate) {
        order.deliveryDate = changeForm.value.newDeliveryDate
      }
      order.items = changeForm.value.itemChanges
      order.totalAmount = order.items.reduce((sum: number, item: any) => sum + (item.quantity * item.price), 0)

      // 添加变更记录
      order.changeHistory.push({
        version: oldVersion + 1,
        changeDate: new Date().toISOString().split('T')[0],
        changeType: '订单变更',
        description: changeForm.value.changeReason,
        operator: `${appStore.currentUser.role}-${appStore.currentUser.name}`,
      })

      localStorage.setItem('orders', JSON.stringify(orders.value))
      appStore.showToast('订单变更成功', 'success')
      isChangeDialogOpen.value = false
    }
  }

  // 确认订单
  function handleConfirmOrder(order: any) {
    const idx = orders.value.findIndex(o => o.id === order.id)
    if (idx > -1) {
      orders.value[idx].status = '生产中'
      localStorage.setItem('orders', JSON.stringify(orders.value))
      appStore.showToast('订单已确认', 'success')
    }
  }

  // 确认收货
  function handleConfirmReceipt(order: any) {
    const idx = orders.value.findIndex(o => o.id === order.id)
    if (idx > -1) {
      orders.value[idx].status = '已收货'
      localStorage.setItem('orders', JSON.stringify(orders.value))
      appStore.showToast('收货确认成功', 'success')
    }
  }
  </script>

  <template>
    <div class="space-y-6">
      <!-- 页面标题 -->
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-semibold text-gray-900">{{ appStore.t('page.orders.title') }}</h1>
          <p class="text-sm text-gray-500 mt-1">与ERP (用友U8) 深度集成 · 全流程追踪</p>
        </div>
        <div class="flex items-center gap-3">
          <ExportButton :export-options="exportOptions" filename="orders" />
          <button @click="handleSyncFromERP" class="flex items-center gap-2 px-4 py-2 border border-gray-300 rounded-lg text-sm font-medium hover:bg-gray-50 transition-colors">
            <Package class="w-4 h-4" />
            从用友U8同步
          </button>
          <button class="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700 transition-colors" @click="openCreateDialog">
            <Plus class="w-4 h-4" />
            {{ appStore.t('btn.createOrder') }}
          </button>
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div class="bg-white rounded-lg border border-gray-200 p-6 text-center">
          <p class="text-3xl font-semibold text-gray-900">{{ orders.length }}</p>
          <p class="text-sm text-gray-500 mt-1">{{ appStore.t('order.total') }}</p>
        </div>
        <div class="bg-white rounded-lg border border-gray-200 p-6 text-center">
          <p class="text-3xl font-semibold text-orange-600">{{ orders.filter((o) => o.status === '生产中').length }}</p>
          <p class="text-sm text-gray-500 mt-1">{{ appStore.t('order.inProduction') }}</p>
        </div>
        <div class="bg-white rounded-lg border border-gray-200 p-6 text-center">
          <p class="text-3xl font-semibold text-blue-600">{{ orders.filter((o) => o.status === '已发货').length }}</p>
          <p class="text-sm text-gray-500 mt-1">{{ appStore.t('order.shipped') }}</p>
        </div>
        <div class="bg-white rounded-lg border border-gray-200 p-6 text-center">
          <p class="text-3xl font-semibold text-green-600">{{ orders.filter((o) => o.status === '已完成').length }}</p>
          <p class="text-sm text-gray-500 mt-1">{{ appStore.t('order.completed') }}</p>
        </div>
      </div>

      <!-- 搜索筛选 -->
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex flex-col md:flex-row gap-4">
          <div class="flex-1 relative">
            <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
            <input v-model="searchQuery" :placeholder="appStore.t('order.searchPlaceholder')" class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
          </div>
          <select v-model="statusFilter" class="w-full md:w-48 px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
            <option value="all">{{ appStore.t('order.allStatus') }}</option>
            <option value="待确认">待确认</option>
            <option value="生产中">生产中</option>
            <option value="已发货">已发货</option>
            <option value="已收货">已收货</option>
            <option value="已入库">已入库</option>
            <option value="已完成">已完成</option>
          </select>
          <div class="flex items-center border border-gray-300 rounded-lg p-1 w-full md:w-auto">
            <button
              class="px-3 py-1.5 rounded-md text-sm flex items-center gap-1"
              :class="viewMode === 'grid' ? 'bg-blue-600 text-white' : 'text-gray-700 hover:bg-gray-100'"
              @click="viewMode = 'grid'"
            >
              <LayoutGrid class="w-4 h-4" />{{ appStore.t('btn.grid') }}
            </button>
            <button
              class="px-3 py-1.5 rounded-md text-sm flex items-center gap-1"
              :class="viewMode === 'list' ? 'bg-blue-600 text-white' : 'text-gray-700 hover:bg-gray-100'"
              @click="viewMode = 'list'"
            >
              <List class="w-4 h-4" />{{ appStore.t('btn.list') }}
            </button>
          </div>
        </div>
      </div>

      <!-- 订单列表 -->
      <div v-if="viewMode === 'grid'" class="space-y-4">
        <div v-for="order in filteredOrders" :key="order.id" class="bg-white rounded-lg border border-gray-200 p-6 hover:shadow-lg transition-shadow">
          <div class="space-y-4">
            <!-- 订单头部 -->
            <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
              <div class="flex items-center gap-4">
                <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
                  <Package class="w-6 h-6 text-blue-600" />
                </div>
                <div>
                  <div class="flex items-center gap-2">
                    <h3 class="font-semibold text-gray-900">{{ order.orderNo }}</h3>
                    <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="getStatusClass(order.status)">{{ order.status }}</span>
                  </div>
                  <p class="text-sm text-gray-500 mt-1">{{ order.supplierName }}</p>
                </div>
              </div>
              <div class="text-right">
                <p class="text-2xl font-semibold text-gray-900">¥{{ order.totalAmount.toLocaleString() }}</p>
                <p class="text-sm text-gray-500 mt-1">订单金额</p>
              </div>
            </div>

            <!-- 订单详情 -->
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4 p-4 bg-gray-50 rounded-lg">
              <div><p class="text-sm text-gray-500">订单日期</p><p class="font-medium text-gray-900 mt-1">{{ order.orderDate }}</p></div>
              <div><p class="text-sm text-gray-500">交货日期</p><p class="font-medium text-gray-900 mt-1">{{ order.deliveryDate }}</p></div>
              <div><p class="text-sm text-gray-500">物料数量</p><p class="font-medium text-gray-900 mt-1">{{ order.items.length }} 种物料</p></div>
            </div>

            <!-- 物料明细 -->
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
            <div class="flex gap-2 pt-2">
              <RouterLink :to="`/orders/${order.id}`" class="flex-1">
                <button class="w-full px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">{{ appStore.t('btn.viewDetail') }}</button>
              </RouterLink>
              <button @click="openChangeDialog(order)" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">变更</button>
              <button v-if="order.status === '待确认'" @click="handleConfirmOrder(order)" class="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">{{ appStore.t('btn.confirmOrder') }}</button>
              <button v-if="order.status === '已发货'" @click="handleConfirmReceipt(order)" class="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">{{ appStore.t('btn.confirmReceipt') }}</button>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="bg-white rounded-lg border border-gray-200 overflow-x-auto">
        <table class="w-full min-w-max text-sm">
          <thead class="bg-gray-50">
            <tr>
              <th class="text-left px-4 py-3 font-medium text-gray-700">{{ appStore.t('order.table.orderNo') }}</th>
              <th class="text-left px-4 py-3 font-medium text-gray-700">{{ appStore.t('order.table.supplier') }}</th>
              <th class="text-left px-4 py-3 font-medium text-gray-700">{{ appStore.t('order.table.status') }}</th>
              <th class="text-left px-4 py-3 font-medium text-gray-700">{{ appStore.t('order.table.orderDate') }}</th>
              <th class="text-left px-4 py-3 font-medium text-gray-700">{{ appStore.t('order.table.deliveryDate') }}</th>
              <th class="text-right px-4 py-3 font-medium text-gray-700">{{ appStore.t('order.table.amount') }}</th>
              <th class="text-right px-4 py-3 font-medium text-gray-700">{{ appStore.t('order.table.itemCount') }}</th>
              <th class="text-left px-4 py-3 font-medium text-gray-700">{{ appStore.t('common.action') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in filteredOrders" :key="order.id" class="border-t border-gray-200">
              <td class="px-4 py-3 text-gray-900 font-medium">{{ order.orderNo }}</td>
              <td class="px-4 py-3 text-gray-900">{{ order.supplierName }}</td>
              <td class="px-4 py-3">
                <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="getStatusClass(order.status)">{{ order.status }}</span>
              </td>
              <td class="px-4 py-3 text-gray-900">{{ order.orderDate }}</td>
              <td class="px-4 py-3 text-gray-900">{{ order.deliveryDate }}</td>
              <td class="px-4 py-3 text-right text-gray-900 font-medium">¥{{ order.totalAmount.toLocaleString() }}</td>
              <td class="px-4 py-3 text-right text-gray-900">{{ order.items.length }}</td>
              <td class="px-4 py-3">
                <div class="flex gap-2">
                  <RouterLink :to="`/orders/${order.id}`">
                    <button class="px-3 py-1.5 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">{{ appStore.t('btn.viewDetail') }}</button>
                  </RouterLink>
                  <button @click="openChangeDialog(order)" class="px-3 py-1.5 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">变更</button>
                  <button v-if="order.status === '待确认'" @click="handleConfirmOrder(order)" class="px-3 py-1.5 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">{{ appStore.t('btn.confirmOrder') }}</button>
                  <button v-if="order.status === '已发货'" @click="handleConfirmReceipt(order)" class="px-3 py-1.5 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">{{ appStore.t('btn.confirmReceipt') }}</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="filteredOrders.length === 0" class="bg-white rounded-lg border border-gray-200 py-12">
        <div class="text-center text-gray-500">
          <Package class="w-12 h-12 mx-auto mb-4 opacity-50" />
          <p>{{ appStore.t('order.empty') }}</p>
        </div>
      </div>

      <!-- 创建订单弹窗 -->
      <Teleport to="body">
        <Transition name="modal">
          <div v-if="isCreateDialogOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4">
            <div class="absolute inset-0 bg-black/50" @click="isCreateDialogOpen = false" />
            <div class="relative bg-white rounded-xl shadow-xl w-full max-w-3xl max-h-[90vh] overflow-y-auto p-6">
              <h3 class="text-lg font-semibold text-gray-900 mb-4">创建订单</h3>

              <div class="space-y-4">
                <!-- 基本信息 -->
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">供应商 <span class="text-red-500">*</span></label>
                    <select
                      v-model="newOrder.supplierId"
                      :class="['w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2', formErrors.supplierId ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500']"
                    >
                      <option value="">请选择供应商</option>
                      <option v-for="supplier in suppliers" :key="supplier.id" :value="supplier.id">
                        {{ supplier.name }}
                      </option>
                    </select>
                    <p v-if="formErrors.supplierId" class="text-red-500 text-xs mt-1 flex items-center gap-1">
                      <XCircle class="w-3 h-3" />{{ formErrors.supplierId }}
                    </p>
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">订单日期</label>
                    <input v-model="newOrder.orderDate" type="date" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">交货日期 <span class="text-red-500">*</span></label>
                    <input
                      v-model="newOrder.deliveryDate"
                      type="date"
                      :class="['w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2', formErrors.deliveryDate ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500']"
                    />
                    <p v-if="formErrors.deliveryDate" class="text-red-500 text-xs mt-1 flex items-center gap-1">
                      <XCircle class="w-3 h-3" />{{ formErrors.deliveryDate }}
                    </p>
                  </div>
                </div>

                <!-- 物料明细 -->
                <div>
                  <div class="flex items-center justify-between mb-2">
                    <label class="block text-sm font-medium text-gray-700">物料明细</label>
                    <button type="button" @click="addOrderItem" class="text-sm text-blue-600 hover:text-blue-700 flex items-center gap-1">
                      <Plus class="w-4 h-4" />添加物料
                    </button>
                  </div>
                  <div class="border border-gray-200 rounded-lg overflow-hidden">
                    <table class="w-full text-sm">
                      <thead class="bg-gray-50">
                        <tr>
                          <th class="text-left px-3 py-2 font-medium text-gray-700">物料编码</th>
                          <th class="text-left px-3 py-2 font-medium text-gray-700">物料名称</th>
                          <th class="text-right px-3 py-2 font-medium text-gray-700 w-20">数量</th>
                          <th class="text-right px-3 py-2 font-medium text-gray-700 w-20">单位</th>
                          <th class="text-right px-3 py-2 font-medium text-gray-700 w-24">单价</th>
                          <th class="text-right px-3 py-2 font-medium text-gray-700 w-24">金额</th>
                          <th class="w-10"></th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="(item, index) in newOrder.items" :key="index" class="border-t border-gray-200">
                          <td class="px-3 py-2">
                            <input
                              v-model="item.materialCode"
                              placeholder="物料编码"
                              :class="['w-full px-2 py-1.5 border rounded text-sm focus:outline-none focus:ring-1', formErrors[`item-${index}-code`] ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500']"
                            />
                          </td>
                          <td class="px-3 py-2">
                            <input
                              v-model="item.materialName"
                              placeholder="物料名称"
                              :class="['w-full px-2 py-1.5 border rounded text-sm focus:outline-none focus:ring-1', formErrors[`item-${index}-name`] ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500']"
                            />
                          </td>
                          <td class="px-3 py-2">
                            <input
                              v-model.number="item.quantity"
                              type="number"
                              min="1"
                              :class="['w-full px-2 py-1.5 border rounded text-sm text-right focus:outline-none focus:ring-1', formErrors[`item-${index}-quantity`] ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500']"
                            />
                          </td>
                          <td class="px-3 py-2">
                            <select v-model="item.unit" class="w-full px-2 py-1.5 border border-gray-300 rounded text-sm focus:outline-none focus:ring-1 focus:ring-blue-500">
                              <option value="个">个</option>
                              <option value="套">套</option>
                              <option value="件">件</option>
                              <option value="KG">KG</option>
                              <option value="米">米</option>
                            </select>
                          </td>
                          <td class="px-3 py-2">
                            <input
                              v-model.number="item.price"
                              type="number"
                              min="0"
                              step="0.01"
                              :class="['w-full px-2 py-1.5 border rounded text-sm text-right focus:outline-none focus:ring-1', formErrors[`item-${index}-price`] ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500']"
                            />
                          </td>
                          <td class="px-3 py-2 text-right font-medium">¥{{ calculateItemAmount(item).toLocaleString() }}</td>
                          <td class="px-3 py-2">
                            <button type="button" @click="removeOrderItem(index)" class="p-1 text-gray-400 hover:text-red-500" :disabled="newOrder.items.length === 1">
                              <XIcon class="w-4 h-4" />
                            </button>
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                  <p v-if="formErrors[`item-0-code`]" class="text-red-500 text-xs mt-1 flex items-center gap-1">
                    <XCircle class="w-3 h-3" />{{ formErrors[`item-0-code`] }}
                  </p>
                </div>

                <!-- 总金额 -->
                <div class="bg-gray-50 rounded-lg p-4 flex justify-between items-center">
                  <span class="text-sm font-medium text-gray-700">订单总金额</span>
                  <span class="text-xl font-semibold text-blue-600">¥{{ calculateTotalAmount().toLocaleString() }}</span>
                </div>
              </div>

              <div class="flex justify-end gap-3 mt-6">
                <button type="button" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50" @click="isCreateDialogOpen = false">取消</button>
                <button type="button" class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700" @click="handleCreateOrder">创建订单</button>
              </div>
            </div>
          </div>
        </Transition>
      </Teleport>

      <!-- ERP同步对话框 -->
      <Teleport to="body">
        <Transition name="modal">
          <div v-if="isERPSyncDialogOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4">
            <div class="absolute inset-0 bg-black/50" @click="isERPSyncDialogOpen = false" />
            <div class="relative bg-white rounded-xl shadow-xl w-full max-w-lg p-6">
              <h3 class="text-lg font-semibold text-gray-900 mb-4">从用友U8同步订单</h3>
              <div class="space-y-4">
                <div class="p-4 bg-blue-50 rounded-lg">
                  <p class="text-sm text-blue-800">从用友U8 ERP系统同步采购订单数据，支持按日期范围筛选。</p>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">ERP服务器地址</label>
                  <input v-model="erpConfig.serverUrl" type="text" placeholder="http://erp-server:8080/U8API" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">API密钥</label>
                  <input v-model="erpConfig.apiKey" type="password" placeholder="请输入API密钥" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">同步起始日期</label>
                  <input v-model="erpConfig.syncDate" type="date" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>
              </div>
              <div class="flex justify-end gap-3 mt-6">
                <button @click="isERPSyncDialogOpen = false" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">取消</button>
                <button @click="handleExecuteSync" class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">开始同步</button>
              </div>
            </div>
          </div>
        </Transition>
      </Teleport>

      <!-- 订单变更对话框 -->
      <Teleport to="body">
        <Transition name="modal">
          <div v-if="isChangeDialogOpen && selectedOrderForChange" class="fixed inset-0 z-50 flex items-center justify-center p-4">
            <div class="absolute inset-0 bg-black/50" @click="isChangeDialogOpen = false" />
            <div class="relative bg-white rounded-xl shadow-xl w-full max-w-2xl max-h-[90vh] overflow-hidden">
              <div class="flex items-center justify-between p-4 border-b">
                <h3 class="text-lg font-semibold text-gray-900">变更订单 - {{ selectedOrderForChange.orderNo }}</h3>
                <button @click="isChangeDialogOpen = false" class="p-2 hover:bg-gray-100 rounded-lg">✕</button>
              </div>
              <div class="p-6 overflow-y-auto max-h-[60vh] space-y-4">
                <div class="p-3 bg-gray-50 rounded-lg">
                  <div class="grid grid-cols-2 gap-3 text-sm">
                    <div><p class="text-gray-500">订单号</p><p class="font-medium">{{ selectedOrderForChange.orderNo }}</p></div>
                    <div><p class="text-gray-500">供应商</p><p class="font-medium">{{ selectedOrderForChange.supplierName }}</p></div>
                    <div><p class="text-gray-500">当前交期</p><p class="font-medium">{{ selectedOrderForChange.deliveryDate }}</p></div>
                    <div><p class="text-gray-500">订单金额</p><p class="font-medium">¥{{ selectedOrderForChange.totalAmount.toLocaleString() }}</p></div>
                  </div>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">变更原因 <span class="text-red-500">*</span></label>
                  <textarea v-model="changeForm.changeReason" rows="2" placeholder="请输入变更原因" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"></textarea>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">新交货日期</label>
                  <input v-model="changeForm.newDeliveryDate" type="date" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">物料明细调整</label>
                  <div class="border border-gray-200 rounded-lg overflow-hidden">
                    <table class="w-full text-sm">
                      <thead class="bg-gray-50">
                        <tr>
                          <th class="text-left px-3 py-2 font-medium text-gray-700">物料编码</th>
                          <th class="text-left px-3 py-2 font-medium text-gray-700">物料名称</th>
                          <th class="text-right px-3 py-2 font-medium text-gray-700">数量</th>
                          <th class="text-right px-3 py-2 font-medium text-gray-700">单价</th>
                          <th class="text-right px-3 py-2 font-medium text-gray-700">金额</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="(item, idx) in changeForm.itemChanges" :key="idx" class="border-t border-gray-200">
                          <td class="px-3 py-2 text-gray-900">{{ item.materialCode }}</td>
                          <td class="px-3 py-2 text-gray-900">{{ item.materialName }}</td>
                          <td class="px-3 py-2 text-right">
                            <input v-model.number="item.quantity" type="number" min="0" class="w-20 px-2 py-1 border rounded text-right text-sm" />
                          </td>
                          <td class="px-3 py-2 text-right">
                            <input v-model.number="item.price" type="number" min="0" step="0.01" class="w-24 px-2 py-1 border rounded text-right text-sm" />
                          </td>
                          <td class="px-3 py-2 text-right font-medium">¥{{ (item.quantity * item.price).toLocaleString() }}</td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
              <div class="flex justify-end gap-3 p-4 border-t bg-gray-50">
                <button @click="isChangeDialogOpen = false" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">取消</button>
                <button @click="handleSubmitChange" class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">提交变更</button>
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
