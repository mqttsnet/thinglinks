import {
  VideoDeviceGroupRelationSaveVO,
  VideoDeviceGroupRelationResultVO,
} from './model/groupModel';
import { defHttp } from '/@/utils/http/axios';
import { RequestEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';

const MODULAR = 'deviceGroupRelation';
const ServicePrefix = ServicePrefixEnum.VIDEO;

export const listByGroup = (groupId: string) =>
  defHttp.request<VideoDeviceGroupRelationResultVO[]>({
    url: `${ServicePrefix}/${MODULAR}/listByGroup`,
    method: RequestEnum.GET,
    params: { groupId },
  });

export const listByDevice = (deviceIdentification: string) =>
  defHttp.request<VideoDeviceGroupRelationResultVO[]>({
    url: `${ServicePrefix}/${MODULAR}/listByDevice`,
    method: RequestEnum.GET,
    params: { deviceIdentification },
  });

export const bind = (params: VideoDeviceGroupRelationSaveVO) =>
  defHttp.request<boolean>({
    url: `${ServicePrefix}/${MODULAR}/bind`,
    method: RequestEnum.POST,
    params,
  });

export const unbind = (id: string) =>
  defHttp.request<boolean>({
    url: `${ServicePrefix}/${MODULAR}/${id}`,
    method: RequestEnum.DELETE,
  });
