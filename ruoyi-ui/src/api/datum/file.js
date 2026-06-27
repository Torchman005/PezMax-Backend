import request from '@/utils/request'

// 查询试卷文件列表
export function listFile(query) {
    return request({
        url: '/datum/file/list',
        method: 'get',
        params: query
    })
}

// 查询试卷文件详细

//预览功能使用
export function getFile(fileId) {
    return request({
        url: '/datum/file/' + fileId,
        method: 'get'
    })
}

// 上传至 MinIO（ptmj 桶根目录）
export function uploadDatumFile(formData) {
    return request({
        url: '/datum/file/upload',
        method: 'post',
        timeout: 120000,
        data: formData,
        headers: { repeatSubmit: false },
        transformRequest: [(data, headers) => {
            if (data instanceof FormData) {
                delete headers['Content-Type']
            }
            return data
        }]
    })
}

// 新增试卷文件
export function addFile(data) {
    return request({
        url: '/datum/file',
        method: 'post',
        data: data
    })
}

// 修改试卷文件
export function updateFile(data) {
    return request({
        url: '/datum/file',
        method: 'put',
        data: data
    })
}

// 按上传用户ID批量通过未审核文件
export function approvePendingByUserId(userId) {
    return request({
        url: '/datum/file/approvePendingByUser/' + userId,
        method: 'put'
    })
}

// 删除试卷文件
export function delFile(fileId) {
    return request({
        url: '/datum/file/' + fileId,
        method: 'delete'
    })
}
