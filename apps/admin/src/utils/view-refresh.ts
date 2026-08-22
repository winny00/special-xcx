export function viewRefreshKey(path: string, tick: number): string {
  return `${path}:${tick}`
}

export function incrementRefreshTick(tick: number): number {
  return tick + 1
}
