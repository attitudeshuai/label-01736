import request from './request'

export const weeklyReportApi = {
  list(params) {
    return request.get('/weekly-reports', { params })
  },
  
  getById(id) {
    return request.get(`/weekly-reports/${id}`)
  },
  
  create(data) {
    return request.post('/weekly-reports', data)
  },
  
  update(id, data) {
    return request.put(`/weekly-reports/${id}`, data)
  },
  
  delete(id) {
    return request.delete(`/weekly-reports/${id}`)
  },
  
  submit(id) {
    return request.put(`/weekly-reports/${id}/submit`)
  },
  
  review(id, comment) {
    return request.put(`/weekly-reports/${id}/review`, { comment })
  }
}
