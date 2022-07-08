import request from '@/utils/request'

// 查询产品属性数据列表
export function listProperties(query) {
  return request({
    url: '/link/productProperties/list',
    method: 'get',
    params: query
  })
}

// 查询产品属性数据详细
export function getProperties(id) {
  return request({
    url: '/link/productProperties/' + id,
    method: 'get'
  })
}

// 新增产品属性数据
export function addProperties(data) {
  return request({
    url: '/link/productProperties',
    method: 'post',
    data: data
  })
}

// 修改产品属性数据
export function updateProperties(data) {
  return request({
    url: '/link/productProperties',
    method: 'put',
    data: data
  })
}

// 删除产品属性数据
export function delProperties(id) {
  return request({
    url: '/link/productProperties/' + id,
    method: 'delete'
  })
}
