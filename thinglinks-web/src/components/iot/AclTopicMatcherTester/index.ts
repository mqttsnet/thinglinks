/**
 * ACL 主题匹配测试器 ── IoT 通用组件,可在 ACL 详情 / 产品 topic 编辑等场景复用。
 *
 * <p>使用样例:
 * <pre>
 *   import {
 *     AclTopicMatcherTesterModal,
 *   } from '/@/components/iot/AclTopicMatcherTester';
 *
 *   const [registerTester, { openModal }] = useModal();
 *
 *   function handleTest() {
 *     openModal(true, {
 *       rule: {
 *         ruleName: '车间温度订阅',
 *         topicPattern: 'factory/+/temp/${device_identification}/data',
 *         decision: true,
 *         actionType: 'SUBSCRIBE',
 *         priority: 100,
 *         enabled: true,
 *       },
 *       presetProductIdentification: 'pid-xxx',  // 可选预填
 *       presetDeviceIdentification: 'did-xxx',   // 可选预填
 *     });
 *   }
 * </pre>
 */
import AclTopicMatcherTesterModal from './AclTopicMatcherTesterModal.vue';

export { AclTopicMatcherTesterModal };
export type {
  AclLevelDiff,
  AclPlaceholderKey,
  AclPlaceholderValues,
  AclTestResult,
  TestableAclRule,
} from './types';
export {
  detectUsedPlaceholders,
  diffLevels,
  isTopicMatch,
  replacePlaceholders,
  testAclMatch,
  validatePattern,
  validateTopic,
} from './matcher';
