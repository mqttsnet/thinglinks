import { Ref } from 'vue';
import { Tag } from 'ant-design-vue';
import { dateUtil } from '/@/utils/dateUtil';
import { yesNoComponentProps, renderYesNoComponent } from '/@/utils/thinglinks/common';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
import { dictComponentProps } from '/@/utils/thinglinks/common';
import { FormSchemaExt } from '/@/api/thinglinks/common/formValidateService';
import { echoMapText } from '/@/utils/echo';
const { t } = useI18n();

// 列表页字段
export const columns = (): BasicColumn[] => {
  return [
    {
      title: t('video.media.push.appId'),
      dataIndex: ['echoMap', 'appId'],
    },
    {
      title: t('video.media.push.streamIdentification'),
      dataIndex: 'streamIdentification',
    },
    {
      title: t('video.media.push.totalReaderCount'),
      dataIndex: 'totalReaderCount',
    },
    {
      title: t('video.media.push.originType'),
      dataIndex: ['echoMap', 'originType'],
    },
    {
      title: t('video.media.push.originUrl'),
      dataIndex: 'originUrl',
    },
    {
      title: t('video.media.push.vhost'),
      dataIndex: 'vhost',
    },
    {
      title: t('video.media.push.bytesSpeed'),
      dataIndex: 'bytesSpeed',
    },
    {
      title: t('video.media.push.aliveSecond'),
      dataIndex: 'aliveSecond',
    },
    {
      title: t('video.media.push.mediaIdentification'),
      dataIndex: 'mediaIdentification',
    },
    {
      title: t('video.media.push.serverId'),
      dataIndex: 'serverId',
    },
    {
      title: t('video.media.push.pushTime'),
      dataIndex: 'pushTime',
    },
    {
      title: t('video.media.push.status'),
      dataIndex: 'status',
      customRender: ({ text }) => renderYesNoComponent(text, true),
    },
    {
      title: t('video.media.push.pushIng'),
      dataIndex: 'pushIng',
      customRender: ({ text }) => renderYesNoComponent(text, true),
    },
    {
      title: t('video.media.push.self'),
      dataIndex: 'self',
      customRender: ({ text }) => renderYesNoComponent(text, true),
    },
    {
      title: t('video.media.push.extendParams'),
      dataIndex: 'extendParams',
    },
    {
      title: t('video.media.push.remark'),
      dataIndex: 'remark',
    },
    {
      title: t('video.media.push.createdOrgId'),
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
      label: t('video.media.push.appId'),
      field: 'appId',
      component: 'ApiSelect',
      colProps: { span: 6 },
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.VIDEO_APPLICATION_SCENARIO),
      },
    },
    {
      label: t('video.media.push.streamIdentification'),
      field: 'streamIdentification',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('video.media.push.originType'),
      field: 'originType',
      component: 'ApiSelect',
      componentProps: {
        ...dictComponentProps(DictEnum.VIDEO_MEDIA_ORIGIN_TYPE),
      },
      colProps: { span: 6 },
    },
    {
      label: t('video.media.push.originUrl'),
      field: 'originUrl',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('video.media.push.vhost'),
      field: 'vhost',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('video.media.push.mediaIdentification'),
      field: 'mediaIdentification',
      component: 'ApiVideoSelectNodeCard',
      colProps: { span: 6 },
    },
    {
      label: t('video.media.push.pushTime'),
      field: 'pushTime',
      component: 'DatePicker',
      componentProps: {
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        showTime: { defaultValue: dateUtil('00:00:00', 'HH:mm:ss') },
      },
      colProps: { span: 6 },
    },
    {
      label: t('video.media.push.status'),
      field: 'status',
      component: 'ApiSelect',
      componentProps: {
        allowClear: true,
        ...dictComponentProps(DictEnum.VIDEO_MEDIA_STREAM_PROXY_STATUS),
      },
      colProps: { span: 6 },
    },
    {
      label: t('video.media.push.pushIng'),
      field: 'pushIng',
      component: 'RadioGroup',
      componentProps: {
        ...yesNoComponentProps(),
      },
      colProps: { span: 6 },
    },
    {
      label: t('video.media.push.self'),
      field: 'self',
      component: 'RadioGroup',
      componentProps: {
        ...yesNoComponentProps(),
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
  const isAdd = () => _type.value === ActionEnum.ADD;
  return [
    {
      field: 'id',
      label: 'ID',
      component: 'Input',
      show: false,
    },
    {
      label: t('video.media.push.appId'),
      field: 'appId',
      component: 'ApiSelect',
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.VIDEO_APPLICATION_SCENARIO),
      },
      required: true,
    },
    {
      label: t('video.media.push.streamIdentification'),
      field: 'streamIdentification',
      component: 'Input',
      required: true,
      dynamicDisabled: () => !isAdd(),
    },
    {
      label: t('video.media.push.mediaIdentification'),
      field: 'mediaIdentification',
      component: 'ApiVideoSelectNodeCard',
      required: true,
      colProps: {
        span: 22,
      },
    },
    {
      label: t('video.media.push.status'),
      field: 'status',
      component: 'ApiSelect',
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.VIDEO_MEDIA_STREAM_PROXY_STATUS),
      },
      required: true,
    },
    // --- 以下为运行时字段，新增时隐藏，编辑时只读 ---
    {
      label: t('video.media.push.totalReaderCount'),
      field: 'totalReaderCount',
      component: 'Input',
      ifShow: () => !isAdd(),
      dynamicDisabled: true,
    },
    {
      label: t('video.media.push.originType'),
      field: 'originType',
      component: 'ApiSelect',
      componentProps: {
        ...dictComponentProps(DictEnum.VIDEO_MEDIA_ORIGIN_TYPE),
      },
      ifShow: () => !isAdd(),
      dynamicDisabled: true,
    },
    {
      label: t('video.media.push.originUrl'),
      field: 'originUrl',
      component: 'Input',
      ifShow: () => !isAdd(),
      dynamicDisabled: true,
    },
    {
      label: t('video.media.push.vhost'),
      field: 'vhost',
      component: 'Input',
      ifShow: () => !isAdd(),
      dynamicDisabled: true,
    },
    {
      label: t('video.media.push.bytesSpeed'),
      field: 'bytesSpeed',
      component: 'Input',
      ifShow: () => !isAdd(),
      dynamicDisabled: true,
    },
    {
      label: t('video.media.push.aliveSecond'),
      field: 'aliveSecond',
      component: 'Input',
      ifShow: () => !isAdd(),
      dynamicDisabled: true,
    },
    {
      label: t('video.media.push.serverId'),
      field: 'serverId',
      component: 'Input',
      ifShow: () => !isAdd(),
      dynamicDisabled: true,
    },
    {
      label: t('video.media.push.pushTime'),
      field: 'pushTime',
      component: 'DatePicker',
      componentProps: {
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        showTime: { defaultValue: dateUtil('00:00:00', 'HH:mm:ss') },
      },
      ifShow: () => !isAdd(),
      dynamicDisabled: true,
    },
    {
      label: t('video.media.push.pushIng'),
      field: 'pushIng',
      component: 'RadioGroup',
      defaultValue: false,
      componentProps: {
        ...yesNoComponentProps(),
      },
      ifShow: () => !isAdd(),
      dynamicDisabled: true,
    },
    {
      label: t('video.media.push.self'),
      field: 'self',
      component: 'RadioGroup',
      defaultValue: false,
      dynamicDisabled: true,
      componentProps: {
        ...yesNoComponentProps(),
      },
      ifShow: () => !isAdd(),
    },
    // --- 用户可编辑的通用字段 ---
    {
      label: t('video.media.push.extendParams'),
      field: 'extendParams',
      component: 'Input',
    },
    {
      label: t('video.media.push.remark'),
      field: 'remark',
      component: 'InputTextArea',
    },
  ];
};

// 前端自定义表单验证规则
export const customFormSchemaRules = (_): Partial<FormSchemaExt>[] => {
  return [];
};
