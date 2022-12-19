import request from '@/utils/request'



// 分页查询规则列表
export function listRule(params) {
  return request({
    url: `/rule/rule/list`,
    method: 'get',
    params
  })
}
// 删除规则
export function deleteRule(id) {
  return request({
    url: `/rule/rule/${id}`,
    method: 'delete'
  })
}

// 获取所有产品
export function listProduct() {
  return request({
    url: `/rule/ruleDeviceLinkage/selectAllProduct`,
    method: 'get'
  })
}

//根据产品标识获取产品关联设备
export function listDevice(productIdentification) {
  return request({
    url: `/rule/ruleDeviceLinkage/selectDeviceByProductIdentification/${productIdentification}`,
    method: 'get'
  })
}
//根据产品标识获取产品关联服务
export function listService(productIdentification) {
  return request({
    url: `/rule/ruleDeviceLinkage/selectProductServicesByProductIdentification/${productIdentification}`,
    method: 'get'
  })
}

//根据产品id获取产品属性
export function listProperties(serviceId) {
  return request({
    url: `/rule/ruleDeviceLinkage/selectProductPropertiesByServiceId/${serviceId}`,
    method: 'get'
  })
}


// 提交规则基本信息
export function ruleSaveBasic(data) {
  return request({
    url: '/rule/rule',
    method: 'post',
    data
  })
}
