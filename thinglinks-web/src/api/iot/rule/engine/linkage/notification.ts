import { defHttp } from '/@/utils/http/axios';
import { RequestEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';

const ServicePrefix = ServicePrefixEnum.RULE;
const MODULAR = 'ruleNotification';

export interface RuleAlarmRecipient {
  type: 'PHONE' | 'EMPLOYEE' | 'ALL';
  value: string;
  label?: string;
}

export interface RuleAlarmChannelTemplate {
  channelType: number;
  templateId?: string;
  enabled?: boolean;
  format?: 'MARKDOWN' | 'NOTICE' | 'TEXT';
  titleTemplate?: string;
  contentTemplate?: string;
  urlTemplate?: string;
  atAll?: boolean;
  recipients?: RuleAlarmRecipient[];
}

export interface RuleNotificationVariable {
  key: string;
  placeholder: string;
  label: string;
  description?: string;
  sample?: string;
  channelTypes?: number[];
}

export interface RuleNotificationVariableGroup {
  groupCode: string;
  groupName: string;
  variables: RuleNotificationVariable[];
}

export interface RuleNotificationPreviewParam {
  alarmIdentification?: string;
  recipients?: RuleAlarmRecipient[];
  channelTemplates?: RuleAlarmChannelTemplate[];
  sampleVariables?: Recordable;
}

export interface RuleAlarmRenderedNotification {
  channelType: number;
  channelName: string;
  format: string;
  title: string;
  content: string;
  url?: string;
  atAll?: boolean;
  recipients?: RuleAlarmRecipient[];
}

export interface RuleNotificationPreviewResult {
  channels: RuleAlarmRenderedNotification[];
}

export const getNotificationVariables = () =>
  defHttp.request<RuleNotificationVariableGroup[]>(
    {
      url: `${ServicePrefix}/${MODULAR}/variables`,
      method: RequestEnum.GET,
    },
    { errorMessageMode: 'none' },
  );

export const previewNotification = (params: RuleNotificationPreviewParam) =>
  defHttp.request<RuleNotificationPreviewResult>(
    {
      url: `${ServicePrefix}/${MODULAR}/preview`,
      method: RequestEnum.POST,
      params,
    },
    { errorMessageMode: 'none' },
  );
