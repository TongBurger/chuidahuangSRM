<script setup lang="ts">
import { ref } from 'vue'
import { TrendingUp, Award, Target, Lightbulb, Calendar, CheckCircle } from 'lucide-vue-next'

const performanceData = {
  overallScore: 91.25,
  level: '战略供应商',
  scores: {
    quality: { score: 95, label: '质量评分', trend: '+2', description: '产品合格率98.5%，高于平均水平' },
    delivery: { score: 92, label: '交付评分', trend: '+1', description: '准时交付率96%，行业领先' },
    cost: { score: 88, label: '成本评分', trend: '0', description: '价格竞争力良好，略有优化空间' },
    innovation: { score: 90, label: '创新评分', trend: '+3', description: '技术研发能力强，积极配合改进' },
    service: { score: 93, label: '服务评分', trend: '+1', description: '响应速度快，售后服务优秀' },
  },
  advantages: [
    '质量管理体系完善，产品合格率高',
    '交付准时率行业领先',
    '技术研发能力强，积极配合改进',
    '售后服务响应及时',
    '供应链配合度高'
  ],
  improvements: [
    '可以进一步优化成本控制，提升价格竞争力',
    '建议增加产能储备，应对旺季需求',
    '加强原材料库存管理，降低缺料风险'
  ],
}

// 历史绩效数据
const performanceHistory = [
  { period: '2026年Q1', overallScore: 91.25, quality: 95, delivery: 92, cost: 88, innovation: 90, service: 93 },
  { period: '2025年Q4', overallScore: 90.5, quality: 94, delivery: 91, cost: 88, innovation: 88, service: 92 },
  { period: '2025年Q3', overallScore: 89.75, quality: 93, delivery: 90, cost: 87, innovation: 88, service: 91 },
  { period: '2025年Q2', overallScore: 89.0, quality: 92, delivery: 90, cost: 87, innovation: 87, service: 90 },
  { period: '2025年Q1', overallScore: 88.5, quality: 92, delivery: 89, cost: 86, innovation: 86, service: 90 },
]

// 质量指标详情
const qualityMetrics = [
  { name: '产品合格率', value: '98.5%', target: '≥98%', status: '达标' },
  { name: '一次交检合格率', value: '96.8%', target: '≥95%', status: '达标' },
  { name: '客户投诉率', value: '0.2%', target: '≤1%', status: '达标' },
  { name: '质量事故次数', value: '0次', target: '0次', status: '达标' },
]

// 交付指标详情
const deliveryMetrics = [
  { name: '准时交付率', value: '96%', target: '≥95%', status: '达标' },
  { name: '平均交付周期', value: '12天', target: '≤15天', status: '达标' },
  { name: '紧急订单响应', value: '24小时', target: '≤48小时', status: '优秀' },
  { name: '交付准确率', value: '99.5%', target: '≥99%', status: '达标' },
]

const scoreItems = [
  { label: '质量评分', score: 95, color: 'bg-blue-600', trend: '+2', description: '产品合格率98.5%，高于平均水平' },
  { label: '交付评分', score: 92, color: 'bg-green-600', trend: '+1', description: '准时交付率96%，行业领先' },
  { label: '成本评分', score: 88, color: 'bg-purple-600', trend: '0', description: '价格竞争力良好，略有优化空间' },
  { label: '创新评分', score: 90, color: 'bg-orange-600', trend: '+3', description: '技术研发能力强，积极配合改进' },
  { label: '服务评分', score: 93, color: 'bg-cyan-600', trend: '+1', description: '响应速度快，售后服务优秀' },
]
</script>

<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-semibold text-gray-900">绩效报告</h1>
      <p class="text-sm text-gray-500 mt-1">查看供应商绩效评估和改进建议</p>
    </div>

    <!-- 综合评分卡片 -->
    <div class="bg-gradient-to-r from-green-600 to-green-800 rounded-lg p-6 text-white">
      <div class="flex items-center justify-between">
        <div>
          <h2 class="text-xl font-semibold mb-2">综合评分</h2>
          <p class="text-green-100">基于最近12个月的绩效评估</p>
          <div class="flex items-center gap-2 mt-3">
            <CheckCircle class="w-5 h-5" />
            <span class="text-sm">连续5个季度保持优秀水平</span>
          </div>
        </div>
        <div class="text-center">
          <div class="text-5xl font-bold">{{ performanceData.overallScore }}</div>
          <div class="text-green-100 mt-1">{{ performanceData.level }}</div>
        </div>
      </div>
    </div>

    <!-- 评分详情 -->
    <div class="bg-white rounded-lg border border-gray-200 p-6">
      <h3 class="text-lg font-semibold text-gray-900 mb-4">评分详情</h3>
      <div class="space-y-4">
        <div v-for="item in scoreItems" :key="item.label" class="p-4 bg-gray-50 rounded-lg">
          <div class="flex items-center justify-between mb-2">
            <div class="flex items-center gap-2">
              <span class="text-sm font-medium text-gray-700">{{ item.label }}</span>
              <span v-if="item.trend !== '0'" class="text-xs px-2 py-0.5 rounded-full" :class="item.trend.startsWith('+') ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'">
                {{ item.trend }}
              </span>
            </div>
            <span class="text-sm font-semibold text-gray-900">{{ item.score }}</span>
          </div>
          <div class="h-3 bg-gray-200 rounded-full overflow-hidden mb-2">
            <div class="h-full transition-all" :class="item.color" :style="{ width: `${item.score}%` }" />
          </div>
          <p class="text-xs text-gray-600">{{ item.description }}</p>
        </div>
      </div>
    </div>

    <!-- 详细指标 -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- 质量指标 -->
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <h3 class="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
          <Award class="w-5 h-5 text-blue-600" />
          质量指标
        </h3>
        <div class="space-y-3">
          <div v-for="metric in qualityMetrics" :key="metric.name" class="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
            <div>
              <p class="text-sm font-medium text-gray-900">{{ metric.name }}</p>
              <p class="text-xs text-gray-500">目标: {{ metric.target }}</p>
            </div>
            <div class="text-right">
              <p class="text-lg font-semibold" :class="metric.status === '优秀' ? 'text-green-600' : 'text-blue-600'">{{ metric.value }}</p>
              <p class="text-xs" :class="metric.status === '优秀' ? 'text-green-600' : 'text-gray-500'">{{ metric.status }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 交付指标 -->
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <h3 class="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
          <TrendingUp class="w-5 h-5 text-green-600" />
          交付指标
        </h3>
        <div class="space-y-3">
          <div v-for="metric in deliveryMetrics" :key="metric.name" class="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
            <div>
              <p class="text-sm font-medium text-gray-900">{{ metric.name }}</p>
              <p class="text-xs text-gray-500">目标: {{ metric.target }}</p>
            </div>
            <div class="text-right">
              <p class="text-lg font-semibold" :class="metric.status === '优秀' ? 'text-green-600' : 'text-blue-600'">{{ metric.value }}</p>
              <p class="text-xs" :class="metric.status === '优秀' ? 'text-green-600' : 'text-gray-500'">{{ metric.status }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 优势与建议 -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex items-center gap-2 mb-4">
          <Award class="w-5 h-5 text-green-600" />
          <h3 class="text-lg font-semibold text-gray-900">核心优势</h3>
        </div>
        <ul class="space-y-2">
          <li v-for="(advantage, idx) in performanceData.advantages" :key="idx" class="flex items-start gap-2">
            <Target class="w-4 h-4 text-green-600 mt-0.5 flex-shrink-0" />
            <span class="text-sm text-gray-700">{{ advantage }}</span>
          </li>
        </ul>
      </div>

      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex items-center gap-2 mb-4">
          <Lightbulb class="w-5 h-5 text-orange-600" />
          <h3 class="text-lg font-semibold text-gray-900">改进建议</h3>
        </div>
        <ul class="space-y-2">
          <li v-for="(improvement, idx) in performanceData.improvements" :key="idx" class="flex items-start gap-2">
            <Lightbulb class="w-4 h-4 text-orange-600 mt-0.5 flex-shrink-0" />
            <span class="text-sm text-gray-700">{{ improvement }}</span>
          </li>
        </ul>
      </div>
    </div>

    <!-- 历史趋势 -->
    <div class="bg-white rounded-lg border border-gray-200 p-6">
      <div class="flex items-center gap-2 mb-4">
        <Calendar class="w-5 h-5 text-gray-600" />
        <h3 class="text-lg font-semibold text-gray-900">评分趋势</h3>
      </div>
      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead class="bg-gray-50">
            <tr>
              <th class="text-left px-4 py-3 font-medium text-gray-700">周期</th>
              <th class="text-right px-4 py-3 font-medium text-gray-700">综合评分</th>
              <th class="text-right px-4 py-3 font-medium text-gray-700">质量</th>
              <th class="text-right px-4 py-3 font-medium text-gray-700">交付</th>
              <th class="text-right px-4 py-3 font-medium text-gray-700">成本</th>
              <th class="text-right px-4 py-3 font-medium text-gray-700">创新</th>
              <th class="text-right px-4 py-3 font-medium text-gray-700">服务</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(record, idx) in performanceHistory" :key="idx" class="border-t border-gray-200">
              <td class="px-4 py-3 font-medium text-gray-900">{{ record.period }}</td>
              <td class="px-4 py-3 text-right font-semibold text-green-600">{{ record.overallScore }}</td>
              <td class="px-4 py-3 text-right text-gray-900">{{ record.quality }}</td>
              <td class="px-4 py-3 text-right text-gray-900">{{ record.delivery }}</td>
              <td class="px-4 py-3 text-right text-gray-900">{{ record.cost }}</td>
              <td class="px-4 py-3 text-right text-gray-900">{{ record.innovation }}</td>
              <td class="px-4 py-3 text-right text-gray-900">{{ record.service }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
