import { Ref } from 'vue';
import { Tag } from 'ant-design-vue';
import { renderYesNoComponent, yesNoComponentProps } from '/@/utils/thinglinks/common';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { FormSchemaExt } from '/@/api/thinglinks/common/formValidateService';
import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
import { dictComponentProps } from '/@/utils/thinglinks/common';
import { VideoStreamProxyType } from '/@/enums/video/streamProxy';
import { echoMapText } from '/@/utils/echo';
const { t } = useI18n();

// 列表页字段
export const columns = (): BasicColumn[] => {
  return [
    {
      title: t('video.media.proxy.appId'),
      dataIndex: ['echoMap', 'appId'],
    },
    {
      title: t('video.media.proxy.proxyType'),
      dataIndex: ['echoMap', 'proxyType'],
    },
    {
      title: t('video.media.proxy.proxyName'),
      dataIndex: 'proxyName',
    },
    {
      title: t('video.media.proxy.streamIdentification'),
      dataIndex: 'streamIdentification',
    },
    {
      title: t('video.media.proxy.url'),
      dataIndex: 'url',
    },
    {
      title: t('video.media.proxy.srcUrl'),
      dataIndex: 'srcUrl',
    },
    {
      title: t('video.media.proxy.dstUrl'),
      dataIndex: 'dstUrl',
    },
    {
      title: t('video.media.proxy.timeoutMs'),
      dataIndex: 'timeoutMs',
    },
    {
      title: t('video.media.proxy.ffmpegCmdKey'),
      dataIndex: 'ffmpegCmdKey',
    },
    {
      title: t('video.media.proxy.rtpType'),
      dataIndex: ['echoMap', 'rtpType'],
    },
    {
      title: t('video.media.proxy.gbIdentification'),
      dataIndex: 'gbIdentification',
    },
    {
      title: t('video.media.proxy.mediaIdentification'),
      dataIndex: 'mediaIdentification',
    },
    {
      title: t('video.media.proxy.enableAudio'),
      dataIndex: 'enableAudio',
      customRender: ({ text }) => {
        return renderYesNoComponent(text, true);
      },
    },
    {
      title: t('video.media.proxy.enableMp4'),
      dataIndex: 'enableMp4',
      customRender: ({ text }) => {
        return renderYesNoComponent(text, true);
      },
    },
    {
      title: t('video.media.proxy.status'),
      dataIndex: 'status',
      customRender: ({ text }) => {
        return renderYesNoComponent(text, true);
      },
    },
    {
      title: t('video.media.proxy.enableRemoveNoneReader'),
      dataIndex: 'enableRemoveNoneReader',
      customRender: ({ text }) => {
        return renderYesNoComponent(text, true);
      },
    },
    {
      title: t('video.media.proxy.streamKey'),
      dataIndex: 'streamKey',
    },
    {
      title: t('video.media.proxy.enableDisableNoneReader'),
      dataIndex: 'enableDisableNoneReader',
      customRender: ({ text }) => {
        return renderYesNoComponent(text, true);
      },
    },
    {
      title: t('video.media.proxy.extendParams'),
      dataIndex: 'extendParams',
    },
    {
      title: t('video.media.proxy.remark'),
      dataIndex: 'remark',
    },
    {
      title: t('video.media.proxy.createdOrgId'),
      dataIndex: 'createdOrgId',
      customRender: ({ record }) => echoMapText(record, 'createdOrgId'),
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
      label: t('video.media.proxy.appId'),
      field: 'appId',
      component: 'ApiSelect',
      colProps: { span: 6 },
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.VIDEO_APPLICATION_SCENARIO),
      },
    },
    {
      label: t('video.media.proxy.proxyType'),
      field: 'proxyType',
      component: 'ApiSelect',
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.VIDEO_MEDIA_STREAM_PROXY_TYPE),
      },
      colProps: { span: 6 },
    },
    {
      label: t('video.media.proxy.proxyName'),
      field: 'proxyName',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('video.media.proxy.streamIdentification'),
      field: 'streamIdentification',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('video.media.proxy.timeoutMs'),
      field: 'timeoutMs',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('video.media.proxy.rtpType'),
      field: 'rtpType',
      component: 'ApiSelect',
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.VIDEO_MEDIA_STREAM_PROXY_RTP_TYPE),
      },
      colProps: { span: 6 },
    },
    {
      label: t('video.media.proxy.mediaIdentification'),
      field: 'mediaIdentification',
      component: 'ApiVideoSelectNodeCard',
      colProps: { span: 6 },
    },
    {
      label: t('video.media.proxy.enableAudio'),
      field: 'enableAudio',
      component: 'RadioGroup',
      componentProps: {
        ...yesNoComponentProps(),
      },
      colProps: { span: 6 },
    },
    {
      label: t('video.media.proxy.status'),
      field: 'status',
      component: 'ApiSelect',
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.VIDEO_MEDIA_STREAM_PROXY_STATUS),
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
      label: t('video.media.proxy.proxyName'),
      field: 'proxyName',
      component: 'Input',
      required: true,
    },
    {
      label: t('video.media.proxy.appId'),
      field: 'appId',
      component: 'ApiSelect',
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.VIDEO_APPLICATION_SCENARIO),
      },
      required: true,
    },
    {
      label: t('video.media.proxy.proxyType'),
      field: 'proxyType',
      component: 'ApiSelect',
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.VIDEO_MEDIA_STREAM_PROXY_TYPE),
      },
      required: true,
    },
    {
      label: t('video.media.proxy.streamIdentification'),
      field: 'streamIdentification',
      component: 'Input',
      required: true,
    },
    // 默认代理：直接给 ZLM addStreamProxy 一个源 URL，由 ZLM 主动拉
    {
      label: t('video.media.proxy.url'),
      field: 'url',
      component: 'Input',
      colProps: { span: 22 },
      helpMessage: t('video.media.proxy.urlHelp'),
      componentProps: {
        placeholder: 'rtsp://admin:pass@host:554/h264/ch1/main/av_stream',
      },
      required: true,
      ifShow: ({ values }) => values?.proxyType !== VideoStreamProxyType.FFMPEG,
    },
    // FFmpeg 代理：用 ffmpeg 把任意源（HTTP/MMS/复杂 RTSP）拉到 ZLM
    {
      label: t('video.media.proxy.srcUrl'),
      field: 'srcUrl',
      component: 'Input',
      colProps: { span: 22 },
      helpMessage: t('video.media.proxy.srcUrlHelp'),
      required: true,
      ifShow: ({ values }) => values?.proxyType === VideoStreamProxyType.FFMPEG,
    },
    // dstUrl 完全由后端按"流媒体节点 + appId + streamIdentification"拼装，前端只读展示
    {
      label: t('video.media.proxy.dstUrl'),
      field: 'dstUrl',
      component: 'Input',
      colProps: { span: 22 },
      helpMessage: t('video.media.proxy.dstUrlHelp'),
      ifShow: () => _type.value !== ActionEnum.ADD,
      dynamicDisabled: true,
    },
    {
      label: t('video.media.proxy.timeoutMs'),
      field: 'timeoutMs',
      component: 'InputNumber',
      helpMessage: t('video.media.proxy.timeoutMsHelp'),
      defaultValue: 15,
      componentProps: {
        min: 1,
        max: 300,
        addonAfter: 's',
      },
      ifShow: ({ values }) => values?.proxyType === VideoStreamProxyType.FFMPEG,
    },
    {
      label: t('video.media.proxy.rtpType'),
      field: 'rtpType',
      component: 'ApiSelect',
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.VIDEO_MEDIA_STREAM_PROXY_RTP_TYPE),
      },
      required: true,
    },
    {
      label: t('video.media.proxy.enableAudio'),
      field: 'enableAudio',
      component: 'RadioGroup',
      defaultValue: false,
      componentProps: {
        ...yesNoComponentProps(),
      },
      required: true,
    },
    {
      label: t('video.media.proxy.enableMp4'),
      field: 'enableMp4',
      component: 'RadioGroup',
      defaultValue: false,
      componentProps: {
        ...yesNoComponentProps(),
      },
      required: true,
    },
    {
      label: t('video.media.proxy.enableRemoveNoneReader'),
      field: 'enableRemoveNoneReader',
      component: 'RadioGroup',
      defaultValue: false,
      componentProps: {
        ...yesNoComponentProps(),
      },
      required: true,
    },
    {
      label: t('video.media.proxy.status'),
      field: 'status',
      component: 'ApiSelect',
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.VIDEO_MEDIA_STREAM_PROXY_STATUS),
      },
      required: true,
    },
    {
      label: t('video.media.proxy.mediaIdentification'),
      field: 'mediaIdentification',
      component: 'ApiVideoSelectNodeCard',
      required: true,
      colProps: {
        span: 22,
      },
    },
    {
      label: t('video.media.proxy.remark'),
      field: 'remark',
      component: 'InputTextArea',
      colProps: {
        span: 22,
      },
    },
  ];
};

// 前端自定义表单验证规则
export const customFormSchemaRules = (_): Partial<FormSchemaExt>[] => {
  return [];
};
