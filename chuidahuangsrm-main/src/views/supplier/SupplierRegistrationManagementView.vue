<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { UserCheck, XCircle, CheckCircle, Eye, FileText, AlertCircle } from 'lucide-vue-next'
import { useAppStore } from '@/stores/useAppStore'
import { mockSuppliers } from '@/data/mockData'
import type { Supplier } from '@/types'

const appStore = useAppStore()

interface Registration {
  id: string
  companyName: string
  unifiedCreditCode: string
  legalPerson: string
  registeredCapital: string
  establishDate: string
  companyType: string
  businessScope: string
  address: string
  contactPerson: string
  contactPhone: string
  contactEmail: string
  mainProducts: string
  annualRevenue: string
  certificateTypes: string[]
  username: string
  status: 'pending' | 'approved' | 'rejected'
  submittedAt: string
  reviewedAt?: string
  reviewNote?: string
}

const registrations = ref<Registration[]>([])
const selectedRegistration = ref<Registration | null>(null)
const isDetailDialogOpen = ref(false)
const reviewForm = ref({
  action: 'approve' as 'approve' | 'reject',
  note: '',
})

// 初始化注册数据
onMounted(() => {
  const saved = localStorage.getItem('supplier_registrations')
  if (saved) {
    try {
      registrations.value = JSON.parse(saved)
    } catch {
      registrations.value = []
    }
  } else {
    // 初始化示例数据
    registrations.value = [
      {
        id: 'REG001',
        companyName: '广州市新兴五金制品有限公司',
        unifiedCreditCode: '91440101MA59XXX123',
        legalPerson: '张伟',
        registeredCapital: '500',
        establishDate: '2018-05-15',
        companyType: '有限责任公司',
        businessScope: '五金制品、模具制造、机械加工',
        address: '广州市白云区工业大道168号',
        contactPerson: '李经理',
        contactPhone: '13800138001',
        contactEmail: 'liming@xinwang.com',
        mainProducts: '压铸件、精密五金件、模具',
        annualRevenue: '1000-5000',
        certificateTypes: ['ISO9001'],
        username: 'xinwang2024',
        status: 'pending',
        submittedAt: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000).toISOString(),
      },
      {
        id: 'REG002',
        companyName: '深圳市精诚模具制造有限公司',
        unifiedCreditCode: '91440300MA59XXX456',
        legalPerson: '王芳',
        registeredCapital: '1000',
        establishDate: '2015-08-20',
        companyType: '有限责任公司',
        businessScope: '模具设计制造、五金塑胶件生产',
        address: '深圳市宝安区沙井街道工业园',
        contactPerson: '陈主管',
        contactPhone: '13900139001',
        contactEmail: 'sales@jingcheng.com',
        mainProducts: '压铸模具、注塑模具、模具配件',
        annualRevenue: '5000以上',
        certificateTypes: ['ISO9001', 'ISO14001', 'IATF16949'],
        username: 'jingcheng',
        status: 'approved',
        submittedAt: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000).toISOString(),
        reviewedAt: new Date(Date.now() - 4 * 24 * 60 * 60 * 1000).toISOString(),
        reviewNote: '资质齐全，符合要求',
      },
      {
        id: 'REG003',
        companyName: '中山市火炬区精密机械厂',
        unifiedCreditCode: '91440200MA59XXX789',
        legalPerson: '刘强',
        registeredCapital: '800',
        establishDate: '2016-03-10',
        companyType: '有限责任公司',
        businessScope: '精密机械加工、数控加工、自动化设备',
        address: '中山市火炬开发区兴业路56号',
        contactPerson: '赵工',
        contactPhone: '13700137003',
        contactEmail: 'zhaogong@huoju.com',
        mainProducts: 'CNC加工件、精密零件、自动化设备',
        annualRevenue: '1000-5000',
        certificateTypes: ['ISO9001'],
        username: 'huoju2024',
        status: 'pending',
        submittedAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000).toISOString(),
      },
      {
        id: 'REG004',
        companyName: '东莞市横沥镇电子材料厂',
        unifiedCreditCode: '91441900MA59XXX234',
        legalPerson: '孙丽',
        registeredCapital: '300',
        establishDate: '2019-07-22',
        companyType: '有限责任公司',
        businessScope: '电子材料、绝缘材料、包装材料',
        address: '东莞市横沥镇新城路234号',
        contactPerson: '周主管',
        contactPhone: '13600136004',
        contactEmail: 'zhou@hengelec.com',
        mainProducts: '绝缘板、电子包装、防静电材料',
        annualRevenue: '500-1000',
        certificateTypes: ['ISO9001', 'UL认证'],
        username: 'hengelec',
        status: 'pending',
        submittedAt: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000).toISOString(),
      },
      {
        id: 'REG005',
        companyName: '佛山市顺德区塑胶制品厂',
        unifiedCreditCode: '91440606MA59XXX567',
        legalPerson: '郑明',
        registeredCapital: '600',
        establishDate: '2017-11-05',
        companyType: '有限责任公司',
        businessScope: '塑料制品、橡胶制品、模具制造',
        address: '佛山市顺德区伦教街道工业路88号',
        contactPerson: '吴经理',
        contactPhone: '13500135005',
        contactEmail: 'wumgr@shundeplastic.com',
        mainProducts: '塑料手柄、橡胶配件、注塑件',
        annualRevenue: '1000-5000',
        certificateTypes: ['ISO9001', 'ISO14001'],
        username: 'shundeplastic',
        status: 'rejected',
        submittedAt: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString(),
        reviewedAt: new Date(Date.now() - 6 * 24 * 60 * 60 * 1000).toISOString(),
        reviewNote: '资质不全，需要补充环评报告',
      },
      {
        id: 'REG006',
        companyName: '惠州市惠阳区不锈钢制品厂',
        unifiedCreditCode: '91441300MA59XXX890',
        legalPerson: '钱华',
        registeredCapital: '400',
        establishDate: '2020-02-15',
        companyType: '有限责任公司',
        businessScope: '不锈钢制品、厨具制造、金属加工',
        address: '惠州市惠阳区淡水镇工业大道123号',
        contactPerson: '冯工',
        contactPhone: '13800138006',
        contactEmail: 'feng@huizhou-ss.com',
        mainProducts: '不锈钢锅具、厨房用品、五金件',
        annualRevenue: '500-1000',
        certificateTypes: ['ISO9001'],
        username: 'huizhou-ss',
        status: 'pending',
        submittedAt: new Date(Date.now() - 4 * 24 * 60 * 60 * 1000).toISOString(),
      },
      {
        id: 'REG007',
        companyName: '江门市蓬江区粉末冶金厂',
        unifiedCreditCode: '91440700MA59XXX345',
        legalPerson: '陈建',
        registeredCapital: '700',
        establishDate: '2014-09-18',
        companyType: '有限责任公司',
        businessScope: '粉末冶金、金属注射成型、精密零件',
        address: '江门市蓬江区杜阮镇工业区56号',
        contactPerson: '韩经理',
        contactPhone: '13900139007',
        contactEmail: 'han@pmpowder.com',
        mainProducts: '粉末冶金件、金属注射件、齿轮',
        annualRevenue: '1000-5000',
        certificateTypes: ['ISO9001', 'IATF16949'],
        username: 'pmpowder',
        status: 'pending',
        submittedAt: new Date(Date.now() - 6 * 24 * 60 * 60 * 1000).toISOString(),
      },
      {
        id: 'REG008',
        companyName: '珠海市香洲区热处理加工厂',
        unifiedCreditCode: '91440400MA59XXX678',
        legalPerson: '曹强',
        registeredCapital: '350',
        establishDate: '2019-04-28',
        companyType: '有限责任公司',
        businessScope: '金属热处理、表面处理、精加工',
        address: '珠海市香洲区南屏镇工业园西区8号',
        contactPerson: '许主管',
        contactPhone: '13700137008',
        contactEmail: 'xu@zhuhai-heat.com',
        mainProducts: '热处理加工、淬火、回火、渗碳',
        annualRevenue: '500-1000',
        certificateTypes: ['ISO9001'],
        username: 'zhuhai-heat',
        status: 'pending',
        submittedAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000).toISOString(),
      },
      {
        id: 'REG009',
        companyName: '肇庆市端州区铝材厂',
        unifiedCreditCode: '91441200MA59XXX012',
        legalPerson: '魏明',
        registeredCapital: '900',
        establishDate: '2013-06-30',
        companyType: '有限责任公司',
        businessScope: '铝材生产、铝型材、铝制品',
        address: '肇庆市端州区端州一路工业区',
        contactPerson: '丁工',
        contactPhone: '13600136009',
        contactEmail: 'ding@zhaoqing-al.com',
        mainProducts: '工业铝型材、建筑铝材、铝制品加工',
        annualRevenue: '5000以上',
        certificateTypes: ['ISO9001', 'ISO14001'],
        username: 'zhaoqing-al',
        status: 'approved',
        submittedAt: new Date(Date.now() - 8 * 24 * 60 * 60 * 1000).toISOString(),
        reviewedAt: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString(),
        reviewNote: '资质齐全，产能充足',
      },
      {
        id: 'REG010',
        companyName: '汕头市龙湖区涂装设备厂',
        unifiedCreditCode: '91440500MA59XXX445',
        legalPerson: '蒋伟',
        registeredCapital: '550',
        establishDate: '2017-12-12',
        companyType: '有限责任公司',
        businessScope: '涂装设备、表面处理设备、环保设备',
        address: '汕头市龙湖区珠池路工业二区',
        contactPerson: '沈经理',
        contactPhone: '13500135010',
        contactEmail: 'shen@shantou-coat.com',
        mainProducts: '喷涂线、烘箱、涂装设备',
        annualRevenue: '1000-5000',
        certificateTypes: ['ISO9001'],
        username: 'shantou-coat',
        status: 'pending',
        submittedAt: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000).toISOString(),
      },
    ]
    localStorage.setItem('supplier_registrations', JSON.stringify(registrations.value))
  }
})

// 按状态筛选
const statusFilter = ref<'all' | 'pending' | 'approved' | 'rejected'>('all')

const filteredRegistrations = computed(() => {
  if (statusFilter.value === 'all') return registrations.value
  return registrations.value.filter(r => r.status === statusFilter.value)
})

// 统计数据
const stats = computed(() => ({
  total: registrations.value.length,
  pending: registrations.value.filter(r => r.status === 'pending').length,
  approved: registrations.value.filter(r => r.status === 'approved').length,
  rejected: registrations.value.filter(r => r.status === 'rejected').length,
}))

// 获取状态徽章样式
function getStatusBadgeClass(status: string) {
  switch (status) {
    case 'pending': return 'bg-yellow-100 text-yellow-800'
    case 'approved': return 'bg-green-100 text-green-800'
    case 'rejected': return 'bg-red-100 text-red-800'
    default: return 'bg-gray-100 text-gray-800'
  }
}

// 获取状态文本
function getStatusText(status: string) {
  switch (status) {
    case 'pending': return '待审核'
    case 'approved': return '已通过'
    case 'rejected': return '已拒绝'
    default: return status
  }
}

// 打开详情对话框
function openDetailDialog(registration: Registration) {
  selectedRegistration.value = registration
  reviewForm.value = {
    action: 'approve',
    note: '',
  }
  isDetailDialogOpen.value = true
}

// 处理审核
function handleReview() {
  if (!selectedRegistration.value) return

  const idx = registrations.value.findIndex(r => r.id === selectedRegistration.value!.id)
  if (idx === -1) return

  // 更新注册状态
  registrations.value[idx].status = reviewForm.value.action === 'approve' ? 'approved' : 'rejected'
  registrations.value[idx].reviewedAt = new Date().toISOString()
  registrations.value[idx].reviewNote = reviewForm.value.note

  // 如果通过审核，创建供应商账号
  if (reviewForm.value.action === 'approve') {
    const reg = registrations.value[idx]
    const newSupplier: Supplier = {
      id: `S${String(mockSuppliers.length + 1).padStart(3, '0')}`,
      code: `SUP${Date.now().toString().slice(-6)}`,
      name: reg.companyName,
      category: '常规',
      status: '合格',
      riskLevel: '低',
      riskScore: 25,
      contact: reg.contactPerson,
      phone: reg.contactPhone,
      email: reg.contactEmail,
      address: reg.address,
      certifications: reg.certificateTypes.map(cert => ({
        id: `CERT${Date.now()}`,
        name: cert,
        validUntil: new Date(Date.now() + 365 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
        status: '有效'
      })),
      overallScore: 85,
      qualityScore: 85,
      deliveryScore: 85,
      costScore: 85,
      innovationScore: 85,
      annualSpend: 0,
      orderCount: 0,
      onTimeRate: 100,
      qualityPassRate: 100,
    }

    mockSuppliers.push(newSupplier)
    localStorage.setItem('suppliers', JSON.stringify(mockSuppliers))
  }

  // 保存更新
  localStorage.setItem('supplier_registrations', JSON.stringify(registrations.value))

  appStore.showToast(reviewForm.value.action === 'approve' ? '注册申请已通过' : '注册申请已拒绝', 'success')
  isDetailDialogOpen.value = false
  selectedRegistration.value = null
}

// 删除注册记录
function handleDeleteRegistration(id: string) {
  if (!confirm('确定要删除此注册记录吗？')) return
  registrations.value = registrations.value.filter(r => r.id !== id)
  localStorage.setItem('supplier_registrations', JSON.stringify(registrations.value))
  appStore.showToast('注册记录已删除', 'success')
}
</script>

<template>
  <div class="space-y-6">
    <!-- 标题 -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-semibold text-gray-900">供应商注册审核</h1>
        <p class="text-sm text-gray-500 mt-1">审核供应商自助注册申请</p>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">总申请数</p>
            <p class="text-2xl font-semibold text-gray-900 mt-1">{{ stats.total }}</p>
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
            <p class="text-2xl font-semibold text-yellow-600 mt-1">{{ stats.pending }}</p>
          </div>
          <div class="w-12 h-12 bg-yellow-100 rounded-lg flex items-center justify-center">
            <AlertCircle class="w-6 h-6 text-yellow-600" />
          </div>
        </div>
      </div>
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">已通过</p>
            <p class="text-2xl font-semibold text-green-600 mt-1">{{ stats.approved }}</p>
          </div>
          <div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
            <CheckCircle class="w-6 h-6 text-green-600" />
          </div>
        </div>
      </div>
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">已拒绝</p>
            <p class="text-2xl font-semibold text-red-600 mt-1">{{ stats.rejected }}</p>
          </div>
          <div class="w-12 h-12 bg-red-100 rounded-lg flex items-center justify-center">
            <XCircle class="w-6 h-6 text-red-600" />
          </div>
        </div>
      </div>
    </div>

    <!-- 筛选器 -->
    <div class="bg-white rounded-lg border border-gray-200 p-4">
      <div class="flex items-center gap-4">
        <label class="text-sm font-medium text-gray-700">状态筛选：</label>
        <div class="flex gap-2">
          <button
            @click="statusFilter = 'all'"
            class="px-3 py-1.5 rounded-lg text-sm"
            :class="statusFilter === 'all' ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'"
          >
            全部
          </button>
          <button
            @click="statusFilter = 'pending'"
            class="px-3 py-1.5 rounded-lg text-sm"
            :class="statusFilter === 'pending' ? 'bg-yellow-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'"
          >
            待审核
          </button>
          <button
            @click="statusFilter = 'approved'"
            class="px-3 py-1.5 rounded-lg text-sm"
            :class="statusFilter === 'approved' ? 'bg-green-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'"
          >
            已通过
          </button>
          <button
            @click="statusFilter = 'rejected'"
            class="px-3 py-1.5 rounded-lg text-sm"
            :class="statusFilter === 'rejected' ? 'bg-red-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'"
          >
            已拒绝
          </button>
        </div>
      </div>
    </div>

    <!-- 注册列表 -->
    <div class="bg-white rounded-lg border border-gray-200">
      <div v-if="filteredRegistrations.length === 0" class="text-center py-12 text-gray-500">
        <UserCheck class="w-12 h-12 mx-auto mb-3 opacity-50" />
        <p>暂无注册申请</p>
      </div>
      <div v-else class="divide-y divide-gray-200">
        <div v-for="reg in filteredRegistrations" :key="reg.id" class="p-6 hover:bg-gray-50">
          <div class="flex items-start justify-between">
            <div class="flex-1">
              <div class="flex items-center gap-3 mb-3">
                <h3 class="text-lg font-semibold text-gray-900">{{ reg.companyName }}</h3>
                <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="getStatusBadgeClass(reg.status)">
                  {{ getStatusText(reg.status) }}
                </span>
              </div>
              <div class="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm">
                <div>
                  <p class="text-gray-500">统一社会信用代码</p>
                  <p class="font-medium text-gray-900">{{ reg.unifiedCreditCode }}</p>
                </div>
                <div>
                  <p class="text-gray-500">法定代表人</p>
                  <p class="font-medium text-gray-900">{{ reg.legalPerson }}</p>
                </div>
                <div>
                  <p class="text-gray-500">企业类型</p>
                  <p class="font-medium text-gray-900">{{ reg.companyType }}</p>
                </div>
                <div>
                  <p class="text-gray-500">业务联系人</p>
                  <p class="font-medium text-gray-900">{{ reg.contactPerson }}</p>
                </div>
                <div>
                  <p class="text-gray-500">联系电话</p>
                  <p class="font-medium text-gray-900">{{ reg.contactPhone }}</p>
                </div>
                <div>
                  <p class="text-gray-500">联系邮箱</p>
                  <p class="font-medium text-gray-900">{{ reg.contactEmail }}</p>
                </div>
              </div>
              <div class="mt-3 text-sm text-gray-500">
                <p>主营产品：{{ reg.mainProducts }}</p>
                <p class="mt-1">申请时间：{{ new Date(reg.submittedAt).toLocaleString('zh-CN') }}</p>
                <p v-if="reg.reviewedAt" class="mt-1">审核时间：{{ new Date(reg.reviewedAt).toLocaleString('zh-CN') }}</p>
                <p v-if="reg.reviewNote" class="mt-1">审核备注：{{ reg.reviewNote }}</p>
              </div>
            </div>
            <div class="flex items-center gap-2 ml-4">
              <button
                @click="openDetailDialog(reg)"
                class="flex items-center gap-1 px-3 py-1.5 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-100"
              >
                <Eye class="w-4 h-4" />详情
              </button>
              <button
                v-if="reg.status === 'pending'"
                @click="openDetailDialog(reg)"
                class="flex items-center gap-1 px-3 py-1.5 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700"
              >
                <UserCheck class="w-4 h-4" />审核
              </button>
              <button
                @click="handleDeleteRegistration(reg.id)"
                class="flex items-center gap-1 px-3 py-1.5 border border-red-300 rounded-lg text-sm text-red-700 hover:bg-red-50"
              >
                删除
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 审核对话框 -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="isDetailDialogOpen && selectedRegistration" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-black/50" @click="isDetailDialogOpen = false"></div>
          <div class="relative bg-white rounded-xl shadow-2xl w-full max-w-3xl max-h-[80vh] overflow-y-auto">
            <div class="p-6">
              <div class="flex items-center justify-between mb-6">
                <h3 class="text-lg font-semibold text-gray-900">注册申请详情</h3>
                <button @click="isDetailDialogOpen = false" class="p-2 hover:bg-gray-100 rounded-lg">
                  <XCircle class="w-5 h-5 text-gray-400" />
                </button>
              </div>

              <!-- 基本信息 -->
              <div class="space-y-6">
                <div>
                  <h4 class="text-base font-medium text-gray-900 mb-3">基本信息</h4>
                  <div class="grid grid-cols-2 gap-4 text-sm">
                    <div><p class="text-gray-500">企业名称</p><p class="font-medium text-gray-900">{{ selectedRegistration.companyName }}</p></div>
                    <div><p class="text-gray-500">统一社会信用代码</p><p class="font-medium text-gray-900">{{ selectedRegistration.unifiedCreditCode }}</p></div>
                    <div><p class="text-gray-500">法定代表人</p><p class="font-medium text-gray-900">{{ selectedRegistration.legalPerson }}</p></div>
                    <div><p class="text-gray-500">注册资本</p><p class="font-medium text-gray-900">{{ selectedRegistration.registeredCapital }} 万元</p></div>
                    <div><p class="text-gray-500">成立日期</p><p class="font-medium text-gray-900">{{ selectedRegistration.establishDate }}</p></div>
                    <div><p class="text-gray-500">企业类型</p><p class="font-medium text-gray-900">{{ selectedRegistration.companyType }}</p></div>
                    <div class="col-span-2"><p class="text-gray-500">企业地址</p><p class="font-medium text-gray-900">{{ selectedRegistration.address }}</p></div>
                    <div class="col-span-2"><p class="text-gray-500">经营范围</p><p class="font-medium text-gray-900">{{ selectedRegistration.businessScope }}</p></div>
                  </div>
                </div>

                <div>
                  <h4 class="text-base font-medium text-gray-900 mb-3">业务信息</h4>
                  <div class="grid grid-cols-2 gap-4 text-sm">
                    <div><p class="text-gray-500">业务联系人</p><p class="font-medium text-gray-900">{{ selectedRegistration.contactPerson }}</p></div>
                    <div><p class="text-gray-500">联系电话</p><p class="font-medium text-gray-900">{{ selectedRegistration.contactPhone }}</p></div>
                    <div><p class="text-gray-500">联系邮箱</p><p class="font-medium text-gray-900">{{ selectedRegistration.contactEmail }}</p></div>
                    <div><p class="text-gray-500">年营业额</p><p class="font-medium text-gray-900">{{ selectedRegistration.annualRevenue }}</p></div>
                    <div class="col-span-2"><p class="text-gray-500">主营产品/服务</p><p class="font-medium text-gray-900">{{ selectedRegistration.mainProducts }}</p></div>
                    <div class="col-span-2">
                      <p class="text-gray-500">已获得认证</p>
                      <div class="flex flex-wrap gap-2 mt-1">
                        <span v-for="cert in selectedRegistration.certificateTypes" :key="cert" class="px-2 py-1 bg-blue-100 text-blue-800 rounded text-xs">
                          {{ cert }}
                        </span>
                        <span v-if="selectedRegistration.certificateTypes.length === 0" class="text-gray-500">暂无认证</span>
                      </div>
                    </div>
                  </div>
                </div>

                <div v-if="selectedRegistration.status === 'pending'">
                  <h4 class="text-base font-medium text-gray-900 mb-3">审核操作</h4>
                  <div class="space-y-4">
                    <div class="flex gap-4">
                      <label class="flex items-center gap-2 cursor-pointer">
                        <input type="radio" v-model="reviewForm.action" value="approve" class="w-4 h-4 text-blue-600">
                        <span class="text-sm text-gray-700">通过审核，创建供应商账号</span>
                      </label>
                      <label class="flex items-center gap-2 cursor-pointer">
                        <input type="radio" v-model="reviewForm.action" value="reject" class="w-4 h-4 text-red-600">
                        <span class="text-sm text-gray-700">拒绝申请</span>
                      </label>
                    </div>
                    <div>
                      <label class="block text-sm font-medium text-gray-700 mb-1">审核备注</label>
                      <textarea v-model="reviewForm.note" rows="3" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" placeholder="请输入审核意见（可选）"></textarea>
                    </div>
                  </div>
                </div>

                <div v-if="selectedRegistration.status !== 'pending'">
                  <div class="p-4 rounded-lg" :class="selectedRegistration.status === 'approved' ? 'bg-green-50' : 'bg-red-50'">
                    <p class="text-sm font-medium" :class="selectedRegistration.status === 'approved' ? 'text-green-900' : 'text-red-900'">
                      {{ selectedRegistration.status === 'approved' ? '✓ 已通过审核' : '✗ 已拒绝申请' }}
                    </p>
                    <p v-if="selectedRegistration.reviewNote" class="text-sm mt-1" :class="selectedRegistration.status === 'approved' ? 'text-green-800' : 'text-red-800'">
                      备注：{{ selectedRegistration.reviewNote }}
                    </p>
                    <p class="text-xs mt-2 text-gray-500">审核时间：{{ selectedRegistration.reviewedAt ? new Date(selectedRegistration.reviewedAt).toLocaleString('zh-CN') : '' }}</p>
                  </div>
                </div>
              </div>

              <div class="flex justify-end gap-3 mt-6 pt-6 border-t">
                <button @click="isDetailDialogOpen = false" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">
                  {{ selectedRegistration.status === 'pending' ? '取消' : '关闭' }}
                </button>
                <button
                  v-if="selectedRegistration.status === 'pending'"
                  @click="handleReview"
                  class="px-4 py-2 rounded-lg text-sm text-white"
                  :class="reviewForm.action === 'approve' ? 'bg-green-600 hover:bg-green-700' : 'bg-red-600 hover:bg-red-700'"
                >
                  {{ reviewForm.action === 'approve' ? '通过审核' : '拒绝申请' }}
                </button>
              </div>
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
