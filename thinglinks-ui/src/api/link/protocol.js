import request from '@/utils/request'

// 查询协议管理列表
export function listProtocol(query) {
  return request({
    url: '/link/protocol/list',
    method: 'get',
    params: query
  })
}

// 查询协议管理详细
export function getProtocol(id) {
  return request({
    url: '/link/protocol/' + id,
    method: 'get'
  })
}

// 新增协议管理
export function addProtocol(data) {
  return request({
    url: '/link/protocol',
    method: 'post',
    data: data
  })
}

// 修改协议管理
export function updateProtocol(data) {
  return request({
    url: '/link/protocol',
    method: 'put',
    data: data
  })
}

// 删除协议管理
export function delProtocol(id) {
  return request({
    url: '/link/protocol/' + id,
    method: 'delete'
  })
}
