import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface Notification {
  id: string
  type: 'order' | 'asn' | 'invoice' | 'reconciliation' | 'mold' | 'supplier' | 'system'
  title: string
  message: string
  link?: string
  isRead: boolean
  createdAt: string
}

export const useNotificationsStore = defineStore('notifications', () => {
  const notifications = ref<Notification[]>([])

  // 初始化通知数据
  function loadNotifications() {
    const saved = localStorage.getItem('notifications')
    if (saved) {
      try {
        notifications.value = JSON.parse(saved)
      } catch {
        notifications.value = []
      }
    }
  }

  // 自动加载通知
  loadNotifications()

  // 获取未读通知数量
  const unreadCount = computed(() => notifications.value.filter(n => !n.isRead).length)

  // 添加通知
  function addNotification(notification: Omit<Notification, 'id' | 'isRead' | 'createdAt'>) {
    const newNotification: Notification = {
      id: `N${Date.now()}`,
      isRead: false,
      createdAt: new Date().toISOString(),
      ...notification,
    }
    notifications.value.unshift(newNotification)
    saveNotifications()
  }

  // 标记为已读
  function markAsRead(id: string) {
    const notification = notifications.value.find(n => n.id === id)
    if (notification) {
      notification.isRead = true
      saveNotifications()
    }
  }

  // 标记全部为已读
  function markAllAsRead() {
    notifications.value.forEach(n => n.isRead = true)
    saveNotifications()
  }

  // 删除通知
  function deleteNotification(id: string) {
    notifications.value = notifications.value.filter(n => n.id !== id)
    saveNotifications()
  }

  // 清空已读
  function clearRead() {
    notifications.value = notifications.value.filter(n => !n.isRead)
    saveNotifications()
  }

  // 保存到localStorage
  function saveNotifications() {
    localStorage.setItem('notifications', JSON.stringify(notifications.value))
  }

  return {
    notifications,
    unreadCount,
    loadNotifications,
    addNotification,
    markAsRead,
    markAllAsRead,
    deleteNotification,
    clearRead,
  }
})
