<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAppStore } from '@/stores/useAppStore'
import { Lock, User, LogIn, Eye, EyeOff } from 'lucide-vue-next'

const router = useRouter()
const appStore = useAppStore()

const loginForm = reactive({
  username: '',
  password: '',
})

const loading = ref(false)
const errorMessage = ref('')
const showPassword = ref(false)

async function handleLogin() {
  if (!loginForm.username || !loginForm.password) {
    errorMessage.value = '请输入用户名和密码'
    return
  }

  loading.value = true
  errorMessage.value = ''

  // Demo 测试账号
  const demoAccounts = [
    // 内部用户账号
    { username: 'admin', password: '123456', userId: 'U001', name: '张三', role: '采购员', department: '采购部', avatar: '张', type: 'internal' },
    { username: 'zhangsan', password: '123456', userId: 'U002', name: '李四', role: '质检员', department: '质量部', avatar: '李', type: 'internal' },
    { username: 'lisi', password: '123456', userId: 'U003', name: '王五', role: '仓管员', department: '仓储部', avatar: '王', type: 'internal' },
    // 供应商账号
    { username: 'supplier001', password: '123456', userId: 'S001', name: '深圳市精诚模具制造有限公司', companyName: '深圳市精诚模具制造有限公司', role: '供应商业务员', department: '供应商', avatar: '精', type: 'supplier', supplierId: 'S001' },
    { username: 'supplier002', password: '123456', userId: 'S002', name: '东莞市华泰五金制品有限公司', companyName: '东莞市华泰五金制品有限公司', role: '供应商业务员', department: '供应商', avatar: '华', type: 'supplier', supplierId: 'S002' },
    { username: 'supplier003', password: '123456', userId: 'S003', name: '佛山市永盛铝制品有限公司', companyName: '佛山市永盛铝制品有限公司', role: '供应商业务员', department: '供应商', avatar: '永', type: 'supplier', supplierId: 'S003' },
  ]

  // 查找匹配的账号
  const account = demoAccounts.find(acc => acc.username === loginForm.username && acc.password === loginForm.password)

  if (account) {
    // 保存模拟的Token和用户信息
    const token = `demo-token-${account.userId}-${Date.now()}`
    localStorage.setItem('auth-token', token)
    localStorage.setItem('user-info', JSON.stringify(account))

    // 更新store
    appStore.login({
      id: account.userId,
      name: account.name,
      role: account.role,
      department: account.department,
      avatar: account.avatar,
    })

    // 根据用户类型跳转
    if (account.type === 'supplier') {
      router.push('/supplier-portal')
    } else {
      router.push('/')
    }
    return
  }

  // 如果不是Demo账号，尝试调用后端API
  try {
    const response = await fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(loginForm),
    })

    const data = await response.json()

    if (data.code === 200) {
      // 保存Token和用户信息
      localStorage.setItem('auth-token', data.data.token)
      localStorage.setItem('user-info', JSON.stringify(data.data))

      // 更新store
      appStore.login({
        id: String(data.data.userId),
        name: data.data.name,
        role: data.data.role,
        department: data.data.department,
        avatar: data.data.avatar,
      })

      // 跳转到首页
      router.push('/')
    } else {
      errorMessage.value = data.message || '登录失败，请检查用户名和密码'
    }
  } catch (error) {
    errorMessage.value = '用户名或密码错误（提示：Demo模式请使用测试账号）'
  } finally {
    loading.value = false
  }
}

function handleKeyPress(event: KeyboardEvent) {
  if (event.key === 'Enter') {
    handleLogin()
  }
}
</script>

<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 to-blue-100 dark:from-gray-900 dark:to-gray-800 flex items-center justify-center p-4">
    <!-- 背景装饰 -->
    <div class="absolute inset-0 overflow-hidden pointer-events-none">
      <div class="absolute -top-40 -right-40 w-80 h-80 bg-blue-400 rounded-full mix-blend-multiply filter blur-xl opacity-20 animate-blob"></div>
      <div class="absolute -bottom-40 -left-40 w-80 h-80 bg-blue-600 rounded-full mix-blend-multiply filter blur-xl opacity-20 animate-blob animation-delay-2000"></div>
      <div class="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-80 h-80 bg-blue-300 rounded-full mix-blend-multiply filter blur-xl opacity-20 animate-blob animation-delay-4000"></div>
    </div>

    <!-- 登录卡片 -->
    <div class="relative w-full max-w-md">
      <!-- Logo和标题 -->
      <div class="text-center mb-8">
        <div class="inline-flex items-center justify-center w-20 h-20 bg-gradient-to-br from-blue-600 to-blue-800 rounded-2xl shadow-lg mb-4">
          <span class="text-white font-bold text-3xl">炊</span>
        </div>
        <h1 class="text-3xl font-bold text-gray-900 dark:text-gray-100 mb-2">
          {{ appStore.t('system.name') }}
        </h1>
        <p class="text-gray-600 dark:text-gray-400">供应商关系管理系统</p>
      </div>

      <!-- 登录表单 -->
      <div class="bg-white dark:bg-gray-950 rounded-2xl shadow-xl p-8">
        <form @submit.prevent="handleLogin" class="space-y-6">
          <!-- 用户名输入 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              用户名
            </label>
            <div class="relative">
              <User class="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input
                v-model="loginForm.username"
                type="text"
                class="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-900 dark:border-gray-700 dark:text-gray-100 transition-all"
                placeholder="请输入用户名"
                @keypress="handleKeyPress"
                autofocus
              />
            </div>
          </div>

          <!-- 密码输入 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              密码
            </label>
            <div class="relative">
              <Lock class="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input
                v-model="loginForm.password"
                :type="showPassword ? 'text' : 'password'"
                class="w-full pl-10 pr-12 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-900 dark:border-gray-700 dark:text-gray-100 transition-all"
                placeholder="请输入密码"
                @keypress="handleKeyPress"
              />
              <button
                type="button"
                @click="showPassword = !showPassword"
                class="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 transition-colors"
              >
                <Eye v-if="!showPassword" class="w-5 h-5" />
                <EyeOff v-else class="w-5 h-5" />
              </button>
            </div>
          </div>

          <!-- 错误提示 -->
          <div v-if="errorMessage" class="p-3 bg-red-50 border border-red-200 rounded-lg dark:bg-red-900/20 dark:border-red-800">
            <p class="text-sm text-red-600 dark:text-red-400">{{ errorMessage }}</p>
          </div>

          <!-- 登录按钮 -->
          <button
            type="submit"
            :disabled="loading"
            class="w-full flex items-center justify-center gap-2 px-4 py-3 bg-gradient-to-r from-blue-600 to-blue-800 text-white font-medium rounded-lg hover:from-blue-700 hover:to-blue-900 focus:ring-4 focus:ring-blue-300 disabled:opacity-50 disabled:cursor-not-allowed transition-all shadow-lg"
          >
            <LogIn class="w-5 h-5" />
            <span>{{ loading ? '登录中...' : '登录' }}</span>
          </button>
        </form>
      </div>

      <!-- 底部信息 -->
      <div class="mt-6 text-center text-sm text-gray-500 dark:text-gray-400">
        <p>© 2026 炊大皇. All rights reserved.</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
@keyframes blob {
  0% {
    transform: translate(0px, 0px) scale(1);
  }
  33% {
    transform: translate(30px, -50px) scale(1.1);
  }
  66% {
    transform: translate(-20px, 20px) scale(0.9);
  }
  100% {
    transform: translate(0px, 0px) scale(1);
  }
}
.animate-blob {
  animation: blob 7s infinite;
}
.animation-delay-2000 {
  animation-delay: 2s;
}
.animation-delay-4000 {
  animation-delay: 4s;
}
</style>
