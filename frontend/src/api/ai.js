import request from './request'

export function getAiSuggestions(data) {
  return request.post('/ai/assist', data)
}
