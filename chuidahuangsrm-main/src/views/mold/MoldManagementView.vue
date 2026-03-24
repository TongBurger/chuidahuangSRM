  <script setup lang="ts">
  import { ref, computed, onMounted } from 'vue'
  import { RouterLink } from 'vue-router'
  import { Search, Plus, Package, AlertTriangle, Wrench, LayoutGrid, List, XCircle, History } from 'lucide-vue-next'
  import { mockMolds } from '@/data/mockData'
  import type { Mold } from '@/types'
  import { useAppStore } from '@/stores/useAppStore'

  const searchQuery = ref('')
  const statusFilter = ref('all')
  const viewMode = ref<'grid' | 'list'>('list')
  const appStore = useAppStore()

  // 对话框状态
  const isAddDialogOpen = ref(false)
  const isUsageRecordDialogOpen = ref(false)
  const isUsageHistoryDialogOpen = ref(false)
  const isMaintenanceDialogOpen = ref(false)
  const isMaintenanceHistoryDialogOpen = ref(false)
  const formErrors = ref<Record<string, string>>({})

  // 选中的模具和使用记录
  const selectedMold = ref<Mold | null>(null)
  const selectedMoldForHistory = ref<Mold | null>(null)
  const selectedMoldForMaintenance = ref<Mold | null>(null)

  // 维保记录表单
  const newMaintenanceRecord = ref({
    maintenanceType: '预防性维保',
    maintenanceDate: new Date().toISOString().split('T')[0],
    nextMaintenanceDate: '',
    cost: 0,
    operator: '',
    notes: '',
  })

  // 使用记录表单
  const newUsageRecord = ref({
    usageCount: 0,
    usageDate: new Date().toISOString().split('T')[0],
    productionOrder: '',
    operator: '',
    notes: '',
  })

  // 模具列表，优先从 localStorage 读取
  const molds = ref<Mold[]>([])

  // 获取供应商列表
  const suppliers = ref<any[]>([])

  // 初始化模具数据
  onMounted(() => {
    const saved = localStorage.getItem('molds')
    if (saved) {
      try {
        molds.value = JSON.parse(saved)
      } catch {
        molds.value = [...mockMolds]
      }
    } else {
      molds.value = [...mockMolds]
    }

    // 获取供应商列表
    const savedSuppliers = localStorage.getItem('suppliers')
    if (savedSuppliers) {
      try {
        suppliers.value = JSON.parse(savedSuppliers)
      } catch {
        suppliers.value = []
      }
    }
  })

  // 新模具表单数据
  const newMold = ref({
    code: '',
    name: '',
    category: '压铸模',
    supplierId: '',
    originalValue: 0,
    designLife: 100000,
    nextMaintenanceDate: '',
  })

  const filteredMolds = computed(() =>
    molds.value.filter((mold) => {
      const matchesSearch =
        mold.code.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
        mold.name.toLowerCase().includes(searchQuery.value.toLowerCase())
      const matchesStatus = statusFilter.value === 'all' || mold.status === statusFilter.value
      return matchesSearch && matchesStatus
    })
  )

  function getStatusColor(status: string) {
    switch (status) {
      case '在用': return 'bg-green-100 text-green-800'
      case '维保中': return 'bg-yellow-100 text-yellow-800'
      case '闲置': return 'bg-gray-100 text-gray-800'
      case '报废': return 'bg-red-100 text-red-800'
      default: return 'bg-gray-100 text-gray-800'
    }
  }

  function getLifeColor(life: number) {
    if (life < 10) return 'text-red-600'
    if (life < 20) return 'text-orange-600'
    return 'text-green-600'
  }

  function getLifeBarColor(life: number) {
    if (life < 10) return 'bg-red-500'
    if (life < 20) return 'bg-orange-500'
    return 'bg-green-500'
  }

  function getSupplierName(id: string) {
    const supplier = suppliers.value.find(s => s.id === id)
    return supplier?.name || ''
  }

  function openAddDialog() {
    formErrors.value = {}
    newMold.value = {
      code: '',
      name: '',
      category: '压铸模',
      supplierId: '',
      originalValue: 0,
      designLife: 100000,
      nextMaintenanceDate: '',
    }
    isAddDialogOpen.value = true
  }

  function handleAddMold() {
    // 清空之前的错误
    formErrors.value = {}

    // 表单验证
    if (!newMold.value.code.trim()) {
      formErrors.value.code = '请输入模具编码'
      return
    }
    if (!newMold.value.name.trim()) {
      formErrors.value.name = '请输入模具名称'
      return
    }
    if (!newMold.value.supplierId) {
      formErrors.value.supplierId = '请选择供应商'
      return
    }
    if (newMold.value.originalValue <= 0) {
      formErrors.value.originalValue = '请输入有效的原值'
      return
    }
    if (newMold.value.designLife <= 0) {
      formErrors.value.designLife = '请输入有效的设计寿命'
      return
    }
    if (!newMold.value.nextMaintenanceDate) {
      formErrors.value.nextMaintenanceDate = '请选择下次维保日期'
      return
    }

    // 创建新模具对象
    const mold: Mold = {
      id: `M${String(molds.value.length + 1).padStart(3, '0')}`,
      code: newMold.value.code.trim(),
      name: newMold.value.name.trim(),
      category: newMold.value.category,
      status: '在用',
      location: getSupplierName(newMold.value.supplierId),
      supplierId: newMold.value.supplierId,
      supplierName: getSupplierName(newMold.value.supplierId),
      originalValue: newMold.value.originalValue,
      designLife: newMold.value.designLife,
      currentUsage: 0,
      remainingLife: 100,
      lastMaintenanceDate: new Date().toISOString().split('T')[0],
      nextMaintenanceDate: newMold.value.nextMaintenanceDate,
      drawings: [],
      productionOrders: [],
    }

    // 添加到列表
    molds.value.push(mold)

    // 保存到 localStorage
    localStorage.setItem('molds', JSON.stringify(molds.value))

    // 显示成功提示
    appStore.showToast('模具添加成功', 'success')

    // 关闭对话框
    isAddDialogOpen.value = false
  }

  // 使用记录相关类型定义
  interface UsageRecord {
    id: string
    moldId: string
    usageCount: number
    usageDate: string
    productionOrder: string
    operator: string
    notes: string
    createdAt: string
  }

  // 获取模具的使用记录列表
  function getMoldUsageRecords(moldId: string): UsageRecord[] {
    const saved = localStorage.getItem('mold_usage_records')
    if (saved) {
      try {
        const allRecords = JSON.parse(saved) as UsageRecord[]
        return allRecords.filter(r => r.moldId === moldId)
      } catch {
        return []
      }
    }
    return []
  }

  // 打开使用记录对话框
  function openUsageRecordDialog(mold: Mold) {
    selectedMold.value = mold
    newUsageRecord.value = {
      usageCount: 0,
      usageDate: new Date().toISOString().split('T')[0],
      productionOrder: '',
      operator: '',
      notes: '',
    }
    isUsageRecordDialogOpen.value = true
  }

  // 保存使用记录
  function handleSaveUsageRecord() {
    if (!selectedMold.value) return

    // 表单验证
    if (newUsageRecord.value.usageCount <= 0) {
      appStore.showToast('请输入有效的使用模次', 'warning')
      return
    }

    // 创建使用记录
    const record: UsageRecord = {
      id: `UR${Date.now()}`,
      moldId: selectedMold.value.id,
      usageCount: newUsageRecord.value.usageCount,
      usageDate: newUsageRecord.value.usageDate,
      productionOrder: newUsageRecord.value.productionOrder,
      operator: newUsageRecord.value.operator,
      notes: newUsageRecord.value.notes,
      createdAt: new Date().toISOString(),
    }

    // 保存到 localStorage
    const saved = localStorage.getItem('mold_usage_records')
    let allRecords = saved ? JSON.parse(saved) : []
    allRecords.push(record)
    localStorage.setItem('mold_usage_records', JSON.stringify(allRecords))

    // 更新模具的使用量和寿命
    const moldIndex = molds.value.findIndex(m => m.id === selectedMold.value!.id)
    if (moldIndex > -1) {
      const mold = molds.value[moldIndex]
      mold.currentUsage += newUsageRecord.value.usageCount
      mold.remainingLife = Math.max(0, Math.round((1 - mold.currentUsage / mold.designLife) * 100))

      // 保存模具列表
      localStorage.setItem('molds', JSON.stringify(molds.value))
    }

    appStore.showToast('使用记录添加成功', 'success')
    isUsageRecordDialogOpen.value = false
  }

  // 打开使用历史对话框
  function openUsageHistoryDialog(mold: Mold) {
    selectedMoldForHistory.value = mold
    isUsageHistoryDialogOpen.value = true
  }

  // 计算总使用记录数
  function getTotalUsageRecords(moldId: string): number {
    return getMoldUsageRecords(moldId).length
  }

  // 维保记录类型
  interface MaintenanceRecord {
    id: string
    moldId: string
    maintenanceType: string
    maintenanceDate: string
    nextMaintenanceDate: string
    cost: number
    operator: string
    notes: string
    createdAt: string
  }

  // 获取模具的维保记录列表
  function getMoldMaintenanceRecords(moldId: string): MaintenanceRecord[] {
    const saved = localStorage.getItem('mold_maintenance_records')
    if (saved) {
      try {
        const allRecords = JSON.parse(saved) as MaintenanceRecord[]
        return allRecords.filter(r => r.moldId === moldId)
      } catch {
        return []
      }
    }
    return []
  }

  // 打开维保记录对话框
  function openMaintenanceDialog(mold: Mold) {
    selectedMoldForMaintenance.value = mold
    newMaintenanceRecord.value = {
      maintenanceType: '预防性维保',
      maintenanceDate: new Date().toISOString().split('T')[0],
      nextMaintenanceDate: mold.nextMaintenanceDate,
      cost: 0,
      operator: '',
      notes: '',
    }
    isMaintenanceDialogOpen.value = true
  }

  // 保存维保记录
  function handleSaveMaintenanceRecord() {
    if (!selectedMoldForMaintenance.value) return

    if (!newMaintenanceRecord.value.nextMaintenanceDate) {
      appStore.showToast('请选择下次维保日期', 'warning')
      return
    }

    const record: MaintenanceRecord = {
      id: `MR${Date.now()}`,
      moldId: selectedMoldForMaintenance.value.id,
      maintenanceType: newMaintenanceRecord.value.maintenanceType,
      maintenanceDate: newMaintenanceRecord.value.maintenanceDate,
      nextMaintenanceDate: newMaintenanceRecord.value.nextMaintenanceDate,
      cost: newMaintenanceRecord.value.cost,
      operator: newMaintenanceRecord.value.operator,
      notes: newMaintenanceRecord.value.notes,
      createdAt: new Date().toISOString(),
    }

    // 保存到 localStorage
    const saved = localStorage.getItem('mold_maintenance_records')
    let allRecords = saved ? JSON.parse(saved) : []
    allRecords.push(record)
    localStorage.setItem('mold_maintenance_records', JSON.stringify(allRecords))

    // 更新模具的维保日期
    const moldIndex = molds.value.findIndex(m => m.id === selectedMoldForMaintenance.value!.id)
    if (moldIndex > -1) {
      molds.value[moldIndex].lastMaintenanceDate = newMaintenanceRecord.value.maintenanceDate
      molds.value[moldIndex].nextMaintenanceDate = newMaintenanceRecord.value.nextMaintenanceDate
      localStorage.setItem('molds', JSON.stringify(molds.value))
    }

    appStore.showToast('维保记录已保存', 'success')
    isMaintenanceDialogOpen.value = false
  }

  // 打开维保历史对话框
  function openMaintenanceHistoryDialog(mold: Mold) {
    selectedMoldForMaintenance.value = mold
    isMaintenanceHistoryDialogOpen.value = true
  }
  </script>

  <template>
    <div class="space-y-6">
      <!-- 标题 -->
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-semibold text-gray-900">{{ appStore.t('page.molds.title') }}</h1>
          <p class="text-sm text-gray-500 mt-1">一模一码 · 全生命周期追踪 · 智能维保</p>
        </div>
        <button class="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700" @click="openAddDialog">
          <Plus class="w-4 h-4" />{{ appStore.t('btn.addMold') }}
        </button>
      </div>

      <!-- 统计卡片 -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div class="bg-white rounded-lg border border-gray-200 p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-gray-500">{{ appStore.t('mold.total') }}</p>
              <p class="text-2xl font-semibold text-gray-900 mt-1">{{ molds.length }}</p>
            </div>
            <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
              <Package class="w-6 h-6 text-blue-600" />
            </div>
          </div>
        </div>
        <div class="bg-white rounded-lg border border-gray-200 p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-gray-500">{{ appStore.t('mold.active') }}</p>
              <p class="text-2xl font-semibold text-green-600 mt-1">{{ molds.filter((m) => m.status === '在用').length }}</p>
            </div>
            <div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
              <Package class="w-6 h-6 text-green-600" />
            </div>
          </div>
        </div>
        <div class="bg-white rounded-lg border border-gray-200 p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-gray-500">{{ appStore.t('mold.maintenance') }}</p>
              <p class="text-2xl font-semibold text-yellow-600 mt-1">{{ molds.filter((m) => m.status === '维保中').length }}</p>
            </div>
            <div class="w-12 h-12 bg-yellow-100 rounded-lg flex items-center justify-center">
              <Wrench class="w-6 h-6 text-yellow-600" />
            </div>
          </div>
        </div>
        <div class="bg-white rounded-lg border border-gray-200 p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-gray-500">{{ appStore.t('mold.alert') }}</p>
              <p class="text-2xl font-semibold text-red-600 mt-1">{{ molds.filter((m) => m.remainingLife < 15).length }}</p>
            </div>
            <div class="w-12 h-12 bg-red-100 rounded-lg flex items-center justify-center">
              <AlertTriangle class="w-6 h-6 text-red-600" />
            </div>
          </div>
        </div>
      </div>

      <!-- 搜索筛选 -->
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex flex-col md:flex-row gap-4">
          <div class="flex-1 relative">
            <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
            <input v-model="searchQuery" :placeholder="appStore.t('mold.searchPlaceholder')" class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
          </div>
          <select v-model="statusFilter" class="w-full md:w-40 px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
            <option value="all">{{ appStore.t('mold.allStatus') }}</option>
            <option value="在用">在用</option>
            <option value="维保中">维保中</option>
            <option value="闲置">闲置</option>
            <option value="报废">报废</option>
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

      <!-- 模具列表 -->
      <div v-if="viewMode === 'grid'" class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div v-for="mold in filteredMolds" :key="mold.id" class="bg-white rounded-lg border border-gray-200 p-6 hover:shadow-lg transition-shadow">
          <div class="space-y-4">
            <div class="flex items-start justify-between">
              <div class="flex-1">
                <div class="flex items-center gap-2 mb-2">
                  <h3 class="font-semibold text-gray-900">{{ mold.code }}</h3>
                  <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="getStatusColor(mold.status)">{{ mold.status }}</span>
                </div>
                <p class="text-sm text-gray-600">{{ mold.name }}</p>
                <p class="text-xs text-gray-500 mt-1">{{ mold.category }}</p>
              </div>
            </div>

            <!-- 寿命指标 -->
            <div class="p-4 bg-gradient-to-br from-blue-50 to-blue-100 rounded-lg">
              <div class="flex items-center justify-between mb-3">
                <span class="text-sm font-medium text-gray-900">模具寿命</span>
                <span class="text-lg font-bold" :class="getLifeColor(mold.remainingLife)">{{ mold.remainingLife }}%</span>
              </div>
              <div class="h-3 bg-white/60 rounded-full overflow-hidden mb-2">
                <div class="h-full transition-all rounded-full" :class="getLifeBarColor(mold.remainingLife)" :style="{ width: `${mold.remainingLife}%` }" />
              </div>
              <div class="flex items-center justify-between text-xs text-gray-600">
                <span>已使用: {{ mold.currentUsage.toLocaleString() }} 模次</span>
                <span>设计寿命: {{ mold.designLife.toLocaleString() }} 模次</span>
              </div>
            </div>

            <!-- 寿命预警 -->
            <div v-if="mold.remainingLife < 15" class="flex items-center gap-2 p-3 bg-red-50 rounded-lg">
              <AlertTriangle class="w-4 h-4 text-red-600 flex-shrink-0" />
              <span class="text-sm text-red-800">模具寿命不足15%，建议尽快安排维保或更换</span>
            </div>

            <!-- 关键信息 -->
            <div class="grid grid-cols-2 gap-4 text-sm">
              <div><p class="text-gray-500">原值</p><p class="font-medium text-gray-900 mt-1">¥{{ mold.originalValue.toLocaleString() }}</p></div>
              <div><p class="text-gray-500">存放位置</p><p class="font-medium text-gray-900 mt-1 truncate" :title="mold.location">{{ mold.location }}</p></div>
              <div><p class="text-gray-500">上次维保</p><p class="font-medium text-gray-900 mt-1">{{ mold.lastMaintenanceDate }}</p></div>
              <div><p class="text-gray-500">下次维保</p><p class="font-medium text-gray-900 mt-1">{{ mold.nextMaintenanceDate }}</p></div>
            </div>

            <div class="p-3 bg-gray-50 rounded-lg">
              <p class="text-xs text-gray-500">使用供应商</p>
              <p class="text-sm font-medium text-gray-900 mt-1">{{ mold.supplierName }}</p>
            </div>

            <!-- 操作 -->
            <div class="flex gap-2 pt-2">
              <RouterLink :to="`/molds/${mold.id}`" class="flex-1">
                <button class="w-full px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">{{ appStore.t('btn.viewDetail') }}</button>
              </RouterLink>
              <button @click="openUsageRecordDialog(mold)" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50 flex items-center justify-center gap-2">
                <Plus class="w-4 h-4" />记录使用
              </button>
              <button @click="openUsageHistoryDialog(mold)" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50 flex items-center justify-center gap-2">
                <History class="w-4 h-4" />使用历史
              </button>
              <button @click="openMaintenanceHistoryDialog(mold)" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50 flex items-center justify-center gap-2">
                <Wrench class="w-4 h-4" />维保历史
              </button>
              <button @click="openMaintenanceDialog(mold)" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50 flex items-center justify-center gap-2">
                <Plus class="w-4 h-4" />记录维保
              </button>
              <button v-if="mold.status === '在用' && mold.remainingLife < 90" class="flex-1 flex items-center justify-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">
                <Wrench class="w-4 h-4" />{{ appStore.t('btn.scheduleMaintenance') }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="bg-white rounded-lg border border-gray-200 overflow-x-auto">
        <table class="w-full min-w-max text-sm">
          <thead class="bg-gray-50">
            <tr>
              <th class="text-left px-4 py-3 font-medium text-gray-700">{{ appStore.t('mold.table.code') }}</th>
              <th class="text-left px-4 py-3 font-medium text-gray-700">{{ appStore.t('mold.table.name') }}</th>
              <th class="text-left px-4 py-3 font-medium text-gray-700">{{ appStore.t('mold.table.status') }}</th>
              <th class="text-right px-4 py-3 font-medium text-gray-700">{{ appStore.t('mold.table.life') }}</th>
              <th class="text-left px-4 py-3 font-medium text-gray-700">{{ appStore.t('mold.table.supplier') }}</th>
              <th class="text-left px-4 py-3 font-medium text-gray-700">{{ appStore.t('mold.table.nextMaintenance') }}</th>
              <th class="text-left px-4 py-3 font-medium text-gray-700">{{ appStore.t('common.action') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="mold in filteredMolds" :key="mold.id" class="border-t border-gray-200">
              <td class="px-4 py-3 font-medium text-gray-900">{{ mold.code }}</td>
              <td class="px-4 py-3 text-gray-900">{{ mold.name }}</td>
              <td class="px-4 py-3">
                <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="getStatusColor(mold.status)">{{ mold.status }}</span>
              </td>
              <td class="px-4 py-3 text-right" :class="getLifeColor(mold.remainingLife)">{{ mold.remainingLife }}%</td>
              <td class="px-4 py-3 text-gray-900">{{ mold.supplierName }}</td>
              <td class="px-4 py-3 text-gray-900">{{ mold.nextMaintenanceDate }}</td>
              <td class="px-4 py-3">
                <div class="flex gap-2">
                  <RouterLink :to="`/molds/${mold.id}`">
                    <button class="px-3 py-1.5 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">{{ appStore.t('btn.viewDetail') }}</button>
                  </RouterLink>
                  <button @click="openUsageRecordDialog(mold)" class="px-3 py-1.5 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">记录使用</button>
                  <button @click="openUsageHistoryDialog(mold)" class="px-3 py-1.5 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">使用历史</button>
                  <button v-if="mold.status === '在用' && mold.remainingLife < 90" class="px-3 py-1.5 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">{{ appStore.t('btn.scheduleMaintenance') }}</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="filteredMolds.length === 0" class="bg-white rounded-lg border border-gray-200 py-12">
        <div class="text-center text-gray-500">
          <Package class="w-12 h-12 mx-auto mb-4 opacity-50" />
          <p>{{ appStore.t('mold.empty') }}</p>
        </div>
      </div>

      <!-- 新增模具弹窗 -->
      <Teleport to="body">
        <Transition name="modal">
          <div v-if="isAddDialogOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4">
            <div class="absolute inset-0 bg-black/50" @click="isAddDialogOpen = false" />
            <div class="relative bg-white rounded-xl shadow-xl w-full max-w-lg p-6">
              <h3 class="text-lg font-semibold text-gray-900 mb-4">新增模具</h3>

              <div class="space-y-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">模具编码 <span class="text-red-500">*</span></label>
                  <input
                    v-model="newMold.code"
                    placeholder="请输入模具编码"
                    :class="['w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2', formErrors.code ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500']"
                  />
                  <p v-if="formErrors.code" class="text-red-500 text-xs mt-1 flex items-center gap-1">
                    <XCircle class="w-3 h-3" />{{ formErrors.code }}
                  </p>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">模具名称 <span class="text-red-500">*</span></label>
                  <input
                    v-model="newMold.name"
                    placeholder="请输入模具名称"
                    :class="['w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2', formErrors.name ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500']"
                  />
                  <p v-if="formErrors.name" class="text-red-500 text-xs mt-1 flex items-center gap-1">
                    <XCircle class="w-3 h-3" />{{ formErrors.name }}
                  </p>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">模具分类</label>
                  <select v-model="newMold.category" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
                    <option value="压铸模">压铸模</option>
                    <option value="注塑模">注塑模</option>
                    <option value="冲压模">冲压模</option>
                    <option value="锻造模">锻造模</option>
                  </select>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">供应商 <span class="text-red-500">*</span></label>
                  <select
                    v-model="newMold.supplierId"
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
                <div class="grid grid-cols-2 gap-4">
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">原值 (元) <span class="text-red-500">*</span></label>
                    <input
                      v-model.number="newMold.originalValue"
                      type="number"
                      min="0"
                      placeholder="请输入原值"
                      :class="['w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2', formErrors.originalValue ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500']"
                    />
                    <p v-if="formErrors.originalValue" class="text-red-500 text-xs mt-1 flex items-center gap-1">
                      <XCircle class="w-3 h-3" />{{ formErrors.originalValue }}
                    </p>
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">设计寿命 (模次) <span class="text-red-500">*</span></label>
                    <input
                      v-model.number="newMold.designLife"
                      type="number"
                      min="1"
                      placeholder="请输入设计寿命"
                      :class="['w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2', formErrors.designLife ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500']"
                    />
                    <p v-if="formErrors.designLife" class="text-red-500 text-xs mt-1 flex items-center gap-1">
                      <XCircle class="w-3 h-3" />{{ formErrors.designLife }}
                    </p>
                  </div>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">下次维保日期 <span class="text-red-500">*</span></label>
                  <input
                    v-model="newMold.nextMaintenanceDate"
                    type="date"
                    :class="['w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2', formErrors.nextMaintenanceDate ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500']"
                  />
                  <p v-if="formErrors.nextMaintenanceDate" class="text-red-500 text-xs mt-1 flex items-center gap-1">
                    <XCircle class="w-3 h-3" />{{ formErrors.nextMaintenanceDate }}
                  </p>
                </div>
              </div>

              <div class="flex justify-end gap-3 mt-6">
                <button type="button" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50" @click="isAddDialogOpen = false">取消</button>
                <button type="button" class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700" @click="handleAddMold">保存</button>
              </div>
            </div>
          </div>
        </Transition>
      </Teleport>

      <!-- 使用记录对话框 -->
      <Teleport to="body">
        <Transition name="modal">
          <div v-if="isUsageRecordDialogOpen && selectedMold" class="fixed inset-0 z-50 flex items-center justify-center p-4">
            <div class="absolute inset-0 bg-black/50" @click="isUsageRecordDialogOpen = false" />
            <div class="relative bg-white rounded-xl shadow-xl w-full max-w-lg">
              <div class="flex items-center justify-between p-4 border-b">
                <h3 class="text-lg font-semibold text-gray-900">记录模具使用</h3>
                <button @click="isUsageRecordDialogOpen = false" class="p-2 hover:bg-gray-100 rounded-lg">✕</button>
              </div>
              <div class="p-6 space-y-4">
                <div class="p-3 bg-gray-50 rounded-lg">
                  <p class="text-sm text-gray-500">模具编码</p>
                  <p class="font-medium text-gray-900">{{ selectedMold.code }}</p>
                  <p class="text-sm text-gray-500 mt-2">模具名称</p>
                  <p class="font-medium text-gray-900">{{ selectedMold.name }}</p>
                  <div class="mt-3 grid grid-cols-2 gap-3 text-sm">
                    <div><p class="text-gray-500">当前使用</p><p class="font-medium">{{ selectedMold.currentUsage.toLocaleString() }} 模次</p></div>
                    <div><p class="text-gray-500">剩余寿命</p><p class="font-medium" :class="getLifeColor(selectedMold.remainingLife)">{{ selectedMold.remainingLife }}%</p></div>
                  </div>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">使用模次 <span class="text-red-500">*</span></label>
                  <input
                    v-model.number="newUsageRecord.usageCount"
                    type="number"
                    min="1"
                    placeholder="请输入本次使用的模次"
                    class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">使用日期</label>
                  <input
                    v-model="newUsageRecord.usageDate"
                    type="date"
                    class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">关联生产订单</label>
                  <input
                    v-model="newUsageRecord.productionOrder"
                    type="text"
                    placeholder="请输入生产订单号（可选）"
                    class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">操作人员</label>
                  <input
                    v-model="newUsageRecord.operator"
                    type="text"
                    placeholder="请输入操作人员姓名（可选）"
                    class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">备注</label>
                  <textarea
                    v-model="newUsageRecord.notes"
                    rows="2"
                    placeholder="请输入备注信息（可选）"
                    class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  ></textarea>
                </div>
              </div>
              <div class="flex justify-end gap-3 p-4 border-t bg-gray-50">
                <button type="button" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50" @click="isUsageRecordDialogOpen = false">取消</button>
                <button type="button" class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700" @click="handleSaveUsageRecord">保存记录</button>
              </div>
            </div>
          </div>
        </Transition>
      </Teleport>

      <!-- 使用历史对话框 -->
      <Teleport to="body">
        <Transition name="modal">
          <div v-if="isUsageHistoryDialogOpen && selectedMoldForHistory" class="fixed inset-0 z-50 flex items-center justify-center p-4">
            <div class="absolute inset-0 bg-black/50" @click="isUsageHistoryDialogOpen = false" />
            <div class="relative bg-white rounded-xl shadow-xl w-full max-w-3xl max-h-[80vh] overflow-hidden">
              <div class="flex items-center justify-between p-4 border-b">
                <h3 class="text-lg font-semibold text-gray-900">使用历史记录 - {{ selectedMoldForHistory.code }}</h3>
                <button @click="isUsageHistoryDialogOpen = false" class="p-2 hover:bg-gray-100 rounded-lg">✕</button>
              </div>
              <div class="p-6 overflow-y-auto max-h-[60vh]">
                <div class="mb-4 p-4 bg-gray-50 rounded-lg">
                  <div class="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                    <div><p class="text-gray-500">模具名称</p><p class="font-medium">{{ selectedMoldForHistory.name }}</p></div>
                    <div><p class="text-gray-500">总使用模次</p><p class="font-medium">{{ selectedMoldForHistory.currentUsage.toLocaleString() }}</p></div>
                    <div><p class="text-gray-500">记录总数</p><p class="font-medium">{{ getTotalUsageRecords(selectedMoldForHistory.id) }} 条</p></div>
                    <div><p class="text-gray-500">剩余寿命</p><p class="font-medium" :class="getLifeColor(selectedMoldForHistory.remainingLife)">{{ selectedMoldForHistory.remainingLife }}%</p></div>
                  </div>
                </div>
                <div v-if="getMoldUsageRecords(selectedMoldForHistory.id).length === 0" class="text-center py-8 text-gray-500">
                  <History class="w-12 h-12 mx-auto mb-3 opacity-50" />
                  <p>暂无使用记录</p>
                </div>
                <div v-else class="border border-gray-200 rounded-lg overflow-hidden">
                  <table class="w-full text-sm">
                    <thead class="bg-gray-50">
                      <tr>
                        <th class="text-left px-4 py-2 font-medium text-gray-700">使用日期</th>
                        <th class="text-right px-4 py-2 font-medium text-gray-700">使用模次</th>
                        <th class="text-left px-4 py-2 font-medium text-gray-700">生产订单</th>
                        <th class="text-left px-4 py-2 font-medium text-gray-700">操作人员</th>
                        <th class="text-left px-4 py-2 font-medium text-gray-700">备注</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="record in getMoldUsageRecords(selectedMoldForHistory.id)" :key="record.id" class="border-t border-gray-200">
                        <td class="px-4 py-3 text-gray-900">{{ record.usageDate }}</td>
                        <td class="px-4 py-3 text-right font-medium text-gray-900">{{ record.usageCount.toLocaleString() }}</td>
                        <td class="px-4 py-3 text-gray-900">{{ record.productionOrder || '-' }}</td>
                        <td class="px-4 py-3 text-gray-900">{{ record.operator || '-' }}</td>
                        <td class="px-4 py-3 text-gray-600 text-xs">{{ record.notes || '-' }}</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
              <div class="flex justify-end gap-3 p-4 border-t bg-gray-50">
                <button type="button" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50" @click="isUsageHistoryDialogOpen = false">关闭</button>
              </div>
            </div>
          </div>
        </Transition>
      </Teleport>

      <!-- 维保记录对话框 -->
      <Teleport to="body">
        <Transition name="modal">
          <div v-if="isMaintenanceDialogOpen && selectedMoldForMaintenance" class="fixed inset-0 z-50 flex items-center justify-center p-4">
            <div class="absolute inset-0 bg-black/50" @click="isMaintenanceDialogOpen = false" />
            <div class="relative bg-white rounded-xl shadow-xl w-full max-w-lg">
              <div class="flex items-center justify-between p-4 border-b">
                <h3 class="text-lg font-semibold text-gray-900">记录维保</h3>
                <button @click="isMaintenanceDialogOpen = false" class="p-2 hover:bg-gray-100 rounded-lg">✕</button>
              </div>
              <div class="p-6 space-y-4">
                <div class="p-3 bg-gray-50 rounded-lg">
                  <p class="text-sm text-gray-500">模具编码</p>
                  <p class="font-medium text-gray-900">{{ selectedMoldForMaintenance.code }}</p>
                  <div class="mt-3 grid grid-cols-2 gap-3 text-sm">
                    <div><p class="text-gray-500">上次维保</p><p class="font-medium">{{ selectedMoldForMaintenance.lastMaintenanceDate }}</p></div>
                    <div><p class="text-gray-500">当前计划</p><p class="font-medium">{{ selectedMoldForMaintenance.nextMaintenanceDate }}</p></div>
                  </div>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">维保类型</label>
                  <select v-model="newMaintenanceRecord.maintenanceType" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
                    <option value="预防性维保">预防性维保</option>
                    <option value=" corrective_maintenance "> corrective_maintenance </option>
                    <option value="大修">大修</option>
                    <option value="报废前维保">报废前维保</option>
                  </select>
                </div>
                <div class="grid grid-cols-2 gap-4">
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">维保日期</label>
                    <input v-model="newMaintenanceRecord.maintenanceDate" type="date" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">下次维保日期 <span class="text-red-500">*</span></label>
                    <input v-model="newMaintenanceRecord.nextMaintenanceDate" type="date" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                  </div>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">维保费用</label>
                  <input v-model.number="newMaintenanceRecord.cost" type="number" min="0" placeholder="请输入维保费用" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">操作人员</label>
                  <input v-model="newMaintenanceRecord.operator" type="text" placeholder="请输入操作人员姓名" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">备注</label>
                  <textarea v-model="newMaintenanceRecord.notes" rows="2" placeholder="请输入备注信息" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"></textarea>
                </div>
              </div>
              <div class="flex justify-end gap-3 p-4 border-t bg-gray-50">
                <button @click="isMaintenanceDialogOpen = false" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">取消</button>
                <button @click="handleSaveMaintenanceRecord" class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">保存记录</button>
              </div>
            </div>
          </div>
        </Transition>
      </Teleport>

      <!-- 维保历史对话框 -->
      <Teleport to="body">
        <Transition name="modal">
          <div v-if="isMaintenanceHistoryDialogOpen && selectedMoldForMaintenance" class="fixed inset-0 z-50 flex items-center justify-center p-4">
            <div class="absolute inset-0 bg-black/50" @click="isMaintenanceHistoryDialogOpen = false" />
            <div class="relative bg-white rounded-xl shadow-xl w-full max-w-3xl max-h-[80vh] overflow-hidden">
              <div class="flex items-center justify-between p-4 border-b">
                <h3 class="text-lg font-semibold text-gray-900">维保历史 - {{ selectedMoldForMaintenance.code }}</h3>
                <button @click="isMaintenanceHistoryDialogOpen = false" class="p-2 hover:bg-gray-100 rounded-lg">✕</button>
              </div>
              <div class="p-6 overflow-y-auto max-h-[60vh]">
                <div class="mb-4 p-4 bg-gray-50 rounded-lg">
                  <div class="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                    <div><p class="text-gray-500">模具名称</p><p class="font-medium">{{ selectedMoldForMaintenance.name }}</p></div>
                    <div><p class="text-gray-500">维保次数</p><p class="font-medium">{{ getMoldMaintenanceRecords(selectedMoldForMaintenance.id).length }} 次</p></div>
                    <div><p class="text-gray-500">上次维保</p><p class="font-medium">{{ selectedMoldForMaintenance.lastMaintenanceDate }}</p></div>
                    <div><p class="text-gray-500">下次维保</p><p class="font-medium">{{ selectedMoldForMaintenance.nextMaintenanceDate }}</p></div>
                  </div>
                </div>
                <div v-if="getMoldMaintenanceRecords(selectedMoldForMaintenance.id).length === 0" class="text-center py-8 text-gray-500">
                  <Wrench class="w-12 h-12 mx-auto mb-3 opacity-50" />
                  <p>暂无维保记录</p>
                </div>
                <div v-else class="border border-gray-200 rounded-lg overflow-hidden">
                  <table class="w-full text-sm">
                    <thead class="bg-gray-50">
                      <tr>
                        <th class="text-left px-4 py-2 font-medium text-gray-700">维保日期</th>
                        <th class="text-left px-4 py-2 font-medium text-gray-700">维保类型</th>
                        <th class="text-right px-4 py-2 font-medium text-gray-700">费用</th>
                        <th class="text-left px-4 py-2 font-medium text-gray-700">下次维保</th>
                        <th class="text-left px-4 py-2 font-medium text-gray-700">操作人员</th>
                        <th class="text-left px-4 py-2 font-medium text-gray-700">备注</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="record in getMoldMaintenanceRecords(selectedMoldForMaintenance.id)" :key="record.id" class="border-t border-gray-200">
                        <td class="px-4 py-3 text-gray-900">{{ record.maintenanceDate }}</td>
                        <td class="px-4 py-3 text-gray-900">{{ record.maintenanceType }}</td>
                        <td class="px-4 py-3 text-right text-gray-900">¥{{ record.cost.toLocaleString() }}</td>
                        <td class="px-4 py-3 text-gray-900">{{ record.nextMaintenanceDate }}</td>
                        <td class="px-4 py-3 text-gray-900">{{ record.operator || '-' }}</td>
                        <td class="px-4 py-3 text-gray-600 text-xs">{{ record.notes || '-' }}</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
              <div class="flex justify-end gap-3 p-4 border-t bg-gray-50">
                <button @click="isMaintenanceHistoryDialogOpen = false" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">关闭</button>
              </div>
            </div>
          </div>
        </Transition>
      </Teleport>
      <!-- 使用记录历史对话框 -->
      <Teleport to="body">
        <Transition name="modal">
          <div v-if="isUsageHistoryDialogOpen && selectedMoldForHistory" class="fixed inset-0 z-50 flex items-center justify-center p-4">
            <div class="absolute inset-0 bg-black/50" @click="isUsageHistoryDialogOpen = false"></div>
            <div class="relative bg-white rounded-xl shadow-2xl w-full max-w-4xl max-h-[80vh] overflow-y-auto">
              <!-- 对话框内容保持不变 -->
              <div class="flex items-center justify-between p-4 border-b">
                <h3 class="text-lg font-semibold text-gray-900">使用历史 - {{ selectedMoldForHistory.code }}</h3>
                <button @click="isUsageHistoryDialogOpen = false" class="p-2 hover:bg-gray-100 rounded-lg">✕</button>
              </div>
              <div class="p-6 overflow-y-auto max-h-[60vh]">
                <div class="mb-4 p-4 bg-gray-50 rounded-lg">
                  <div class="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                    <div><p class="text-gray-500">模具名称</p><p class="font-medium">{{ selectedMoldForHistory.name }}</p></div>
                    <div><p class="text-gray-500">总使用模次</p><p class="font-medium">{{ selectedMoldForHistory.currentUsage.toLocaleString() }}</p></div>
                    <div><p class="text-gray-500">记录总数</p><p class="font-medium">{{ getTotalUsageRecords(selectedMoldForHistory.id) }} 条</p></div>
                    <div><p class="text-gray-500">剩余寿命</p><p class="font-medium" :class="getLifeColor(selectedMoldForHistory.remainingLife)">{{ selectedMoldForHistory.remainingLife }}%</p></div>
                  </div>
                </div>
                <div v-if="getMoldUsageRecords(selectedMoldForHistory.id).length === 0" class="text-center py-8 text-gray-500">
                  <History class="w-12 h-12 mx-auto mb-3 opacity-50" />
                  <p>暂无使用记录</p>
                </div>
                <div v-else class="border border-gray-200 rounded-lg overflow-hidden">
                  <table class="w-full text-sm">
                    <thead class="bg-gray-50">
                      <tr>
                        <th class="text-left px-4 py-2 font-medium text-gray-700">使用日期</th>
                        <th class="text-right px-4 py-2 font-medium text-gray-700">使用模次</th>
                        <th class="text-left px-4 py-2 font-medium text-gray-700">生产订单</th>
                        <th class="text-left px-4 py-2 font-medium text-gray-700">操作人员</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="record in getMoldUsageRecords(selectedMoldForHistory.id)" :key="record.id" class="border-t border-gray-200">
                        <td class="px-4 py-3 text-gray-900">{{ record.usageDate }}</td>
                        <td class="px-4 py-3 text-right font-medium text-gray-900">{{ record.usageCount.toLocaleString() }}</td>
                        <td class="px-4 py-3 text-gray-900">{{ record.productionOrder || '-' }}</td>
                        <td class="px-4 py-3 text-gray-900">{{ record.operator || '-' }}</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
              <div class="flex justify-end gap-3 p-4 border-t bg-gray-50">
                <button @click="isUsageHistoryDialogOpen = false" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">关闭</button>
              </div>
            </div>
          </div>
        </Transition>
      </Teleport>
      <!-- 维保记录对话框 -->
      <Teleport to="body">
        <Transition name="modal">
          <div v-if="isMaintenanceDialogOpen && selectedMold" class="fixed inset-0 z-50 flex items-center justify-center p-4">
            <div class="absolute inset-0 bg-black/50" @click="isMaintenanceDialogOpen = false"></div>
            <div class="relative bg-white rounded-xl shadow-2xl w-full max-w-lg max-h-[80vh] overflow-y-auto">
              <!-- 对话框内容保持不变 -->
              <div class="flex items-center justify-between p-4 border-b">
                <h3 class="text-lg font-semibold text-gray-900">添加维保记录</h3>
                <button @click="isMaintenanceDialogOpen = false" class="p-2 hover:bg-gray-100 rounded-lg">✕</button>
              </div>
              <div class="p-6">
                <form @submit.prevent="handleSaveMaintenanceRecord" class="space-y-4">
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">维保类型</label>
                    <select v-model="newMaintenanceRecord.maintenanceType" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" required>
                      <option value="">请选择维保类型</option>
                      <option value="日常保养">日常保养</option>
                      <option value="定期检修">定期检修</option>
                      <option value="大修">大修</option>
                      <option value="紧急维修">紧急维修</option>
                    </select>
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">维保日期</label>
                    <input v-model="newMaintenanceRecord.maintenanceDate" type="date" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" required>
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">费用 (元)</label>
                    <input v-model.number="newMaintenanceRecord.cost" type="number" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" required>
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">下次维保日期</label>
                    <input v-model="newMaintenanceRecord.nextMaintenanceDate" type="date" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" required>
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">操作人员</label>
                    <input v-model="newMaintenanceRecord.operator" type="text" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">备注</label>
                    <textarea v-model="newMaintenanceRecord.notes" rows="3" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"></textarea>
                  </div>
                  <div class="flex justify-end gap-3 pt-4">
                    <button type="button" @click="isMaintenanceDialogOpen = false" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">取消</button>
                    <button type="submit" class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">保存</button>
                  </div>
                </form>
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
