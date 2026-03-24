/**
 * 角色权限管理 Store
 * 基于 RBAC (Role-Based Access Control) 模型
 */

import { defineStore } from 'pinia'
import { ref } from 'vue'

// 权限定义
export type Permission =
  // 供应商管理
  | 'supplier.view' | 'supplier.create' | 'supplier.edit' | 'supplier.delete' | 'supplier.evaluate'
  // 订单管理
  | 'order.view' | 'order.create' | 'order.edit' | 'order.delete' | 'order.confirm' | 'order.erp_sync'
  // 模具管理
  | 'mold.view' | 'mold.create' | 'mold.edit' | 'mold.delete' | 'mold.maintenance'
  // 仓储管理
  | 'warehouse.view' | 'warehouse.adjust' | 'warehouse.reconcile'
  // 战略寻源
  | 'sourcing.view' | 'sourcing.create' | 'sourcing.evaluate' | 'sourcing.award'
  // 财务对账
  | 'finance.view' | 'finance.audit' | 'finance.confirm'
  // 用户管理
  | 'user.view' | 'user.create' | 'user.edit' | 'user.delete' | 'user.assign_role'
  // 风险预警
  | 'risk.view'
  // 系统管理
  | 'system.export' | 'system.settings'
  // 供应商门户
  | 'portal.view' | 'portal.order' | 'portal.asn' | 'portal.inventory' | 'portal.invoice' | 'portal.performance'

// 角色定义
export interface Role {
  id: string
  name: string
  description: string
  permissions: Permission[]
  isSystemRole: boolean
}

// 用户角色关联
export interface UserRole {
  userId: string
  roleId: string
  assignedAt: string
  assignedBy: string
}

// 角色权限映射配置
const rolePermissionsConfig: Record<string, Permission[]> = {
  // 系统管理员 - 拥有所有权限
  'admin': [
    'supplier.view', 'supplier.create', 'supplier.edit', 'supplier.delete', 'supplier.evaluate',
    'order.view', 'order.create', 'order.edit', 'order.delete', 'order.confirm', 'order.erp_sync',
    'mold.view', 'mold.create', 'mold.edit', 'mold.delete', 'mold.maintenance',
    'warehouse.view', 'warehouse.adjust', 'warehouse.reconcile',
    'sourcing.view', 'sourcing.create', 'sourcing.evaluate', 'sourcing.award',
    'finance.view', 'finance.audit', 'finance.confirm',
    'user.view', 'user.create', 'user.edit', 'user.delete', 'user.assign_role',
    'risk.view',
    'system.export', 'system.settings',
  ],
  // 采购经理 - 战略和核心采购权限
  'procurement_manager': [
    'supplier.view', 'supplier.create', 'supplier.edit', 'supplier.evaluate',
    'order.view', 'order.create', 'order.edit', 'order.confirm', 'order.erp_sync',
    'mold.view', 'mold.edit',
    'warehouse.view',
    'sourcing.view', 'sourcing.create', 'sourcing.evaluate', 'sourcing.award',
    'finance.view',
    'user.view',
    'risk.view',
    'system.export',
  ],
  // 采购员 - 基础采购权限
  'procurement_staff': [
    'supplier.view',
    'order.view', 'order.create', 'order.edit',
    'mold.view',
    'warehouse.view',
    'sourcing.view',
    'system.export',
  ],
  // 质检员 - 质量相关权限
  'quality_inspector': [
    'supplier.view', 'supplier.evaluate',
    'order.view', 'order.confirm',
    'mold.view',
    'warehouse.view',
    'risk.view',
  ],
  // 仓管员 - 仓储权限
  'warehouse_keeper': [
    'supplier.view',
    'order.view',
    'mold.view',
    'warehouse.view', 'warehouse.adjust', 'warehouse.reconcile',
    'system.export',
  ],
  // 财务人员 - 财务权限
  'finance_staff': [
    'supplier.view',
    'order.view',
    'finance.view', 'finance.audit', 'finance.confirm',
    'system.export',
  ],
  // 模具管理员 - 模具管理权限
  'mold_admin': [
    'supplier.view',
    'mold.view', 'mold.create', 'mold.edit', 'mold.maintenance',
    'system.export',
  ],
  // 供应商业务员 - 供应商门户权限
  'supplier_agent': [
    'portal.view', 'portal.order', 'portal.asn', 'portal.inventory', 'portal.invoice', 'portal.performance',
  ],
}

export const usePermissionsStore = defineStore('permissions', () => {
  // 角色列表
  const roles = ref<Role[]>([
    {
      id: 'admin',
      name: '系统管理员',
      description: '拥有系统所有权限',
      permissions: rolePermissionsConfig['admin'],
      isSystemRole: true,
    },
    {
      id: 'procurement_manager',
      name: '采购经理',
      description: '负责战略采购和供应商管理',
      permissions: rolePermissionsConfig['procurement_manager'],
      isSystemRole: true,
    },
    {
      id: 'procurement_staff',
      name: '采购员',
      description: '执行日常采购任务',
      permissions: rolePermissionsConfig['procurement_staff'],
      isSystemRole: true,
    },
    {
      id: 'quality_inspector',
      name: '质检员',
      description: '负责质量检验和评估',
      permissions: rolePermissionsConfig['quality_inspector'],
      isSystemRole: true,
    },
    {
      id: 'warehouse_keeper',
      name: '仓管员',
      description: '负责仓储管理和盘点',
      permissions: rolePermissionsConfig['warehouse_keeper'],
      isSystemRole: true,
    },
    {
      id: 'finance_staff',
      name: '财务人员',
      description: '负责财务对账和结算',
      permissions: rolePermissionsConfig['finance_staff'],
      isSystemRole: true,
    },
    {
      id: 'mold_admin',
      name: '模具管理员',
      description: '负责模具管理和维护',
      permissions: rolePermissionsConfig['mold_admin'],
      isSystemRole: true,
    },
    {
      id: 'supplier_agent',
      name: '供应商业务员',
      description: '供应商门户用户',
      permissions: rolePermissionsConfig['supplier_agent'],
      isSystemRole: true,
    },
  ])

  // 用户角色关联
  const userRoles = ref<UserRole[]>([])

  // 初始化用户角色
  function loadUserRoles() {
    const saved = localStorage.getItem('user_roles')
    if (saved) {
      try {
        userRoles.value = JSON.parse(saved)
      } catch {
        userRoles.value = []
      }
    }
  }

  // 获取用户的角色ID列表
  function getUserRoleIds(userId: string): string[] {
    return userRoles.value
      .filter(ur => ur.userId === userId)
      .map(ur => ur.roleId)
  }

  // 获取用户的所有权限
  function getUserPermissions(userId: string): Permission[] {
    const roleIds = getUserRoleIds(userId)
    const permissions = new Set<Permission>()

    roleIds.forEach(roleId => {
      const role = roles.value.find(r => r.id === roleId)
      if (role) {
        role.permissions.forEach(p => permissions.add(p))
      }
    })

    return Array.from(permissions)
  }

  // 检查用户是否有指定权限
  function hasPermission(userId: string, permission: Permission): boolean {
    const permissions = getUserPermissions(userId)
    return permissions.includes(permission)
  }

  // 检查用户是否有任意一个指定权限
  function hasAnyPermission(userId: string, permissions: Permission[]): boolean {
    return permissions.some(p => hasPermission(userId, p))
  }

  // 检查用户是否有所有指定权限
  function hasAllPermissions(userId: string, permissions: Permission[]): boolean {
    return permissions.every(p => hasPermission(userId, p))
  }

  // 为用户分配角色
  function assignRole(userId: string, roleId: string, assignedBy: string): void {
    // 检查是否已分配
    const existing = userRoles.value.find(ur => ur.userId === userId && ur.roleId === roleId)
    if (existing) return

    userRoles.value.push({
      userId,
      roleId,
      assignedAt: new Date().toISOString(),
      assignedBy,
    })

    saveUserRoles()
  }

  // 撤销用户角色
  function revokeRole(userId: string, roleId: string): void {
    userRoles.value = userRoles.value.filter(ur => !(ur.userId === userId && ur.roleId === roleId))
    saveUserRoles()
  }

  // 保存用户角色到 localStorage
  function saveUserRoles(): void {
    localStorage.setItem('user_roles', JSON.stringify(userRoles.value))
  }

  // 获取角色信息
  function getRole(roleId: string): Role | undefined {
    return roles.value.find(r => r.id === roleId)
  }

  // 获取所有角色
  function getAllRoles(): Role[] {
    return roles.value
  }

  // 创建自定义角色
  function createCustomRole(name: string, description: string, permissions: Permission[]): Role {
    const role: Role = {
      id: `custom_${Date.now()}`,
      name,
      description,
      permissions,
      isSystemRole: false,
    }
    roles.value.push(role)
    return role
  }

  // 删除自定义角色
  function deleteCustomRole(roleId: string): void {
    const role = roles.value.find(r => r.id === roleId)
    if (!role) return
    if (role.isSystemRole) return // 不能删除系统角色

    roles.value = roles.value.filter(r => r.id !== roleId)
    // 同时删除所有用户角色关联
    userRoles.value = userRoles.value.filter(ur => ur.roleId !== roleId)
    saveUserRoles()
  }

  // 根据角色名称获取角色ID（用于兼容旧代码）
  function getRoleIdByRoleName(roleName: string): string {
    const roleMap: Record<string, string> = {
      '系统管理员': 'admin',
      '采购经理': 'procurement_manager',
      '采购员': 'procurement_staff',
      '质检员': 'quality_inspector',
      '仓管员': 'warehouse_keeper',
      '财务': 'finance_staff',
      '财务人员': 'finance_staff',
      '模具管理员': 'mold_admin',
      '供应商业务员': 'supplier_agent',
    }
    return roleMap[roleName] || 'procurement_staff'
  }

  // 初始化
  loadUserRoles()

  return {
    roles,
    userRoles,
    getUserRoleIds,
    getUserPermissions,
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
    assignRole,
    revokeRole,
    getRole,
    getAllRoles,
    createCustomRole,
    deleteCustomRole,
    getRoleIdByRoleName,
    loadUserRoles,
  }
})
