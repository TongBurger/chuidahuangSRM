<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Building2, User, Phone, Mail, MapPin, Edit, CheckCircle, X } from 'lucide-vue-next'
import { useAppStore } from '@/stores/useAppStore'

const appStore = useAppStore()
const supplierId = 'S001'

const isEditing = ref(false)
const isPasswordDialogOpen = ref(false)

const supplierInfo = ref({
  name: '深圳市精诚模具制造有限公司',
  code: 'SUP001',
  legalPerson: '张伟',
  contactPerson: '张伟',
  phone: '13800138000',
  email: 'zhangwei@jingcheng.com',
  address: '深圳市宝安区西乡街道',
  businessScope: '模具设计、制造、销售',
  establishedDate: '2010-05-15',
  registeredCapital: '5000万',
})

// 修改密码表单
const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const passwordErrors = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: '',
})

// 加载企业信息
onMounted(() => {
  const saved = localStorage.getItem('supplier_profile')
  if (saved) {
    try {
      const profile = JSON.parse(saved)
      if (profile.supplierId === supplierId) {
        supplierInfo.value = { ...supplierInfo.value, ...profile }
      }
    } catch {
      // 使用默认值
    }
  }
})

function handleSave() {
  // 保存到 localStorage
  const profileToSave = {
    ...supplierInfo.value,
    supplierId,
    updatedAt: new Date().toISOString(),
  }
  localStorage.setItem('supplier_profile', JSON.stringify(profileToSave))
  isEditing.value = false
  appStore.showToast('企业信息已更新', 'success')
}

function handleCancel() {
  // 重新加载数据
  const saved = localStorage.getItem('supplier_profile')
  if (saved) {
    try {
      const profile = JSON.parse(saved)
      if (profile.supplierId === supplierId) {
        supplierInfo.value = { ...supplierInfo.value, ...profile }
      }
    } catch {
      // 使用默认值
    }
  }
  isEditing.value = false
}

// 打开修改密码对话框
function openPasswordDialog() {
  passwordForm.value = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  }
  passwordErrors.value = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  }
  isPasswordDialogOpen.value = true
}

// 验证密码表单
function validatePasswordForm() {
  let isValid = true
  passwordErrors.value = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  }

  if (!passwordForm.value.currentPassword) {
    passwordErrors.value.currentPassword = '请输入当前密码'
    isValid = false
  }

  if (!passwordForm.value.newPassword) {
    passwordErrors.value.newPassword = '请输入新密码'
    isValid = false
  } else if (passwordForm.value.newPassword.length < 6) {
    passwordErrors.value.newPassword = '密码长度不能少于6位'
    isValid = false
  }

  if (!passwordForm.value.confirmPassword) {
    passwordErrors.value.confirmPassword = '请确认新密码'
    isValid = false
  } else if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    passwordErrors.value.confirmPassword = '两次输入的密码不一致'
    isValid = false
  }

  return isValid
}

// 修改密码
function handleChangePassword() {
  if (!validatePasswordForm()) {
    return
  }

  // 这里应该调用API验证当前密码并修改
  // 模拟成功
  appStore.showToast('密码修改成功', 'success')
  isPasswordDialogOpen.value = false
}
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-semibold text-gray-900">企业信息</h1>
        <p class="text-sm text-gray-500 mt-1">查看和编辑企业基本信息</p>
      </div>
      <div class="flex gap-2">
        <button v-if="!isEditing" @click="openPasswordDialog" class="flex items-center gap-2 px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">
          修改密码
        </button>
        <button v-if="!isEditing" @click="isEditing = true" class="flex items-center gap-2 px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">
          <Edit class="w-4 h-4" />编辑信息
        </button>
        <div v-else class="flex gap-2">
          <button @click="handleCancel" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">取消</button>
          <button @click="handleSave" class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">保存</button>
        </div>
      </div>
    </div>

    <!-- 基本信息卡片 -->
    <div class="bg-white rounded-lg border border-gray-200 p-6">
      <div class="flex items-center gap-2 mb-4">
        <Building2 class="w-5 h-5 text-blue-600" />
        <h3 class="text-lg font-semibold text-gray-900">基本信息</h3>
      </div>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">企业名称</label>
          <input v-if="isEditing" v-model="supplierInfo.name" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
          <p v-else class="text-gray-900">{{ supplierInfo.name }}</p>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">供应商编码</label>
          <p class="text-gray-900">{{ supplierInfo.code }}</p>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">法定代表人</label>
          <input v-if="isEditing" v-model="supplierInfo.legalPerson" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
          <p v-else class="text-gray-900">{{ supplierInfo.legalPerson }}</p>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">成立日期</label>
          <p class="text-gray-900">{{ supplierInfo.establishedDate }}</p>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">注册资本</label>
          <input v-if="isEditing" v-model="supplierInfo.registeredCapital" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
          <p v-else class="text-gray-900">{{ supplierInfo.registeredCapital }}</p>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">经营范围</label>
          <input v-if="isEditing" v-model="supplierInfo.businessScope" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
          <p v-else class="text-gray-900">{{ supplierInfo.businessScope }}</p>
        </div>
      </div>
    </div>

    <!-- 联系信息卡片 -->
    <div class="bg-white rounded-lg border border-gray-200 p-6">
      <div class="flex items-center gap-2 mb-4">
        <User class="w-5 h-5 text-blue-600" />
        <h3 class="text-lg font-semibold text-gray-900">联系信息</h3>
      </div>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">业务联系人</label>
          <input v-if="isEditing" v-model="supplierInfo.contactPerson" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
          <p v-else class="text-gray-900">{{ supplierInfo.contactPerson }}</p>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">联系电话</label>
          <div class="flex items-center gap-2">
            <Phone class="w-4 h-4 text-gray-500" />
            <input v-if="isEditing" v-model="supplierInfo.phone" class="flex-1 px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
            <p v-else class="text-gray-900">{{ supplierInfo.phone }}</p>
          </div>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">联系邮箱</label>
          <div class="flex items-center gap-2">
            <Mail class="w-4 h-4 text-gray-500" />
            <input v-if="isEditing" v-model="supplierInfo.email" class="flex-1 px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
            <p v-else class="text-gray-900">{{ supplierInfo.email }}</p>
          </div>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">企业地址</label>
          <div class="flex items-center gap-2">
            <MapPin class="w-4 h-4 text-gray-500" />
            <input v-if="isEditing" v-model="supplierInfo.address" class="flex-1 px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
            <p v-else class="text-gray-900">{{ supplierInfo.address }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 资质认证 -->
    <div class="bg-white rounded-lg border border-gray-200 p-6">
      <div class="flex items-center gap-2 mb-4">
        <CheckCircle class="w-5 h-5 text-blue-600" />
        <h3 class="text-lg font-semibold text-gray-900">资质认证</h3>
      </div>
      <div class="space-y-3">
        <div class="flex items-center justify-between p-3 bg-green-50 rounded-lg">
          <div>
            <p class="font-medium text-gray-900">ISO9001质量管理体系</p>
            <p class="text-sm text-gray-600">有效期至: 2026-12-31</p>
          </div>
          <span class="px-2 py-1 bg-green-600 text-white rounded-full text-xs">有效</span>
        </div>
        <div class="flex items-center justify-between p-3 bg-green-50 rounded-lg">
          <div>
            <p class="font-medium text-gray-900">ISO14001环境管理体系</p>
            <p class="text-sm text-gray-600">有效期至: 2026-06-30</p>
          </div>
          <span class="px-2 py-1 bg-green-600 text-white rounded-full text-xs">有效</span>
        </div>
        <div class="flex items-center justify-between p-3 bg-orange-50 rounded-lg">
          <div>
            <p class="font-medium text-gray-900">IATF16949汽车质量管理</p>
            <p class="text-sm text-gray-600">有效期至: 2025-05-15</p>
          </div>
          <span class="px-2 py-1 bg-orange-600 text-white rounded-full text-xs">即将到期</span>
        </div>
      </div>
    </div>

    <!-- 修改密码对话框 -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="isPasswordDialogOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-black/50" @click="isPasswordDialogOpen = false" />
          <div class="relative bg-white rounded-xl shadow-xl w-full max-w-md">
            <div class="flex items-center justify-between p-4 border-b">
              <h3 class="text-lg font-semibold text-gray-900">修改密码</h3>
              <button @click="isPasswordDialogOpen = false" class="p-2 hover:bg-gray-100 rounded-lg">
                <X class="w-5 h-5" />
              </button>
            </div>
            <div class="p-6 space-y-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">当前密码</label>
                <input v-model="passwordForm.currentPassword" type="password" placeholder="请输入当前密码" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                <p v-if="passwordErrors.currentPassword" class="text-red-600 text-xs mt-1">{{ passwordErrors.currentPassword }}</p>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">新密码</label>
                <input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码（至少6位）" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                <p v-if="passwordErrors.newPassword" class="text-red-600 text-xs mt-1">{{ passwordErrors.newPassword }}</p>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">确认新密码</label>
                <input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                <p v-if="passwordErrors.confirmPassword" class="text-red-600 text-xs mt-1">{{ passwordErrors.confirmPassword }}</p>
              </div>
            </div>
            <div class="flex justify-end gap-3 p-4 border-t bg-gray-50">
              <button @click="isPasswordDialogOpen = false" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">取消</button>
              <button @click="handleChangePassword" class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">确认修改</button>
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
