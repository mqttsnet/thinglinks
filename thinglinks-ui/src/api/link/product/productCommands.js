import request from '@/utils/request'

// 查询产品指令数据列表
export function listCommands(query) {
  return request({
    url: '/link/productCommands/list',
    method: 'get',
    params: query
  })
}

// 查询产品指令数据详细
export function getCommands(id) {
  return request({
    url: '/link/productCommands/' + id,
    method: 'get'
  })
}

// 新增产品指令数据
export function addCommands(data) {
  return request({
    url: '/link/productCommands',
    method: 'post',
    data: data
  })
}

// 修改产品指令数据
export function updateCommands(data) {
  return request({
    url: '/link/productCommands',
    method: 'put',
    data: data
  })
}

// 删除产品指令数据
export function delCommands(id) {
  return request({
    url: '/link/productCommands/' + id,
    method: 'delete'
  })
}
