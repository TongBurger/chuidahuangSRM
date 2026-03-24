<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Upload, FileText, CheckCircle, X, Eye, Download } from 'lucide-vue-next'
import { useAppStore } from '@/stores/useAppStore'

const appStore = useAppStore()
const supplierId = 'S001'

const isUploadDialogOpen = ref(false)
const isDetailDialogOpen = ref(false)
const selectedInvoice = ref<any>(null)

// Mock 发票数据 - 扩展到12条
const mockInvoices = [
  { id: 'INV001', invoiceNo: 'INV202603001', amount: 285000, uploadDate: '2026-03-20', status: '待审核', supplierId: 'S001', fileName: '发票_INV202603001.pdf' },
  { id: 'INV002', invoiceNo: 'INV202603002', amount: 456000, uploadDate: '2026-03-18', status: '待审核', supplierId: 'S001', fileName: '发票_INV202603002.pdf' },
  { id: 'INV003', invoiceNo: 'INV202602001', amount: 328000, uploadDate: '2026-02-28', status: '已认证', supplierId: 'S001', fileName: '发票_INV202602001.pdf' },
  { id: 'INV004', invoiceNo: 'INV202602002', amount: 215000, uploadDate: '2026-02-25', status: '已认证', supplierId: 'S001', fileName: '发票_INV202602002.pdf' },
  { id: 'INV005', invoiceNo: 'INV202602003', amount: 392000, uploadDate: '2026-02-22', status: '已认证', supplierId: 'S001', fileName: '发票_INV202602003.pdf' },
  { id: 'INV006', invoiceNo: 'INV202602004', amount: 178000, uploadDate: '2026-02-18', status: '已认证', supplierId: 'S001', fileName: '发票_INV202602004.pdf' },
  { id: 'INV007', invoiceNo: 'INV202602005', amount: 524000, uploadDate: '2026-02-15', status: '已认证', supplierId: 'S001', fileName: '发票_INV202602005.pdf' },
  { id: 'INV008', invoiceNo: 'INV202601001', amount: 298000, uploadDate: '2026-01-30', status: '已认证', supplierId: 'S001', fileName: '发票_INV202601001.pdf' },
  { id: 'INV009', invoiceNo: 'INV202601002', amount: 412000, uploadDate: '2026-01-25', status: '已认证', supplierId: 'S001', fileName: '发票_INV202601002.pdf' },
  { id: 'INV010', invoiceNo: 'INV202601003', amount: 335000, uploadDate: '2026-01-20', status: '已认证', supplierId: 'S001', fileName: '发票_INV202601003.pdf' },
  { id: 'INV011', invoiceNo: 'INV202512001', amount: 267000, uploadDate: '2025-12-28', status: '已认证', supplierId: 'S001', fileName: '发票_INV202512001.pdf' },
  { id: 'INV012', invoiceNo: 'INV202512002', amount: 589000, uploadDate: '2025-12-15', status: '已驳回', supplierId: 'S001', fileName: '发票_INV202512002.pdf', rejectReason: '发票金额与订单不符，请核对后重新上传' },
]

const invoices = ref<any[]>([])

// 新增发票表单
const newInvoice = ref({
  invoiceNo: '',
  amount: '',
  invoiceDate: new Date().toISOString().split('T')[0],
  file: null as File | null,
})

onMounted(() => {
  // 加载发票数据
  const saved = localStorage.getItem('supplier_invoices')
  if (saved) {
    try {
      const allInvoices = JSON.parse(saved)
      invoices.value = allInvoices.filter((i: any) => i.supplierId === supplierId)
    } catch {
      invoices.value = mockInvoices
    }
  } else {
    invoices.value = mockInvoices
  }
})

function getStatusClass(status: string) {
  switch (status) {
    case '已认证': return 'bg-green-100 text-green-800'
    case '待审核': return 'bg-orange-100 text-orange-800'
    case '已驳回': return 'bg-red-100 text-red-800'
    default: return 'bg-gray-100 text-gray-700'
  }
}

// 上传发票
function handleUploadInvoice() {
  if (!newInvoice.value.invoiceNo.trim()) {
    appStore.showToast('请输入发票号码', 'warning')
    return
  }
  if (!newInvoice.value.amount || Number(newInvoice.value.amount) <= 0) {
    appStore.showToast('请输入有效的发票金额', 'warning')
    return
  }

  const invoice = {
    id: `INV${Date.now()}`,
    invoiceNo: newInvoice.value.invoiceNo,
    amount: Number(newInvoice.value.amount),
    uploadDate: newInvoice.value.invoiceDate,
    status: '待审核',
    supplierId,
    fileName: newInvoice.value.file?.name || '未上传文件',
    createdAt: new Date().toISOString(),
  }

  // 保存到 localStorage
  const saved = localStorage.getItem('supplier_invoices')
  let allInvoices = saved ? JSON.parse(saved) : []
  allInvoices.push(invoice)
  localStorage.setItem('supplier_invoices', JSON.stringify(allInvoices))

  // 更新本地数据
  invoices.value.unshift(invoice)

  appStore.showToast('发票上传成功', 'success')
  isUploadDialogOpen.value = false

  // 重置表单
  newInvoice.value = {
    invoiceNo: '',
    amount: '',
    invoiceDate: new Date().toISOString().split('T')[0],
    file: null,
  }
}

// 处理文件选择
function handleFileSelect(event: Event) {
  const target = event.target as HTMLInputElement
  if (target.files && target.files[0]) {
    const file = target.files[0]
    // 验证文件大小 (5MB)
    if (file.size > 5 * 1024 * 1024) {
      appStore.showToast('文件大小不能超过5MB', 'error')
      return
    }
    // 验证文件类型
    const allowedTypes = ['application/pdf', 'image/jpeg', 'image/png', 'image/jpg']
    if (!allowedTypes.includes(file.type)) {
      appStore.showToast('仅支持PDF、JPG、PNG格式', 'error')
      return
    }
    newInvoice.value.file = file
    appStore.showToast(`已选择文件: ${file.name}`, 'info')
  }
}

// 查看详情
function handleViewDetail(invoice: any) {
  selectedInvoice.value = invoice
  isDetailDialogOpen.value = true
}

// 下载发票
function handleDownloadInvoice(invoice: any) {
  appStore.showToast(`正在下载发票 ${invoice.invoiceNo}...`, 'info')
  setTimeout(() => {
    appStore.showToast('下载完成', 'success')
  }, 1500)
}

// 删除发票
function handleDeleteInvoice(invoiceId: string) {
  if (confirm('确定要删除这张发票吗？')) {
    invoices.value = invoices.value.filter(i => i.id !== invoiceId)

    // 更新 localStorage
    const saved = localStorage.getItem('supplier_invoices')
    if (saved) {
      let allInvoices = JSON.parse(saved)
      allInvoices = allInvoices.filter((i: any) => i.id !== invoiceId)
      localStorage.setItem('supplier_invoices', JSON.stringify(allInvoices))
    }

    appStore.showToast('发票已删除', 'success')
    if (selectedInvoice.value?.id === invoiceId) {
      isDetailDialogOpen.value = false
    }
  }
}
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-semibold text-gray-900">发票管理</h1>
        <p class="text-sm text-gray-500 mt-1">上传和管理发票</p>
      </div>
      <button class="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700" @click="isUploadDialogOpen = true">
        <Upload class="w-4 h-4" />上传发票
      </button>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">发票总数</p>
            <p class="text-2xl font-semibold text-gray-900 mt-1">{{ invoices.length }}</p>
          </div>
          <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
            <FileText class="w-6 h-6 text-blue-600" />
          </div>
        </div>
      </div>
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">待审核</p>
            <p class="text-2xl font-semibold text-orange-600 mt-1">{{ invoices.filter(i => i.status === '待审核').length }}</p>
          </div>
          <div class="w-12 h-12 bg-orange-100 rounded-lg flex items-center justify-center">
            <Eye class="w-6 h-6 text-orange-600" />
          </div>
        </div>
      </div>
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">已认证</p>
            <p class="text-2xl font-semibold text-green-600 mt-1">{{ invoices.filter(i => i.status === '已认证').length }}</p>
          </div>
          <div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
            <CheckCircle class="w-6 h-6 text-green-600" />
          </div>
        </div>
      </div>
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">总金额</p>
            <p class="text-2xl font-semibold text-purple-600 mt-1">¥{{ (invoices.reduce((sum, i) => sum + i.amount, 0) / 10000).toFixed(1) }}万</p>
          </div>
          <div class="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center">
            <FileText class="w-6 h-6 text-purple-600" />
          </div>
        </div>
      </div>
    </div>

    <div class="bg-white rounded-lg border border-gray-200 overflow-hidden">
      <table class="w-full text-sm">
        <thead class="bg-gray-50">
          <tr>
            <th class="text-left px-4 py-3 font-medium text-gray-700">发票号码</th>
            <th class="text-right px-4 py-3 font-medium text-gray-700">发票金额</th>
            <th class="text-left px-4 py-3 font-medium text-gray-700">上传日期</th>
            <th class="text-left px-4 py-3 font-medium text-gray-700">状态</th>
            <th class="text-left px-4 py-3 font-medium text-gray-700">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="invoice in invoices" :key="invoice.id" class="border-t border-gray-200">
            <td class="px-4 py-3 font-medium text-gray-900">{{ invoice.invoiceNo }}</td>
            <td class="px-4 py-3 text-right text-gray-900">¥{{ invoice.amount.toLocaleString() }}</td>
            <td class="px-4 py-3 text-gray-900">{{ invoice.uploadDate }}</td>
            <td class="px-4 py-3">
              <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="getStatusClass(invoice.status)">{{ invoice.status }}</span>
            </td>
            <td class="px-4 py-3">
              <div class="flex items-center gap-2">
                <button @click="handleViewDetail(invoice)" class="px-3 py-1.5 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50 flex items-center gap-1">
                  <Eye class="w-3 h-3" />查看
                </button>
                <button @click="handleDownloadInvoice(invoice)" class="p-1.5 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50" title="下载">
                  <Download class="w-4 h-4" />
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="invoices.length === 0" class="text-center py-12 text-gray-500">
        <FileText class="w-12 h-12 mx-auto mb-4 opacity-50" />
        <p>暂无发票记录</p>
      </div>
    </div>

    <!-- 上传发票对话框 -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="isUploadDialogOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-black/50" @click="isUploadDialogOpen = false" />
          <div class="relative bg-white rounded-xl shadow-xl w-full max-w-lg p-6">
            <div class="flex items-center justify-between mb-4">
              <h3 class="text-lg font-semibold text-gray-900">上传发票</h3>
              <button @click="isUploadDialogOpen = false" class="p-2 hover:bg-gray-100 rounded-lg">
                <X class="w-5 h-5" />
              </button>
            </div>
            <div class="space-y-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">发票号码 *</label>
                <input v-model="newInvoice.invoiceNo" type="text" placeholder="请输入发票号码" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">发票金额 *</label>
                <input v-model.number="newInvoice.amount" type="number" placeholder="请输入发票金额" min="0" step="0.01" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">开票日期</label>
                <input v-model="newInvoice.invoiceDate" type="date" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">发票文件</label>
                <div class="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center hover:border-blue-400 transition-colors cursor-pointer">
                  <input type="file" accept=".pdf,.jpg,.jpeg,.png" @change="handleFileSelect" class="hidden" id="invoice-file-input" />
                  <label for="invoice-file-input" class="cursor-pointer">
                    <Upload class="w-8 h-8 text-gray-400 mx-auto mb-2" />
                    <p class="text-sm text-gray-600">点击或拖拽文件到此处上传</p>
                    <p class="text-xs text-gray-500 mt-1">支持 PDF、JPG、PNG 格式，不超过 5MB</p>
                    <p v-if="newInvoice.file" class="text-sm text-blue-600 mt-2">{{ newInvoice.file.name }}</p>
                  </label>
                </div>
              </div>
            </div>
            <div class="flex justify-end gap-3 mt-6">
              <button @click="isUploadDialogOpen = false" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">取消</button>
              <button @click="handleUploadInvoice" class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">上传</button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- 发票详情对话框 -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="isDetailDialogOpen && selectedInvoice" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-black/50" @click="isDetailDialogOpen = false" />
          <div class="relative bg-white rounded-xl shadow-xl w-full max-w-lg">
            <div class="flex items-center justify-between p-4 border-b">
              <h3 class="text-lg font-semibold text-gray-900">发票详情</h3>
              <button @click="isDetailDialogOpen = false" class="p-2 hover:bg-gray-100 rounded-lg">
                <X class="w-5 h-5" />
              </button>
            </div>
            <div class="p-6">
              <div class="space-y-4">
                <div class="grid grid-cols-2 gap-4">
                  <div>
                    <p class="text-sm text-gray-500">发票号码</p>
                    <p class="font-medium text-gray-900">{{ selectedInvoice.invoiceNo }}</p>
                  </div>
                  <div>
                    <p class="text-sm text-gray-500">发票金额</p>
                    <p class="font-medium text-gray-900">¥{{ selectedInvoice.amount.toLocaleString() }}</p>
                  </div>
                  <div>
                    <p class="text-sm text-gray-500">上传日期</p>
                    <p class="font-medium text-gray-900">{{ selectedInvoice.uploadDate }}</p>
                  </div>
                  <div>
                    <p class="text-sm text-gray-500">状态</p>
                    <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="getStatusClass(selectedInvoice.status)">{{ selectedInvoice.status }}</span>
                  </div>
                </div>
                <div v-if="selectedInvoice.fileName">
                  <p class="text-sm text-gray-500">附件</p>
                  <div class="flex items-center gap-2 mt-1 p-2 bg-gray-50 rounded-lg">
                    <FileText class="w-4 h-4 text-gray-500" />
                    <span class="text-sm text-gray-900">{{ selectedInvoice.fileName }}</span>
                  </div>
                </div>
                <div v-if="selectedInvoice.rejectReason" class="p-3 bg-red-50 rounded-lg">
                  <p class="text-sm text-red-800"><strong>驳回原因：</strong>{{ selectedInvoice.rejectReason }}</p>
                </div>
              </div>
            </div>
            <div class="flex justify-end gap-3 p-4 border-t bg-gray-50">
              <button @click="isDetailDialogOpen = false" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">关闭</button>
              <button @click="handleDownloadInvoice(selectedInvoice)" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50 flex items-center gap-2">
                <Download class="w-4 h-4" />下载
              </button>
              <button @click="handleDeleteInvoice(selectedInvoice.id)" class="px-4 py-2 bg-red-600 text-white rounded-lg text-sm hover:bg-red-700">删除</button>
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
