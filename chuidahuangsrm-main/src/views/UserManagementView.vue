  <script setup lang="ts">
  import { ref, onMounted, computed } from 'vue'
  import { Users, UserPlus, Shield, XCircle, Settings, Key, Eye, EyeOff, Check } from 'lucide-vue-next'
  import { mockUsers } from '@/data/mockData'
  import type { User } from '@/types'
  import { useAppStore } from '@/stores/useAppStore'
  import { usePermissionsStore, type Role, type Permission } from '@/stores/permissions'

  const appStore = useAppStore()
  const permissionsStore = usePermissionsStore()

  // 对话框状态
  const isAddDialogOpen = ref(false)
  const isRoleDialogOpen = ref(false)
  const formErrors = ref<Record<string, string>>({})

  // 选中的用户
  const selectedUser = ref<User | null>(null)

  // 用户列表，优先从 localStorage 读取
  const users = ref<User[]>([])

  // 初始化用户数据
  onMounted(() => {
    const saved = localStorage.getItem('users')
    if (saved) {
      try {
        users.value = JSON.parse(saved)
      } catch {
        users.value = [...mockUsers]
      }
    } else {
      users.value = [...mockUsers]
    }
  })

  // 新用户表单数据
  const newUser = ref({
    name: '',
    username: '',
    role: '采购员',
    department: '采购部',
    phone: '',
    email: '',
  })

  // 角色管理表单
  const roleForm = ref({
    selectedRoles: [] as string[],
  })

  const roles = ['采购员', '质检员', '仓管员', '财务', '模具管理员']
  const departments = ['采购部', '质量部', '仓储部', '财务部', '生产部']

  // 获取所有可用角色
  const availableRoles = computed(() => permissionsStore.getAllRoles())

  // 获取用户拥有的角色
  function getUserRoles(user: User): Role[] {
    const roleIds = permissionsStore.getUserRoleIds(user.id)
    return roleIds.map(id => permissionsStore.getRole(id)).filter(Boolean) as Role[]
  }

  // 获取用户权限数量
  function getUserPermissionCount(user: User): number {
    return permissionsStore.getUserPermissions(user.id).length
  }

  // 获取用户权限列表
  function getUserPermissions(userId: string): Permission[] {
    return permissionsStore.getUserPermissions(userId)
  }

  function getRoleBadgeClass(role: string) {
    switch (role) {
      case '采购员': case '财务': return 'bg-blue-100 text-blue-800'
      case '质检员': case '模具管理员': return 'bg-gray-200 text-gray-800'
      default: return 'border border-gray-300 text-gray-700'
    }
  }

  function openAddDialog() {
    formErrors.value = {}
    newUser.value = {
      name: '',
      username: '',
      role: '采购员',
      department: '采购部',
      phone: '',
      email: '',
    }
    isAddDialogOpen.value = true
  }

  function handleAddUser() {
    // 清空之前的错误
    formErrors.value = {}

    // 表单验证
    if (!newUser.value.name.trim()) {
      formErrors.value.name = '请输入姓名'
      return
    }
    if (!newUser.value.username.trim()) {
      formErrors.value.username = '请输入用户名'
      return
    }
    if (!newUser.value.phone.trim()) {
      formErrors.value.phone = '请输入联系电话'
      return
    }
    if (!newUser.value.email.trim()) {
      formErrors.value.email = '请输入邮箱'
      return
    }

    // 创建新用户对象
    const user: User = {
      id: `U${String(users.value.length + 1).padStart(3, '0')}`,
      name: newUser.value.name.trim(),
      username: newUser.value.username.trim(),
      role: newUser.value.role,
      department: newUser.value.department,
      status: '在职',
      phone: newUser.value.phone.trim(),
      email: newUser.value.email.trim(),
    }

    // 添加到列表
    users.value.push(user)

    // 自动分配对应角色
    const roleId = permissionsStore.getRoleIdByRoleName(newUser.value.role)
    permissionsStore.assignRole(user.id, roleId, appStore.currentUser.name)

    // 保存到 localStorage
    localStorage.setItem('users', JSON.stringify(users.value))

    // 显示成功提示
    appStore.showToast('用户添加成功', 'success')

    // 关闭对话框
    isAddDialogOpen.value = false
  }

  // 打开角色管理对话框
  function openRoleDialog(user: User) {
    selectedUser.value = user
    const userRoles = permissionsStore.getUserRoleIds(user.id)
    roleForm.value.selectedRoles = [...userRoles]
    isRoleDialogOpen.value = true
  }

  // 保存用户角色
  function handleSaveRoles() {
    if (!selectedUser.value) return

    // 获取当前用户角色
    const currentRoles = permissionsStore.getUserRoleIds(selectedUser.value.id)

    // 移除已取消的角色
    currentRoles.forEach(roleId => {
      if (!roleForm.value.selectedRoles.includes(roleId)) {
        permissionsStore.revokeRole(selectedUser.value!.id, roleId)
      }
    })

    // 添加新角色
    roleForm.value.selectedRoles.forEach(roleId => {
      if (!currentRoles.includes(roleId)) {
        permissionsStore.assignRole(selectedUser.value!.id, roleId, appStore.currentUser.name)
      }
    })

    appStore.showToast('角色分配成功', 'success')
    isRoleDialogOpen.value = false
    selectedUser.value = null
  }
  </script>

  <template>
    <div class="space-y-6">
      <!-- 标题 -->
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-semibold text-gray-900">{{ appStore.t('page.users.title') }}</h1>
          <p class="text-sm text-gray-500 mt-1">基于RBAC的权限控制 · 内外部用户分离</p>
        </div>
        <button class="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700" @click="openAddDialog">
          <UserPlus class="w-4 h-4" />{{ appStore.t('btn.addUser') }}
        </button>
      </div>

      <!-- 统计卡片 -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div class="bg-white rounded-lg border border-gray-200 p-6">
          <div class="flex items-center justify-between"><div><p class="text-sm text-gray-500">内部用户</p><p class="text-2xl font-semibold text-gray-900 mt-1">{{ users.filter((u) => u.role !== '供应商').length }}</p></div><div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center"><Users class="w-6 h-6 text-blue-600" /></div></div>
        </div>
        <div class="bg-white rounded-lg border border-gray-200 p-6">
          <div class="flex items-center justify-between"><div><p class="text-sm text-gray-500">采购员</p><p class="text-2xl font-semibold text-gray-900 mt-1">{{ users.filter((u) => u.role === '采购员').length }}</p></div><div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center"><Shield class="w-6 h-6 text-green-600" /></div></div>
        </div>
        <div class="bg-white rounded-lg border border-gray-200 p-6">
          <div class="flex items-center justify-between"><div><p class="text-sm text-gray-500">质检员</p><p class="text-2xl font-semibold text-gray-900 mt-1">{{ users.filter((u) => u.role === '质检员').length }}</p></div><div class="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center"><Shield class="w-6 h-6 text-purple-600" /></div></div>
        </div>
        <div class="bg-white rounded-lg border border-gray-200 p-6">
          <div class="flex items-center justify-between"><div><p class="text-sm text-gray-500">仓管员</p><p class="text-2xl font-semibold text-gray-900 mt-1">{{ users.filter((u) => u.role === '仓管员').length }}</p></div><div class="w-12 h-12 bg-orange-100 rounded-lg flex items-center justify-center"><Shield class="w-6 h-6 text-orange-600" /></div></div>
        </div>
      </div>

      <!-- 用户列表 -->
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <h3 class="text-base font-semibold text-gray-900 mb-4">用户列表</h3>
        <div class="border border-gray-200 rounded-lg overflow-hidden overflow-x-auto">
          <table class="w-full min-w-max">
            <thead class="bg-gray-50">
              <tr>
                <th class="text-left px-4 py-3 text-sm font-medium text-gray-700">姓名</th>
                <th class="text-left px-4 py-3 text-sm font-medium text-gray-700">用户名</th>
                <th class="text-left px-4 py-3 text-sm font-medium text-gray-700">角色</th>
                <th class="text-left px-4 py-3 text-sm font-medium text-gray-700">部门</th>
                <th class="text-left px-4 py-3 text-sm font-medium text-gray-700">联系电话</th>
                <th class="text-left px-4 py-3 text-sm font-medium text-gray-700">邮箱</th>
                <th class="text-left px-4 py-3 text-sm font-medium text-gray-700">状态</th>
                <th class="text-left px-4 py-3 text-sm font-medium text-gray-700">权限</th>
                <th class="text-left px-4 py-3 text-sm font-medium text-gray-700">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in users" :key="user.id" class="border-t border-gray-200">
                <td class="px-4 py-3 text-sm font-medium text-gray-900">{{ user.name }}</td>
                <td class="px-4 py-3 text-sm text-gray-900">{{ user.username }}</td>
                <td class="px-4 py-3 text-sm">
                  <div class="flex flex-wrap gap-1">
                    <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="getRoleBadgeClass(user.role)">{{ user.role }}</span>
                    <span v-for="role in getUserRoles(user)" :key="role.id" class="px-2 py-0.5 rounded-full text-xs font-medium bg-purple-100 text-purple-800">{{ role.name }}</span>
                  </div>
                </td>
                <td class="px-4 py-3 text-sm text-gray-900">{{ user.department }}</td>
                <td class="px-4 py-3 text-sm text-gray-900">{{ user.phone }}</td>
                <td class="px-4 py-3 text-sm text-gray-900">{{ user.email }}</td>
                <td class="px-4 py-3 text-sm">
                  <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="user.status === '在职' ? 'bg-green-100 text-green-800' : 'border border-gray-300 text-gray-700'">{{ user.status }}</span>
                </td>
                <td class="px-4 py-3 text-sm">
                  <span class="text-xs text-gray-600">{{ getUserPermissionCount(user) }} 项权限</span>
                </td>
                <td class="px-4 py-3 text-sm">
                  <div class="flex gap-2">
                    <button class="px-3 py-1.5 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">编辑</button>
                    <button @click="openRoleDialog(user)" class="px-3 py-1.5 border border-blue-300 rounded-lg text-sm text-blue-700 hover:bg-blue-50 flex items-center gap-1">
                      <Key class="w-3 h-3" />权限
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 角色权限说明 -->
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <h3 class="text-base font-semibold text-gray-900 mb-4">角色权限说明</h3>
        <div class="space-y-4">
          <div class="p-4 bg-blue-50 rounded-lg">
            <div class="flex items-center gap-2 mb-2">
              <Shield class="w-5 h-5 text-blue-600" />
              <h4 class="font-semibold text-blue-900">RBAC权限控制</h4>
            </div>
            <p class="text-sm text-blue-800">基于角色的访问控制，确保数据隔离。供应商仅能看到自身订单，采购员按品类管辖区域隔离。</p>
          </div>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="border border-gray-200 rounded-lg p-4">
              <h4 class="font-semibold text-gray-900 mb-3">内部用户角色</h4>
              <ul class="space-y-2 text-sm text-gray-700">
                <li v-for="role in [{ name: '采购员', desc: '寻源、订单管理、供应商评价' }, { name: '质检员', desc: '质量检验、不合格品处理' }, { name: '仓管员', desc: '收货、入库、虚拟仓管理' }, { name: '财务', desc: '对账、发票管理、付款审批' }, { name: '模具管理员', desc: '模具追踪、维保管理' }]" :key="role.name" class="flex items-start gap-2">
                  <span class="text-blue-600 mt-1">•</span>
                  <span><strong>{{ role.name }}：</strong>{{ role.desc }}</span>
                </li>
              </ul>
            </div>
            <div class="border border-gray-200 rounded-lg p-4">
              <h4 class="font-semibold text-gray-900 mb-3">外部用户角色</h4>
              <ul class="space-y-2 text-sm text-gray-700">
                <li v-for="role in [{ name: '供应商业务员', desc: '订单确认、ASN创建' }, { name: '供应商发货员', desc: '发货管理、物流追踪' }, { name: '供应商财务', desc: '对账确认、发票上传' }]" :key="role.name" class="flex items-start gap-2">
                  <span class="text-green-600 mt-1">•</span>
                  <span><strong>{{ role.name }}：</strong>{{ role.desc }}</span>
                </li>
              </ul>
              <div class="mt-4 p-3 bg-green-50 rounded-lg">
                <p class="text-xs text-green-800">供应商通过门户自助注册，管理其内部的业务、发货及财务子账号</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 添加用户弹窗 -->
      <Teleport to="body">
        <Transition name="modal">
          <div v-if="isAddDialogOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4">
            <div class="absolute inset-0 bg-black/50" @click="isAddDialogOpen = false" />
            <div class="relative bg-white rounded-xl shadow-xl w-full max-w-md p-6">
              <h3 class="text-lg font-semibold text-gray-900 mb-4">添加用户</h3>

              <div class="space-y-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">姓名 <span class="text-red-500">*</span></label>
                  <input
                    v-model="newUser.name"
                    placeholder="请输入姓名"
                    :class="['w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2', formErrors.name ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500']"
                  />
                  <p v-if="formErrors.name" class="text-red-500 text-xs mt-1 flex items-center gap-1">
                    <XCircle class="w-3 h-3" />{{ formErrors.name }}
                  </p>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">用户名 <span class="text-red-500">*</span></label>
                  <input
                    v-model="newUser.username"
                    placeholder="请输入用户名"
                    :class="['w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2', formErrors.username ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500']"
                  />
                  <p v-if="formErrors.username" class="text-red-500 text-xs mt-1 flex items-center gap-1">
                    <XCircle class="w-3 h-3" />{{ formErrors.username }}
                  </p>
                </div>
                <div class="grid grid-cols-2 gap-4">
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">角色</label>
                    <select v-model="newUser.role" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
                      <option v-for="role in roles" :key="role" :value="role">{{ role }}</option>
                    </select>
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">部门</label>
                    <select v-model="newUser.department" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
                      <option v-for="dept in departments" :key="dept" :value="dept">{{ dept }}</option>
                    </select>
                  </div>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">联系电话 <span class="text-red-500">*</span></label>
                  <input
                    v-model="newUser.phone"
                    placeholder="请输入联系电话"
                    :class="['w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2', formErrors.phone ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500']"
                  />
                  <p v-if="formErrors.phone" class="text-red-500 text-xs mt-1 flex items-center gap-1">
                    <XCircle class="w-3 h-3" />{{ formErrors.phone }}
                  </p>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">邮箱 <span class="text-red-500">*</span></label>
                  <input
                    v-model="newUser.email"
                    type="email"
                    placeholder="请输入邮箱"
                    :class="['w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2', formErrors.email ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500']"
                  />
                  <p v-if="formErrors.email" class="text-red-500 text-xs mt-1 flex items-center gap-1">
                    <XCircle class="w-3 h-3" />{{ formErrors.email }}
                  </p>
                </div>
              </div>

              <div class="flex justify-end gap-3 mt-6">
                <button type="button" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50" @click="isAddDialogOpen = false">取消</button>
                <button type="button" class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700" @click="handleAddUser">添加</button>
              </div>
            </div>
          </div>
        </Transition>
      </Teleport>

      <!-- 角色管理对话框 -->
      <Teleport to="body">
        <Transition name="modal">
          <div v-if="isRoleDialogOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4">
            <div class="absolute inset-0 bg-black/50" @click="isRoleDialogOpen = false"></div>
            <div class="relative bg-white rounded-xl shadow-2xl w-full max-w-lg max-h-[80vh] overflow-y-auto">
              <div class="p-6">
                <div class="flex items-center justify-between mb-4">
                  <div class="flex items-center gap-3">
                    <div class="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center">
                      <Shield class="w-5 h-5 text-purple-600" />
                    </div>
                    <div>
                      <h3 class="text-lg font-semibold text-gray-900">权限管理</h3>
                      <p class="text-sm text-gray-500">{{ selectedUser?.name }} - {{ selectedUser?.username }}</p>
                    </div>
                  </div>
                  <button @click="isRoleDialogOpen = false" class="p-2 hover:bg-gray-100 rounded-lg">
                    <XCircle class="w-5 h-5 text-gray-400" />
                  </button>
                </div>

                <div class="space-y-4">
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-2">分配角色</label>
                    <div class="grid grid-cols-1 gap-2 max-h-60 overflow-y-auto border border-gray-200 rounded-lg p-3">
                      <label v-for="role in availableRoles" :key="role.id" class="flex items-start gap-3 p-3 hover:bg-gray-50 rounded-lg cursor-pointer">
                        <input
                          type="checkbox"
                          :value="role.id"
                          v-model="roleForm.selectedRoles"
                          class="mt-1 w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
                        />
                        <div class="flex-1">
                          <div class="flex items-center gap-2">
                            <span class="font-medium text-gray-900">{{ role.name }}</span>
                            <span v-if="role.isSystemRole" class="px-1.5 py-0.5 bg-gray-100 text-gray-600 rounded text-xs">系统</span>
                          </div>
                          <p class="text-sm text-gray-500 mt-1">{{ role.description }}</p>
                          <p class="text-xs text-purple-600 mt-1">{{ role.permissions.length }} 项权限</p>
                        </div>
                      </label>
                    </div>
                  </div>

                  <!-- 当前权限预览 -->
                  <div v-if="selectedUser">
                    <label class="block text-sm font-medium text-gray-700 mb-2">当前权限 ({{ getUserPermissionCount(selectedUser) }} 项)</label>
                    <div class="flex flex-wrap gap-1 p-3 bg-gray-50 rounded-lg">
                      <span v-for="permission in getUserPermissionCount(selectedUser) > 0 ? getUserPermissions(selectedUser.id) : []" :key="permission" class="px-2 py-1 bg-white border border-gray-200 rounded text-xs text-gray-700">
                        {{ permission }}
                      </span>
                      <span v-if="getUserPermissionCount(selectedUser) === 0" class="text-sm text-gray-500">暂无权限</span>
                    </div>
                  </div>
                </div>

                <div class="flex justify-end gap-3 mt-6">
                  <button @click="isRoleDialogOpen = false" class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">取消</button>
                  <button @click="handleSaveRoles" class="px-4 py-2 bg-purple-600 text-white rounded-lg text-sm hover:bg-purple-700 flex items-center gap-2">
                    <Check class="w-4 h-4" />保存权限
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
  .modal-enter-active, .modal-leave-active {
    transition: opacity 0.2s ease;
  }
  .modal-enter-from, .modal-leave-to {
    opacity: 0;
  }
  </style>
