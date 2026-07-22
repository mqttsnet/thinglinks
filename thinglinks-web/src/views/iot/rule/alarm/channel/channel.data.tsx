import { Ref } from 'vue';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { dictComponentProps } from '/@/utils/thinglinks/common';
import { ActionEnum } from '/@/enums/commonEnum';
import { DictEnum } from '/@/enums/commonEnum';
import { FormSchemaExt } from '/@/api/thinglinks/common/formValidateService';
import { echoMapText } from '/@/utils/echo';
import type { CardField } from '/@/components/BusinessCardList';
import { query as queryEmployee } from '/@/api/basic/user/baseEmployee';
import { tree as queryOrgTree } from '/@/api/basic/user/baseOrg';
import type {
  BaseEmployeePageQuery,
  BaseEmployeeResultVO,
} from '/@/api/basic/user/model/baseEmployeeModel';
import { useUserStoreWithOut } from '/@/store/modules/user';

const { t } = useI18n();
const searchColProps = { xs: 24, sm: 12, md: 8, lg: 6, xl: 6, xxl: 4 };
export const CHANNEL_TYPE = {
  DING_TALK: '0',
  ENTERPRISE_WECHAT: '1',
  FEISHU: '2',
  SITE_MESSAGE: '3',
} as const;
const ROBOT_CHANNEL_TYPES: string[] = [
  CHANNEL_TYPE.DING_TALK,
  CHANNEL_TYPE.ENTERPRISE_WECHAT,
  CHANNEL_TYPE.FEISHU,
];

async function queryCurrentDeptEmployees(
  params: BaseEmployeePageQuery = {},
): Promise<BaseEmployeeResultVO[]> {
  const userStore = useUserStoreWithOut();
  const baseEmployee = userStore.getUserInfo?.baseEmployee;
  const currentDeptId = baseEmployee?.lastDeptId || baseEmployee?.createdOrgId;
  if (!currentDeptId) {
    return queryEmployee(params);
  }

  let orgIdList = [String(currentDeptId)];
  try {
    const orgTree = await queryOrgTree({ state: true });
    const scopedOrgIds = collectOrgIds(orgTree, currentDeptId);
    if (scopedOrgIds.length) {
      orgIdList = scopedOrgIds;
    }
  } catch {
    // 查询组织树失败时仍限制在当前部门，避免接收人范围被放大。
  }

  return queryEmployee({
    ...params,
    orgIdList,
  });
}

function collectOrgIds(treeData: unknown, rootId: string | number): string[] {
  const ids: string[] = [];
  const roots = Array.isArray(treeData) ? treeData : treeData ? [treeData] : [];

  const walk = (nodes: Recordable[], matchedParent: boolean) => {
    nodes.forEach((node) => {
      const matched = matchedParent || String(node.id) === String(rootId);
      if (matched && node.id !== undefined && node.id !== null) {
        ids.push(String(node.id));
      }
      if (Array.isArray(node.children)) {
        walk(node.children, matched);
      }
    });
  };

  walk(roots as Recordable[], false);
  return ids;
}

function getSelectValue(item: unknown): string {
  if (item === undefined || item === null) {
    return '';
  }
  if (typeof item === 'object') {
    const option = item as Recordable;
    return String(option.value ?? option.id ?? option.key ?? option.label ?? '').trim();
  }
  return String(item).trim();
}

function hasRecipientValue(value: unknown): boolean {
  if (Array.isArray(value)) {
    return value.some((item) => getSelectValue(item));
  }
  if (typeof value === 'string') {
    return value.split(/[,，\n]/).some((item) => item.trim());
  }
  return Boolean(getSelectValue(value));
}

export const channelTypeOptions = () => [
  { label: t('iot.link.engine.channel.channelTypeOption.dingTalk'), value: CHANNEL_TYPE.DING_TALK },
  {
    label: t('iot.link.engine.channel.channelTypeOption.enterpriseWechat'),
    value: CHANNEL_TYPE.ENTERPRISE_WECHAT,
  },
  { label: t('iot.link.engine.channel.channelTypeOption.feishu'), value: CHANNEL_TYPE.FEISHU },
  {
    label: t('iot.link.engine.channel.channelTypeOption.siteMessage'),
    value: CHANNEL_TYPE.SITE_MESSAGE,
  },
];

export const resolveAlarmChannelTypeLabel = (value?: string | number) => {
  const option = channelTypeOptions().find((item) => String(item.value) === String(value));
  return option?.label || String(value ?? '');
};

export const cardFields = (): CardField[] => [
  {
    label: t('iot.link.engine.channel.channelType'),
    field: 'channelTypeLabel',
    span: 12,
  },
  {
    label: t('iot.link.engine.channel.id'),
    field: 'id',
    span: 12,
  },
  {
    label: t('iot.link.engine.channel.createdTime'),
    field: 'createdTime',
    span: 12,
  },
  {
    label: t('iot.link.engine.channel.remark'),
    field: 'remark',
    span: 12,
  },
];

// 列表页字段
export const columns = (): BasicColumn[] => {
  return [
    {
      title: t('iot.link.engine.channel.id'),
      dataIndex: 'id',
    },
    {
      title: t('iot.link.engine.channel.channelName'),
      dataIndex: 'channelName',
    },
    {
      title: t('iot.link.engine.channel.channelType'),
      dataIndex: 'channelType',
      slots: { customRender: 'channelType' },
    },
    {
      title: t('iot.link.engine.channel.remark'),
      dataIndex: 'remark',
    },
    {
      title: t('iot.link.engine.channel.channelStatus'),
      dataIndex: 'status',
      slots: { customRender: 'status' },
    },
    {
      title: t('iot.link.engine.channel.createdBy'),
      dataIndex: 'createdBy',
      customRender: ({ record }) => echoMapText(record, 'createdBy'),
    },
    {
      title: t('iot.link.engine.channel.createdTime'),
      dataIndex: 'createdTime',
      sorter: true,
      width: 180,
    },
    {
      title: t('iot.link.engine.channel.updatedBy'),
      dataIndex: 'updatedBy',
      customRender: ({ record }) => echoMapText(record, 'updatedBy'),
    },
    {
      title: t('iot.link.engine.channel.updatedTime'),
      dataIndex: 'updatedTime',
      sorter: true,
      width: 180,
    },
  ];
};

export const searchFormSchema = (): FormSchema[] => {
  return [
    {
      label: t('iot.link.engine.channel.channelName'),
      field: 'channelName',
      component: 'Input',
      colProps: searchColProps,
    },
    {
      field: 'channelType',
      label: t('iot.link.engine.channel.channelType'),
      component: 'Select',
      colProps: searchColProps,
      show: true,
      componentProps: {
        options: channelTypeOptions(),
        showSearch: true,
        optionFilterProp: 'label',
      },
    },
    {
      field: 'status',
      label: t('iot.link.engine.channel.channelStatus'),
      component: 'ApiSelect',
      colProps: searchColProps,
      show: true,
      componentProps: {
        ...dictComponentProps(DictEnum.RULE_ALARM_CHANNEL_STATUS),
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
    {
      label: t('iot.link.engine.channel.channelName'),
      field: 'channelName',
      rules: [{ required: true }],
      component: 'Input',
    },
    {
      label: t('iot.link.engine.channel.status'),
      field: 'status',
      component: 'ApiSelect',
      defaultValue: '0',
      show: true,
      rules: [{ required: true }],
      componentProps: {
        disabled: false,
        allowClear: false,
        // placeholder: `请选择${t('iot.link.engine.channel.status')}`,
        ...dictComponentProps(DictEnum.RULE_ALARM_CHANNEL_STATUS),
      },
    },
    {
      field: 'channelType',
      label: t('iot.link.engine.channel.channelType'),
      rules: [{ required: true }],
      component: 'Select',
      componentProps: {
        disabled: false,
        allowClear: false,
        options: channelTypeOptions(),
        showSearch: true,
        optionFilterProp: 'label',
      },
    },
    {
      field: 'token',
      label: t('iot.link.engine.channel.token'),
      rules: [{ required: true }],
      component: 'Input',
      show: ({ values }) => {
        return ROBOT_CHANNEL_TYPES.includes(String(values.channelType));
      },
    },
    {
      field: 'appId',
      label: t('iot.link.engine.channel.appId'),
      rules: [{ required: true }],
      component: 'Input',
      show: ({ values }) => {
        return values.channelType == CHANNEL_TYPE.FEISHU;
      },
    },
    {
      field: 'appSecret',
      label: t('iot.link.engine.channel.appSecret'),
      rules: [{ required: true }],
      component: 'Input',
      show: ({ values }) => {
        return values.channelType == CHANNEL_TYPE.FEISHU;
      },
    },

    {
      field: 'serverAddress',
      label: t('iot.link.engine.channel.serverAddress'),
      rules: [{ required: true }],
      component: 'Input',
      show: () => {
        return false;
      },
    },
    {
      field: 'port',
      label: t('iot.link.engine.channel.port'),
      rules: [{ required: true }],
      component: 'Input',
      show: () => {
        return false;
      },
    },
    {
      field: 'sendPeople',
      label: t('iot.link.engine.channel.sendPeople'),
      rules: [{ required: true }],
      component: 'Input',
      show: () => {
        return false;
      },
      componentProps: {
        disabled: false,
        allowClear: false,
        placeholder: t('iot.link.engine.channel.placeholder.userName'),
      },
    },
    {
      field: 'userName',
      label: t('iot.link.engine.channel.userName'),
      rules: [{ required: true }],
      component: 'Input',
      show: () => {
        return false;
      },
      componentProps: {
        disabled: false,
        allowClear: false,
        placeholder: t('iot.link.engine.channel.placeholder.userName'),
      },
    },
    {
      field: 'password',
      label: t('iot.link.engine.channel.password'),
      rules: [{ required: true }],
      component: 'Input',
      show: () => {
        return false;
      },
    },
    {
      field: 'reginId',
      label: 'ReginId',
      rules: [{ required: true }],
      component: 'Input',
      show: () => {
        return false;
      },
    },
    {
      field: 'accessKeyld',
      label: 'AccessKeyld',
      rules: [{ required: true }],
      component: 'Input',
      show: () => {
        return false;
      },
    },
    {
      field: 'secret',
      label: t('iot.link.engine.channel.secret'),
      rules: [{ required: true }],
      component: 'Input',
      show: ({ values }) => {
        return values.channelType == CHANNEL_TYPE.DING_TALK;
      },
    },
    {
      field: 'remindMode',
      label: t('iot.link.engine.channel.remindMode'),
      component: 'ApiSelect',
      defaultValue: '02',
      rules: [{ required: true }],
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.NoticeRemindModeEnum),
      },
      show: ({ values }) => {
        return values.channelType == CHANNEL_TYPE.SITE_MESSAGE;
      },
    },
    {
      field: 'target',
      label: t('iot.link.engine.channel.target'),
      component: 'ApiSelect',
      defaultValue: '01',
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.EchoDictType_Base_NOTICE_TARGET),
      },
      show: ({ values }) => {
        return values.channelType == CHANNEL_TYPE.SITE_MESSAGE;
      },
    },
    {
      field: 'autoRead',
      label: t('iot.link.engine.channel.autoRead'),
      component: 'Switch',
      defaultValue: false,
      componentProps: {
        checkedValue: true,
        unCheckedValue: false,
      },
      show: ({ values }) => {
        return values.channelType == CHANNEL_TYPE.SITE_MESSAGE;
      },
    },
    {
      field: 'url',
      label: t('iot.link.engine.channel.defaultUrl'),
      component: 'Input',
      componentProps: {
        placeholder: '/#/engine/linkage',
      },
      show: ({ values }) => {
        return values.channelType == CHANNEL_TYPE.SITE_MESSAGE;
      },
    },
    {
      field: 'recipientList',
      label: t('iot.link.engine.channel.recipientList'),
      component: 'ApiSelect',
      rules: [
        {
          trigger: 'change',
          validator: async (_, value) => {
            if (hasRecipientValue(value)) {
              return Promise.resolve();
            }
            return Promise.reject(new Error(t('iot.link.engine.channel.recipientListRequired')));
          },
        },
      ],
      componentProps: {
        api: queryCurrentDeptEmployees,
        labelField: 'realName',
        valueField: 'id',
        mode: 'multiple',
        showSearch: true,
        numberToString: true,
        optionFilterProp: 'label',
        placeholder: t('iot.link.engine.channel.placeholder.recipientList'),
      },
      show: ({ values }) => {
        return values.channelType == CHANNEL_TYPE.SITE_MESSAGE;
      },
    },
    {
      field: 'remark',
      label: t('iot.link.engine.channel.remark'),
      component: 'Input',
      show: ({ values }) => {
        return values.channelType !== undefined && values.channelType !== null;
      },
    },
  ];
};

// 前端自定义表单验证规则
export const customFormSchemaRules = (_): Partial<FormSchemaExt>[] => {
  return [];
};
