/**
 * 播放器类型枚举定义
 */

/**
 * 播放器类型枚举值（与 components/video/player/types.ts 中 PlayerType 对应）
 */
export enum VideoPlayerType {
  /** Jessibuca 播放器 */
  JESSIBUCA = 'jessibuca',
  /** FLV.js 播放器 */
  FLV = 'flv',
  /** HLS.js 播放器 */
  HLS = 'hls',
  /** 原生 HTML5 播放器 */
  NATIVE = 'native',
  /** WebRTC 播放器 */
  WEBRTC = 'webrtc',
}
