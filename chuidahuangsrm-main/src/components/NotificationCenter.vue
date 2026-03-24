<script setup lang="ts">
import { ref } from 'vue'
import { Bell, X, Check, Trash2, Clock, Package, FileText, DollarSign, AlertTriangle, Award, CheckCircle } from 'lucide-vue-next'
import { useNotificationsStore } from '@/stores/notifications'
import { useRouter } from 'vue-router'

const router = useRouter()
const notificationsStore = useNotificationsStore()
const isOpen = ref(false)

function getNotificationIcon(type: string) {
  switch (type) {
    case 'order': return Package
    case 'asn': return Package
    case 'invoice': return FileText
    case 'reconciliation': return DollarSign
    case 'mold': return Award
    case 'supplier': return CheckCircle
    case 'system': return AlertTriangle
    default: return Bell
  }
}

function getNotificationColor(type: string) {
  switch (type) {
    case 'order': return 'bg-blue-100 text-blue-600'
    case 'asn': return 'bg-purple-100 text-purple-600'
    case 'invoice': return 'bg-green-100 text-green-600'
    case 'reconciliation': return 'bg-yellow-100 text-yellow-600'
    case 'mold': return 'bg-orange-100 text-orange-600'
    case 'supplier': return 'bg-teal-100 text-teal-600'
    case 'system': return 'bg-red-100 text-red-600'
    default: return 'bg-gray-100 text-gray-600'
  }
}

function handleNotificationClick(notification: any) {
  notificationsStore.markAsRead(notification.id)
  if (notification.link) {
    router.push(notification.link)
  }
}

function handleMarkAllAsRead() {
  notificationsStore.markAllAsRead()
}

function handleDeleteNotification(id: string) {
  notificationsStore.deleteNotification(id)
}

function handleClearRead() {
  notificationsStore.clearRead()
}
</script>

<template>
  <div class="relative">
    <!-- 通知图标按钮 -->
    <button @click="isOpen = !isOpen" class="relative p-2 rounded-lg hover:bg-gray-100 transition-colors">
      <Bell class="w-5 h-5 text-gray-600" />
      <span v-if="notificationsStore.unreadCount > 0" class="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
        {{ notificationsStore.unreadCount > 9 ? '9+' : notificationsStore.unreadCount }}
      </span>
    </button>

    <!-- 通知下拉面板 -->
    <Teleport to="body">
      <Transition name="dropdown">
        <div v-if="isOpen" class="fixed right-4 mt-12 w-96 bg-white rounded-xl shadow-2xl border border-gray-200 z-50 max-h-[80vh] flex flex-col">
          <!-- 头部 -->
          <div class="flex items-center justify-between p-4 border-b">
            <h3 class="text-lg font-semibold text-gray-900">消息通知</h3>
            <div class="flex items-center gap-2">
              <button v-if="notificationsStore.unreadCount > 0" @click="handleMarkAllAsRead" class="text-sm text-blue-600 hover:text-blue-700 flex items-center gap-1">
                <Check class="w-4 h-4" />全部已读
              </button>
              <button @click="isOpen = false" class="p-1 hover:bg-gray-100 rounded-lg">
                <X class="w-5 h-5 text-gray-400" />
              </button>
            </div>
          </div>

          <!-- 通知列表 -->
          <div class="flex-1 overflow-y-auto">
            <div v-if="notificationsStore.notifications.length === 0" class="text-center py-8 text-gray-500">
              <Bell class="w-12 h-12 mx-auto mb-3 opacity-50" />
              <p>暂无通知</p>
            </div>
            <div v-else class="divide-y divide-gray-100">
              <div
                v-for="notification in notificationsStore.notifications.slice(0, 50)"
                :key="notification.id"
                @click="handleNotificationClick(notification)"
                class="p-4 hover:bg-gray-50 cursor-pointer transition-colors"
                :class="{ 'bg-blue-50': !notification.isRead }"
              >
                <div class="flex gap-3">
                  <!-- 图标 -->
                  <div class="w-10 h-10 rounded-full flex items-center justify-center flex-shrink-0" :class="getNotificationColor(notification.type)">
                    <component :is="getNotificationIcon(notification.type)" class="w-5 h-5" />
                  </div>

                  <!-- 内容 -->
                  <div class="flex-1 min-w-0">
                    <div class="flex items-start justify-between gap-2">
                      <h4 class="text-sm font-medium text-gray-900" :class="{ 'font-semibold': !notification.isRead }">
                        {{ notification.title }}
                      </h4>
                      <span class="text-xs text-gray-500 flex items-center gap-1">
                        <Clock class="w-3 h-3" />
                        {{ notification.createdAt }}
                      </span>
                    </div>
                    <p class="text-sm text-gray-600 mt-1">{{ notification.message }}</p>
                  </div>

                  <!-- 操作 -->
                  <button
                    @click.stop="handleDeleteNotification(notification.id)"
                    class="flex-shrink-0 p-1 hover:bg-red-50 rounded-lg text-gray-400 hover:text-red-600"
                  >
                    <X class="w-4 h-4" />
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- 底部操作 -->
          <div v-if="notificationsStore.notifications.length > 0" class="p-3 border-t bg-gray-50 flex items-center justify-between">
            <button @click="handleClearRead" class="text-sm text-gray-600 hover:text-gray-900 flex items-center gap-1">
              <Trash2 class="w-4 h-4" />清空已读
            </button>
            <button @click="isOpen = false" class="text-sm text-blue-600 hover:text-blue-700">关闭</button>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<style scoped>
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.2s ease;
}
.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
