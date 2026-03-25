<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { TrendingUp, DollarSign, FileText, Target, LayoutGrid, List, Plus, XCircle, Check, CheckCircle, Eye, Award, Edit, Trash2, AlertCircle, Send, Clock, Users, Package, X } from 'lucide-vue-next'
import { useAppStore } from '@/stores/useAppStore'
import { mockSuppliers } from '@/data/mockData'

const appStore = useAppStore()
const activeTab = ref('projects')
const viewMode = ref<'grid' | 'list'>('list')

// 对话框状态
const isCreateDialogOpen = ref(false)
const isDetailDialogOpen = ref(false)
const isQuoteDetailDialogOpen = ref(false)
const isEvaluationDialogOpen = ref(false)
const isFinalizeDialogOpen = ref(false)
const isApprovalDialogOpen = ref(false)
const formErrors = ref<Record<string, string>>({})

// 选中的项目
const selectedProject = ref<any>(null)

// 供应商列表
const suppliers = ref<any[]>([])

// 寻源项目列表
const projects = ref<any[]>([])

// 审批列表
const approvals = ref<any[]>([])

// 物料明细类型
interface MaterialItem {
  id: string
  materialCode: string
  materialName: string
  quantity: number
  unit: string
  targetPrice: number
  description: string
}

// 报价数据类型
interface Quotation {
  id: string
  projectId: string
  supplierId: string
  supplierName: string
  items: Array<{
    materialCode: string
    materialName: string
    quantity: number
    unitPrice: number
    totalPrice: number
  }>
  totalAmount: number
  deliveryTime: string
  paymentTerms: string
  validUntil: string
  submitDate: string
  status: 'pending' | 'reviewing' | 'accepted' | 'rejected'
  notes: string
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

// 定标表单
const finalizeForm = ref({
  selectedSupplierId: '',
  finalizeReason: '',
  attachments: [] as string[],
})

// 审批表单
const approvalForm = ref({
  action: 'approve', // approve | reject
  comment: '',
})

// 新寻源项目表单数据
const newProject = ref({
  name: '',
  category: '原材料',
  description: '',
  targetAmount: 0,
  deadline: '',
  invitedSuppliers: [] as string[],
  materials: [] as MaterialItem[],
})

// 添加物料
function addMaterial() {
  newProject.value.materials.push({
    id: `M${Date.now()}`,
    materialCode: '',
    materialName: '',
    quantity: 0,
    unit: '个',
    targetPrice: 0,
    description: '',
  })
}

// 删除物料
function removeMaterial(materialId: string) {
  const index = newProject.value.materials.findIndex(m => m.id === materialId)
  if (index > -1) {
    newProject.value.materials.splice(index, 1)
  }
}

// 计算项目总目标金额
function calculateProjectTarget() {
  return newProject.value.materials.reduce((sum, item) => sum + (item.quantity * item.targetPrice), 0)
}

// 打开项目详情对话框
function openDetailDialog(project: any) {
  selectedProject.value = project
  isDetailDialogOpen.value = true
}

// 打开编辑对话框
function openEditDialog(project: any) {
  selectedProject.value = project
  newProject.value = {
    name: project.name,
    category: project.category,
    description: project.description || '',
    targetAmount: project.targetAmount,
    deadline: project.deadline,
    invitedSuppliers: project.invitedSuppliers || [],
    materials: project.materials || [],
  }
  formErrors.value = {}
  isCreateDialogOpen.value = true
}

// 打开创建对话框
function openCreateDialog() {
  selectedProject.value = null
  formErrors.value = {}
  newProject.value = {
    name: '',
    category: '原材料',
    description: '',
    targetAmount: 0,
    deadline: '',
    invitedSuppliers: [],
    materials: [],
  }
  isCreateDialogOpen.value = true
}

// 切换供应商选择
function toggleSupplier(supplierId: string) {
  const index = newProject.value.invitedSuppliers.indexOf(supplierId)
  if (index > -1) {
    newProject.value.invitedSuppliers.splice(index, 1)
  } else {
    newProject.value.invitedSuppliers.push(supplierId)
  }
}

// 供应商搜索下拉框状态
const supplierSearchQuery = ref('')
const isSupplierDropdownOpen = ref(false)
const supplierSearchInputRef = ref<HTMLElement | null>(null)
const supplierDropdownRef = ref<HTMLElement | null>(null)
const dropdownStyle = ref({ top: '0px', left: '0px', width: '0px' })

// 计算下拉框位置（只在需要时调用）
function updateDropdownPosition() {
  if (supplierSearchInputRef.value) {
    const rect = supplierSearchInputRef.value.getBoundingClientRect()
    dropdownStyle.value = {
      top: `${rect.bottom + window.scrollY + 4}px`,
      left: `${rect.left + window.scrollX}px`,
      width: `${rect.width}px`
    }
  }
}

// 打开下拉框并更新位置
function openDropdown() {
  isSupplierDropdownOpen.value = true
  nextTick(() => {
    updateDropdownPosition()
  })
}

// 点击外部关闭下拉框
function handleClickOutside(event: MouseEvent) {
  // 如果下拉框未打开，不处理
  if (!isSupplierDropdownOpen.value) return

  const target = event.target as Node

  // 检查是否点击了输入框或下拉框
  const clickedInput = supplierSearchInputRef.value && supplierSearchInputRef.value.contains(target)
  const clickedDropdown = supplierDropdownRef.value && supplierDropdownRef.value.contains(target)

  // 如果既没点击输入框，也没点击下拉框，则关闭下拉框
  if (!clickedInput && !clickedDropdown) {
    isSupplierDropdownOpen.value = false
  }
}

// 过滤后的供应商列表（排除已选）
const filteredSuppliers = computed(() => {
  const query = supplierSearchQuery.value.toLowerCase().trim()
  return suppliers.value.filter(s => {
    const isSelected = newProject.value.invitedSuppliers.includes(s.id)
    const matchesSearch = !query ||
      s.name.toLowerCase().includes(query) ||
      s.code.toLowerCase().includes(query) ||
      s.category.toLowerCase().includes(query)
    return !isSelected && matchesSearch
  })
})

// 已选供应商对象列表
const selectedSupplierObjects = computed(() => {
  return suppliers.value.filter(s => newProject.value.invitedSuppliers.includes(s.id))
})

// 添加供应商
function addSupplier(supplierId: string, event?: MouseEvent) {
  // 阻止事件冒泡，防止触发外部点击
  if (event) {
    event.stopPropagation()
  }
  if (!newProject.value.invitedSuppliers.includes(supplierId)) {
    newProject.value.invitedSuppliers.push(supplierId)
  }
  supplierSearchQuery.value = ''
  // 等待 DOM 更新后重新计算位置
  nextTick(() => {
    updateDropdownPosition()
  })
}

// 移除供应商
function removeSupplier(supplierId: string) {
  const index = newProject.value.invitedSuppliers.indexOf(supplierId)
  if (index > -1) {
    newProject.value.invitedSuppliers.splice(index, 1)
  }
  // 如果下拉框打开，更新位置
  if (isSupplierDropdownOpen.value) {
    nextTick(() => {
      updateDropdownPosition()
    })
  }
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

  // 检查是否有报价
  if (quotations.length === 0) {
    appStore.showToast('暂无供应商报价，无法评标', 'warning')
    return
  }

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
  return Math.round((score.priceScore * 0.3 + score.qualityScore * 0.3 + score.deliveryScore * 0.2 + score.serviceScore * 0.2))
}

// 提交评标结果
function handleSubmitEvaluation() {
  // 保存评标结果
  const evaluations = JSON.parse(localStorage.getItem('sourcing_evaluations') || '{}')
  evaluations[selectedProject.value.id] = evaluationForm.value.scores
  localStorage.setItem('sourcing_evaluations', JSON.stringify(evaluations))

  // 更新项目状态为"评标完成"
  const project = projects.value.find(p => p.id === selectedProject.value.id)
  if (project) {
    project.status = '待定标'
    project.evaluationCompleted = true
  }

  localStorage.setItem('sourcingProjects', JSON.stringify(projects.value))
  appStore.showToast('评标结果已保存', 'success')
  isEvaluationDialogOpen.value = false
}

// 打开定标对话框
function openFinalizeDialog(project: any) {
  selectedProject.value = project

  // 检查是否已完成评标
  if (!project.evaluationCompleted) {
    appStore.showToast('请先完成评标后再进行定标', 'warning')
    return
  }

  // 检查截止日期
  const today = new Date()
  const deadline = new Date(project.deadline)
  if (today < deadline) {
    appStore.showToast(`项目截止日期为 ${project.deadline}，尚未到达定标时间`, 'warning')
  }

  // 获取评标结果
  const evaluations = JSON.parse(localStorage.getItem('sourcing_evaluations') || '{}')
  const scores = evaluations[project.id] || []

  // 默认选择得分最高的供应商
  if (scores.length > 0) {
    const highestScore = scores.reduce((max: any, s: any) =>
      s.totalScore > max.totalScore ? s : max
    )
    finalizeForm.value.selectedSupplierId = highestScore.supplierId
  }

  finalizeForm.value.finalizeReason = ''
  isFinalizeDialogOpen.value = true
}

// 提交定标（生成审批）
function handleSubmitFinalize() {
  if (!finalizeForm.value.selectedSupplierId) {
    appStore.showToast('请选择中标供应商', 'warning')
    return
  }

  if (!finalizeForm.value.finalizeReason.trim()) {
    appStore.showToast('请填写定标说明', 'warning')
    return
  }

  // 创建审批记录
  const approval = {
    id: `APV${Date.now()}`,
    projectId: selectedProject.value.id,
    projectName: selectedProject.value.name,
    type: '定标审批',
    selectedSupplierId: finalizeForm.value.selectedSupplierId,
    selectedSupplierName: suppliers.value.find(s => s.id === finalizeForm.value.selectedSupplierId)?.name || '',
    reason: finalizeForm.value.finalizeReason,
    status: '待审批',
    submitDate: new Date().toISOString(),
    submitBy: '当前用户',
  }

  approvals.value.push(approval)
  localStorage.setItem('sourcing_approvals', JSON.stringify(approvals.value))

  // 更新项目状态
  const project = projects.value.find(p => p.id === selectedProject.value.id)
  if (project) {
    project.status = '待审批'
    project.approvalId = approval.id
  }
  localStorage.setItem('sourcingProjects', JSON.stringify(projects.value))

  appStore.showToast('定标申请已提交，等待审批', 'success')
  isFinalizeDialogOpen.value = false
}

// 打开审批对话框
function openApprovalDialog() {
  isApprovalDialogOpen.value = true
}

// 处理审批
function handleApproval(approval: any, action: 'approve' | 'reject') {
  const approvalIndex = approvals.value.findIndex(a => a.id === approval.id)
  if (approvalIndex === -1) return

  if (action === 'reject') {
    if (!approvalForm.value.comment.trim()) {
      appStore.showToast('请填写拒绝原因', 'warning')
      return
    }
    approvals.value[approvalIndex].status = '已拒绝'
    approvals.value[approvalIndex].rejectReason = approvalForm.value.comment

    // 恢复项目状态
    const project = projects.value.find(p => p.id === approval.projectId)
    if (project) {
      project.status = '待定标'
    }
  } else {
    approvals.value[approvalIndex].status = '已通过'
    approvals.value[approvalIndex].approvalDate = new Date().toISOString()
    approvals.value[approvalIndex].approver = '当前用户'

    // 更新项目状态为"已定标"
    const project = projects.value.find(p => p.id === approval.projectId)
    if (project) {
      project.status = '已定标'
      project.selectedSupplierId = approval.selectedSupplierId
      project.selectedSupplierName = approval.selectedSupplierName
      project.finalizeDate = new Date().toISOString().split('T')[0]
    }

    // 更新报价状态
    const quotations = JSON.parse(localStorage.getItem('sourcing_quotations') || '[]')
    quotations.forEach((q: Quotation) => {
      if (q.projectId === approval.projectId) {
        q.status = q.supplierId === approval.selectedSupplierId ? 'accepted' : 'rejected'
      }
    })
    localStorage.setItem('sourcing_quotations', JSON.stringify(quotations))
  }

  localStorage.setItem('sourcing_approvals', JSON.stringify(approvals.value))
  localStorage.setItem('sourcingProjects', JSON.stringify(projects.value))

  appStore.showToast(action === 'approve' ? '审批通过' : '已拒绝', action === 'approve' ? 'success' : 'info')
  approvalForm.value.comment = ''
}

// 删除项目
function handleDeleteProject(project: any) {
  if (!confirm(`确定要删除项目"${project.name}"吗？`)) return

  const index = projects.value.findIndex(p => p.id === project.id)
  if (index > -1) {
    projects.value.splice(index, 1)
    localStorage.setItem('sourcingProjects', JSON.stringify(projects.value))
    appStore.showToast('项目已删除', 'success')
  }
}

// 保存项目（创建或编辑）
function handleSaveProject() {
  // 清空之前的错误
  formErrors.value = {}

  // 表单验证
  if (!newProject.value.name.trim()) {
    formErrors.value.name = '请输入项目名称'
    return
  }
  if (newProject.value.materials.length === 0) {
    appStore.showToast('请至少添加一个物料', 'warning')
    return
  }
  // 验证每个物料
  for (let i = 0; i < newProject.value.materials.length; i++) {
    const material = newProject.value.materials[i]
    if (!material.materialCode.trim()) {
      appStore.showToast(`第 ${i + 1} 行物料：请输入物料编码`, 'warning')
      return
    }
    if (!material.materialName.trim()) {
      appStore.showToast(`第 ${i + 1} 行物料：请输入物料名称`, 'warning')
      return
    }
    if (material.quantity <= 0) {
      appStore.showToast(`第 ${i + 1} 行物料：请输入有效数量`, 'warning')
      return
    }
    if (material.targetPrice <= 0) {
      appStore.showToast(`第 ${i + 1} 行物料：请输入有效目标单价`, 'warning')
      return
    }
  }
  if (newProject.value.invitedSuppliers.length === 0) {
    appStore.showToast('请至少邀请一家供应商', 'warning')
    return
  }
  if (!newProject.value.deadline) {
    formErrors.value.deadline = '请选择截止日期'
    return
  }

  const calculatedTarget = calculateProjectTarget()

  if (selectedProject.value) {
    // 编辑模式
    const project = projects.value.find(p => p.id === selectedProject.value.id)
    if (project) {
      project.name = newProject.value.name.trim()
      project.category = newProject.value.category
      project.description = newProject.value.description
      project.targetAmount = calculatedTarget
      project.deadline = newProject.value.deadline
      project.invitedSuppliers = [...newProject.value.invitedSuppliers]
      project.materials = [...newProject.value.materials]
    }
    appStore.showToast('项目已更新', 'success')
  } else {
    // 创建模式
    const project = {
      id: `SP${String(projects.value.length + 1).padStart(3, '0')}`,
      name: newProject.value.name.trim(),
      category: newProject.value.category,
      description: newProject.value.description,
      status: '询价中',
      targetAmount: calculatedTarget,
      currentBestPrice: 0,
      savingRate: 0,
      supplierCount: newProject.value.invitedSuppliers.length,
      quotedCount: 0,
      deadline: newProject.value.deadline,
      invitedSuppliers: [...newProject.value.invitedSuppliers],
      materials: [...newProject.value.materials],
      createdAt: new Date().toISOString(),
      evaluationCompleted: false,
    }
    projects.value.push(project)
    appStore.showToast('寻源项目创建成功', 'success')
  }

  localStorage.setItem('sourcingProjects', JSON.stringify(projects.value))
  isCreateDialogOpen.value = false
  selectedProject.value = null
}

// ==================== 发送询价单功能 ====================

// 询价单发送记录
interface InquiryInvitation {
  id: string
  projectId: string
  projectName: string
  supplierIds: string[]
  sendDate: string
  status: 'pending' | 'viewed' | 'quoted'
}

const inquiryInvitations = ref<InquiryInvitation[]>([])

// 加载询价单记录
function loadInquiryInvitations() {
  const saved = localStorage.getItem('sourcing_inquiries')
  if (saved) {
    try {
      inquiryInvitations.value = JSON.parse(saved)
    } catch {
      inquiryInvitations.value = []
    }
  }
}

// 发送询价单
function handleSendInquiry(project: any) {
  // 检查是否有邀请的供应商
  if (!project.invitedSuppliers || project.invitedSuppliers.length === 0) {
    appStore.showToast('该项目没有邀请供应商，请先编辑项目添加供应商', 'warning')
    return
  }

  // 创建询价单记录
  const invitation: InquiryInvitation = {
    id: `INQ${Date.now()}`,
    projectId: project.id,
    projectName: project.name,
    supplierIds: [...project.invitedSuppliers],
    sendDate: new Date().toISOString(),
    status: 'pending',
  }

  inquiryInvitations.value.push(invitation)
  localStorage.setItem('sourcing_inquiries', JSON.stringify(inquiryInvitations.value))

  // 更新项目状态为"报价中"
  const projectIndex = projects.value.findIndex(p => p.id === project.id)
  if (projectIndex >= 0) {
    projects.value[projectIndex].status = '报价中'
    projects.value[projectIndex].inquirySentDate = new Date().toISOString()
    localStorage.setItem('sourcingProjects', JSON.stringify(projects.value))
  }

  appStore.showToast(`询价单已发送给 ${project.invitedSuppliers.length} 家供应商`, 'success')
}

// 获取项目的询价单状态
function getInquiryStatus(project: any) {
  const invitation = inquiryInvitations.value.find(i => i.projectId === project.id)
  return invitation ? invitation.status : null
}

// ==================== 项目进度跟踪功能 ====================

// 项目进度阶段
function getProjectProgress(project: any) {
  const stages = [
    { key: 'created', label: '项目创建', completed: true, date: project.createdAt ? new Date(project.createdAt).toLocaleDateString() : '-' },
    { key: 'inquiry', label: '发送询价', completed: !!project.inquirySentDate, date: project.inquirySentDate ? new Date(project.inquirySentDate).toLocaleDateString() : '-' },
    { key: 'quoting', label: '报价收集中', completed: (project.quotedCount || 0) >= (project.supplierCount || 0) && (project.quotedCount || 0) > 0, date: '-' },
    { key: 'evaluation', label: '评标完成', completed: !!project.evaluationCompleted, date: '-' },
    { key: 'approval', label: '审批完成', completed: project.status === '已定标' || project.status === '已完成', date: project.finalizeDate ? new Date(project.finalizeDate).toLocaleDateString() : '-' },
    { key: 'finalized', label: '项目完成', completed: project.status === '已完成', date: '-' },
  ]
  return stages
}

// 获取项目进度百分比
function getProjectProgressPercent(project: any) {
  const stages = getProjectProgress(project)
  const completed = stages.filter(s => s.completed).length
  return Math.round((completed / stages.length) * 100)
}

// ==================== 报价有效期提醒功能 ====================

// 即将到期的报价列表
const expiringQuotations = ref<any[]>([])

// 检查报价有效期
function checkExpiringQuotations() {
  const allQuotations = JSON.parse(localStorage.getItem('sourcing_quotations') || '[]')
  const today = new Date()
  const warnings: any[] = []

  allQuotations.forEach((q: any) => {
    if (q.status === 'pending' || q.status === 'reviewing') {
      const validUntil = new Date(q.validUntil)
      const diffTime = validUntil.getTime() - today.getTime()
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))

      // 7天内到期或已过期
      if (diffDays <= 7 && diffDays >= -30) {
        const project = projects.value.find(p => p.id === q.projectId)
        warnings.push({
          quoteId: q.id,
          projectName: project?.name || q.projectId,
          supplierName: q.supplierName,
          validUntil: q.validUntil,
          days: diffDays,
          status: diffDays < 0 ? 'expired' : diffDays <= 3 ? 'urgent' : 'warning',
        })
      }
    }
  })

  expiringQuotations.value = warnings
  return warnings
}

// 获取状态标签样式
function getStatusClass(status: string) {
  const statusMap: Record<string, string> = {
    '询价中': 'bg-blue-600 text-white',
    '报价中': 'bg-yellow-600 text-white',
    '待定标': 'bg-purple-600 text-white',
    '待审批': 'bg-orange-600 text-white',
    '已定标': 'bg-green-600 text-white',
    '已完成': 'bg-gray-600 text-white',
    '已拒绝': 'bg-red-600 text-white',
  }
  return statusMap[status] || 'bg-gray-100 text-gray-700'
}

// 初始化数据
onMounted(() => {
  // 获取供应商列表 - 从 mockData 加载
  suppliers.value = mockSuppliers

  // 获取寻源项目列表
  const savedProjects = localStorage.getItem('sourcingProjects')
  if (savedProjects) {
    try {
      projects.value = JSON.parse(savedProjects)
    } catch {
      projects.value = []
    }
  }

  // 获取审批列表
  const savedApprovals = localStorage.getItem('sourcing_approvals')
  if (savedApprovals) {
    try {
      approvals.value = JSON.parse(savedApprovals)
    } catch {
      approvals.value = []
    }
  }

  // 如果没有项目，创建示例数据
  if (projects.value.length === 0) {
    projects.value = [
      {
        id: 'SP001',
        name: '2026年Q2铝合金锅体采购',
        category: '原材料',
        description: '采购铝合金锅体原材料，用于厨房用品生产线',
        status: '询价中',
        targetAmount: 3500000,
        currentBestPrice: 2850000,
        savingRate: 18.6,
        supplierCount: 3,
        quotedCount: 2,
        deadline: '2026-04-15',
        invitedSuppliers: ['S001', 'S002', 'S003'],
        materials: [
          { id: 'M001', materialCode: 'ADC12-001', materialName: 'ADC12铝合金锭', quantity: 50000, unit: 'KG', targetPrice: 57, description: '铝合金锭，符合ADC12标准' },
        ],
        createdAt: new Date().toISOString(),
        evaluationCompleted: false,
      },
      {
        id: 'SP002',
        name: '不粘涂层供应商寻源',
        category: '辅料',
        description: '寻找优质不粘涂层供应商，用于不粘锅产品',
        status: '报价中',
        targetAmount: 1200000,
        currentBestPrice: 1050000,
        savingRate: 12.5,
        supplierCount: 2,
        quotedCount: 2,
        deadline: '2026-04-20',
        invitedSuppliers: ['S003', 'S004'],
        materials: [
          { id: 'M002', materialCode: 'TC-001', materialName: '特氟龙不粘涂层', quantity: 1000, unit: 'KG', targetPrice: 800, description: '食品级特氟龙涂层' },
        ],
        createdAt: new Date().toISOString(),
        evaluationCompleted: false,
      },
    ]
    localStorage.setItem('sourcingProjects', JSON.stringify(projects.value))
  }

  // 加载询价单记录
  loadInquiryInvitations()

  // 检查报价有效期
  checkExpiringQuotations()

  // 添加点击外部关闭下拉框的事件监听
  document.addEventListener('click', handleClickOutside)
})

// 组件卸载时移除事件监听
onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})

// 计算属性
const mockSourcingProjects = computed(() => projects.value)

const pendingApprovals = computed(() => approvals.value.filter(a => a.status === '待审批'))
</script>

<template>
  <div class="space-y-6">
    <!-- 标题 -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-semibold text-gray-900">{{ appStore.t('page.sourcing.title') }}</h1>
        <p class="text-sm text-gray-500 mt-1">精细化核价 · 阳光采购 · 成本优化</p>
      </div>
      <div class="flex items-center gap-3">
        <!-- 审批提醒 -->
        <button v-if="pendingApprovals.length > 0" @click="openApprovalDialog" class="relative flex items-center gap-2 px-4 py-2 bg-orange-600 text-white rounded-lg text-sm font-medium hover:bg-orange-700">
          <AlertCircle class="w-4 h-4" />
          待审批 ({{ pendingApprovals.length }})
        </button>
        <button class="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700" @click="openCreateDialog">
          <Plus class="w-4 h-4" />{{ appStore.t('btn.createSourcingProject') }}
        </button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-5 gap-4">
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div><p class="text-sm text-gray-500">进行中项目</p><p class="text-2xl font-semibold text-gray-900 mt-1">{{ projects.filter(p => !['已定标', '已完成', '已拒绝'].includes(p.status)).length }}</p></div>
          <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center"><Target class="w-6 h-6 text-blue-600" /></div>
        </div>
      </div>
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div><p class="text-sm text-gray-500">参与供应商</p><p class="text-2xl font-semibold text-gray-900 mt-1">{{ suppliers.length }}</p></div>
          <div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center"><Users class="w-6 h-6 text-green-600" /></div>
        </div>
      </div>
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div><p class="text-sm text-gray-500">目标总金额</p><p class="text-2xl font-semibold text-gray-900 mt-1">{{ (projects.reduce((sum, p) => sum + p.targetAmount, 0) / 10000).toFixed(0) }}万</p></div>
          <div class="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center"><DollarSign class="w-6 h-6 text-purple-600" /></div>
        </div>
      </div>
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div><p class="text-sm text-gray-500">平均节约率</p><p class="text-2xl font-semibold text-green-600 mt-1">{{ projects.length > 0 ? (projects.reduce((sum, p) => sum + p.savingRate, 0) / projects.length).toFixed(1) : 0 }}%</p></div>
          <div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center"><TrendingUp class="w-6 h-6 text-green-600" /></div>
        </div>
      </div>
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div><p class="text-sm text-gray-500">待审批</p><p class="text-2xl font-semibold text-orange-600 mt-1">{{ pendingApprovals.length }}</p></div>
          <div class="w-12 h-12 bg-orange-100 rounded-lg flex items-center justify-center"><Clock class="w-6 h-6 text-orange-600" /></div>
        </div>
      </div>
    </div>

    <!-- 报价有效期提醒 -->
    <div v-if="expiringQuotations.length > 0" class="p-4 bg-orange-50 border border-orange-200 rounded-lg">
      <div class="flex items-center gap-3 mb-3">
        <AlertCircle class="w-5 h-5 text-orange-600" />
        <h4 class="font-medium text-orange-900">报价有效期提醒</h4>
      </div>
      <p class="text-sm text-orange-800 mb-3">以下报价即将到期或已过期，请及时跟进：</p>
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
        <div v-for="warning in expiringQuotations" :key="warning.quoteId" class="p-3 bg-white rounded-lg border" :class="warning.status === 'expired' ? 'border-red-200' : warning.status === 'urgent' ? 'border-orange-200' : 'border-yellow-200'">
          <div class="flex items-start justify-between mb-2">
            <div class="flex-1">
              <p class="text-sm font-medium text-gray-900">{{ warning.projectName }}</p>
              <p class="text-xs text-gray-500">{{ warning.supplierName }}</p>
            </div>
            <span class="text-xs px-2 py-1 rounded" :class="warning.status === 'expired' ? 'bg-red-100 text-red-700' : warning.status === 'urgent' ? 'bg-orange-100 text-orange-700' : 'bg-yellow-100 text-yellow-700'">
              {{ warning.status === 'expired' ? '已过期' : `${warning.days}天后到期` }}
            </span>
          </div>
          <p class="text-xs text-gray-600">有效期至: {{ warning.validUntil }}</p>
        </div>
      </div>
    </div>

    <!-- 选项卡 -->
    <div class="bg-white rounded-lg border border-gray-200">
      <div class="flex border-b border-gray-200">
        <button v-for="tab in [{ id: 'projects', label: '寻源项目' }, { id: 'quotations', label: '报价对比' }, { id: 'approvals', label: '审批管理' }]" :key="tab.id" class="px-6 py-3 text-sm font-medium transition-colors" :class="activeTab === tab.id ? 'border-b-2 border-blue-600 text-blue-600' : 'text-gray-600 hover:text-gray-900'" @click="activeTab = tab.id">
          {{ tab.label }}
          <span v-if="tab.id === 'approvals' && pendingApprovals.length > 0" class="ml-2 px-2 py-0.5 bg-orange-100 text-orange-800 rounded-full text-xs">{{ pendingApprovals.length }}</span>
        </button>
      </div>

      <div class="p-6">
        <!-- 寻源项目 -->
        <div v-if="activeTab === 'projects'" class="space-y-4">
          <div class="flex justify-end">
            <div class="flex items-center border border-gray-300 rounded-lg p-1">
              <button class="px-3 py-1.5 rounded-md text-sm flex items-center gap-1" :class="viewMode === 'grid' ? 'bg-blue-600 text-white' : 'text-gray-700 hover:bg-gray-100'" @click="viewMode = 'grid'">
                <LayoutGrid class="w-4 h-4" />{{ appStore.t('btn.grid') }}
              </button>
              <button class="px-3 py-1.5 rounded-md text-sm flex items-center gap-1" :class="viewMode === 'list' ? 'bg-blue-600 text-white' : 'text-gray-700 hover:bg-gray-100'" @click="viewMode = 'list'">
                <List class="w-4 h-4" />{{ appStore.t('btn.list') }}
              </button>
            </div>
          </div>

          <!-- 网格视图 -->
          <div v-if="viewMode === 'grid'" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <div v-for="project in mockSourcingProjects" :key="project.id" class="border border-gray-200 rounded-lg p-5 hover:shadow-md transition-shadow">
              <div class="flex items-start justify-between mb-3">
                <div class="flex-1">
                  <div class="flex items-center gap-2 mb-2 flex-wrap">
                    <h3 class="font-semibold text-gray-900">{{ project.name }}</h3>
                    <span class="px-2 py-0.5 text-white rounded-full text-xs font-medium" :class="getStatusClass(project.status)">{{ project.status }}</span>
                  </div>
                  <p class="text-sm text-gray-500">项目编号: {{ project.id }}</p>
                  <p v-if="project.description" class="text-sm text-gray-600 mt-1 line-clamp-2">{{ project.description }}</p>
                </div>
              </div>

              <!-- 物料信息 -->
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
                <div class="p-2 bg-blue-50 rounded-lg"><p class="text-xs text-gray-600">已报价</p><p class="text-sm font-semibold text-blue-600">{{ project.quotedCount || 0 }}/{{ project.supplierCount }}家</p></div>
              </div>

              <div class="flex items-center justify-between text-xs text-gray-500 mb-3">
                <span>截止: {{ project.deadline }}</span>
                <span v-if="project.currentBestPrice > 0" class="text-green-600">最优: ¥{{ (project.currentBestPrice / 10000).toFixed(1) }}万</span>
              </div>

              <div class="flex gap-2 flex-wrap">
                <button @click="openDetailDialog(project)" class="flex-1 min-w-[60px] px-3 py-1.5 border border-gray-300 rounded-lg text-xs text-gray-700 hover:bg-gray-50 flex items-center justify-center gap-1">
                  <Eye class="w-3 h-3" />详情
                </button>
                <button v-if="project.status === '询价中'" @click="handleSendInquiry(project)" class="flex-1 min-w-[60px] px-3 py-1.5 bg-green-600 text-white rounded-lg text-xs hover:bg-green-700 flex items-center justify-center gap-1">
                  <Send class="w-3 h-3" />发送询价
                </button>
                <button @click="openEditDialog(project)" class="flex-1 min-w-[60px] px-3 py-1.5 border border-gray-300 rounded-lg text-xs text-gray-700 hover:bg-gray-50 flex items-center justify-center gap-1">
                  <Edit class="w-3 h-3" />编辑
                </button>
                <button @click="openQuoteDetailDialog(project)" class="flex-1 min-w-[60px] px-3 py-1.5 border border-gray-300 rounded-lg text-xs text-gray-700 hover:bg-gray-50 flex items-center justify-center gap-1">
                  <FileText class="w-3 h-3" />报价
                </button>
                <button v-if="project.status === '待定标'" @click="openEvaluationDialog(project)" class="flex-1 min-w-[60px] px-3 py-1.5 border border-gray-300 rounded-lg text-xs text-gray-700 hover:bg-gray-50 flex items-center justify-center gap-1">
                  <Award class="w-3 h-3" />评标
                </button>
                <button v-if="project.status === '待定标'" @click="openFinalizeDialog(project)" class="flex-1 min-w-[60px] px-3 py-1.5 bg-blue-600 text-white rounded-lg text-xs hover:bg-blue-700 flex items-center justify-center gap-1">
                  <Check class="w-3 h-3" />定标
                </button>
                <button @click="handleDeleteProject(project)" class="px-3 py-1.5 border border-red-300 rounded-lg text-xs text-red-600 hover:bg-red-50">
                  <Trash2 class="w-3 h-3" />
                </button>
              </div>

              <!-- 项目进度跟踪 -->
              <div class="mt-3 pt-3 border-t border-gray-100">
                <div class="flex items-center justify-between mb-2">
                  <p class="text-xs font-medium text-gray-700">项目进度</p>
                  <span class="text-xs font-medium" :class="getProjectProgressPercent(project) >= 80 ? 'text-green-600' : getProjectProgressPercent(project) >= 50 ? 'text-blue-600' : 'text-gray-600'">{{ getProjectProgressPercent(project) }}%</span>
                </div>
                <div class="flex items-center gap-1">
                  <div v-for="(stage, idx) in getProjectProgress(project)" :key="stage.key" class="flex-1 flex flex-col items-center">
                    <div class="w-full h-1 bg-gray-200 rounded-full mb-1">
                      <div class="h-1 rounded-full transition-all" :class="stage.completed ? 'bg-green-500' : 'bg-gray-300'" :style="stage.completed ? 'width: 100%' : 'width: 0%'"></div>
                    </div>
                    <p class="text-xs" :class="stage.completed ? 'text-gray-900' : 'text-gray-400'" :title="stage.date">{{ stage.label }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 列表视图 -->
          <div v-else class="border border-gray-200 rounded-lg overflow-x-auto">
            <table class="w-full min-w-max text-sm">
              <thead class="bg-gray-50">
                <tr>
                  <th class="text-left px-4 py-3 font-medium text-gray-700">项目编号</th>
                  <th class="text-left px-4 py-3 font-medium text-gray-700">项目名称</th>
                  <th class="text-left px-4 py-3 font-medium text-gray-700">分类</th>
                  <th class="text-left px-4 py-3 font-medium text-gray-700">状态</th>
                  <th class="text-right px-4 py-3 font-medium text-gray-700">目标金额</th>
                  <th class="text-right px-4 py-3 font-medium text-gray-700">已报价</th>
                  <th class="text-left px-4 py-3 font-medium text-gray-700">截止日期</th>
                  <th class="text-left px-4 py-3 font-medium text-gray-700">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="project in mockSourcingProjects" :key="project.id" class="border-t border-gray-200">
                  <td class="px-4 py-3 text-gray-900">{{ project.id }}</td>
                  <td class="px-4 py-3">
                    <div class="font-medium text-gray-900">{{ project.name }}</div>
                    <div v-if="project.materials" class="text-xs text-gray-500">{{ project.materials.length }}个物料</div>
                  </td>
                  <td class="px-4 py-3 text-gray-900">{{ project.category }}</td>
                  <td class="px-4 py-3">
                    <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="getStatusClass(project.status)">{{ project.status }}</span>
                  </td>
                  <td class="px-4 py-3 text-right text-gray-900">¥{{ project.targetAmount.toLocaleString() }}</td>
                  <td class="px-4 py-3 text-right text-blue-600">{{ project.quotedCount || 0 }}/{{ project.supplierCount }}家</td>
                  <td class="px-4 py-3 text-gray-900">{{ project.deadline }}</td>
                  <td class="px-4 py-3">
                    <div class="flex gap-2 flex-wrap">
                      <button @click="openDetailDialog(project)" class="px-2 py-1 border border-gray-300 rounded text-xs text-gray-700 hover:bg-gray-50">详情</button>
                      <button v-if="project.status === '询价中'" @click="handleSendInquiry(project)" class="px-2 py-1 border border-green-300 rounded text-xs text-green-700 hover:bg-green-50">发送询价</button>
                      <button @click="openQuoteDetailDialog(project)" class="px-2 py-1 border border-gray-300 rounded text-xs text-gray-700 hover:bg-gray-50">报价</button>
                      <button v-if="project.status === '待定标'" @click="openEvaluationDialog(project)" class="px-2 py-1 border border-gray-300 rounded text-xs text-gray-700 hover:bg-gray-50">评标</button>
                      <button v-if="project.status === '待定标'" @click="openFinalizeDialog(project)" class="px-2 py-1 bg-blue-600 text-white rounded text-xs hover:bg-blue-700">定标</button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- 报价对比 -->
        <div v-if="activeTab === 'quotations'">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-base font-semibold text-gray-900">报价对比分析</h3>
            <select v-model="selectedProject" class="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
              <option :value="null">请选择项目</option>
              <option v-for="project in projects.filter(p => p.quotedCount > 0)" :key="project.id" :value="project">{{ project.name }}</option>
            </select>
          </div>

          <div v-if="!selectedProject" class="text-center py-12 text-gray-500">
            <FileText class="w-12 h-12 mx-auto mb-3 text-gray-300" />
            <p>请选择一个项目查看报价对比</p>
          </div>

          <div v-else class="space-y-4">
            <div class="border border-gray-200 rounded-lg overflow-hidden">
              <table class="w-full text-sm">
                <thead class="bg-gray-50">
                  <tr>
                    <th class="text-left px-4 py-3 font-medium text-gray-700">供应商</th>
                    <th class="text-left px-4 py-3 font-medium text-gray-700">物料</th>
                    <th class="text-right px-4 py-3 font-medium text-gray-700">数量</th>
                    <th class="text-right px-4 py-3 font-medium text-gray-700">单价</th>
                    <th class="text-right px-4 py-3 font-medium text-gray-700">总价</th>
                    <th class="text-left px-4 py-3 font-medium text-gray-700">交期</th>
                    <th class="text-left px-4 py-3 font-medium text-gray-700">有效期至</th>
                    <th class="text-left px-4 py-3 font-medium text-gray-700">状态</th>
                  </tr>
                </thead>
                <tbody>
                  <template v-for="(quote, idx) in getProjectQuotations(selectedProject.id)" :key="quote.id">
                    <tr v-for="(item, itemIdx) in quote.items" :key="`${quote.id}-${itemIdx}`" class="border-t border-gray-200" :class="idx === 0 && itemIdx === 0 ? 'bg-green-50' : ''">
                      <td v-if="itemIdx === 0" :rowspan="quote.items.length" class="px-4 py-3 text-sm text-gray-900">
                        {{ quote.supplierName }}
                        <span v-if="idx === 0" class="ml-2 px-2 py-0.5 bg-green-600 text-white rounded-full text-xs">最优</span>
                      </td>
                      <td class="px-4 py-3 text-sm text-gray-900">{{ item.materialName }}</td>
                      <td class="px-4 py-3 text-sm text-right text-gray-900">{{ item.quantity.toLocaleString() }}</td>
                      <td class="px-4 py-3 text-sm text-right text-gray-900">¥{{ item.unitPrice.toLocaleString() }}</td>
                      <td class="px-4 py-3 text-sm text-right font-medium text-gray-900">¥{{ item.totalPrice.toLocaleString() }}</td>
                      <td v-if="itemIdx === 0" :rowspan="quote.items.length" class="px-4 py-3 text-sm text-gray-900">{{ quote.deliveryTime }}</td>
                      <td v-if="itemIdx === 0" :rowspan="quote.items.length" class="px-4 py-3 text-sm text-gray-900">{{ quote.validUntil }}</td>
                      <td v-if="itemIdx === 0" :rowspan="quote.items.length" class="px-4 py-3 text-sm">
                        <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="quote.status === 'accepted' ? 'bg-green-100 text-green-800' : quote.status === 'rejected' ? 'bg-red-100 text-red-800' : 'bg-gray-100 text-gray-700'">
                          {{ quote.status === 'accepted' ? '已中标' : quote.status === 'rejected' ? '未中标' : '待定标' }}
                        </span>
                      </td>
                    </tr>
                    <tr class="border-t border-gray-200 bg-gray-50">
                      <td colspan="4" class="px-4 py-2 text-right text-sm font-medium text-gray-700">合计</td>
                      <td class="px-4 py-2 text-right text-sm font-bold text-gray-900">¥{{ quote.totalAmount.toLocaleString() }}</td>
                      <td colspan="3"></td>
                    </tr>
                  </template>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <!-- 审批管理 -->
        <div v-if="activeTab === 'approvals'" class="space-y-4">
          <div v-if="approvals.length === 0" class="text-center py-12 text-gray-500">
            <Check class="w-12 h-12 mx-auto mb-3 text-gray-300" />
            <p>暂无审批记录</p>
          </div>
          <div v-else class="space-y-3">
            <div v-for="approval in approvals" :key="approval.id" class="border border-gray-200 rounded-lg p-4">
              <div class="flex items-start justify-between mb-3">
                <div class="flex-1">
                  <div class="flex items-center gap-2 mb-1">
                    <h4 class="font-semibold text-gray-900">{{ approval.projectName }}</h4>
                    <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="approval.status === '待审批' ? 'bg-orange-100 text-orange-800' : approval.status === '已通过' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'">{{ approval.status }}</span>
                  </div>
                  <p class="text-sm text-gray-600">类型: {{ approval.type }}</p>
                  <p class="text-sm text-gray-600">中标供应商: {{ approval.selectedSupplierName }}</p>
                </div>
                <div class="text-right text-sm text-gray-500">
                  <p>提交时间: {{ new Date(approval.submitDate).toLocaleString() }}</p>
                  <p>提交人: {{ approval.submitBy }}</p>
                </div>
              </div>
              <div class="p-3 bg-gray-50 rounded-lg mb-3">
                <p class="text-sm text-gray-700"><span class="font-medium">定标说明:</span> {{ approval.reason }}</p>
              </div>
              <div v-if="approval.status === '待审批'" class="flex justify-end gap-2">
                <button @click="handleApproval(approval, 'reject')" class="px-4 py-2 border border-red-300 rounded-lg text-sm text-red-600 hover:bg-red-50">拒绝</button>
                <button @click="handleApproval(approval, 'approve')" class="px-4 py-2 bg-green-600 text-white rounded-lg text-sm hover:bg-green-700">通过</button>
              </div>
              <div v-else-if="approval.status === '已拒绝'" class="p-3 bg-red-50 rounded-lg">
                <p class="text-sm text-red-700"><span class="font-medium">拒绝原因:</span> {{ approval.rejectReason }}</p>
              </div>
              <div v-else class="p-3 bg-green-50 rounded-lg">
                <p class="text-sm text-green-700">审批人: {{ approval.approver }} | 审批时间: {{ new Date(approval.approvalDate).toLocaleString() }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 创建/编辑项目弹窗 -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="isCreateDialogOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-black/50" @click="isCreateDialogOpen = false" />
          <div class="relative bg-white rounded-xl shadow-xl w-full max-w-4xl max-h-[90vh] overflow-hidden flex flex-col">
            <div class="flex items-center justify-between p-4 border-b">
              <h3 class="text-lg font-semibold text-gray-900">{{ selectedProject ? '编辑寻源项目' : '创建寻源项目' }}</h3>
              <button @click="isCreateDialogOpen = false" class="p-2 hover:bg-gray-100 rounded-lg"><X class="w-5 h-5" /></button>
            </div>

            <div class="p-6 overflow-y-auto flex-1 space-y-4">
              <!-- 基本信息 -->
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">项目名称 <span class="text-red-500">*</span></label>
                  <input v-model="newProject.name" placeholder="请输入项目名称" :class="['w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2', formErrors.name ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500']" />
                  <p v-if="formErrors.name" class="text-red-500 text-xs mt-1 flex items-center gap-1"><XCircle class="w-3 h-3" />{{ formErrors.name }}</p>
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
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">项目描述</label>
                <textarea v-model="newProject.description" rows="2" placeholder="请输入项目描述、技术要求等" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>

              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">截止日期 <span class="text-red-500">*</span></label>
                  <input v-model="newProject.deadline" type="date" :class="['w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2', formErrors.deadline ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500']" />
                  <p v-if="formErrors.deadline" class="text-red-500 text-xs mt-1 flex items-center gap-1"><XCircle class="w-3 h-3" />{{ formErrors.deadline }}</p>
                </div>
                <div class="flex items-end">
                  <div class="flex-1 p-3 bg-blue-50 rounded-lg">
                    <p class="text-xs text-blue-600">预计目标金额</p>
                    <p class="text-lg font-semibold text-blue-900">¥{{ calculateProjectTarget().toLocaleString() }}</p>
                  </div>
                </div>
              </div>

              <!-- 物料明细 -->
              <div>
                <div class="flex items-center justify-between mb-2">
                  <label class="block text-sm font-medium text-gray-700">物料明细 <span class="text-red-500">*</span></label>
                  <button @click="addMaterial" class="flex items-center gap-1 px-3 py-1 bg-blue-600 text-white rounded text-sm hover:bg-blue-700">
                    <Plus class="w-4 h-4" />添加物料
                  </button>
                </div>
                <div class="border border-gray-200 rounded-lg overflow-hidden">
                  <table class="w-full text-sm">
                    <thead class="bg-gray-50">
                      <tr>
                        <th class="text-left px-3 py-2 font-medium text-gray-700">物料编码*</th>
                        <th class="text-left px-3 py-2 font-medium text-gray-700">物料名称*</th>
                        <th class="text-right px-3 py-2 font-medium text-gray-700">数量*</th>
                        <th class="text-left px-3 py-2 font-medium text-gray-700">单位</th>
                        <th class="text-right px-3 py-2 font-medium text-gray-700">目标单价*</th>
                        <th class="text-right px-3 py-2 font-medium text-gray-700">小计</th>
                        <th class="text-left px-3 py-2 font-medium text-gray-700">备注</th>
                        <th class="text-left px-3 py-2 font-medium text-gray-700 w-20">操作</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="(material, idx) in newProject.materials" :key="material.id" class="border-t border-gray-200">
                        <td class="px-2 py-2"><input v-model="material.materialCode" placeholder="编码" class="w-full px-2 py-1 border border-gray-300 rounded text-sm" /></td>
                        <td class="px-2 py-2"><input v-model="material.materialName" placeholder="名称" class="w-full px-2 py-1 border border-gray-300 rounded text-sm" /></td>
                        <td class="px-2 py-2"><input v-model.number="material.quantity" type="number" min="1" class="w-20 px-2 py-1 border border-gray-300 rounded text-sm text-right" /></td>
                        <td class="px-2 py-2">
                          <select v-model="material.unit" class="w-16 px-2 py-1 border border-gray-300 rounded text-sm">
                            <option value="个">个</option>
                            <option value="套">套</option>
                            <option value="件">件</option>
                            <option value="KG">KG</option>
                            <option value="米">米</option>
                            <option value="张">张</option>
                          </select>
                        </td>
                        <td class="px-2 py-2"><input v-model.number="material.targetPrice" type="number" min="0" step="0.01" class="w-24 px-2 py-1 border border-gray-300 rounded text-sm text-right" /></td>
                        <td class="px-2 py-2 text-right text-sm text-gray-900">¥{{ ((material.quantity || 0) * (material.targetPrice || 0)).toLocaleString() }}</td>
                        <td class="px-2 py-2"><input v-model="material.description" placeholder="备注" class="w-full px-2 py-1 border border-gray-300 rounded text-sm" /></td>
                        <td class="px-2 py-2">
                          <button @click="removeMaterial(material.id)" class="p-1 text-red-600 hover:bg-red-50 rounded"><Trash2 class="w-4 h-4" /></button>
                        </td>
                      </tr>
                      <tr v-if="newProject.materials.length === 0">
                        <td colspan="8" class="px-4 py-8 text-center text-gray-500">暂无物料，请点击"添加物料"按钮添加</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>

              <!-- 邀请供应商 - 方案B: 搜索下拉 + 标签 -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">邀请供应商 <span class="text-red-500">*</span></label>

                <!-- 已选供应商标签 -->
                <div v-if="selectedSupplierObjects.length > 0" class="flex flex-wrap gap-2 mb-2">
                  <span v-for="supplier in selectedSupplierObjects" :key="supplier.id" class="inline-flex items-center gap-1 px-3 py-1.5 bg-blue-50 text-blue-700 rounded-lg text-sm">
                    {{ supplier.name }}
                    <button @click="removeSupplier(supplier.id)" class="hover:text-blue-900">
                      <X class="w-3 h-3" />
                    </button>
                  </span>
                </div>

                <!-- 搜索下拉框 -->
                <div class="relative">
                  <div class="relative" ref="supplierSearchInputRef">
                    <input
                      v-model="supplierSearchQuery"
                      @focus="openDropdown"
                      type="text"
                      placeholder="搜索供应商名称/编码/分类..."
                      class="w-full px-3 py-2 pl-9 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                    <svg class="w-4 h-4 text-gray-400 absolute left-3 top-1/2 -translate-y-1/2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                    </svg>
                  </div>

                  <!-- 下拉列表 - Teleport 到 body -->
                  <Teleport to="body">
                    <div
                      ref="supplierDropdownRef"
                      v-if="isSupplierDropdownOpen && filteredSuppliers.length > 0"
                      class="fixed bg-white border border-gray-200 rounded-lg shadow-lg max-h-60 overflow-y-auto z-[100]"
                      :style="dropdownStyle"
                    >
                      <div
                        v-for="supplier in filteredSuppliers"
                        :key="supplier.id"
                        @click="addSupplier(supplier.id, $event)"
                        class="px-4 py-2.5 hover:bg-blue-50 cursor-pointer border-b border-gray-100 last:border-0"
                      >
                        <div class="flex items-center justify-between">
                          <div>
                            <p class="text-sm font-medium text-gray-900">{{ supplier.name }}</p>
                            <p class="text-xs text-gray-500">{{ supplier.code }} · {{ supplier.category }}</p>
                          </div>
                          <span class="text-xs px-2 py-0.5 rounded-full" :class="{
                            'bg-blue-100 text-blue-800': supplier.category === '战略',
                            'bg-green-100 text-green-800': supplier.category === '关键',
                            'bg-gray-100 text-gray-800': supplier.category === '杠杆'
                          }">{{ supplier.overallScore }}分</span>
                        </div>
                      </div>
                    </div>

                    <!-- 无结果提示 -->
                    <div
                      v-if="isSupplierDropdownOpen && filteredSuppliers.length === 0 && supplierSearchQuery"
                      class="fixed bg-white border border-gray-200 rounded-lg shadow-lg p-4 text-center text-sm text-gray-500 z-[100]"
                      :style="dropdownStyle"
                    >
                      未找到匹配的供应商
                    </div>
                  </Teleport>
                </div>

                <p class="text-xs text-gray-500 mt-1">已选择 {{ newProject.invitedSuppliers.length }} 家供应商</p>
              </div>
            </div>

            <div class="flex justify-end gap-3 p-4 border-t bg-gray-50">
              <button type="button" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50" @click="isCreateDialogOpen = false">取消</button>
              <button type="button" class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700" @click="handleSaveProject">{{ selectedProject ? '保存修改' : '创建项目' }}</button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- 项目详情对话框 -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="isDetailDialogOpen && selectedProject" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-black/50" @click="isDetailDialogOpen = false" />
          <div class="relative bg-white rounded-xl shadow-xl w-full max-w-3xl max-h-[90vh] overflow-hidden flex flex-col">
            <div class="flex items-center justify-between p-4 border-b">
              <h3 class="text-lg font-semibold text-gray-900">项目详情</h3>
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
                  <div><span class="text-gray-600">项目状态:</span> <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="getStatusClass(selectedProject.status)">{{ selectedProject.status }}</span></div>
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

              <!-- 邀请供应商 -->
              <div>
                <h4 class="font-medium text-gray-900 mb-3">邀请供应商 ({{ selectedProject.supplierCount }}家)</h4>
                <div class="grid grid-cols-2 gap-2">
                  <div v-for="supplierId in selectedProject.invitedSuppliers || []" :key="supplierId" class="flex items-center gap-2 p-2 border border-gray-200 rounded-lg">
                    <Users class="w-4 h-4 text-gray-500" />
                    <span class="text-sm text-gray-900">{{ suppliers.find(s => s.id === supplierId)?.name || supplierId }}</span>
                  </div>
                </div>
              </div>

              <!-- 报价情况 -->
              <div>
                <h4 class="font-medium text-gray-900 mb-3">报价情况 ({{ selectedProject.quotedCount || 0 }}/{{ selectedProject.supplierCount }})</h4>
                <div class="border border-gray-200 rounded-lg overflow-hidden">
                  <table class="w-full text-sm">
                    <thead class="bg-gray-50">
                      <tr>
                        <th class="text-left px-4 py-2 font-medium text-gray-700">供应商</th>
                        <th class="text-right px-4 py-2 font-medium text-gray-700">报价金额</th>
                        <th class="text-left px-4 py-2 font-medium text-gray-700">交期</th>
                        <th class="text-left px-4 py-2 font-medium text-gray-700">状态</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="quote in getProjectQuotations(selectedProject.id)" :key="quote.id" class="border-t border-gray-200">
                        <td class="px-4 py-2 text-gray-900">{{ quote.supplierName }}</td>
                        <td class="px-4 py-2 text-right text-gray-900">¥{{ quote.totalAmount.toLocaleString() }}</td>
                        <td class="px-4 py-2 text-gray-900">{{ quote.deliveryTime }}</td>
                        <td class="px-4 py-2">
                          <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="quote.status === 'accepted' ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-700'">
                            {{ quote.status === 'accepted' ? '已中标' : '待定标' }}
                          </span>
                        </td>
                      </tr>
                      <tr v-if="getProjectQuotations(selectedProject.id).length === 0">
                        <td colspan="4" class="px-4 py-4 text-center text-gray-500">暂无报价</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>

              <!-- 项目进度跟踪 -->
              <div>
                <h4 class="font-medium text-gray-900 mb-3">项目进度 ({{ getProjectProgressPercent(selectedProject) }}%)</h4>
                <div class="space-y-2">
                  <div v-for="stage in getProjectProgress(selectedProject)" :key="stage.key" class="flex items-center gap-3">
                    <div class="flex-shrink-0 w-8 h-8 rounded-full flex items-center justify-center" :class="stage.completed ? 'bg-green-100 text-green-600' : 'bg-gray-100 text-gray-400'">
                      <CheckCircle v-if="stage.completed" class="w-4 h-4" />
                      <Clock v-else class="w-4 h-4" />
                    </div>
                    <div class="flex-1">
                      <p class="text-sm font-medium text-gray-900">{{ stage.label }}</p>
                      <p class="text-xs text-gray-500">{{ stage.date }}</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="flex justify-end gap-3 p-4 border-t bg-gray-50">
              <button @click="isDetailDialogOpen = false" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">关闭</button>
              <button @click="isDetailDialogOpen = false; openEditDialog(selectedProject)" class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">编辑项目</button>
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
          <div class="relative bg-white rounded-xl shadow-xl w-full max-w-4xl max-h-[80vh] overflow-hidden">
            <div class="flex items-center justify-between p-4 border-b">
              <h3 class="text-lg font-semibold text-gray-900">报价详情 - {{ selectedProject.name }}</h3>
              <button @click="isQuoteDetailDialogOpen = false" class="p-2 hover:bg-gray-100 rounded-lg">✕</button>
            </div>
            <div class="p-6 overflow-y-auto max-h-[60vh]">
              <div v-if="getProjectQuotations(selectedProject.id).length === 0" class="text-center py-8 text-gray-500">
                <FileText class="w-12 h-12 mx-auto mb-3 text-gray-300" />
                <p>暂无供应商报价</p>
              </div>
              <div v-else class="border border-gray-200 rounded-lg overflow-hidden">
                <table class="w-full text-sm">
                  <thead class="bg-gray-50">
                    <tr>
                      <th class="text-left px-4 py-3 font-medium text-gray-700">供应商</th>
                      <th class="text-left px-4 py-3 font-medium text-gray-700">物料明细</th>
                      <th class="text-right px-4 py-3 font-medium text-gray-700">总金额</th>
                      <th class="text-left px-4 py-3 font-medium text-gray-700">交期</th>
                      <th class="text-left px-4 py-3 font-medium text-gray-700">付款条款</th>
                      <th class="text-left px-4 py-3 font-medium text-gray-700">有效期</th>
                      <th class="text-left px-4 py-3 font-medium text-gray-700">状态</th>
                    </tr>
                  </thead>
                  <tbody>
                    <template v-for="(quote, idx) in getProjectQuotations(selectedProject.id)" :key="quote.id">
                      <tr class="border-t border-gray-200" :class="idx === 0 ? 'bg-green-50' : ''">
                        <td class="px-4 py-3 text-sm text-gray-900" :rowspan="quote.items.length + 1">
                          {{ quote.supplierName }}
                          <span v-if="idx === 0" class="ml-2 px-2 py-0.5 bg-green-600 text-white rounded-full text-xs">最优</span>
                        </td>
                        <td class="px-4 py-2 text-sm text-gray-900">
                          <div v-for="(item, i) in quote.items" :key="i" class="mb-1 last:mb-0">
                            {{ item.materialName }} x {{ item.quantity }} @ ¥{{ item.unitPrice }}
                          </div>
                        </td>
                        <td class="px-4 py-3 text-sm text-right font-bold text-gray-900" :rowspan="quote.items.length + 1">¥{{ quote.totalAmount.toLocaleString() }}</td>
                        <td class="px-4 py-3 text-sm text-gray-900" :rowspan="quote.items.length + 1">{{ quote.deliveryTime }}</td>
                        <td class="px-4 py-3 text-sm text-gray-900" :rowspan="quote.items.length + 1">{{ quote.paymentTerms }}</td>
                        <td class="px-4 py-3 text-sm text-gray-900" :rowspan="quote.items.length + 1">{{ quote.validUntil }}</td>
                        <td class="px-4 py-3 text-sm" :rowspan="quote.items.length + 1">
                          <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="quote.status === 'accepted' ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-700'">
                            {{ quote.status === 'accepted' ? '已中标' : '待定标' }}
                          </span>
                        </td>
                      </tr>
                    </template>
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
              <button @click="isEvaluationDialogOpen = false" class="p-2 hover:bg-gray-100 rounded-lg"><X class="w-5 h-5" /></button>
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
                    <tr v-for="score in evaluationForm.scores" :key="score.supplierId" class="border-t border-gray-200">
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

    <!-- 定标对话框 -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="isFinalizeDialogOpen && selectedProject" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-black/50" @click="isFinalizeDialogOpen = false" />
          <div class="relative bg-white rounded-xl shadow-xl w-full max-w-lg">
            <div class="flex items-center justify-between p-4 border-b">
              <h3 class="text-lg font-semibold text-gray-900">定标确认</h3>
              <button @click="isFinalizeDialogOpen = false" class="p-2 hover:bg-gray-100 rounded-lg"><X class="w-5 h-5" /></button>
            </div>

            <div class="p-6 space-y-4">
              <div class="p-3 bg-blue-50 rounded-lg">
                <div class="flex items-start gap-2">
                  <AlertCircle class="w-5 h-5 text-blue-600 flex-shrink-0 mt-0.5" />
                  <div class="text-sm text-blue-800">
                    <p class="font-medium mb-1">定标说明</p>
                    <p>定标后将生成审批流程，需要经过审批后才能正式确定中标供应商。</p>
                  </div>
                </div>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">选择中标供应商 <span class="text-red-500">*</span></label>
                <div class="space-y-2">
                  <label v-for="supplier in suppliers.filter(s => (selectedProject.invitedSuppliers || []).includes(s.id))" :key="supplier.id" class="flex items-center gap-3 p-3 border rounded-lg cursor-pointer hover:bg-gray-50" :class="finalizeForm.selectedSupplierId === supplier.id ? 'border-blue-500 bg-blue-50' : 'border-gray-200'">
                    <input type="radio" :value="supplier.id" v-model="finalizeForm.selectedSupplierId" class="text-blue-600" />
                    <div class="flex-1">
                      <p class="font-medium text-gray-900">{{ supplier.name }}</p>
                      <p class="text-xs text-gray-500">{{ suppliers.find(s => s.id === supplier.id)?.category }}</p>
                    </div>
                  </label>
                </div>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">定标说明 <span class="text-red-500">*</span></label>
                <textarea v-model="finalizeForm.finalizeReason" rows="3" placeholder="请说明定标理由，如价格优势、质量保障、交期保障等" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>

              <div class="grid grid-cols-2 gap-4 text-sm">
                <div class="p-3 bg-gray-50 rounded-lg">
                  <p class="text-gray-600">项目编号</p>
                  <p class="font-medium text-gray-900">{{ selectedProject.id }}</p>
                </div>
                <div class="p-3 bg-gray-50 rounded-lg">
                  <p class="text-gray-600">截止日期</p>
                  <p class="font-medium text-gray-900">{{ selectedProject.deadline }}</p>
                </div>
              </div>
            </div>

            <div class="flex justify-end gap-3 p-4 border-t bg-gray-50">
              <button @click="isFinalizeDialogOpen = false" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">取消</button>
              <button @click="handleSubmitFinalize" class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">提交审批</button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- 审批管理对话框 -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="isApprovalDialogOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-black/50" @click="isApprovalDialogOpen = false" />
          <div class="relative bg-white rounded-xl shadow-xl w-full max-w-3xl max-h-[80vh] overflow-hidden">
            <div class="flex items-center justify-between p-4 border-b">
              <h3 class="text-lg font-semibold text-gray-900">审批管理</h3>
              <button @click="isApprovalDialogOpen = false" class="p-2 hover:bg-gray-100 rounded-lg"><X class="w-5 h-5" /></button>
            </div>

            <div class="p-6 overflow-y-auto max-h-[60vh] space-y-4">
              <div v-if="approvals.length === 0" class="text-center py-8 text-gray-500">
                <Check class="w-12 h-12 mx-auto mb-3 text-gray-300" />
                <p>暂无审批记录</p>
              </div>
              <div v-else class="space-y-4">
                <div v-for="approval in approvals" :key="approval.id" class="border border-gray-200 rounded-lg p-4">
                  <div class="flex items-start justify-between mb-3">
                    <div class="flex-1">
                      <div class="flex items-center gap-2 mb-1">
                        <h4 class="font-semibold text-gray-900">{{ approval.projectName }}</h4>
                        <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="approval.status === '待审批' ? 'bg-orange-100 text-orange-800' : approval.status === '已通过' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'">{{ approval.status }}</span>
                      </div>
                      <p class="text-sm text-gray-600">中标供应商: {{ approval.selectedSupplierName }}</p>
                    </div>
                  </div>
                  <div class="p-3 bg-gray-50 rounded-lg mb-3">
                    <p class="text-sm text-gray-700"><span class="font-medium">定标说明:</span> {{ approval.reason }}</p>
                  </div>

                  <div v-if="approval.status === '待审批'" class="space-y-3">
                    <div>
                      <label class="block text-sm font-medium text-gray-700 mb-1">审批意见</label>
                      <textarea v-model="approvalForm.comment" rows="2" placeholder="请输入审批意见（拒绝时必填）" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                    </div>
                    <div class="flex justify-end gap-2">
                      <button @click="handleApproval(approval, 'reject')" class="px-4 py-2 border border-red-300 rounded-lg text-sm text-red-600 hover:bg-red-50">拒绝</button>
                      <button @click="handleApproval(approval, 'approve')" class="px-4 py-2 bg-green-600 text-white rounded-lg text-sm hover:bg-green-700">通过</button>
                    </div>
                  </div>
                  <div v-else-if="approval.status === '已拒绝'" class="p-3 bg-red-50 rounded-lg">
                    <p class="text-sm text-red-700"><span class="font-medium">拒绝原因:</span> {{ approval.rejectReason }}</p>
                  </div>
                  <div v-else class="p-3 bg-green-50 rounded-lg">
                    <p class="text-sm text-green-700">审批人: {{ approval.approver }} | 审批时间: {{ new Date(approval.approvalDate).toLocaleString() }}</p>
                  </div>
                </div>
              </div>
            </div>

            <div class="flex justify-end p-4 border-t bg-gray-50">
              <button @click="isApprovalDialogOpen = false" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">关闭</button>
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
