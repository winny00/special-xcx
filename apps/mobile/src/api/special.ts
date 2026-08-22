import type { IRuoYiPageResult, ISpecialAppointment, ISpecialArticle, ISpecialOrganization, ISpecialResource, ISpecialTeacher } from './types/special'
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

/** 资讯列表（已发布） */
export function getArticleList(params: {
  pageNum?: number
  pageSize?: number
  category?: string
  title?: string
}) {
  return http.get<IRuoYiPageResult<ISpecialArticle>>('/special/mobile/article/list', params)
}

/** 资讯详情 */
export function getArticleDetail(id: string | number) {
  return http.get<ISpecialArticle>(`/special/mobile/article/${id}`)
}

export function getTeacherList(params: {
  pageNum?: number
  pageSize?: number
  name?: string
  specialties?: string
}) {
  return http.get<IRuoYiPageResult<ISpecialTeacher>>('/special/mobile/teacher/list', params)
}

export function getTeacherDetail(id: string | number) {
  return http.get<ISpecialTeacher>(`/special/mobile/teacher/${id}`).then((row) => {
    if (!row) {
      return row
    }
    return {
      ...row,
      id: row.id == null ? row.id : String(row.id),
      userId: row.userId == null ? row.userId : String(row.userId),
      orgId: row.orgId == null ? row.orgId : String(row.orgId),
      resourceId: row.resourceId == null ? row.resourceId : String(row.resourceId),
    }
  })
}
