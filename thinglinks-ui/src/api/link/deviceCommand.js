// 设备命令相关接口
import request from "@/utils/request";

export function sendCustomMessage(data) {
  return request({
    url: '/link/deviceCommand/sendCustomMessage',
    method: 'post',
    data: data
  })
}
