import { Ref, h } from 'vue';
import { dateUtil } from '/@/utils/dateUtil';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { DescItem } from '/@/components/Description/index';
import { useI18n } from '/@/hooks/web/useI18n';
import { ActionEnum, FileBizTypeEnum } from '/@/enums/commonEnum';
import { TenantStatusEnum } from '/@/enums/biz/tenant';
import { Tag, Badge, Switch } from 'ant-design-vue';
import { RuleType, FormSchemaExt } from '/@/api/thinglinks/common/formValidateService';
import { useMessage } from '/@/hooks/web/useMessage';
import { updateState } from '/@/api/devOperation/tenant/tenant';
import { stateFilters } from '/@/utils/thinglinks/common';
import cities from '/@/utils/thinglinks/cities.json';
import { echoMapText } from '/@/utils/echo';

const { t } = useI18n();
const { createMessage } = useMessage();

const statusMap = new Map();
const colorMap = new Map();
statusMap.set(TenantStatusEnum.NORMAL, 'success'); //正常
statusMap.set(TenantStatusEnum.WAIT_INIT_SCHEMA, 'processing'); // 待初始 表结构
statusMap.set(TenantStatusEnum.WAIT_INIT_DATASOURCE, 'warning'); // 待初始 数据源
statusMap.set(TenantStatusEnum.WITHDRAW, 'default'); // 已撤回
colorMap.set(TenantStatusEnum.WAITING, 'gold'); // 待审核
statusMap.set(TenantStatusEnum.REFUSE, 'error'); // 已拒绝
statusMap.set(TenantStatusEnum.AGREED, 'default'); // 已同意

// 列表页字段
export const columns: BasicColumn[] = [
  {
    title: t('devOperation.tenant.defTenant.name'),
    dataIndex: 'name',
  },
  {
    title: t('devOperation.tenant.defTenant.registerType'),
    dataIndex: ['echoMap', 'registerType'],
    width: 100,
  },
  {
    title: t('devOperation.tenant.defTenant.state'),
    dataIndex: 'state',
    width: 80,
    customRender: ({ record }) => {
      if (!Reflect.has(record, 'pendingStatus')) {
        record.pendingStatus = false;
      }
      return h(Switch, {
        checked: record.state,
        checkedChildren: t('thinglinks.common.enable'),
        unCheckedChildren: t('thinglinks.common.disable'),
        loading: record.pendingStatus,
        onChange(checked: boolean) {
          record.pendingStatus = true;
          const newState = checked;
          updateState(record.id, newState)
            .then(() => {
              record.state = newState;
              createMessage.success(t(`common.tips.editSuccess`));
            })
            .catch(() => {
              createMessage.success(t(`common.tips.editFail`));
            })
            .finally(() => {
              record.pendingStatus = false;
            });
        },
      });
    },
  },
  {
    title: t('devOperation.tenant.defTenant.status'),
    dataIndex: 'status',
    width: 140,
    customRender: ({ record }) => {
      const status = statusMap.get(record.status);
      if (status) {
        return <Badge status={status} text={record.echoMap?.status} />;
      } else {
        const color = colorMap.get(record.status);
        return <Badge color={color} text={record.echoMap?.status} />;
      }
    },
  },
  {
    title: t('devOperation.tenant.defTenant.expirationTime'),
    dataIndex: 'expirationTime',
    width: 180,
    customRender: ({ record }) => {
      // 永久有效 已过期， 还剩2天到期  xxxx
      if (record.expirationTime) {
        if (dateUtil(record.expirationTime).isBefore(Date.now())) {
          return <Tag color="error">{t('common.expired')}</Tag>;
        } else if (dateUtil(record.expirationTime).isBefore(dateUtil().add(30, 'days'))) {
          const duration = dateUtil.duration(dateUtil(record.expirationTime).diff(Date.now()));
          if (duration.days() > 0) {
            return <Tag color="warning">{t('common.expiresInDays', {s: duration.days() + 1})}</Tag>;
          } else {
            return <Tag color="warning">{t('common.expiresInHours', {s: duration.hours()})}</Tag>;
          }
        } else {
          return <Tag color="processing">{record.expirationTime}</Tag>;
        }
      } else {
        return <Tag color="success">{t('common.permanentlyValid')}</Tag>;
      }
    },
  },
  {
    title: t('thinglinks.common.createdTime'),
    dataIndex: 'createdTime',
    sorter: true,
    width: 180,
  },
];

// 列表页搜索表单字段
export const searchFormSchema: FormSchema[] = [
  {
    field: 'name',
    label: t('devOperation.tenant.defTenant.name'),
    component: 'Input',
  },
  {
    field: 'createTimeRange',
    label: t('thinglinks.common.createdTime'),
    component: 'RangePicker',
  },
];

// 新增、编辑、查看页面表单字段
export const editFormSchema = (_: Ref<ActionEnum>): FormSchema[] => {
  return [
    {
      field: 'divider-selects1',
      component: 'Divider',
      label: t('devOperation.tenant.defTenant.basicInfo'),
    },
    {
      field: 'id',
      label: 'ID',
      component: 'Input',
      required: false,
      show: false,
    },
    {
      field: 'name',
      label: t('devOperation.tenant.defTenant.name'),
      component: 'Input',
      colProps: {
        span: 12,
      },
    },
    {
      field: 'abbreviation',
      label: t('devOperation.tenant.defTenant.abbreviation'),
      component: 'Input',
      colProps: {
        span: 12,
      },
    },
    {
      field: 'expirationTime',
      label: t('devOperation.tenant.defTenant.expirationTime'),
      component: 'DatePicker',
      colProps: {
        span: 12,
      },
      componentProps: {
        style: {
          width: '100%',
        },
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        disabledDate: (current) => {
          return current && current < dateUtil().endOf('day');
        },
        showTime: { defaultValue: dateUtil('00:00:00', 'HH:mm:ss') },
      },
    },

    {
      field: 'logos',
      label: t('devOperation.tenant.defTenant.logo'),
      component: 'Upload',
      componentProps: {
        uploadParams: {
          bizType: FileBizTypeEnum.DEF_TENANT_LOGO,
        },
        multiple: false,
        maxNumber: 1,
        accept: ['image/*', '.xlsx', 'docx'],
        isDef: true,
      },
      colProps: {
        span: 12,
      },
    },
    {
      field: 'divider-selects2',
      component: 'Divider',
      label: t('devOperation.tenant.defTenant.contactPhoneInfo'),
    },
    {
      field: 'contactPerson',
      label: t('devOperation.tenant.defTenant.contactPerson'),
      component: 'Input',
      colProps: {
        span: 12,
      },
    },
    {
      field: 'contactPhone',
      label: t('devOperation.tenant.defTenant.contactPhone'),
      component: 'Input',
      colProps: {
        span: 12,
      },
    },
    {
      field: 'contactEmail',
      label: t('devOperation.tenant.defTenant.contactEmail'),
      component: 'Input',
      colProps: {
        span: 12,
      },
    },
    {
      field: 'divider-selects3',
      component: 'Divider',
      label: t('devOperation.tenant.defTenant.areaInfo'),
    },
    {
      field: 'area',
      label: t('devOperation.tenant.defTenant.area'),
      // component: 'ApiCascader',
      // componentProps: {
      //   api: lazyList,
      //   asyncFetchParamKey: 'parentId',
      //   dataField: '',
      //   labelField: 'name',
      //   valueField: 'id',
      //   initFetchParams: {
      //     parentId: '0',
      //   },
      //   isLeaf: (record: Recordable) => {
      //     return !(record.treeGrade < 2);
      //   },
      // },
      component: 'Cascader',
      componentProps: {
        options: cities,
      },
      // defaultValue: [1, 3]
    },
    {
      field: 'address',
      label: t('devOperation.tenant.defTenant.address'),
      component: 'Input',
    },
    {
      field: 'divider-selects4',
      component: 'Divider',
      label: t('devOperation.tenant.defTenant.basicInfo'),
    },
    {
      field: 'creditCode',
      label: t('devOperation.tenant.defTenant.creditCode'),
      component: 'Input',
      colProps: {
        span: 12,
      },
    },
    {
      field: 'createdName',
      label: t('devOperation.tenant.defTenant.createdName'),
      component: 'Input',
      colProps: {
        span: 12,
      },
    },
    {
      field: 'describe',
      label: t('devOperation.tenant.defTenant.describe'),
      component: 'InputTextArea',
    },
  ];
};

// 查看页面表单字段
export const viewFormSchema = (): DescItem[] => {
  return [
    {
      field: 'name',
      label: t('devOperation.tenant.defTenant.name'),
    },
    {
      field: 'abbreviation',
      label: t('devOperation.tenant.defTenant.abbreviation'),
    },
    {
      field: 'expirationTime',
      label: t('devOperation.tenant.defTenant.expirationTime'),
    },

    {
      field: 'logos',
      label: t('devOperation.tenant.defTenant.logo'),
    },

    {
      field: 'contactPerson',
      label: t('devOperation.tenant.defTenant.contactPerson'),
    },

    {
      field: 'contactPhone',
      label: t('devOperation.tenant.defTenant.contactPhone'),
    },
    {
      field: 'contactEmail',
      label: t('devOperation.tenant.defTenant.contactEmail'),
    },
    {
      field: 'divider-selects3',

      label: '地区信息',
    },
    {
      field: 'area',
      label: '地区',
      render: (_curVal, data) => echoMapText(data, 'area'),
    },
    {
      field: 'address',
      label: t('devOperation.tenant.defTenant.address'),
    },

    {
      field: 'creditCode',
      label: t('devOperation.tenant.defTenant.creditCode'),
    },
    {
      field: 'createdName',
      label: t('devOperation.tenant.defTenant.createdName'),
    },
    {
      field: 'describe',
      label: t('devOperation.tenant.defTenant.describe'),
    },
  ];
};

export const viewFormSchema2: DescItem[] = [
  {
    field: 'name',
    label: t('devOperation.tenant.defTenant.name'),
  },
  {
    field: 'abbreviation',
    label: t('devOperation.tenant.defTenant.abbreviation'),
  },
  {
    field: 'expirationTime',
    label: t('devOperation.tenant.defTenant.expirationTime'),
  },

  {
    field: 'logos',
    label: t('devOperation.tenant.defTenant.logo'),
  },

  {
    field: 'contactPerson',
    label: t('devOperation.tenant.defTenant.contactPerson'),
  },

  {
    field: 'contactPhone',
    label: t('devOperation.tenant.defTenant.contactPhone'),
  },
  {
    field: 'contactEmail',
    label: t('devOperation.tenant.defTenant.contactEmail'),
  },
  {
    field: 'area',
    label: '地区',
  },
  {
    field: 'address',
    label: t('devOperation.tenant.defTenant.address'),
  },

  {
    field: 'creditCode',
    label: t('devOperation.tenant.defTenant.creditCode'),
  },
  {
    field: 'createdName',
    label: t('devOperation.tenant.defTenant.createdName'),
  },
  {
    field: 'describe',
    label: t('devOperation.tenant.defTenant.describe'),
  },
];

// 额外的新增、编辑表单验证规则
export const customFormSchemaRules = (_: Ref<ActionEnum>): Partial<FormSchemaExt>[] => {
  return [
    // {
    //   field: 'code',
    //   type: RuleType.append,
    //   rules: [
    //     {
    //       trigger: ['change', 'blur'],
    //       async validator(_, value) {
    //         if (unref(type) !== ActionEnum.ADD) {
    //           return Promise.resolve();
    //         }
    //         if (value) {
    //           const res = await check(value);
    //           if (res) {
    //             return Promise.reject('企业编码已经存在');
    //           } else {
    //             return Promise.resolve();
    //           }
    //         } else {
    //           return Promise.resolve();
    //         }
    //       },
    //     },
    //   ],
    // },
    {
      field: 'logos',
      rules: [
        {
          validator(_, value) {
            if (value) {
              if (value.length > 1) {
                return Promise.reject('只能上传一个文件');
              } else {
                return Promise.resolve();
              }
            } else {
              return Promise.resolve();
            }
          },
        },
      ],
      type: RuleType.cover,
    },
  ];
};

// 列表页字段
export const userColumns: BasicColumn[] = [
  {
    title: t('devOperation.tenant.defUser.username'),
    dataIndex: 'username',
    width: 180,
  },
  {
    title: t('devOperation.tenant.defUser.nickName'),
    dataIndex: 'nickName',
    width: 180,
  },
  {
    title: t('devOperation.tenant.defUser.email'),
    dataIndex: 'email',
    // width: 180,
  },
  {
    title: t('devOperation.tenant.defUser.mobile'),
    dataIndex: 'mobile',
    // width: 180,
  },
  {
    title: t('devOperation.tenant.defUser.sex'),
    dataIndex: ['echoMap', 'sex'],
    width: 80,
  },
  {
    title: t('devOperation.tenant.defUser.state'),
    dataIndex: 'state',
    width: 80,
    filters: [...stateFilters()],
    filterMultiple: false,
    format: (text) => {
      return text ? t('thinglinks.common.enable') : t('thinglinks.common.disable');
    },
  },
  {
    title: t('thinglinks.common.createdTime'),
    dataIndex: 'createdTime',
    sorter: true,
    width: 180,
  },
];

// 列表页搜索表单字段
export const userSearchFormSchema: FormSchema[] = [
  {
    label: t('devOperation.tenant.defUser.username'),
    field: 'username',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    label: t('devOperation.tenant.defUser.nickName'),
    field: 'nickName',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    label: t('devOperation.tenant.defUser.email'),
    field: 'email',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    label: t('devOperation.tenant.defUser.mobile'),
    field: 'mobile',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    label: t('devOperation.tenant.defUser.idCard'),
    field: 'idCard',
    component: 'Input',
    colProps: { span: 6 },
  },
];

// 审核页面
export const toExamineFormSchema = (): FormSchema[] => {
  return [
    {
      field: 'id',
      label: 'ID',
      component: 'Input',
      required: false,
      show: false,
    },
    {
      field: 'createdName',
      label: 'ID',
      component: 'Input',
      show: false,
    },
    {
      field: 'createdBy',
      label: 'ID',
      component: 'Input',
      show: false,
    },
    {
      field: 'status',
      label: '审核状态',
      component: 'RadioGroup',
      componentProps: {
        options: [
          {
            label: '同意',
            value: TenantStatusEnum.AGREED,
          },
          {
            label: '拒绝',
            value: TenantStatusEnum.REFUSE,
          },
        ],
      },
      rules: [{ required: true }],
    },
    {
      field: 'reviewComments',
      label: '审批意见',
      component: 'InputTextArea',
    },
  ];
};
