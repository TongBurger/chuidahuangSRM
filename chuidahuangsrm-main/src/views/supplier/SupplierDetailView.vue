<script setup lang="ts">
import { ref, computed } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { ArrowLeft, Download, FileText, AlertCircle, TrendingUp, Plus, Edit, Trash2, X } from 'lucide-vue-next'
import { mockSuppliers } from '@/data/mockData'
import { useAppStore } from '@/stores/useAppStore'

const route = useRoute()
const appStore = useAppStore()
const supplier = computed(() => mockSuppliers.find((s) => s.id === route.params.id))
const activeTab = ref('certifications')

// 资质管理对话框
const isCertDialogOpen = ref(false)
const isEditingCert = ref(false)
const certForm = ref({
  id: '',
  name: '',
  validUntil: '',
  status: '有效'
})

// 打开新增资质对话框
function openAddCertDialog() {
  isEditingCert.value = false
  certForm.value = {
    id: '',
    name: '',
    validUntil: '',
    status: '有效'
  }
  isCertDialogOpen.value = true
}

// 打开编辑资质对话框
function openEditCertDialog(cert: any) {
  isEditingCert.value = true
  certForm.value = {
    id: cert.id || Date.now().toString(),
    name: cert.name,
    validUntil: cert.validUntil,
    status: cert.status
  }
  isCertDialogOpen.value = true
}

// 保存资质
function handleSaveCert() {
  if (!supplier.value) return

  // 从localStorage获取供应商数据
  const savedSuppliers = localStorage.getItem('suppliers')
  let suppliers = savedSuppliers ? JSON.parse(savedSuppliers) : [...mockSuppliers]

  const supplierIndex = suppliers.findIndex((s: any) => s.id === supplier.value!.id)
  if (supplierIndex !== -1) {
    if (isEditingCert.value) {
      // 编辑现有资质
      const certIndex = suppliers[supplierIndex].certifications.findIndex((c: any) => c.id === certForm.value.id)
      if (certIndex !== -1) {
        suppliers[supplierIndex].certifications[certIndex] = { ...certForm.value }
      }
    } else {
      // 添加新资质
      const { id, ...certData } = certForm.value
      suppliers[supplierIndex].certifications.push({
        id: Date.now().toString(),
        ...certData
      })
    }

    // 保存到localStorage
    localStorage.setItem('suppliers', JSON.stringify(suppliers))

    // 更新mockSuppliers
    const mockIndex = mockSuppliers.findIndex(s => s.id === supplier.value!.id)
    if (mockIndex !== -1) {
      mockSuppliers[mockIndex] = suppliers[supplierIndex]
    }

    appStore.showToast(isEditingCert.value ? '资质更新成功' : '资质添加成功', 'success')
    isCertDialogOpen.value = false
  }
}

// 删除资质
function handleDeleteCert(cert: any) {
  if (!supplier.value) return
  if (!confirm(`确定要删除资质"${cert.name}"吗？`)) return

  // 从localStorage获取供应商数据
  const savedSuppliers = localStorage.getItem('suppliers')
  let suppliers = savedSuppliers ? JSON.parse(savedSuppliers) : [...mockSuppliers]

  const supplierIndex = suppliers.findIndex((s: any) => s.id === supplier.value!.id)
  if (supplierIndex !== -1) {
    suppliers[supplierIndex].certifications = suppliers[supplierIndex].certifications.filter((c: any) => c.id !== cert.id)

    // 保存到localStorage
    localStorage.setItem('suppliers', JSON.stringify(suppliers))

    // 更新mockSuppliers
    const mockIndex = mockSuppliers.findIndex(s => s.id === supplier.value!.id)
    if (mockIndex !== -1) {
      mockSuppliers[mockIndex] = suppliers[supplierIndex]
    }

    appStore.showToast('资质删除成功', 'success')
  }
}

function getCertStatusColor(status: string) {
  switch (status) {
    case '有效': return 'bg-green-100 text-green-800'
    case '即将到期': return 'bg-orange-100 text-orange-800'
    case '已过期': return 'bg-red-100 text-red-800'
    default: return 'bg-gray-100 text-gray-800'
  }
}

const performanceHistory = [
  { period: '2026年Q1', score: 91.25, trend: 'up' },
  { period: '2025年Q4', score: 89.5, trend: 'up' },
  { period: '2025年Q3', score: 87.8, trend: 'down' },
]

// 导出报告
function handleExportReport() {
  if (!supplier.value) return
  appStore.showToast(`正在导出 ${supplier.value.name} 的报告...`, 'info')
  setTimeout(() => {
    appStore.showToast('报告导出成功', 'success')
  }, 1500)
}

// 下载资质证书
function handleDownloadCert(cert: any) {
  appStore.showToast(`正在下载 ${cert.name}...`, 'info')
  setTimeout(() => {
    appStore.showToast('下载完成', 'success')
  }, 1000)
}
</script>

<template>
  <div v-if="!supplier" class="text-center py-12">
    <p class="text-gray-500">供应商不存在</p>
    <RouterLink to="/suppliers">
      <button class="mt-4 px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">
        返回列表
      </button>
    </RouterLink>
  </div>

  <div v-else class="space-y-6">
    <!-- 页面标题 -->
    <div class="flex items-center gap-4">
      <RouterLink to="/suppliers">
        <button class="flex items-center gap-2 px-3 py-1.5 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">
          <ArrowLeft class="w-4 h-4" />
          返回
        </button>
      </RouterLink>
      <div class="flex-1">
        <h1 class="text-2xl font-semibold text-gray-900">{{ supplier.name }}</h1>
        <p class="text-sm text-gray-500 mt-1">供应商编码: {{ supplier.code }}</p>
      </div>
      <button @click="handleExportReport" class="flex items-center gap-2 px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">
        <Download class="w-4 h-4" />
        导出报告
      </button>
    </div>

    <!-- 基本信息卡片 -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <h3 class="text-base font-semibold text-gray-900 mb-4">基本信息</h3>
        <div class="space-y-3">
          <div>
            <p class="text-sm text-gray-500">供应商分类</p>
            <span class="inline-block mt-1 px-2 py-0.5 bg-blue-600 text-white rounded-full text-xs font-medium">{{ supplier.category }}</span>
          </div>
          <div><p class="text-sm text-gray-500">状态</p><p class="font-medium text-gray-900 mt-1">{{ supplier.status }}</p></div>
          <div>
            <p class="text-sm text-gray-500">风险等级</p>
            <span class="inline-block mt-1 px-2 py-0.5 rounded-full text-xs font-medium" :class="supplier.riskLevel === '低' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'">{{ supplier.riskLevel }}风险</span>
          </div>
          <div><p class="text-sm text-gray-500">联系人</p><p class="font-medium text-gray-900 mt-1">{{ supplier.contact }}</p></div>
          <div><p class="text-sm text-gray-500">联系电话</p><p class="font-medium text-gray-900 mt-1">{{ supplier.phone }}</p></div>
          <div><p class="text-sm text-gray-500">电子邮箱</p><p class="font-medium text-gray-900 mt-1">{{ supplier.email }}</p></div>
          <div><p class="text-sm text-gray-500">地址</p><p class="font-medium text-gray-900 mt-1">{{ supplier.address }}</p></div>
        </div>
      </div>

      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <h3 class="text-base font-semibold text-gray-900 mb-4">业务数据</h3>
        <div class="space-y-4">
          <div>
            <p class="text-sm text-gray-500">年度支出</p>
            <p class="text-2xl font-semibold text-gray-900 mt-1">¥{{ supplier.annualSpend.toLocaleString() }}</p>
          </div>
          <div>
            <p class="text-sm text-gray-500">订单总数</p>
            <p class="text-2xl font-semibold text-gray-900 mt-1">{{ supplier.orderCount }}</p>
          </div>
          <div>
            <p class="text-sm text-gray-500 mb-2">准时交付率</p>
            <div class="flex items-center justify-between text-sm mb-1">
              <span class="text-gray-900">{{ supplier.onTimeRate }}%</span>
            </div>
            <div class="h-2 bg-gray-200 rounded-full overflow-hidden">
              <div class="h-full bg-blue-600" :style="{ width: `${supplier.onTimeRate}%` }" />
            </div>
          </div>
          <div>
            <p class="text-sm text-gray-500 mb-2">质量合格率</p>
            <div class="flex items-center justify-between text-sm mb-1">
              <span class="text-gray-900">{{ supplier.qualityPassRate }}%</span>
            </div>
            <div class="h-2 bg-gray-200 rounded-full overflow-hidden">
              <div class="h-full bg-green-600" :style="{ width: `${supplier.qualityPassRate}%` }" />
            </div>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <h3 class="text-base font-semibold text-gray-900 mb-4">综合评分</h3>
        <div class="text-center py-4">
          <div class="inline-flex items-center justify-center w-24 h-24 rounded-full bg-gradient-to-br from-blue-600 to-blue-800 mb-2">
            <span class="text-3xl font-bold text-white">{{ supplier.overallScore }}</span>
          </div>
          <p class="text-sm text-gray-500">综合得分</p>
        </div>
        <div class="space-y-3">
          <div v-for="(score, label) in { '质量评分': supplier.qualityScore, '交付评分': supplier.deliveryScore, '成本评分': supplier.costScore, '创新评分': supplier.innovationScore }" :key="label">
            <div class="flex items-center justify-between text-sm mb-1">
              <span class="text-gray-600">{{ label }}</span>
              <span class="font-medium text-gray-900">{{ score }}</span>
            </div>
            <div class="h-2 bg-gray-200 rounded-full overflow-hidden">
              <div class="h-full bg-blue-600" :style="{ width: `${score}%` }" />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 详细信息选项卡 -->
    <div class="bg-white rounded-lg border border-gray-200">
      <div class="flex border-b border-gray-200">
        <button v-for="tab in [{ id: 'certifications', label: '资质证书' }, { id: 'risk', label: '风险评估' }, { id: 'orders', label: '订单记录' }, { id: 'performance', label: '绩效历史' }]" :key="tab.id" class="px-6 py-3 text-sm font-medium transition-colors" :class="activeTab === tab.id ? 'border-b-2 border-blue-600 text-blue-600' : 'text-gray-600 hover:text-gray-900'" @click="activeTab = tab.id">
          {{ tab.label }}
        </button>
      </div>

      <div class="p-6">
        <!-- 资质证书 -->
        <div v-if="activeTab === 'certifications'" class="space-y-4">
          <div class="flex items-center justify-between">
            <p class="text-sm text-gray-600">共有 {{ supplier.certifications.length }} 项资质</p>
            <button @click="openAddCertDialog" class="flex items-center gap-1 px-3 py-1.5 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">
              <Plus class="w-4 h-4" />添加资质
            </button>
          </div>
          <div v-for="(cert, idx) in supplier.certifications" :key="idx" class="flex items-center justify-between p-4 border border-gray-200 rounded-lg">
            <div class="flex items-center gap-4">
              <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
                <FileText class="w-6 h-6 text-blue-600" />
              </div>
              <div>
                <p class="font-medium text-gray-900">{{ cert.name }}</p>
                <p class="text-sm text-gray-500 mt-1">有效期至: {{ cert.validUntil }}</p>
              </div>
            </div>
            <div class="flex items-center gap-3">
              <span class="px-3 py-1 rounded-full text-xs font-medium" :class="getCertStatusColor(cert.status)">{{ cert.status }}</span>
              <button @click="openEditCertDialog(cert)" class="flex items-center gap-1 px-2 py-1.5 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">
                <Edit class="w-3 h-3" />编辑
              </button>
              <button @click="handleDeleteCert(cert)" class="flex items-center gap-1 px-2 py-1.5 border border-red-300 rounded-lg text-sm text-red-700 hover:bg-red-50">
                <Trash2 class="w-3 h-3" />删除
              </button>
              <button @click="handleDownloadCert(cert)" class="flex items-center gap-1 px-3 py-1.5 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">
                <Download class="w-4 h-4" />下载
              </button>
            </div>
          </div>
        </div>

        <!-- 风险评估 -->
        <div v-if="activeTab === 'risk'" class="space-y-4">
          <div class="p-4 bg-gray-50 rounded-lg">
            <div class="flex items-center justify-between mb-2">
              <span class="text-sm font-medium text-gray-900">风险综合评分</span>
              <span class="text-2xl font-bold text-gray-900">{{ supplier.riskScore }}</span>
            </div>
            <div class="h-2 bg-gray-200 rounded-full overflow-hidden">
              <div class="h-full bg-green-600" :style="{ width: `${100 - supplier.riskScore}%` }" />
            </div>
            <p class="text-xs text-gray-500 mt-2">分数越低，风险越小</p>
          </div>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div v-for="risk in [{ label: '合规风险', desc: '资质齐全，合规性良好' }, { label: '财务风险', desc: '财务状况稳定' }, { label: '运营风险', desc: '生产能力充足' }, { label: '集中度风险', desc: '需关注依赖程度' }]" :key="risk.label" class="p-4 border border-gray-200 rounded-lg">
              <div class="flex items-center gap-2 mb-2">
                <AlertCircle class="w-4 h-4 text-green-600" />
                <span class="text-sm font-medium text-gray-900">{{ risk.label }}</span>
              </div>
              <p class="text-xs text-gray-500">{{ risk.desc }}</p>
            </div>
          </div>
        </div>

        <!-- 订单记录 -->
        <div v-if="activeTab === 'orders'" class="text-center py-8 text-gray-500">
          <p>订单记录请在"订单协同"模块查看</p>
          <RouterLink to="/orders">
            <button class="mt-4 px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">前往订单管理</button>
          </RouterLink>
        </div>

        <!-- 绩效历史 -->
        <div v-if="activeTab === 'performance'" class="space-y-4">
          <div v-for="item in performanceHistory" :key="item.period" class="flex items-center justify-between p-4 border border-gray-200 rounded-lg">
            <div>
              <p class="font-medium text-gray-900">{{ item.period }}</p>
              <p class="text-sm text-gray-500 mt-1">综合评分</p>
            </div>
            <div class="flex items-center gap-4">
              <p class="text-2xl font-semibold text-gray-900">{{ item.score }}</p>
              <TrendingUp class="w-5 h-5" :class="item.trend === 'up' ? 'text-green-600' : 'text-red-600 rotate-180'" />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 资质管理对话框 -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="isCertDialogOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <!-- 背景遮罩 -->
          <div class="absolute inset-0 bg-black/50" @click="isCertDialogOpen = false"></div>
          <!-- 对话框内容 -->
          <div class="relative bg-white rounded-xl shadow-2xl w-full max-w-md p-6">
            <div class="flex items-center justify-between mb-4">
              <h3 class="text-lg font-semibold text-gray-900">{{ isEditingCert ? '编辑资质' : '添加资质' }}</h3>
              <button @click="isCertDialogOpen = false" class="p-1 hover:bg-gray-100 rounded-lg">
                <X class="w-5 h-5 text-gray-400" />
              </button>
            </div>
            <div class="space-y-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">资质名称</label>
                <input v-model="certForm.name" type="text" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500" placeholder="如：ISO9001质量管理体系认证">
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">有效期至</label>
                <input v-model="certForm.validUntil" type="date" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500">
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">状态</label>
                <select v-model="certForm.status" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500">
                  <option value="有效">有效</option>
                  <option value="即将到期">即将到期</option>
                  <option value="已过期">已过期</option>
                </select>
              </div>
            </div>
            <div class="flex items-center justify-end gap-3 mt-6">
              <button @click="isCertDialogOpen = false" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">取消</button>
              <button @click="handleSaveCert" class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">保存</button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<style scoped>
.modal-enter-active,
.modal-leave-active {
  transition: all 0.2s ease;
}
.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}
.modal-enter-from .relative,
.modal-leave-to .relative {
  transform: scale(0.95);
}
</style>
