import type { IRuoYiPageResult, ISpecialTeacher } from './types/special'
import { http } from '@/http/http'
import { mapAppointmentIds, mapTeacherIds } from './snowflake'

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
  id: string
  resourceId?: string
  teacherId?: string
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
  return http.get<IRuoYiPageResult<Record<string, unknown>>>('/special/mobile/me/appointments', params).then((res) => ({
    ...res,
    rows: (res.rows || []).map(row => mapAppointmentIds(row as Record<string, unknown>)) as IMyAppointment[],
  }))
}

export function getMyAppointmentDetail(id: string) {
  return http.get<Record<string, unknown>>(`/special/mobile/me/appointments/${id}`).then((row) => {
    if (!row) {
      return row as unknown as IMyAppointment
    }
    return mapAppointmentIds(row) as IMyAppointment
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
  return http.get<Record<string, unknown>>('/special/mobile/me/teacher-profile').then((row) => {
    if (!row) {
      return row as unknown as ISpecialTeacher
    }
    return mapTeacherIds(row) as ISpecialTeacher
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
