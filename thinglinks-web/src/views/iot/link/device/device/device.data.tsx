import { Ref } from 'vue';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { ActionEnum } from '/@/enums/commonEnum';
import { FormSchemaExt, RuleType } from '/@/api/thinglinks/common/formValidateService';
import { dictComponentProps } from '/@/utils/thinglinks/common';
import { echoMapText } from '/@/utils/echo';
import { DictEnum } from '/@/enums/commonEnum';
import citiesGd from '/@/utils/thinglinks/citiesGd.json';
import { deviceQuery } from '../../../../../api/iot/link/device/device';
import { useDict } from '/@/components/Dict';
import { DescItem } from '/@/components/Description/index';
import { InfoCircleOutlined } from '@ant-design/icons-vue';
import CopyableText from '/@/components/CopyableText';
import { SnapshotIdTag } from '/@/components/iot';
import type { CardField } from '/@/components/BusinessCardList';
import {
  DeviceAuthMode,
  DeviceNodeType,
} from '/@/enums/link/device';
import DeviceCertInfo from '/@/components/Form/src/components/cacert/DeviceCertInfo.vue';

const { getDictLabel } = useDict();
const { t } = useI18n();

/**
 * BusinessCardList 卡片字段配置 ── Flexy 风格,与产品列表卡片同款。
 *
 * 名称(nameField=deviceName)+ 在线徽章(connectStatus)+ 节点类型徽章(nodeType)
 * 由 BusinessCardList 单独渲染,本配置只覆盖信息行。
 *
 * 注意:BusinessCardList.getNestedValue 支持点路径(如 `productResultVO.productName`)
 * 反查嵌套对象,无需在前端再 reshape 数据。
 */
/**
 * 卡片字段配置 ── 只用 Device 表自有字段,不引产品维度。
 *
 * 设计原则:
 *   1. 设备标识独占一行(雪花 ID 长),其他字段两两并排
 *   2. 列表分页本身走单表(superManager.getPage),不做 product 表 enrichment,
 *      避免大数据量场景为每页 N 条设备额外打 N 次产品查询
 *   3. productName / productType 等需要"关联产品"的信息全部下沉到设备详情页
 *      (详情接口 enrichment 一次性补完,代价可控)
 *   4. 列表卡片只展示运维最常用的"是不是真的活着"信号:
 *      lastHeartbeatTime + deviceStatus + 在线状态(右下角徽标,BusinessCardList 渲染)
 *   5. 右上 nodeType 徽章(BusinessCardList 渲染)区分普通/网关/子设备
 */
export const cardFields = (): CardField[] => [
  {
    label: t('iot.link.device.device.deviceIdentification'),
    field: 'deviceIdentification',
    span: 24,
  },
  {
    // 最后心跳时间:判断"是否真的还在通信"的核心运维指标,与在线状态徽标互为佐证
    label: t('iot.link.device.device.lastHeartbeatTime'),
    field: 'lastHeartbeatTime',
    span: 24,
  },
  {
    label: t('iot.link.device.device.deviceStatus'),
    field: 'deviceStatus',
    span: 12,
    dictType: 'LINK_DEVICE_STATUS',
  },
  {
    label: t('iot.link.device.device.appId'),
    field: 'appId',
    span: 12,
  },
  {
    label: t('iot.link.device.device.fwVersion'),
    field: 'fwVersion',
    span: 12,
  },
  {
    label: t('iot.link.device.device.swVersion'),
    field: 'swVersion',
    span: 12,
  },
];
// 列表页字段
export const columns = (): BasicColumn[] => {
  return [
    {
      title: t('iot.link.device.device.appId'),
      dataIndex: 'appId',
    },
    {
      title: t('iot.link.device.device.deviceIdentification'),
      dataIndex: 'deviceIdentification',
    },
    {
      title: t('iot.link.device.device.connectStatus'),
      dataIndex: ['connectStatus'],
      customRender: ({ record }) => {
        return getDictLabel('LINK_DEVICE_CONNECT_STATUS', record.connectStatus, '');
      },
    },
    {
      title: t('iot.link.device.device.clientId'),
      dataIndex: 'clientId',
    },
    {
      title: t('iot.link.device.device.deviceStatus'),
      dataIndex: ['echoMap', 'deviceStatus'],
      customRender: ({ record }) => {
        ``;
        return getDictLabel('LINK_DEVICE_STATUS', record.deviceStatus, '');
      },
    },
    {
      title: t('iot.link.device.device.createdTime'),
      dataIndex: 'createdTime',
      sorter: true,
      width: 180,
    },
    {
      title: t('iot.link.device.device.remark'),
      dataIndex: 'remark',
    },
  ];
};

export const searchFormSchema = (): FormSchema[] => {
  return [
    {
      label: t('iot.link.device.device.appId'),
      field: 'appId',
      colProps: { span: 6 },
      component: 'ApiSelect',
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.LINK_APPLICATION_SCENARIO),
      },
    },
    {
      label: t('iot.link.device.device.deviceName'),
      field: 'deviceName',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.link.device.device.deviceIdentification'),
      field: 'deviceIdentification',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.link.device.device.clientId'),
      field: 'clientId',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.link.product.product.productIdentification'),
      field: 'productIdentification',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.link.device.device.connectStatus'),
      field: 'connectStatus',
      colProps: { span: 6 },
      component: 'ApiSelect',
      show: true,
      componentProps: {
        ...dictComponentProps(DictEnum.LINK_DEVICE_CONNECT_STATUS),
      },
    },
    {
      label: t('iot.link.device.device.deviceStatus'),
      field: 'deviceStatus',
      colProps: { span: 6 },
      component: 'ApiSelect',
      show: true,
      componentProps: {
        ...dictComponentProps(DictEnum.LINK_DEVICE_STATUS),
      },
    },
    {
      label: t('iot.link.device.device.nodeType'),
      field: 'nodeType',
      colProps: { span: 6 },
      component: 'ApiSelect',
      show: true,
      componentProps: {
        ...dictComponentProps(DictEnum.LINK_DEVICE_NODE_TYPE),
      },
    },
    {
      field: 'createTimeRange',
      label: t('iot.link.device.device.createdTime'),
      component: 'RangePicker',
      colProps: { span: 6 },
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
      label: '',
      labelWidth: 0,
      colProps: {
        span: 22,
      },
      field: 'd',
      component: 'FormTitle',
      componentProps: {
        title: t('iot.link.engine.linkage.basicInfo'),
      },
    },
    {
      label: t('iot.link.device.device.appId'),
      field: 'appId',
      component: 'ApiSelect',
      rules: [{ required: true }],
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.LINK_APPLICATION_SCENARIO),
      },
    },
    {
      label: t('iot.link.device.device.deviceName'),
      field: 'deviceName',
      component: 'Input',
      show: true,
      rules: [{ required: true }],
      componentProps: {
        disabled: false,
        // placeholder: `请输入${t('iot.link.device.device.deviceName')}`,
      },
    },
    {
      label: t('iot.link.device.device.nodeType'),
      field: 'nodeType',
      component: 'ApiSelect',
      show: true,
      rules: [{ required: true }],
      componentProps: {
        ...dictComponentProps(DictEnum.LINK_DEVICE_NODE_TYPE),
      },
    },
    // {
    //   label: t('iot.link.product.product.protocolType'),
    //   field: 'protocolType',
    //   component: 'ApiSelect',
    //   show: true,
    //   rules: [{ required: true }],
    //   componentProps: {
    //     ...dictComponentProps(DictEnum.LINK_PRODUCT_PROTOCOL_TYPE),
    //   },
    // },
    {
      label: t('iot.link.device.device.authMode'),
      field: 'authMode',
      component: 'ApiSelect',
      defaultValue: '0',
      show: true,
      rules: [{ required: true }],
      componentProps: {
        disabled: false,
        allowClear: false,
        // placeholder: `请选择${t('iot.link.device.device.authMode')}`,
        ...dictComponentProps(DictEnum.LINK_DEVICE_AUTH_MODE),
      },
      helpMessage: [
        t('iot.link.device.device.authModeMsg[0]'),
        t('iot.link.device.device.authModeMsg[1]'),
        t('iot.link.device.device.authModeMsg[2]'),
        t('iot.link.device.device.authModeMsg[3]'),
      ],
    },
    {
      label: t('iot.link.device.device.userName'),
      field: 'userName',
      component: 'Input',
      // show: ({ values }) => {
      //   return values.authMode == '0';
      // },
      rules: [{ required: true }],
      componentProps: {
        disabled: false,
        // placeholder: `请输入${t('iot.link.device.device.userName')}`,
      },
    },
    {
      label: t('iot.link.device.device.password'),
      field: 'password',
      component: 'StrengthMeter',
      // show: ({ values }) => {
      //   return values.authMode == '0';
      // },
      rules: [
        { required: true, message: '请输入密码' },
        {
          pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&.])[A-Za-z\d@$!%*?&.]{6,20}$/,
          message: '密码必须包含大小写字母、数字和特殊字符，且长度在 6 到 20 个字符之间',
        },
      ],
      componentProps: {
        disabled: false,
        autocomplete: 'off',
        // placeholder: `请输入${t('iot.link.device.device.password')}`,
      },
    },
    {
      label: t('iot.link.device.device.selectCertSerialNumber'),
      field: 'certSerialNumber',
      component: 'ApiCacertSelectNodeCard',
      show: ({ values }) => {
        return values.authMode == DeviceAuthMode.SSL_TLS_CERTIFICATE;
      },
      rules: [{ required: true }],
      helpMessage: [t('iot.link.device.device.cert.tipSelectIssued')],
      componentProps: ({ formModel, formActionType }) => {
        return {
          // 当前已选中的证书序列号(用于回显)
          value: formModel.certSerialNumber,
          onSelect: (record) => {
            const { validateFields } = formActionType;
            formModel.certSerialNumber = record?.serialNumber;
            validateFields(['certSerialNumber']);
          },
        };
      },
    },
    {
      label: t('iot.link.product.product.productName'),
      field: 'productName',
      component: 'Input',
      show: false,
    },
    {
      // 产品标识 ── 走通用 IotProductPicker(已在 ACL 等多处复用)
      // Flexy 卡片化选择 + 协议徽标 + 启用状态 + 应用场景,统一项目内"选产品"交互
      // slot 形式接入,具体渲染由 Edit.vue 的 #productIdentification 模板提供
      // colProps span:22 独占一行 ── IotProductPicker trigger 内容(产品名标签 + 删除按钮 + 选择按钮)
      // 较宽,挤在 11 列里会与右侧字段重叠;独占一行后视觉清爽,也方便后续切产品时下方"绑定版本号"
      // 选项联动展开
      label: t('iot.link.product.product.productIdentification'),
      field: 'productIdentification',
      component: 'Input',
      slot: 'productIdentification',
      rules: [{ required: true }],
      colProps: { span: 22 },
      show: ({ values }) => {
        return values.nodeType;
      },
    },
    {
      // 设备绑定版本 ── 必填;选完产品后自动默认 activeVersionNo,colProps 22 独占一行防版本号被压扁
      label: t('iot.link.device.device.boundVersionShort'),
      field: 'boundProductVersionNo',
      component: 'Input',
      slot: 'boundProductVersionNo',
      rules: [{ required: true }],
      colProps: { span: 22 },
      show: ({ values }) => !!values.productIdentification,
      helpMessage: [t('iot.link.device.device.boundProductVersionTip')],
    },
    {
      label: t('iot.link.device.device.deviceStatus'),
      field: 'deviceStatus',
      component: 'ApiSelect',
      show: true,
      rules: [{ required: true }],
      componentProps: {
        ...dictComponentProps(DictEnum.LINK_DEVICE_STATUS),
      },
    },
    {
      label: t('iot.link.device.device.connector'),
      field: 'connector',
      component: 'ApiSelect',
      show: true,
      rules: [{ required: true }],
      componentProps: {
        disabled: false,
        // placeholder: `请输入${t('iot.link.device.device.connector')}`,
        ...dictComponentProps(DictEnum.LINK_DEVICE_CONNECTOR),
      },
    },

    {
      label: '',
      labelWidth: 0,
      colProps: {
        span: 23,
      },
      field: 'dd',
      component: 'FormTitle',
      componentProps: {
        title: t('iot.link.engine.linkage.geoInfo'),
      },
    },
    // {
    //   label: '地理信息',
    //   colProps: {
    //     span: 23,
    //   },
    //   field: 'dd',
    //   component: 'Divider',
    //   componentProps: {
    //     type: 'vertical',
    //     style: {
    //      height: '20px',
    //      lineHeight: '20px',
    //      borderLeft: '3px solid #1A66FF',
    //      fontSize: '16px',
    //      color: '#2e3033',
    //      fontWeight: 600,
    //      margin: 0,
    //     }
    //   },
    // },
    {
      field: 'area',
      label: t('iot.link.device.device.area'),
      colProps: {
        span: 22,
      },
      rules: [{ required: true }],
      component: 'Cascader',
      componentProps: ({ formModel, formActionType }) => {
        return {
          options: citiesGd,
          disabled: true,
          onChange: (e) => {
            if (e) {
              const province = citiesGd.find((item) => {
                return item.value === e[0];
              });
              const city = province?.children.find((item) => {
                return item.value === e[1];
              });
              const district = city?.children.find((item) => {
                return item.value === e[2];
              });
              formModel.address =
                (province?.label || '') + (city?.label || '') + (district?.label || '');
              const { updateSchema } = formActionType;
              updateSchema({
                field: 'map',
                componentProps: {
                  address: formModel.address,
                  isInput: true, // 触发地图组件的输入事件，让地图组件重新渲染zu、
                },
              });
            } else {
              formModel.address = '';
            }
          },
        };
      },
      dynamicRules: ({ values }) => {
        return [
          {
            required: values.map ? true : false,
          },
        ];
      },
    },
    {
      label: t('iot.link.device.device.address'),
      field: 'address',
      component: 'InputTextArea',
      show: true,
      rules: [{ required: true }],
      colProps: {
        span: 22,
      },
      componentProps: ({ formModel, formActionType }) => {
        return {
          disabled: true,
          // placeholder: `请输入${t('iot.link.device.device.address')}`,
          onBlur: (e) => {
            const { updateSchema } = formActionType;
            updateSchema({
              field: 'map',
              componentProps: {
                address: e.target.value,
                isInput: true, // 触发地图组件的输入事件，让地图组件重新渲染zu
              },
            });
          },
        };
      },
      dynamicRules: ({ values }) => {
        return [
          {
            required: values.map ? true : false,
          },
        ];
      },
    },
    {
      label: t('iot.link.device.device.map'),
      colProps: {
        span: 22,
      },
      field: 'map',
      rules: [{ required: true }],
      component: 'AMap',
      componentProps: ({ formModel, formActionType }) => {
        // console.log(formModel, '地图渲染');
        return {
          address: formModel.address,
          latitude: formModel.latitude,
          longitude: formModel.longitude,
          onChange: (e) => {
            console.log(111111, e);
          },
          onUpdateMap: (e) => {
            console.log(e);
            formModel.map = e;
          },
          onAddressClick: (e, addressComponent, formattedAddress) => {
            const { updateSchema } = formActionType;
            console.log(e);
            console.log(addressComponent);
            console.log(formattedAddress);
            const province = citiesGd.find((item) => {
              return item.label.replace(/\s/g, '') === addressComponent.province;
            });
            const city =
              province?.children.find((item) => {
                return item.label.replace(/\s/g, '') === addressComponent.city;
              }) || province?.children[0];
            const district =
              city?.children.find((item) => {
                return item.label.replace(/\s/g, '') === addressComponent.district;
              }) || city?.children[0];
            formModel.area = [province?.value, city?.value, district?.value];
            formModel.address = formattedAddress;
            formModel.map = e;
          },
        };
      },
      dynamicRules: ({ values }) => {
        return [
          {
            required: values.map ? true : false,
          },
        ];
      },
    },
    // {
    //   label: '其他信息',
    //   colProps: {
    //     span: 23,
    //   },
    //   field: 'dd',
    //   component: 'Divider',
    //   componentProps: {
    //     type: 'vertical',
    //     style: {
    //      height: '20px',
    //      lineHeight: '20px',
    //      borderLeft: '3px solid #1A66FF',
    //      fontSize: '16px',
    //      color: '#2e3033',
    //      fontWeight: 600,
    //      margin: 0,
    //     }
    //   },
    // },
    {
      label: '',
      labelWidth: 0,
      colProps: {
        span: 23,
      },
      field: 'dd',
      component: 'FormTitle',
      componentProps: {
        title: t('iot.link.device.device.otherInfo'),
      },
    },
    {
      label: t('iot.link.device.device.encryptMethod'),
      field: 'encryptMethod',
      component: 'ApiSelect',
      defaultValue: '0',
      show: true,
      componentProps: {
        disabled: false,
        allowClear: false,
        // placeholder: `请选择${t('iot.link.device.device.encryptMethod')}`,
        ...dictComponentProps(DictEnum.LINK_DEVICE_ENCRYPT_METHOD),
      },
    },
    {
      label: t('iot.link.device.device.signKey'),
      field: 'signKey',
      component: 'Input',
      show: true,
      rules: [{ required: true }],
      componentProps: {
        disabled: false,
        // placeholder: `请输入${t('iot.link.device.device.signKey')}`,
      },
      colSlot: 'signKey',
      // renderComponentContent: () => {
      //   const [, { setFieldsValue }] = useForm(); // 解构出 setFieldsValue 方法
      //   return {
      //     addonAfter: () => (
      //       <ReloadOutlined
      //         onClick={() => {
      //           // 弹窗提示是否随机生成
      //           Modal.confirm({
      //             title: '提示',
      //             content: '是否随机生成一个 16 位字符？',
      //             onOk() {
      //               console.log(setFieldsValue);
      //               // 生成随机字符串
      //               const randomString = generateRandomString();
      //               // 更新表单的值
      //               setFieldsValue({ signKey: randomString });
      //             },
      //             onCancel() {},
      //           });
      //         }}
      //       />
      //     ),
      //   };
      // },
    },

    {
      label: t('iot.link.device.device.encryptKey'),
      field: 'encryptKey',
      component: 'Input',
      show: ({ values }) => {
        return ['1', '2'].indexOf(values.encryptMethod) != -1;
      },
      rules: [{ required: true }],
      componentProps: {
        disabled: false,
        // placeholder: `请输入${t('iot.link.device.device.encryptKey')}`,
      },
      colSlot: 'encryptKey',
    },
    {
      label: t('iot.link.device.device.encryptVector'),
      field: 'encryptVector',
      component: 'Input',
      show: ({ values }) => {
        return ['1', '2'].indexOf(values.encryptMethod) != -1;
      },
      rules: [{ required: true }],
      componentProps: {
        disabled: false,
        // placeholder: `请输入${t('iot.link.device.device.encryptVector')}`,
      },
      colSlot: 'encryptVector',
    },
    {
      label: 'gatewayName',
      field: 'gatewayName',
      component: 'Input',
      show: false,
    },
    {
      label: t('iot.link.device.device.gateway'),
      field: 'gatewayId',
      component: 'ApiSelectTable',
      show: ({ values }) => {
        return values.nodeType == DeviceNodeType.SUB_DEVICE;
      },
      rules: [{ required: true }],
      componentProps: ({ formModel, formActionType }) => {
        return {
          type: formModel.gatewayName,
          typeName: 'deviceName',
          api: deviceQuery,
          params: {
            nodeType: 1,
          },
          columns: [
            {
              title: 'ID',
              dataIndex: 'id',
              width: 180,
            },
            {
              title: t('iot.link.device.device.deviceIdentification'),
              dataIndex: 'deviceIdentification',
              width: 180,
            },
            {
              title: t('iot.link.device.device.deviceName'),
              dataIndex: 'deviceName',
            },
            {
              title: t('iot.link.device.device.nodeType'),
              dataIndex: 'model',
              width: 180,
            },
          ],
          title: '选择网关设备',
          subTitle: 'device',
          onSelect: (e) => {
            const { validate } = formActionType;
            formModel.gatewayName = e.deviceName;
            formModel.gatewayId = e.deviceIdentification;
            validate(['gatewayId']);
          },
        };
      },
    },

    {
      // SDK / 固件 / 软件 三个版本号收成一排三联(span 7),避免设备标签抽成整行后剩奇数个半宽字段留孤儿。
      label: t('iot.link.device.device.deviceSdkVersion'),
      field: 'deviceSdkVersion',
      component: 'Input',
      defaultValue: 'v1',
      show: true,
      rules: [{ required: true }],
      colProps: { span: 7 },
      componentProps: {
        disabled: false,
        // placeholder: `请输入${t('iot.link.device.device.deviceSdkVersion')}`,
      },
    },
    {
      label: t('iot.link.device.device.fwVersion'),
      field: 'fwVersion',
      component: 'Input',
      show: true,
      rules: [{ required: true }],
      colProps: { span: 7 },
      componentProps: {
        disabled: false,
        // placeholder: `请输入${t('iot.link.device.device.fwVersion')}`,
      },
    },
    {
      label: t('iot.link.device.device.swVersion'),
      field: 'swVersion',
      component: 'Input',
      show: true,
      rules: [{ required: true }],
      colProps: { span: 7 },
      componentProps: {
        disabled: false,
        // placeholder: `请输入${t('iot.link.device.device.swVersion')}`,
      },
    },
    {
      // 设备标签 ── 多标签输入(对齐 ACL IP 白名单的交互与色调),独占一行放在描述正上方。
      // 渲染见 Edit.vue #deviceTags;后端 deviceTags 为逗号拼接字符串,slot 内做 数组<->字符串 转换。
      label: t('iot.link.device.device.deviceTags'),
      field: 'deviceTags',
      component: 'Input',
      slot: 'deviceTags',
      show: true,
      rules: [{ required: false }],
      colProps: { span: 22 },
    },
    {
      // 设备描述 ── 独占一行(span 22),避免与右侧字段配对时留半行空白显得突兀。
      label: t('iot.link.device.device.description'),
      field: 'description',
      component: 'Input',
      show: true,
      rules: [{ required: false }],
      colProps: { span: 22 },
      componentProps: {
        disabled: false,
      },
    },
    {
      label: t('iot.link.device.device.remark'),
      field: 'remark',
      component: 'InputTextArea',
      show: true,
      rules: [{ required: false }],
      colProps: {
        span: 22,
      },
      componentProps: {
        disabled: false,
        // placeholder: `${t('common.inputText')}` + `${t('iot.link.device.device.remark')}`,
      },
    },
  ];
};

// 前端自定义表单验证规则
export const customFormSchemaRules = (_): Partial<FormSchemaExt>[] => {
  return [
    {
      field: 'name',
      type: RuleType.append,
      rules: [{ trigger: 'blur', required: true }],
    },
    {
      field: 'key',
      type: RuleType.append,
      rules: [{ trigger: 'blur', required: true }],
    },
  ];
};

// 详情页字段
/**
 * 详情页 Description 公用 labelStyle ── 与 product/detail 同款,统一视觉。
 */
const detailLabelStyle = {
  width: '140px',
  'font-weight': '600',
  'font-size': '14px',
};

/**
 * 设备身份 ── deviceIdentification / clientId / nodeType / userName。
 *
 * 设计意图:首屏左侧第一卡片,回答"这台设备是谁、怎么连进来的",
 * 设备业务标识 + 客户端 ID 用 CopyableText 方便用户拷贝去查日志。
 */
export function detailIdentitySchema(): DescItem[] {
  const labelStyle = detailLabelStyle;
  return [
    {
      field: 'deviceIdentification',
      label: t('iot.link.device.device.deviceIdentification'),
      labelStyle,
      render: (curVal) => <CopyableText text={curVal || ''} />,
    },
    {
      field: 'clientId',
      label: t('iot.link.device.device.clientId'),
      labelStyle,
      render: (curVal) => <CopyableText text={curVal || ''} />,
    },
    {
      field: 'nodeType',
      label: t('iot.link.device.device.nodeType'),
      labelStyle,
      render: (curVal) => getDictLabel(DictEnum.LINK_DEVICE_NODE_TYPE, curVal, ''),
    },
    {
      field: 'userName',
      label: t('iot.link.device.device.userName'),
      labelStyle,
      render: (curVal) => <CopyableText text={curVal || ''} />,
    },
  ];
}

/**
 * 产品与版本绑定 ── 详情页的"灰度可观测核心":
 * productIdentification(设备所属产品的业务键)+ boundProductVersionNo(设备实际跑在哪一版物模型上),
 * 这两个字段对灰度排查、版本回滚分析至关重要,所以单独拎成一个卡片置顶展示。
 */
export function detailProductBindingSchema(): DescItem[] {
  const labelStyle = detailLabelStyle;
  return [
    {
      field: 'productResultVO.productName',
      label: t('iot.link.product.product.productName'),
      labelStyle,
    },
    {
      // 产品标识(产品维度业务键)── 用 CopyableText 是因为这是排查灰度 / 数据流的入口键
      field: 'productResultVO.productIdentification',
      label: t('iot.link.product.product.productIdentification'),
      labelStyle,
      render: (curVal) => <CopyableText text={curVal || ''} />,
    },
    {
      field: 'productResultVO.productType',
      label: t('iot.link.product.product.productType'),
      labelStyle,
      render: (curVal) => getDictLabel(DictEnum.LINK_PRODUCT_TYPE, curVal, ''),
    },
    {
      // 产品当前生效版本序号(产品维度,所有设备共享):蓝色 chip
      field: 'productResultVO.activeVersionNo',
      label: t('iot.link.product.product.activeVersionNo'),
      labelStyle,
      render: (curVal) => <SnapshotIdTag value={curVal} />,
    },
    {
      // 设备绑定的产品版本序号(设备维度,灰度路由依据)── 紫色 chip 区分维度
      // 空值时:展示友好提示而非纯 "-",告知"首次数据上报时按产品当前版本回填"
      // (mqs DeviceDataProcessingServiceImpl asyncBackfillBoundProductVersion 兜底逻辑)
      field: 'boundProductVersionNo',
      label: t('iot.link.device.device.boundProductVersionNo'),
      labelStyle,
      render: (curVal) =>
        curVal ? (
          <SnapshotIdTag value={curVal} color="purple" />
        ) : (
          <span style="color: var(--text-tertiary, #97a1b0); font-size: 12px; display: inline-flex; align-items: center; gap: 4px;">
            <InfoCircleOutlined />
            {t('iot.link.device.device.boundProductVersionEmpty')}
          </span>
        ),
    },
  ];
}

/**
 * 协议与认证 ── 协议 / 数据格式 / 认证模式 / 证书 / 加解密。
 *
 * 设计意图:与"产品与版本"分开是因为这里偏运维 / 安全视角,
 * 单独成卡片避免和业务字段混在一起影响阅读。
 */
export function detailProtocolAuthSchema(): DescItem[] {
  const labelStyle = detailLabelStyle;
  return [
    {
      field: 'productResultVO.protocolType',
      label: t('iot.link.product.product.protocolType'),
      labelStyle,
    },
    {
      field: 'productResultVO.dataFormat',
      label: t('iot.link.product.product.dataFormat'),
      labelStyle,
    },
    {
      field: 'authMode',
      label: t('iot.link.device.device.authMode'),
      labelStyle,
      render: (curVal) => getDictLabel(DictEnum.LINK_DEVICE_AUTH_MODE, curVal, ''),
    },
    {
      field: 'certSerialNumber',
      label: t('iot.link.device.device.certSerialNumber'),
      labelStyle,
      // 仅 SSL 模式下展示富文本(certName + 状态 chip + 算法 chip + 剩余天数 chip)
      show: (data) =>
        String(data?.authMode ?? '') === String(DeviceAuthMode.SSL_TLS_CERTIFICATE),
      render: (curVal) => <DeviceCertInfo serialNumber={curVal} />,
    },
    {
      field: 'encryptMethod',
      label: t('iot.link.device.device.encryptMethod'),
      labelStyle,
      render: (curVal) => getDictLabel(DictEnum.LINK_DEVICE_ENCRYPT_METHOD, curVal, ''),
    },
    {
      field: 'password',
      label: t('iot.link.device.device.password'),
      labelStyle,
      desensitize: true,
    },
    {
      field: 'encryptKey',
      label: t('iot.link.device.device.encryptKey'),
      labelStyle,
      desensitize: true,
    },
    {
      field: 'encryptVector',
      label: t('iot.link.device.device.encryptVector'),
      labelStyle,
      desensitize: true,
    },
    {
      field: 'signKey',
      label: t('iot.link.device.device.signKey'),
      labelStyle,
      desensitize: true,
    },
  ];
}

/**
 * 厂商 + 固件 + 时间线 ── 偏元信息,放最下方折叠区。
 */
export function detailManufacturerSchema(): DescItem[] {
  const labelStyle = detailLabelStyle;
  return [
    {
      field: 'productResultVO.manufacturerId',
      label: t('iot.link.product.product.manufacturerId'),
      labelStyle,
    },
    {
      field: 'productResultVO.manufacturerName',
      label: t('iot.link.product.product.manufacturerName'),
      labelStyle,
    },
    {
      field: 'productResultVO.model',
      label: t('iot.link.product.product.model'),
      labelStyle,
    },
    {
      field: 'swVersion',
      label: t('iot.link.device.device.swVersion'),
      labelStyle,
    },
    {
      field: 'fwVersion',
      label: t('iot.link.device.device.fwVersion'),
      labelStyle,
    },
    {
      field: 'deviceSdkVersion',
      label: t('iot.link.device.device.deviceSdkVersion'),
      labelStyle,
    },
  ];
}

/** 时间线:心跳 + 审计字段,排查问题时定位时间窗。 */
export function detailTimelineSchema(): DescItem[] {
  const labelStyle = detailLabelStyle;
  return [
    {
      field: 'lastHeartbeatTime',
      label: t('iot.link.device.device.lastHeartbeatTime'),
      labelStyle,
    },
    {
      field: 'createdBy',
      label: t('iot.link.device.device.createdBy'),
      labelStyle,
      render: (_curVal, data) => echoMapText(data, 'createdBy'),
    },
    {
      field: 'createdTime',
      label: t('iot.link.device.device.createdTime'),
      labelStyle,
    },
    {
      field: 'updatedBy',
      label: t('iot.link.device.device.updatedBy'),
      labelStyle,
      render: (_curVal, data) => echoMapText(data, 'updatedBy'),
    },
    {
      field: 'updatedTime',
      label: t('iot.link.device.device.updatedTime'),
      labelStyle,
    },
  ];
}
