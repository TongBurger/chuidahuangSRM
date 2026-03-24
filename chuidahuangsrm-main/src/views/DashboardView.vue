<script setup lang="ts">
  import { computed, ref, watch, onMounted } from 'vue'
  import { useRouter, useRoute } from 'vue-router'
  import VueDraggable from 'vuedraggable'
  import { RouterLink } from 'vue-router'
  import {
    TrendingUp,
    AlertTriangle,
    CheckCircle,
    Clock,
    Package,
    UserPlus,
    FileText,
    ClipboardList,
    Wrench,
    Calculator,
    ChevronRight,
    GripVertical,
  } from 'lucide-vue-next'
  import { mockSuppliers, mockOrders, mockMolds } from '@/data/mockData'
  import { useAppStore } from '@/stores/useAppStore'

  const router = useRouter()
  const route = useRoute()
  const appStore = useAppStore()

  // 默认组件顺序（四个模块可自由拖拽）
  const defaultWidgets = [
    { id: 'pending', title: '待办事项', icon: ClipboardList },
    { id: 'risk', title: '风险预警中心', icon: AlertTriangle },
    { id: 'topSuppliers', title: '供应商绩效排名', icon: TrendingUp },
    { id: 'recentOrders', title: '近期订单动态', icon: Package },
  ]

  // 从localStorage读取布局配置
  const getSavedLayout = () => {
    try {
      const saved = localStorage.getItem('dashboard-layout')
      if (saved) {
        return JSON.parse(saved)
      }
    } catch (e) {
      console.error('Failed to load layout:', e)
    }
    return null
  }

  // 保存布局到localStorage
  const saveLayout = (widgets: any[]) => {
    try {
      localStorage.setItem('dashboard-layout', JSON.stringify(widgets))
    } catch (e) {
      console.error('Failed to save layout:', e)
    }
  }

  // 布局保存提示
  const showLayoutSavedToast = ref(false)

  // 显示保存成功提示
  function handleLayoutSaved() {
    showLayoutSavedToast.value = true
    setTimeout(() => {
      showLayoutSavedToast.value = false
    }, 2000)
  }

  // 组件顺序（可拖拽）
  const widgets = ref([...defaultWidgets])

  // 加载保存的布局
  onMounted(() => {
    const savedLayout = getSavedLayout()
    if (savedLayout) {
      widgets.value = savedLayout
    }
  })

  // 监听拖拽变化并保存
  watch(widgets, (newVal) => {
    saveLayout(newVal)
    handleLayoutSaved()
  }, { deep: true })

  const totalSuppliers = mockSuppliers.length
  const activeSuppliers = mockSuppliers.filter((s) => s.status === '合格').length

  const totalOrders = mockOrders.length
  const inProductionOrders = mockOrders.filter((o) => o.status === '生产中').length

  const totalMolds = mockMolds.length
  const activeMolds = mockMolds.filter((m) => m.status === '在用').length
  const maintenanceMolds = mockMolds.filter((m) => m.status === '维保中').length

  const expiringCerts = mockSuppliers.flatMap((s) =>
    s.certifications
      .filter((c) => c.status === '即将到期')
      .map((c) => ({ supplierId: s.id, supplier: s.name, cert: c }))
  )

  const moldsNeedMaintenance = mockMolds.filter((m) => m.remainingLife < 15)

  const topSuppliers = computed(() =>
    [...mockSuppliers].sort((a, b) => b.overallScore - a.overallScore).slice(0, 5)
  )

  // 待办事项数据
  const pendingOrders = computed(() =>
    mockOrders.filter((o) => o.status === '待确认' || o.status === '待审核')
  )

  const pendingMolds = computed(() =>
    mockMolds.filter((m) => m.status === '维保中' || m.remainingLife < 20)
  )

  const pendingReconciliations = ref([
    {
      id: 'REC001',
      supplier: '深圳市精诚模具制造有限公司',
      period: '2026年3月',
      amount: 285000,
      status: '待对账',
      type: 'reconciliation',
    },
    {
      id: 'REC002',
      supplier: '东莞市华泰五金制品厂',
      period: '2026年3月',
      amount: 52000,
      status: '待确认',
      type: 'reconciliation',
    },
    {
      id: 'REC003',
      supplier: '佛山市永盛铝制品有限公司',
      period: '2026年3月',
      amount: 156000,
      status: '待审核',
      type: 'reconciliation',
    },
  ])

  const pendingItems = computed(() => {
    const items: any[] = []

    pendingOrders.value.forEach((order) => {
      items.push({
        id: order.id,
        type: 'order',
        title: `订单 ${order.orderNo}`,
        description: `供应商: ${order.supplierName}`,
        status: order.status,
        statusColor: order.status === '待确认' ? 'orange' : 'yellow',
        amount: `¥${order.totalAmount.toLocaleString()}`,
        date: order.orderDate,
      })
    })

    pendingMolds.value.forEach((mold) => {
      items.push({
        id: mold.id,
        type: 'mold',
        title: `模具 ${mold.code}`,
        description: mold.name,
        status: mold.status === '维保中' ? '维保中' : '寿命预警',
        statusColor: mold.remainingLife < 10 ? 'red' : 'orange',
        amount: `剩余寿命 ${mold.remainingLife}%`,
        date: mold.nextMaintenanceDate || '待安排',
      })
    })

    pendingReconciliations.value.forEach((rec) => {
      items.push({
        id: rec.id,
        type: 'reconciliation',
        title: `对账单 ${rec.id}`,
        description: `${rec.supplier} - ${rec.period}`,
        status: rec.status,
        statusColor: 'blue',
        amount: `¥${rec.amount.toLocaleString()}`,
        date: '-',
      })
    })

    return items
  })

  const pendingStats = computed(() => ({
    total: pendingItems.value.length,
    orders: pendingOrders.value.length,
    molds: pendingMolds.value.length,
    reconciliations: pendingReconciliations.value.length,
  }))

  const displayPendingItems = computed(() =>
    route.query.showAllPending === 'true' ? pendingItems.value : pendingItems.value.slice(0, 5)
  )

  function getItemIcon(type: string) {
    switch (type) {
      case 'order':
        return ClipboardList
      case 'mold':
        return Wrench
      case 'reconciliation':
        return Calculator
      default:
        return FileText
    }
  }

  function getStatusColor(color: string) {
    switch (color) {
      case 'red':
        return 'bg-red-100 text-red-800'
      case 'orange':
        return 'bg-orange-100 text-orange-800'
      case 'yellow':
        return 'bg-yellow-100 text-yellow-800'
      case 'blue':
        return 'bg-blue-100 text-blue-800'
      default:
        return 'bg-gray-100 text-gray-800'
    }
  }

  function handleItemClick(item: any) {
    switch (item.type) {
      case 'order':
        router.push(`/orders/${item.id}`)
        break
      case 'mold':
        router.push(`/molds/${item.id}`)
        break
      case 'reconciliation':
        router.push('/finance')
        break
    }
  }

  function getCategoryVariant(category: string) {
    switch (category) {
      case '战略':
        return 'bg-blue-100 text-blue-800'
      case '关键':
        return 'bg-gray-100 text-gray-800'
      default:
        return 'bg-gray-100 text-gray-700'
    }
  }

  function getOrderStatusVariant(status: string) {
    switch (status) {
      case '已完成':
        return 'bg-green-100 text-green-800'
      case '已发货':
      case '已收货':
        return 'bg-blue-100 text-blue-800'
      default:
        return 'bg-gray-100 text-gray-700'
    }
  }

  // 重置布局
  function resetLayout() {
    widgets.value = [...defaultWidgets]
    localStorage.removeItem('dashboard-layout')
  }
</script>

<template>
  <div class="space-y-5">
    <!-- 布局保存成功提示 -->
    <Transition
      enter-active-class="transition ease-out duration-300"
      enter-from-class="transform opacity-0 translate-y-2"
      enter-to-class="transform opacity-100 translate-y-0"
      leave-active-class="transition ease-in duration-200"
      leave-from-class="transform opacity-100 translate-y-0"
      leave-to-class="transform opacity-0 translate-y-2"
    >
      <div
        v-if="showLayoutSavedToast"
        class="fixed top-4 right-4 z-50 bg-green-600 text-white px-4 py-2 rounded-lg shadow-lg flex items-center gap-2"
      >
        <CheckCircle class="w-5 h-5" />
        <span class="text-sm font-medium">布局已保存</span>
      </div>
    </Transition>

    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-semibold text-gray-900">{{ appStore.t('page.dashboard.title') }}</h1>
        <p class="text-sm text-gray-500 mt-1">欢迎回来，{{ appStore.currentUser.name }} | 今天是 {{ new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' }) }}</p>
      </div>
      <button
        @click="resetLayout"
        class="px-3 py-1.5 text-sm text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition-colors border border-gray-300"
        title="恢复默认布局"
      >
        重置布局
      </button>
    </div>

    <!-- 关键指标卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
      <div
        @click="router.push('/suppliers')"
        class="bg-white rounded-lg border border-gray-200 p-4 cursor-pointer hover:shadow-md hover:border-blue-300 transition-all"
      >
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">供应商总数</p>
            <p class="text-2xl font-semibold text-gray-900 mt-1">{{ totalSuppliers }}</p>
            <div class="flex items-center gap-1 mt-2">
              <CheckCircle class="w-4 h-4 text-green-600" />
              <span class="text-sm text-gray-600">{{ activeSuppliers }} 家合格</span>
            </div>
          </div>
          <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
            <TrendingUp class="w-6 h-6 text-blue-600" />
          </div>
        </div>
      </div>

      <div
        @click="router.push('/orders')"
        class="bg-white rounded-lg border border-gray-200 p-4 cursor-pointer hover:shadow-md hover:border-blue-300 transition-all"
      >
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">在途订单</p>
            <p class="text-2xl font-semibold text-gray-900 mt-1">{{ totalOrders }}</p>
            <div class="flex items-center gap-1 mt-2">
              <Clock class="w-4 h-4 text-orange-600" />
              <span class="text-sm text-gray-600">{{ inProductionOrders }} 生产中</span>
            </div>
          </div>
          <div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
            <Package class="w-6 h-6 text-green-600" />
          </div>
        </div>
      </div>

      <div
        @click="router.push('/molds')"
        class="bg-white rounded-lg border border-gray-200 p-4 cursor-pointer hover:shadow-md hover:border-blue-300 transition-all"
      >
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">模具在用</p>
            <p class="text-2xl font-semibold text-gray-900 mt-1">{{ activeMolds }}/{{ totalMolds }}</p>
            <div class="flex items-center gap-1 mt-2">
              <AlertTriangle class="w-4 h-4 text-yellow-600" />
              <span class="text-sm text-gray-600">{{ maintenanceMolds }} 维保中</span>
            </div>
          </div>
          <div class="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center">
            <Package class="w-6 h-6 text-purple-600" />
          </div>
        </div>
      </div>

      <div
        @click="router.push('/risk-alerts')"
        class="bg-white rounded-lg border border-gray-200 p-4 cursor-pointer hover:shadow-md hover:border-blue-300 transition-all"
      >
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">风险预警</p>
            <p class="text-2xl font-semibold text-gray-900 mt-1">
              {{ expiringCerts.length + moldsNeedMaintenance.length }}
            </p>
            <div class="flex items-center gap-1 mt-2">
              <AlertTriangle class="w-4 h-4 text-red-600" />
              <span class="text-sm text-gray-600">需处理</span>
            </div>
          </div>
          <div class="w-12 h-12 bg-red-100 rounded-lg flex items-center justify-center">
            <AlertTriangle class="w-6 h-6 text-red-600" />
          </div>
        </div>
      </div>
    </div>

    <!-- 可拖拽的模块区域 -->
    <VueDraggable
      v-model="widgets"
      class="grid grid-cols-1 lg:grid-cols-2 gap-5"
      item-key="id"
      :animation="200"
      handle=".drag-handle"
    >
      <template #item="{ element }">
        <div
          class="bg-white rounded-lg border border-gray-200 p-2 relative group min-h-[340px] flex flex-col"
        >
          <!-- 拖拽手柄 -->
          <div class="drag-handle absolute top-2 right-2 opacity-0 group-hover:opacity-50 cursor-grab p-1 hover:bg-gray-100 rounded transition-opacity">
            <GripVertical class="w-5 h-5 text-gray-400" />
          </div>

          <!-- 待办事项模块 -->
          <div v-if="element.id === 'pending'" class="flex flex-col flex-1">
            <div class="flex items-center justify-between mb-1">
              <h3 class="text-base font-semibold text-gray-900 flex items-center gap-2">
                <ClipboardList class="w-4 h-4 text-blue-600" />
                {{ element.title }}
              </h3>
              <span class="px-1.5 py-0.5 bg-blue-100 text-blue-800 rounded-full text-xs font-medium">
                {{ pendingStats.total }} {{ appStore.t('pending.total') }}
              </span>
            </div>

            <div v-if="displayPendingItems.length === 0" class="text-center py-3 flex-1">
              <CheckCircle class="w-10 h-10 text-green-500 mx-auto mb-2" />
              <p class="text-sm text-gray-600">{{ appStore.t('pending.noItems') }}</p>
            </div>

            <div v-else class="space-y-3 flex-1">
              <div
                v-for="item in displayPendingItems"
                :key="item.id"
                @click="handleItemClick(item)"
                class="flex items-center gap-2 px-3 py-2 border border-gray-200 rounded-lg hover:bg-blue-50 hover:border-blue-300 transition-all cursor-pointer group"
              >
                <div
                  class="w-6 h-6 rounded flex items-center justify-center flex-shrink-0"
                  :class="[
                    item.type === 'order' ? 'bg-orange-100' : '',
                    item.type === 'mold' ? 'bg-yellow-100' : '',
                    item.type === 'reconciliation' ? 'bg-blue-100' : '',
                  ]"
                >
                  <component
                    :is="getItemIcon(item.type)"
                    class="w-3.5 h-3.5"
                    :class="[
                      item.type === 'order' ? 'text-orange-600' : '',
                      item.type === 'mold' ? 'text-yellow-600' : '',
                      item.type === 'reconciliation' ? 'text-blue-600' : '',
                    ]"
                  />
                </div>

                <div class="flex-1 min-w-0">
                  <div class="flex items-center justify-between">
                    <div class="flex-1 min-w-0">
                      <div class="flex items-center gap-2">
                        <p class="text-sm font-medium text-gray-900 group-hover:text-blue-700 transition-colors truncate">
                          {{ item.title }}
                        </p>
                        <span
                          class="px-2 py-0.5 rounded text-xs font-medium flex-shrink-0"
                          :class="getStatusColor(item.statusColor)"
                        >
                          {{ item.status }}
                        </span>
                      </div>
                      <p class="text-xs text-gray-600 truncate mt-1">{{ item.description }}</p>
                    </div>
                    <div class="text-right flex-shrink-0 ml-2">
                      <p class="text-sm font-medium text-gray-900">{{ item.amount }}</p>
                    </div>
                    <ChevronRight class="w-5 h-5 text-gray-400 group-hover:text-blue-600 transition-colors flex-shrink-0" />
                  </div>
                </div>
              </div>
            </div>

            <div v-if="pendingItems.length > 0" class="pt-2 border-t border-gray-200 mt-auto">
              <button
                @click="
                  route.query.showAllPending === 'true'
                    ? router.push({ path: '/' })
                    : router.push({ path: '/', query: { showAllPending: 'true' } })
                "
                class="w-full flex items-center justify-center gap-2 px-4 py-2 text-sm text-blue-700 hover:bg-blue-50 rounded-lg transition-colors"
              >
                <ClipboardList class="w-4 h-4" />
                <span>
                  {{ route.query.showAllPending === 'true' ? appStore.t('pending.collapse') : appStore.t('pending.viewAllPending') }}
                </span>
                <span class="text-xs text-gray-500">({{ pendingStats.total }})</span>
              </button>
            </div>
          </div>

          <!-- 风险预警中心模块 -->
          <div v-if="element.id === 'risk'" class="flex flex-col flex-1">
            <h3 class="text-base font-semibold text-gray-900 flex items-center gap-2 mb-1.5">
              <AlertTriangle class="w-4 h-4 text-orange-600" />
              {{ element.title }}
            </h3>
            <div class="space-y-2 flex-1">
              <div>
                <h4 class="text-base font-medium text-gray-900 mb-2">资质到期预警</h4>
                <p v-if="expiringCerts.length === 0" class="text-sm text-gray-500">
                  暂无即将到期的资质
                </p>
                <div v-else class="space-y-2">
                  <div
                    v-for="(item, idx) in expiringCerts.slice(0, 2)"
                    :key="idx"
                    @click="router.push(`/suppliers/${item.supplierId}`)"
                    class="flex items-start gap-2 p-2.5 bg-orange-50 rounded-lg hover:bg-orange-100 cursor-pointer transition-colors"
                  >
                    <AlertTriangle class="w-4 h-4 text-orange-600 mt-0.5 flex-shrink-0" />
                    <div class="flex-1 min-w-0">
                      <p class="text-sm font-medium text-gray-900 mb-1">{{ item.supplier }}</p>
                      <p class="text-xs text-gray-600">
                        {{ item.cert.name }} - 有效期至 {{ item.cert.validUntil }}
                      </p>
                    </div>
                    <ChevronRight class="w-4 h-4 text-gray-400 flex-shrink-0" />
                  </div>
                  <div v-if="expiringCerts.length > 2" class="text-center pt-1">
                    <button
                      @click="router.push('/risk-alerts')"
                      class="text-xs text-blue-600 hover:text-blue-700"
                    >
                      查看全部 {{ expiringCerts.length }} 条
                    </button>
                  </div>
                </div>
              </div>

              <div class="pt-2 border-t">
                <h4 class="text-base font-medium text-gray-900 mb-2">模具寿命预警</h4>
                <p v-if="moldsNeedMaintenance.length === 0" class="text-sm text-gray-500">
                  暂无需要维护的模具
                </p>
                <div v-else class="space-y-2">
                  <div
                    v-for="(mold, idx) in moldsNeedMaintenance.slice(0, 2)"
                    :key="mold.id"
                    @click="router.push(`/molds/${mold.id}`)"
                    class="p-2.5 bg-yellow-50 rounded-lg hover:bg-yellow-100 cursor-pointer transition-colors"
                  >
                    <div class="flex items-start justify-between mb-2">
                      <div>
                        <p class="text-sm font-medium text-gray-900 mb-1">{{ mold.code }}</p>
                        <p class="text-sm text-gray-600">{{ mold.name }}</p>
                      </div>
                      <span
                        class="px-2 py-0.5 rounded-full text-xs font-medium"
                        :class="
                          mold.remainingLife < 10
                            ? 'bg-red-100 text-red-800'
                            : 'bg-gray-100 text-gray-800'
                        "
                      >
                        剩余 {{ mold.remainingLife }}%
                      </span>
                    </div>
                    <div class="h-1.5 bg-gray-200 rounded-full overflow-hidden">
                      <div
                        class="h-full bg-yellow-500 transition-all"
                        :style="{ width: `${mold.remainingLife}%` }"
                      />
                    </div>
                  </div>
                  <div v-if="moldsNeedMaintenance.length > 2" class="text-center pt-1">
                    <button
                      @click="router.push('/risk-alerts')"
                      class="text-xs text-blue-600 hover:text-blue-700"
                    >
                      查看全部 {{ moldsNeedMaintenance.length }} 条
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 供应商绩效 TOP5 -->
          <div v-if="element.id === 'topSuppliers'" class="flex flex-col flex-1">
            <h3 class="text-base font-semibold text-gray-900 mb-2 flex items-center gap-2">
              <TrendingUp class="w-4 h-4 text-blue-600" />
              {{ element.title }}
            </h3>
            <div class="space-y-3 flex-1">
              <div
                v-for="(supplier, idx) in topSuppliers"
                :key="supplier.id"
                @click="router.push(`/suppliers/${supplier.id}`)"
                class="flex items-center gap-2 p-2.5 rounded-lg hover:bg-gray-50 cursor-pointer transition-colors"
              >
                <div
                  class="w-6 h-6 bg-gradient-to-br from-blue-600 to-blue-800 rounded-full flex items-center justify-center flex-shrink-0"
                >
                  <span class="text-white text-xs font-medium">{{ idx + 1 }}</span>
                </div>
                <div class="flex-1 min-w-0">
                  <div class="flex items-center justify-between mb-1.5">
                    <p class="text-sm font-medium text-gray-900 truncate">{{ supplier.name }}</p>
                    <span
                      class="px-2 py-0.5 rounded-full text-xs font-medium ml-2 flex-shrink-0"
                      :class="getCategoryVariant(supplier.category)"
                    >
                      {{ supplier.category }}
                    </span>
                  </div>
                  <div class="h-1.5 bg-gray-200 rounded-full overflow-hidden">
                    <div
                      class="h-full bg-blue-600 transition-all"
                      :style="{ width: `${supplier.overallScore}%` }"
                    />
                  </div>
                  <p class="text-sm text-gray-500 mt-1">综合得分: {{ supplier.overallScore }}</p>
                </div>
                <ChevronRight class="w-4 h-4 text-gray-400 flex-shrink-0" />
              </div>
            </div>
          </div>

          <!-- 近期订单 -->
          <div v-if="element.id === 'recentOrders'" class="flex flex-col flex-1">
            <h3 class="text-base font-semibold text-gray-900 mb-2 flex items-center gap-2">
              <Package class="w-4 h-4 text-green-600" />
              {{ element.title }}
            </h3>
            <div class="space-y-2.5 flex-1">
              <div
                v-for="order in mockOrders.slice(0, 5)"
                :key="order.id"
                class="flex items-center gap-3 p-2 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors cursor-pointer"
                @click="router.push(`/orders/${order.id}`)"
              >
                <div class="flex-1">
                  <div class="flex items-center gap-2 mb-1">
                    <p class="text-sm font-medium text-gray-900">{{ order.orderNo }}</p>
                    <span
                      class="px-2 py-0.5 rounded-full text-xs font-medium"
                      :class="getOrderStatusVariant(order.status)"
                    >
                      {{ order.status }}
                    </span>
                  </div>
                  <p class="text-sm text-gray-600">{{ order.supplierName }}</p>
                  <div class="flex items-center gap-3 mt-1 text-sm text-gray-500">
                    <span>{{ order.orderDate }}</span>
                    <span>¥{{ order.totalAmount.toLocaleString() }}</span>
                  </div>
                </div>
                <ChevronRight class="w-4 h-4 text-gray-400 flex-shrink-0" />
              </div>
            </div>
          </div>
        </div>
      </template>
    </VueDraggable>

    <!-- 供应商注册入口 -->
    <div class="bg-white rounded-lg border border-gray-200 p-4">
      <div
        class="flex items-center justify-between p-4 bg-gradient-to-r from-blue-50 to-blue-100 rounded-lg"
      >
        <div class="flex items-center gap-4">
          <div class="w-10 h-10 bg-blue-600 rounded-lg flex items-center justify-center">
            <UserPlus class="w-5 h-5 text-white" />
          </div>
          <div>
            <h3 class="text-sm font-semibold text-gray-900 mb-1">新供应商注册</h3>
            <p class="text-sm text-gray-600">邀请供应商自助注册，快速接入供应商协同平台</p>
          </div>
        </div>
        <RouterLink
          to="/supplier-register"
          class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700 transition-colors"
        >
          {{ appStore.t('btn.goRegister') }}
        </RouterLink>
      </div>
    </div>
  </div>
</template>

<style scoped>
.sortable-ghost {
  opacity: 0.5;
  background: #f0f9ff;
}
</style>
