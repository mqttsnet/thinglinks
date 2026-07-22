/**
 * IoT 应用业务组件统一导出。
 *
 * <p>每个组件均通过 {@link withInstall} 包装,既可作为局部组件 `import` 使用,
 * 也可以注册为全局组件;调用方推荐用命名导入:
 * <pre>
 *   import { AllOrCustomPicker } from '/@/components/iot';
 * </pre>
 *
 * <p>子模块原始路径仍可直接引用(不破坏既有调用方):
 * <pre>
 *   import { AllOrCustomPicker } from '/@/components/iot/AllOrCustomPicker';
 * </pre>
 *
 * @author mqttsnet
 * @since 2026-05-08
 */
import { withInstall } from '/@/utils';
import basicDeviceSelector from './BasicSelect/BasicDevice/BasicDeviceSelector.vue';
import basicAreaSelector from './BasicAreaSelection/BasicAreaSelector.vue';
import basicSelectDeviceModal from './BasicSelectDeviceModal/BasicSelectDeviceModal.vue';
import allOrCustomPicker from './AllOrCustomPicker/AllOrCustomPicker.vue';
import allOrCustomPickerModal from './AllOrCustomPicker/PickerModal.vue';
import productTopicPicker from './ProductTopicPicker/ProductTopicPicker.vue';
import productTopicPickerModal from './ProductTopicPicker/PickerModal.vue';
import basicEntityPicker from './BasicEntityPicker/BasicEntityPicker.vue';
import basicEntityPickerModal from './BasicEntityPicker/PickerModal.vue';
import iotProductPicker from './IotProductDevicePicker/IotProductPicker.vue';
import iotDevicePicker from './IotProductDevicePicker/IotDevicePicker.vue';
import iotAllOrCustomProductPicker from './IotProductDevicePicker/IotAllOrCustomProductPicker.vue';
import iotAllOrCustomDevicePicker from './IotProductDevicePicker/IotAllOrCustomDevicePicker.vue';
import aclTopicMatcherTesterModal from './AclTopicMatcherTester/AclTopicMatcherTesterModal.vue';
import productVersionDiffViewer from './ProductVersionDiffViewer/ProductVersionDiffViewer.vue';
import productChangeLogPanel from './ProductChangeLogPanel/ProductChangeLogPanel.vue';
import snapshotIdTag from './SnapshotIdTag/SnapshotIdTag.vue';
import secretField from './SecretField/SecretField.vue';
import propertyMethodBadge from './PropertyMethodBadge/PropertyMethodBadge.vue';
import propertyTrendChart from './PropertyTrendChart/PropertyTrendChart.vue';
import scriptDebugPanel from './ScriptDebugPanel/index.vue';
import scriptTemplatePicker from './ScriptTemplatePicker/index.vue';

export const BasicDeviceSelector = withInstall(basicDeviceSelector);
export const BasicAreaSelector = withInstall(basicAreaSelector);
export const BasicSelectDeviceModal = withInstall(basicSelectDeviceModal);
export const AllOrCustomPicker = withInstall(allOrCustomPicker);
export const AllOrCustomPickerModal = withInstall(allOrCustomPickerModal);
export const ProductTopicPicker = withInstall(productTopicPicker);
export const ProductTopicPickerModal = withInstall(productTopicPickerModal);
export const BasicEntityPicker = withInstall(basicEntityPicker);
export const BasicEntityPickerModal = withInstall(basicEntityPickerModal);
export const IotProductPicker = withInstall(iotProductPicker);
export const IotDevicePicker = withInstall(iotDevicePicker);
export const IotAllOrCustomProductPicker = withInstall(iotAllOrCustomProductPicker);
export const IotAllOrCustomDevicePicker = withInstall(iotAllOrCustomDevicePicker);
export const AclTopicMatcherTesterModal = withInstall(aclTopicMatcherTesterModal);
export const ProductVersionDiffViewer = withInstall(productVersionDiffViewer);
export const ProductChangeLogPanel = withInstall(productChangeLogPanel);
export const SnapshotIdTag = withInstall(snapshotIdTag);
export const SecretField = withInstall(secretField);
export const PropertyMethodBadge = withInstall(propertyMethodBadge);
export const PropertyTrendChart = withInstall(propertyTrendChart);
export const ScriptDebugPanel = withInstall(scriptDebugPanel);
export const ScriptTemplatePicker = withInstall(scriptTemplatePicker);

// AllOrCustomPicker 模块的 TS 类型一并 re-export,调用方一处引入
export type {
  PickerValue,
  PickerRawValue,
  PickerDescField,
  PickerFilter,
  PickerFilterType,
  PickerPageRequest,
  PickerPageResponse,
  PickerDetailApi,
  PickerTriggerStyle,
  PickerTriggerLabels,
  PickerExpose,
} from './AllOrCustomPicker/types';

// ProductTopicPicker 类型 re-export
export type {
  ProductTopicPickerMode,
  ProductTopicRecord,
  ProductTopicPickerTriggerStyle,
} from './ProductTopicPicker/types';

// BasicEntityPicker 类型 re-export
export type {
  EntityRawValue,
  EntityValue,
  EntityDescField,
  EntityFilter,
  EntityPageRequest,
  EntityPageResponse,
  EntityDetailApi,
  EntityTriggerStyle,
  EntityTriggerLabels,
  EntityPickerExpose,
} from './BasicEntityPicker/types';

export * from './types';
