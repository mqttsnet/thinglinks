/**
 * 产品版本发布策略 ── 对齐后端 ProductPublishStrategyEnum:0 全量 / 1 灰度 / 2 影子。
 *
 * <p>注意与「版本状态 versionStatus」区分:后者是生命周期态
 * (草稿 0 / 已发布 1 / 灰度中 2 / 影子 3 / 已回滚 4 / 已归档 5),其 SHADOW=3;
 * 本枚举是「发布时所选策略」,SHADOW=2。版本选择器按本枚举过滤与打标签。</p>
 */
export enum ProductPublishStrategyEnum {
  /** 全量发布 */
  FULL = 0,
  /** 灰度发布 */
  CANARY = 1,
  /** 影子发布 */
  SHADOW = 2,
}

/** 全部发布策略 ── 设备绑定等「全策略可选」场景的默认值。 */
export const ALL_PUBLISH_STRATEGIES: number[] = [
  ProductPublishStrategyEnum.FULL,
  ProductPublishStrategyEnum.CANARY,
  ProductPublishStrategyEnum.SHADOW,
];
