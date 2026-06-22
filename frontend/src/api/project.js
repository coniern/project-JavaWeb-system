import request from './request'

export function getProjectList(params) {
  return request.get('/project/list', { params })
}

export function getProjectDetail(id) {
  return request.get(`/project/${id}`)
}

export function createProject(data) {
  return request.post('/project/create', data)
}

export function updateProject(data) {
  return request.put('/project/update', data)
}

export function deleteProject(id) {
  return request.delete(`/project/delete/${id}`)
}

export function getProjectStats() {
  return request.get('/project/stats')
}
