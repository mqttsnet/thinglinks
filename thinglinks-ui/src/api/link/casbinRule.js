import request from '@/utils/request'

// 查询CAS规则管理列表
export function listCasbinRule(query) {
  return request({
    url: '/link/casbinRule/list',
    method: 'get',
    params: query
  })
}

// 查询CAS规则管理详细
export function getCasbinRule(id) {
  return request({
    url: '/link/casbinRule/' + id,
    method: 'get'
  })
}

// 新增CAS规则管理
export function addCasbinRule(data) {
  return request({
    url: '/link/casbinRule',
    method: 'post',
    data: data
  })
}

// 修改CAS规则管理
export function updateCasbinRule(data) {
  return request({
    url: '/link/casbinRule',
    method: 'put',
    data: data
  })
}

// 删除CAS规则管理
export function delCasbinRule(id) {
  return request({
    url: '/link/casbinRule/' + id,
    method: 'delete'
  })
}
