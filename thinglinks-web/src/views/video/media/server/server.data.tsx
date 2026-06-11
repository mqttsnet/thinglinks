import { Ref } from 'vue';
import { Tag } from 'ant-design-vue';
import { renderYesNoComponent } from '/@/utils/thinglinks/common';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { FormSchemaExt } from '/@/api/thinglinks/common/formValidateService';
import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
import { MediaServerType } from '/@/enums/video/mediaServer';
import { dictComponentProps } from '/@/utils/thinglinks/common';
const { t } = useI18n();

// 列表页字段
export const columns = (): BasicColumn[] => {
  return [
    {
      title: t('video.media.server.name'),
      dataIndex: 'name',
      width: 150,
      fixed: 'left',
    },
    {
      title: t('video.media.server.type'),
      dataIndex: ['echoMap', 'type'],
      width: 100,
    },
    {
      title: t('video.media.server.appId'),
      dataIndex: ['echoMap', 'appId'],
      width: 120,
    },
    {
      title: t('video.media.server.host'),
      dataIndex: 'host',
      width: 150,
    },
    {
      title: t('video.media.server.httpPort'),
      dataIndex: 'httpPort',
      width: 100,
    },
    {
      title: t('video.media.server.rtmpPort'),
      dataIndex: 'rtmpPort',
      width: 100,
    },
    {
      title: t('video.media.server.rtspPort'),
      dataIndex: 'rtspPort',
      width: 100,
    },
    {
      title: t('video.media.server.autoConfig'),
      dataIndex: 'autoConfig',
      width: 120,
      customRender: ({ text }) => renderYesNoComponent(text, true),
    },
    {
      title: t('video.media.server.rtpEnable'),
      dataIndex: 'rtpEnable',
      width: 120,
      customRender: ({ text }) => renderYesNoComponent(text, true),
    },
    {
      title: t('video.media.server.defaultServer'),
      dataIndex: 'defaultServer',
      width: 120,
      customRender: ({ text }) => renderYesNoComponent(text, true),
    },
    {
      title: t('video.media.server.onlineStatus'),
      dataIndex: 'onlineStatus',
      width: 100,
      customRender: ({ text }) => renderYesNoComponent(text, true),
    },
    {
      title: t('video.media.server.version'),
      dataIndex: 'version',
      width: 120,
    },
    {
      title: t('video.media.server.remark'),
      dataIndex: 'remark',
      width: 150,
      ellipsis: true,
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
      label: t('video.media.server.name'),
      field: 'name',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('video.media.server.appId'),
      field: 'appId',
      component: 'ApiSelect',
      colProps: { span: 6 },
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.VIDEO_APPLICATION_SCENARIO),
      },
    },
    {
      label: t('video.media.server.host'),
      field: 'host',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('video.media.server.hookHost'),
      field: 'hookHost',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('video.media.server.httpPort'),
      field: 'httpPort',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('video.media.server.secret'),
      field: 'secret',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('video.media.server.type'),
      field: 'type',
      component: 'ApiSelect',
      colProps: { span: 6 },
      componentProps: {
        ...dictComponentProps(DictEnum.VIDEO_MEDIA_SERVER_TYPE),
      },
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
    // ========== 基本信息 ==========
    {
      field: 'divider-basic',
      component: 'Divider',
      label: t('video.media.server.groupBasic'),
      colProps: { span: 24 },
    },
    {
      label: t('video.media.server.name'),
      field: 'name',
      component: 'Input',
      helpMessage: t('video.media.server.nameHelp'),
      required: true,
      componentProps: {
        placeholder: t('video.media.server.namePlaceholder'),
      },
    },
    {
      label: t('video.media.server.type'),
      field: 'type',
      component: 'ApiSelect',
      defaultValue: MediaServerType.ZLM,
      helpMessage: t('video.media.server.typeHelp'),
      componentProps: {
        ...dictComponentProps(DictEnum.VIDEO_MEDIA_SERVER_TYPE),
      },
      required: true,
    },
    {
      label: t('video.media.server.appId'),
      field: 'appId',
      component: 'ApiSelect',
      helpMessage: t('video.media.server.appIdHelp'),
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.VIDEO_APPLICATION_SCENARIO),
      },
      required: true,
    },

    // ========== 连接配置（平台 → 流媒体）==========
    // 这一组回答"平台怎么访问流媒体服务器"的问题，用于调用 ZLM HTTP 管理 API。
    {
      field: 'divider-connect',
      component: 'Divider',
      label: t('video.media.server.groupConnect'),
      colProps: { span: 24 },
    },
    {
      label: t('video.media.server.host'),
      field: 'host',
      component: 'Input',
      helpMessage: t('video.media.server.hostHelp'),
      required: true,
      componentProps: {
        placeholder: t('video.media.server.hostPlaceholder'),
      },
      rules: [
        { required: true, message: t('video.media.server.hostRequired') },
        {
          pattern: /^(\d{1,3}\.){3}\d{1,3}$|^[a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?(\.[a-zA-Z]{2,})+$/,
          message: t('video.media.server.hostInvalid'),
        },
      ],
    },
    {
      label: t('video.media.server.httpPort'),
      field: 'httpPort',
      component: 'InputNumber',
      helpMessage: t('video.media.server.httpPortHelp'),
      required: true,
      componentProps: {
        min: 1,
        max: 65535,
        placeholder: t('video.media.server.httpPortPlaceholder'),
      },
    },
    {
      label: t('video.media.server.secret'),
      field: 'secret',
      component: 'Input',
      helpMessage: t('video.media.server.secretHelp'),
      required: true,
    },
    {
      field: 'testConnection',
      label: ' ',
      component: 'Input',
      slot: 'testConnection',
      ifShow: () => _type.value !== ActionEnum.VIEW,
      colProps: { span: 22 },
    },

    // ========== 对外服务地址（设备/浏览器 → 流媒体）==========
    // 平台访问流媒体的 IP 和设备/浏览器访问流媒体的 IP 可能不同（容器、NAT、跨网段）。
    // 相同时全部留空即可，由后端沿用"服务器地址"。
    {
      field: 'divider-external',
      component: 'Divider',
      label: t('video.media.server.groupExternal'),
      colProps: { span: 24 },
    },
    {
      label: t('video.media.server.sdpHost'),
      field: 'sdpHost',
      component: 'Input',
      helpMessage: t('video.media.server.sdpHostHelp'),
      componentProps: { placeholder: t('video.media.server.sdpHostPlaceholder') },
    },
    {
      label: t('video.media.server.streamHost'),
      field: 'streamHost',
      component: 'Input',
      helpMessage: t('video.media.server.streamHostHelp'),
      componentProps: { placeholder: t('video.media.server.streamHostPlaceholder') },
    },

    // ========== Hook 回调（流媒体 → 平台）==========
    // 流媒体事件（流建立/断开/录像完成等）通过此 URL 回到平台 Gateway。
    {
      field: 'divider-hook',
      component: 'Divider',
      label: t('video.media.server.groupHook'),
      colProps: { span: 24 },
    },
    {
      label: t('video.media.server.hookHost'),
      field: 'hookHost',
      component: 'Input',
      helpMessage: t('video.media.server.hookHostHelp'),
      componentProps: { placeholder: t('video.media.server.hookHostPlaceholder') },
    },

    // ========== 备注 ==========
    {
      field: 'divider-remark',
      component: 'Divider',
      label: t('video.media.server.groupRemark'),
      colProps: { span: 24 },
    },
    {
      label: t('video.media.server.remark'),
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
