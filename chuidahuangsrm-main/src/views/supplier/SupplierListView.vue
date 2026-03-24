  <script setup lang="ts">
  import { ref, computed, onMounted } from 'vue'
  import { RouterLink } from 'vue-router'
  import { Search, Filter, Plus, AlertTriangle, LayoutGrid, List, CheckCircle, XCircle } from 'lucide-vue-next'
  import { mockSuppliers } from '@/data/mockData'
  import type { Supplier } from '@/types'
  import { useAppStore } from '@/stores/useAppStore'
  import ExportButton from '@/components/ExportButton.vue'

  // 导出配置
  const exportOptions = computed(() => ({
    title: '供应商列表',
    columns: [
      { key: 'code', label: '供应商编码' },
      { key: 'name', label: '供应商名称' },
      { key: 'category', label: '供应商分类' },
      { key: 'status', label: '状态' },
      { key: 'contact', label: '联系人' },
      { key: 'phone', label: '联系电话' },
      { key: 'email', label: '电子邮箱' },
      { key: 'riskLevel', label: '风险等级' },
      { key: 'annualSpend', label: '年度支出', formatter: (v: number) => `¥${v.toLocaleString()}` },
      { key: 'orderCount', label: '订单总数' },
      { key: 'onTimeRate', label: '准时交付率', formatter: (v: number) => `${v}%` },
      { key: 'qualityPassRate', label: '质量合格率', formatter: (v: number) => `${v}%` },
      { key: 'overallScore', label: '综合评分' },
    ],
    data: filteredSuppliers.value
  }))

  const searchQuery = ref('')
  const categoryFilter = ref('all')
  const riskFilter = ref('all')
  const viewMode = ref<'grid' | 'list'>('list')
  const isAddDialogOpen = ref(false)
  const isEvaluationDialogOpen = ref(false)
  const appStore = useAppStore()

  // 选中的供应商
  const selectedSupplier = ref<Supplier | null>(null)

  // 表单验证错误
  const formErrors = ref<Record<string, string>>({})

  // 评价表单
  const evaluationForm = ref({
    qualityScore: 85,
    deliveryScore: 85,
    costScore: 85,
    innovationScore: 80,
    evaluationDate: new Date().toISOString().split('T')[0],
    evaluator: '',
    comments: '',
  })

  const newSupplier = ref({
    name: '',
    code: '',
    category: '常规',
    contact: '',
    phone: '',
    email: '',
    address: '',
  })

  // 供应商列表，优先从 localStorage 读取
  const suppliers = ref<Supplier[]>([])

  // 初始化供应商数据
  onMounted(() => {
    const saved = localStorage.getItem('suppliers')
    if (saved) {
      try {
        suppliers.value = JSON.parse(saved)
      } catch {
        suppliers.value = [...mockSuppliers]
      }
    } else {
      suppliers.value = [...mockSuppliers]
    }
  })

  const filteredSuppliers = computed(() =>
    suppliers.value.filter((supplier) => {
      const matchesSearch =
        supplier.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
        supplier.code.toLowerCase().includes(searchQuery.value.toLowerCase())
      const matchesCategory =
        categoryFilter.value === 'all' || supplier.category === categoryFilter.value
      const matchesRisk = riskFilter.value === 'all' || supplier.riskLevel === riskFilter.value
      return matchesSearch && matchesCategory && matchesRisk
    })
  )

  const totalAnnualSpend = computed(() =>
    suppliers.value.reduce((sum, s) => sum + s.annualSpend, 0)
  )

  function getRiskColor(level: string) {
    switch (level) {
      case '高': return 'bg-red-100 text-red-800'
      case '中': return 'bg-yellow-100 text-yellow-800'
      case '低': return 'bg-green-100 text-green-800'
      default: return 'bg-gray-100 text-gray-800'
    }
  }

  function getCategoryVariant(category: string) {
    switch (category) {
      case '战略': return 'bg-blue-600 text-white'
      case '关键': return 'bg-gray-200 text-gray-800'
      default: return 'border border-gray-300 text-gray-700'
    }
  }

  function handleAddSupplier() {
    // 清空之前的错误
    formErrors.value = {}

    // 表单验证
    if (!newSupplier.value.name.trim()) {
      formErrors.value.name = '请输入供应商名称'
      return
    }
    if (!newSupplier.value.code.trim()) {
      formErrors.value.code = '请输入供应商编码'
      return
    }
    if (!newSupplier.value.contact.trim()) {
      formErrors.value.contact = '请输入联系人'
      return
    }
    if (!newSupplier.value.phone.trim()) {
      formErrors.value.phone = '请输入联系电话'
      return
    }

    // 创建新供应商对象
    const supplier: Supplier = {
      id: `S${String(suppliers.value.length + 1).padStart(3, '0')}`,
      name: newSupplier.value.name.trim(),
      code: newSupplier.value.code.trim(),
      category: newSupplier.value.category,
      status: '合格',
      riskLevel: '低',
      riskScore: Math.floor(Math.random() * 30) + 10,
      qualityScore: Math.floor(Math.random() * 15) + 85,
      deliveryScore: Math.floor(Math.random() * 15) + 85,
      costScore: Math.floor(Math.random() * 15) + 85,
      innovationScore: Math.floor(Math.random() * 20) + 75,
      overallScore: 0,
      contact: newSupplier.value.contact.trim(),
      phone: newSupplier.value.phone.trim(),
      email: newSupplier.value.email.trim(),
      address: newSupplier.value.address.trim(),
      certifications: [],
      annualSpend: 0,
      orderCount: 0,
      onTimeRate: 0,
      qualityPassRate: 0,
    }
    supplier.overallScore = (supplier.qualityScore + supplier.deliveryScore + supplier.costScore + supplier.innovationScore) / 4

    // 添加到列表
    suppliers.value.push(supplier)

    // 保存到 localStorage
    localStorage.setItem('suppliers', JSON.stringify(suppliers.value))

    // 显示成功提示
    appStore.showToast('供应商添加成功', 'success')

    // 关闭对话框并重置表单
    isAddDialogOpen.value = false
    newSupplier.value = { name: '', code: '', category: '常规', contact: '', phone: '', email: '', address: '' }
    formErrors.value = {}
  }

  function openAddDialog() {
    formErrors.value = {}
    isAddDialogOpen.value = true
  }

  // 打开绩效评价对话框
  function openEvaluationDialog(supplier: Supplier) {
    selectedSupplier.value = supplier
    evaluationForm.value = {
      qualityScore: supplier.qualityScore,
      deliveryScore: supplier.deliveryScore,
      costScore: supplier.costScore,
      innovationScore: supplier.innovationScore,
      evaluationDate: new Date().toISOString().split('T')[0],
      evaluator: '',
      comments: '',
    }
    isEvaluationDialogOpen.value = true
  }

  // 提交绩效评价
  function handleSubmitEvaluation() {
    if (!selectedSupplier.value) return

    const idx = suppliers.value.findIndex(s => s.id === selectedSupplier.value!.id)
    if (idx > -1) {
      suppliers.value[idx].qualityScore = evaluationForm.value.qualityScore
      suppliers.value[idx].deliveryScore = evaluationForm.value.deliveryScore
      suppliers.value[idx].costScore = evaluationForm.value.costScore
      suppliers.value[idx].innovationScore = evaluationForm.value.innovationScore
      suppliers.value[idx].overallScore = Math.round(
        (evaluationForm.value.qualityScore + evaluationForm.value.deliveryScore + evaluationForm.value.costScore + evaluationForm.value.innovationScore) / 4
      )

      // 保存评价记录
      const evaluationRecord = {
        id: `EVAL${Date.now()}`,
        supplierId: selectedSupplier.value.id,
        supplierName: selectedSupplier.value.name,
        ...evaluationForm.value,
        overallScore: suppliers.value[idx].overallScore,
        createdAt: new Date().toISOString(),
      }

      const saved = localStorage.getItem('supplier_evaluations')
      let evaluations = saved ? JSON.parse(saved) : []
      evaluations.push(evaluationRecord)
      localStorage.setItem('supplier_evaluations', JSON.stringify(evaluations))

      localStorage.setItem('suppliers', JSON.stringify(suppliers.value))
      appStore.showToast('绩效评价已保存', 'success')
      isEvaluationDialogOpen.value = false
    }
  }
  </script>

  <template>
    <div class="space-y-6">
      <!-- 页面标题 -->
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-semibold text-gray-900">{{ appStore.t('page.suppliers.title') }}</h1>
          <p class="text-sm text-gray-500 mt-1">供应商全生命周期管理 · 360°画像</p>
        </div>
        <div class="flex items-center gap-3">
          <ExportButton :export-options="exportOptions" filename="suppliers" />
          <button
            class="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700 transition-colors"
            @click="openAddDialog"
          >
            <Plus class="w-4 h-4" />
            {{ appStore.t('btn.addSupplier') }}
          </button>
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div class="bg-white rounded-lg border border-gray-200 p-6 text-center">
          <p class="text-3xl font-semibold text-gray-900">{{ suppliers.length }}</p>
          <p class="text-sm text-gray-500 mt-1">{{ appStore.t('supplier.total') }}</p>
        </div>
        <div class="bg-white rounded-lg border border-gray-200 p-6 text-center">
          <p class="text-3xl font-semibold text-blue-600">
            {{ suppliers.filter((s) => s.category === '战略').length }}
          </p>
          <p class="text-sm text-gray-500 mt-1">{{ appStore.t('supplier.strategic') }}</p>
        </div>
        <div class="bg-white rounded-lg border border-gray-200 p-6 text-center">
          <p class="text-3xl font-semibold text-green-600">
            {{ suppliers.filter((s) => s.riskLevel === '低').length }}
          </p>
          <p class="text-sm text-gray-500 mt-1">{{ appStore.t('supplier.lowRisk') }}</p>
        </div>
        <div class="bg-white rounded-lg border border-gray-200 p-6 text-center">
          <p class="text-3xl font-semibold text-orange-600">
            {{ totalAnnualSpend.toLocaleString() }}
          </p>
          <p class="text-sm text-gray-500 mt-1">{{ appStore.t('supplier.annualSpendYuan') }}</p>
        </div>
      </div>

      <!-- 搜索和筛选 -->
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex flex-col md:flex-row gap-4">
          <div class="flex-1 relative">
            <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
            <input
              v-model="searchQuery"
              :placeholder="appStore.t('supplier.searchPlaceholder')"
              class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          <select
            v-model="categoryFilter"
            class="w-full md:w-40 px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="all">{{ appStore.t('supplier.allCategory') }}</option>
            <option value="战略">战略</option>
            <option value="关键">关键</option>
            <option value="杠杆">杠杆</option>
            <option value="常规">常规</option>
          </select>
          <select
            v-model="riskFilter"
            class="w-full md:w-40 px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="all">{{ appStore.t('supplier.allRisk') }}</option>
            <option value="低">低风险</option>
            <option value="中">中风险</option>
            <option value="高">高风险</option>
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

      <!-- 供应商列表 -->
      <div v-if="viewMode === 'grid'" class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div
          v-for="supplier in filteredSuppliers"
          :key="supplier.id"
          class="bg-white rounded-lg border border-gray-200 p-6 hover:shadow-lg transition-shadow"
        >
          <div class="space-y-4">
            <!-- 头部信息 -->
            <div class="flex items-start justify-between">
              <div class="flex-1">
                <div class="flex items-center gap-2 mb-2">
                  <h3 class="font-semibold text-gray-900">{{ supplier.name }}</h3>
                  <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="getCategoryVariant(supplier.category)">
                    {{ supplier.category }}
                  </span>
                </div>
                <p class="text-sm text-gray-500">编码: {{ supplier.code }}</p>
              </div>
              <div class="px-3 py-1 rounded-full text-xs font-medium" :class="getRiskColor(supplier.riskLevel)">
                {{ supplier.riskLevel }}风险
              </div>
            </div>

            <!-- 绩效指标 -->
            <div class="grid grid-cols-4 gap-4 p-4 bg-gray-50 rounded-lg">
              <div class="text-center">
                <p class="text-lg font-semibold text-gray-900">{{ supplier.qualityScore }}</p>
                <p class="text-xs text-gray-500 mt-1">质量</p>
              </div>
              <div class="text-center">
                <p class="text-lg font-semibold text-gray-900">{{ supplier.deliveryScore }}</p>
                <p class="text-xs text-gray-500 mt-1">交付</p>
              </div>
              <div class="text-center">
                <p class="text-lg font-semibold text-gray-900">{{ supplier.costScore }}</p>
                <p class="text-xs text-gray-500 mt-1">成本</p>
              </div>
              <div class="text-center">
                <p class="text-lg font-semibold text-gray-900">{{ supplier.innovationScore }}</p>
                <p class="text-xs text-gray-500 mt-1">创新</p>
              </div>
            </div>

            <!-- 关键数据 -->
            <div class="grid grid-cols-3 gap-4 text-sm">
              <div>
                <p class="text-gray-500">年度支出</p>
                <p class="font-medium text-gray-900 mt-1">¥{{ (supplier.annualSpend / 10000).toFixed(1) }}万</p>
              </div>
              <div>
                <p class="text-gray-500">订单数量</p>
                <p class="font-medium text-gray-900 mt-1">{{ supplier.orderCount }}单</p>
              </div>
              <div>
                <p class="text-gray-500">准时率</p>
                <p class="font-medium text-gray-900 mt-1">{{ supplier.onTimeRate }}%</p>
              </div>
            </div>

            <!-- 资质预警 -->
            <div
              v-if="supplier.certifications.some((c) => c.status === '即将到期')"
              class="flex items-center gap-2 p-3 bg-orange-50 rounded-lg"
            >
              <AlertTriangle class="w-4 h-4 text-orange-600" />
              <span class="text-sm text-orange-800">有资质证书即将到期，请及时更新</span>
            </div>

            <!-- 操作按钮 -->
            <div class="flex gap-2 pt-2">
              <RouterLink :to="`/suppliers/${supplier.id}`" class="flex-1">
                <button class="w-full px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50 transition-colors">
                  {{ appStore.t('btn.viewDetail') }}
                </button>
              </RouterLink>
              <button @click="openEvaluationDialog(supplier)" class="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50 transition-colors">
                {{ appStore.t('btn.performanceReview') }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="bg-white rounded-lg border border-gray-200 overflow-x-auto">
        <table class="w-full min-w-max">
          <thead class="bg-gray-50">
            <tr>
              <th class="text-left px-4 py-3 text-sm font-medium text-gray-700">{{ appStore.t('supplier.table.name') }}</th>
              <th class="text-left px-4 py-3 text-sm font-medium text-gray-700">{{ appStore.t('supplier.table.code') }}</th>
              <th class="text-left px-4 py-3 text-sm font-medium text-gray-700">{{ appStore.t('supplier.table.category') }}</th>
              <th class="text-left px-4 py-3 text-sm font-medium text-gray-700">{{ appStore.t('supplier.table.risk') }}</th>
              <th class="text-right px-4 py-3 text-sm font-medium text-gray-700">{{ appStore.t('supplier.table.annualSpend') }}</th>
              <th class="text-right px-4 py-3 text-sm font-medium text-gray-700">{{ appStore.t('supplier.table.orderCount') }}</th>
              <th class="text-right px-4 py-3 text-sm font-medium text-gray-700">{{ appStore.t('supplier.table.onTimeRate') }}</th>
              <th class="text-left px-4 py-3 text-sm font-medium text-gray-700">{{ appStore.t('common.action') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="supplier in filteredSuppliers" :key="supplier.id" class="border-t border-gray-200">
              <td class="px-4 py-3 text-sm font-medium text-gray-900">{{ supplier.name }}</td>
              <td class="px-4 py-3 text-sm text-gray-900">{{ supplier.code }}</td>
              <td class="px-4 py-3 text-sm">
                <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="getCategoryVariant(supplier.category)">
                  {{ supplier.category }}
                </span>
              </td>
              <td class="px-4 py-3 text-sm">
                <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="getRiskColor(supplier.riskLevel)">
                  {{ supplier.riskLevel }}风险
                </span>
              </td>
              <td class="px-4 py-3 text-sm text-right text-gray-900">¥{{ supplier.annualSpend.toLocaleString() }}</td>
              <td class="px-4 py-3 text-sm text-right text-gray-900">{{ supplier.orderCount }}</td>
              <td class="px-4 py-3 text-sm text-right text-gray-900">{{ supplier.onTimeRate }}%</td>
              <td class="px-4 py-3 text-sm">
                <div class="flex gap-2">
                  <RouterLink :to="`/suppliers/${supplier.id}`">
                    <button class="px-3 py-1.5 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">{{ appStore.t('btn.viewDetail') }}</button>
                  </RouterLink>
                  <button @click="openEvaluationDialog(supplier)" class="px-3 py-1.5 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">{{ appStore.t('btn.performanceReview') }}</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 空状态 -->
      <div v-if="filteredSuppliers.length === 0" class="bg-white rounded-lg border border-gray-200 py-12">
        <div class="text-center text-gray-500">
          <Filter class="w-12 h-12 mx-auto mb-4 opacity-50" />
          <p>{{ appStore.t('common.noneFound') }}</p>
        </div>
      </div>

      <!-- 新增供应商弹窗 -->
      <Teleport to="body">
        <Transition name="modal">
          <div v-if="isAddDialogOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4">
            <div class="absolute inset-0 bg-black/50" @click="isAddDialogOpen = false" />
            <div class="relative bg-white rounded-xl shadow-xl w-full max-w-md p-6">
              <h3 class="text-lg font-semibold text-gray-900 mb-4">新增供应商</h3>
              <div class="space-y-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">供应商名称 <span class="text-red-500">*</span></label>
                  <input
                    v-model="newSupplier.name"
                    placeholder="请输入供应商名称"
                    :class="['w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2', formErrors.name ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500']"
                  />
                  <p v-if="formErrors.name" class="text-red-500 text-xs mt-1 flex items-center gap-1">
                    <XCircle class="w-3 h-3" />{{ formErrors.name }}
                  </p>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">供应商编码 <span class="text-red-500">*</span></label>
                  <input
                    v-model="newSupplier.code"
                    placeholder="请输入供应商编码"
                    :class="['w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2', formErrors.code ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500']"
                  />
                  <p v-if="formErrors.code" class="text-red-500 text-xs mt-1 flex items-center gap-1">
                    <XCircle class="w-3 h-3" />{{ formErrors.code }}
                  </p>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">供应商分类</label>
                  <select v-model="newSupplier.category" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
                    <option value="战略">战略</option>
                    <option value="关键">关键</option>
                    <option value="杠杆">杠杆</option>
                    <option value="常规">常规</option>
                  </select>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">联系人 <span class="text-red-500">*</span></label>
                  <input
                    v-model="newSupplier.contact"
                    placeholder="请输入联系人姓名"
                    :class="['w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2', formErrors.contact ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500']"
                  />
                  <p v-if="formErrors.contact" class="text-red-500 text-xs mt-1 flex items-center gap-1">
                    <XCircle class="w-3 h-3" />{{ formErrors.contact }}
                  </p>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">联系电话 <span class="text-red-500">*</span></label>
                  <input
                    v-model="newSupplier.phone"
                    placeholder="请输入联系电话"
                    :class="['w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2', formErrors.phone ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500']"
                  />
                  <p v-if="formErrors.phone" class="text-red-500 text-xs mt-1 flex items-center gap-1">
                    <XCircle class="w-3 h-3" />{{ formErrors.phone }}
                  </p>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">联系邮箱</label>
                  <input v-model="newSupplier.email" placeholder="请输入联系邮箱" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">地址</label>
                  <input v-model="newSupplier.address" placeholder="请输入地址" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>
              </div>
              <div class="flex justify-end gap-3 mt-6">
                <button class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50" @click="isAddDialogOpen = false">取消</button>
                <button class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700" @click="handleAddSupplier">保存</button>
              </div>
            </div>
          </div>
        </Transition>
      </Teleport>

      <!-- 绩效评价对话框 -->
      <Teleport to="body">
        <Transition name="modal">
          <div v-if="isEvaluationDialogOpen && selectedSupplier" class="fixed inset-0 z-50 flex items-center justify-center p-4">
            <div class="absolute inset-0 bg-black/50" @click="isEvaluationDialogOpen = false" />
            <div class="relative bg-white rounded-xl shadow-xl w-full max-w-lg">
              <div class="flex items-center justify-between p-4 border-b">
                <h3 class="text-lg font-semibold text-gray-900">绩效评价 - {{ selectedSupplier.name }}</h3>
                <button @click="isEvaluationDialogOpen = false" class="p-2 hover:bg-gray-100 rounded-lg">✕</button>
              </div>
              <div class="p-6 space-y-4">
                <div class="p-3 bg-gray-50 rounded-lg">
                  <div class="grid grid-cols-2 gap-3 text-sm">
                    <div><p class="text-gray-500">供应商编码</p><p class="font-medium">{{ selectedSupplier.code }}</p></div>
                    <div><p class="text-gray-500">当前综合评分</p><p class="font-medium">{{ selectedSupplier.overallScore }}</p></div>
                  </div>
                </div>
                <div class="space-y-3">
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">质量评分 (0-100)</label>
                    <input v-model.number="evaluationForm.qualityScore" type="number" min="0" max="100" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">交付评分 (0-100)</label>
                    <input v-model.number="evaluationForm.deliveryScore" type="number" min="0" max="100" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">成本评分 (0-100)</label>
                    <input v-model.number="evaluationForm.costScore" type="number" min="0" max="100" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">创新评分 (0-100)</label>
                    <input v-model.number="evaluationForm.innovationScore" type="number" min="0" max="100" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                  </div>
                </div>
                <div class="p-3 bg-blue-50 rounded-lg">
                  <div class="flex items-center justify-between">
                    <span class="text-sm font-medium text-gray-700">综合评分</span>
                    <span class="text-xl font-bold text-blue-600">{{ Math.round((evaluationForm.qualityScore + evaluationForm.deliveryScore + evaluationForm.costScore + evaluationForm.innovationScore) / 4) }}</span>
                  </div>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">评价日期</label>
                  <input v-model="evaluationForm.evaluationDate" type="date" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">评价人</label>
                  <input v-model="evaluationForm.evaluator" type="text" placeholder="请输入评价人姓名" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">评价意见</label>
                  <textarea v-model="evaluationForm.comments" rows="2" placeholder="请输入评价意见" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"></textarea>
                </div>
              </div>
              <div class="flex justify-end gap-3 p-4 border-t bg-gray-50">
                <button @click="isEvaluationDialogOpen = false" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">取消</button>
                <button @click="handleSubmitEvaluation" class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">提交评价</button>
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
