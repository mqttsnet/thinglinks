import request from '@/utils/request'

// 查询产品模型设备响应服务命令属性列表
export function listResponse(query) {
  return request({
    url: '/link/product/commands/response/list',
    method: 'get',
    params: query
  })
}

// 查询产品模型设备响应服务命令属性详细
export function getResponse(id) {
  return request({
    url: '/link/product/commands/response/' + id,
    method: 'get'
  })
}

// 新增产品模型设备响应服务命令属性
export function addResponse(data) {
  return request({
    url: '/link/product/commands/response',
    method: 'post',
    data: data
  })
}

// 修改产品模型设备响应服务命令属性
export function updateResponse(data) {
  return request({
    url: '/link/product/commands/response',
    method: 'put',
    data: data
  })
}

// 删除产品模型设备响应服务命令属性
export function delResponse(id) {
  return request({
    url: '/link/product/commands/response/' + id,
    method: 'delete'
  })
}
