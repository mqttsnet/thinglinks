import { defHttp } from '/@/utils/http/axios';

export interface OnvifDevice {
  xaddr: string;
  endpointReference?: string;
  types?: string;
  scopes?: string[];
}

export interface OnvifProfile {
  token: string;
  name?: string;
  videoEncoding?: string;
  width?: number;
  height?: number;
  frameRate?: number;
  bitrate?: number;
  streamUri?: string;
}

export const Api = {
  Discover: { url: '/video/onvif/discover', method: 'POST' as const },
  Profiles: { url: '/video/onvif/profiles', method: 'POST' as const },
  Import: { url: '/video/onvif/import', method: 'POST' as const },
};

/** 局域网扫描 ONVIF 设备 */
export const discoverOnvif = (timeoutSeconds = 4) =>
  defHttp.post<OnvifDevice[]>({
    url: Api.Discover.url,
    params: { timeoutSeconds },
  });

/** 拉取设备 Profile 列表 */
export const fetchOnvifProfiles = (xaddr: string, username?: string, password?: string) =>
  defHttp.post<OnvifProfile[]>({
    url: Api.Profiles.url,
    params: { xaddr, username, password },
  });

/** 把 ONVIF 设备 + 选定 Profile 导入为 RTSP 设备 */
export const importOnvifDevice = (params: {
  xaddr: string;
  username?: string;
  password?: string;
  profileToken: string;
  mediaIdentification: string;
  customName?: string;
}) =>
  defHttp.post<string>({
    url: Api.Import.url,
    params,
  });
