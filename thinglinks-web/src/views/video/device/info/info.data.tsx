import { h, Ref } from 'vue';
import { Tag } from 'ant-design-vue';
import { dictComponentProps } from '/@/utils/thinglinks/common';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
import { FormSchemaExt } from '/@/api/thinglinks/common/formValidateService';
const { t } = useI18n();

// RTSP / ONVIF 等"平台主动拉流"协议
const ACTIVE_STREAM_PROTOCOLS = ['RTSP', 'ONVIF'];
// GB28181 / SIP 等"设备主动注册"协议——共享 SIP 字段集
const SIP_PROTOCOLS = ['GB28181', 'SIP'];

const isActiveStreamProtocol = (value?: string) =>
  !!value && ACTIVE_STREAM_PROTOCOLS.includes(value.toUpperCase());
const isSipProtocol = (value?: string) =>
  // 默认值（未选）走 SIP 字段，兼容老数据
  !value || SIP_PROTOCOLS.includes(value.toUpperCase());

// 列表页字段
export const columns = (): BasicColumn[] => {
  return [
    {
      title: t('video.device.info.deviceIdentification'),
      dataIndex: 'deviceIdentification',
    },
    {
      title: t('video.device.info.accessProtocol'),
      dataIndex: ['echoMap', 'accessProtocol'],
    },
    {
      title: t('video.device.info.deviceName'),
      dataIndex: 'deviceName',
    },
    {
      title: t('video.device.info.customName'),
      dataIndex: 'customName',
    },
    {
      title: t('video.device.info.manufacturer'),
      dataIndex: 'manufacturer',
    },
    {
      title: t('video.device.info.host'),
      dataIndex: 'host',
    },
    {
      title: t('video.device.info.port'),
      dataIndex: 'port',
    },
    {
      title: t('video.device.info.transport'),
      dataIndex: 'transport',
      slots: { customRender: 'transport' },
    },
    {
      title: t('video.device.info.streamMode'),
      dataIndex: 'streamMode',
      slots: { customRender: 'streamMode' },
    },
    {
      title: t('video.device.info.onlineStatus'),
      dataIndex: 'onlineStatus',
      width: 80,
      customRender: ({ text }) =>
        text === true || text === 1
          ? h(Tag, { color: 'success' }, () => t('video.device.info.online'))
          : h(Tag, { color: 'default' }, () => t('video.device.info.offline')),
    },
    {
      title: t('video.device.info.registerTime'),
      dataIndex: 'registerTime',
    },
    {
      title: t('video.device.info.lastKeepaliveTime'),
      dataIndex: 'lastKeepaliveTime',
    },
    {
      title: t('video.device.info.mediaIdentification'),
      dataIndex: 'mediaIdentification',
    },
    {
      title: t('video.device.info.channelCount'),
      dataIndex: 'channelCount',
    },
    {
      title: t('video.device.info.remark'),
      dataIndex: 'remark',
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
      label: t('video.device.info.deviceIdentification'),
      field: 'deviceIdentification',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('video.device.info.deviceName'),
      field: 'deviceName',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('video.device.info.accessProtocol'),
      field: 'accessProtocol',
      component: 'ApiSelect',
      componentProps: {
        ...dictComponentProps(DictEnum.VIDEO_DEVICE_ACCESS_PROTOCOL),
      },
      colProps: { span: 6 },
    },
    {
      label: t('video.device.info.manufacturer'),
      field: 'manufacturer',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('video.device.info.transport'),
      field: 'transport',
      component: 'ApiSelect',
      componentProps: {
        ...dictComponentProps(DictEnum.VIDEO_DEVICE_TRANSPORT),
      },
      colProps: { span: 6 },
    },
    {
      label: t('video.device.info.streamMode'),
      field: 'streamMode',
      component: 'ApiSelect',
      componentProps: {
        ...dictComponentProps(DictEnum.VIDEO_DEVICE_STREAM_MODE),
      },
      colProps: { span: 6 },
    },
    {
      field: 'createTimeRange',
      label: t('thinglinks.common.createdTime'),
      component: 'RangePicker',
      colProps: { span: 6 },
    },
  ];
};

// 编辑页字段
export const editFormSchema = (_type: Ref<ActionEnum>): FormSchema[] => {
  return [
    {
      field: 'id',
      label: 'ID',
      component: 'Input',
      show: false,
    },
    {
      label: t('video.device.info.deviceIdentification'),
      field: 'deviceIdentification',
      component: 'Input',
      helpMessage: t('video.device.info.deviceIdentificationHelp'),
      required: true,
      componentProps: {
        placeholder: t('video.device.info.deviceIdentificationPlaceholder'),
      },
    },
    {
      label: t('video.device.info.accessProtocol'),
      field: 'accessProtocol',
      component: 'ApiSelect',
      helpMessage: t('video.device.info.accessProtocolHelp'),
      required: true,
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.VIDEO_DEVICE_ACCESS_PROTOCOL),
      },
    },
    {
      label: t('video.device.info.deviceName'),
      field: 'deviceName',
      component: 'Input',
      required: true,
    },
    {
      label: t('video.device.info.customName'),
      field: 'customName',
      component: 'Input',
      helpMessage: t('video.device.info.customNameHelp'),
    },
    {
      label: t('video.device.info.mediaIdentification'),
      field: 'mediaIdentification',
      component: 'ApiVideoSelectNodeCard',
      helpMessage: t('video.device.info.mediaIdentificationHelp'),
      required: true,
      colProps: { span: 22 },
    },
    {
      label: t('video.device.info.manufacturer'),
      field: 'manufacturer',
      component: 'Input',
    },
    {
      label: t('video.device.info.model'),
      field: 'model',
      component: 'Input',
    },
    {
      label: t('video.device.info.firmware'),
      field: 'firmware',
      component: 'Input',
    },
    {
      label: t('video.device.info.host'),
      field: 'host',
      component: 'Input',
      helpMessage: t('video.device.info.hostHelp'),
      componentProps: {
        placeholder: 'e.g. 192.168.1.100',
      },
    },
    {
      label: t('video.device.info.port'),
      field: 'port',
      component: 'InputNumber',
      helpMessage: t('video.device.info.portHelp'),
      // placeholder 按 accessProtocol 动态：RTSP 默认 554，GB28181/SIP 默认 5060，ONVIF 多为 80/8080
      componentProps: ({ formModel }) => {
        const protocol = String(formModel?.accessProtocol || '').toUpperCase();
        const placeholder =
          protocol === 'RTSP'
            ? 'e.g. 554'
            : protocol === 'ONVIF'
            ? 'e.g. 80 / 8080'
            : 'e.g. 5060';
        return { min: 1, max: 65535, placeholder };
      },
    },
    // ========== RTSP / ONVIF 主动拉流字段（仅 RTSP/ONVIF 协议显示）==========
    {
      label: t('video.device.info.streamSourceUrl'),
      field: 'streamSourceUrl',
      component: 'Input',
      helpMessage: t('video.device.info.streamSourceUrlHelp', { at: '@' }),
      colProps: { span: 22 },
      componentProps: {
        placeholder: 'rtsp://admin:pass@192.168.1.108:554/h264/ch1/main/av_stream',
      },
      ifShow: ({ values }) => isActiveStreamProtocol(values?.accessProtocol),
    },
    {
      label: t('video.device.info.streamSourceUsername'),
      field: 'streamSourceUsername',
      component: 'Input',
      helpMessage: t('video.device.info.streamSourceUsernameHelp'),
      ifShow: ({ values }) => isActiveStreamProtocol(values?.accessProtocol),
    },
    {
      label: t('video.device.info.streamSourcePath'),
      field: 'streamSourcePath',
      component: 'Input',
      helpMessage: t('video.device.info.streamSourcePathHelp', { at: '@' }),
      componentProps: {
        placeholder: '/h264/ch1/main/av_stream',
      },
      ifShow: ({ values }) => isActiveStreamProtocol(values?.accessProtocol),
    },
    {
      label: t('video.device.info.streamSourceRtpType'),
      field: 'streamSourceRtpType',
      component: 'Select',
      helpMessage: t('video.device.info.streamSourceRtpTypeHelp'),
      defaultValue: '0',
      componentProps: {
        options: [
          { label: 'TCP', value: '0' },
          { label: 'UDP', value: '1' },
          { label: 'Multicast', value: '2' },
        ],
      },
      ifShow: ({ values }) => isActiveStreamProtocol(values?.accessProtocol),
    },
    // ========== GB28181 / SIP 专属字段 ==========
    {
      label: t('video.device.info.wanHost'),
      field: 'wanHost',
      component: 'Input',
      helpMessage: t('video.device.info.wanHostHelp'),
      ifShow: ({ values }) => isSipProtocol(values?.accessProtocol),
    },
    {
      label: t('video.device.info.lanHost'),
      field: 'lanHost',
      component: 'Input',
      ifShow: ({ values }) => isSipProtocol(values?.accessProtocol),
    },
    {
      label: t('video.device.info.sdpHost'),
      field: 'sdpHost',
      component: 'Input',
      helpMessage: t('video.device.info.sdpHostHelp'),
      ifShow: ({ values }) => isSipProtocol(values?.accessProtocol),
    },
    {
      label: t('video.device.info.localHost'),
      field: 'localHost',
      component: 'Input',
      ifShow: ({ values }) => isSipProtocol(values?.accessProtocol),
    },
    {
      label: t('video.device.info.transport'),
      field: 'transport',
      component: 'ApiSelect',
      helpMessage: t('video.device.info.transportHelp'),
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.VIDEO_DEVICE_TRANSPORT),
      },
      ifShow: ({ values }) => isSipProtocol(values?.accessProtocol),
    },
    {
      label: t('video.device.info.streamMode'),
      field: 'streamMode',
      component: 'ApiSelect',
      helpMessage: t('video.device.info.streamModeHelp'),
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.VIDEO_DEVICE_STREAM_MODE),
      },
      ifShow: ({ values }) => isSipProtocol(values?.accessProtocol),
    },
    {
      label: t('video.device.info.expires'),
      field: 'expires',
      component: 'InputNumber',
      helpMessage: t('video.device.info.expiresHelp'),
      componentProps: {
        min: 0,
        placeholder: 'e.g. 3600',
        addonAfter: 's',
      },
      ifShow: ({ values }) => isSipProtocol(values?.accessProtocol),
    },
    {
      label: t('video.device.info.keepaliveInterval'),
      field: 'keepaliveInterval',
      component: 'InputNumber',
      helpMessage: t('video.device.info.keepaliveIntervalHelp'),
      componentProps: {
        min: 1,
        placeholder: 'e.g. 60',
        addonAfter: 's',
      },
      ifShow: ({ values }) => isSipProtocol(values?.accessProtocol),
    },
    {
      label: t('video.device.info.keepaliveTimeoutCount'),
      field: 'keepaliveTimeoutCount',
      component: 'InputNumber',
      helpMessage: t('video.device.info.keepaliveTimeoutCountHelp'),
      componentProps: {
        min: 1,
        placeholder: 'e.g. 3',
      },
      ifShow: ({ values }) => isSipProtocol(values?.accessProtocol),
    },
    {
      label: t('video.device.info.authType'),
      field: 'authType',
      component: 'ApiSelect',
      componentProps: {
        ...dictComponentProps(DictEnum.VIDEO_GB28181_SIP_REGISTER_WAY),
      },
      ifShow: ({ values }) => isSipProtocol(values?.accessProtocol),
    },
    {
      // RTSP / ONVIF 用同一个 authSecret 字段存密码（实体已加密落库）
      label: t('video.device.info.authSecret'),
      field: 'authSecret',
      component: 'InputPassword',
    },
    {
      label: t('video.device.info.ability'),
      field: 'ability',
      component: 'Input',
    },
    {
      label: t('video.device.info.extendParams'),
      field: 'extendParams',
      component: 'Input',
    },
    {
      label: t('video.device.info.remark'),
      field: 'remark',
      component: 'InputTextArea',
      colProps: { span: 22 },
      componentProps: {
        rows: 3,
        maxlength: 500,
        showCount: true,
      },
    },
  ];
};

// 前端自定义表单验证规则
export const customFormSchemaRules = (_): Partial<FormSchemaExt>[] => {
  return [];
};
