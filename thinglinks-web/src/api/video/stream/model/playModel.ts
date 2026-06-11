export interface PlayStartParams {
  deviceIdentification: string;
  channelIdentification: string;
}

export interface StreamURL {
  protocol: string;
  host: string;
  port: number;
  file: string;
  url: string;
}

export interface PlayResultVO {
  deviceIdentification: string;
  channelIdentification: string;
  app: string;
  stream: string;
  mediaServerIp: string;
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
