import { describe, expect, it } from 'vitest'
import { buildTeacherPayload } from './teacher-payload'
import type { SpecialTeacher } from '@/api/special'

const screenshotForm: SpecialTeacher = {
  name: '周老师',
  title: '语言干预',
  specialties: '耐心',
  qualification: '北京大学心理专家',
  avatarUrl: 'https://winny.oss-cn-beijing.aliyuncs.com/special/avatar.png',
  certImageUrl: '',
  intro: '北京大学心理专家',
  orgId: '北京大学',
  status: 0,
}

describe('buildTeacherPayload', () => {
  it('rejects org name so Jackson does not get a non-numeric orgId', () => {
    expect(() => buildTeacherPayload(screenshotForm)).toThrow('请选择已有机构，不要填写机构名称')
  })

  it('omits empty optional URLs', () => {
    const payload = buildTeacherPayload({
      ...screenshotForm,
      orgId: '',
    })
    expect(payload).not.toHaveProperty('certImageUrl')
    expect(payload.avatarUrl).toBe('https://winny.oss-cn-beijing.aliyuncs.com/special/avatar.png')
    expect(payload.orgId).toBeUndefined()
    expect(payload.status).toBe(0)
  })

  it('keeps snowflake orgId as string', () => {
    const payload = buildTeacherPayload({
      ...screenshotForm,
      orgId: '1938123456789012345',
    })
    expect(payload.orgId).toBe('1938123456789012345')
  })
})
