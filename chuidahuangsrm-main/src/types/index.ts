export interface Supplier {
  id: string
  name: string
  code: string
  category: '战略' | '关键' | '杠杆' | '常规'
  status: '合格' | '暂停' | '黑名单'
  riskLevel: '低' | '中' | '高'
  riskScore: number
  qualityScore: number
  deliveryScore: number
  costScore: number
  innovationScore: number
  overallScore: number
  contact: string
  phone: string
  email: string
  address: string
  certifications: Array<{
    name: string
    validUntil: string
    status: '有效' | '即将到期' | '已过期'
  }>
  annualSpend: number
  orderCount: number
  onTimeRate: number
  qualityPassRate: number
}

export interface OrderItem {
  materialCode: string
  materialName: string
  quantity: number
  unit: string
  price: number
  amount: number
}

export interface ChangeHistory {
  version: number
  changeDate: string
  changeType: string
  description: string
  operator: string
}

export interface Order {
  id: string
  orderNo: string
  supplierId: string
  supplierName: string
  orderDate: string
  deliveryDate: string
  status: '待审核' | '待确认' | '生产中' | '已发货' | '已收货' | '已入库' | '已完成'
  totalAmount: number
  items: OrderItem[]
  changeHistory: ChangeHistory[]
}

export interface Mold {
  id: string
  code: string
  name: string
  category: string
  status: '在用' | '维保中' | '闲置' | '报废'
  location: string
  supplierId: string
  supplierName: string
  originalValue: number
  designLife: number
  currentUsage: number
  remainingLife: number
  lastMaintenanceDate: string
  nextMaintenanceDate: string
  drawings: string[]
  productionOrders: Array<{
    orderId: string
    productionDate: string
    quantity: number
  }>
}

export interface VirtualWarehouse {
  id: string
  supplierId: string
  supplierName: string
  materialCode: string
  materialName: string
  theoreticalQty: number
  actualQty: number
  variance: number
  varianceRate: number
  unit: string
  lastUpdateDate: string
}

export interface ASNItem {
  materialCode: string
  materialName: string
  quantity: number
  unit: string
  barcode: string
}

export interface ASN {
  id: string
  asnNo: string
  supplierId: string
  supplierName: string
  orderNo: string
  shipDate: string
  estimatedArrival: string
  status: '待发货' | '在途' | '已到货' | '已收货'
  items: ASNItem[]
}

export interface User {
  id: string
  name: string
  username: string
  role: '采购员' | '质检员' | '仓管员' | '财务' | '模具管理员' | '供应商'
  department: string
  status: '在职' | '离职'
  phone: string
  email: string
}
