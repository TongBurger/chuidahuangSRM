<script setup lang="ts">
import { RouterView, RouterLink, useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/useAppStore'
import NotificationCenter from '@/components/NotificationCenter.vue'
import {
  LayoutDashboard,
  FileText,
  Package,
  Truck,
  Receipt,
  TrendingUp,
  Calculator,
  UserCircle,
  Menu,
  X,
  Building2,
  Languages,
  Sun,
  Moon,
  LogOut,
  Warehouse,
} from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

const supplierNavigation = [
  { key: 'supplier.portal.home', href: '/supplier-portal', icon: LayoutDashboard },
  { key: 'supplier.portal.orders', href: '/supplier-portal/orders', icon: FileText },
  { key: 'supplier.portal.asn', href: '/supplier-portal/asn', icon: Truck },
  { key: 'supplier.portal.inventory', href: '/supplier-portal/inventory', icon: Warehouse },
  { key: 'supplier.portal.reconciliation', href: '/supplier-portal/reconciliation', icon: Calculator },
  { key: 'supplier.portal.invoice', href: '/supplier-portal/invoice', icon: Receipt },
  { key: 'supplier.portal.performance', href: '/supplier-portal/performance', icon: TrendingUp },
  { key: 'supplier.portal.profile', href: '/supplier-portal/profile', icon: UserCircle },
]

function isActive(href: string): boolean {
  if (href === '/supplier-portal') return route.path === '/supplier-portal'
  return route.path.startsWith(href)
}

function handleLogout() {
  appStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="min-h-screen bg-gray-50 text-gray-900 dark:bg-gray-900 dark:text-gray-100">
    <!-- 移动端侧边栏遮罩 -->
    <Transition name="fade">
      <div
        v-if="appStore.sidebarOpen"
        class="fixed inset-0 bg-black/50 z-40 lg:hidden"
        @click="appStore.setSidebarOpen(false)"
      />
    </Transition>

    <!-- 侧边栏 -->
    <aside
      class="fixed top-0 left-0 z-50 h-screen w-64 bg-gradient-to-b from-blue-600 to-blue-800 transition-transform duration-300 lg:translate-x-0"
      :class="appStore.sidebarOpen ? 'translate-x-0' : '-translate-x-full'"
    >
      <div class="flex items-center justify-between h-16 px-6 border-b border-blue-500/50">
        <div class="flex items-center gap-3">
          <div class="w-8 h-8 bg-white rounded-lg flex items-center justify-center">
            <span class="text-blue-600 font-bold text-sm">供</span>
          </div>
          <span class="font-semibold text-white">供应商门户</span>
        </div>
        <button
          class="lg:hidden p-2 hover:bg-blue-700/50 rounded-lg text-white"
          @click="appStore.setSidebarOpen(false)"
        >
          <X class="w-5 h-5" />
        </button>
      </div>

      <!-- 供应商信息 -->
      <div class="p-4 border-b border-blue-500/30">
        <div class="flex items-center gap-3 p-3 bg-white/10 rounded-lg backdrop-blur-sm">
          <div class="w-10 h-10 bg-white rounded-full flex items-center justify-center">
            <span class="text-blue-600 font-semibold">{{ appStore.currentUser.avatar }}</span>
          </div>
          <div class="flex-1 min-w-0">
            <p class="text-sm font-medium text-white truncate">{{ appStore.currentUser.companyName || appStore.currentUser.name }}</p>
            <p class="text-xs text-blue-100">{{ appStore.currentUser.role }}</p>
          </div>
        </div>
      </div>

      <nav class="p-4 space-y-1 overflow-y-auto h-[calc(100vh-8rem)]">
        <RouterLink
          v-for="item in supplierNavigation"
          :key="item.key"
          :to="item.href"
          class="flex items-center gap-3 px-4 py-3 rounded-lg transition-all duration-200"
          :class="
            isActive(item.href)
              ? 'bg-white text-blue-700 shadow-md font-medium'
              : 'text-white/90 hover:bg-white/10'
          "
          @click="appStore.setSidebarOpen(false)"
        >
          <component :is="item.icon" class="w-5 h-5 flex-shrink-0" />
          <span>{{ appStore.t(item.key) || item.key }}</span>
        </RouterLink>
      </nav>
    </aside>

    <!-- 主内容区 -->
    <div class="lg:pl-64">
      <!-- 顶部栏 -->
      <header
        class="sticky top-0 z-30 h-16 bg-white border-b border-gray-200 flex items-center justify-between px-6 dark:bg-gray-950 dark:border-gray-800"
      >
        <button
          class="lg:hidden p-2 hover:bg-gray-100 rounded-lg dark:hover:bg-gray-800"
          @click="appStore.setSidebarOpen(true)"
        >
          <Menu class="w-5 h-5" />
        </button>

        <div class="flex items-center gap-4 ml-auto">
          <!-- 通知中心 -->
          <NotificationCenter />
          <button
            class="flex items-center gap-2 px-3 py-2 text-sm border border-gray-300 rounded-lg hover:bg-gray-100 dark:border-gray-700 dark:hover:bg-gray-800"
            @click="appStore.toggleLanguage()"
          >
            <Languages class="w-4 h-4" />
            <span>{{ appStore.language === 'zh-CN' ? appStore.t('lang.zh') : appStore.t('lang.en') }}</span>
          </button>
          <button
            class="flex items-center gap-2 px-3 py-2 text-sm border border-gray-300 rounded-lg hover:bg-gray-100 dark:border-gray-700 dark:hover:bg-gray-800"
            @click="appStore.toggleTheme()"
          >
            <Sun v-if="appStore.theme === 'light'" class="w-4 h-4" />
            <Moon v-else class="w-4 h-4" />
            <span>{{ appStore.theme === 'light' ? appStore.t('theme.light') : appStore.t('theme.dark') }}</span>
          </button>
          <div class="text-right hidden sm:block">
            <div class="text-sm text-gray-900 dark:text-gray-100">
              {{ appStore.currentUser.companyName || appStore.currentUser.name }}
            </div>
            <div class="text-xs text-gray-500 dark:text-gray-400">{{ appStore.currentUser.role }}</div>
          </div>
          <div class="flex items-center gap-2">
            <div
              class="w-10 h-10 bg-gradient-to-br from-blue-600 to-blue-800 rounded-full flex items-center justify-center cursor-pointer"
              :title="appStore.currentUser.name"
            >
              <span class="text-white text-sm">{{ appStore.currentUser.avatar }}</span>
            </div>
            <button
              class="p-2 text-gray-600 hover:bg-gray-100 rounded-lg dark:text-gray-400 dark:hover:bg-gray-800"
              title="退出登录"
              @click="handleLogout"
            >
              <LogOut class="w-5 h-5" />
            </button>
          </div>
        </div>
      </header>

      <!-- 页面内容 -->
      <main class="p-6">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
