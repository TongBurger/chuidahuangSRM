<script setup lang="ts">
import { computed, ref } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { ArrowLeft, Download, FileText, Wrench, Package, QrCode, X, Plus } from 'lucide-vue-next'
import { mockMolds } from '@/data/mockData'
import { useAppStore } from '@/stores/useAppStore'

const route = useRoute()
const appStore = useAppStore()
const mold = computed(() => mockMolds.find((m) => m.id === route.params.id))

// 维保任务对话框
const isMaintenanceDialogOpen = ref(false)
const maintenanceForm = ref({
  type: '定期保养',
  scheduledDate: '',
  notes: '',
})

function getLifeColor(life: number) {
  if (life < 10) return 'text-red-600'
  if (life < 20) return 'text-orange-600'
  return 'text-green-600'
}

const maintenanceHistory = [
  { date: '2026-01-15', type: '定期保养', result: '正常' },
  { date: '2025-10-10', type: '定期保养', result: '正常' },
  { date: '2025-07-05', type: '故障维修', result: '已修复' },
]

// 打印二维码
function handlePrintQR() {
  if (!mold.value) return
  appStore.showToast(`正在生成 ${mold.value.code} 的二维码...`, 'info')
  setTimeout(() => {
    appStore.showToast('二维码生成成功，准备打印', 'success')
  }, 1000)
}

// 导出报告
function handleExportReport() {
  if (!mold.value) return
  appStore.showToast(`正在导出 ${mold.value.code} 的报告...`, 'info')
  setTimeout(() => {
    appStore.showToast('报告导出成功', 'success')
  }, 1000)
}

// 打开维保任务对话框
function handleCreateMaintenance() {
  maintenanceForm.value = {
    type: '定期保养',
    scheduledDate: mold.value?.nextMaintenanceDate || '',
    notes: '',
  }
  isMaintenanceDialogOpen.value = true
}

// 确认创建维保任务
function handleConfirmMaintenance() {
  if (!maintenanceForm.value.scheduledDate) {
    appStore.showToast('请选择计划日期', 'warning')
    return
  }
  appStore.showToast(`已创建 ${mold.value?.code} 的维保任务`, 'success')
  isMaintenanceDialogOpen.value = false
}
</script>

<template>
  <div v-if="!mold" class="text-center py-12">
    <p class="text-gray-500">模具不存在</p>
    <RouterLink to="/molds"><button class="mt-4 px-4 py-2 border border-gray-300 rounded-lg text-sm">返回列表</button></RouterLink>
  </div>

  <div v-else class="space-y-6">
    <!-- 标题 -->
    <div class="flex items-center gap-4 flex-wrap">
      <RouterLink to="/molds">
        <button class="flex items-center gap-2 px-3 py-1.5 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">
          <ArrowLeft class="w-4 h-4" />返回
        </button>
      </RouterLink>
      <div class="flex-1">
        <h1 class="text-2xl font-semibold text-gray-900">{{ mold.code }}</h1>
        <p class="text-sm text-gray-500 mt-1">{{ mold.name }}</p>
      </div>
      <button @click="handlePrintQR" class="flex items-center gap-2 px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">
        <QrCode class="w-4 h-4" />打印二维码
      </button>
      <button @click="handleExportReport" class="flex items-center gap-2 px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">
        <Download class="w-4 h-4" />导出报告
      </button>
    </div>

    <!-- 信息卡片 -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <h3 class="text-base font-semibold text-gray-900 mb-4">基本信息</h3>
        <div class="space-y-3">
          <div><p class="text-sm text-gray-500">模具编码</p><p class="font-medium text-gray-900 mt-1">{{ mold.code }}</p></div>
          <div><p class="text-sm text-gray-500">模具名称</p><p class="font-medium text-gray-900 mt-1">{{ mold.name }}</p></div>
          <div><p class="text-sm text-gray-500">模具分类</p><p class="font-medium text-gray-900 mt-1">{{ mold.category }}</p></div>
          <div><p class="text-sm text-gray-500">状态</p><span class="inline-block mt-1 px-2 py-0.5 bg-blue-100 text-blue-800 rounded-full text-xs font-medium">{{ mold.status }}</span></div>
          <div><p class="text-sm text-gray-500">存放位置</p><p class="font-medium text-gray-900 mt-1">{{ mold.location }}</p></div>
          <div><p class="text-sm text-gray-500">使用供应商</p><p class="font-medium text-gray-900 mt-1">{{ mold.supplierName }}</p></div>
          <div><p class="text-sm text-gray-500">模具原值</p><p class="text-xl font-semibold text-gray-900 mt-1">¥{{ mold.originalValue.toLocaleString() }}</p></div>
        </div>
      </div>

      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <h3 class="text-base font-semibold text-gray-900 mb-4">寿命追踪</h3>
        <div class="text-center py-4">
          <div class="text-5xl font-bold mb-2" :class="getLifeColor(mold.remainingLife)">{{ mold.remainingLife }}%</div>
          <p class="text-sm text-gray-500">剩余寿命</p>
        </div>
        <div class="space-y-3">
          <div>
            <p class="text-sm text-gray-500 mb-2">寿命进度</p>
            <div class="h-3 bg-gray-200 rounded-full overflow-hidden">
              <div class="h-full bg-blue-600 transition-all" :style="{ width: `${100 - mold.remainingLife}%` }" />
            </div>
          </div>
          <div class="p-3 bg-gray-50 rounded-lg space-y-2">
            <div class="flex justify-between text-sm">
              <span class="text-gray-500">设计寿命</span>
              <span class="font-medium text-gray-900">{{ mold.designLife.toLocaleString() }} 模次</span>
            </div>
            <div class="flex justify-between text-sm">
              <span class="text-gray-500">已使用</span>
              <span class="font-medium text-gray-900">{{ mold.currentUsage.toLocaleString() }} 模次</span>
            </div>
            <div class="flex justify-between text-sm">
              <span class="text-gray-500">剩余可用</span>
              <span class="font-medium" :class="getLifeColor(mold.remainingLife)">{{ (mold.designLife - mold.currentUsage).toLocaleString() }} 模次</span>
            </div>
          </div>
          <div v-if="mold.remainingLife < 15" class="p-3 bg-red-50 rounded-lg">
            <p class="text-sm text-red-800 font-medium">⚠️ 寿命预警</p>
            <p class="text-xs text-red-700 mt-1">剩余寿命不足15%，建议尽快安排维保或更换</p>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <h3 class="text-base font-semibold text-gray-900 mb-4">维保记录</h3>
        <div class="space-y-4">
          <div><p class="text-sm text-gray-500">上次维保日期</p><p class="font-medium text-gray-900 mt-1">{{ mold.lastMaintenanceDate }}</p></div>
          <div><p class="text-sm text-gray-500">下次维保日期</p><p class="font-medium text-gray-900 mt-1">{{ mold.nextMaintenanceDate }}</p></div>
          <div class="pt-4 border-t">
            <button @click="handleCreateMaintenance" class="w-full flex items-center justify-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">
              <Wrench class="w-4 h-4" />创建维保任务
            </button>
          </div>
          <div class="space-y-2 pt-4">
            <p class="text-sm font-medium text-gray-900">历史维保记录</p>
            <div v-for="(record, idx) in maintenanceHistory" :key="idx" class="p-3 bg-gray-50 rounded-lg">
              <div class="flex items-center justify-between mb-1">
                <span class="text-sm font-medium text-gray-900">{{ record.type }}</span>
                <span class="px-2 py-0.5 border border-gray-300 rounded-full text-xs text-gray-700">{{ record.result }}</span>
              </div>
              <p class="text-xs text-gray-500">{{ record.date }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 技术文档 -->
    <div class="bg-white rounded-lg border border-gray-200 p-6">
      <h3 class="text-base font-semibold text-gray-900 mb-4">技术文档</h3>
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div v-for="(drawing, idx) in mold.drawings" :key="idx" class="flex items-center gap-4 p-4 border border-gray-200 rounded-lg hover:bg-gray-50 cursor-pointer transition-colors">
          <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center flex-shrink-0">
            <FileText class="w-6 h-6 text-blue-600" />
          </div>
          <div class="flex-1 min-w-0">
            <p class="font-medium text-gray-900 truncate">{{ drawing }}</p>
            <p class="text-xs text-gray-500 mt-1">技术图纸</p>
          </div>
          <Download class="w-4 h-4 text-gray-400" />
        </div>
      </div>
    </div>

    <!-- 生产记录 -->
    <div class="bg-white rounded-lg border border-gray-200 p-6">
      <h3 class="text-base font-semibold text-gray-900 mb-4">生产记录</h3>
      <div class="border border-gray-200 rounded-lg overflow-hidden">
        <table class="w-full">
          <thead class="bg-gray-50">
            <tr>
              <th class="text-left px-4 py-3 text-sm font-medium text-gray-700">订单编号</th>
              <th class="text-left px-4 py-3 text-sm font-medium text-gray-700">生产日期</th>
              <th class="text-right px-4 py-3 text-sm font-medium text-gray-700">生产数量</th>
              <th class="text-right px-4 py-3 text-sm font-medium text-gray-700">累计模次</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(order, idx) in mold.productionOrders" :key="idx" class="border-t border-gray-200">
              <td class="px-4 py-3 text-sm text-gray-900">{{ order.orderId }}</td>
              <td class="px-4 py-3 text-sm text-gray-900">{{ order.productionDate }}</td>
              <td class="px-4 py-3 text-sm text-right text-gray-900">{{ order.quantity.toLocaleString() }}</td>
              <td class="px-4 py-3 text-sm text-right font-medium text-gray-900">
                {{ mold.productionOrders.slice(0, idx + 1).reduce((sum, o) => sum + o.quantity, 0).toLocaleString() }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="mt-4 p-4 bg-blue-50 rounded-lg">
        <div class="flex items-center gap-2">
          <Package class="w-5 h-5 text-blue-600" />
          <p class="text-sm text-blue-900">
            <span class="font-medium">共生产 {{ mold.productionOrders.length }} 批次</span>，累计使用 {{ mold.currentUsage.toLocaleString() }} 模次
          </p>
        </div>
      </div>
    </div>

    <!-- 维保任务对话框 -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="isMaintenanceDialogOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-black/50" @click="isMaintenanceDialogOpen = false" />
          <div class="relative bg-white rounded-xl shadow-xl w-full max-w-md">
            <div class="flex items-center justify-between p-4 border-b">
              <h3 class="text-lg font-semibold text-gray-900">创建维保任务</h3>
              <button @click="isMaintenanceDialogOpen = false" class="p-2 hover:bg-gray-100 rounded-lg">
                <X class="w-5 h-5" />
              </button>
            </div>
            <div class="p-6 space-y-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">维保类型</label>
                <select v-model="maintenanceForm.type" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
                  <option value="定期保养">定期保养</option>
                  <option value="故障维修">故障维修</option>
                  <option value="寿命到期更换">寿命到期更换</option>
                </select>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">计划日期 *</label>
                <input v-model="maintenanceForm.scheduledDate" type="date" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">备注</label>
                <textarea v-model="maintenanceForm.notes" rows="3" placeholder="请输入维保要求或备注" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"></textarea>
              </div>
            </div>
            <div class="flex justify-end gap-3 p-4 border-t bg-gray-50">
              <button @click="isMaintenanceDialogOpen = false" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">取消</button>
              <button @click="handleConfirmMaintenance" class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700 flex items-center gap-2">
                <Plus class="w-4 h-4" />创建任务
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
