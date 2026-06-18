/**
 * OTA 场景 SVG 图标聚合导出(等距 3D 玻璃质感,适配 flexy 蓝色调)。
 *
 * 升级包类型与后端 {@code OtaPackageTypeEnum} 对齐:
 *   - 0 = SOFTWARE 软件包
 *   - 1 = FIRMWARE 固件包
 *
 * 用法:
 *   import { getOtaPackageTypeSvg, OtaTaskSvg, OtaRecordStatusBadge } from '/@/components/iot/ota/svg';
 *   <component :is="getOtaPackageTypeSvg(record.packageType)" />   // 资源卡片 / 详情页
 *   <OtaTaskSvg />                                                 // 升级任务卡片
 *   <OtaRecordStatusBadge :status="record.upgradeStatus" :size="48" /> // 升级记录状态徽标(flexy 指标卡同款)
 */
import type { Component } from 'vue';

import FirmwarePackageSvg from './FirmwarePackageSvg.vue';
import SoftwarePackageSvg from './SoftwarePackageSvg.vue';
import OtaTaskSvg from './OtaTaskSvg.vue';
import OtaRecordStatusBadge from './OtaRecordStatusBadge.vue';

export { FirmwarePackageSvg, SoftwarePackageSvg, OtaTaskSvg, OtaRecordStatusBadge };

/**
 * 按升级包类型取对应的 SVG 组件。
 *
 * @param packageType 升级包类型(0=软件包 / 1=固件包,接受 number | string)
 * @returns 对应的 Vue SVG 组件;固件包→芯片,其余(软件包)→分层包裹
 */
export function getOtaPackageTypeSvg(packageType?: number | string | null): Component {
  return Number(packageType) === 1 ? FirmwarePackageSvg : SoftwarePackageSvg;
}
