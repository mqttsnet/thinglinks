import request from '@/utils/request'



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
