import request from '@/utils/request'

// 查询外部书签列表
export function listBookmark(query) {
  return request({
    url: '/datum/bookmark/list',
    method: 'get',
    params: query
  })
}

// 查询外部书签详细
export function getBookmark(id) {
  return request({
    url: '/datum/bookmark/' + id,
    method: 'get'
  })
}

// 新增外部书签
export function addBookmark(data) {
  return request({
    url: '/datum/bookmark',
    method: 'post',
    data: data
  })
}

// 修改外部书签
export function updateBookmark(data) {
  return request({
    url: '/datum/bookmark',
    method: 'put',
    data: data
  })
}

// 删除外部书签
export function delBookmark(id) {
  return request({
    url: '/datum/bookmark/' + id,
    method: 'delete'
  })
}
