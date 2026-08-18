import type { IRuoYiPageResult, ISpecialAppointment, ISpecialOrganization, ISpecialResource } from './types/special'
import { http } from '@/http/http'

/** 资源列表（已发布） */
export function getResourceList(params: {
  pageNum?: number
  pageSize?: number
  resourceType?: string
  category?: string
  title?: string
}) {
  return http.get<IRuoYiPageResult<ISpecialResource>>('/special/mobile/resource/list', params)
}

/** 资源详情 */
export function getResourceDetail(id: string | number) {
  return http.get<ISpecialResource>(`/special/mobile/resource/${id}`)
}

/** 机构列表（已审核） */
export function getOrganizationList(params: {
  pageNum?: number
  pageSize?: number
  orgType?: string
  name?: string
}) {
  return http.get<IRuoYiPageResult<ISpecialOrganization>>('/special/mobile/organization/list', params)
}

/** 提交预约申请 */
export function createAppointment(data: ISpecialAppointment) {
  return http.post<void>('/special/mobile/appointment', data)
}
