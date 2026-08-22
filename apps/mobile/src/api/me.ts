import type { IRuoYiPageResult, ISpecialTeacher } from './types/special'
import { http } from '@/http/http'

/** 家长资料 */
export interface IMobileProfile {
  userId: string | number
  nickname: string
  avatar?: string
  phone?: string
  roleKey?: string
  roleName?: string
  currentRole?: string
  roles?: string[]
  phoneBound?: boolean
}

/** 我的预约 */
export interface IMyAppointment {
  id: string | number
  resourceId?: string | number
  teacherId?: string | number
  resourceTitle?: string
  contactName?: string
  contactPhone?: string
  childAge?: string | number
  remark?: string
  appointStatus?: number
  handlerRemark?: string
  createTime?: string
  updateTime?: string
}

function mapAppointmentIds(row: IMyAppointment) {
  return {
    ...row,
    id: row.id == null ? row.id : String(row.id),
    resourceId: row.resourceId == null ? row.resourceId : String(row.resourceId),
    teacherId: row.teacherId == null ? row.teacherId : String(row.teacherId),
  }
}

export function getMyProfile() {
  return http.get<IMobileProfile>('/special/mobile/me/profile').then((row) => ({
    ...row,
    userId: String(row?.userId ?? ''),
  }))
}

export function updateMyProfile(data: { nickname?: string, phone?: string }) {
  return http.put<void>('/special/mobile/me/profile', data)
}

export function getMyAppointments(params: { pageNum?: number, pageSize?: number, appointStatus?: number }) {
  return http.get<IRuoYiPageResult<IMyAppointment>>('/special/mobile/me/appointments', params).then((res) => ({
    ...res,
    rows: (res.rows || []).map(mapAppointmentIds),
  }))
}

export function getMyAppointmentDetail(id: string | number) {
  return http.get<IMyAppointment>(`/special/mobile/me/appointments/${id}`).then((row) => {
    if (!row) {
      return row
    }
    return mapAppointmentIds(row)
  })
}

export interface IBindPhoneVo {
  access_token?: string
  expire_in?: number
  client_id?: string
}

export function bindMyPhone(data: { phone: string, smsCode: string }) {
  return http.post<IBindPhoneVo>('/special/mobile/me/bind-phone', data)
}

export function getMyTeacherProfile() {
  return http.get<ISpecialTeacher>('/special/mobile/me/teacher-profile').then((row) => {
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

export function updateMyTeacherProfile(data: {
  name?: string
  title?: string
  specialties?: string
  qualification?: string
  intro?: string
  avatarUrl?: string
  certImageUrl?: string
}) {
  return http.put<void>('/special/mobile/me/teacher-profile', data)
}

export const APPOINTMENT_STATUS_MAP: Record<number, { label: string, tone: 'info' | 'primary' | 'success' | 'warning' }> = {
  0: { label: '待处理', tone: 'info' },
  1: { label: '已联系', tone: 'primary' },
  2: { label: '已完成', tone: 'success' },
  3: { label: '已取消', tone: 'warning' },
}
