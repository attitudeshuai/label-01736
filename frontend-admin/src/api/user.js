import request from './request'

export const userApi = {
  list(params) {
    return request.get('/users', { params })
  },
  
  getById(id) {
    return request.get(`/users/${id}`)
  },
  
  create(data) {
    return request.post('/users', data)
  },
  
  update(id, data) {
    return request.put(`/users/${id}`, data)
  },
  
  delete(id) {
    return request.delete(`/users/${id}`)
  },
  
  getTeachers() {
    return request.get('/users/teachers')
  },
  
  getStudents() {
    return request.get('/users/students')
  }
}
