/**
 * IoT 产品/设备选择器统一导出。
 *
 * <p>使用样例:
 * <pre>
 *   // 选产品
 *   &lt;IotProductPicker v-model="productIdent" /&gt;
 *
 *   // 选设备(必须指定关联产品,否则自动 disabled)
 *   &lt;IotDevicePicker v-model="deviceIdent" :productIdentification="productIdent" /&gt;
 *
 *   // 覆盖默认文案 / 卡片字段(可选)
 *   &lt;IotProductPicker
 *     v-model="productIdent"
 *     :title="t('xxx.dialog.pickProduct')"
 *     :triggerLabels="{ empty: '...', button: '...' }"
 *   /&gt;
 * </pre>
 *
 * @author mqttsnet
 */
import IotProductPicker from './IotProductPicker.vue';
import IotDevicePicker from './IotDevicePicker.vue';
import IotAllOrCustomProductPicker from './IotAllOrCustomProductPicker.vue';
import IotAllOrCustomDevicePicker from './IotAllOrCustomDevicePicker.vue';

export {
  IotProductPicker,
  IotDevicePicker,
  IotAllOrCustomProductPicker,
  IotAllOrCustomDevicePicker,
};
