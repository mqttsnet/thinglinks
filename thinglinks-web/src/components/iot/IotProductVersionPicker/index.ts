/**
 * IoT 产品版本选择器统一导出。
 *
 * <p>使用样例:
 * <pre>
 *   // 选某产品下的目标版本(versionNo)。产品标识为空时自动 disabled
 *   &lt;IotProductVersionPicker
 *     v-model="versionNo"
 *     :productIdentification="productIdent"
 *   /&gt;
 *
 *   // 按发布策略过滤(默认 全量 / 灰度 / 影子;如 OTA 只取影子)
 *   &lt;IotProductVersionPicker
 *     v-model="versionNo"
 *     :productIdentification="productIdent"
 *     :publish-strategies="[ProductPublishStrategyEnum.SHADOW]"
 *   /&gt;
 * </pre>
 *
 * @author mqttsnet
 */
import IotProductVersionPicker from './IotProductVersionPicker.vue';

export { IotProductVersionPicker };
