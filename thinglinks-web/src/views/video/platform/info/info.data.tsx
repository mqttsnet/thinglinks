import { Ref } from 'vue';
import { renderYesNoComponent } from '/@/utils/thinglinks/common';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { FormSchemaExt } from '/@/api/thinglinks/common/formValidateService';
import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
import { dictComponentProps } from '/@/utils/thinglinks/common';
import citiesGd from '/@/utils/thinglinks/citiesGd.json';

const { t } = useI18n();

export const columns = (): BasicColumn[] => {
  return [
    {
      title: t('video.platform.info.name'),
      dataIndex: 'name',
      width: 150,
      fixed: 'left',
    },
    {
      title: t('video.platform.info.enable'),
      dataIndex: 'enable',
      width: 100,
      customRender: ({ text }) => renderYesNoComponent(text, true),
    },
    {
      title: t('video.platform.info.serverGbId'),
      dataIndex: 'serverGbId',
      width: 180,
    },
    {
      title: t('video.platform.info.serverIp'),
      dataIndex: 'serverIp',
      width: 150,
    },
    {
      title: t('video.platform.info.serverPort'),
      dataIndex: 'serverPort',
      width: 100,
    },
    {
      title: t('video.platform.info.deviceGbId'),
      dataIndex: 'deviceGbId',
      width: 180,
    },
    {
      title: t('video.platform.info.transport'),
      dataIndex: ['echoMap', 'transport'],
      width: 120,
    },
    {
      title: t('video.platform.info.cascadeType'),
      dataIndex: ['echoMap', 'cascadeType'],
      width: 120,
    },
    {
      title: t('video.platform.info.gbVersion'),
      dataIndex: ['echoMap', 'gbVersion'],
      width: 120,
    },
    {
      title: t('video.platform.info.onlineStatus'),
      dataIndex: 'onlineStatus',
      width: 100,
      customRender: ({ text }) => renderYesNoComponent(text, true),
    },
    {
      title: t('video.platform.info.lastRegisterTime'),
      dataIndex: 'lastRegisterTime',
      width: 180,
    },
    {
      title: t('video.platform.info.lastKeepaliveTime'),
      dataIndex: 'lastKeepaliveTime',
      width: 180,
    },
    {
      title: t('thinglinks.common.createdTime'),
      dataIndex: 'createdTime',
      sorter: true,
      width: 180,
    },
  ];
};

export const searchFormSchema = (): FormSchema[] => {
  return [
    {
      label: t('video.platform.info.name'),
      field: 'name',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('video.platform.info.serverGbId'),
      field: 'serverGbId',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('video.platform.info.deviceGbId'),
      field: 'deviceGbId',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('video.platform.info.transport'),
      field: 'transport',
      component: 'ApiSelect',
      colProps: { span: 6 },
      componentProps: {
        ...dictComponentProps(DictEnum.VIDEO_DEVICE_TRANSPORT),
      },
    },
    {
      label: t('video.platform.info.cascadeType'),
      field: 'cascadeType',
      component: 'ApiSelect',
      colProps: { span: 6 },
      componentProps: {
        ...dictComponentProps(DictEnum.VIDEO_PLATFORM_CASCADE_TYPE),
      },
    },
    {
      label: t('video.platform.info.gbVersion'),
      field: 'gbVersion',
      component: 'ApiSelect',
      colProps: { span: 6 },
      componentProps: {
        ...dictComponentProps(DictEnum.VIDEO_PLATFORM_GB_VERSION),
      },
    },
    {
      label: t('video.platform.info.enable'),
      field: 'enable',
      component: 'RadioButtonGroup',
      colProps: { span: 6 },
      componentProps: {
        options: [
          { label: t('thinglinks.common.yes'), value: true },
          { label: t('thinglinks.common.no'), value: false },
        ],
      },
    },
    {
      label: t('video.platform.info.onlineStatus'),
      field: 'onlineStatus',
      component: 'RadioButtonGroup',
      colProps: { span: 6 },
      componentProps: {
        options: [
          { label: t('thinglinks.common.yes'), value: true },
          { label: t('thinglinks.common.no'), value: false },
        ],
      },
    },
  ];
};

export const editFormSchema = (_type: Ref<ActionEnum>): FormSchema[] => {
  return [
    {
      field: 'id',
      label: 'ID',
      component: 'Input',
      show: false,
    },
    // ===== 基本信息 =====
    {
      field: 'divider-basic',
      component: 'Divider',
      label: t('video.platform.info.basicInfo'),
      colProps: { span: 24 },
    },
    {
      label: t('video.platform.info.name'),
      field: 'name',
      component: 'Input',
      required: true,
      helpMessage: t('video.platform.info.helpMessage.name'),
      componentProps: {
        maxlength: 64,
      },
    },
    {
      label: t('video.platform.info.enable'),
      field: 'enable',
      component: 'RadioButtonGroup',
      defaultValue: true,
      helpMessage: t('video.platform.info.helpMessage.enable'),
      componentProps: {
        options: [
          { label: t('video.platform.info.enablePlatform'), value: true },
          { label: t('video.platform.info.disablePlatform'), value: false },
        ],
      },
    },
    {
      label: t('video.platform.info.cascadeType'),
      field: 'cascadeType',
      component: 'ApiSelect',
      helpMessage: t('video.platform.info.helpMessage.cascadeType'),
      componentProps: {
        ...dictComponentProps(DictEnum.VIDEO_PLATFORM_CASCADE_TYPE),
      },
    },
    {
      label: t('video.platform.info.gbVersion'),
      field: 'gbVersion',
      component: 'ApiSelect',
      helpMessage: t('video.platform.info.helpMessage.gbVersion'),
      componentProps: {
        ...dictComponentProps(DictEnum.VIDEO_PLATFORM_GB_VERSION),
      },
    },
    {
      label: t('video.platform.info.civilCode'),
      field: 'civilCodeArea',
      component: 'Cascader',
      helpMessage: t('video.platform.info.helpMessage.civilCode'),
      componentProps: ({ formModel }) => {
        return {
          options: citiesGd,
          changeOnSelect: true,
          showSearch: true,
          placeholder: t('video.platform.info.helpMessage.civilCode'),
          onChange: (value: string[]) => {
            if (value && value.length > 0) {
              formModel.civilCode = value[value.length - 1];
              const province = citiesGd.find((item) => item.value === value[0]);
              const city = province?.children?.find((item) => item.value === value[1]);
              const district = city?.children?.find((item) => item.value === value[2]);
              formModel.address =
                (province?.label || '') + (city?.label || '') + (district?.label || '');
            } else {
              formModel.civilCode = '';
            }
          },
        };
      },
    },
    {
      field: 'civilCode',
      label: '',
      component: 'Input',
      show: false,
    },
    {
      label: t('video.platform.info.manufacturer'),
      field: 'manufacturer',
      component: 'Input',
      helpMessage: t('video.platform.info.helpMessage.manufacturer'),
      componentProps: {
        maxlength: 64,
      },
    },
    {
      label: t('video.platform.info.model'),
      field: 'model',
      component: 'Input',
      helpMessage: t('video.platform.info.helpMessage.model'),
      componentProps: {
        maxlength: 64,
      },
    },
    {
      label: t('video.platform.info.address'),
      field: 'address',
      component: 'Input',
      helpMessage: t('video.platform.info.helpMessage.address'),
      componentProps: {
        maxlength: 256,
      },
    },
    // ===== SIP 配置 =====
    {
      field: 'divider-sip',
      component: 'Divider',
      label: t('video.platform.info.sipConfig'),
      colProps: { span: 24 },
    },
    {
      label: t('video.platform.info.serverGbId'),
      field: 'serverGbId',
      component: 'Input',
      required: true,
      helpMessage: t('video.platform.info.helpMessage.serverGbId'),
      rules: [
        { required: true, message: t('video.platform.info.serverGbId') },
        { pattern: /^\d{20}$/, message: t('video.platform.info.helpMessage.serverGbId') },
      ],
      componentProps: {
        maxlength: 20,
      },
    },
    {
      label: t('video.platform.info.serverGbDomain'),
      field: 'serverGbDomain',
      component: 'Input',
      helpMessage: t('video.platform.info.helpMessage.serverGbDomain'),
      componentProps: {
        maxlength: 20,
      },
    },
    {
      label: t('video.platform.info.serverIp'),
      field: 'serverIp',
      component: 'Input',
      required: true,
      helpMessage: t('video.platform.info.helpMessage.serverIp'),
      rules: [
        { required: true, message: t('video.platform.info.serverIp') },
        {
          pattern:
            /^(?:(?:25[0-5]|2[0-4]\d|[01]?\d\d?)\.){3}(?:25[0-5]|2[0-4]\d|[01]?\d\d?)$|^[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+$/,
          message: t('video.platform.info.helpMessage.serverIp'),
        },
      ],
    },
    {
      label: t('video.platform.info.serverPort'),
      field: 'serverPort',
      component: 'InputNumber',
      required: true,
      helpMessage: t('video.platform.info.helpMessage.serverPort'),
      componentProps: {
        min: 1,
        max: 65535,
      },
    },
    {
      label: t('video.platform.info.deviceGbId'),
      field: 'deviceGbId',
      component: 'Input',
      required: true,
      helpMessage: t('video.platform.info.helpMessage.deviceGbId'),
      rules: [
        { required: true, message: t('video.platform.info.deviceGbId') },
        { pattern: /^\d{20}$/, message: t('video.platform.info.helpMessage.deviceGbId') },
      ],
      componentProps: {
        maxlength: 20,
      },
    },
    {
      label: t('video.platform.info.deviceIp'),
      field: 'deviceIp',
      component: 'Input',
      helpMessage: t('video.platform.info.helpMessage.deviceIp'),
    },
    {
      label: t('video.platform.info.devicePort'),
      field: 'devicePort',
      component: 'InputNumber',
      helpMessage: t('video.platform.info.helpMessage.devicePort'),
      componentProps: {
        min: 1,
        max: 65535,
      },
    },
    {
      label: t('video.platform.info.transport'),
      field: 'transport',
      component: 'ApiSelect',
      defaultValue: 'UDP',
      helpMessage: t('video.platform.info.helpMessage.transport'),
      componentProps: {
        ...dictComponentProps(DictEnum.VIDEO_DEVICE_TRANSPORT),
      },
    },
    {
      label: t('video.platform.info.username'),
      field: 'username',
      component: 'Input',
      helpMessage: t('video.platform.info.helpMessage.username'),
      componentProps: {
        maxlength: 64,
      },
    },
    {
      label: t('video.platform.info.password'),
      field: 'password',
      component: 'InputPassword',
      helpMessage: t('video.platform.info.helpMessage.password'),
    },
    {
      label: t('video.platform.info.expires'),
      field: 'expires',
      component: 'InputNumber',
      defaultValue: 3600,
      helpMessage: t('video.platform.info.helpMessage.expires'),
      componentProps: {
        min: 60,
        max: 86400,
      },
    },
    {
      label: t('video.platform.info.keepTimeout'),
      field: 'keepTimeout',
      component: 'InputNumber',
      defaultValue: 60,
      helpMessage: t('video.platform.info.helpMessage.keepTimeout'),
      componentProps: {
        min: 5,
        max: 600,
      },
    },
    {
      label: t('video.platform.info.registerWay'),
      field: 'registerWay',
      component: 'ApiSelect',
      helpMessage: t('video.platform.info.helpMessage.registerWay'),
      componentProps: {
        ...dictComponentProps(DictEnum.VIDEO_GB28181_SIP_REGISTER_WAY),
      },
    },
    {
      label: t('video.platform.info.secrecy'),
      field: 'secrecy',
      component: 'ApiSelect',
      helpMessage: t('video.platform.info.helpMessage.secrecy'),
      componentProps: {
        ...dictComponentProps(DictEnum.VIDEO_DEVICE_SECRECY),
      },
    },
    {
      label: t('video.platform.info.characterSet'),
      field: 'characterSet',
      component: 'Select',
      defaultValue: 'GB2312',
      helpMessage: t('video.platform.info.helpMessage.characterSet'),
      componentProps: {
        options: [
          { label: 'GB2312', value: 'GB2312' },
          { label: 'UTF-8', value: 'UTF-8' },
        ],
      },
    },
    // ===== 订阅配置 =====
    {
      field: 'divider-subscribe',
      component: 'Divider',
      label: t('video.platform.info.subscribeConfig'),
      colProps: { span: 24 },
    },
    {
      label: t('video.platform.info.catalogSubscribe'),
      field: 'catalogSubscribe',
      component: 'RadioButtonGroup',
      defaultValue: false,
      helpMessage: t('video.platform.info.helpMessage.catalogSubscribe'),
      componentProps: {
        options: [
          { label: t('thinglinks.common.yes'), value: true },
          { label: t('thinglinks.common.no'), value: false },
        ],
      },
    },
    {
      label: t('video.platform.info.alarmSubscribe'),
      field: 'alarmSubscribe',
      component: 'RadioButtonGroup',
      defaultValue: false,
      helpMessage: t('video.platform.info.helpMessage.alarmSubscribe'),
      componentProps: {
        options: [
          { label: t('thinglinks.common.yes'), value: true },
          { label: t('thinglinks.common.no'), value: false },
        ],
      },
    },
    {
      label: t('video.platform.info.mobilePositionSubscribe'),
      field: 'mobilePositionSubscribe',
      component: 'RadioButtonGroup',
      defaultValue: false,
      helpMessage: t('video.platform.info.helpMessage.mobilePositionSubscribe'),
      componentProps: {
        options: [
          { label: t('thinglinks.common.yes'), value: true },
          { label: t('thinglinks.common.no'), value: false },
        ],
      },
    },
    // ===== 高级配置 =====
    {
      field: 'divider-advanced',
      component: 'Divider',
      label: t('video.platform.info.advancedConfig'),
      colProps: { span: 24 },
    },
    {
      label: t('video.platform.info.ptz'),
      field: 'ptz',
      component: 'RadioButtonGroup',
      defaultValue: false,
      helpMessage: t('video.platform.info.helpMessage.ptz'),
      componentProps: {
        options: [
          { label: t('thinglinks.common.yes'), value: true },
          { label: t('thinglinks.common.no'), value: false },
        ],
      },
    },
    {
      label: t('video.platform.info.rtcp'),
      field: 'rtcp',
      component: 'RadioButtonGroup',
      defaultValue: false,
      helpMessage: t('video.platform.info.helpMessage.rtcp'),
      componentProps: {
        options: [
          { label: t('thinglinks.common.yes'), value: true },
          { label: t('thinglinks.common.no'), value: false },
        ],
      },
    },
    {
      label: t('video.platform.info.asMessageChannel'),
      field: 'asMessageChannel',
      component: 'RadioButtonGroup',
      defaultValue: false,
      helpMessage: t('video.platform.info.helpMessage.asMessageChannel'),
      componentProps: {
        options: [
          { label: t('thinglinks.common.yes'), value: true },
          { label: t('thinglinks.common.no'), value: false },
        ],
      },
    },
    {
      label: t('video.platform.info.autoPushChannel'),
      field: 'autoPushChannel',
      component: 'RadioButtonGroup',
      defaultValue: false,
      helpMessage: t('video.platform.info.helpMessage.autoPushChannel'),
      componentProps: {
        options: [
          { label: t('thinglinks.common.yes'), value: true },
          { label: t('thinglinks.common.no'), value: false },
        ],
      },
    },
    {
      label: t('video.platform.info.startOfflinePush'),
      field: 'startOfflinePush',
      component: 'RadioButtonGroup',
      defaultValue: false,
      helpMessage: t('video.platform.info.helpMessage.startOfflinePush'),
      componentProps: {
        options: [
          { label: t('thinglinks.common.yes'), value: true },
          { label: t('thinglinks.common.no'), value: false },
        ],
      },
    },
    {
      label: t('video.platform.info.sendStreamIp'),
      field: 'sendStreamIp',
      component: 'Input',
      helpMessage: t('video.platform.info.helpMessage.sendStreamIp'),
    },
    {
      label: t('video.platform.info.sipIp'),
      field: 'sipIp',
      component: 'Input',
      helpMessage: t('video.platform.info.helpMessage.sipIp'),
    },
    {
      label: t('video.platform.info.sipPort'),
      field: 'sipPort',
      component: 'InputNumber',
      helpMessage: t('video.platform.info.helpMessage.sipPort'),
      componentProps: {
        min: 1,
        max: 65535,
      },
    },
    {
      label: t('video.platform.info.hookUrlPrefix'),
      field: 'hookUrlPrefix',
      component: 'Input',
      helpMessage: t('video.platform.info.helpMessage.hookUrlPrefix'),
    },
    {
      label: t('video.platform.info.cascadeSdpIp'),
      field: 'cascadeSdpIp',
      component: 'Input',
      helpMessage: t('video.platform.info.helpMessage.cascadeSdpIp'),
    },
    {
      label: t('video.platform.info.registerExpires'),
      field: 'registerExpires',
      component: 'InputNumber',
      helpMessage: t('video.platform.info.helpMessage.registerExpires'),
      componentProps: {
        min: 60,
        max: 86400,
      },
    },
    {
      label: t('video.platform.info.keepaliveInterval'),
      field: 'keepaliveInterval',
      component: 'InputNumber',
      helpMessage: t('video.platform.info.helpMessage.keepaliveInterval'),
      componentProps: {
        min: 5,
        max: 600,
      },
    },
    {
      label: t('video.platform.info.keepaliveTimeoutCount'),
      field: 'keepaliveTimeoutCount',
      component: 'InputNumber',
      helpMessage: t('video.platform.info.helpMessage.keepaliveTimeoutCount'),
      componentProps: {
        min: 1,
        max: 10,
      },
    },
    {
      label: t('video.platform.info.serverId'),
      field: 'serverId',
      component: 'Input',
      helpMessage: t('video.platform.info.helpMessage.serverId'),
    },
    {
      label: t('video.platform.info.serviceInstanceId'),
      field: 'serviceInstanceId',
      component: 'Input',
      helpMessage: t('video.platform.info.helpMessage.serviceInstanceId'),
    },
  ];
};

export const customFormSchemaRules = (_): Partial<FormSchemaExt>[] => {
  return [];
};
