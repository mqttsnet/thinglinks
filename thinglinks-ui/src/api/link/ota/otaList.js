import request from '@/utils/request'
import {
  praseStrEmpty
} from "@/utils/thinglinks";

// 查询ota列表
export function listOta(data) {
  return request({
    url: '/link/otaUpgrades/page',
    method: 'post',
    data: data
  })
}

// 删除ota
export function deleteOta(data) {
  return request({
    url: '/link/otaUpgrades',
    method: 'delete',
    data: data
  })
}