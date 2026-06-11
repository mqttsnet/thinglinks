/**
 * 拉流代理相关枚举定义
 */

/**
 * 流代理类型枚举值（与后端 VideoStreamProxyTypeEnum 对应）。
 *
 * 注意取值用小写，因为字典 / 后端枚举 value 都是小写。
 */
export enum VideoStreamProxyType {
  /** 默认代理：调 ZLM addStreamProxy，给一个源 URL 由 ZLM 主动拉 */
  DEFAULT = 'default',
  /** FFmpeg 代理：用 ZLM addFFmpegSource，借助 ffmpeg 转封装/转码后落地 */
  FFMPEG = 'ffmpeg',
}

/** 拉流代理 RTP 传输模式 */
export enum VideoStreamProxyRtpType {
  /** TCP 主动 */
  TCP_ACTIVE = '0',
  /** TCP 被动 */
  TCP_PASSIVE = '1',
  /** UDP */
  UDP = '2',
  /** 组播 */
  MULTICAST = '3',
}
