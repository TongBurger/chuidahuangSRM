<script setup lang="ts">
import { computed, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Download, History, Edit, Printer, Mail, X, Check } from 'lucide-vue-next'
import { mockOrders } from '@/data/mockData'
import { useAppStore } from '@/stores/useAppStore'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

const order = computed(() => mockOrders.find((o) => o.id === route.params.id))

// 编辑对话框
const isEditDialogOpen = ref(false)
const editForm = ref({
  deliveryDate: '',
  note: '',
})

// 发送邮件对话框
const isEmailDialogOpen = ref(false)
const emailForm = ref({
  to: '',
  cc: '',
  subject: '',
  body: '',
})

function getProgressSteps(status: string) {
  return [
    { label: '订单创建', completed: true },
    { label: '供应商确认', completed: true },
    { label: '生产中', completed: status !== '待确认' },
    { label: '已发货', completed: ['已发货', '已收货', '已入库', '已完成'].includes(status) },
    { label: '已收货', completed: ['已收货', '已入库', '已完成'].includes(status) },
    { label: '已入库', completed: ['已入库', '已完成'].includes(status) },
  ]
}

// 导出订单
function handleExportOrder() {
  if (!order.value) return
  appStore.showToast(`正在导出订单 ${order.value.orderNo}...`, 'info')
  setTimeout(() => {
    // 模拟导出
    const orderData = JSON.stringify(order.value, null, 2)
    const blob = new Blob([orderData], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `订单_${order.value.orderNo}.json`
    a.click()
    URL.revokeObjectURL(url)
    appStore.showToast('订单导出成功', 'success')
  }, 1000)
}

// 打开编辑对话框
function handleEditOrder() {
  if (!order.value) return
  editForm.value = {
    deliveryDate: order.value.deliveryDate,
    note: '',
  }
  isEditDialogOpen.value = true
}

// 保存修改
function handleSaveEdit() {
  if (!order.value) return
  appStore.showToast(`订单 ${order.value.orderNo} 修改成功`, 'success')
  isEditDialogOpen.value = false
}

// 打印订单
function handlePrintOrder() {
  if (!order.value) return
  appStore.showToast(`正在准备打印订单 ${order.value.orderNo}...`, 'info')
  setTimeout(() => {
    window.print()
    appStore.showToast('打印命令已发送', 'success')
  }, 500)
}

// 发送邮件
function handleSendEmail() {
  if (!order.value) return
  emailForm.value = {
    to: `${order.value.supplierName}@example.com`,
    cc: '',
    subject: `订单确认 - ${order.value.orderNo}`,
    body: `尊敬的供应商，\n\n您的订单 ${order.value.orderNo} 已确认，请按时交付。\n\n订单金额: ¥${order.value.totalAmount.toLocaleString()}\n交货日期: ${order.value.deliveryDate}\n\n谢谢！`,
  }
  isEmailDialogOpen.value = true
}

// 确认发送邮件
function handleConfirmSendEmail() {
  appStore.showToast('邮件发送成功', 'success')
  isEmailDialogOpen.value = false
}
</script>

<template>
  <div v-if="!order" class="text-center py-12">
    <p class="text-gray-500">订单不存在</p>
    <RouterLink to="/orders"><button class="mt-4 px-4 py-2 border border-gray-300 rounded-lg text-sm">返回列表</button></RouterLink>
  </div>

  <div v-else class="space-y-6">
    <!-- 标题 -->
    <div class="flex items-center gap-4">
      <RouterLink to="/orders">
        <button class="flex items-center gap-2 px-3 py-1.5 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">
          <ArrowLeft class="w-4 h-4" />返回
        </button>
      </RouterLink>
      <div class="flex-1">
        <div class="flex items-center gap-3">
          <h1 class="text-2xl font-semibold text-gray-900">{{ order.orderNo }}</h1>
          <span class="px-2 py-0.5 bg-gray-100 text-gray-800 rounded-full text-xs font-medium">{{ order.status }}</span>
        </div>
        <p class="text-sm text-gray-500 mt-1">订单详情 · ERP单据同步</p>
      </div>
      <button @click="handleExportOrder" class="flex items-center gap-2 px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">
        <Download class="w-4 h-4" />导出订单
      </button>
    </div>

    <!-- 信息卡片 -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <h3 class="text-base font-semibold text-gray-900 mb-4">订单信息</h3>
        <div class="space-y-3">
          <div><p class="text-sm text-gray-500">订单编号</p><p class="font-medium text-gray-900 mt-1">{{ order.orderNo }}</p></div>
          <div><p class="text-sm text-gray-500">供应商</p><p class="font-medium text-gray-900 mt-1">{{ order.supplierName }}</p></div>
          <div><p class="text-sm text-gray-500">订单日期</p><p class="font-medium text-gray-900 mt-1">{{ order.orderDate }}</p></div>
          <div><p class="text-sm text-gray-500">交货日期</p><p class="font-medium text-gray-900 mt-1">{{ order.deliveryDate }}</p></div>
          <div><p class="text-sm text-gray-500">订单状态</p><span class="inline-block mt-1 px-2 py-0.5 bg-blue-100 text-blue-800 rounded-full text-xs font-medium">{{ order.status }}</span></div>
        </div>
      </div>

      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <h3 class="text-base font-semibold text-gray-900 mb-4">金额信息</h3>
        <div class="space-y-3">
          <div><p class="text-sm text-gray-500">订单总额</p><p class="text-2xl font-semibold text-gray-900 mt-1">¥{{ order.totalAmount.toLocaleString() }}</p></div>
          <div class="pt-4 border-t">
            <div><p class="text-sm text-gray-500">物料种类</p><p class="font-medium text-gray-900 mt-1">{{ order.items.length }} 种</p></div>
            <div class="mt-3"><p class="text-sm text-gray-500">总数量</p><p class="font-medium text-gray-900 mt-1">{{ order.items.reduce((sum, item) => sum + item.quantity, 0) }} 件</p></div>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <h3 class="text-base font-semibold text-gray-900 mb-4">进度追踪</h3>
        <div class="space-y-4">
          <div v-for="(step, idx) in getProgressSteps(order.status)" :key="idx" class="flex items-start gap-3">
            <div class="w-6 h-6 rounded-full flex items-center justify-center flex-shrink-0" :class="step.completed ? 'bg-green-600' : 'bg-gray-300'">
              <Check v-if="step.completed" class="w-4 h-4 text-white" />
            </div>
            <p class="text-sm font-medium" :class="step.completed ? 'text-gray-900' : 'text-gray-500'">{{ step.label }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 物料明细 -->
    <div class="bg-white rounded-lg border border-gray-200 p-6">
      <h3 class="text-base font-semibold text-gray-900 mb-4">物料明细</h3>
      <div class="border border-gray-200 rounded-lg overflow-hidden">
        <table class="w-full">
          <thead class="bg-gray-50">
            <tr>
              <th class="text-left px-4 py-3 text-sm font-medium text-gray-700">物料编码</th>
              <th class="text-left px-4 py-3 text-sm font-medium text-gray-700">物料名称</th>
              <th class="text-right px-4 py-3 text-sm font-medium text-gray-700">数量</th>
              <th class="text-right px-4 py-3 text-sm font-medium text-gray-700">单位</th>
              <th class="text-right px-4 py-3 text-sm font-medium text-gray-700">单价</th>
              <th class="text-right px-4 py-3 text-sm font-medium text-gray-700">金额</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, idx) in order.items" :key="idx" class="border-t border-gray-200">
              <td class="px-4 py-3 text-sm text-gray-900">{{ item.materialCode }}</td>
              <td class="px-4 py-3 text-sm text-gray-900">{{ item.materialName }}</td>
              <td class="px-4 py-3 text-sm text-right text-gray-900">{{ item.quantity }}</td>
              <td class="px-4 py-3 text-sm text-right text-gray-900">{{ item.unit }}</td>
              <td class="px-4 py-3 text-sm text-right text-gray-900">¥{{ item.price.toLocaleString() }}</td>
              <td class="px-4 py-3 text-sm text-right font-medium text-gray-900">¥{{ item.amount.toLocaleString() }}</td>
            </tr>
            <tr class="border-t-2 border-gray-300 bg-gray-50">
              <td colspan="5" class="px-4 py-3 text-sm font-medium text-right text-gray-900">合计</td>
              <td class="px-4 py-3 text-sm text-right font-semibold text-gray-900">¥{{ order.totalAmount.toLocaleString() }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 变更历史 -->
    <div class="bg-white rounded-lg border border-gray-200 p-6">
      <h3 class="text-base font-semibold text-gray-900 flex items-center gap-2 mb-4">
        <History class="w-5 h-5" />变更历史
      </h3>
      <div class="space-y-3">
        <div v-for="(change, idx) in order.changeHistory" :key="idx" class="flex gap-4 p-4 bg-gray-50 rounded-lg">
          <div class="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center flex-shrink-0">
            <span class="text-sm font-medium text-blue-600">V{{ change.version }}</span>
          </div>
          <div class="flex-1">
            <div class="flex items-center justify-between mb-2">
              <p class="font-medium text-gray-900">{{ change.changeType }}</p>
              <span class="text-xs text-gray-500">{{ change.changeDate }}</span>
            </div>
            <p class="text-sm text-gray-600">{{ change.description }}</p>
            <p class="text-xs text-gray-500 mt-2">操作人: {{ change.operator }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 操作区域 -->
    <div class="flex gap-4">
      <button @click="handleEditOrder" class="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50 flex items-center justify-center gap-2">
        <Edit class="w-4 h-4" />修改订单
      </button>
      <button @click="handlePrintOrder" class="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50 flex items-center justify-center gap-2">
        <Printer class="w-4 h-4" />打印订单
      </button>
      <button @click="handleSendEmail" class="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50 flex items-center justify-center gap-2">
        <Mail class="w-4 h-4" />发送邮件
      </button>
    </div>

    <!-- 编辑订单对话框 -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="isEditDialogOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-black/50" @click="isEditDialogOpen = false" />
          <div class="relative bg-white rounded-xl shadow-xl w-full max-w-lg">
            <div class="flex items-center justify-between p-4 border-b">
              <h3 class="text-lg font-semibold text-gray-900">修改订单</h3>
              <button @click="isEditDialogOpen = false" class="p-2 hover:bg-gray-100 rounded-lg">
                <X class="w-5 h-5" />
              </button>
            </div>
            <div class="p-6 space-y-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">订单号</label>
                <input :value="order.orderNo" disabled class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm bg-gray-50 text-gray-500" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">交货日期</label>
                <input v-model="editForm.deliveryDate" type="date" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">备注</label>
                <textarea v-model="editForm.note" rows="3" placeholder="请输入修改备注" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"></textarea>
              </div>
            </div>
            <div class="flex justify-end gap-3 p-4 border-t bg-gray-50">
              <button @click="isEditDialogOpen = false" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">取消</button>
              <button @click="handleSaveEdit" class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">保存修改</button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- 发送邮件对话框 -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="isEmailDialogOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-black/50" @click="isEmailDialogOpen = false" />
          <div class="relative bg-white rounded-xl shadow-xl w-full max-w-lg">
            <div class="flex items-center justify-between p-4 border-b">
              <h3 class="text-lg font-semibold text-gray-900">发送邮件</h3>
              <button @click="isEmailDialogOpen = false" class="p-2 hover:bg-gray-100 rounded-lg">
                <X class="w-5 h-5" />
              </button>
            </div>
            <div class="p-6 space-y-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">收件人</label>
                <input v-model="emailForm.to" type="email" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">抄送</label>
                <input v-model="emailForm.cc" type="email" placeholder="可选" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">主题</label>
                <input v-model="emailForm.subject" type="text" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">正文</label>
                <textarea v-model="emailForm.body" rows="5" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"></textarea>
              </div>
            </div>
            <div class="flex justify-end gap-3 p-4 border-t bg-gray-50">
              <button @click="isEmailDialogOpen = false" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">取消</button>
              <button @click="handleConfirmSendEmail" class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700 flex items-center gap-2">
                <Mail class="w-4 h-4" />发送邮件
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

@media print {
  button, a {
    display: none !important;
  }
}
</style>
