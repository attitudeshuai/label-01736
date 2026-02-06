import request from './request'

export const achievementApi = {
  list(params) {
    return request.get('/achievements', { params })
  },
  
  getById(id) {
    return request.get(`/achievements/${id}`)
  },
  
  create(data) {
    return request.post('/achievements', data)
  },
  
  update(id, data) {
    return request.put(`/achievements/${id}`, data)
  },
  
  delete(id) {
    return request.delete(`/achievements/${id}`)
  },
  
  getStatistics(userId) {
    return request.get('/achievements/statistics', { params: { userId } })
  },
  
  getPublishedPapers() {
    return request.get('/achievements/published-papers')
  }
}
