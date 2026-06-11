import type { VideoRecordFileResultVO, RecordDateItem } from './model/fileModel';
import { defHttp } from '/@/utils/http/axios';
import { RequestEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';
import type { AxiosRequestConfig } from 'axios';

const MODULAR = 'recordFile';
const ServicePrefix = ServicePrefixEnum.VIDEO;

export const Api = {
  ListByDevice: {
    url: `${ServicePrefix}/${MODULAR}/listByDevice`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
  ListByPlan: {
    url: `${ServicePrefix}/${MODULAR}/listByPlan`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
  ListByDate: {
    url: `${ServicePrefix}/${MODULAR}/listByDate`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
  GetRecordDates: {
    url: `${ServicePrefix}/${MODULAR}/getRecordDates`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
  PlayUrl: {
    url: `${ServicePrefix}/${MODULAR}/playUrl`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
  DownloadUrl: {
    url: `${ServicePrefix}/${MODULAR}/downloadUrl`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
  Detail: {
    url: `${ServicePrefix}/${MODULAR}`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
  Delete: {
    url: `${ServicePrefix}/${MODULAR}`,
    method: RequestEnum.DELETE,
  } as AxiosRequestConfig,
};

export const listByDevice = (deviceIdentification: string, channelIdentification: string) =>
  defHttp.request<VideoRecordFileResultVO[]>({
    ...Api.ListByDevice,
    params: { deviceIdentification, channelIdentification },
  });

export const listByPlan = (planId: string) =>
  defHttp.request<VideoRecordFileResultVO[]>({ ...Api.ListByPlan, params: { planId } });

export const listByDate = (
  deviceIdentification: string,
  channelIdentification: string,
  date: string,
) =>
  defHttp.request<VideoRecordFileResultVO[]>({
    ...Api.ListByDate,
    params: { deviceIdentification, channelIdentification, date },
  });

export const getRecordDates = (
  deviceIdentification: string,
  channelIdentification: string,
  month: string,
) =>
  defHttp.request<RecordDateItem[]>({
    ...Api.GetRecordDates,
    params: { deviceIdentification, channelIdentification, month },
  });

export const getPlayUrl = (id: string) =>
  defHttp.request<string>({ url: `${ServicePrefix}/${MODULAR}/playUrl/${id}`, method: RequestEnum.GET });

export const getDownloadUrl = (id: string) =>
  defHttp.request<string>({ url: `${ServicePrefix}/${MODULAR}/downloadUrl/${id}`, method: RequestEnum.GET });

export const fileDetail = (id: string) =>
  defHttp.request<VideoRecordFileResultVO>({ url: `${ServicePrefix}/${MODULAR}/${id}`, method: RequestEnum.GET });

export const deleteFile = (id: string) =>
  defHttp.request<boolean>({ url: `${ServicePrefix}/${MODULAR}/${id}`, method: RequestEnum.DELETE });
