import type { StreamURL } from '/@/api/video/stream/model/playModel';

export interface PlaybackStartParams {
  deviceIdentification: string;
  channelIdentification: string;
  startTime: string;
  endTime: string;
}

export interface PlaybackResultVO {
  deviceIdentification: string;
  channelIdentification: string;
  app: string;
  stream: string;
  mediaServerIp: string;
  startTime: string;
  endTime: string;
  flv: StreamURL;
  httpsFlv: StreamURL;
  wsFlv: StreamURL;
  hls: StreamURL;
  httpsHls: StreamURL;
  rtmp: StreamURL;
  rtsp: StreamURL;
  fmp4: StreamURL;
  ts: StreamURL;
  rtc: StreamURL;
  rtcs: StreamURL;
  callId: string;
}

export interface RecordQueryParams {
  deviceIdentification: string;
  channelIdentification: string;
  startTime: string;
  endTime: string;
  type?: string;
}

export interface RecordSegment {
  startTime: string;
  endTime: string;
  name?: string;
  fileSize?: number;
  duration?: number;
}
