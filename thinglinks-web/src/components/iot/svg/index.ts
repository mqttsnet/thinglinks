/**
 * 产品 + 设备类型 SVG 图标聚合导出。
 *
 * 产品类型与后端 {@code ProductTypeEnum} 对齐:
 *   - 1 = COMMON  普通产品
 *   - 2 = GATEWAY 网关产品
 *
 * 设备类型与后端 {@code DeviceNodeTypeEnum} 对齐:
 *   - 0 = COMMON     普通设备(直连)
 *   - 1 = GATEWAY    网关设备(挂载子设备)
 *   - 2 = SUBDEVICE  子设备(挂在网关下)
 *
 * 用法:
 *   import { getProductTypeSvg, getDeviceNodeTypeSvg } from '/@/components/iot/svg';
 *   <component :is="getProductTypeSvg(record.productType)" />
 *   <component :is="getDeviceNodeTypeSvg(record.nodeType)" />
 */
import type { Component } from 'vue';

import CommonProductSvg from './CommonProductSvg.vue';
import GatewayProductSvg from './GatewayProductSvg.vue';
import CommonDeviceSvg from './CommonDeviceSvg.vue';
import GatewayDeviceSvg from './GatewayDeviceSvg.vue';
import SubDeviceSvg from './SubDeviceSvg.vue';

export {
  CommonProductSvg,
  GatewayProductSvg,
  CommonDeviceSvg,
  GatewayDeviceSvg,
  SubDeviceSvg,
};

/**
 * 按 productType 取对应的 SVG 组件。
 *
 * @param productType 产品类型(1=COMMON / 2=GATEWAY,接受 number | string)
 * @returns 对应的 Vue SVG 组件;未匹配时 fallback 到 CommonProductSvg
 */
export function getProductTypeSvg(productType?: number | string | null): Component {
  const v = Number(productType);
  if (v === 2) return GatewayProductSvg;
  return CommonProductSvg;
}

/**
 * 按设备 nodeType 取对应的 SVG 组件。
 *
 * @param nodeType 设备节点类型(0=COMMON / 1=GATEWAY / 2=SUBDEVICE,接受 number | string)
 * @returns 对应的 Vue SVG 组件;未匹配时 fallback 到 CommonDeviceSvg
 */
export function getDeviceNodeTypeSvg(nodeType?: number | string | null): Component {
  const v = Number(nodeType);
  if (v === 1) return GatewayDeviceSvg;
  if (v === 2) return SubDeviceSvg;
  return CommonDeviceSvg;
}
