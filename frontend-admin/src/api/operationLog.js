import request from './request'

export const operationLogApi = {
  list(params) {
    return request.get('/operation-logs', { params })
  }
}
