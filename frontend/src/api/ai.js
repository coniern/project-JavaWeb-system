import request from './request'

export function getAiSuggestions(data) {
  return request.post('/ai/assist', data)
}

export function getAiStatus() {
  return request.get('/ai/status')
}

export function getDashboardInsight() {
  return request.get('/ai/dashboard-insight')
}
