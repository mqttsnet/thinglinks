import request from '@/utils/request'
import { praseStrEmpty } from "@/utils/thinglinks";

export function proOptions()  {
  return request({
    url: '/api/tdengine/shadow/proOptions',
    method: 'get',
  })
}

export function dataList(query) {
  return request({
    url: '/api/tdengine/shadow/dataList',
    method: 'get',
    params: query
  })
}

export function dataCharts() {
  return request({
    url: '/api/tdengine/shadow/dataCharts',
    method: 'get',
  })
}
