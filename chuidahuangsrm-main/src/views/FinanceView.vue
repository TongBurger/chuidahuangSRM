<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { FileCheck, DollarSign, Clock, CheckCircle, LayoutGrid, List, Eye, Download, Upload, X, AlertCircle } from 'lucide-vue-next'
import { useAppStore } from '@/stores/useAppStore'

const appStore = useAppStore()
const activeTab = ref('reconciliation')
const viewMode = ref<'grid' | 'list'>('list')

// 对账单数据
const reconciliations = ref<any[]>([])

// 发票数据
const invoices = ref<any[]>([])

// 付款记录数据
const payments = ref<any[]>([])

// 对账单详情对话框
const selectedReconciliation = ref<any>(null)
const isReconDetailOpen = ref(false)

// 发票上传对话框
const isInvoiceUploadOpen = ref(false)
const newInvoice = ref({
  supplierId: '',
  invoiceNo: '',
  amount: '',
  taxAmount: '',
  invoiceDate: new Date().toISOString().split('T')[0],
  file: null as File | null,
})

// 付款对话框
const isPaymentDialogOpen = ref(false)
const selectedPaymentRecon = ref<any>(null)

// 加载数据
onMounted(() => {
  // 加载对账单数据
  const savedRecon = localStorage.getItem('finance_reconciliations')
  if (savedRecon) {
    try {
      reconciliations.value = JSON.parse(savedRecon)
    } catch {
      reconciliations.value = [
        { id: 'RC001', supplier: '深圳市精诚模具制造有限公司', supplierId: 'S001', period: '2026年3月', orderCount: 5, totalAmount: 285000, paidAmount: 150000, unpaidAmount: 135000, status: '待确认' },
        { id: 'RC002', supplier: '东莞市华泰五金制品厂', supplierId: 'S002', period: '2026年3月', orderCount: 3, totalAmount: 156000, paidAmount: 156000, unpaidAmount: 0, status: '已完成' },
      ]
    }
  } else {
    reconciliations.value = [
      { id: 'RC001', supplier: '深圳市精诚模具制造有限公司', supplierId: 'S001', period: '2026年3月', orderCount: 5, totalAmount: 285000, paidAmount: 150000, unpaidAmount: 135000, status: '待确认' },
      { id: 'RC002', supplier: '东莞市华泰五金制品厂', supplierId: 'S002', period: '2026年3月', orderCount: 3, totalAmount: 156000, paidAmount: 156000, unpaidAmount: 0, status: '已完成' },
    ]
  }

  // 加载发票数据
  const savedInvoices = localStorage.getItem('finance_invoices')
  if (savedInvoices) {
    try {
      invoices.value = JSON.parse(savedInvoices)
    } catch {
      invoices.value = [
        { id: 'INV001', invoiceNo: '44030001', supplier: '深圳市精诚模具制造有限公司', amount: 285000, taxAmount: 36225, totalAmount: 321225, invoiceDate: '2026-03-15', status: '已上传' },
      ]
    }
  } else {
    invoices.value = [
      { id: 'INV001', invoiceNo: '44030001', supplier: '深圳市精诚模具制造有限公司', amount: 285000, taxAmount: 36225, totalAmount: 321225, invoiceDate: '2026-03-15', status: '已上传' },
    ]
  }

  // 加载付款记录
  const savedPayments = localStorage.getItem('finance_payments')
  if (savedPayments) {
    try {
      payments.value = JSON.parse(savedPayments)
    } catch {
      payments.value = [
        { id: 'PAY001', supplier: '东莞市华泰五金制品厂', amount: 156000, paymentDate: '2026-03-18', status: '已付款', method: '银行转账' },
      ]
    }
  } else {
    payments.value = [
      { id: 'PAY001', supplier: '东莞市华泰五金制品厂', amount: 156000, paymentDate: '2026-03-18', status: '已付款', method: '银行转账' },
    ]
  }
})

const totalUnpaid = reconciliations.value.reduce((sum, r) => sum + r.unpaidAmount, 0)
const totalPaid = reconciliations.value.reduce((sum, r) => sum + r.paidAmount, 0)

// 查看对账单明细
function handleViewReconDetail(recon: any) {
  selectedReconciliation.value = recon
  isReconDetailOpen.value = true
}

// 下载对账单
function handleDownloadRecon(recon: any) {
  appStore.showToast(`正在下载 ${recon.period} 对账单...`, 'info')
  setTimeout(() => {
    appStore.showToast('对账单下载完成', 'success')
  }, 1500)
}

// 推送供应商确认
function handlePushToSupplier(recon: any) {
  // 更新对账单状态
  const idx = reconciliations.value.findIndex(r => r.id === recon.id)
  if (idx > -1) {
    reconciliations.value[idx].status = '已推送'
    localStorage.setItem('finance_reconciliations', JSON.stringify(reconciliations.value))
    appStore.showToast(`已推送对账单至 ${recon.supplier}`, 'success')
  }
}

// 发起付款
function handleInitiatePayment(recon: any) {
  selectedPaymentRecon.value = recon
  isPaymentDialogOpen.value = true
}

// 确认付款
function handleConfirmPayment() {
  if (!selectedPaymentRecon.value) return

  const payment = {
    id: `PAY${Date.now()}`,
    supplier: selectedPaymentRecon.value.supplier,
    amount: selectedPaymentRecon.value.unpaidAmount,
    paymentDate: new Date().toISOString().split('T')[0],
    status: '已付款',
    method: '银行转账',
  }

  // 更新付款记录
  payments.value.unshift(payment)
  localStorage.setItem('finance_payments', JSON.stringify(payments.value))

  // 更新对账单状态
  const idx = reconciliations.value.findIndex(r => r.id === selectedPaymentRecon.value.id)
  if (idx > -1) {
    reconciliations.value[idx].paidAmount += selectedPaymentRecon.value.unpaidAmount
    reconciliations.value[idx].unpaidAmount = 0
    reconciliations.value[idx].status = '已完成'
    localStorage.setItem('finance_reconciliations', JSON.stringify(reconciliations.value))
  }

  appStore.showToast('付款已成功发起', 'success')
  isPaymentDialogOpen.value = false
  selectedPaymentRecon.value = null
}

// 查看发票影像
function handleViewInvoiceImage(invoice: any) {
  appStore.showToast(`正在打开 ${invoice.invoiceNo} 发票影像...`, 'info')
}

// 下载发票PDF
function handleDownloadInvoice(invoice: any) {
  appStore.showToast(`正在下载 ${invoice.invoiceNo} PDF...`, 'info')
  setTimeout(() => {
    appStore.showToast('发票下载完成', 'success')
  }, 1500)
}

// 上传发票
function handleUploadInvoice() {
  if (!newInvoice.value.supplierId) {
    appStore.showToast('请选择供应商', 'warning')
    return
  }
  if (!newInvoice.value.invoiceNo || !newInvoice.value.amount) {
    appStore.showToast('请填写发票信息', 'warning')
    return
  }

  const invoice = {
    id: `INV${Date.now()}`,
    invoiceNo: newInvoice.value.invoiceNo,
    supplier: reconciliations.value.find(r => r.supplierId === newInvoice.value.supplierId)?.supplier || '',
    supplierId: newInvoice.value.supplierId,
    amount: Number(newInvoice.value.amount),
    taxAmount: Number(newInvoice.value.taxAmount) || 0,
    totalAmount: Number(newInvoice.value.amount) + (Number(newInvoice.value.taxAmount) || 0),
    invoiceDate: newInvoice.value.invoiceDate,
    status: '已上传',
  }

  invoices.value.unshift(invoice)
  localStorage.setItem('finance_invoices', JSON.stringify(invoices.value))

  appStore.showToast('发票上传成功', 'success')
  isInvoiceUploadOpen.value = false
  newInvoice.value = {
    supplierId: '',
    invoiceNo: '',
    amount: '',
    taxAmount: '',
    invoiceDate: new Date().toISOString().split('T')[0],
    file: null,
  }
}

// 查看付款详情
function handleViewPaymentDetail(payment: any) {
  appStore.showToast(`查看付款记录 ${payment.id}`, 'info')
}

// 供应商列表
const suppliers = [
  { id: 'S001', name: '深圳市精诚模具制造有限公司' },
  { id: 'S002', name: '东莞市华泰五金制品厂' },
  { id: 'S003', name: '佛山市永盛铝制品有限公司' },
]
</script>

<template>
  <div class="space-y-6">
    <!-- 标题 -->
    <div>
      <h1 class="text-2xl font-semibold text-gray-900">{{ appStore.t('page.finance.title') }}</h1>
      <p class="text-sm text-gray-500 mt-1">自动对账 · 电子签章 · 发票管理</p>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex items-center justify-between"><div><p class="text-sm text-gray-500">{{ appStore.t('finance.kpi.monthlyRecon') }}</p><p class="text-2xl font-semibold text-gray-900 mt-1">{{ reconciliations.length }}</p></div><div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center"><FileCheck class="w-6 h-6 text-blue-600" /></div></div>
      </div>
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex items-center justify-between"><div><p class="text-sm text-gray-500">{{ appStore.t('finance.kpi.unpaid') }}</p><p class="text-2xl font-semibold text-orange-600 mt-1">¥{{ (totalUnpaid / 10000).toFixed(1) }}万</p></div><div class="w-12 h-12 bg-orange-100 rounded-lg flex items-center justify-center"><Clock class="w-6 h-6 text-orange-600" /></div></div>
      </div>
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex items-center justify-between"><div><p class="text-sm text-gray-500">{{ appStore.t('finance.kpi.paid') }}</p><p class="text-2xl font-semibold text-green-600 mt-1">¥{{ (totalPaid / 10000).toFixed(1) }}万</p></div><div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center"><CheckCircle class="w-6 h-6 text-green-600" /></div></div>
      </div>
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex items-center justify-between"><div><p class="text-sm text-gray-500">{{ appStore.t('finance.kpi.invoiceCount') }}</p><p class="text-2xl font-semibold text-gray-900 mt-1">{{ invoices.length }}</p></div><div class="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center"><DollarSign class="w-6 h-6 text-purple-600" /></div></div>
      </div>
    </div>

    <!-- 选项卡 -->
    <div class="bg-white rounded-lg border border-gray-200">
      <div class="flex border-b border-gray-200">
        <button v-for="tab in [{ id: 'reconciliation', label: '对账单' }, { id: 'invoices', label: '发票管理' }, { id: 'payments', label: '付款记录' }]" :key="tab.id" class="px-6 py-3 text-sm font-medium transition-colors" :class="activeTab === tab.id ? 'border-b-2 border-blue-600 text-blue-600' : 'text-gray-600 hover:text-gray-900'" @click="activeTab = tab.id">{{ tab.label }}</button>
      </div>

      <div class="p-6">
        <!-- 对账单 -->
        <div v-if="activeTab === 'reconciliation'" class="space-y-4">
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
          <div v-if="viewMode === 'grid'" v-for="recon in reconciliations" :key="recon.id" class="border border-gray-200 rounded-lg p-6">
            <div class="flex items-start justify-between mb-4">
              <div class="flex-1">
                <div class="flex items-center gap-3 mb-2">
                  <h3 class="font-semibold text-gray-900">{{ recon.supplier }}</h3>
                  <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="recon.status === '已完成' ? 'bg-green-100 text-green-800' : recon.status === '已推送' ? 'bg-blue-100 text-blue-800' : 'bg-gray-100 text-gray-700'">{{ recon.status }}</span>
                </div>
                <p class="text-sm text-gray-500">对账期间: {{ recon.period }} · 对账单号: {{ recon.id }}</p>
              </div>
            </div>
            <div class="grid grid-cols-2 md:grid-cols-5 gap-4 mb-4">
              <div class="p-3 bg-gray-50 rounded-lg"><p class="text-xs text-gray-600">订单数量</p><p class="text-lg font-semibold text-gray-900 mt-1">{{ recon.orderCount }}单</p></div>
              <div class="p-3 bg-blue-50 rounded-lg"><p class="text-xs text-gray-600">对账总额</p><p class="text-lg font-semibold text-blue-600 mt-1">¥{{ (recon.totalAmount / 10000).toFixed(1) }}万</p></div>
              <div class="p-3 bg-green-50 rounded-lg"><p class="text-xs text-gray-600">已付金额</p><p class="text-lg font-semibold text-green-600 mt-1">¥{{ (recon.paidAmount / 10000).toFixed(1) }}万</p></div>
              <div class="p-3 bg-orange-50 rounded-lg"><p class="text-xs text-gray-600">待付金额</p><p class="text-lg font-semibold text-orange-600 mt-1">¥{{ (recon.unpaidAmount / 10000).toFixed(1) }}万</p></div>
              <div class="p-3 bg-purple-50 rounded-lg"><p class="text-xs text-gray-600">付款进度</p><p class="text-lg font-semibold text-purple-600 mt-1">{{ ((recon.paidAmount / recon.totalAmount) * 100).toFixed(0) }}%</p></div>
            </div>
            <div class="flex gap-2">
              <button @click="handleViewReconDetail(recon)" class="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50 flex items-center justify-center gap-1">
                <Eye class="w-4 h-4" />{{ appStore.t('btn.viewStatementDetail') }}
              </button>
              <button @click="handleDownloadRecon(recon)" class="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50 flex items-center justify-center gap-1">
                <Download class="w-4 h-4" />{{ appStore.t('btn.downloadStatement') }}
              </button>
              <button v-if="recon.status === '待确认'" @click="handlePushToSupplier(recon)" class="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">{{ appStore.t('btn.pushSupplierConfirm') }}</button>
              <button v-if="recon.unpaidAmount > 0" @click="handleInitiatePayment(recon)" class="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">{{ appStore.t('btn.initiatePayment') }}</button>
            </div>
          </div>
          <div v-else class="border border-gray-200 rounded-lg overflow-x-auto">
            <table class="w-full min-w-max text-sm">
              <thead class="bg-gray-50">
                <tr>
                  <th class="text-left px-4 py-3 font-medium text-gray-700">{{ appStore.t('finance.table.id') }}</th>
                  <th class="text-left px-4 py-3 font-medium text-gray-700">{{ appStore.t('finance.table.supplier') }}</th>
                  <th class="text-left px-4 py-3 font-medium text-gray-700">{{ appStore.t('finance.table.period') }}</th>
                  <th class="text-right px-4 py-3 font-medium text-gray-700">{{ appStore.t('finance.table.orderCount') }}</th>
                  <th class="text-right px-4 py-3 font-medium text-gray-700">{{ appStore.t('finance.table.totalAmount') }}</th>
                  <th class="text-right px-4 py-3 font-medium text-gray-700">{{ appStore.t('finance.table.paidAmount') }}</th>
                  <th class="text-right px-4 py-3 font-medium text-gray-700">{{ appStore.t('finance.table.unpaidAmount') }}</th>
                  <th class="text-left px-4 py-3 font-medium text-gray-700">{{ appStore.t('finance.table.status') }}</th>
                  <th class="text-left px-4 py-3 font-medium text-gray-700">{{ appStore.t('common.action') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="recon in reconciliations" :key="recon.id" class="border-t border-gray-200">
                  <td class="px-4 py-3 text-gray-900">{{ recon.id }}</td>
                  <td class="px-4 py-3 text-gray-900 font-medium">{{ recon.supplier }}</td>
                  <td class="px-4 py-3 text-gray-900">{{ recon.period }}</td>
                  <td class="px-4 py-3 text-right text-gray-900">{{ recon.orderCount }}</td>
                  <td class="px-4 py-3 text-right text-gray-900">¥{{ recon.totalAmount.toLocaleString() }}</td>
                  <td class="px-4 py-3 text-right text-green-600">¥{{ recon.paidAmount.toLocaleString() }}</td>
                  <td class="px-4 py-3 text-right text-orange-600">¥{{ recon.unpaidAmount.toLocaleString() }}</td>
                  <td class="px-4 py-3">
                    <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="recon.status === '已完成' ? 'bg-green-100 text-green-800' : recon.status === '已推送' ? 'bg-blue-100 text-blue-800' : 'bg-gray-100 text-gray-700'">{{ recon.status }}</span>
                  </td>
                  <td class="px-4 py-3">
                    <div class="flex gap-2 flex-wrap">
                      <button @click="handleViewReconDetail(recon)" class="px-3 py-1.5 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">查看明细</button>
                      <button @click="handleDownloadRecon(recon)" class="px-3 py-1.5 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">下载</button>
                      <button v-if="recon.status === '待确认'" @click="handlePushToSupplier(recon)" class="px-3 py-1.5 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">推送确认</button>
                      <button v-if="recon.unpaidAmount > 0" @click="handleInitiatePayment(recon)" class="px-3 py-1.5 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">发起付款</button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- 发票管理 -->
        <div v-if="activeTab === 'invoices'" class="space-y-4">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-base font-semibold text-gray-900">发票管理</h3>
            <button @click="isInvoiceUploadOpen = true" class="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">
              <Upload class="w-4 h-4" />上传发票
            </button>
          </div>
          <div class="p-4 bg-blue-50 rounded-lg">
            <div class="flex items-center gap-2 mb-2">
              <DollarSign class="w-5 h-5 text-blue-600" />
              <h4 class="font-semibold text-blue-900">电子发票管理</h4>
            </div>
            <p class="text-sm text-blue-800">支持发票影像上传、OCR自动识别、三单匹配（订单、入库单、发票），确保财务合规性。</p>
          </div>
          <div v-for="invoice in invoices" :key="invoice.id" class="border border-gray-200 rounded-lg p-4">
            <div class="flex items-start justify-between mb-4">
              <div>
                <div class="flex items-center gap-2 mb-2">
                  <h4 class="font-semibold text-gray-900">发票号码: {{ invoice.invoiceNo }}</h4>
                  <span class="px-2 py-0.5 bg-blue-100 text-blue-800 rounded-full text-xs font-medium">{{ invoice.status }}</span>
                </div>
                <p class="text-sm text-gray-600">{{ invoice.supplier }}</p>
                <p class="text-xs text-gray-500 mt-1">开票日期: {{ invoice.invoiceDate }}</p>
              </div>
            </div>
            <div class="grid grid-cols-3 gap-4 mb-4">
              <div class="p-3 bg-gray-50 rounded-lg"><p class="text-xs text-gray-600">不含税金额</p><p class="text-lg font-semibold text-gray-900 mt-1">¥{{ invoice.amount.toLocaleString() }}</p></div>
              <div class="p-3 bg-gray-50 rounded-lg"><p class="text-xs text-gray-600">税额</p><p class="text-lg font-semibold text-gray-900 mt-1">¥{{ invoice.taxAmount.toLocaleString() }}</p></div>
              <div class="p-3 bg-blue-50 rounded-lg"><p class="text-xs text-gray-600">价税合计</p><p class="text-lg font-semibold text-blue-600 mt-1">¥{{ invoice.totalAmount.toLocaleString() }}</p></div>
            </div>
            <div class="flex gap-2">
              <button @click="handleViewInvoiceImage(invoice)" class="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50 flex items-center justify-center gap-1">
                <Eye class="w-4 h-4" />查看发票影像
              </button>
              <button @click="handleDownloadInvoice(invoice)" class="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50 flex items-center justify-center gap-1">
                <Download class="w-4 h-4" />下载PDF
              </button>
              <button @click="handleInitiatePayment({ id: invoice.id, supplier: invoice.supplier, unpaidAmount: invoice.totalAmount })" class="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">发起付款</button>
            </div>
          </div>
        </div>

        <!-- 付款记录 -->
        <div v-if="activeTab === 'payments'">
          <h3 class="text-base font-semibold text-gray-900 mb-4">付款记录</h3>
          <div class="border border-gray-200 rounded-lg overflow-hidden">
            <table class="w-full">
              <thead class="bg-gray-50">
                <tr>
                  <th class="text-left px-4 py-3 text-sm font-medium text-gray-700">付款编号</th>
                  <th class="text-left px-4 py-3 text-sm font-medium text-gray-700">供应商</th>
                  <th class="text-right px-4 py-3 text-sm font-medium text-gray-700">付款金额</th>
                  <th class="text-left px-4 py-3 text-sm font-medium text-gray-700">付款日期</th>
                  <th class="text-left px-4 py-3 text-sm font-medium text-gray-700">付款方式</th>
                  <th class="text-left px-4 py-3 text-sm font-medium text-gray-700">状态</th>
                  <th class="text-left px-4 py-3 text-sm font-medium text-gray-700">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="payment in payments" :key="payment.id" class="border-t border-gray-200">
                  <td class="px-4 py-3 text-sm text-gray-900">{{ payment.id }}</td>
                  <td class="px-4 py-3 text-sm text-gray-900">{{ payment.supplier }}</td>
                  <td class="px-4 py-3 text-sm text-right font-medium text-gray-900">¥{{ payment.amount.toLocaleString() }}</td>
                  <td class="px-4 py-3 text-sm text-gray-900">{{ payment.paymentDate }}</td>
                  <td class="px-4 py-3 text-sm text-gray-900">{{ payment.method }}</td>
                  <td class="px-4 py-3 text-sm"><span class="px-2 py-0.5 bg-green-100 text-green-800 rounded-full text-xs font-medium">{{ payment.status }}</span></td>
                  <td class="px-4 py-3 text-sm"><button @click="handleViewPaymentDetail(payment)" class="px-3 py-1.5 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">查看详情</button></td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="mt-4 p-4 bg-green-50 rounded-lg">
            <div class="flex items-center gap-2">
              <CheckCircle class="w-5 h-5 text-green-600" />
              <p class="text-sm text-green-900"><span class="font-medium">财务闭环：</span>系统与用友U8财务模块深度集成，付款申请自动同步至ERP，确保财务数据一致性。</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 对账单详情对话框 -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="isReconDetailOpen && selectedReconciliation" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-black/50" @click="isReconDetailOpen = false" />
          <div class="relative bg-white rounded-xl shadow-xl w-full max-w-2xl max-h-[80vh] overflow-hidden">
            <div class="flex items-center justify-between p-4 border-b">
              <h3 class="text-lg font-semibold text-gray-900">对账单详情</h3>
              <button @click="isReconDetailOpen = false" class="p-2 hover:bg-gray-100 rounded-lg">
                <X class="w-5 h-5" />
              </button>
            </div>
            <div class="p-6 overflow-y-auto max-h-[60vh]">
              <div class="space-y-4">
                <div class="grid grid-cols-2 md:grid-cols-4 gap-4 p-4 bg-gray-50 rounded-lg">
                  <div><p class="text-sm text-gray-500">对账单号</p><p class="font-medium text-gray-900">{{ selectedReconciliation.id }}</p></div>
                  <div><p class="text-sm text-gray-500">供应商</p><p class="font-medium text-gray-900">{{ selectedReconciliation.supplier }}</p></div>
                  <div><p class="text-sm text-gray-500">对账期间</p><p class="font-medium text-gray-900">{{ selectedReconciliation.period }}</p></div>
                  <div><p class="text-sm text-gray-500">状态</p><span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="selectedReconciliation.status === '已完成' ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-700'">{{ selectedReconciliation.status }}</span></div>
                </div>
                <div class="grid grid-cols-2 md:grid-cols-4 gap-4 p-4 bg-blue-50 rounded-lg">
                  <div><p class="text-sm text-gray-600">订单数量</p><p class="text-lg font-semibold text-blue-900">{{ selectedReconciliation.orderCount }}</p></div>
                  <div><p class="text-sm text-gray-600">对账总额</p><p class="text-lg font-semibold text-blue-900">¥{{ selectedReconciliation.totalAmount.toLocaleString() }}</p></div>
                  <div><p class="text-sm text-gray-600">已付金额</p><p class="text-lg font-semibold text-green-900">¥{{ selectedReconciliation.paidAmount.toLocaleString() }}</p></div>
                  <div><p class="text-sm text-gray-600">待付金额</p><p class="text-lg font-semibold text-orange-900">¥{{ selectedReconciliation.unpaidAmount.toLocaleString() }}</p></div>
                </div>
              </div>
            </div>
            <div class="flex justify-end gap-3 p-4 border-t bg-gray-50">
              <button @click="isReconDetailOpen = false" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">关闭</button>
              <button @click="handleDownloadRecon(selectedReconciliation)" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50 flex items-center gap-2">
                <Download class="w-4 h-4" />下载PDF
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- 发票上传对话框 -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="isInvoiceUploadOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-black/50" @click="isInvoiceUploadOpen = false" />
          <div class="relative bg-white rounded-xl shadow-xl w-full max-w-lg">
            <div class="flex items-center justify-between p-4 border-b">
              <h3 class="text-lg font-semibold text-gray-900">上传发票</h3>
              <button @click="isInvoiceUploadOpen = false" class="p-2 hover:bg-gray-100 rounded-lg">
                <X class="w-5 h-5" />
              </button>
            </div>
            <div class="p-6 space-y-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">供应商</label>
                <select v-model="newInvoice.supplierId" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
                  <option value="">请选择供应商</option>
                  <option v-for="s in suppliers" :key="s.id" :value="s.id">{{ s.name }}</option>
                </select>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">发票号码</label>
                <input v-model="newInvoice.invoiceNo" type="text" placeholder="请输入发票号码" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">不含税金额</label>
                  <input v-model.number="newInvoice.amount" type="number" placeholder="不含税金额" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">税额</label>
                  <input v-model.number="newInvoice.taxAmount" type="number" placeholder="税额" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">开票日期</label>
                <input v-model="newInvoice.invoiceDate" type="date" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">发票文件</label>
                <div class="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center hover:border-blue-400 transition-colors cursor-pointer">
                  <Upload class="w-8 h-8 text-gray-400 mx-auto mb-2" />
                  <p class="text-sm text-gray-600">点击或拖拽文件到此处上传</p>
                  <p class="text-xs text-gray-500 mt-1">支持 PDF、JPG、PNG 格式，不超过 5MB</p>
                </div>
              </div>
            </div>
            <div class="flex justify-end gap-3 p-4 border-t bg-gray-50">
              <button @click="isInvoiceUploadOpen = false" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">取消</button>
              <button @click="handleUploadInvoice" class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">上传</button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- 付款确认对话框 -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="isPaymentDialogOpen && selectedPaymentRecon" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-black/50" @click="isPaymentDialogOpen = false" />
          <div class="relative bg-white rounded-xl shadow-xl w-full max-w-md">
            <div class="flex items-center justify-between p-4 border-b">
              <h3 class="text-lg font-semibold text-gray-900">发起付款</h3>
              <button @click="isPaymentDialogOpen = false" class="p-2 hover:bg-gray-100 rounded-lg">
                <X class="w-5 h-5" />
              </button>
            </div>
            <div class="p-6 space-y-4">
              <div class="p-4 bg-blue-50 rounded-lg">
                <div class="flex items-center gap-2 mb-3">
                  <AlertCircle class="w-5 h-5 text-blue-600" />
                  <h4 class="font-medium text-blue-900">付款确认</h4>
                </div>
                <div class="space-y-2 text-sm">
                  <p class="text-blue-800">供应商: <span class="font-medium">{{ selectedPaymentRecon.supplier }}</span></p>
                  <p class="text-blue-800">付款金额: <span class="font-medium">¥{{ selectedPaymentRecon.unpaidAmount.toLocaleString() }}</span></p>
                  <p class="text-blue-800">付款方式: <span class="font-medium">银行转账</span></p>
                </div>
              </div>
            </div>
            <div class="flex justify-end gap-3 p-4 border-t bg-gray-50">
              <button @click="isPaymentDialogOpen = false" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">取消</button>
              <button @click="handleConfirmPayment" class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">确认付款</button>
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
