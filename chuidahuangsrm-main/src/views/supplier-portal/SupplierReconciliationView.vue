<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Calculator, Download, CheckCircle, X, Eye, FileText } from 'lucide-vue-next'
import { useAppStore } from '@/stores/useAppStore'

const appStore = useAppStore()
const supplierId = 'S001'

// Mock 对账单数据 - 扩展到10条
const mockReconciliations = [
  { id: 'RC001', period: '2026年3月', orderCount: 5, totalAmount: 857000, paidAmount: 0, status: '待确认', supplierId },
  { id: 'RC002', period: '2026年2月', orderCount: 12, totalAmount: 1256000, paidAmount: 1256000, status: '已结清', supplierId },
  { id: 'RC003', period: '2026年1月', orderCount: 8, totalAmount: 980000, paidAmount: 980000, status: '已结清', supplierId },
  { id: 'RC004', period: '2025年12月', orderCount: 15, totalAmount: 1580000, paidAmount: 1580000, status: '已结清', supplierId },
  { id: 'RC005', period: '2025年11月', orderCount: 10, totalAmount: 1120000, paidAmount: 800000, status: '部分支付', supplierId },
  { id: 'RC006', period: '2025年10月', orderCount: 9, totalAmount: 965000, paidAmount: 965000, status: '已结清', supplierId },
  { id: 'RC007', period: '2025年9月', orderCount: 11, totalAmount: 1340000, paidAmount: 1340000, status: '已结清', supplierId },
  { id: 'RC008', period: '2025年8月', orderCount: 7, totalAmount: 785000, paidAmount: 785000, status: '已结清', supplierId },
  { id: 'RC009', period: '2025年7月', orderCount: 13, totalAmount: 1420000, paidAmount: 1420000, status: '已结清', supplierId },
  { id: 'RC010', period: '2025年6月', orderCount: 8, totalAmount: 890000, paidAmount: 890000, status: '已结清', supplierId },
]

const reconciliations = ref<any[]>([])
const selectedRecon = ref<any>(null)
const isDetailDialogOpen = ref(false)

onMounted(() => {
  // 加载对账单数据
  const saved = localStorage.getItem('supplier_reconciliations')
  if (saved) {
    try {
      const allRecon = JSON.parse(saved)
      reconciliations.value = allRecon.filter((r: any) => r.supplierId === supplierId)
    } catch {
      reconciliations.value = mockReconciliations
    }
  } else {
    reconciliations.value = mockReconciliations
  }
})

function getStatusClass(status: string) {
  switch (status) {
    case '已结清': return 'bg-green-100 text-green-800'
    case '待确认': return 'bg-orange-100 text-orange-800'
    case '部分支付': return 'bg-blue-100 text-blue-800'
    default: return 'bg-gray-100 text-gray-700'
  }
}

// 查看明细
function handleViewDetail(recon: any) {
  selectedRecon.value = recon
  isDetailDialogOpen.value = true
}

// 下载PDF
function handleDownloadPDF(recon: any) {
  appStore.showToast(`正在下载 ${recon.period} 对账单PDF...`, 'info')
  // 模拟下载
  setTimeout(() => {
    appStore.showToast('PDF下载完成', 'success')
  }, 1500)
}

// 确认对账
function handleConfirmReconciliation(reconId: string) {
  const idx = reconciliations.value.findIndex(r => r.id === reconId)
  if (idx > -1) {
    reconciliations.value[idx].status = '已结清'

    // 更新 localStorage
    const saved = localStorage.getItem('supplier_reconciliations')
    let allRecon = saved ? JSON.parse(saved) : []
    const globalIdx = allRecon.findIndex((r: any) => r.id === reconId)
    if (globalIdx > -1) {
      allRecon[globalIdx].status = '已结清'
      localStorage.setItem('supplier_reconciliations', JSON.stringify(allRecon))
    }

    appStore.showToast('对账单已确认', 'success')
  }
}

// 模拟的订单明细数据 - 扩展
const mockOrderDetails = [
  { orderNo: 'PO2026031901', materialName: '电机外壳', quantity: 500, unit: '个', amount: 125000 },
  { orderNo: 'PO2026031902', materialName: '端盖组件', quantity: 1000, unit: '件', amount: 160000 },
  { orderNo: 'PO2026031501', materialName: '散热片', quantity: 2000, unit: '片', amount: 80000 },
  { orderNo: 'PO2026031001', materialName: '底座组件', quantity: 500, unit: '个', amount: 215000 },
  { orderNo: 'PO2026030501', materialName: '控制面板外壳', quantity: 700, unit: '个', amount: 277000 },
  { orderNo: 'PO2026030201', materialName: '散热器支架', quantity: 1200, unit: '个', amount: 95000 },
  { orderNo: 'PO2026022801', materialName: '外壳总成', quantity: 400, unit: '套', amount: 380000 },
  { orderNo: 'PO2026022501', materialName: '电机外壳A型', quantity: 600, unit: '个', amount: 150000 },
  { orderNo: 'PO2026022001', materialName: '端盖组件B型', quantity: 1500, unit: '件', amount: 240000 },
  { orderNo: 'PO2026021501', materialName: '连接件', quantity: 3000, unit: '个', amount: 135000 },
]
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-semibold text-gray-900">财务对账</h1>
        <p class="text-sm text-gray-500 mt-1">查看和确认对账单</p>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">对账单总数</p>
            <p class="text-2xl font-semibold text-gray-900 mt-1">{{ reconciliations.length }}</p>
          </div>
          <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
            <Calculator class="w-6 h-6 text-blue-600" />
          </div>
        </div>
      </div>
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">待确认</p>
            <p class="text-2xl font-semibold text-orange-600 mt-1">{{ reconciliations.filter(r => r.status === '待确认').length }}</p>
          </div>
          <div class="w-12 h-12 bg-orange-100 rounded-lg flex items-center justify-center">
            <Eye class="w-6 h-6 text-orange-600" />
          </div>
        </div>
      </div>
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">已结清</p>
            <p class="text-2xl font-semibold text-green-600 mt-1">{{ reconciliations.filter(r => r.status === '已结清').length }}</p>
          </div>
          <div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
            <CheckCircle class="w-6 h-6 text-green-600" />
          </div>
        </div>
      </div>
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">待付金额</p>
            <p class="text-2xl font-semibold text-purple-600 mt-1">¥{{ (reconciliations.reduce((sum, r) => sum + (r.totalAmount - r.paidAmount), 0) / 10000).toFixed(1) }}万</p>
          </div>
          <div class="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center">
            <FileText class="w-6 h-6 text-purple-600" />
          </div>
        </div>
      </div>
    </div>

    <div class="space-y-4">
      <div v-for="recon in reconciliations" :key="recon.id" class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex items-start justify-between mb-4">
          <div>
            <div class="flex items-center gap-3 mb-2">
              <h3 class="font-semibold text-gray-900">{{ recon.period }}对账单</h3>
              <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="getStatusClass(recon.status)">{{ recon.status }}</span>
            </div>
            <p class="text-sm text-gray-500">对账单号: {{ recon.id }}</p>
          </div>
          <button @click="handleDownloadPDF(recon)" class="flex items-center gap-2 px-3 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">
            <Download class="w-4 h-4" />下载PDF
          </button>
        </div>

        <div class="grid grid-cols-2 md:grid-cols-4 gap-4 p-4 bg-gray-50 rounded-lg">
          <div><p class="text-sm text-gray-500">订单数量</p><p class="text-lg font-semibold text-gray-900 mt-1">{{ recon.orderCount }}</p></div>
          <div><p class="text-sm text-gray-500">对账总额</p><p class="text-lg font-semibold text-gray-900 mt-1">¥{{ recon.totalAmount.toLocaleString() }}</p></div>
          <div><p class="text-sm text-gray-500">已付金额</p><p class="text-lg font-semibold text-green-600 mt-1">¥{{ recon.paidAmount.toLocaleString() }}</p></div>
          <div><p class="text-sm text-gray-500">待付金额</p><p class="text-lg font-semibold text-orange-600 mt-1">¥{{ (recon.totalAmount - recon.paidAmount).toLocaleString() }}</p></div>
        </div>

        <div class="flex gap-2 mt-4">
          <button @click="handleViewDetail(recon)" class="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50 flex items-center justify-center gap-2">
            <Eye class="w-4 h-4" />查看明细
          </button>
          <button v-if="recon.status === '待确认'" @click="handleConfirmReconciliation(recon.id)" class="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700 flex items-center justify-center gap-2">
            <CheckCircle class="w-4 h-4" />确认对账
          </button>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="reconciliations.length === 0" class="bg-white rounded-lg border border-gray-200 py-12">
        <div class="text-center text-gray-500">
          <Calculator class="w-12 h-12 mx-auto mb-4 opacity-50" />
          <p>暂无对账单</p>
        </div>
      </div>
    </div>

    <!-- 对账单明细对话框 -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="isDetailDialogOpen && selectedRecon" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-black/50" @click="isDetailDialogOpen = false" />
          <div class="relative bg-white rounded-xl shadow-xl w-full max-w-3xl max-h-[80vh] overflow-hidden">
            <div class="flex items-center justify-between p-4 border-b">
              <h3 class="text-lg font-semibold text-gray-900">对账单明细</h3>
              <button @click="isDetailDialogOpen = false" class="p-2 hover:bg-gray-100 rounded-lg">
                <X class="w-5 h-5" />
              </button>
            </div>
            <div class="p-6 overflow-y-auto max-h-[60vh]">
              <div class="space-y-6">
                <!-- 对账单基本信息 -->
                <div class="grid grid-cols-2 md:grid-cols-4 gap-4 p-4 bg-gray-50 rounded-lg">
                  <div>
                    <p class="text-sm text-gray-500">对账单号</p>
                    <p class="font-medium text-gray-900">{{ selectedRecon.id }}</p>
                  </div>
                  <div>
                    <p class="text-sm text-gray-500">对账期间</p>
                    <p class="font-medium text-gray-900">{{ selectedRecon.period }}</p>
                  </div>
                  <div>
                    <p class="text-sm text-gray-500">状态</p>
                    <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="getStatusClass(selectedRecon.status)">{{ selectedRecon.status }}</span>
                  </div>
                  <div>
                    <p class="text-sm text-gray-500">订单数量</p>
                    <p class="font-medium text-gray-900">{{ selectedRecon.orderCount }}笔</p>
                  </div>
                </div>

                <!-- 订单明细列表 -->
                <div>
                  <div class="flex items-center gap-2 mb-3">
                    <FileText class="w-5 h-5 text-gray-600" />
                    <h4 class="font-medium text-gray-900">订单明细</h4>
                  </div>
                  <div class="border border-gray-200 rounded-lg overflow-hidden">
                    <table class="w-full text-sm">
                      <thead class="bg-gray-50">
                        <tr>
                          <th class="text-left px-4 py-2 font-medium text-gray-700">订单号</th>
                          <th class="text-left px-4 py-2 font-medium text-gray-700">物料名称</th>
                          <th class="text-right px-4 py-2 font-medium text-gray-700">数量</th>
                          <th class="text-left px-4 py-2 font-medium text-gray-700">单位</th>
                          <th class="text-right px-4 py-2 font-medium text-gray-700">金额</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="(item, idx) in mockOrderDetails.slice(0, selectedRecon.orderCount)" :key="idx" class="border-t border-gray-200">
                          <td class="px-4 py-3 text-gray-900">{{ item.orderNo }}</td>
                          <td class="px-4 py-3 text-gray-900">{{ item.materialName }}</td>
                          <td class="px-4 py-3 text-right text-gray-900">{{ item.quantity }}</td>
                          <td class="px-4 py-3 text-gray-900">{{ item.unit }}</td>
                          <td class="px-4 py-3 text-right font-medium text-gray-900">¥{{ item.amount.toLocaleString() }}</td>
                        </tr>
                      </tbody>
                      <tfoot class="bg-gray-50 border-t border-gray-200">
                        <tr>
                          <td colspan="4" class="px-4 py-3 text-right font-medium text-gray-700">合计</td>
                          <td class="px-4 py-3 text-right font-bold text-gray-900">¥{{ selectedRecon.totalAmount.toLocaleString() }}</td>
                        </tr>
                      </tfoot>
                    </table>
                  </div>
                </div>

                <!-- 汇总信息 -->
                <div class="grid grid-cols-2 gap-4 p-4 bg-blue-50 rounded-lg">
                  <div>
                    <p class="text-sm text-blue-700">对账总额</p>
                    <p class="text-xl font-bold text-blue-900">¥{{ selectedRecon.totalAmount.toLocaleString() }}</p>
                  </div>
                  <div>
                    <p class="text-sm text-blue-700">待付金额</p>
                    <p class="text-xl font-bold text-orange-600">¥{{ (selectedRecon.totalAmount - selectedRecon.paidAmount).toLocaleString() }}</p>
                  </div>
                </div>
              </div>
            </div>
            <div class="flex justify-end gap-3 p-4 border-t bg-gray-50">
              <button @click="isDetailDialogOpen = false" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">关闭</button>
              <button @click="handleDownloadPDF(selectedRecon)" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50 flex items-center gap-2">
                <Download class="w-4 h-4" />下载PDF
              </button>
              <button v-if="selectedRecon.status === '待确认'" @click="handleConfirmReconciliation(selectedRecon.id); isDetailDialogOpen = false" class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700 flex items-center gap-2">
                <CheckCircle class="w-4 h-4" />确认对账
              </button>
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
