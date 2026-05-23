import request from '@/utils/request'

// 查询书签举报列表
export function listBookmarkReport(query) {
    return request({
        url: '/datum/bookmarkReport/list',
        method: 'get',
        params: query
    })
}

// 查询书签举报详细
export function getBookmarkReport(reportId) {
    return request({
        url: '/datum/bookmarkReport/' + reportId,
        method: 'get'
    })
}

// 新增书签举报
export function addBookmarkReport(data) {
    return request({
        url: '/datum/bookmarkReport',
        method: 'post',
        data: data
    })
}

// 修改书签举报
export function updateBookmarkReport(data) {
    return request({
        url: '/datum/bookmarkReport',
        method: 'put',
        data: data
    })
}

// 删除书签举报
export function delBookmarkReport(reportId) {
    return request({
        url: '/datum/bookmarkReport/' + reportId,
        method: 'delete'
    })
}

// 处理书签举报
export function handleBookmarkReport(reportId, result) {
    return request({
        url: '/datum/bookmarkReport/handle',
        method: 'post',
        params: { reportId, result }
    })
}

// 管理员审核书签举报
export function auditBookmarkReport(data) {
    return request({
        url: '/datum/bookmarkReport/audit',
        method: 'put',
        data: data
    })
}

// 查询书签举报时间线
export function getBookmarkReportTimeline(reportId) {
    return request({
        url: '/datum/bookmarkReport/timeline/' + reportId,
        method: 'get'
    })
}
