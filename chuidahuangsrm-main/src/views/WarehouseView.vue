<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Warehouse, Package, AlertTriangle, TrendingUp, TrendingDown, QrCode, LayoutGrid, List, Plus, Eye, Printer, Scan, BarChart, Edit, Upload, Download, X } from 'lucide-vue-next'
import { mockVirtualWarehouses, mockASNs } from '@/data/mockData'
import { useAppStore } from '@/stores/useAppStore'

const appStore = useAppStore()
const activeTab = ref('virtual-warehouse')
const supplierFilter = ref('all')
const viewMode = ref<'grid' | 'list'>('list')
const asnViewMode = ref<'grid' | 'list'>('list')
const reconciliationViewMode = ref<'grid' | 'list'>('list')

// 查询框状态
const warehouseSearchQuery = ref('')
const asnSearchQuery = ref('')
const reconciliationSearchQuery = ref('')

// 详情对话框
const selectedWarehouse = ref<any>(null)
const selectedASN = ref<any>(null)
const isWarehouseDetailOpen = ref(false)
const isASNDetailOpen = ref(false)
const isInventoryAdjustDialogOpen = ref(false)

// 库存调整表单
const inventoryAdjustForm = ref({
  actualQty: 0,
  adjustReason: '',
  adjustDate: new Date().toISOString().split('T')[0],
  operator: '',
})

// 库存调整历史类型
interface InventoryAdjustment {
  id: string
  warehouseId: string
  beforeQty: number
  afterQty: number
  adjustReason: string
  adjustDate: string
  operator: string
  createdAt: string
}

// 核销单类型
interface ReconciliationStatement {
  id: string
  warehouseId: string
  materialName: string
  theoreticalQty: number
  actualQty: number
  varianceQty: number
  varianceRate: number
  statementDate: string
  status: 'pending' | 'confirmed'
  createdAt: string
}

// 核销单对话框状态
const isReconciliationDialogOpen = ref(false)
const selectedWarehouseForRecon = ref<any>(null)

// 新增虚拟库存对话框状态
const isCreateInventoryOpen = ref(false)
const newInventory = ref({
  supplierId: '',
  materialCode: '',
  materialName: '',
  theoreticalQty: 0,
  actualQty: 0,
  unit: '个',
  location: '',
  notes: '',
})

// 上传库存对话框状态
const isUploadInventoryOpen = ref(false)
const uploadedFile = ref<File | null>(null)
const uploadPreview = ref<any[]>([])
const isProcessingUpload = ref(false)

// 新增核销对话框状态
const isCreateReconciliationOpen = ref(false)
const newReconciliation = ref({
  supplierId: '',
  materialCode: '',
  materialName: '',
  theoreticalQty: 0,
  actualQty: 0,
  unit: '个',
  reconciliationDate: new Date().toISOString().split('T')[0],
  notes: '',
})

// 供应商列表
const suppliers = [
  { id: 'S001', name: '深圳市精诚模具制造有限公司' },
  { id: 'S002', name: '东莞市华泰五金制品厂' },
  { id: 'S003', name: '佛山市永盛铝制品有限公司' },
]

// 获取库存调整历史
function getInventoryAdjustHistory(warehouseId: string): InventoryAdjustment[] {
  const saved = localStorage.getItem('inventory_adjustments')
  if (saved) {
    try {
      const allAdjustments = JSON.parse(saved) as InventoryAdjustment[]
      return allAdjustments.filter(a => a.warehouseId === warehouseId)
    } catch {
      return []
    }
  }
  return []
}

// 打开核销单对话框
function openReconciliationDialog(warehouse: any) {
  selectedWarehouseForRecon.value = warehouse
  isReconciliationDialogOpen.value = true
}

// 生成核销单
function handleGenerateReconciliationStatement() {
  if (!selectedWarehouseForRecon.value) return

  const statement: ReconciliationStatement = {
    id: `RS${Date.now()}`,
    warehouseId: selectedWarehouseForRecon.value.id,
    materialName: selectedWarehouseForRecon.value.materialName,
    theoreticalQty: selectedWarehouseForRecon.value.theoreticalQty,
    actualQty: selectedWarehouseForRecon.value.actualQty,
    varianceQty: selectedWarehouseForRecon.value.variance,
    varianceRate: selectedWarehouseForRecon.value.varianceRate,
    statementDate: new Date().toISOString().split('T')[0],
    status: 'pending',
    createdAt: new Date().toISOString(),
  }

  // 保存到 localStorage
  const saved = localStorage.getItem('reconciliation_statements')
  let statements = saved ? JSON.parse(saved) : []
  statements.push(statement)
  localStorage.setItem('reconciliation_statements', JSON.stringify(statements))

  appStore.showToast('核销单已生成', 'success')
  isReconciliationDialogOpen.value = false
}

// 创建ASN对话框
const isCreateASNOpen = ref(false)
const newASN = ref({
  supplierId: '',
  orderNo: '',
  shipDate: new Date().toISOString().split('T')[0],
  estimatedArrival: '',
  items: [{ materialCode: '', materialName: '', quantity: 1, unit: '个' }],
})

const filteredWarehouses = computed(() => {
  let result = supplierFilter.value === 'all'
    ? mockVirtualWarehouses
    : mockVirtualWarehouses.filter((w) => w.supplierId === supplierFilter.value)

  if (warehouseSearchQuery.value.trim()) {
    const query = warehouseSearchQuery.value.toLowerCase().trim()
    result = result.filter((w) =>
      w.materialName.toLowerCase().includes(query) ||
      w.materialCode.toLowerCase().includes(query) ||
      w.supplierName.toLowerCase().includes(query)
    )
  }
  return result
})

// 过滤后的ASN列表
const filteredASNs = computed(() => {
  let result = mockASNs

  if (asnSearchQuery.value.trim()) {
    const query = asnSearchQuery.value.toLowerCase().trim()
    result = result.filter((a) =>
      a.asnNo.toLowerCase().includes(query) ||
      a.supplierName.toLowerCase().includes(query) ||
      a.orderNo.toLowerCase().includes(query)
    )
  }
  return result
})

// 过滤后的核销列表
const filteredReconciliations = computed(() => {
  let result = mockVirtualWarehouses

  if (reconciliationSearchQuery.value.trim()) {
    const query = reconciliationSearchQuery.value.toLowerCase().trim()
    result = result.filter((w) =>
      w.materialName.toLowerCase().includes(query) ||
      w.materialCode.toLowerCase().includes(query) ||
      w.supplierName.toLowerCase().includes(query)
    )
  }
  return result
})

// 查看库存明细
function handleViewWarehouseDetail(warehouse: any) {
  selectedWarehouse.value = warehouse
  isWarehouseDetailOpen.value = true
}

// 打印标签
function handlePrintLabel(asn: any) {
  appStore.showToast(`正在打印ASN ${asn.asnNo} 的标签...`, 'info')
  setTimeout(() => {
    appStore.showToast('标签打印成功', 'success')
  }, 1500)
}

// 扫码收货
function handleScanReceipt(asn: any) {
  appStore.showToast(`请扫描ASN ${asn.asnNo} 的二维码进行收货...`, 'info')
  setTimeout(() => {
    appStore.showToast('收货确认成功', 'success')
  }, 2000)
}

// 创建ASN
function handleCreateASN() {
  isCreateASNOpen.value = true
}

// 保存ASN
function handleSaveASN() {
  if (!newASN.value.supplierId || !newASN.value.orderNo) {
    appStore.showToast('请填写完整信息', 'warning')
    return
  }
  appStore.showToast('ASN创建成功', 'success')
  isCreateASNOpen.value = false
}

// 生成核销单
function handleGenerateReconciliation(warehouse: any) {
  openReconciliationDialog(warehouse)
}

// 查看ASN详情
function handleViewASNDetail(asn: any) {
  selectedASN.value = asn
  isASNDetailOpen.value = true
}

// 添加ASN物料
function addASNItem() {
  newASN.value.items.push({ materialCode: '', materialName: '', quantity: 1, unit: '个' })
}

// 删除ASN物料
function removeASNItem(index: number) {
  if (newASN.value.items.length > 1) {
    newASN.value.items.splice(index, 1)
  }
}

// 打开库存调整对话框
function openInventoryAdjustDialog(warehouse: any) {
  selectedWarehouse.value = warehouse
  inventoryAdjustForm.value = {
    actualQty: warehouse.actualQty,
    adjustReason: '',
    adjustDate: new Date().toISOString().split('T')[0],
    operator: '',
  }
  isInventoryAdjustDialogOpen.value = true
}

// 保存库存调整
function handleSaveInventoryAdjust() {
  if (!selectedWarehouse.value) return

  if (!inventoryAdjustForm.value.adjustReason.trim()) {
    appStore.showToast('请输入调整原因', 'warning')
    return
  }

  const beforeQty = selectedWarehouse.value.actualQty
  const afterQty = inventoryAdjustForm.value.actualQty

  // 创建调整记录
  const adjustment: InventoryAdjustment = {
    id: `IA${Date.now()}`,
    warehouseId: selectedWarehouse.value.id,
    beforeQty,
    afterQty,
    adjustReason: inventoryAdjustForm.value.adjustReason,
    adjustDate: inventoryAdjustForm.value.adjustDate,
    operator: inventoryAdjustForm.value.operator || '系统管理员',
    createdAt: new Date().toISOString(),
  }

  // 保存到 localStorage
  const saved = localStorage.getItem('inventory_adjustments')
  let allAdjustments = saved ? JSON.parse(saved) : []
  allAdjustments.push(adjustment)
  localStorage.setItem('inventory_adjustments', JSON.stringify(allAdjustments))

  // 更新虚拟仓库的实际库存
  const idx = mockVirtualWarehouses.findIndex(w => w.id === selectedWarehouse.value!.id)
  if (idx > -1) {
    mockVirtualWarehouses[idx].actualQty = afterQty
    mockVirtualWarehouses[idx].variance = mockVirtualWarehouses[idx].theoreticalQty - afterQty
    mockVirtualWarehouses[idx].varianceRate = parseFloat(((mockVirtualWarehouses[idx].variance / mockVirtualWarehouses[idx].theoreticalQty) * 100).toFixed(2))
  }

  appStore.showToast('库存调整成功', 'success')
  isInventoryAdjustDialogOpen.value = false
}

// 打开新增库存对话框
function openCreateInventoryDialog() {
  newInventory.value = {
    supplierId: '',
    materialCode: '',
    materialName: '',
    theoreticalQty: 0,
    actualQty: 0,
    unit: '个',
    location: '',
    notes: '',
  }
  isCreateInventoryOpen.value = true
}

// 保存新增库存
function handleSaveInventory() {
  if (!newInventory.value.supplierId) {
    appStore.showToast('请选择供应商', 'warning')
    return
  }
  if (!newInventory.value.materialCode.trim()) {
    appStore.showToast('请输入物料编码', 'warning')
    return
  }
  if (!newInventory.value.materialName.trim()) {
    appStore.showToast('请输入物料名称', 'warning')
    return
  }
  if (newInventory.value.theoreticalQty <= 0) {
    appStore.showToast('请输入有效的理论数量', 'warning')
    return
  }

  const supplier = suppliers.find(s => s.id === newInventory.value.supplierId)
  const variance = newInventory.value.theoreticalQty - newInventory.value.actualQty
  const varianceRate = parseFloat(((variance / newInventory.value.theoreticalQty) * 100).toFixed(2))

  // 创建新的虚拟仓库记录
  const warehouse = {
    id: `VW${Date.now()}`,
    supplierId: newInventory.value.supplierId,
    supplierName: supplier?.name || '',
    materialCode: newInventory.value.materialCode,
    materialName: newInventory.value.materialName,
    theoreticalQty: newInventory.value.theoreticalQty,
    actualQty: newInventory.value.actualQty,
    variance,
    varianceRate,
    unit: newInventory.value.unit,
    location: newInventory.value.location,
    lastUpdateDate: new Date().toISOString().split('T')[0],
    status: 'normal',
  }

  // 保存到 localStorage
  const saved = localStorage.getItem('virtual_warehouses')
  let allWarehouses = saved ? JSON.parse(saved) : []
  allWarehouses.push(warehouse)
  localStorage.setItem('virtual_warehouses', JSON.stringify(allWarehouses))

  // 更新 mockVirtualWarehouses
  mockVirtualWarehouses.push(warehouse)

  appStore.showToast('虚拟库存创建成功', 'success')
  isCreateInventoryOpen.value = false
}

// 打开上传库存对话框
function openUploadInventoryDialog() {
  uploadedFile.value = null
  uploadPreview.value = []
  isUploadInventoryOpen.value = true
}

// 处理文件选择
function handleFileSelect(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  // 验证文件类型
  const validTypes = [
    'application/vnd.ms-excel',
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    'text/csv',
  ]
  if (!validTypes.includes(file.type) && !file.name.endsWith('.csv') && !file.name.endsWith('.xlsx') && !file.name.endsWith('.xls')) {
    appStore.showToast('请上传 Excel 或 CSV 文件', 'warning')
    return
  }

  uploadedFile.value = file
  parseUploadedFile(file)
}

// 解析上传的文件
function parseUploadedFile(file: File) {
  isProcessingUpload.value = true
  const reader = new FileReader()

  reader.onload = (e) => {
    const content = e.target?.result as string
    const parsed = parseCSV(content)
    uploadPreview.value = parsed.slice(0, 10) // 只显示前10条预览
    isProcessingUpload.value = false
    appStore.showToast(`成功解析 ${parsed.length} 条库存数据`, 'success')
  }

  reader.onerror = () => {
    appStore.showToast('文件解析失败', 'error')
    isProcessingUpload.value = false
  }

  reader.readAsText(file, 'GBK') // 尝试 GBK 编码，如果失败会自动回退
}

// 解析 CSV 内容
function parseCSV(content: string): any[] {
  const lines = content.split(/\r?\n/).filter(line => line.trim())
  if (lines.length < 2) return []

  // 假设第一行是标题，从第二行开始是数据
  // 支持格式：供应商ID,物料编码,物料名称,理论数量,实际数量,单位,存放位置,备注
  const result: any[] = []

  for (let i = 1; i < lines.length; i++) {
    const fields = parseCSVLine(lines[i])
    if (fields.length >= 4) {
      const theoreticalQty = parseFloat(fields[3]) || 0
      const actualQty = parseFloat(fields[4]) || 0
      const variance = theoreticalQty - actualQty
      const varianceRate = theoreticalQty > 0 ? parseFloat(((variance / theoreticalQty) * 100).toFixed(2)) : 0

      result.push({
        supplierId: fields[0] || '',
        materialCode: fields[1] || '',
        materialName: fields[2] || '',
        theoreticalQty,
        actualQty,
        variance,
        varianceRate,
        unit: fields[5] || '个',
        location: fields[6] || '',
        notes: fields[7] || '',
        valid: !!(fields[0] && fields[1] && fields[2] && theoreticalQty > 0),
      })
    }
  }

  return result
}

// 解析 CSV 单行（处理引号）
function parseCSVLine(line: string): string[] {
  const result: string[] = []
  let current = ''
  let inQuotes = false

  for (let i = 0; i < line.length; i++) {
    const char = line[i]
    if (char === '"') {
      inQuotes = !inQuotes
    } else if (char === ',' && !inQuotes) {
      result.push(current.trim())
      current = ''
    } else {
      current += char
    }
  }
  result.push(current.trim())

  return result
}

// 下载模板
function downloadTemplate() {
  const template = `供应商ID,物料编码,物料名称,理论数量,实际数量,单位,存放位置,备注
S001,M001,模具钢A,100,98,个,A区01号,库存正常
S002,M002,螺丝M6,500,500,个,B区02号,
S003,M003,铝板,50,48,张,C区03号,部分已使用`

  const blob = new Blob([template], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = '库存导入模板.csv'
  link.click()
  URL.revokeObjectURL(link.href)
}

// 确认上传
function handleConfirmUpload() {
  if (uploadPreview.value.length === 0) {
    appStore.showToast('没有可上传的数据', 'warning')
    return
  }

  const validData = uploadPreview.value.filter(item => item.valid)
  if (validData.length === 0) {
    appStore.showToast('没有有效数据可上传', 'warning')
    return
  }

  // 保存有效数据
  const saved = localStorage.getItem('virtual_warehouses')
  let allWarehouses = saved ? JSON.parse(saved) : []

  validData.forEach((item: any) => {
    const supplier = suppliers.find(s => s.id === item.supplierId)
    const warehouse = {
      id: `VW${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
      supplierId: item.supplierId,
      supplierName: supplier?.name || '',
      materialCode: item.materialCode,
      materialName: item.materialName,
      theoreticalQty: item.theoreticalQty,
      actualQty: item.actualQty,
      variance: item.variance,
      varianceRate: item.varianceRate,
      unit: item.unit,
      location: item.location,
      lastUpdateDate: new Date().toISOString().split('T')[0],
      status: 'normal',
    }
    allWarehouses.push(warehouse)
    mockVirtualWarehouses.push(warehouse)
  })

  localStorage.setItem('virtual_warehouses', JSON.stringify(allWarehouses))
  appStore.showToast(`成功导入 ${validData.length} 条库存数据`, 'success')
  isUploadInventoryOpen.value = false
}

// 确认供应商上传的库存
function handleConfirmSupplierInventory(inventoryId: string) {
  const saved = localStorage.getItem('supplier_inventories')
  if (saved) {
    let allInventories = JSON.parse(saved)
    const idx = allInventories.findIndex((i: any) => i.id === inventoryId)
    if (idx > -1) {
      allInventories[idx].status = '已确认'
      localStorage.setItem('supplier_inventories', JSON.stringify(allInventories))

      // 创建对应的虚拟仓库记录
      const inventory = allInventories[idx]
      const supplier = suppliers.find(s => s.id === inventory.supplierId)
      const warehouse = {
        id: `VW${Date.now()}`,
        supplierId: inventory.supplierId,
        supplierName: supplier?.name || inventory.supplierName,
        materialCode: inventory.materialCode,
        materialName: inventory.materialName,
        theoreticalQty: inventory.quantity,
        actualQty: inventory.quantity,
        variance: 0,
        varianceRate: 0,
        unit: inventory.unit,
        location: '',
        lastUpdateDate: inventory.uploadDate,
        status: 'normal',
      }

      mockVirtualWarehouses.push(warehouse)
      localStorage.setItem('virtual_warehouses', JSON.stringify([...mockVirtualWarehouses]))
    }
  }
  appStore.showToast('库存已确认并同步到虚拟仓库', 'success')
}

// 打开新增核销对话框
function openCreateReconciliationDialog() {
  newReconciliation.value = {
    supplierId: '',
    materialCode: '',
    materialName: '',
    theoreticalQty: 0,
    actualQty: 0,
    unit: '个',
    reconciliationDate: new Date().toISOString().split('T')[0],
    notes: '',
  }
  isCreateReconciliationOpen.value = true
}

// 保存新增核销
function handleSaveReconciliation() {
  if (!newReconciliation.value.supplierId) {
    appStore.showToast('请选择供应商', 'warning')
    return
  }
  if (!newReconciliation.value.materialCode.trim()) {
    appStore.showToast('请输入物料编码', 'warning')
    return
  }
  if (!newReconciliation.value.materialName.trim()) {
    appStore.showToast('请输入物料名称', 'warning')
    return
  }
  if (newReconciliation.value.theoreticalQty <= 0) {
    appStore.showToast('请输入有效的应耗数量', 'warning')
    return
  }
  if (newReconciliation.value.actualQty < 0) {
    appStore.showToast('请输入有效的实耗数量', 'warning')
    return
  }

  const supplier = suppliers.find(s => s.id === newReconciliation.value.supplierId)
  const variance = newReconciliation.value.theoreticalQty - newReconciliation.value.actualQty
  const varianceRate = parseFloat(((variance / newReconciliation.value.theoreticalQty) * 100).toFixed(2))

  // 创建核销单记录
  const statement: ReconciliationStatement = {
    id: `RS${Date.now()}`,
    warehouseId: '',
    materialName: newReconciliation.value.materialName,
    theoreticalQty: newReconciliation.value.theoreticalQty,
    actualQty: newReconciliation.value.actualQty,
    varianceQty: variance,
    varianceRate: varianceRate,
    statementDate: newReconciliation.value.reconciliationDate,
    status: 'pending',
    createdAt: new Date().toISOString(),
  }

  // 保存到 localStorage
  const saved = localStorage.getItem('reconciliation_statements')
  let statements = saved ? JSON.parse(saved) : []
  statements.push(statement)
  localStorage.setItem('reconciliation_statements', JSON.stringify(statements))

  appStore.showToast('核销单已创建成功', 'success')
  isCreateReconciliationOpen.value = false
}
</script>

<template>
  <div class="space-y-6">
    <!-- 标题 -->
    <div>
      <h1 class="text-2xl font-semibold text-gray-900">{{ appStore.t('page.warehouse.title') }}</h1>
      <p class="text-sm text-gray-500 mt-1">虚拟仓 · ASN预发货通知 · 自动核销</p>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex items-center justify-between"><div><p class="text-sm text-gray-500">{{ appStore.t('warehouse.kpi.virtualCount') }}</p><p class="text-2xl font-semibold text-gray-900 mt-1">{{ mockVirtualWarehouses.length }}</p></div><div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center"><Warehouse class="w-6 h-6 text-blue-600" /></div></div>
      </div>
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex items-center justify-between"><div><p class="text-sm text-gray-500">{{ appStore.t('warehouse.kpi.inTransitAsn') }}</p><p class="text-2xl font-semibold text-orange-600 mt-1">{{ mockASNs.filter((a) => a.status === '在途').length }}</p></div><div class="w-12 h-12 bg-orange-100 rounded-lg flex items-center justify-center"><Package class="w-6 h-6 text-orange-600" /></div></div>
      </div>
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex items-center justify-between"><div><p class="text-sm text-gray-500">{{ appStore.t('warehouse.kpi.pendingReceipt') }}</p><p class="text-2xl font-semibold text-blue-600 mt-1">{{ mockASNs.filter((a) => a.status === '已到货').length }}</p></div><div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center"><QrCode class="w-6 h-6 text-blue-600" /></div></div>
      </div>
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex items-center justify-between"><div><p class="text-sm text-gray-500">{{ appStore.t('warehouse.kpi.varianceAlert') }}</p><p class="text-2xl font-semibold text-red-600 mt-1">{{ mockVirtualWarehouses.filter((w) => Math.abs(w.varianceRate) > 2).length }}</p></div><div class="w-12 h-12 bg-red-100 rounded-lg flex items-center justify-center"><AlertTriangle class="w-6 h-6 text-red-600" /></div></div>
      </div>
    </div>

    <!-- 选项卡 -->
    <div class="bg-white rounded-lg border border-gray-200">
      <div class="flex border-b border-gray-200">
        <button v-for="tab in [{ id: 'virtual-warehouse', label: '库存管理' }, { id: 'asn', label: 'ASN管理' }, { id: 'reconciliation', label: '物料核销' }]" :key="tab.id" class="px-6 py-3 text-sm font-medium transition-colors" :class="activeTab === tab.id ? 'border-b-2 border-blue-600 text-blue-600' : 'text-gray-600 hover:text-gray-900'" @click="activeTab = tab.id">{{ tab.label }}</button>
      </div>

      <div class="p-6">
        <!-- 虚拟仓库 -->
        <div v-if="activeTab === 'virtual-warehouse'" class="space-y-4">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-base font-semibold text-gray-900">库存管理</h3>
            <div class="flex items-center gap-3">
              <!-- 查询框 -->
              <div class="relative">
                <input
                  v-model="warehouseSearchQuery"
                  type="text"
                  placeholder="搜索物料名称/编码/供应商..."
                  class="w-64 pl-9 pr-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
                <svg class="w-4 h-4 text-gray-400 absolute left-3 top-1/2 -translate-y-1/2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                </svg>
              </div>
              <button @click="openCreateInventoryDialog" class="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700">
                <Plus class="w-4 h-4" />新增库存
              </button>
              <button @click="openUploadInventoryDialog" class="flex items-center gap-2 px-4 py-2 bg-green-600 text-white rounded-lg text-sm font-medium hover:bg-green-700">
                <Upload class="w-4 h-4" />批量上传
              </button>
              <select v-model="supplierFilter" class="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 w-64">
                <option value="all">{{ appStore.t('warehouse.allSupplier') }}</option>
                <option value="S001">深圳市精诚模具制造有限公司</option>
                <option value="S002">东莞市华泰五金制品厂</option>
                <option value="S003">佛山市永盛铝制品有限公司</option>
              </select>
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
          </div>
          <div v-if="viewMode === 'grid'" v-for="warehouse in filteredWarehouses" :key="warehouse.id" class="border border-gray-200 rounded-lg p-4">
            <div class="flex items-start justify-between mb-4">
              <div>
                <div class="flex items-center gap-2 mb-2">
                  <h4 class="font-semibold text-gray-900">{{ warehouse.materialName }}</h4>
                  <span v-if="Math.abs(warehouse.varianceRate) > 2" class="px-2 py-0.5 bg-red-100 text-red-800 rounded-full text-xs font-medium">差异预警</span>
                </div>
                <p class="text-sm text-gray-600">物料编码: {{ warehouse.materialCode }}</p>
                <p class="text-xs text-gray-500 mt-1">{{ warehouse.supplierName }}</p>
              </div>
              <div class="flex gap-2">
                <button @click="handleViewWarehouseDetail(warehouse)" class="px-3 py-1.5 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">查看明细</button>
                <button @click="openInventoryAdjustDialog(warehouse)" class="px-3 py-1.5 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50 flex items-center gap-1">
                  <Edit class="w-3 h-3" />调整库存
                </button>
              </div>
            </div>
            <div class="grid grid-cols-2 md:grid-cols-5 gap-4">
              <div class="p-3 bg-blue-50 rounded-lg">
                <p class="text-xs text-gray-600">理论库存</p>
                <p class="text-lg font-semibold text-gray-900 mt-1">{{ warehouse.theoreticalQty.toLocaleString() }}</p>
                <p class="text-xs text-gray-500">{{ warehouse.unit }}</p>
              </div>
              <div class="p-3 bg-green-50 rounded-lg">
                <p class="text-xs text-gray-600">实际库存</p>
                <p class="text-lg font-semibold text-gray-900 mt-1">{{ warehouse.actualQty.toLocaleString() }}</p>
                <p class="text-xs text-gray-500">{{ warehouse.unit }}</p>
              </div>
              <div class="p-3 rounded-lg" :class="warehouse.variance < 0 ? 'bg-red-50' : 'bg-yellow-50'">
                <p class="text-xs text-gray-600">差异数量</p>
                <div class="flex items-center gap-1 mt-1">
                  <TrendingDown v-if="warehouse.variance < 0" class="w-4 h-4 text-red-600" />
                  <TrendingUp v-else class="w-4 h-4 text-yellow-600" />
                  <p class="text-lg font-semibold" :class="warehouse.variance < 0 ? 'text-red-600' : 'text-yellow-600'">{{ Math.abs(warehouse.variance) }}</p>
                </div>
                <p class="text-xs text-gray-500">{{ warehouse.unit }}</p>
              </div>
              <div class="p-3 rounded-lg" :class="Math.abs(warehouse.varianceRate) > 2 ? 'bg-red-50' : 'bg-gray-50'">
                <p class="text-xs text-gray-600">差异率</p>
                <p class="text-lg font-semibold mt-1" :class="Math.abs(warehouse.varianceRate) > 2 ? 'text-red-600' : 'text-gray-900'">{{ warehouse.varianceRate > 0 ? '+' : '' }}{{ warehouse.varianceRate }}%</p>
              </div>
              <div class="p-3 bg-gray-50 rounded-lg">
                <p class="text-xs text-gray-600">更新时间</p>
                <p class="text-sm font-medium text-gray-900 mt-1">{{ warehouse.lastUpdateDate }}</p>
              </div>
            </div>
          </div>
          <div v-else class="border border-gray-200 rounded-lg overflow-x-auto">
            <table class="w-full min-w-max text-sm">
              <thead class="bg-gray-50">
                <tr>
                  <th class="text-left px-4 py-3 font-medium text-gray-700">{{ appStore.t('warehouse.table.materialCode') }}</th>
                  <th class="text-left px-4 py-3 font-medium text-gray-700">{{ appStore.t('warehouse.table.materialName') }}</th>
                  <th class="text-left px-4 py-3 font-medium text-gray-700">{{ appStore.t('warehouse.table.supplier') }}</th>
                  <th class="text-right px-4 py-3 font-medium text-gray-700">{{ appStore.t('warehouse.table.theoreticalQty') }}</th>
                  <th class="text-right px-4 py-3 font-medium text-gray-700">{{ appStore.t('warehouse.table.actualQty') }}</th>
                  <th class="text-right px-4 py-3 font-medium text-gray-700">{{ appStore.t('warehouse.table.varianceRate') }}</th>
                  <th class="text-left px-4 py-3 font-medium text-gray-700">{{ appStore.t('warehouse.table.updateTime') }}</th>
                  <th class="text-left px-4 py-3 font-medium text-gray-700">{{ appStore.t('common.action') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="warehouse in filteredWarehouses" :key="warehouse.id" class="border-t border-gray-200">
                  <td class="px-4 py-3 text-gray-900">{{ warehouse.materialCode }}</td>
                  <td class="px-4 py-3 text-gray-900 font-medium">{{ warehouse.materialName }}</td>
                  <td class="px-4 py-3 text-gray-900">{{ warehouse.supplierName }}</td>
                  <td class="px-4 py-3 text-right text-gray-900">{{ warehouse.theoreticalQty.toLocaleString() }}</td>
                  <td class="px-4 py-3 text-right text-gray-900">{{ warehouse.actualQty.toLocaleString() }}</td>
                  <td class="px-4 py-3 text-right" :class="Math.abs(warehouse.varianceRate) > 2 ? 'text-red-600 font-medium' : 'text-gray-900'">
                    {{ warehouse.varianceRate > 0 ? '+' : '' }}{{ warehouse.varianceRate }}%
                  </td>
                  <td class="px-4 py-3 text-gray-900">{{ warehouse.lastUpdateDate }}</td>
                  <td class="px-4 py-3">
                    <div class="flex gap-2">
                      <button @click="handleViewWarehouseDetail(warehouse)" class="px-3 py-1.5 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">{{ appStore.t('btn.viewStatementDetail') }}</button>
                      <button @click="openInventoryAdjustDialog(warehouse)" class="px-3 py-1.5 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50 flex items-center gap-1">
                        <Edit class="w-3 h-3" />调整
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- ASN管理 -->
        <div v-if="activeTab === 'asn'" class="space-y-4">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-base font-semibold text-gray-900">{{ appStore.t('warehouse.asn.title') }}</h3>
            <div class="flex items-center gap-3">
              <!-- 查询框 -->
              <div class="relative">
                <input
                  v-model="asnSearchQuery"
                  type="text"
                  placeholder="搜索ASN号/供应商/订单号..."
                  class="w-64 pl-9 pr-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
                <svg class="w-4 h-4 text-gray-400 absolute left-3 top-1/2 -translate-y-1/2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                </svg>
              </div>
              <div class="flex items-center border border-gray-300 rounded-lg p-1">
                <button
                  class="px-3 py-1.5 rounded-md text-sm flex items-center gap-1"
                  :class="asnViewMode === 'grid' ? 'bg-blue-600 text-white' : 'text-gray-700 hover:bg-gray-100'"
                  @click="asnViewMode = 'grid'"
                >
                  <LayoutGrid class="w-4 h-4" />{{ appStore.t('btn.grid') }}
                </button>
                <button
                  class="px-3 py-1.5 rounded-md text-sm flex items-center gap-1"
                  :class="asnViewMode === 'list' ? 'bg-blue-600 text-white' : 'text-gray-700 hover:bg-gray-100'"
                  @click="asnViewMode = 'list'"
                >
                  <List class="w-4 h-4" />{{ appStore.t('btn.list') }}
                </button>
              </div>
              <button @click="handleCreateASN" class="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">
                <Plus class="w-4 h-4" />{{ appStore.t('btn.createAsn') }}
              </button>
            </div>
          </div>
          <div v-if="asnViewMode === 'grid'" v-for="asn in filteredASNs" :key="asn.id" class="border border-gray-200 rounded-lg p-4">
            <div class="flex items-start justify-between mb-4">
              <div>
                <div class="flex items-center gap-2 mb-2">
                  <h4 class="font-semibold text-gray-900">{{ asn.asnNo }}</h4>
                  <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="asn.status === '在途' ? 'bg-blue-100 text-blue-800' : 'bg-gray-100 text-gray-800'">{{ asn.status }}</span>
                </div>
                <p class="text-sm text-gray-600">{{ asn.supplierName }}</p>
                <p class="text-xs text-gray-500 mt-1">关联订单: {{ asn.orderNo }}</p>
              </div>
              <div class="text-right">
                <p class="text-sm text-gray-500">发货日期</p>
                <p class="font-medium text-gray-900">{{ asn.shipDate }}</p>
                <p class="text-xs text-gray-500 mt-1">预计到达: {{ asn.estimatedArrival }}</p>
              </div>
            </div>
            <div class="border border-gray-200 rounded-lg overflow-hidden mb-3">
              <table class="w-full text-sm">
                <thead class="bg-gray-50">
                  <tr>
                    <th class="text-left px-4 py-2 font-medium text-gray-700">物料编码</th>
                    <th class="text-left px-4 py-2 font-medium text-gray-700">物料名称</th>
                    <th class="text-right px-4 py-2 font-medium text-gray-700">数量</th>
                    <th class="text-left px-4 py-2 font-medium text-gray-700">条码</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(item, idx) in asn.items" :key="idx" class="border-t border-gray-200">
                    <td class="px-4 py-3 text-gray-900">{{ item.materialCode }}</td>
                    <td class="px-4 py-3 text-gray-900">{{ item.materialName }}</td>
                    <td class="px-4 py-3 text-right text-gray-900">{{ item.quantity }} {{ item.unit }}</td>
                    <td class="px-4 py-3 text-gray-900 font-mono text-xs">{{ item.barcode }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
            <div class="flex gap-2">
              <button @click="handlePrintLabel(asn)" class="flex-1 flex items-center justify-center gap-2 px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">
                <Printer class="w-4 h-4" />{{ appStore.t('btn.printLabel') }}
              </button>
              <button @click="handleViewASNDetail(asn)" class="flex-1 flex items-center justify-center gap-2 px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">
                <Eye class="w-4 h-4" />查看详情
              </button>
              <button v-if="asn.status === '已到货'" @click="handleScanReceipt(asn)" class="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700 flex items-center justify-center gap-2">
                <Scan class="w-4 h-4" />{{ appStore.t('btn.scanReceipt') }}
              </button>
            </div>
          </div>
          <div v-else class="border border-gray-200 rounded-lg overflow-x-auto">
            <table class="w-full min-w-max text-sm">
              <thead class="bg-gray-50">
                <tr>
                  <th class="text-left px-4 py-3 font-medium text-gray-700">{{ appStore.t('warehouse.asn.table.asnNo') }}</th>
                  <th class="text-left px-4 py-3 font-medium text-gray-700">{{ appStore.t('warehouse.table.supplier') }}</th>
                  <th class="text-left px-4 py-3 font-medium text-gray-700">{{ appStore.t('warehouse.asn.table.orderNo') }}</th>
                  <th class="text-left px-4 py-3 font-medium text-gray-700">{{ appStore.t('common.status') }}</th>
                  <th class="text-left px-4 py-3 font-medium text-gray-700">{{ appStore.t('warehouse.asn.table.shipDate') }}</th>
                  <th class="text-left px-4 py-3 font-medium text-gray-700">{{ appStore.t('warehouse.asn.table.eta') }}</th>
                  <th class="text-right px-4 py-3 font-medium text-gray-700">{{ appStore.t('warehouse.asn.table.itemRows') }}</th>
                  <th class="text-left px-4 py-3 font-medium text-gray-700">{{ appStore.t('common.action') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="asn in filteredASNs" :key="asn.id" class="border-t border-gray-200">
                  <td class="px-4 py-3 text-gray-900 font-medium">{{ asn.asnNo }}</td>
                  <td class="px-4 py-3 text-gray-900">{{ asn.supplierName }}</td>
                  <td class="px-4 py-3 text-gray-900">{{ asn.orderNo }}</td>
                  <td class="px-4 py-3">
                    <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="asn.status === '在途' ? 'bg-blue-100 text-blue-800' : 'bg-gray-100 text-gray-800'">{{ asn.status }}</span>
                  </td>
                  <td class="px-4 py-3 text-gray-900">{{ asn.shipDate }}</td>
                  <td class="px-4 py-3 text-gray-900">{{ asn.estimatedArrival }}</td>
                  <td class="px-4 py-3 text-right text-gray-900">{{ asn.items.length }}</td>
                  <td class="px-4 py-3">
                    <div class="flex gap-2">
                      <button @click="handlePrintLabel(asn)" class="px-3 py-1.5 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">{{ appStore.t('btn.printLabel') }}</button>
                      <button @click="handleViewASNDetail(asn)" class="px-3 py-1.5 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">查看</button>
                      <button v-if="asn.status === '已到货'" @click="handleScanReceipt(asn)" class="px-3 py-1.5 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">{{ appStore.t('btn.scanReceipt') }}</button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- 物料核销 -->
        <div v-if="activeTab === 'reconciliation'" class="space-y-4">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-base font-semibold text-gray-900">{{ appStore.t('warehouse.recon.title') }}</h3>
            <div class="flex items-center gap-3">
              <!-- 查询框 -->
              <div class="relative">
                <input
                  v-model="reconciliationSearchQuery"
                  type="text"
                  placeholder="搜索物料名称/编码/供应商..."
                  class="w-64 pl-9 pr-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
                <svg class="w-4 h-4 text-gray-400 absolute left-3 top-1/2 -translate-y-1/2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                </svg>
              </div>
              <button @click="openCreateReconciliationDialog" class="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700">
                <Plus class="w-4 h-4" />新增核销
              </button>
              <button
                class="px-3 py-1.5 rounded-md text-sm flex items-center gap-1"
                :class="reconciliationViewMode === 'grid' ? 'bg-blue-600 text-white' : 'text-gray-700 hover:bg-gray-100'"
                @click="reconciliationViewMode = 'grid'"
              >
                <LayoutGrid class="w-4 h-4" />{{ appStore.t('btn.grid') }}
              </button>
              <button
                class="px-3 py-1.5 rounded-md text-sm flex items-center gap-1"
                :class="reconciliationViewMode === 'list' ? 'bg-blue-600 text-white' : 'text-gray-700 hover:bg-gray-100'"
                @click="reconciliationViewMode = 'list'"
              >
                <List class="w-4 h-4" />{{ appStore.t('btn.list') }}
              </button>
            </div>
          </div>
          <div class="p-4 bg-blue-50 rounded-lg">
            <div class="flex items-center gap-2 mb-3">
              <Package class="w-5 h-5 text-blue-600" />
              <h4 class="font-semibold text-blue-900">核销说明</h4>
            </div>
            <p class="text-sm text-blue-800">系统根据ERP的BOM自动计算理论耗量，定期比对"实耗 vs. 应耗"，自动生成损耗分析报告。</p>
          </div>
          <div v-if="reconciliationViewMode === 'grid'" v-for="warehouse in filteredReconciliations" :key="warehouse.id" class="border border-gray-200 rounded-lg p-4">
            <div class="flex items-center justify-between mb-4">
              <div>
                <h4 class="font-semibold text-gray-900">{{ warehouse.materialName }}</h4>
                <p class="text-sm text-gray-500 mt-1">{{ warehouse.supplierName }}</p>
              </div>
              <button @click="handleGenerateReconciliation(warehouse)" class="px-3 py-1.5 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50 flex items-center gap-1">
                <BarChart class="w-4 h-4" />{{ appStore.t('btn.generateReconciliation') }}
              </button>
            </div>
            <div class="grid grid-cols-3 gap-4 mb-4">
              <div class="p-3 bg-gray-50 rounded-lg"><p class="text-xs text-gray-600">应耗数量</p><p class="text-lg font-semibold text-gray-900 mt-1">{{ warehouse.theoreticalQty }} {{ warehouse.unit }}</p></div>
              <div class="p-3 bg-gray-50 rounded-lg"><p class="text-xs text-gray-600">实耗数量</p><p class="text-lg font-semibold text-gray-900 mt-1">{{ warehouse.theoreticalQty - warehouse.variance }} {{ warehouse.unit }}</p></div>
              <div class="p-3 rounded-lg" :class="Math.abs(warehouse.varianceRate) > 2 ? 'bg-red-50' : 'bg-green-50'">
                <p class="text-xs text-gray-600">损耗率</p>
                <p class="text-lg font-semibold mt-1" :class="Math.abs(warehouse.varianceRate) > 2 ? 'text-red-600' : 'text-green-600'">{{ Math.abs(warehouse.varianceRate) }}%</p>
              </div>
            </div>
            <div v-if="Math.abs(warehouse.varianceRate) > 2" class="flex items-center gap-2 p-3 bg-orange-50 rounded-lg">
              <AlertTriangle class="w-4 h-4 text-orange-600" />
              <p class="text-sm text-orange-800">损耗率超过阈值（2%），建议核查原因</p>
            </div>
          </div>
          <div v-else class="border border-gray-200 rounded-lg overflow-x-auto">
            <table class="w-full min-w-max text-sm">
              <thead class="bg-gray-50">
                <tr>
                  <th class="text-left px-4 py-3 font-medium text-gray-700">{{ appStore.t('warehouse.table.materialName') }}</th>
                  <th class="text-left px-4 py-3 font-medium text-gray-700">{{ appStore.t('warehouse.table.supplier') }}</th>
                  <th class="text-right px-4 py-3 font-medium text-gray-700">{{ appStore.t('warehouse.recon.table.requiredQty') }}</th>
                  <th class="text-right px-4 py-3 font-medium text-gray-700">{{ appStore.t('warehouse.recon.table.actualQty') }}</th>
                  <th class="text-right px-4 py-3 font-medium text-gray-700">{{ appStore.t('warehouse.recon.table.lossRate') }}</th>
                  <th class="text-left px-4 py-3 font-medium text-gray-700">{{ appStore.t('common.action') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="warehouse in filteredReconciliations" :key="warehouse.id" class="border-t border-gray-200">
                  <td class="px-4 py-3 text-gray-900 font-medium">{{ warehouse.materialName }}</td>
                  <td class="px-4 py-3 text-gray-900">{{ warehouse.supplierName }}</td>
                  <td class="px-4 py-3 text-right text-gray-900">{{ warehouse.theoreticalQty }} {{ warehouse.unit }}</td>
                  <td class="px-4 py-3 text-right text-gray-900">{{ warehouse.theoreticalQty - warehouse.variance }} {{ warehouse.unit }}</td>
                  <td class="px-4 py-3 text-right" :class="Math.abs(warehouse.varianceRate) > 2 ? 'text-red-600 font-medium' : 'text-green-600 font-medium'">{{ Math.abs(warehouse.varianceRate) }}%</td>
                  <td class="px-4 py-3">
                    <button @click="handleGenerateReconciliation(warehouse)" class="px-3 py-1.5 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50 flex items-center gap-1">
                      <BarChart class="w-4 h-4" />{{ appStore.t('btn.generateReconciliation') }}
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <!-- 库存明细对话框 -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="isWarehouseDetailOpen && selectedWarehouse" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-black/50" @click="isWarehouseDetailOpen = false" />
          <div class="relative bg-white rounded-xl shadow-xl w-full max-w-lg">
            <div class="flex items-center justify-between p-4 border-b">
              <h3 class="text-lg font-semibold text-gray-900">库存明细</h3>
              <button @click="isWarehouseDetailOpen = false" class="p-2 hover:bg-gray-100 rounded-lg">✕</button>
            </div>
            <div class="p-6">
              <div class="space-y-4">
                <div><p class="text-sm text-gray-500">物料名称</p><p class="font-medium text-gray-900">{{ selectedWarehouse.materialName }}</p></div>
                <div><p class="text-sm text-gray-500">物料编码</p><p class="font-medium text-gray-900">{{ selectedWarehouse.materialCode }}</p></div>
                <div><p class="text-sm text-gray-500">供应商</p><p class="font-medium text-gray-900">{{ selectedWarehouse.supplierName }}</p></div>
                <div class="grid grid-cols-2 gap-4">
                  <div><p class="text-sm text-gray-500">理论库存</p><p class="font-medium text-gray-900">{{ selectedWarehouse.theoreticalQty.toLocaleString() }} {{ selectedWarehouse.unit }}</p></div>
                  <div><p class="text-sm text-gray-500">实际库存</p><p class="font-medium text-gray-900">{{ selectedWarehouse.actualQty.toLocaleString() }} {{ selectedWarehouse.unit }}</p></div>
                  <div><p class="text-sm text-gray-500">差异数量</p><p class="font-medium" :class="selectedWarehouse.variance < 0 ? 'text-red-600' : 'text-yellow-600'">{{ selectedWarehouse.variance }} {{ selectedWarehouse.unit }}</p></div>
                  <div><p class="text-sm text-gray-500">差异率</p><p class="font-medium" :class="Math.abs(selectedWarehouse.varianceRate) > 2 ? 'text-red-600' : 'text-gray-900'">{{ selectedWarehouse.varianceRate }}%</p></div>
                </div>
              </div>
            </div>
            <div class="flex justify-end p-4 border-t bg-gray-50">
              <button @click="isWarehouseDetailOpen = false" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">关闭</button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- ASN详情对话框 -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="isASNDetailOpen && selectedASN" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-black/50" @click="isASNDetailOpen = false" />
          <div class="relative bg-white rounded-xl shadow-xl w-full max-w-2xl max-h-[80vh] overflow-hidden">
            <div class="flex items-center justify-between p-4 border-b">
              <h3 class="text-lg font-semibold text-gray-900">ASN详情 - {{ selectedASN.asnNo }}</h3>
              <button @click="isASNDetailOpen = false" class="p-2 hover:bg-gray-100 rounded-lg">✕</button>
            </div>
            <div class="p-6 overflow-y-auto max-h-[60vh]">
              <div class="space-y-4">
                <div class="grid grid-cols-2 gap-4">
                  <div><p class="text-sm text-gray-500">ASN号</p><p class="font-medium text-gray-900">{{ selectedASN.asnNo }}</p></div>
                  <div><p class="text-sm text-gray-500">状态</p><span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="selectedASN.status === '在途' ? 'bg-blue-100 text-blue-800' : 'bg-gray-100 text-gray-800'">{{ selectedASN.status }}</span></div>
                  <div><p class="text-sm text-gray-500">供应商</p><p class="font-medium text-gray-900">{{ selectedASN.supplierName }}</p></div>
                  <div><p class="text-sm text-gray-500">关联订单</p><p class="font-medium text-gray-900">{{ selectedASN.orderNo }}</p></div>
                  <div><p class="text-sm text-gray-500">发货日期</p><p class="font-medium text-gray-900">{{ selectedASN.shipDate }}</p></div>
                  <div><p class="text-sm text-gray-500">预计到达</p><p class="font-medium text-gray-900">{{ selectedASN.estimatedArrival }}</p></div>
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
                          <th class="text-left px-4 py-2 font-medium text-gray-700">条码</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="(item, idx) in selectedASN.items" :key="idx" class="border-t border-gray-200">
                          <td class="px-4 py-3 text-gray-900">{{ item.materialCode }}</td>
                          <td class="px-4 py-3 text-gray-900">{{ item.materialName }}</td>
                          <td class="px-4 py-3 text-right text-gray-900">{{ item.quantity }} {{ item.unit }}</td>
                          <td class="px-4 py-3 text-gray-900 font-mono text-xs">{{ item.barcode }}</td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            </div>
            <div class="flex justify-end gap-3 p-4 border-t bg-gray-50">
              <button @click="isASNDetailOpen = false" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">关闭</button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- 创建ASN对话框 -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="isCreateASNOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-black/50" @click="isCreateASNOpen = false" />
          <div class="relative bg-white rounded-xl shadow-xl w-full max-w-lg">
            <div class="flex items-center justify-between p-4 border-b">
              <h3 class="text-lg font-semibold text-gray-900">创建ASN</h3>
              <button @click="isCreateASNOpen = false" class="p-2 hover:bg-gray-100 rounded-lg">✕</button>
            </div>
            <div class="p-6 space-y-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">供应商</label>
                <select v-model="newASN.supplierId" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
                  <option value="">请选择供应商</option>
                  <option value="S001">深圳市精诚模具制造有限公司</option>
                  <option value="S002">东莞市华泰五金制品厂</option>
                </select>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">订单号</label>
                <input v-model="newASN.orderNo" type="text" placeholder="请输入订单号" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">发货日期</label>
                  <input v-model="newASN.shipDate" type="date" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">预计到达</label>
                  <input v-model="newASN.estimatedArrival" type="date" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>
              </div>
            </div>
            <div class="flex justify-end gap-3 p-4 border-t bg-gray-50">
              <button @click="isCreateASNOpen = false" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">取消</button>
              <button @click="handleSaveASN" class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">创建</button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- 库存调整对话框 -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="isInventoryAdjustDialogOpen && selectedWarehouse" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-black/50" @click="isInventoryAdjustDialogOpen = false" />
          <div class="relative bg-white rounded-xl shadow-xl w-full max-w-lg">
            <div class="flex items-center justify-between p-4 border-b">
              <h3 class="text-lg font-semibold text-gray-900">库存调整</h3>
              <button @click="isInventoryAdjustDialogOpen = false" class="p-2 hover:bg-gray-100 rounded-lg">✕</button>
            </div>
            <div class="p-6 space-y-4">
              <div class="p-3 bg-gray-50 rounded-lg">
                <p class="text-sm text-gray-500">物料名称</p>
                <p class="font-medium text-gray-900">{{ selectedWarehouse.materialName }}</p>
                <div class="mt-3 grid grid-cols-2 gap-3 text-sm">
                  <div><p class="text-gray-500">理论库存</p><p class="font-medium">{{ selectedWarehouse.theoreticalQty.toLocaleString() }} {{ selectedWarehouse.unit }}</p></div>
                  <div><p class="text-gray-500">当前实际库存</p><p class="font-medium">{{ selectedWarehouse.actualQty.toLocaleString() }} {{ selectedWarehouse.unit }}</p></div>
                </div>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">调整后实际库存 <span class="text-red-500">*</span></label>
                <input
                  v-model.number="inventoryAdjustForm.actualQty"
                  type="number"
                  min="0"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">调整原因 <span class="text-red-500">*</span></label>
                <select
                  v-model="inventoryAdjustForm.adjustReason"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="">请选择调整原因</option>
                  <option value="盘点调整">盘点调整</option>
                  <option value="供应商上报更新">供应商上报更新</option>
                  <option value="损耗报废">损耗报废</option>
                  <option value="数据错误修正">数据错误修正</option>
                  <option value="其他">其他</option>
                </select>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">调整日期</label>
                <input
                  v-model="inventoryAdjustForm.adjustDate"
                  type="date"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">操作人员</label>
                <input
                  v-model="inventoryAdjustForm.operator"
                  type="text"
                  placeholder="请输入操作人员姓名"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>
            </div>
            <div class="flex justify-end gap-3 p-4 border-t bg-gray-50">
              <button @click="isInventoryAdjustDialogOpen = false" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">取消</button>
              <button @click="handleSaveInventoryAdjust" class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">保存调整</button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- 核销单对话框 -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="isReconciliationDialogOpen && selectedWarehouseForRecon" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-black/50" @click="isReconciliationDialogOpen = false" />
          <div class="relative bg-white rounded-xl shadow-xl w-full max-w-lg">
            <div class="flex items-center justify-between p-4 border-b">
              <h3 class="text-lg font-semibold text-gray-900">生成核销单</h3>
              <button @click="isReconciliationDialogOpen = false" class="p-2 hover:bg-gray-100 rounded-lg">✕</button>
            </div>
            <div class="p-6 space-y-4">
              <div class="p-3 bg-gray-50 rounded-lg">
                <p class="text-sm text-gray-500">物料名称</p>
                <p class="font-medium text-gray-900">{{ selectedWarehouseForRecon.materialName }}</p>
                <div class="mt-3 grid grid-cols-3 gap-3 text-sm">
                  <div><p class="text-gray-500">应耗数量</p><p class="font-medium">{{ selectedWarehouseForRecon.theoreticalQty }} {{ selectedWarehouseForRecon.unit }}</p></div>
                  <div><p class="text-gray-500">实耗数量</p><p class="font-medium">{{ selectedWarehouseForRecon.actualQty }} {{ selectedWarehouseForRecon.unit }}</p></div>
                  <div><p class="text-gray-500">损耗率</p><p class="font-medium" :class="Math.abs(selectedWarehouseForRecon.varianceRate) > 2 ? 'text-red-600' : 'text-green-600'">{{ Math.abs(selectedWarehouseForRecon.varianceRate) }}%</p></div>
                </div>
              </div>
              <div class="border border-gray-200 rounded-lg p-4">
                <h4 class="text-sm font-medium text-gray-900 mb-3">核销单预览</h4>
                <div class="space-y-2 text-sm">
                  <div class="flex justify-between"><span class="text-gray-600">核销单号:</span><span class="font-medium">RS{{ Date.now() }}</span></div>
                  <div class="flex justify-between"><span class="text-gray-600">物料编码:</span><span class="font-medium">{{ selectedWarehouseForRecon.materialCode }}</span></div>
                  <div class="flex justify-between"><span class="text-gray-600">供应商:</span><span class="font-medium">{{ selectedWarehouseForRecon.supplierName }}</span></div>
                  <div class="flex justify-between"><span class="text-gray-600">核销日期:</span><span class="font-medium">{{ new Date().toISOString().split('T')[0] }}</span></div>
                  <div class="border-t pt-2 mt-2">
                    <div class="flex justify-between"><span class="text-gray-600">应耗:</span><span class="font-medium">{{ selectedWarehouseForRecon.theoreticalQty }} {{ selectedWarehouseForRecon.unit }}</span></div>
                    <div class="flex justify-between"><span class="text-gray-600">实耗:</span><span class="font-medium">{{ selectedWarehouseForRecon.actualQty }} {{ selectedWarehouseForRecon.unit }}</span></div>
                    <div class="flex justify-between"><span class="text-gray-600">差异数量:</span><span class="font-medium" :class="selectedWarehouseForRecon.variance < 0 ? 'text-red-600' : 'text-orange-600'">{{ selectedWarehouseForRecon.variance }} {{ selectedWarehouseForRecon.unit }}</span></div>
                    <div class="flex justify-between"><span class="text-gray-600">损耗率:</span><span class="font-medium" :class="Math.abs(selectedWarehouseForRecon.varianceRate) > 2 ? 'text-red-600' : 'text-green-600'">{{ selectedWarehouseForRecon.varianceRate }}%</span></div>
                  </div>
                </div>
              </div>
            </div>
            <div class="flex justify-end gap-3 p-4 border-t bg-gray-50">
              <button @click="isReconciliationDialogOpen = false" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">取消</button>
              <button @click="handleGenerateReconciliationStatement" class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">生成核销单</button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- 新增库存对话框 -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="isCreateInventoryOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-black/50" @click="isCreateInventoryOpen = false" />
          <div class="relative bg-white rounded-xl shadow-xl w-full max-w-lg">
            <div class="flex items-center justify-between p-4 border-b">
              <h3 class="text-lg font-semibold text-gray-900">新增虚拟库存</h3>
              <button @click="isCreateInventoryOpen = false" class="p-2 hover:bg-gray-100 rounded-lg">
                <X class="w-5 h-5" />
              </button>
            </div>
            <div class="p-6 space-y-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">供应商 <span class="text-red-500">*</span></label>
                <select v-model="newInventory.supplierId" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
                  <option value="">请选择供应商</option>
                  <option v-for="s in suppliers" :key="s.id" :value="s.id">{{ s.name }}</option>
                </select>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">物料编码 <span class="text-red-500">*</span></label>
                <input v-model="newInventory.materialCode" type="text" placeholder="请输入物料编码" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">物料名称 <span class="text-red-500">*</span></label>
                <input v-model="newInventory.materialName" type="text" placeholder="请输入物料名称" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">理论数量 <span class="text-red-500">*</span></label>
                  <input v-model.number="newInventory.theoreticalQty" type="number" min="0" placeholder="理论数量" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">实际数量</label>
                  <input v-model.number="newInventory.actualQty" type="number" min="0" placeholder="实际数量" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>
              </div>
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">单位</label>
                  <select v-model="newInventory.unit" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
                    <option value="个">个</option>
                    <option value="套">套</option>
                    <option value="件">件</option>
                    <option value="KG">KG</option>
                    <option value="米">米</option>
                  </select>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">存放位置</label>
                  <input v-model="newInventory.location" type="text" placeholder="请输入存放位置" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">备注</label>
                <textarea v-model="newInventory.notes" rows="2" placeholder="请输入备注信息（可选）" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"></textarea>
              </div>
            </div>
            <div class="flex justify-end gap-3 p-4 border-t bg-gray-50">
              <button @click="isCreateInventoryOpen = false" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">取消</button>
              <button @click="handleSaveInventory" class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">保存</button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- 批量上传库存对话框 -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="isUploadInventoryOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-black/50" @click="isUploadInventoryOpen = false" />
          <div class="relative bg-white rounded-xl shadow-xl w-full max-w-3xl max-h-[90vh] overflow-hidden flex flex-col">
            <div class="flex items-center justify-between p-4 border-b">
              <h3 class="text-lg font-semibold text-gray-900">批量上传库存</h3>
              <button @click="isUploadInventoryOpen = false" class="p-2 hover:bg-gray-100 rounded-lg">
                <X class="w-5 h-5" />
              </button>
            </div>
            <div class="p-6 space-y-4 overflow-y-auto flex-1">
              <!-- 说明和下载模板 -->
              <div class="p-4 bg-blue-50 rounded-lg">
                <div class="flex items-start gap-3">
                  <div class="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center flex-shrink-0">
                    <Upload class="w-5 h-5 text-blue-600" />
                  </div>
                  <div class="flex-1">
                    <h4 class="font-medium text-blue-900 mb-1">上传说明</h4>
                    <p class="text-sm text-blue-700 mb-2">请先下载模板，按照模板格式填写库存信息后上传。支持 CSV、Excel 格式文件。</p>
                    <button @click="downloadTemplate" class="inline-flex items-center gap-2 px-3 py-1.5 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">
                      <Download class="w-4 h-4" />下载模板
                    </button>
                  </div>
                </div>
              </div>

              <!-- 文件上传区域 -->
              <div class="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center hover:border-blue-400 transition-colors">
                <input
                  type="file"
                  accept=".csv,.xlsx,.xls"
                  @change="handleFileSelect"
                  class="hidden"
                  id="file-upload"
                  :disabled="isProcessingUpload"
                />
                <label for="file-upload" class="cursor-pointer">
                  <div class="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-3">
                    <Upload class="w-8 h-8 text-gray-400" />
                  </div>
                  <p class="text-sm text-gray-600 mb-1">点击上传或拖拽文件到此处</p>
                  <p class="text-xs text-gray-500">支持 CSV、Excel 格式，单次最多导入 1000 条数据</p>
                </label>
              </div>

              <!-- 文件信息 -->
              <div v-if="uploadedFile" class="flex items-center justify-between p-3 bg-green-50 rounded-lg">
                <div class="flex items-center gap-2">
                  <div class="w-8 h-8 bg-green-100 rounded-lg flex items-center justify-center">
                    <Upload class="w-4 h-4 text-green-600" />
                  </div>
                  <div>
                    <p class="text-sm font-medium text-gray-900">{{ uploadedFile.name }}</p>
                    <p class="text-xs text-gray-500">{{ (uploadedFile.size / 1024).toFixed(2) }} KB</p>
                  </div>
                </div>
                <button @click="uploadedFile = null; uploadPreview = []" class="p-1 hover:bg-gray-200 rounded">
                  <X class="w-4 h-4 text-gray-500" />
                </button>
              </div>

              <!-- 预览表格 -->
              <div v-if="uploadPreview.length > 0" class="border border-gray-200 rounded-lg overflow-hidden">
                <div class="px-4 py-3 bg-gray-50 border-b flex items-center justify-between">
                  <h4 class="font-medium text-gray-900">数据预览 (前 10 条)</h4>
                  <span class="text-sm text-gray-500">共 {{ uploadPreview.length }} 条数据</span>
                </div>
                <div class="overflow-x-auto max-h-64">
                  <table class="w-full text-sm">
                    <thead class="bg-gray-50 sticky top-0">
                      <tr>
                        <th class="text-left px-4 py-2 font-medium text-gray-700 whitespace-nowrap">供应商</th>
                        <th class="text-left px-4 py-2 font-medium text-gray-700 whitespace-nowrap">物料编码</th>
                        <th class="text-left px-4 py-2 font-medium text-gray-700 whitespace-nowrap">物料名称</th>
                        <th class="text-right px-4 py-2 font-medium text-gray-700 whitespace-nowrap">理论数量</th>
                        <th class="text-right px-4 py-2 font-medium text-gray-700 whitespace-nowrap">实际数量</th>
                        <th class="text-left px-4 py-2 font-medium text-gray-700 whitespace-nowrap">单位</th>
                        <th class="text-left px-4 py-2 font-medium text-gray-700 whitespace-nowrap">状态</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="(item, idx) in uploadPreview" :key="idx" class="border-t border-gray-200" :class="!item.valid ? 'bg-red-50' : ''">
                        <td class="px-4 py-2 text-gray-900">{{ item.supplierId || '-' }}</td>
                        <td class="px-4 py-2 text-gray-900 font-mono">{{ item.materialCode || '-' }}</td>
                        <td class="px-4 py-2 text-gray-900">{{ item.materialName || '-' }}</td>
                        <td class="px-4 py-2 text-right text-gray-900">{{ item.theoreticalQty || 0 }}</td>
                        <td class="px-4 py-2 text-right text-gray-900">{{ item.actualQty || 0 }}</td>
                        <td class="px-4 py-2 text-gray-900">{{ item.unit }}</td>
                        <td class="px-4 py-2">
                          <span v-if="item.valid" class="px-2 py-0.5 bg-green-100 text-green-800 rounded-full text-xs">有效</span>
                          <span v-else class="px-2 py-0.5 bg-red-100 text-red-800 rounded-full text-xs">无效</span>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>

              <!-- 处理中提示 -->
              <div v-if="isProcessingUpload" class="flex items-center justify-center py-4">
                <div class="animate-spin w-5 h-5 border-2 border-blue-600 border-t-transparent rounded-full mr-2"></div>
                <span class="text-sm text-gray-600">正在解析文件...</span>
              </div>
            </div>
            <div class="flex justify-end gap-3 p-4 border-t bg-gray-50">
              <button @click="isUploadInventoryOpen = false" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">取消</button>
              <button
                @click="handleConfirmUpload"
                :disabled="uploadPreview.length === 0 || isProcessingUpload"
                class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed"
              >
                确认导入 ({{ uploadPreview.filter(i => i.valid).length }} 条有效)
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- 新增核销对话框 -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="isCreateReconciliationOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-black/50" @click="isCreateReconciliationOpen = false" />
          <div class="relative bg-white rounded-xl shadow-xl w-full max-w-lg">
            <div class="flex items-center justify-between p-4 border-b">
              <h3 class="text-lg font-semibold text-gray-900">新增核销单</h3>
              <button @click="isCreateReconciliationOpen = false" class="p-2 hover:bg-gray-100 rounded-lg">
                <X class="w-5 h-5" />
              </button>
            </div>
            <div class="p-6 space-y-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">供应商 <span class="text-red-500">*</span></label>
                <select v-model="newReconciliation.supplierId" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
                  <option value="">请选择供应商</option>
                  <option v-for="s in suppliers" :key="s.id" :value="s.id">{{ s.name }}</option>
                </select>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">物料编码 <span class="text-red-500">*</span></label>
                <input v-model="newReconciliation.materialCode" type="text" placeholder="请输入物料编码" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">物料名称 <span class="text-red-500">*</span></label>
                <input v-model="newReconciliation.materialName" type="text" placeholder="请输入物料名称" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">应耗数量 <span class="text-red-500">*</span></label>
                  <input v-model.number="newReconciliation.theoreticalQty" type="number" min="0" placeholder="应耗数量" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">实耗数量 <span class="text-red-500">*</span></label>
                  <input v-model.number="newReconciliation.actualQty" type="number" min="0" placeholder="实耗数量" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>
              </div>
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">单位</label>
                  <select v-model="newReconciliation.unit" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
                    <option value="个">个</option>
                    <option value="套">套</option>
                    <option value="件">件</option>
                    <option value="KG">KG</option>
                    <option value="米">米</option>
                    <option value="张">张</option>
                  </select>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">核销日期</label>
                  <input v-model="newReconciliation.reconciliationDate" type="date" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">备注</label>
                <textarea v-model="newReconciliation.notes" rows="2" placeholder="请输入备注信息（可选）" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"></textarea>
              </div>
              <!-- 预览损耗信息 -->
              <div v-if="newReconciliation.theoreticalQty > 0" class="p-3 bg-gray-50 rounded-lg">
                <p class="text-sm font-medium text-gray-700 mb-2">损耗预览</p>
                <div class="grid grid-cols-2 gap-3 text-sm">
                  <div><span class="text-gray-600">差异数量:</span> <span class="font-medium">{{ newReconciliation.theoreticalQty - newReconciliation.actualQty }} {{ newReconciliation.unit }}</span></div>
                  <div><span class="text-gray-600">损耗率:</span> <span class="font-medium" :class="Math.abs(((newReconciliation.theoreticalQty - newReconciliation.actualQty) / newReconciliation.theoreticalQty) * 100) > 2 ? 'text-red-600' : 'text-green-600'">{{ (((newReconciliation.theoreticalQty - newReconciliation.actualQty) / newReconciliation.theoreticalQty) * 100).toFixed(2) }}%</span></div>
                </div>
              </div>
            </div>
            <div class="flex justify-end gap-3 p-4 border-t bg-gray-50">
              <button @click="isCreateReconciliationOpen = false" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">取消</button>
              <button @click="handleSaveReconciliation" class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">保存</button>
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
