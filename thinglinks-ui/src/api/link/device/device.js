import request from '@/utils/request'
import {
  praseStrEmpty
} from "@/utils/thinglinks";

// 查询设备管理列表
export function listDevice(query) {
  return request({
    url: '/link/device/list',
    method: 'get',
    params: query
  })
}
// 查询设备统计接口
export function listStatusCount(query) {
  return request({
    url: '/link/device/listStatusCount',
    method: 'get',
    params: query
  })
}

// 查询设备管理详细
export function getDevice(id) {
  return request({
    url: '/link/device/' + praseStrEmpty(id),
    method: 'get'
  })
}

// 新增设备管理
export function addDevice(data) {
  return request({
    url: '/link/device',
    method: 'post',
    data: data
  })
}

// 修改设备管理
export function updateDevice(data) {
  return request({
    url: '/link/device',
    method: 'put',
    data: data
  })
}

// 删除设备管理
export function delDevice(id) {
  return request({
    url: '/link/device/' + id,
    method: 'delete'
  })
}

// 断开设备连接
export function disconnectDevice(id) {
  return request({
    url: '/link/device/disconnect/' + id,
    method: 'post'
  })
}

// 校验clientId是否存在
export function validationDeviceIdentification_clientId(clientId) {
  return request({
    url: '/link/device/validationFindOneByClientId/' + praseStrEmpty(clientId),
    method: 'get'
  })
}

// 校验设备标识是否存在
export function validationDeviceIdentification_deviceIdentification(deviceIdentification) {
  return request({
    url: '/link/device/validationFindOneByDeviceIdentification/' + praseStrEmpty(deviceIdentification),
    method: 'get'
  })
}

// 查询普通设备影子数据
export function getDeviceShadow(data) {
  return request({
    url: '/link/shadow/getDeviceShadow',
    method: 'post',
    data: data
  })
}
// 查询设备告警信息列表
export function listAlarm(query) {
  return request({
    url: '/link/alarm/list',
    method: 'get',
    params: query
  })
}
