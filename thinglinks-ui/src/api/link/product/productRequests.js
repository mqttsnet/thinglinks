import request from '@/utils/request'

// 查询产品模型设备下发服务命令属性列表
export function listRequests(query) {
  return request({
    url: '/link/product/commands/requests/list',
    method: 'get',
    params: query
  })
}

// 查询产品模型设备下发服务命令属性详细
export function getRequests(id) {
  return request({
    url: '/link/product/commands/requests/' + id,
    method: 'get'
  })
}

// 新增产品模型设备下发服务命令属性
export function addRequests(data) {
  return request({
    url: '/link/product/commands/requests',
    method: 'post',
    data: data
  })
}

// 修改产品模型设备下发服务命令属性
export function updateRequests(data) {
  return request({
    url: '/link/product/commands/requests',
    method: 'put',
    data: data
  })
}

// 删除产品模型设备下发服务命令属性
export function delRequests(id) {
  return request({
    url: '/link/product/commands/requests/' + id,
    method: 'delete'
  })
}
