/** Raw JSON snowflake before the HTTP mapper. Never Number() these. */
export type RawSnowflake = string | number | null | undefined

export function asSnowflakeId(value: unknown): string {
  return String(value ?? '')
}

export function asOptionalSnowflakeId(value: unknown): string | undefined {
  if (value == null || value === '') {
    return undefined
  }
  return String(value)
}

export function mapAppointmentIds<T extends Record<string, unknown>>(row: T) {
  return {
    ...row,
    id: asSnowflakeId(row.id),
    resourceId: asOptionalSnowflakeId(row.resourceId),
    teacherId: asOptionalSnowflakeId(row.teacherId),
  }
}

export function mapTeacherIds<T extends Record<string, unknown>>(row: T) {
  return {
    ...row,
    id: asSnowflakeId(row.id),
    userId: asOptionalSnowflakeId(row.userId),
    orgId: asOptionalSnowflakeId(row.orgId),
    resourceId: asOptionalSnowflakeId(row.resourceId),
  }
}
