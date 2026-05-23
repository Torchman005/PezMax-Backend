import request from '@/utils/request'

// 查询通知列表
export function listNotification(query) {
  return request({
    url: '/system/notification/list',
    method: 'get',
    params: query
  })
}

// 查询通知详细
export function getNotification(notifyId) {
  return request({
    url: '/system/notification/' + notifyId,
    method: 'get'
  })
}

// 新增通知
export function addNotification(data) {
  return request({
    url: '/system/notification',
    method: 'post',
    data: data
  })
}

// 修改通知
export function updateNotification(data) {
  return request({
    url: '/system/notification',
    method: 'put',
    data: data
  })
}

// 删除通知
export function delNotification(notifyId) {
  return request({
    url: '/system/notification/' + notifyId,
    method: 'delete'
  })
}
