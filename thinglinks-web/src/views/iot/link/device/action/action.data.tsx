import { Ref } from 'vue';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { ActionEnum } from '/@/enums/commonEnum';
import { FormSchemaExt } from '/@/api/thinglinks/common/formValidateService';
import { Tag } from 'ant-design-vue';
import { dictComponentProps, handleCopyTextV2 } from '/@/utils/thinglinks/common';
import { DictEnum } from '/@/enums/commonEnum';
import { useDict } from '/@/components/Dict';
import { CopyOutlined, EyeOutlined } from '@ant-design/icons-vue';

const { getDictLabel } = useDict();

const { t } = useI18n();

// 列表页字段
export const columns = ({ openRemarkModal }: { openRemarkModal: Fn }): BasicColumn[] => {
  return [
    // {
    //   title: t('iot.link.device.device.id'),
    //   dataIndex: 'id',
    // },
    // {
    //   title: t('iot.link.device.device.deviceIdentification'),
    //   dataIndex: 'deviceIdentification',
    // },
    {
      // 动作类型 ── 后端 VO 已 @Echo 回填中文到 echoMap.actionType,
      // 优先取回填值,fallback 走前端字典翻译,都没有时显示原始 code 而非空,便于排查
      title: t('iot.link.device.device.actionType'),
      dataIndex: 'actionType',
      width: 160,
      customRender: ({ record }) => {
        return (
          record?.echoMap?.actionType ||
          getDictLabel('LINK_DEVICE_ACTION_TYPE', record?.actionType, record?.actionType ?? '-')
        );
      },
    },
    {
      title: t('iot.link.device.device.message'),
      dataIndex: 'message',
      ellipsis: true,
      showSorterTooltip: true,
      customRender: ({ record }) => {
        return (
          <div>
            {record.message ? (
              <span>
                <CopyOutlined
                  onClick={() => handleCopyTextV2(record.message || '')}
                  style={{ marginRight: '10px' }}
                />
                <EyeOutlined
                  onClick={() =>
                    openRemarkModal(record.message, t('iot.link.device.device.content'))
                  }
                  style={{ marginRight: '10px' }}
                />
              </span>
            ) : null}
            <span>{record.message}</span>
          </div>
        );
      },
    },
    {
      title: t('iot.link.device.device.remark'),
      dataIndex: 'remark',
      ellipsis: true,
      showSorterTooltip: true,
      customRender: ({ record }) => {
        // 备注多为 trace JSON,过长 hover tooltip 展示不下;改走眼睛图标 → ViewValueModal 格式化弹窗(与指令记录一致)
        return (
          <div>
            {record.remark ? (
              <EyeOutlined
                onClick={() => openRemarkModal(record.remark, t('iot.link.device.device.remark'))}
                style={{ marginRight: '10px' }}
              />
            ) : null}
            <span>{record.remark}</span>
          </div>
        );
      },
    },
    {
      title: t('iot.link.device.device.createdTime'),
      dataIndex: 'createdTime',
      sorter: true,
      width: 180,
    },
    {
      // 状态 ── 后端目前 status 字段无 @Echo;label 走前端字典翻译,
      // 但如果将来后端补 @Echo 也能无缝优先使用 echoMap.status
      title: t('iot.link.device.device.status'),
      dataIndex: 'status',
      width: 100,
      customRender: ({ record }) => {
        const label =
          record?.echoMap?.status || getDictLabel('LINK_DEVICE_ACTION_STATUS', record?.status, '');
        if (record?.status == 0) {
          return (
            <Tag color="green" style={{ marginRight: '0px' }}>
              {label}
            </Tag>
          );
        } else if (record?.status == 1) {
          return (
            <Tag color="red" style={{ marginRight: '0px' }}>
              {label}
            </Tag>
          );
        }
        return label || '-';
      },
    },
  ];
};

export const searchFormSchema = (): FormSchema[] => {
  return [
    {
      // 动作类型 ── 字典文案如"客户端被踢出连接"较长,默认 dropdown 宽度 = trigger 宽度会截断;
      // 加 dropdownMatchSelectWidth: false 让下拉宽度按内容自适应,minWidth 防过窄。
      // (showSearch / filterOption 已在 dictComponentProps 默认设置)
      label: t('iot.link.device.device.actionType'),
      field: 'actionType',
      component: 'ApiSelect',
      show: true,
      componentProps: {
        disabled: false,
        allowClear: true,
        dropdownMatchSelectWidth: false,
        dropdownStyle: { minWidth: '220px' },
        ...dictComponentProps(DictEnum.LINK_DEVICE_ACTION_TYPE),
      },
      colProps: { span: 5 },
    },
    {
      label: t('iot.link.device.device.status'),
      field: 'status',
      component: 'ApiSelect',
      show: true,
      componentProps: {
        disabled: false,
        allowClear: true,
        dropdownMatchSelectWidth: false,
        dropdownStyle: { minWidth: '180px' },
        ...dictComponentProps(DictEnum.LINK_DEVICE_ACTION_STATUS),
      },
      colProps: { span: 5 },
    },
    {
      field: 'createTimeRange',
      label: t('iot.link.device.device.createdTime'),
      component: 'RangePicker',
      colProps: { span: 8 },
    },
  ];
};

// 编辑页字段
export const editFormSchema = (_type: Ref<ActionEnum>): FormSchema[] => {
  return [
    {
      label: 'ID',
      field: 'id',
      component: 'Input',
      show: false,
    },
    {
      label: t('iot.link.device.device.deviceIdentification'),
      field: 'deviceIdentification',
      component: 'Input',
      show: true,
      rules: [{ required: true }],
      componentProps: {
        disabled: true,
        // placeholder: `请输入${t('iot.link.device.device.deviceIdentification')}`,
      },
    },
    {
      label: t('iot.link.device.device.actionType'),
      field: 'ApiSelect',
      component: 'Select',
      show: true,
      componentProps: {
        disabled: false,
        allowClear: false,
        // placeholder: `请选择${t('iot.link.device.device.actionType')}`,
        ...dictComponentProps(DictEnum.LINK_DEVICE_ACTION_TYPE),
      },
    },
    {
      label: t('iot.link.device.device.message'),
      field: 'message',
      component: 'InputTextArea',
      show: true,
      rules: [{ required: false }],
      componentProps: {
        disabled: false,
        // placeholder: `请输入${t('iot.link.device.device.message')}`,
      },
    },
    {
      label: t('iot.link.device.device.status'),
      field: 'status',
      component: 'ApiSelect',
      defaultValue: '0',
      show: true,
      componentProps: {
        disabled: false,
        allowClear: false,
        // placeholder: `请选择${t('iot.link.device.device.status')}`,
        ...dictComponentProps(DictEnum.LINK_PRODUCT_STATUS),
      },
    },
    {
      label: t('iot.link.device.device.remark'),
      field: 'remark',
      component: 'InputTextArea',
      show: true,
      rules: [{ required: false }],
      componentProps: {
        disabled: false,
        // placeholder: `请输入${t('iot.link.device.device.remark')}`,
      },
    },
  ];
};

// 前端自定义表单验证规则
export const customFormSchemaRules = (_): Partial<FormSchemaExt>[] => {
  return [];
};
