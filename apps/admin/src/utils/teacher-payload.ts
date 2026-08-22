import type { SpecialTeacher } from '@/api/special'

const SNOWFLAKE_ID = /^\d+$/

function omitBlank(value?: string): string | undefined {
  const trimmed = value?.trim()
  return trimmed ? trimmed : undefined
}

function assignIfPresent(target: SpecialTeacher, key: keyof SpecialTeacher, value: string | undefined) {
  if (value)
    (target as Record<string, unknown>)[key] = value
}

/**
 * Build the teacher create/update body.
 * orgId must be a snowflake ID string (never an org name).
 * Empty optional URLs are omitted so Jackson/URL checks do not see "".
 */
export function buildTeacherPayload(form: SpecialTeacher): SpecialTeacher {
  const orgId = omitBlank(form.orgId)
  if (orgId && !SNOWFLAKE_ID.test(orgId))
    throw new Error('请选择已有机构，不要填写机构名称')

  const payload: SpecialTeacher = {
    name: form.name,
    status: form.status,
  }
  if (form.id)
    payload.id = String(form.id)
  assignIfPresent(payload, 'title', omitBlank(form.title))
  assignIfPresent(payload, 'specialties', omitBlank(form.specialties))
  assignIfPresent(payload, 'qualification', omitBlank(form.qualification))
  assignIfPresent(payload, 'avatarUrl', omitBlank(form.avatarUrl))
  assignIfPresent(payload, 'certImageUrl', omitBlank(form.certImageUrl))
  assignIfPresent(payload, 'orgId', orgId)
  assignIfPresent(payload, 'intro', omitBlank(form.intro))
  if (form.resourceId)
    payload.resourceId = String(form.resourceId)
  return payload
}
