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

// 启用协议管理
export function enable(id) {
  return request({
    url: '/link/protocol/enable/' + id,
    method: 'get'
  })
}

// 停用协议管理
export function disable(id) {
  return request({
    url: '/link/protocol/disable/' + id,
    method: 'get'
  })
}

// 动态编译代码
export function dynamicallyXcode(data) {
  return request({
    url: '/link/protocolCompileXcode/dynamicallyXcode',
    method: 'post',
    data: data
  })
}
