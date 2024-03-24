import request from '@/utils/request'

// 查询子设备管理列表
export function listDeviceInfo(query) {
  return request({
    url: '/link/deviceInfo/list',
    method: 'get',
    params: query
  })
}

// 查询子设备管理详细
export function getDeviceInfo(id) {
  return request({
    url: '/link/deviceInfo/' + id,
    method: 'get'
  })
}

// 新增子设备管理
export function addDeviceInfo(data) {
  return request({
    url: '/link/deviceInfo',
    method: 'post',
    data: data
  })
}

// 修改子设备管理
export function updateDeviceInfo(data) {
  return request({
    url: '/link/deviceInfo',
    method: 'put',
    data: data
  })
}

// 删除子设备管理
export function delDeviceInfo(id) {
  return request({
    url: '/link/deviceInfo/' + id,
    method: 'delete'
  })
}

// 查询子设备影子数据
export function getDeviceInfoShadow(data) {
  return request({
    url: '/link/deviceInfo/getDeviceInfoShadow',
    method: 'post',
    data: data
  })
}
