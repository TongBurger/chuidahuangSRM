<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { AlertTriangle, Clock, Filter, Search, ChevronRight, CheckCircle, XCircle, AlertCircle } from 'lucide-vue-next'
import { mockSuppliers, mockMolds } from '@/data/mockData'
import { useAppStore } from '@/stores/useAppStore'

const router = useRouter()
const appStore = useAppStore()

const selectedTab = ref('all')
const searchQuery = ref('')

// 风险类型定义
type RiskLevel = 'high' | 'medium' | 'low'
type RiskType = 'cert' | 'mold' | 'quality' | 'delivery'

interface RiskAlert {
  id: string
  type: RiskType
  level: RiskLevel
  title: string
  description: string
  supplierId?: string
  supplierName?: string
  moldId?: string
  moldCode?: string
  dueDate?: string
  createdAt: string
  status: 'pending' | 'processing' | 'resolved'
}

// 生成风险预警数据
const riskAlerts = computed<RiskAlert[]>(() => {
  const alerts: RiskAlert[] = []

  // 供应商资质预警
  mockSuppliers.forEach((supplier) => {
    supplier.certifications.forEach((cert) => {
      if (cert.status === '即将到期') {
        alerts.push({
          id: `cert-${supplier.id}-${cert.name}`,
          type: 'cert',
          level: 'high',
          title: '资质即将到期',
          description: `${supplier.name} 的 ${cert.name} 即将到期`,
          supplierId: supplier.id,
          supplierName: supplier.name,
          dueDate: cert.validUntil,
          createdAt: '2026-03-18',
          status: 'pending',
        })
      }
    })
  })

  // 模具寿命预警
  mockMolds.forEach((mold) => {
    if (mold.remainingLife < 20) {
      alerts.push({
        id: `mold-${mold.id}`,
        type: 'mold',
        level: mold.remainingLife < 10 ? 'high' : 'medium',
        title: '模具寿命预警',
        description: `${mold.name}(${mold.code}) 剩余寿命仅 ${mold.remainingLife}%`,
        moldId: mold.id,
        moldCode: mold.code,
        supplierName: mold.supplierName,
        dueDate: mold.nextMaintenanceDate,
        createdAt: '2026-03-19',
        status: 'pending',
      })
    }
  })

  // 质量风险（示例数据）
  const lowQualitySuppliers = mockSuppliers.filter((s) => s.qualityScore < 90)
  lowQualitySuppliers.forEach((supplier) => {
    alerts.push({
      id: `quality-${supplier.id}`,
      type: 'quality',
      level: supplier.qualityScore < 85 ? 'high' : 'medium',
      title: '质量分数偏低',
      description: `${supplier.name} 质量评分 ${supplier.qualityScore}，低于合格线`,
      supplierId: supplier.id,
      supplierName: supplier.name,
      createdAt: '2026-03-17',
      status: 'pending',
    })
  })

  return alerts.sort((a, b) => {
    const levelOrder = { high: 0, medium: 1, low: 2 }
    return levelOrder[a.level] - levelOrder[b.level]
  })
})

// 过滤后的预警
const filteredAlerts = computed(() => {
  let alerts = riskAlerts.value

  // 按类型过滤
  if (selectedTab.value !== 'all') {
    alerts = alerts.filter((alert) => {
      if (selectedTab.value === 'cert') return alert.type === 'cert'
      if (selectedTab.value === 'mold') return alert.type === 'mold'
      if (selectedTab.value === 'quality') return alert.type === 'quality'
      return true
    })
  }

  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    alerts = alerts.filter((alert) =>
      alert.title.toLowerCase().includes(query) ||
      alert.description.toLowerCase().includes(query) ||
      alert.supplierName?.toLowerCase().includes(query) ||
      alert.moldCode?.toLowerCase().includes(query)
    )
  }

  return alerts
})

// 统计数据
const stats = computed(() => ({
  total: riskAlerts.value.length,
  high: riskAlerts.value.filter((a) => a.level === 'high').length,
  medium: riskAlerts.value.filter((a) => a.level === 'medium').length,
  cert: riskAlerts.value.filter((a) => a.type === 'cert').length,
  mold: riskAlerts.value.filter((a) => a.type === 'mold').length,
  quality: riskAlerts.value.filter((a) => a.type === 'quality').length,
}))

function getRiskLevelConfig(level: RiskLevel) {
  switch (level) {
    case 'high':
      return {
        label: '高风险',
        bgColor: 'bg-red-100',
        textColor: 'text-red-800',
        iconBg: 'bg-red-600',
      }
    case 'medium':
      return {
        label: '中风险',
        bgColor: 'bg-orange-100',
        textColor: 'text-orange-800',
        iconBg: 'bg-orange-600',
      }
    case 'low':
      return {
        label: '低风险',
        bgColor: 'bg-yellow-100',
        textColor: 'text-yellow-800',
        iconBg: 'bg-yellow-600',
      }
  }
}

function getTypeIcon(type: RiskType) {
  switch (type) {
    case 'cert':
      return CheckCircle
    case 'mold':
      return AlertTriangle
    case 'quality':
      return XCircle
    default:
      return AlertCircle
  }
}

function handleAlertClick(alert: RiskAlert) {
  if (alert.supplierId) {
    router.push(`/suppliers/${alert.supplierId}`)
  } else if (alert.moldId) {
    router.push(`/molds/${alert.moldId}`)
  }
}

function handleMarkResolved(id: string) {
  // 实现标记为已解决的逻辑
  const alert = riskAlerts.value.find(a => a.id === id)
  if (alert) {
    alert.status = 'resolved'
    appStore.showToast('预警已标记为已处理', 'success')
  }
}
</script>

<template>
  <div class="space-y-6">
    <!-- 页面标题 -->
    <div>
      <h1 class="text-2xl font-semibold text-gray-900">风险预警中心</h1>
      <p class="text-sm text-gray-500 mt-1">实时监控系统中的各类风险，及时处理以确保供应链稳定</p>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4">
      <div class="bg-white rounded-lg border border-gray-200 p-4">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">风险总数</p>
            <p class="text-2xl font-semibold text-gray-900 mt-1">{{ stats.total }}</p>
          </div>
          <div class="w-12 h-12 bg-gray-100 rounded-lg flex items-center justify-center">
            <AlertTriangle class="w-6 h-6 text-gray-600" />
          </div>
        </div>
      </div>

      <div class="bg-white rounded-lg border border-gray-200 p-4">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">高风险</p>
            <p class="text-2xl font-semibold text-red-600 mt-1">{{ stats.high }}</p>
          </div>
          <div class="w-12 h-12 bg-red-100 rounded-lg flex items-center justify-center">
            <AlertTriangle class="w-6 h-6 text-red-600" />
          </div>
        </div>
      </div>

      <div class="bg-white rounded-lg border border-gray-200 p-4">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">资质预警</p>
            <p class="text-2xl font-semibold text-orange-600 mt-1">{{ stats.cert }}</p>
          </div>
          <div class="w-12 h-12 bg-orange-100 rounded-lg flex items-center justify-center">
            <Clock class="w-6 h-6 text-orange-600" />
          </div>
        </div>
      </div>

      <div class="bg-white rounded-lg border border-gray-200 p-4">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">模具预警</p>
            <p class="text-2xl font-semibold text-yellow-600 mt-1">{{ stats.mold }}</p>
          </div>
          <div class="w-12 h-12 bg-yellow-100 rounded-lg flex items-center justify-center">
            <AlertTriangle class="w-6 h-6 text-yellow-600" />
          </div>
        </div>
      </div>

      <div class="bg-white rounded-lg border border-gray-200 p-4">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">质量风险</p>
            <p class="text-2xl font-semibold text-purple-600 mt-1">{{ stats.quality }}</p>
          </div>
          <div class="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center">
            <XCircle class="w-6 h-6 text-purple-600" />
          </div>
        </div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="bg-white rounded-lg border border-gray-200 p-4">
      <div class="flex flex-col md:flex-row gap-4">
        <!-- 搜索框 -->
        <div class="relative flex-1">
          <Search class="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
          <input
            v-model="searchQuery"
            type="text"
            placeholder="搜索预警信息..."
            class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          />
        </div>

        <!-- 类型筛选 -->
        <div class="flex items-center gap-2">
          <Filter class="w-5 h-5 text-gray-400" />
          <div class="flex gap-2">
            <button
              @click="selectedTab = 'all'"
              :class="[
                'px-4 py-2 rounded-lg text-sm font-medium transition-colors',
                selectedTab === 'all'
                  ? 'bg-blue-600 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              ]"
            >
              全部
            </button>
            <button
              @click="selectedTab = 'cert'"
              :class="[
                'px-4 py-2 rounded-lg text-sm font-medium transition-colors',
                selectedTab === 'cert'
                  ? 'bg-blue-600 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              ]"
            >
              资质
            </button>
            <button
              @click="selectedTab = 'mold'"
              :class="[
                'px-4 py-2 rounded-lg text-sm font-medium transition-colors',
                selectedTab === 'mold'
                  ? 'bg-blue-600 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              ]"
            >
              模具
            </button>
            <button
              @click="selectedTab = 'quality'"
              :class="[
                'px-4 py-2 rounded-lg text-sm font-medium transition-colors',
                selectedTab === 'quality'
                  ? 'bg-blue-600 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              ]"
            >
              质量
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 预警列表 -->
    <div class="bg-white rounded-lg border border-gray-200">
      <div v-if="filteredAlerts.length === 0" class="text-center py-12">
        <CheckCircle class="w-16 h-16 text-green-500 mx-auto mb-4" />
        <p class="text-gray-500">暂无预警信息</p>
      </div>

      <div v-else class="divide-y divide-gray-200">
        <div
          v-for="alert in filteredAlerts"
          :key="alert.id"
          class="p-4 hover:bg-gray-50 transition-colors cursor-pointer"
          @click="handleAlertClick(alert)"
        >
          <div class="flex items-start gap-4">
            <!-- 风险图标 -->
            <div
              class="w-12 h-12 rounded-lg flex items-center justify-center flex-shrink-0"
              :class="getRiskLevelConfig(alert.level).iconBg"
            >
              <component
                :is="getTypeIcon(alert.type)"
                class="w-6 h-6 text-white"
              />
            </div>

            <!-- 预警内容 -->
            <div class="flex-1 min-w-0">
              <div class="flex items-start justify-between">
                <div class="flex-1">
                  <div class="flex items-center gap-2 mb-1">
                    <h3 class="text-base font-semibold text-gray-900">{{ alert.title }}</h3>
                    <span
                      class="px-2 py-0.5 rounded-full text-xs font-medium"
                      :class="[getRiskLevelConfig(alert.level).bgColor, getRiskLevelConfig(alert.level).textColor]"
                    >
                      {{ getRiskLevelConfig(alert.level).label }}
                    </span>
                  </div>
                  <p class="text-sm text-gray-600">{{ alert.description }}</p>
                  <div class="flex items-center gap-4 mt-2 text-xs text-gray-500">
                    <span v-if="alert.supplierName">供应商: {{ alert.supplierName }}</span>
                    <span v-if="alert.moldCode">模具: {{ alert.moldCode }}</span>
                    <span v-if="alert.dueDate">截止日期: {{ alert.dueDate }}</span>
                    <span>创建时间: {{ alert.createdAt }}</span>
                  </div>
                </div>

                <!-- 操作按钮 -->
                <div class="flex items-center gap-2 ml-4">
                  <button
                    @click.stop="handleMarkResolved(alert.id)"
                    class="px-3 py-1.5 text-sm text-blue-600 hover:bg-blue-50 rounded-lg transition-colors border border-blue-300"
                  >
                    标记已处理
                  </button>
                  <ChevronRight class="w-5 h-5 text-gray-400 flex-shrink-0" />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
