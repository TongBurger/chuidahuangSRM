  <script setup lang="ts">
  import { ref, computed, onMounted } from 'vue'
  import { TrendingUp, DollarSign, FileText, Target, LayoutGrid, List, Plus, XCircle, Check, Eye, Award } from 'lucide-vue-next'
  import { useAppStore } from '@/stores/useAppStore'

  const activeTab = ref('projects')
  const viewMode = ref<'grid' | 'list'>('list')
  const appStore = useAppStore()

  // 对话框状态
  const isCreateDialogOpen = ref(false)
  const isQuoteDetailDialogOpen = ref(false)
  const isEvaluationDialogOpen = ref(false)
  const formErrors = ref<Record<string, string>>({})

  // 选中的项目
  const selectedProject = ref<any>(null)

  // 供应商列表
  const suppliers = ref<any[]>([])

  // 寻源项目列表
  const projects = ref<any[]>([])

  // 报价数据类型
  interface Quotation {
    id: string
    projectId: string
    supplierId: string
    supplierName: string
    unitPrice: number
    totalPrice: number
    quantity: number
    deliveryTime: string
    paymentTerms: string
    submitDate: string
    status: 'pending' | 'reviewed' | 'accepted' | 'rejected'
  }

  // 评标打分类型
  interface EvaluationScore {
    supplierId: string
    supplierName: string
    priceScore: number
    qualityScore: number
    deliveryScore: number
    serviceScore: number
    totalScore: number
    notes: string
  }

  // 获取项目的报价列表
  function getProjectQuotations(projectId: string): Quotation[] {
    const saved = localStorage.getItem('sourcing_quotations')
    if (saved) {
      try {
        const allQuotes = JSON.parse(saved) as Quotation[]
        return allQuotes.filter(q => q.projectId === projectId)
      } catch {
        return []
      }
    }
    return []
  }

  // 打开报价详情对话框
  function openQuoteDetailDialog(project: any) {
    selectedProject.value = project
    isQuoteDetailDialogOpen.value = true
  }

  // 评标表单
  const evaluationForm = ref({
    scores: [] as EvaluationScore[],
  })

  // 打开评标对话框
  function openEvaluationDialog(project: any) {
    selectedProject.value = project
    const quotations = getProjectQuotations(project.id)
    evaluationForm.value.scores = quotations.map(q => ({
      supplierId: q.supplierId,
      supplierName: q.supplierName,
      priceScore: 0,
      qualityScore: 0,
      deliveryScore: 0,
      serviceScore: 0,
      totalScore: 0,
      notes: '',
    }))
    isEvaluationDialogOpen.value = true
  }

  // 计算总分
  function calculateTotalScore(score: EvaluationScore) {
    return Math.round((score.priceScore + score.qualityScore + score.deliveryScore + score.serviceScore) / 4)
  }

  // 提交评标结果
  function handleSubmitEvaluation() {
    appStore.showToast('评标结果已保存', 'success')
    isEvaluationDialogOpen.value = false
  }

  // 定标功能
  function handleFinalizeAward(projectId: any, supplierId: string) {
    appStore.showToast('定标成功', 'success')
  }

  // 初始化数据
  onMounted(() => {
    // 获取供应商列表
    const savedSuppliers = localStorage.getItem('suppliers')
    if (savedSuppliers) {
      try {
        suppliers.value = JSON.parse(savedSuppliers)
      } catch {
        suppliers.value = []
      }
    }

    // 获取寻源项目列表
    const savedProjects = localStorage.getItem('sourcingProjects')
    if (savedProjects) {
      try {
        projects.value = JSON.parse(savedProjects)
      } catch {
        projects.value = [
          { id: 'SP001', name: '2026年Q2铝合金锅体采购', category: '原材料', status: '询价中', targetAmount: 3500000, currentBestPrice: 2850000, savingRate: 18.6, supplierCount: 5, deadline: '2026-03-25' },
          { id: 'SP002', name: '不粘涂层供应商寻源', category: '辅料', status: '评估中', targetAmount: 1200000, currentBestPrice: 1050000, savingRate: 12.5, supplierCount: 3, deadline: '2026-03-30' },
        ]
      }
    } else {
      projects.value = [
        { id: 'SP001', name: '2026年Q2铝合金锅体采购', category: '原材料', status: '询价中', targetAmount: 3500000, currentBestPrice: 2850000, savingRate: 18.6, supplierCount: 5, deadline: '2026-03-25' },
        { id: 'SP002', name: '不粘涂层供应商寻源', category: '辅料', status: '评估中', targetAmount: 1200000, currentBestPrice: 1050000, savingRate: 12.5, supplierCount: 3, deadline: '2026-03-30' },
      ]
    }
  })

  // 计算属性
  const mockSourcingProjects = computed(() => projects.value)

  const mockQuotations = [
    { supplier: '深圳市精诚模具制造有限公司', materialName: 'ADC12铝合金锭', quantity: 50000, unit: 'KG', unitPrice: 57, totalPrice: 2850000, deliveryTime: '30天', paymentTerms: '月结60天' },
    { supplier: '佛山市永盛铝制品有限公司', materialName: 'ADC12铝合金锭', quantity: 50000, unit: 'KG', unitPrice: 59, totalPrice: 2950000, deliveryTime: '45天', paymentTerms: '月结30天' },
  ]

  const costItems = [
    { item: '原材料成本', amount: 45, unit: '元/KG', percentage: 78.9 },
    { item: '加工成本', amount: 8, unit: '元/KG', percentage: 14.0 },
    { item: '物流费用', amount: 2.5, unit: '元/KG', percentage: 4.4 },
    { item: '管理费用', amount: 1.5, unit: '元/KG', percentage: 2.6 },
  ]

  // 新寻源项目表单数据
  const newProject = ref({
    name: '',
    category: '原材料',
    targetAmount: 0,
    deadline: '',
    invitedSuppliers: [] as string[],
  })

  function openCreateDialog() {
    formErrors.value = {}
    newProject.value = {
      name: '',
      category: '原材料',
      targetAmount: 0,
      deadline: '',
      invitedSuppliers: [],
    }
    isCreateDialogOpen.value = true
  }

  function toggleSupplier(supplierId: string) {
    const index = newProject.value.invitedSuppliers.indexOf(supplierId)
    if (index > -1) {
      newProject.value.invitedSuppliers.splice(index, 1)
    } else {
      newProject.value.invitedSuppliers.push(supplierId)
    }
  }

  function handleCreateProject() {
    // 清空之前的错误
    formErrors.value = {}

    // 表单验证
    if (!newProject.value.name.trim()) {
      formErrors.value.name = '请输入项目名称'
      return
    }
    if (newProject.value.targetAmount <= 0) {
      formErrors.value.targetAmount = '请输入有效的目标金额'
      return
    }
    if (!newProject.value.deadline) {
      formErrors.value.deadline = '请选择截止日期'
      return
    }

    // 创建新项目对象
    const project = {
      id: `SP${String(projects.value.length + 1).padStart(3, '0')}`,
      name: newProject.value.name.trim(),
      category: newProject.value.category,
      status: '询价中',
      targetAmount: newProject.value.targetAmount,
      currentBestPrice: 0,
      savingRate: 0,
      supplierCount: newProject.value.invitedSuppliers.length,
      deadline: newProject.value.deadline,
    }

    // 添加到列表
    projects.value.push(project)

    // 保存到 localStorage
    localStorage.setItem('sourcingProjects', JSON.stringify(projects.value))

    // 显示成功提示
    appStore.showToast('寻源项目创建成功', 'success')

    // 关闭对话框
    isCreateDialogOpen.value = false
  }
  </script>

  <template>
    <div class="space-y-6">
      <!-- 标题 -->
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-semibold text-gray-900">{{ appStore.t('page.sourcing.title') }}</h1>
          <p class="text-sm text-gray-500 mt-1">精细化核价 · 阳光采购 · 成本优化</p>
        </div>
        <button class="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700" @click="openCreateDialog">
          <TrendingUp class="w-4 h-4" />{{ appStore.t('btn.createSourcingProject') }}
        </button>
      </div>

      <!-- 统计卡片 -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div class="bg-white rounded-lg border border-gray-200 p-6">
          <div class="flex items-center justify-between"><div><p class="text-sm text-gray-500">{{ appStore.t('sourcing.kpi.activeProjects') }}</p><p class="text-2xl font-semibold text-gray-900 mt-1">2</p></div><div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center"><Target class="w-6 h-6 text-blue-600" /></div></div>
        </div>
        <div class="bg-white rounded-lg border border-gray-200 p-6">
          <div class="flex items-center justify-between"><div><p class="text-sm text-gray-500">{{ appStore.t('sourcing.kpi.suppliers') }}</p><p class="text-2xl font-semibold text-gray-900 mt-1">8</p></div><div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center"><TrendingUp class="w-6 h-6 text-green-600" /></div></div>
        </div>
        <div class="bg-white rounded-lg border border-gray-200 p-6">
          <div class="flex items-center justify-between"><div><p class="text-sm text-gray-500">{{ appStore.t('sourcing.kpi.targetAmount') }}</p><p class="text-2xl font-semibold text-gray-900 mt-1">470万</p></div><div class="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center"><DollarSign class="w-6 h-6 text-purple-600" /></div></div>
        </div>
        <div class="bg-white rounded-lg border border-gray-200 p-6">
          <div class="flex items-center justify-between"><div><p class="text-sm text-gray-500">{{ appStore.t('sourcing.kpi.avgSaving') }}</p><p class="text-2xl font-semibold text-green-600 mt-1">15.6%</p></div><div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center"><TrendingUp class="w-6 h-6 text-green-600" /></div></div>
        </div>
      </div>

      <!-- 选项卡 -->
      <div class="bg-white rounded-lg border border-gray-200">
        <div class="flex border-b border-gray-200">
          <button v-for="tab in [{ id: 'projects', label: '寻源项目' }, { id: 'quotations', label: '报价对比' }, { id: 'cost-analysis', label: '成本分析' }]" :key="tab.id" class="px-6 py-3 text-sm font-medium transition-colors" :class="activeTab === tab.id ? 'border-b-2 border-blue-600 text-blue-600' : 'text-gray-600 hover:text-gray-900'" @click="activeTab = tab.id">{{ tab.label }}</button>
        </div>

        <div class="p-6">
          <!-- 寻源项目 -->
          <div v-if="activeTab === 'projects'" class="space-y-4">
            <div class="flex justify-end">
              <div class="flex items-center border border-gray-300 rounded-lg p-1">
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
            <div v-if="viewMode === 'grid'" v-for="project in mockSourcingProjects" :key="project.id" class="border border-gray-200 rounded-lg p-6">
              <div class="flex items-start justify-between mb-4">
                <div class="flex-1">
                  <div class="flex items-center gap-3 mb-2">
                    <h3 class="font-semibold text-gray-900">{{ project.name }}</h3>
                    <span class="px-2 py-0.5 bg-blue-600 text-white rounded-full text-xs font-medium">{{ project.status }}</span>
                    <span class="px-2 py-0.5 border border-gray-300 text-gray-700 rounded-full text-xs font-medium">{{ project.category }}</span>
                  </div>
                  <p class="text-sm text-gray-500">项目编号: {{ project.id }}</p>
                </div>
                <div class="text-right"><p class="text-xs text-gray-500">截止日期</p><p class="font-medium text-gray-900">{{ project.deadline }}</p></div>
              </div>
              <div class="grid grid-cols-2 md:grid-cols-5 gap-4 mb-4">
                <div class="p-3 bg-gray-50 rounded-lg"><p class="text-xs text-gray-600">目标金额</p><p class="text-lg font-semibold text-gray-900 mt-1">¥{{ (project.targetAmount / 10000).toFixed(1) }}万</p></div>
                <div class="p-3 bg-blue-50 rounded-lg"><p class="text-xs text-gray-600">最优报价</p><p class="text-lg font-semibold text-blue-600 mt-1">¥{{ (project.currentBestPrice / 10000).toFixed(1) }}万</p></div>
                <div class="p-3 bg-green-50 rounded-lg"><p class="text-xs text-gray-600">节约率</p><p class="text-lg font-semibold text-green-600 mt-1">{{ project.savingRate }}%</p></div>
                <div class="p-3 bg-purple-50 rounded-lg"><p class="text-xs text-gray-600">参与供应商</p><p class="text-lg font-semibold text-purple-600 mt-1">{{ project.supplierCount }}家</p></div>
                <div class="p-3 bg-orange-50 rounded-lg"><p class="text-xs text-gray-600">预计节约</p><p class="text-lg font-semibold text-orange-600 mt-1">¥{{ ((project.targetAmount - project.currentBestPrice) / 10000).toFixed(1) }}万</p></div>
              </div>
              <div class="flex gap-2">
                <button class="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">{{ appStore.t('btn.viewDetail') }}</button>
                <button @click="openQuoteDetailDialog(project)" class="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50 flex items-center justify-center gap-1">
                  <Eye class="w-4 h-4" />{{ appStore.t('btn.quoteComparison') }}
                </button>
                <button @click="openEvaluationDialog(project)" class="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50 flex items-center justify-center gap-1">
                  <Award class="w-4 h-4" />评标
                </button>
                <button @click="handleFinalizeAward(project, '')" class="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">{{ appStore.t('btn.finalizeAward') }}</button>
              </div>
            </div>
            <div v-else class="border border-gray-200 rounded-lg overflow-x-auto">
              <table class="w-full min-w-max text-sm">
                <thead class="bg-gray-50">
                  <tr>
                    <th class="text-left px-4 py-3 font-medium text-gray-700">{{ appStore.t('sourcing.table.id') }}</th>
                    <th class="text-left px-4 py-3 font-medium text-gray-700">{{ appStore.t('sourcing.table.name') }}</th>
                    <th class="text-left px-4 py-3 font-medium text-gray-700">{{ appStore.t('sourcing.table.category') }}</th>
                    <th class="text-left px-4 py-3 font-medium text-gray-700">{{ appStore.t('sourcing.table.status') }}</th>
                    <th class="text-right px-4 py-3 font-medium text-gray-700">{{ appStore.t('sourcing.table.targetAmount') }}</th>
                    <th class="text-right px-4 py-3 font-medium text-gray-700">{{ appStore.t('sourcing.table.bestPrice') }}</th>
                    <th class="text-right px-4 py-3 font-medium text-gray-700">{{ appStore.t('sourcing.table.savingRate') }}</th>
                    <th class="text-left px-4 py-3 font-medium text-gray-700">{{ appStore.t('sourcing.table.deadline') }}</th>
                    <th class="text-left px-4 py-3 font-medium text-gray-700">{{ appStore.t('common.action') }}</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="project in mockSourcingProjects" :key="project.id" class="border-t border-gray-200">
                    <td class="px-4 py-3 text-gray-900">{{ project.id }}</td>
                    <td class="px-4 py-3 text-gray-900 font-medium">{{ project.name }}</td>
                    <td class="px-4 py-3 text-gray-900">{{ project.category }}</td>
                    <td class="px-4 py-3">
                      <span class="px-2 py-0.5 bg-blue-600 text-white rounded-full text-xs font-medium">{{ project.status }}</span>
                    </td>
                    <td class="px-4 py-3 text-right text-gray-900">¥{{ project.targetAmount.toLocaleString() }}</td>
                    <td class="px-4 py-3 text-right text-blue-600 font-medium">¥{{ project.currentBestPrice.toLocaleString() }}</td>
                    <td class="px-4 py-3 text-right text-green-600 font-medium">{{ project.savingRate }}%</td>
                    <td class="px-4 py-3 text-gray-900">{{ project.deadline }}</td>
                    <td class="px-4 py-3">
                      <div class="flex gap-2">
                        <button class="px-3 py-1.5 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">{{ appStore.t('btn.viewDetail') }}</button>
                        <button class="px-3 py-1.5 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">{{ appStore.t('btn.quoteComparison') }}</button>
                        <button class="px-3 py-1.5 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">{{ appStore.t('btn.finalizeAward') }}</button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- 报价对比 -->
          <div v-if="activeTab === 'quotations'">
            <h3 class="text-base font-semibold text-gray-900 mb-4">报价对比分析</h3>
            <div class="border border-gray-200 rounded-lg overflow-hidden">
              <table class="w-full">
                <thead class="bg-gray-50">
                  <tr>
                    <th class="text-left px-4 py-3 text-sm font-medium text-gray-700">供应商</th>
                    <th class="text-left px-4 py-3 text-sm font-medium text-gray-700">物料名称</th>
                    <th class="text-right px-4 py-3 text-sm font-medium text-gray-700">数量</th>
                    <th class="text-right px-4 py-3 text-sm font-medium text-gray-700">单价</th>
                    <th class="text-right px-4 py-3 text-sm font-medium text-gray-700">总价</th>
                    <th class="text-left px-4 py-3 text-sm font-medium text-gray-700">交期</th>
                    <th class="text-left px-4 py-3 text-sm font-medium text-gray-700">付款条款</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(quote, idx) in mockQuotations" :key="idx" class="border-t border-gray-200" :class="idx === 0 ? 'bg-green-50' : ''">
                    <td class="px-4 py-3 text-sm text-gray-900">
                      {{ quote.supplier }}
                      <span v-if="idx === 0" class="ml-2 px-2 py-0.5 bg-green-600 text-white rounded-full text-xs">最优</span>
                    </td>
                    <td class="px-4 py-3 text-sm text-gray-900">{{ quote.materialName }}</td>
                    <td class="px-4 py-3 text-sm text-right text-gray-900">{{ quote.quantity.toLocaleString() }} {{ quote.unit }}</td>
                    <td class="px-4 py-3 text-sm text-right text-gray-900">¥{{ quote.unitPrice }}</td>
                    <td class="px-4 py-3 text-sm text-right font-medium text-gray-900">¥{{ quote.totalPrice.toLocaleString() }}</td>
                    <td class="px-4 py-3 text-sm text-gray-900">{{ quote.deliveryTime }}</td>
                    <td class="px-4 py-3 text-sm text-gray-900">{{ quote.paymentTerms }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- 成本分析 -->
          <div v-if="activeTab === 'cost-analysis'" class="space-y-6">
            <h3 class="text-base font-semibold text-gray-900">成本拆解分析</h3>
            <div class="p-4 bg-blue-50 rounded-lg">
              <div class="flex items-center gap-2 mb-3">
                <FileText class="w-5 h-5 text-blue-600" />
                <h4 class="font-semibold text-blue-900">精细化核价</h4>
              </div>
              <p class="text-sm text-blue-800">支持非标件成本拆解，包括：材料成本、加工成本、损耗率、物流费用、管理费用等，实现阳光采购。</p>
            </div>
            <div class="border border-gray-200 rounded-lg p-4">
              <h4 class="font-semibold text-gray-900 mb-4">成本构成示例 - ADC12铝合金锭</h4>
              <div class="space-y-3">
                <div v-for="cost in costItems" :key="cost.item" class="flex items-center gap-4">
                  <div class="flex-1">
                    <div class="flex items-center justify-between mb-1">
                      <span class="text-sm text-gray-700">{{ cost.item }}</span>
                      <span class="text-sm font-medium text-gray-900">{{ cost.amount }} {{ cost.unit }}</span>
                    </div>
                    <div class="flex items-center gap-2">
                      <div class="flex-1 bg-gray-200 rounded-full h-2">
                        <div class="bg-blue-600 h-2 rounded-full" :style="{ width: `${cost.percentage}%` }" />
                      </div>
                      <span class="text-xs text-gray-500 w-12 text-right">{{ cost.percentage }}%</span>
                    </div>
                  </div>
                </div>
                <div class="pt-3 border-t border-gray-200">
                  <div class="flex items-center justify-between">
                    <span class="font-semibold text-gray-900">总成本</span>
                    <span class="text-lg font-semibold text-gray-900">57 元/KG</span>
                  </div>
                </div>
              </div>
            </div>
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div class="p-4 border border-gray-200 rounded-lg"><p class="text-sm text-gray-500 mb-2">市场均价</p><p class="text-2xl font-semibold text-gray-900">¥62/KG</p><p class="text-xs text-gray-500 mt-1">基于近30天市场数据</p></div>
              <div class="p-4 border border-gray-200 rounded-lg"><p class="text-sm text-gray-500 mb-2">最优报价</p><p class="text-2xl font-semibold text-green-600">¥57/KG</p><p class="text-xs text-gray-500 mt-1">深圳市精诚模具</p></div>
              <div class="p-4 border border-gray-200 rounded-lg"><p class="text-sm text-gray-500 mb-2">节约金额</p><p class="text-2xl font-semibold text-orange-600">¥25万</p><p class="text-xs text-gray-500 mt-1">基于50吨采购量</p></div>
            </div>
          </div>
        </div>
      </div>

      <!-- 创建寻源项目弹窗 -->
      <Teleport to="body">
        <Transition name="modal">
          <div v-if="isCreateDialogOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4">
            <div class="absolute inset-0 bg-black/50" @click="isCreateDialogOpen = false" />
            <div class="relative bg-white rounded-xl shadow-xl w-full max-w-lg p-6">
              <h3 class="text-lg font-semibold text-gray-900 mb-4">创建寻源项目</h3>

              <div class="space-y-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">项目名称 <span class="text-red-500">*</span></label>
                  <input
                    v-model="newProject.name"
                    placeholder="请输入项目名称"
                    :class="['w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2', formErrors.name ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500']"
                  />
                  <p v-if="formErrors.name" class="text-red-500 text-xs mt-1 flex items-center gap-1">
                    <XCircle class="w-3 h-3" />{{ formErrors.name }}
                  </p>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">物料分类</label>
                  <select v-model="newProject.category" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
                    <option value="原材料">原材料</option>
                    <option value="辅料">辅料</option>
                    <option value="包材">包材</option>
                    <option value="设备">设备</option>
                    <option value="服务">服务</option>
                  </select>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">目标金额 (元) <span class="text-red-500">*</span></label>
                  <input
                    v-model.number="newProject.targetAmount"
                    type="number"
                    min="0"
                    placeholder="请输入目标金额"
                    :class="['w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2', formErrors.targetAmount ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500']"
                  />
                  <p v-if="formErrors.targetAmount" class="text-red-500 text-xs mt-1 flex items-center gap-1">
                    <XCircle class="w-3 h-3" />{{ formErrors.targetAmount }}
                  </p>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">截止日期 <span class="text-red-500">*</span></label>
                  <input
                    v-model="newProject.deadline"
                    type="date"
                    :class="['w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2', formErrors.deadline ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500']"
                  />
                  <p v-if="formErrors.deadline" class="text-red-500 text-xs mt-1 flex items-center gap-1">
                    <XCircle class="w-3 h-3" />{{ formErrors.deadline }}
                  </p>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">邀请供应商</label>
                  <div class="border border-gray-200 rounded-lg p-3 max-h-40 overflow-y-auto">
                    <div v-if="suppliers.length === 0" class="text-sm text-gray-500 text-center py-2">
                      暂无供应商，请先添加供应商
                    </div>
                    <div v-else class="space-y-2">
                      <label
                        v-for="supplier in suppliers"
                        :key="supplier.id"
                        class="flex items-center gap-2 p-2 hover:bg-gray-50 rounded cursor-pointer"
                      >
                        <input
                          type="checkbox"
                          :value="supplier.id"
                          :checked="newProject.invitedSuppliers.includes(supplier.id)"
                          @change="toggleSupplier(supplier.id)"
                          class="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                        />
                        <span class="text-sm text-gray-700">{{ supplier.name }}</span>
                      </label>
                    </div>
                  </div>
                  <p class="text-xs text-gray-500 mt-1">已选择 {{ newProject.invitedSuppliers.length }} 家供应商</p>
                </div>
              </div>

              <div class="flex justify-end gap-3 mt-6">
                <button type="button" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50" @click="isCreateDialogOpen = false">取消</button>
                <button type="button" class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700" @click="handleCreateProject">创建项目</button>
              </div>
            </div>
          </div>
        </Transition>
      </Teleport>

      <!-- 报价详情对话框 -->
      <Teleport to="body">
        <Transition name="modal">
          <div v-if="isQuoteDetailDialogOpen && selectedProject" class="fixed inset-0 z-50 flex items-center justify-center p-4">
            <div class="absolute inset-0 bg-black/50" @click="isQuoteDetailDialogOpen = false" />
            <div class="relative bg-white rounded-xl shadow-xl w-full max-w-3xl max-h-[80vh] overflow-hidden">
              <div class="flex items-center justify-between p-4 border-b">
                <h3 class="text-lg font-semibold text-gray-900">报价详情 - {{ selectedProject.name }}</h3>
                <button @click="isQuoteDetailDialogOpen = false" class="p-2 hover:bg-gray-100 rounded-lg">✕</button>
              </div>
              <div class="p-6 overflow-y-auto max-h-[60vh]">
                <div class="border border-gray-200 rounded-lg overflow-hidden">
                  <table class="w-full text-sm">
                    <thead class="bg-gray-50">
                      <tr>
                        <th class="text-left px-4 py-3 font-medium text-gray-700">供应商</th>
                        <th class="text-right px-4 py-3 font-medium text-gray-700">单价</th>
                        <th class="text-right px-4 py-3 font-medium text-gray-700">总价</th>
                        <th class="text-left px-4 py-3 font-medium text-gray-700">交期</th>
                        <th class="text-left px-4 py-3 font-medium text-gray-700">付款条款</th>
                        <th class="text-left px-4 py-3 font-medium text-gray-700">状态</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="(quote, idx) in getProjectQuotations(selectedProject.id)" :key="quote.id" class="border-t border-gray-200" :class="idx === 0 ? 'bg-green-50' : ''">
                        <td class="px-4 py-3 text-sm text-gray-900">
                          {{ quote.supplierName }}
                          <span v-if="idx === 0" class="ml-2 px-2 py-0.5 bg-green-600 text-white rounded-full text-xs">最优</span>
                        </td>
                        <td class="px-4 py-3 text-sm text-right text-gray-900">¥{{ quote.unitPrice.toLocaleString() }}</td>
                        <td class="px-4 py-3 text-sm text-right font-medium text-gray-900">¥{{ quote.totalPrice.toLocaleString() }}</td>
                        <td class="px-4 py-3 text-sm text-gray-900">{{ quote.deliveryTime }}</td>
                        <td class="px-4 py-3 text-sm text-gray-900">{{ quote.paymentTerms }}</td>
                        <td class="px-4 py-3 text-sm">
                          <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="quote.status === 'accepted' ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-700'">
                            {{ quote.status === 'accepted' ? '已中标' : '待评标' }}
                          </span>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
              <div class="flex justify-end gap-3 p-4 border-t bg-gray-50">
                <button @click="isQuoteDetailDialogOpen = false" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">关闭</button>
              </div>
            </div>
          </div>
        </Transition>
      </Teleport>

      <!-- 评标对话框 -->
      <Teleport to="body">
        <Transition name="modal">
          <div v-if="isEvaluationDialogOpen && selectedProject" class="fixed inset-0 z-50 flex items-center justify-center p-4">
            <div class="absolute inset-0 bg-black/50" @click="isEvaluationDialogOpen = false" />
            <div class="relative bg-white rounded-xl shadow-xl w-full max-w-4xl max-h-[80vh] overflow-hidden">
              <div class="flex items-center justify-between p-4 border-b">
                <h3 class="text-lg font-semibold text-gray-900">评标打分 - {{ selectedProject.name }}</h3>
                <button @click="isEvaluationDialogOpen = false" class="p-2 hover:bg-gray-100 rounded-lg">✕</button>
              </div>
              <div class="p-6 overflow-y-auto max-h-[60vh]">
                <div class="border border-gray-200 rounded-lg overflow-hidden">
                  <table class="w-full text-sm">
                    <thead class="bg-gray-50">
                      <tr>
                        <th class="text-left px-4 py-3 font-medium text-gray-700">供应商</th>
                        <th class="text-right px-4 py-3 font-medium text-gray-700">价格分(30%)</th>
                        <th class="text-right px-4 py-3 font-medium text-gray-700">质量分(30%)</th>
                        <th class="text-right px-4 py-3 font-medium text-gray-700">交付分(20%)</th>
                        <th class="text-right px-4 py-3 font-medium text-gray-700">服务分(20%)</th>
                        <th class="text-right px-4 py-3 font-medium text-gray-700">总分</th>
                        <th class="text-left px-4 py-3 font-medium text-gray-700">备注</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="(score, idx) in evaluationForm.scores" :key="score.supplierId" class="border-t border-gray-200">
                        <td class="px-4 py-3 text-sm font-medium text-gray-900">{{ score.supplierName }}</td>
                        <td class="px-4 py-3 text-sm text-right">
                          <input v-model.number="score.priceScore" type="number" min="0" max="100" class="w-16 px-2 py-1 border rounded text-right text-sm" @input="score.totalScore = calculateTotalScore(score)" />
                        </td>
                        <td class="px-4 py-3 text-sm text-right">
                          <input v-model.number="score.qualityScore" type="number" min="0" max="100" class="w-16 px-2 py-1 border rounded text-right text-sm" @input="score.totalScore = calculateTotalScore(score)" />
                        </td>
                        <td class="px-4 py-3 text-sm text-right">
                          <input v-model.number="score.deliveryScore" type="number" min="0" max="100" class="w-16 px-2 py-1 border rounded text-right text-sm" @input="score.totalScore = calculateTotalScore(score)" />
                        </td>
                        <td class="px-4 py-3 text-sm text-right">
                          <input v-model.number="score.serviceScore" type="number" min="0" max="100" class="w-16 px-2 py-1 border rounded text-right text-sm" @input="score.totalScore = calculateTotalScore(score)" />
                        </td>
                        <td class="px-4 py-3 text-sm text-right font-bold" :class="score.totalScore >= 80 ? 'text-green-600' : score.totalScore >= 60 ? 'text-blue-600' : 'text-gray-600'">{{ score.totalScore }}</td>
                        <td class="px-4 py-3 text-sm">
                          <input v-model="score.notes" type="text" placeholder="备注" class="w-full px-2 py-1 border rounded text-sm" />
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
              <div class="flex justify-end gap-3 p-4 border-t bg-gray-50">
                <button @click="isEvaluationDialogOpen = false" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">取消</button>
                <button @click="handleSubmitEvaluation" class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">提交评标</button>
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
