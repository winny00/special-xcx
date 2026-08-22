import { mapAppointmentIds, mapTeacherIds } from '@/api/snowflake'
import { describe, expect, it } from 'vitest'

describe('snowflake HTTP mappers', () => {
  it('maps appointment ids to strings without Number()', () => {
    const mapped = mapAppointmentIds({
      id: 9001,
      resourceId: 8001,
      teacherId: 1764000000000000008,
      resourceTitle: '咨询',
    })
    expect(mapped.id).toBe('9001')
    expect(mapped.resourceId).toBe('8001')
    expect(mapped.teacherId).toBe(String(1764000000000000008))
    expect(typeof mapped.id).toBe('string')
    expect(typeof mapped.resourceId).toBe('string')
    expect(typeof mapped.teacherId).toBe('string')
  })

  it('keeps appointment snowflake strings intact', () => {
    const id = '1766000000000000001'
    const mapped = mapAppointmentIds({ id, teacherId: '1764000000000000008' })
    expect(mapped.id).toBe(id)
    expect(mapped.teacherId).toBe('1764000000000000008')
    expect(mapped.resourceId).toBeUndefined()
  })

  it('maps teacher contract ids to strings', () => {
    const mapped = mapTeacherIds({
      id: 9001,
      userId: 1001,
      orgId: 7001,
      resourceId: 8001,
      name: '李老师',
    })
    expect(mapped).toEqual({
      id: '9001',
      userId: '1001',
      orgId: '7001',
      resourceId: '8001',
      name: '李老师',
    })
  })
})
