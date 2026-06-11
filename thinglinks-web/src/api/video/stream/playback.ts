import type { PlaybackStartParams, PlaybackResultVO, RecordQueryParams } from '/@/api/video/record/model/playbackModel';
import { defHttp } from '/@/utils/http/axios';
import { RequestEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';

const MODULAR = 'playback';
const ServicePrefix = ServicePrefixEnum.VIDEO;

export const playbackStart = (params: PlaybackStartParams) =>
  defHttp.request<PlaybackResultVO>({
    url: `${ServicePrefix}/${MODULAR}/start`,
    method: RequestEnum.POST,
    params,
  });

const buildQuery = (params: Record<string, any>) =>
  Object.entries(params)
    .filter(([, v]) => v !== undefined && v !== null)
    .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(String(v))}`)
    .join('&');

export const playbackStop = (deviceIdentification: string, channelIdentification: string) =>
  defHttp.request<boolean>({
    url: `${ServicePrefix}/${MODULAR}/stop?${buildQuery({ deviceIdentification, channelIdentification })}`,
    method: RequestEnum.DELETE,
  });

export const playbackPause = (deviceIdentification: string, channelIdentification: string) =>
  defHttp.request<boolean>({
    url: `${ServicePrefix}/${MODULAR}/pause?${buildQuery({ deviceIdentification, channelIdentification })}`,
    method: RequestEnum.POST,
  });

export const playbackResume = (deviceIdentification: string, channelIdentification: string) =>
  defHttp.request<boolean>({
    url: `${ServicePrefix}/${MODULAR}/resume?${buildQuery({ deviceIdentification, channelIdentification })}`,
    method: RequestEnum.POST,
  });

export const playbackSpeed = (
  deviceIdentification: string,
  channelIdentification: string,
  speed: number,
) =>
  defHttp.request<boolean>({
    url: `${ServicePrefix}/${MODULAR}/speed?${buildQuery({ deviceIdentification, channelIdentification, speed })}`,
    method: RequestEnum.POST,
  });

export const playbackSeek = (
  deviceIdentification: string,
  channelIdentification: string,
  seekTime: number,
) =>
  defHttp.request<boolean>({
    url: `${ServicePrefix}/${MODULAR}/seek?${buildQuery({ deviceIdentification, channelIdentification, seekTime })}`,
    method: RequestEnum.POST,
  });

export const recordQuery = (params: RecordQueryParams) =>
  defHttp.request<boolean>({
    url: `${ServicePrefix}/${MODULAR}/recordQuery`,
    method: RequestEnum.POST,
    params,
  });
