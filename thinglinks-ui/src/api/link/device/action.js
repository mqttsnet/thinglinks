import request from '@/utils/request'

// 查询设备动作数据列表
export function listAction(query) {
  return request({
    url: '/link/action/list',
    method: 'get',
    params: query
  })
}

// 查询设备动作数据详细
export function getAction(id) {
  return request({
    url: '/link/action/' + id,
    method: 'get'
  })
}

// 新增设备动作数据
export function addAction(data) {
  return request({
    url: '/link/action',
    method: 'post',
    data: data
  })
}

// 修改设备动作数据
export function updateAction(data) {
  return request({
    url: '/link/action',
    method: 'put',
    data: data
  })
}

// 删除设备动作数据
export function delAction(id) {
  return request({
    url: '/link/action/' + id,
    method: 'delete'
  })
}
