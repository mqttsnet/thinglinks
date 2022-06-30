import request from '@/utils/request'

// 查询设备位置列表
export function listDeviceLocation(query) {
  return request({
    url: '/link/device_location/list',
    method: 'get',
    params: query
  })
}

// 查询设备位置详细
export function getDeviceLocation(id) {
  return request({
    url: '/link/device_location/' + id,
    method: 'get'
  })
}

// 新增设备位置
export function addDeviceLocation(data) {
  return request({
    url: '/link/device_location',
    method: 'post',
    data: data
  })
}

// 修改设备位置
export function updateDeviceLocation(data) {
  return request({
    url: '/link/device_location',
    method: 'put',
    data: data
  })
}

// 删除设备位置
export function delDeviceLocation(id) {
  return request({
    url: '/link/device_location/' + id,
    method: 'delete'
  })
}
