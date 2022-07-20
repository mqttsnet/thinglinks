import request from '@/utils/request'

// 查询产品服务数据列表 带分页
export function listServices(query) {
  return request({
    url: '/link/productServices/list',
    method: 'get',
    params: query
  })
}
// 查询产品服务数据列表
export function queryServices(query) {
  return request({
    url: '/link/productServices/query',
    method: 'get',
    params: query
  })
}

// 查询产品服务数据详细
export function getServices(id) {
  return request({
    url: '/link/productServices/' + id,
    method: 'get'
  })
}

// 新增产品服务数据
export function addServices(data) {
  return request({
    url: '/link/productServices',
    method: 'post',
    data: data
  })
}

// 修改产品服务数据
export function updateServices(data) {
  return request({
    url: '/link/productServices',
    method: 'put',
    data: data
  })
}

// 删除产品服务数据
export function delServices(id) {
  return request({
    url: '/link/productServices/' + id,
    method: 'delete'
  })
}
