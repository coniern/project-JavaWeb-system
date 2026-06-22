import request from './request'

export function getTaskList(params) {
  return request.get('/task/list', { params })
}

export function getTaskDetail(id) {
  return request.get(`/task/${id}`)
}

export function createTask(data) {
  return request.post('/task/create', data)
}

export function updateTask(data) {
  return request.put('/task/update', data)
}

export function deleteTask(id) {
  return request.delete(`/task/delete/${id}`)
}

export function getTaskStats(projectId) {
  return request.get(`/task/stats/${projectId}`)
}
