import request from './request'

export function login(data) {
  return request.post('/auth/login', data)
}

export function register(data) {
  return request.post('/auth/register', data)
}

export function createUser(data) {
  return request.post('/users', data)
}

export function getUserList() {
  return request.get('/users')
}

export function getCurrentUser() {
  return request.get('/users/me')
}

export function updateUser(id, data) {
  return request.put(`/users/${id}`, data)
}

export function deleteUser(id) {
  return request.delete(`/users/${id}`)
}

export function updateUserStatus(id, status) {
  return request.put(`/users/${id}/status`, null, { params: { status } })
}
