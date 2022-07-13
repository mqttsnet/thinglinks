// 新增设备动作数据
import request from "@/utils/request";

export function sendMsg(data) {
  return request({
    url: '/broker/publish/sendMessage',
    method: 'post',
    data: data
  })
}
