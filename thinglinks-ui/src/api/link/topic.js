import request from '@/utils/request'

// 查询设备Topic数据列表
export function listTopic(query) {
  return request({
    url: '/link/topic/list',
    method: 'get',
    params: query
  })
}

// 查询设备Topic数据详细
export function getTopic(id) {
  return request({
    url: '/link/topic/' + id,
    method: 'get'
  })
}

// 新增设备Topic数据
export function addTopic(data) {
  return request({
    url: '/link/topic',
    method: 'post',
    data: data
  })
}

// 修改设备Topic数据
export function updateTopic(data) {
  return request({
    url: '/link/topic',
    method: 'put',
    data: data
  })
}

// 删除设备Topic数据
export function delTopic(id) {
  return request({
    url: '/link/topic/' + id,
    method: 'delete'
  })
}
