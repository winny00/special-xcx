/** 特教资源 */
export interface ISpecialResource {
  id: string | number
  title: string
  resourceType: string
  category?: string
  summary?: string
  content?: string
  coverUrl?: string
  orgId?: string | number
  providerName?: string
  contactPhone?: string
  region?: string
  price?: number
  status?: string | number
  viewCount?: number
  createTime?: string
}

/** 特教机构 */
export interface ISpecialOrganization {
  id: string | number
  name: string
  orgType: string
  address?: string
  region?: string
  contactName?: string
  contactPhone?: string
  description?: string
}

/** 预约申请 */
export interface ISpecialAppointment {
  id?: string | number
  resourceId: string | number
  resourceTitle?: string
  contactName: string
  contactPhone: string
  childAge?: string
  remark?: string
}

/** RuoYi 分页响应 */
export interface IRuoYiPageResult<T> {
  rows: T[]
  total: number
}

export const RESOURCE_TYPE_MAP: Record<string, string> = {
  course: '课程',
  tool: '工具',
  teacher: '老师',
  org: '机构',
  assessment: '评估',
}

export const RESOURCE_CATEGORIES = ['感统', '语言', '社交', '行为干预', '融合教育', '家庭支持']
