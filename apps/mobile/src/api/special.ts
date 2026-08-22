import type { IRuoYiPageResult, ISpecialAppointment, ISpecialArticle, ISpecialOrganization, ISpecialResource, ISpecialTeacher } from './types/special'
import { http } from '@/http/http'
import { mapTeacherIds } from './snowflake'

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
  return http.get<IRuoYiPageResult<Record<string, unknown>>>('/special/mobile/teacher/list', params).then((res) => ({
    ...res,
    rows: (res.rows || []).map(row => mapTeacherIds(row as Record<string, unknown>)) as ISpecialTeacher[],
  }))
}

export function getTeacherDetail(id: string) {
  return http.get<Record<string, unknown>>(`/special/mobile/teacher/${id}`).then((row) => {
    if (!row) {
      return row as unknown as ISpecialTeacher
    }
    return mapTeacherIds(row) as ISpecialTeacher
  })
}
