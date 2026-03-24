<script setup lang="ts">
import { ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { UserPlus, Upload, Check } from 'lucide-vue-next'
import { useAppStore } from '@/stores/useAppStore'

const router = useRouter()
const appStore = useAppStore()

const step = ref(1)
const showToast = ref(false)
const toastMessage = ref('')
const toastType = ref<'success' | 'error'>('success')

const formData = ref({
  companyName: '',
  unifiedCreditCode: '',
  legalPerson: '',
  registeredCapital: '',
  establishDate: '',
  companyType: '',
  businessScope: '',
  address: '',
  contactPerson: '',
  contactPhone: '',
  contactEmail: '',
  mainProducts: '',
  annualRevenue: '',
  certificateTypes: [] as string[],
  username: '',
  password: '',
  confirmPassword: '',
  agreeToTerms: false,
})

const uploadedFiles = ref({
  businessLicense: false,
  taxCertificate: false,
  bankAccount: false,
})

// 文件存储
const fileData = ref({
  businessLicense: null as File | null,
  taxCertificate: null as File | null,
  bankAccount: null as File | null,
})

function showNotification(msg: string, type: 'success' | 'error') {
  toastMessage.value = msg
  toastType.value = type
  showToast.value = true
  setTimeout(() => { showToast.value = false }, 3000)
}

function handleFileUpload(fileType: keyof typeof uploadedFiles.value) {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = '.pdf,.jpg,.jpeg,.png'

  input.onchange = (e) => {
    const target = e.target as HTMLInputElement
    if (target.files && target.files[0]) {
      const file = target.files[0]

      // 验证文件大小
      if (file.size > 5 * 1024 * 1024) {
        showNotification('文件大小不能超过5MB', 'error')
        return
      }

      fileData.value[fileType] = file
      uploadedFiles.value[fileType] = true

      const labels: Record<string, string> = {
        businessLicense: '营业执照',
        taxCertificate: '税务登记证',
        bankAccount: '银行开户许可证',
      }
      showNotification(`${labels[fileType]}上传成功`, 'success')
    }
  }

  input.click()
}

function handleSubmit() {
  // 验证第一步
  if (!formData.value.companyName?.trim()) {
    showNotification('请输入企业名称', 'error')
    step.value = 1
    return
  }
  if (!formData.value.unifiedCreditCode?.trim()) {
    showNotification('请输入统一社会信用代码', 'error')
    step.value = 1
    return
  }
  if (!formData.value.legalPerson?.trim()) {
    showNotification('请输入法定代表人', 'error')
    step.value = 1
    return
  }
  if (!formData.value.companyType) {
    showNotification('请选择企业类型', 'error')
    step.value = 1
    return
  }

  // 验证第二步
  if (!uploadedFiles.value.businessLicense || !uploadedFiles.value.taxCertificate || !uploadedFiles.value.bankAccount) {
    showNotification('请上传所有必需的资质证明文件', 'error')
    step.value = 2
    return
  }

  // 验证第三步
  if (!formData.value.contactPerson?.trim()) {
    showNotification('请输入业务联系人', 'error')
    step.value = 3
    return
  }
  if (!formData.value.contactPhone?.trim()) {
    showNotification('请输入联系电话', 'error')
    step.value = 3
    return
  }
  if (!formData.value.contactEmail?.trim()) {
    showNotification('请输入联系邮箱', 'error')
    step.value = 3
    return
  }
  if (!formData.value.mainProducts?.trim()) {
    showNotification('请输入主营产品/服务', 'error')
    step.value = 3
    return
  }

  // 验证第四步
  if (!formData.value.username?.trim()) {
    showNotification('请设置登录账号', 'error')
    step.value = 4
    return
  }
  if (formData.value.username.length < 6) {
    showNotification('登录账号至少6位', 'error')
    step.value = 4
    return
  }
  if (!formData.value.password?.trim()) {
    showNotification('请设置登录密码', 'error')
    step.value = 4
    return
  }
  if (formData.value.password.length < 8) {
    showNotification('密码至少8位', 'error')
    step.value = 4
    return
  }
  if (formData.value.password !== formData.value.confirmPassword) {
    showNotification('两次输入的密码不一致', 'error')
    step.value = 4
    return
  }
  if (!formData.value.agreeToTerms) {
    showNotification('请阅读并同意服务协议', 'error')
    step.value = 4
    return
  }

  // 保存注册数据到 localStorage
  const registration = {
    id: `REG${Date.now()}`,
    ...formData.value,
    certificateTypes: [...formData.value.certificateTypes],
    status: 'pending',
    submittedAt: new Date().toISOString(),
    reviewedAt: null,
    reviewNote: '',
  }

  // 保存到 supplier_registrations
  const saved = localStorage.getItem('supplier_registrations')
  let registrations = saved ? JSON.parse(saved) : []
  registrations.push(registration)
  localStorage.setItem('supplier_registrations', JSON.stringify(registrations))

  showNotification('注册申请已提交，我们将在3个工作日内完成审核', 'success')

  // 3秒后跳转到登录页
  setTimeout(() => {
    router.push('/')
  }, 3000)
}

function toggleCert(cert: string) {
  const idx = formData.value.certificateTypes.indexOf(cert)
  if (idx === -1) formData.value.certificateTypes.push(cert)
  else formData.value.certificateTypes.splice(idx, 1)
}

const steps = [
  { num: 1, label: '基本信息' },
  { num: 2, label: '资质证明' },
  { num: 3, label: '业务信息' },
  { num: 4, label: '账号设置' },
]

const certs = ['ISO9001', 'ISO14001', 'IATF16949', 'ISO45001', 'CCC认证', '其他认证']
</script>

<template>
  <div class="min-h-screen bg-gray-50 py-12 px-4">
    <!-- Toast 提示 -->
    <Transition name="slide-down">
      <div v-if="showToast" class="fixed top-4 right-4 z-50 px-4 py-3 rounded-lg shadow-lg text-sm font-medium" :class="toastType === 'success' ? 'bg-green-600 text-white' : 'bg-red-600 text-white'">
        {{ toastMessage }}
      </div>
    </Transition>

    <div class="max-w-4xl mx-auto">
      <!-- 头部 -->
      <div class="text-center mb-8">
        <div class="flex items-center justify-center gap-3 mb-4">
          <div class="w-12 h-12 bg-gradient-to-br from-blue-600 to-blue-800 rounded-lg flex items-center justify-center">
            <span class="text-white text-xl font-bold">炊</span>
          </div>
          <h1 class="text-3xl font-bold text-gray-900">炊大皇供应商注册</h1>
        </div>
        <p class="text-gray-600">欢迎加入炊大皇供应商生态系统</p>
      </div>

      <!-- 进度条 -->
      <div class="mb-8">
        <div class="flex items-center justify-between">
          <template v-for="(item, idx) in steps" :key="item.num">
            <div class="flex flex-col items-center flex-1">
              <div class="w-10 h-10 rounded-full flex items-center justify-center font-semibold" :class="step >= item.num ? 'bg-blue-600 text-white' : 'bg-gray-200 text-gray-600'">
                <Check v-if="step > item.num" class="w-5 h-5" />
                <span v-else>{{ item.num }}</span>
              </div>
              <span class="text-xs text-gray-600 mt-2">{{ item.label }}</span>
            </div>
            <div v-if="idx < 3" class="flex-1 h-1" :class="step > item.num ? 'bg-blue-600' : 'bg-gray-200'" />
          </template>
        </div>
      </div>

      <!-- 表单卡片 -->
      <div class="bg-white rounded-xl border border-gray-200 p-8">
        <h2 class="text-lg font-semibold text-gray-900 mb-6">
          <span v-if="step === 1">第一步：填写基本信息</span>
          <span v-if="step === 2">第二步：上传资质证明</span>
          <span v-if="step === 3">第三步：填写业务信息</span>
          <span v-if="step === 4">第四步：设置登录账号</span>
        </h2>

        <!-- 第一步：基本信息 -->
        <div v-if="step === 1" class="space-y-4">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div><label class="block text-sm font-medium text-gray-700 mb-1">企业名称 *</label><input v-model="formData.companyName" placeholder="请输入企业全称" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" /></div>
            <div><label class="block text-sm font-medium text-gray-700 mb-1">统一社会信用代码 *</label><input v-model="formData.unifiedCreditCode" placeholder="请输入18位统一社会信用代码" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" /></div>
          </div>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div><label class="block text-sm font-medium text-gray-700 mb-1">法定代表人 *</label><input v-model="formData.legalPerson" placeholder="请输入法定代表人姓名" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" /></div>
            <div><label class="block text-sm font-medium text-gray-700 mb-1">注册资本（万元）*</label><input v-model.number="formData.registeredCapital" type="number" placeholder="请输入注册资本" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" /></div>
          </div>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div><label class="block text-sm font-medium text-gray-700 mb-1">成立日期 *</label><input v-model="formData.establishDate" type="date" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" /></div>
            <div><label class="block text-sm font-medium text-gray-700 mb-1">企业类型 *</label>
              <select v-model="formData.companyType" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
                <option value="">请选择企业类型</option>
                <option value="有限责任公司">有限责任公司</option>
                <option value="股份有限公司">股份有限公司</option>
                <option value="个体工商户">个体工商户</option>
                <option value="合伙企业">合伙企业</option>
              </select>
            </div>
          </div>
          <div><label class="block text-sm font-medium text-gray-700 mb-1">企业地址 *</label><input v-model="formData.address" placeholder="请输入企业详细地址" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" /></div>
          <div><label class="block text-sm font-medium text-gray-700 mb-1">经营范围</label><input v-model="formData.businessScope" placeholder="请输入经营范围" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" /></div>
        </div>

        <!-- 第二步：资质证明 -->
        <div v-if="step === 2" class="space-y-4">
          <div class="p-4 bg-blue-50 rounded-lg mb-4">
            <p class="text-sm text-blue-800">请上传企业相关资质证明文件，支持 PDF、JPG、PNG 格式，单个文件不超过 5MB</p>
          </div>
          <div v-for="file in [{ key: 'businessLicense', label: '营业执照', desc: '请上传营业执照扫描件或照片' }, { key: 'taxCertificate', label: '税务登记证', desc: '请上传税务登记证扫描件或照片' }, { key: 'bankAccount', label: '银行开户许可证', desc: '请上传银行开户许可证扫描件' }]" :key="file.key" class="border-2 border-dashed border-gray-300 rounded-lg p-6 hover:border-blue-400 transition-colors">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-4">
                <Upload class="w-8 h-8 text-gray-400" />
                <div>
                  <p class="font-medium text-gray-900">{{ file.label }} *</p>
                  <p class="text-sm text-gray-500">{{ file.desc }}</p>
                </div>
              </div>
              <div v-if="(uploadedFiles as any)[file.key]" class="flex items-center gap-2 text-green-600">
                <Check class="w-5 h-5" /><span class="text-sm">{{ (fileData as any)[file.key]?.name || '已上传' }}</span>
              </div>
              <button v-else class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50" @click="handleFileUpload(file.key as any)">选择文件</button>
            </div>
          </div>
        </div>

        <!-- 第三步：业务信息 -->
        <div v-if="step === 3" class="space-y-4">
          <div><label class="block text-sm font-medium text-gray-700 mb-1">业务联系人 *</label><input v-model="formData.contactPerson" placeholder="请输入业务联系人姓名" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" /></div>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div><label class="block text-sm font-medium text-gray-700 mb-1">联系电话 *</label><input v-model="formData.contactPhone" placeholder="请输入联系电话" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" /></div>
            <div><label class="block text-sm font-medium text-gray-700 mb-1">联系邮箱 *</label><input v-model="formData.contactEmail" type="email" placeholder="请输入联系邮箱" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" /></div>
          </div>
          <div><label class="block text-sm font-medium text-gray-700 mb-1">主营产品/服务 *</label><input v-model="formData.mainProducts" placeholder="请简要描述您的主营产品或服务" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" /></div>
          <div><label class="block text-sm font-medium text-gray-700 mb-1">年营业额（万元）</label>
            <select v-model="formData.annualRevenue" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
              <option value="">请选择年营业额范围</option>
              <option value="100以下">100万以下</option>
              <option value="100-500">100-500万</option>
              <option value="500-1000">500-1000万</option>
              <option value="1000-5000">1000-5000万</option>
              <option value="5000以上">5000万以上</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">已获得的认证（可多选）</label>
            <div class="grid grid-cols-2 gap-3">
              <div v-for="cert in certs" :key="cert" class="flex items-center gap-2 cursor-pointer" @click="toggleCert(cert)">
                <div class="w-4 h-4 border-2 rounded flex items-center justify-center" :class="formData.certificateTypes.includes(cert) ? 'bg-blue-600 border-blue-600' : 'border-gray-300'">
                  <Check v-if="formData.certificateTypes.includes(cert)" class="w-3 h-3 text-white" />
                </div>
                <label class="text-sm text-gray-700 cursor-pointer">{{ cert }}</label>
              </div>
            </div>
          </div>
        </div>

        <!-- 第四步：账号设置 -->
        <div v-if="step === 4" class="space-y-4">
          <div class="p-4 bg-blue-50 rounded-lg mb-4">
            <p class="text-sm text-blue-800">请设置供应商门户的登录账号，账号审核通过后即可使用</p>
          </div>
          <div><label class="block text-sm font-medium text-gray-700 mb-1">登录账号 *</label><input v-model="formData.username" placeholder="请设置登录账号（6-20位字母或数字）" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" /></div>
          <div><label class="block text-sm font-medium text-gray-700 mb-1">登录密码 *</label><input v-model="formData.password" type="password" placeholder="请设置登录密码（至少8位，包含字母和数字）" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" /></div>
          <div><label class="block text-sm font-medium text-gray-700 mb-1">确认密码 *</label><input v-model="formData.confirmPassword" type="password" placeholder="请再次输入密码" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" /></div>
          <div class="pt-4 border-t">
            <div class="flex items-start gap-2 cursor-pointer" @click="formData.agreeToTerms = !formData.agreeToTerms">
              <div class="w-4 h-4 border-2 rounded flex items-center justify-center mt-0.5 flex-shrink-0" :class="formData.agreeToTerms ? 'bg-blue-600 border-blue-600' : 'border-gray-300'">
                <Check v-if="formData.agreeToTerms" class="w-3 h-3 text-white" />
              </div>
              <label class="text-sm text-gray-700 cursor-pointer">
                我已阅读并同意 <a href="#" class="text-blue-600 hover:underline">《供应商服务协议》</a> 和 <a href="#" class="text-blue-600 hover:underline">《隐私政策》</a>
              </label>
            </div>
          </div>
          <div class="p-4 bg-green-50 rounded-lg">
            <h4 class="font-medium text-green-900 mb-2">注册完成后您将获得：</h4>
            <ul class="space-y-1 text-sm text-green-800">
              <li>• 专属的供应商门户账号</li>
              <li>• 在线订单管理和确认</li>
              <li>• ASN发货通知和物流追踪</li>
              <li>• 自动对账和发票管理</li>
              <li>• 绩效评分查看和改进建议</li>
            </ul>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="flex justify-between pt-6 mt-6 border-t">
          <div>
            <button v-if="step > 1" @click="step--" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">上一步</button>
          </div>
          <div class="flex gap-2">
            <RouterLink to="/">
              <button class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">返回首页</button>
            </RouterLink>
            <button v-if="step < 4" @click="step++" class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">下一步</button>
            <button v-else @click="handleSubmit" class="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">
              <UserPlus class="w-4 h-4" />提交注册
            </button>
          </div>
        </div>
      </div>

      <!-- 帮助信息 -->
      <div class="mt-8 text-center text-sm text-gray-600">
        <p>已有账号？ <RouterLink to="/" class="text-blue-600 hover:underline">立即登录</RouterLink></p>
        <p class="mt-2">如有疑问，请联系我们：400-123-4567 或 <a href="mailto:supplier@chudahuang.com" class="text-blue-600 hover:underline">supplier@chudahuang.com</a></p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.slide-down-enter-active, .slide-down-leave-active {
  transition: all 0.3s ease;
}
.slide-down-enter-from, .slide-down-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
