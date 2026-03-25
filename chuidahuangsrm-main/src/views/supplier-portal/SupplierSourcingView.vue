<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { FileText, Clock, CheckCircle, XCircle, AlertCircle, Send, Eye, Package, TrendingUp, Calendar } from 'lucide-vue-next'
import { useAppStore } from '@/stores/useAppStore'

const appStore = useAppStore()

// 对话框状态
const isQuoteDialogOpen = ref(false)
const isDetailDialogOpen = ref(false)

// 选中的项目
const selectedProject = ref<any>(null)

// 当前供应商ID（模拟当前登录供应商）
const currentSupplierId = ref('S001')
const currentSupplierName = ref('深圳市精诚模具制造有限公司')

// 寻源项目列表
const projects = ref<any[]>([])

// 报价数据
const quotations = ref<any[]>([])

// 报价表单
const quoteForm = ref({
  projectId: '',
  items: [] as Array<{
    materialId: string
    materialCode: string
    materialName: string
    quantity: number
    unit: string
    unitPrice: number
    totalPrice: number
    deliveryTime: ''
    notes: string
  }>,
  totalAmount: 0,
  deliveryTime: '',
  paymentTerms: '月结30天',
  validUntil: '',
  notes: '',
})

// 表单错误
const formErrors = ref<Record<string, string>>({})

// 即将到期的报价
const expiringQuotations = ref<any[]>([])

// 定时器
let validityCheckTimer: any = null

// 计算某个项目的报价状态
function getQuoteStatus(project: any) {
  const quote = quotations.value.find(q => q.projectId === project.id)
  if (!quote) return null
  return quote.status
}

// 计算某个项目的报价有效期剩余天数
function getValidityDays(quote: any) {
  if (!quote) return null
  const today = new Date()
  const validUntil = new Date(quote.validUntil)
  const diffTime = validUntil.getTime() - today.getTime()
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  return diffDays
}

// 获取有效期状态
function getValidityStatus(days: number) {
  if (days < 0) return { text: '已过期', class: 'text-red-600 bg-red-50' }
  if (days <= 3) return { text: `${days}天后到期`, class: 'text-orange-600 bg-orange-50' }
  if (days <= 7) return { text: `${days}天后到期`, class: 'text-yellow-600 bg-yellow-50' }
  return { text: `${days}天后到期`, class: 'text-green-600 bg-green-50' }
}

// 打开详情对话框
function openDetailDialog(project: any) {
  selectedProject.value = project
  isDetailDialogOpen.value = true
}

// 打开报价对话框
function openQuoteDialog(project: any) {
  selectedProject.value = project
  formErrors.value = {}

  // 检查是否已报价
  const existingQuote = quotations.value.find(q => q.projectId === project.id)
  if (existingQuote) {
    // 编辑模式
    quoteForm.value = {
      projectId: project.id,
      items: [...existingQuote.items],
      totalAmount: existingQuote.totalAmount,
      deliveryTime: existingQuote.deliveryTime,
      paymentTerms: existingQuote.paymentTerms,
      validUntil: existingQuote.validUntil,
      notes: existingQuote.notes || '',
    }
  } else {
    // 新建模式 - 初始化物料项
    quoteForm.value = {
      projectId: project.id,
      items: (project.materials || []).map((m: any) => ({
        materialId: m.id,
        materialCode: m.materialCode,
        materialName: m.materialName,
        quantity: m.quantity,
        unit: m.unit,
        unitPrice: 0,
        totalPrice: 0,
        deliveryTime: '',
        notes: '',
      })),
      totalAmount: 0,
      deliveryTime: '',
      paymentTerms: '月结30天',
      validUntil: '',
      notes: '',
    }
  }

  // 默认有效期30天后
  if (!quoteForm.value.validUntil) {
    const validDate = new Date()
    validDate.setDate(validDate.getDate() + 30)
    quoteForm.value.validUntil = validDate.toISOString().split('T')[0]
  }

  isQuoteDialogOpen.value = true
}

// 计算单项总价
function calculateItemTotal(item: any) {
  item.totalPrice = item.quantity * item.unitPrice
  calculateTotal()
}

// 计算总金额
function calculateTotal() {
  quoteForm.value.totalAmount = quoteForm.value.items.reduce((sum, item) => sum + (item.totalPrice || 0), 0)
}

// 提交报价
function handleSubmitQuote() {
  formErrors.value = {}

  // 验证
  if (quoteForm.value.items.length === 0) {
    appStore.showToast('请至少填写一个物料的报价', 'warning')
    return
  }

  for (let i = 0; i < quoteForm.value.items.length; i++) {
    const item = quoteForm.value.items[i]
    if (item.unitPrice <= 0) {
      appStore.showToast(`第 ${i + 1} 行：请输入有效单价`, 'warning')
      return
    }
  }

  if (!quoteForm.value.deliveryTime) {
    formErrors.value.deliveryTime = '请输入交期'
    return
  }

  if (!quoteForm.value.validUntil) {
    formErrors.value.validUntil = '请选择报价有效期'
    return
  }

  // 保存报价
  const allQuotations = JSON.parse(localStorage.getItem('sourcing_quotations') || '[]')

  // 查找是否已有报价
  const existingIndex = allQuotations.findIndex((q: any) => q.projectId === quoteForm.value.projectId && q.supplierId === currentSupplierId.value)

  const quotation = {
    id: existingIndex >= 0 ? allQuotations[existingIndex].id : `QT${Date.now()}`,
    projectId: quoteForm.value.projectId,
    supplierId: currentSupplierId.value,
    supplierName: currentSupplierName.value,
    items: [...quoteForm.value.items],
    totalAmount: quoteForm.value.totalAmount,
    deliveryTime: quoteForm.value.deliveryTime,
    paymentTerms: quoteForm.value.paymentTerms,
    validUntil: quoteForm.value.validUntil,
    submitDate: new Date().toISOString(),
    status: 'pending',
    notes: quoteForm.value.notes,
  }

  if (existingIndex >= 0) {
    allQuotations[existingIndex] = quotation
  } else {
    allQuotations.push(quotation)
  }

  localStorage.setItem('sourcing_quotations', JSON.stringify(allQuotations))

  // 更新本地报价列表
  loadQuotations()

  // 更新项目的已报价数量
  const projectsData = JSON.parse(localStorage.getItem('sourcingProjects') || '[]')
  const project = projectsData.find((p: any) => p.id === quoteForm.value.projectId)
  if (project) {
    const quotedCount = allQuotations.filter((q: any) => q.projectId === project.id).length
    project.quotedCount = quotedCount
    localStorage.setItem('sourcingProjects', JSON.stringify(projectsData))
    loadProjects()
  }

  appStore.showToast('报价提交成功', 'success')
  isQuoteDialogOpen.value = false
}

// 检查有效期提醒
function checkValidityWarnings() {
  const warnings: any[] = []

  quotations.value.forEach(quote => {
    const days = getValidityDays(quote)
    if (days !== null && days <= 7 && days > 0) {
      const project = projects.value.find(p => p.id === quote.projectId)
      warnings.push({
        quoteId: quote.id,
        projectName: project?.name || quote.projectId,
        validUntil: quote.validUntil,
        days,
        status: days <= 3 ? 'urgent' : 'warning',
      })
    }
  })

  expiringQuotations.value = warnings
}

// 加载项目
function loadProjects() {
  const allProjects = JSON.parse(localStorage.getItem('sourcingProjects') || '[]')

  // 筛选出邀请当前供应商的项目
  projects.value = allProjects.filter((p: any) =>
    p.invitedSuppliers && p.invitedSuppliers.includes(currentSupplierId.value)
  )
}

// 加载报价
function loadQuotations() {
  const allQuotations = JSON.parse(localStorage.getItem('sourcing_quotations') || '[]')

  // 筛选当前供应商的报价
  quotations.value = allQuotations.filter((q: any) => q.supplierId === currentSupplierId.value)
}

// 计算属性
const pendingProjects = computed(() => {
  return projects.value.filter(p => {
    const quoted = quotations.value.find(q => q.projectId === p.id)
    return !quoted && ['询价中', '报价中'].includes(p.status)
  })
})

const quotedProjects = computed(() => {
  return projects.value.filter(p => {
    const quoted = quotations.value.find(q => q.projectId === p.id)
    return quoted && ['询价中', '报价中', '待定标'].includes(p.status)
  })
})

const completedProjects = computed(() => {
  return projects.value.filter(p => {
    const quoted = quotations.value.find(q => q.projectId === p.id && q.status === 'accepted')
    return quoted
  })
})

// 初始化
onMounted(() => {
  loadProjects()
  loadQuotations()
  checkValidityWarnings()

  // 每小时检查一次有效期
  validityCheckTimer = setInterval(() => {
    checkValidityWarnings()
  }, 60 * 60 * 1000)
})

// 清理
onUnmounted(() => {
  if (validityCheckTimer) {
    clearInterval(validityCheckTimer)
  }
})
</script>

<template>
  <div class="space-y-6">
    <!-- 顶部提醒 -->
    <div v-if="expiringQuotations.length > 0" class="p-4 bg-orange-50 border border-orange-200 rounded-lg">
      <div class="flex items-center gap-3">
        <AlertCircle class="w-5 h-5 text-orange-600" />
        <div class="flex-1">
          <p class="font-medium text-orange-900">报价有效期提醒</p>
          <p class="text-sm text-orange-700 mt-1">您有 {{ expiringQuotations.length }} 个报价即将到期，请注意及时更新报价。</p>
        </div>
      </div>
      <div class="mt-3 space-y-2">
        <div v-for="warning in expiringQuotations" :key="warning.quoteId" class="flex items-center justify-between p-2 bg-white rounded border border-orange-200">
          <div class="flex items-center gap-2">
            <span class="text-sm text-gray-900">{{ warning.projectName }}</span>
            <span class="text-xs" :class="warning.status === 'urgent' ? 'text-red-600 font-medium' : 'text-orange-600'">{{ warning.days }}天后到期</span>
          </div>
          <button @click="openQuoteDialog(projects.find(p => p.id === warning.projectId.replace('SP', 'SP'))) || openDetailDialog(projects.find(p => p.id === warning.projectId))" class="text-xs text-blue-600 hover:underline">查看</button>
        </div>
      </div>
    </div>

    <!-- 标题 -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-semibold text-gray-900">寻源报价</h1>
        <p class="text-sm text-gray-500 mt-1">查看寻源邀请并提交报价</p>
      </div>
      <div class="flex items-center gap-2 text-sm">
        <span class="px-3 py-1 bg-blue-100 text-blue-800 rounded-full">待报价: {{ pendingProjects.length }}</span>
        <span class="px-3 py-1 bg-green-100 text-green-800 rounded-full">已报价: {{ quotedProjects.length }}</span>
        <span class="px-3 py-1 bg-purple-100 text-purple-800 rounded-full">已中标: {{ completedProjects.length }}</span>
      </div>
    </div>

    <!-- 待报价项目 -->
    <div v-if="pendingProjects.length > 0">
      <h2 class="text-lg font-semibold text-gray-900 mb-4">待报价项目</h2>
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div v-for="project in pendingProjects" :key="project.id" class="border border-gray-200 rounded-lg p-5 hover:shadow-md transition-shadow">
          <div class="flex items-start justify-between mb-3">
            <div class="flex-1">
              <div class="flex items-center gap-2 mb-2 flex-wrap">
                <h3 class="font-semibold text-gray-900">{{ project.name }}</h3>
                <span class="px-2 py-0.5 bg-blue-100 text-blue-800 rounded-full text-xs font-medium">{{ project.status }}</span>
              </div>
              <p class="text-sm text-gray-500">项目编号: {{ project.id }}</p>
              <p v-if="project.description" class="text-sm text-gray-600 mt-1 line-clamp-2">{{ project.description }}</p>
            </div>
            <div class="text-right">
              <p class="text-xs text-gray-500">截止日期</p>
              <p class="font-medium text-gray-900">{{ project.deadline }}</p>
            </div>
          </div>

          <!-- 物料明细 -->
          <div v-if="project.materials && project.materials.length > 0" class="mb-3 p-2 bg-gray-50 rounded-lg">
            <div class="flex items-center gap-1 text-xs text-gray-600 mb-1">
              <Package class="w-3 h-3" />
              <span>物料明细 ({{ project.materials.length }}项)</span>
            </div>
            <div class="space-y-1">
              <div v-for="material in project.materials.slice(0, 2)" :key="material.id" class="text-xs text-gray-700">
                {{ material.materialName }} x {{ material.quantity }}{{ material.unit }}
              </div>
              <div v-if="project.materials.length > 2" class="text-xs text-gray-500">还有 {{ project.materials.length - 2 }} 项...</div>
            </div>
          </div>

          <div class="grid grid-cols-2 gap-2 mb-3">
            <div class="p-2 bg-gray-50 rounded-lg"><p class="text-xs text-gray-600">目标金额</p><p class="text-sm font-semibold text-gray-900">¥{{ (project.targetAmount / 10000).toFixed(1) }}万</p></div>
            <div class="p-2 bg-green-50 rounded-lg"><p class="text-xs text-gray-600">物料数</p><p class="text-sm font-semibold text-green-600">{{ project.materials?.length || 0 }}项</p></div>
          </div>

          <div class="flex gap-2">
            <button @click="openDetailDialog(project)" class="flex-1 px-3 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50 flex items-center justify-center gap-1">
              <Eye class="w-4 h-4" />详情
            </button>
            <button @click="openQuoteDialog(project)" class="flex-1 px-3 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700 flex items-center justify-center gap-1">
              <Send class="w-4 h-4" />报价
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 已报价项目 -->
    <div v-if="quotedProjects.length > 0">
      <h2 class="text-lg font-semibold text-gray-900 mb-4">已报价项目</h2>
      <div class="border border-gray-200 rounded-lg overflow-x-auto">
        <table class="w-full text-sm">
          <thead class="bg-gray-50">
            <tr>
              <th class="text-left px-4 py-3 font-medium text-gray-700">项目编号</th>
              <th class="text-left px-4 py-3 font-medium text-gray-700">项目名称</th>
              <th class="text-right px-4 py-3 font-medium text-gray-700">报价金额</th>
              <th class="text-left px-4 py-3 font-medium text-gray-700">交期</th>
              <th class="text-left px-4 py-3 font-medium text-gray-700">有效期至</th>
              <th class="text-left px-4 py-3 font-medium text-gray-700">状态</th>
              <th class="text-left px-4 py-3 font-medium text-gray-700">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="project in quotedProjects" :key="project.id" class="border-t border-gray-200">
              <td class="px-4 py-3 text-gray-900">{{ project.id }}</td>
              <td class="px-4 py-3 text-gray-900 font-medium">{{ project.name }}</td>
              <td class="px-4 py-3 text-right text-gray-900">
                ¥{{ (quotations.find(q => q.projectId === project.id)?.totalAmount || 0).toLocaleString() }}
              </td>
              <td class="px-4 py-3 text-gray-900">{{ quotations.find(q => q.projectId === project.id)?.deliveryTime || '-' }}</td>
              <td class="px-4 py-3">
                <span class="text-xs px-2 py-1 rounded" :class="getValidityStatus(getValidityDays(quotations.find(q => q.projectId === project.id)) || 0).class">
                  {{ quotations.find(q => q.projectId === project.id)?.validUntil || '-' }}
                </span>
              </td>
              <td class="px-4 py-3">
                <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="getQuoteStatus(project) === 'accepted' ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'">
                  {{ getQuoteStatus(project) === 'accepted' ? '已中标' : '评标中' }}
                </span>
              </td>
              <td class="px-4 py-3">
                <div class="flex gap-2">
                  <button @click="openDetailDialog(project)" class="px-2 py-1 border border-gray-300 rounded text-xs text-gray-700 hover:bg-gray-50">详情</button>
                  <button @click="openQuoteDialog(project)" class="px-2 py-1 border border-blue-300 rounded text-xs text-blue-700 hover:bg-blue-50">修改报价</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 已中标项目 -->
    <div v-if="completedProjects.length > 0">
      <h2 class="text-lg font-semibold text-gray-900 mb-4">已中标项目</h2>
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div v-for="project in completedProjects" :key="project.id" class="border-2 border-green-200 rounded-lg p-5 bg-green-50">
          <div class="flex items-center gap-2 mb-3">
            <CheckCircle class="w-5 h-5 text-green-600" />
            <h3 class="font-semibold text-gray-900">{{ project.name }}</h3>
            <span class="px-2 py-0.5 bg-green-600 text-white rounded-full text-xs font-medium">已中标</span>
          </div>
          <div class="grid grid-cols-2 gap-2">
            <div class="p-2 bg-white rounded"><p class="text-xs text-gray-600">中标金额</p><p class="text-sm font-semibold text-green-600">¥{{ (quotations.find(q => q.projectId === project.id && q.status === 'accepted')?.totalAmount || 0).toLocaleString() }}</p></div>
            <div class="p-2 bg-white rounded"><p class="text-xs text-gray-600">定标日期</p><p class="text-sm font-semibold text-gray-900">{{ project.finalizeDate || '-' }}</p></div>
          </div>
          <button @click="openDetailDialog(project)" class="mt-3 w-full px-3 py-2 border border-green-300 rounded-lg text-sm text-green-700 hover:bg-green-100 flex items-center justify-center gap-1">
            <Eye class="w-4 h-4" />查看详情
          </button>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="projects.length === 0" class="text-center py-12 text-gray-500">
      <FileText class="w-12 h-12 mx-auto mb-3 text-gray-300" />
      <p>暂无寻源邀请</p>
    </div>
  </div>

  <!-- 项目详情对话框 -->
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="isDetailDialogOpen && selectedProject" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-black/50" @click="isDetailDialogOpen = false" />
        <div class="relative bg-white rounded-xl shadow-xl w-full max-w-3xl max-h-[90vh] overflow-hidden flex flex-col">
          <div class="flex items-center justify-between p-4 border-b">
            <h3 class="text-lg font-semibold text-gray-900">寻源项目详情</h3>
            <button @click="isDetailDialogOpen = false" class="p-2 hover:bg-gray-100 rounded-lg"><X class="w-5 h-5" /></button>
          </div>

          <div class="p-6 overflow-y-auto flex-1 space-y-6">
            <!-- 基本信息 -->
            <div>
              <h4 class="font-medium text-gray-900 mb-3">基本信息</h4>
              <div class="grid grid-cols-2 gap-3 text-sm">
                <div><span class="text-gray-600">项目编号:</span> <span class="text-gray-900">{{ selectedProject.id }}</span></div>
                <div><span class="text-gray-600">项目名称:</span> <span class="text-gray-900">{{ selectedProject.name }}</span></div>
                <div><span class="text-gray-600">物料分类:</span> <span class="text-gray-900">{{ selectedProject.category }}</span></div>
                <div><span class="text-gray-600">项目状态:</span> <span class="text-gray-900">{{ selectedProject.status }}</span></div>
                <div><span class="text-gray-600">目标金额:</span> <span class="text-gray-900">¥{{ selectedProject.targetAmount.toLocaleString() }}</span></div>
                <div><span class="text-gray-600">截止日期:</span> <span class="text-gray-900">{{ selectedProject.deadline }}</span></div>
                <div class="col-span-2"><span class="text-gray-600">项目描述:</span> <span class="text-gray-900">{{ selectedProject.description || '无' }}</span></div>
              </div>
            </div>

            <!-- 物料明细 -->
            <div v-if="selectedProject.materials && selectedProject.materials.length > 0">
              <h4 class="font-medium text-gray-900 mb-3">物料明细</h4>
              <div class="border border-gray-200 rounded-lg overflow-hidden">
                <table class="w-full text-sm">
                  <thead class="bg-gray-50">
                    <tr>
                      <th class="text-left px-4 py-2 font-medium text-gray-700">物料编码</th>
                      <th class="text-left px-4 py-2 font-medium text-gray-700">物料名称</th>
                      <th class="text-right px-4 py-2 font-medium text-gray-700">数量</th>
                      <th class="text-right px-4 py-2 font-medium text-gray-700">目标单价</th>
                      <th class="text-right px-4 py-2 font-medium text-gray-700">小计</th>
                      <th class="text-left px-4 py-2 font-medium text-gray-700">备注</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="material in selectedProject.materials" :key="material.id" class="border-t border-gray-200">
                      <td class="px-4 py-2 text-gray-900">{{ material.materialCode }}</td>
                      <td class="px-4 py-2 text-gray-900">{{ material.materialName }}</td>
                      <td class="px-4 py-2 text-right text-gray-900">{{ material.quantity }} {{ material.unit }}</td>
                      <td class="px-4 py-2 text-right text-gray-900">¥{{ material.targetPrice }}</td>
                      <td class="px-4 py-2 text-right text-gray-900">¥{{ (material.quantity * material.targetPrice).toLocaleString() }}</td>
                      <td class="px-4 py-2 text-gray-600">{{ material.description || '-' }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          <div class="flex justify-end gap-3 p-4 border-t bg-gray-50">
            <button @click="isDetailDialogOpen = false" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">关闭</button>
            <button @click="isDetailDialogOpen = false; openQuoteDialog(selectedProject)" class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">
              {{ getQuoteStatus(selectedProject) ? '修改报价' : '提交报价' }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>

  <!-- 报价对话框 -->
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="isQuoteDialogOpen && selectedProject" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-black/50" @click="isQuoteDialogOpen = false" />
        <div class="relative bg-white rounded-xl shadow-xl w-full max-w-4xl max-h-[90vh] overflow-hidden flex flex-col">
          <div class="flex items-center justify-between p-4 border-b">
            <h3 class="text-lg font-semibold text-gray-900">提交报价 - {{ selectedProject.name }}</h3>
            <button @click="isQuoteDialogOpen = false" class="p-2 hover:bg-gray-100 rounded-lg"><X class="w-5 h-5" /></button>
          </div>

          <div class="p-6 overflow-y-auto flex-1 space-y-4">
            <!-- 项目信息 -->
            <div class="p-3 bg-blue-50 rounded-lg">
              <p class="text-sm text-blue-800"><span class="font-medium">项目编号:</span> {{ selectedProject.id }}</p>
              <p class="text-sm text-blue-800"><span class="font-medium">截止日期:</span> {{ selectedProject.deadline }}</p>
            </div>

            <!-- 报价明细 -->
            <div>
              <h4 class="font-medium text-gray-900 mb-3">报价明细</h4>
              <div class="border border-gray-200 rounded-lg overflow-hidden">
                <table class="w-full text-sm">
                  <thead class="bg-gray-50">
                    <tr>
                      <th class="text-left px-3 py-2 font-medium text-gray-700">物料编码</th>
                      <th class="text-left px-3 py-2 font-medium text-gray-700">物料名称</th>
                      <th class="text-right px-3 py-2 font-medium text-gray-700">需求数量</th>
                      <th class="text-right px-3 py-2 font-medium text-gray-700">单价(元) <span class="text-red-500">*</span></th>
                      <th class="text-right px-3 py-2 font-medium text-gray-700">小计</th>
                      <th class="text-left px-3 py-2 font-medium text-gray-700">交期</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(item, idx) in quoteForm.items" :key="item.materialId" class="border-t border-gray-200">
                      <td class="px-2 py-2 text-sm text-gray-900">{{ item.materialCode }}</td>
                      <td class="px-2 py-2 text-sm text-gray-900">{{ item.materialName }}</td>
                      <td class="px-2 py-2 text-sm text-right text-gray-900">{{ item.quantity }} {{ item.unit }}</td>
                      <td class="px-2 py-2">
                        <input v-model.number="item.unitPrice" type="number" min="0" step="0.01" @input="calculateItemTotal(item)" class="w-24 px-2 py-1 border border-gray-300 rounded text-sm text-right" />
                      </td>
                      <td class="px-2 py-2 text-sm text-right text-gray-900">¥{{ item.totalPrice.toLocaleString() }}</td>
                      <td class="px-2 py-2">
                        <input v-model="item.deliveryTime" type="text" placeholder="如：30天" class="w-20 px-2 py-1 border border-gray-300 rounded text-sm" />
                      </td>
                    </tr>
                  </tbody>
                  <tfoot class="bg-gray-50">
                    <tr>
                      <td colspan="4" class="px-3 py-2 text-right font-medium text-gray-700">合计</td>
                      <td class="px-3 py-2 text-right font-bold text-gray-900">¥{{ quoteForm.totalAmount.toLocaleString() }}</td>
                      <td></td>
                    </tr>
                  </tfoot>
                </table>
              </div>
            </div>

            <!-- 其他条件 -->
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">交期 <span class="text-red-500">*</span></label>
                <input v-model="quoteForm.deliveryTime" type="text" placeholder="如：收到订单后30天" :class="['w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2', formErrors.deliveryTime ? 'border-red-500' : 'border-gray-300']" />
                <p v-if="formErrors.deliveryTime" class="text-red-500 text-xs mt-1">{{ formErrors.deliveryTime }}</p>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">付款条件</label>
                <select v-model="quoteForm.paymentTerms" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
                  <option value="月结30天">月结30天</option>
                  <option value="月结60天">月结60天</option>
                  <option value="月结90天">月结90天</option>
                  <option value="款到发货">款到发货</option>
                </select>
              </div>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">报价有效期至 <span class="text-red-500">*</span></label>
                <input v-model="quoteForm.validUntil" type="date" :class="['w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2', formErrors.validUntil ? 'border-red-500' : 'border-gray-300']" />
                <p v-if="formErrors.validUntil" class="text-red-500 text-xs mt-1">{{ formErrors.validUntil }}</p>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">报价总额</label>
                <div class="p-3 bg-gray-50 rounded-lg">
                  <p class="text-2xl font-semibold text-blue-600">¥{{ quoteForm.totalAmount.toLocaleString() }}</p>
                </div>
              </div>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">备注说明</label>
              <textarea v-model="quoteForm.notes" rows="2" placeholder="可填写价格说明、质量承诺、特殊要求等" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
            </div>
          </div>

          <div class="flex justify-end gap-3 p-4 border-t bg-gray-50">
            <button @click="isQuoteDialogOpen = false" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">取消</button>
            <button @click="handleSubmitQuote" class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">提交报价</button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>

  <!-- 样式 -->
  <style scoped>
  .modal-enter-active, .modal-leave-active {
    transition: opacity 0.2s ease;
  }
  .modal-enter-from, .modal-leave-to {
    opacity: 0;
  }
  </style>
</template>
