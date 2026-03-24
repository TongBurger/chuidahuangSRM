  <script setup lang="ts">
  import { RouterView, RouterLink, useRoute } from 'vue-router'
  import { useAppStore } from '@/stores/useAppStore'
  import NotificationCenter from '@/components/NotificationCenter.vue'
  import {
    LayoutDashboard,
    Users,
    ShoppingCart,
    Package,
    Warehouse,
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
    AlertTriangle,
    CheckCircle,
    XCircle,
    AlertCircle,
    Info,
    UserPlus,
  } from 'lucide-vue-next'

  const route = useRoute()
  const appStore = useAppStore()

  const navigation = [
    { key: 'nav.dashboard', href: '/', icon: LayoutDashboard },
    { key: 'nav.suppliers', href: '/suppliers', icon: Users },
    { key: 'nav.orders', href: '/orders', icon: ShoppingCart },
    { key: 'nav.molds', href: '/molds', icon: Package },
    { key: 'nav.warehouse', href: '/warehouse', icon: Warehouse },
    { key: 'nav.sourcing', href: '/sourcing', icon: TrendingUp },
    { key: 'nav.finance', href: '/finance', icon: Calculator },
    { key: 'nav.riskAlerts', href: '/risk-alerts', icon: AlertTriangle },
    { key: 'nav.users', href: '/users', icon: UserCircle },
    { key: 'nav.supplierRegistrations', href: '/supplier-registrations', icon: UserPlus },
    { key: 'nav.portal', href: '/supplier-portal', icon: Building2 },
  ]

  function isActive(href: string): boolean {
    if (href === '/') return route.path === '/'
    return route.path.startsWith(href)
  }

  function handleLogout() {
    appStore.logout()
    window.location.href = '/login'
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
        class="fixed top-0 left-0 z-50 h-screen w-64 bg-white border-r border-gray-200 transition-transform duration-300 lg:translate-x-0 dark:bg-gray-950 dark:border-gray-800"
        :class="appStore.sidebarOpen ? 'translate-x-0' : '-translate-x-full'"
      >
        <div class="flex items-center justify-between h-16 px-6 border-b border-gray-200 dark:border-gray-800">
          <div class="flex items-center gap-3">
            <div
              class="w-8 h-8 bg-gradient-to-br from-blue-600 to-blue-800 rounded-lg flex items-center justify-center"
            >
              <span class="text-white font-bold text-sm">炊</span>
            </div>
            <span class="font-semibold text-gray-900 dark:text-gray-100">{{ appStore.t('system.name') }}</span>
          </div>
          <button
            class="lg:hidden p-2 hover:bg-gray-100 rounded-lg dark:hover:bg-gray-800"
            @click="appStore.setSidebarOpen(false)"
          >
            <X class="w-5 h-5" />
          </button>
        </div>

        <nav class="p-4 space-y-1 overflow-y-auto h-[calc(100vh-4rem)]">
          <RouterLink
            v-for="item in navigation"
            :key="item.key"
            :to="item.href"
            class="flex items-center gap-3 px-4 py-3 rounded-lg transition-colors"
            :class="
              isActive(item.href)
                ? 'bg-blue-50 text-blue-700 dark:bg-blue-900/30 dark:text-blue-300'
                : 'text-gray-700 hover:bg-gray-100 dark:text-gray-200 dark:hover:bg-gray-800'
            "
            @click="appStore.setSidebarOpen(false)"
          >
            <component :is="item.icon" class="w-5 h-5 flex-shrink-0" />
            <span>{{ appStore.t(item.key) }}</span>
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
                {{ appStore.currentUser.role }} - {{ appStore.currentUser.name }}
              </div>
              <div class="text-xs text-gray-500 dark:text-gray-400">{{ appStore.currentUser.department }}</div>
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

      <!-- Toast 通知容器 -->
      <Teleport to="body">
        <div class="fixed top-4 right-4 z-[100] flex flex-col gap-2">
          <TransitionGroup name="toast">
            <div
              v-for="toast in appStore.toasts"
              :key="toast.id"
              class="flex items-center gap-3 px-4 py-3 rounded-lg shadow-lg min-w-[300px] max-w-md"
              :class="{
                'bg-green-50 border border-green-200': toast.type === 'success',
                'bg-red-50 border border-red-200': toast.type === 'error',
                'bg-yellow-50 border border-yellow-200': toast.type === 'warning',
                'bg-blue-50 border border-blue-200': toast.type === 'info',
              }"
            >
              <component
                :is="toast.type === 'success' ? CheckCircle : toast.type === 'error' ? XCircle : toast.type === 'warning' ? AlertCircle : Info"
                class="w-5 h-5 flex-shrink-0"
                :class="{
                  'text-green-600': toast.type === 'success',
                  'text-red-600': toast.type === 'error',
                  'text-yellow-600': toast.type === 'warning',
                  'text-blue-600': toast.type === 'info',
                }"
              />
              <span
                class="text-sm flex-1"
                :class="{
                  'text-green-800': toast.type === 'success',
                  'text-red-800': toast.type === 'error',
                  'text-yellow-800': toast.type === 'warning',
                  'text-blue-800': toast.type === 'info',
                }"
              >
                {{ toast.message }}
              </span>
              <button
                @click="appStore.removeToast(toast.id)"
                class="flex-shrink-0 p-1 hover:bg-black/5 rounded"
                :class="{
                  'text-green-600 hover:bg-green-100': toast.type === 'success',
                  'text-red-600 hover:bg-red-100': toast.type === 'error',
                  'text-yellow-600 hover:bg-yellow-100': toast.type === 'warning',
                  'text-blue-600 hover:bg-blue-100': toast.type === 'info',
                }"
              >
                <X class="w-4 h-4" />
              </button>
            </div>
          </TransitionGroup>
        </div>
      </Teleport>
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

  /* Toast 动画 */
  .toast-enter-active,
  .toast-leave-active {
    transition: all 0.3s ease;
  }
  .toast-enter-from {
    opacity: 0;
    transform: translateX(100%);
  }
  .toast-leave-to {
    opacity: 0;
    transform: translateX(100%);
  }
  .toast-move {
    transition: transform 0.3s ease;
  }
  </style>
