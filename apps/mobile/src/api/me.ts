import type { IRuoYiPageResult } from './types/special'
import { http } from '@/http/http'

/** 家长资料 */
export interface IMobileProfile {
  userId: string | number
  nickname: string
  avatar?: string
  phone?: string
  roleKey?: string
  roleName?: string
}

/** 我的预约 */
export interface IMyAppointment {
  id: string | number
  resourceId: string | number
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
  return http.get<IMobileProfile>('/special/mobile/me/profile')
}

export function updateMyProfile(data: { nickname?: string, phone?: string }) {
  return http.put<void>('/special/mobile/me/profile', data)
}

export function getMyAppointments(params: { pageNum?: number, pageSize?: number }) {
  return http.get<IRuoYiPageResult<IMyAppointment>>('/special/mobile/me/appointments', params)
}

export function getMyAppointmentDetail(id: string | number) {
  return http.get<IMyAppointment>(`/special/mobile/me/appointments/${id}`)
}

export const APPOINTMENT_STATUS_MAP: Record<number, { label: string, tone: 'info' | 'primary' | 'success' | 'warning' }> = {
  0: { label: '待处理', tone: 'info' },
  1: { label: '已联系', tone: 'primary' },
  2: { label: '已完成', tone: 'success' },
  3: { label: '已取消', tone: 'warning' },
}
