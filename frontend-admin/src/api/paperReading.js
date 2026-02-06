import request from './request'

export const paperReadingApi = {
  list(params) {
    return request.get('/paper-readings', { params })
  },
  
  getById(id) {
    return request.get(`/paper-readings/${id}`)
  },
  
  create(data) {
    return request.post('/paper-readings', data)
  },
  
  update(id, data) {
    return request.put(`/paper-readings/${id}`, data)
  },
  
  delete(id) {
    return request.delete(`/paper-readings/${id}`)
  }
}
