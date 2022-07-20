import request from '@/utils/request'

// 查询产品模板列表
export function listProductTemplate(query) {
  return request({
    url: '/link/productTemplate/list',
    method: 'get',
    params: query
  })
}

// 查询产品模板详细
export function getProductTemplate(id) {
  return request({
    url: '/link/productTemplate/' + id,
    method: 'get'
  })
}

// 查询产品模板详细
export function getProductTemplateFull(id) {
  return request({
    url: '/link/productTemplate/getFull/' + id,
    method: 'get'
  })
}

// 新增产品模板
export function addProductTemplate(data) {
  return request({
    url: '/link/productTemplate',
    method: 'post',
    data: data
  })
}

// 修改产品模板
export function updateProductTemplate(data) {
  return request({
    url: '/link/productTemplate',
    method: 'put',
    data: data
  })
}

// 删除产品模板
export function delProductTemplate(id) {
  return request({
    url: '/link/productTemplate/' + id,
    method: 'delete'
  })
}
