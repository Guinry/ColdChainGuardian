const AUDIT_STORAGE_KEY = 'ccg_audit_events'
const MAX_AUDIT_EVENTS = 300

const readEvents = () => {
  try {
    const raw = localStorage.getItem(AUDIT_STORAGE_KEY)
    return raw ? JSON.parse(raw) : []
  } catch {
    return []
  }
}

const writeEvents = (events) => {
  try {
    localStorage.setItem(AUDIT_STORAGE_KEY, JSON.stringify(events.slice(0, MAX_AUDIT_EVENTS)))
  } catch {
    // Ignore storage quota errors; audit logs are a convenience layer.
  }
}

export const getAuditEvents = () => readEvents()

export const clearAuditEvents = () => writeEvents([])

export const recordAuditEvent = (event) => {
  const events = readEvents()
  const user = (() => {
    try {
      const raw = localStorage.getItem('user') || sessionStorage.getItem('user')
      return raw ? JSON.parse(raw) : null
    } catch {
      return null
    }
  })()

  events.unshift({
    id: `${Date.now()}-${Math.random().toString(16).slice(2)}`,
    time: new Date().toISOString(),
    operator: user?.realName || user?.username || '未登录用户',
    role: user?.role || '-',
    method: event.method?.toUpperCase() || '-',
    url: event.url || '-',
    status: event.status || '-',
    result: event.result || 'UNKNOWN',
    message: event.message || ''
  })

  writeEvents(events)
}
