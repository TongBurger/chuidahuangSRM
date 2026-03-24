<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Upload, Plus, X, FileText, AlertTriangle } from 'lucide-vue-next'
import { useAppStore } from '@/stores/useAppStore'

const appStore = useAppStore()
const supplierId = 'S001'
const supplierName = '深圳市精诚模具制造有限公司'

// 对话框状态
const isUploadDialogOpen = ref(false)

// 上传表单
const uploadForm = ref({
  materialCode: '',
  materialName: '',
  quantity: 0,
  unit: '个',
  uploadDate: new Date().toISOString().split('T')[0],
  notes: '',
})

// 库存数据
const inventories = ref<any[]>([])

// Mock库存数据
const mockInventories = [
  { id: 'INV001', materialCode: 'MAT001', materialName: '电机外壳', quantity: 5000, unit: '个', uploadDate: '2026-03-20', status: '已确认' },
  { id: 'INV002', materialCode: 'MAT002', materialName: '端盖组件', quantity: 8000, unit: '件', uploadDate: '2026-03-19', status: '已确认' },
  { id: 'INV003', materialCode: 'MAT003', materialName: '散热片', quantity: 12000, unit: '片', uploadDate: '2026-03-18', status: '待确认' },
  { id: 'INV004', materialCode: 'MAT004', materialName: '外壳组件', quantity: 3000, unit: '套', uploadDate: '2026-03-17', status: '已确认' },
  { id: 'INV005', materialCode: 'MAT005', materialName: '底座组件', quantity: 2500, unit: '个', uploadDate: '2026-03-15', status: '已确认' },
]

onMounted(() => {
  const saved = localStorage.getItem('supplier_inventories')
  if (saved) {
    try {
      const allInventories = JSON.parse(saved)
      inventories.value = allInventories.filter((i: any) => i.supplierId === supplierId)
    } catch {
      inventories.value = mockInventories
    }
  } else {
    inventories.value = mockInventories
  }
})

function getStatusClass(status: string) {
  switch (status) {
    case '已确认': return 'bg-green-100 text-green-800'
    case '待确认': return 'bg-orange-100 text-orange-800'
    case '已驳回': return 'bg-red-100 text-red-800'
    default: return 'bg-gray-100 text-gray-700'
  }
}

function openUploadDialog() {
  uploadForm.value = {
    materialCode: '',
    materialName: '',
    quantity: 0,
    unit: '个',
    uploadDate: new Date().toISOString().split('T')[0],
    notes: '',
  }
  isUploadDialogOpen.value = true
}

function handleUpload() {
  if (!uploadForm.value.materialCode.trim()) {
    appStore.showToast('请输入物料编码', 'warning')
    return
  }
  if (!uploadForm.value.materialName.trim()) {
    appStore.showToast('请输入物料名称', 'warning')
    return
  }
  if (uploadForm.value.quantity <= 0) {
    appStore.showToast('请输入有效的数量', 'warning')
    return
  }

  const inventory = {
    id: `INV${Date.now()}`,
    supplierId,
    supplierName,
    materialCode: uploadForm.value.materialCode,
    materialName: uploadForm.value.materialName,
    quantity: uploadForm.value.quantity,
    unit: uploadForm.value.unit,
    uploadDate: uploadForm.value.uploadDate,
    status: '待确认',
    notes: uploadForm.value.notes,
  }

  // 保存到 localStorage
  const saved = localStorage.getItem('supplier_inventories')
  let allInventories = saved ? JSON.parse(saved) : []
  allInventories.push(inventory)
  localStorage.setItem('supplier_inventories', JSON.stringify(allInventories))

  // 更新本地数据
  inventories.value.unshift(inventory)

  appStore.showToast('库存数据上传成功', 'success')
  isUploadDialogOpen.value = false
}

function handleDelete(id: string) {
  if (confirm('确定要删除这条库存记录吗？')) {
    inventories.value = inventories.value.filter(i => i.id !== id)

    // 更新 localStorage
    const saved = localStorage.getItem('supplier_inventories')
    if (saved) {
      let allInventories = JSON.parse(saved)
      allInventories = allInventories.filter((i: any) => i.id !== id)
      localStorage.setItem('supplier_inventories', JSON.stringify(allInventories))
    }

    appStore.showToast('库存记录已删除', 'success')
  }
}

function getTotalQuantity() {
  return inventories.value.reduce((sum, i) => sum + i.quantity, 0)
}

function getPendingCount() {
  return inventories.value.filter(i => i.status === '待确认').length
}
</script>

<template>
  <div class="space-y-6">
    <!-- 页面标题 -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-semibold text-gray-900">库存管理</h1>
        <p class="text-sm text-gray-500 mt-1">上传和管理代加工库存数据</p>
      </div>
      <button @click="openUploadDialog" class="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700">
        <Upload class="w-4 h-4" />上传库存
      </button>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">物料种类</p>
            <p class="text-2xl font-semibold text-gray-900 mt-1">{{ inventories.length }}</p>
          </div>
          <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
            <FileText class="w-6 h-6 text-blue-600" />
          </div>
        </div>
      </div>
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">总库存数量</p>
            <p class="text-2xl font-semibold text-green-600 mt-1">{{ getTotalQuantity().toLocaleString() }}</p>
          </div>
          <div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
            <FileText class="w-6 h-6 text-green-600" />
          </div>
        </div>
      </div>
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">待确认</p>
            <p class="text-2xl font-semibold text-orange-600 mt-1">{{ getPendingCount() }}</p>
          </div>
          <div class="w-12 h-12 bg-orange-100 rounded-lg flex items-center justify-center">
            <AlertTriangle class="w-6 h-6 text-orange-600" />
          </div>
        </div>
      </div>
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">已确认</p>
            <p class="text-2xl font-semibold text-gray-900 mt-1">{{ inventories.filter(i => i.status === '已确认').length }}</p>
          </div>
          <div class="w-12 h-12 bg-gray-100 rounded-lg flex items-center justify-center">
            <FileText class="w-6 h-6 text-gray-600" />
          </div>
        </div>
      </div>
    </div>

    <!-- 库存列表 -->
    <div class="bg-white rounded-lg border border-gray-200 overflow-x-auto">
      <table class="w-full text-sm">
        <thead class="bg-gray-50">
          <tr>
            <th class="text-left px-4 py-3 font-medium text-gray-700">物料编码</th>
            <th class="text-left px-4 py-3 font-medium text-gray-700">物料名称</th>
            <th class="text-right px-4 py-3 font-medium text-gray-700">库存数量</th>
            <th class="text-left px-4 py-3 font-medium text-gray-700">单位</th>
            <th class="text-left px-4 py-3 font-medium text-gray-700">上传日期</th>
            <th class="text-left px-4 py-3 font-medium text-gray-700">状态</th>
            <th class="text-left px-4 py-3 font-medium text-gray-700">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in inventories" :key="item.id" class="border-t border-gray-200">
            <td class="px-4 py-3 font-medium text-gray-900">{{ item.materialCode }}</td>
            <td class="px-4 py-3 text-gray-900">{{ item.materialName }}</td>
            <td class="px-4 py-3 text-right text-gray-900">{{ item.quantity.toLocaleString() }}</td>
            <td class="px-4 py-3 text-gray-900">{{ item.unit }}</td>
            <td class="px-4 py-3 text-gray-900">{{ item.uploadDate }}</td>
            <td class="px-4 py-3">
              <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="getStatusClass(item.status)">{{ item.status }}</span>
            </td>
            <td class="px-4 py-3">
              <button @click="handleDelete(item.id)" class="px-3 py-1.5 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="inventories.length === 0" class="text-center py-12 text-gray-500">
        <FileText class="w-12 h-12 mx-auto mb-4 opacity-50" />
        <p>暂无库存记录</p>
      </div>
    </div>

    <!-- 上传对话框 -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="isUploadDialogOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-black/50" @click="isUploadDialogOpen = false" />
          <div class="relative bg-white rounded-xl shadow-xl w-full max-w-lg">
            <div class="flex items-center justify-between p-4 border-b">
              <h3 class="text-lg font-semibold text-gray-900">上传库存数据</h3>
              <button @click="isUploadDialogOpen = false" class="p-2 hover:bg-gray-100 rounded-lg">
                <X class="w-5 h-5" />
              </button>
            </div>
            <div class="p-6 space-y-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">物料编码 <span class="text-red-500">*</span></label>
                <input v-model="uploadForm.materialCode" type="text" placeholder="请输入物料编码" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">物料名称 <span class="text-red-500">*</span></label>
                <input v-model="uploadForm.materialName" type="text" placeholder="请输入物料名称" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">库存数量 <span class="text-red-500">*</span></label>
                  <input v-model.number="uploadForm.quantity" type="number" min="0" placeholder="请输入数量" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">单位</label>
                  <select v-model="uploadForm.unit" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
                    <option value="个">个</option>
                    <option value="套">套</option>
                    <option value="件">件</option>
                    <option value="KG">KG</option>
                    <option value="米">米</option>
                  </select>
                </div>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">上传日期</label>
                <input v-model="uploadForm.uploadDate" type="date" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">备注</label>
                <textarea v-model="uploadForm.notes" rows="2" placeholder="请输入备注信息（可选）" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"></textarea>
              </div>
            </div>
            <div class="flex justify-end gap-3 p-4 border-t bg-gray-50">
              <button @click="isUploadDialogOpen = false" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">取消</button>
              <button @click="handleUpload" class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">上传</button>
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
