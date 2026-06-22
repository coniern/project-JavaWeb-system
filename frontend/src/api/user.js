import request from './request'

export function login(data) {
  return request.post('/user/login', data)
}

export function register(data) {
  return request.post('/user/register', data)
}

export function createUser(data) {
  return request.post('/user/create', data)
}

export function getUserList() {
  return request.get('/user/list')
}

export function getUserInfo(token) {
  return request.get('/user/info', { params: { token } })
}

export function updateUser(data) {
  return request.put('/user/update', data)
}

export function deleteUser(id) {
  return request.delete(`/user/delete/${id}`)
}
