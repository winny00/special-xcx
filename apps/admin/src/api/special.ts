import request, { type PageResult } from './request'

export interface SpecialResource {
  id?: number
  title: string
  resourceType: string
  category?: string
  summary?: string
  content?: string
  coverUrl?: string
  orgId?: number
  providerName?: string
  contactPhone?: string
  region?: string
  price?: number
  status?: number
  viewCount?: number
  createTime?: string
}

export interface SpecialOrganization {
  id?: number
  name: string
  orgType: string
  licenseNo?: string
  licenseUrl?: string
  address?: string
  region?: string
  contactName?: string
  contactPhone?: string
  description?: string
  coverUrl?: string
  auditStatus?: number
  status?: number
  createTime?: string
}

export interface SpecialAppointment {
  id?: number
  resourceId?: number
  resourceTitle?: string
  userId?: string
  contactName?: string
  contactPhone?: string
  childAge?: number
  remark?: string
  appointStatus?: number
  handlerId?: number
  handlerRemark?: string
  createTime?: string
}

export interface SpecialArticle {
  id?: number
  title: string
  summary?: string
  content?: string
  coverUrl?: string
  category?: string
  status?: number
  publishTime?: string
  viewCount?: number
  createTime?: string
}

export interface ListQuery {
  pageNum?: number
  pageSize?: number
  [key: string]: unknown
}

export interface DashboardStats {
  resourceTotal: number
  resourceByType: Record<string, number>
  resourceDraftCount: number
  orgAuditPending: number
  teacherAuditPending: number
  appointmentPending: number
  appointmentToday: number
}

export function getDashboardStats() {
  return request.get<DashboardStats>('/special/dashboard/stats')
}

// Resource CRUD
export function listResources(params?: ListQuery) {
  return request.get<PageResult<SpecialResource>>('/special/resource/list', { params })
}

export function getResource(id: number) {
  return request.get<SpecialResource>(`/special/resource/${id}`)
}

export function addResource(data: SpecialResource) {
  return request.post('/special/resource', data)
}

export function updateResource(data: SpecialResource) {
  return request.put('/special/resource', data)
}

export function deleteResources(ids: number[]) {
  return request.delete(`/special/resource/${ids.join(',')}`)
}

// Organization CRUD
export function listOrganizations(params?: ListQuery) {
  return request.get<PageResult<SpecialOrganization>>('/special/organization/list', { params })
}

export function getOrganization(id: number) {
  return request.get<SpecialOrganization>(`/special/organization/${id}`)
}

export function addOrganization(data: SpecialOrganization) {
  return request.post('/special/organization', data)
}

export function updateOrganization(data: SpecialOrganization) {
  return request.put('/special/organization', data)
}

export function deleteOrganizations(ids: number[]) {
  return request.delete(`/special/organization/${ids.join(',')}`)
}

// Appointment CRUD
export function listAppointments(params?: ListQuery) {
  return request.get<PageResult<SpecialAppointment>>('/special/appointment/list', { params })
}

export function getAppointment(id: number) {
  return request.get<SpecialAppointment>(`/special/appointment/${id}`)
}

export function addAppointment(data: SpecialAppointment) {
  return request.post('/special/appointment', data)
}

export function updateAppointment(data: SpecialAppointment) {
  return request.put('/special/appointment', data)
}

export function deleteAppointments(ids: number[]) {
  return request.delete(`/special/appointment/${ids.join(',')}`)
}

// Article CRUD
export function listArticles(params?: ListQuery) {
  return request.get<PageResult<SpecialArticle>>('/special/article/list', { params })
}

export function getArticle(id: number) {
  return request.get<SpecialArticle>(`/special/article/${id}`)
}

export function addArticle(data: SpecialArticle) {
  return request.post('/special/article', data)
}

export function updateArticle(data: SpecialArticle) {
  return request.put('/special/article', data)
}

export function deleteArticles(ids: number[]) {
  return request.delete(`/special/article/${ids.join(',')}`)
}

export interface SpecialParent {
  userId: string
  nickName?: string
  phone?: string
  createTime?: string
  appointmentCount?: number
}

export interface SpecialParentDetail extends SpecialParent {
  avatar?: string
  appointments?: SpecialAppointment[]
}

export function listParents(params?: ListQuery) {
  return request.get<PageResult<SpecialParent>>('/special/parent/list', { params })
}

export function getParent(userId: string) {
  return request.get<SpecialParentDetail>(`/special/parent/${userId}`)
}

export interface SpecialTeacher {
  id?: string
  name: string
  title?: string
  specialties?: string
  qualification?: string
  certImageUrl?: string
  avatarUrl?: string
  orgId?: string
  intro?: string
  status?: number
  resourceId?: string
  auditRemark?: string
  auditTime?: string
}

export interface SpecialAuditPayload {
  ids: string[]
  status: number
  remark?: string
}

export function listTeachers(params?: ListQuery) {
  return request.get<PageResult<SpecialTeacher>>('/special/teacher/list', { params })
}

export function addTeacher(data: SpecialTeacher) {
  return request.post('/special/teacher', data)
}

export function updateTeacher(data: SpecialTeacher) {
  return request.put('/special/teacher', data)
}

export function deleteTeachers(ids: string[]) {
  return request.delete(`/special/teacher/${ids.join(',')}`)
}

export function auditTeachers(data: SpecialAuditPayload) {
  return request.put('/special/teacher/audit', data)
}

export function auditOrganizations(data: SpecialAuditPayload) {
  return request.put('/special/organization/audit', data)
}

export function auditResources(data: SpecialAuditPayload) {
  return request.put('/special/resource/audit', data)
}
