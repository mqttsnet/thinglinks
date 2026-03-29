import { useI18n } from '/@/hooks/web/useI18n';

const { t } = useI18n();

// 分组详情上方详细信息字段
export const getGroupBaseInfoField = (): FormSchema[] => {
  return [
    {
      field: 'groupName',
      label: t('iot.link.group.deviceGroup.groupName'),
    },
    {
      field: 'id',
      label: t('iot.link.group.deviceGroup.deviceGroupDetail.groupId'),
    },
    {
      field: 'type',
      label: t('iot.link.group.deviceGroup.type'),
      render: (val) => {
        // TODO 国际化
        return val === 0 ? t('iot.link.group.deviceGroup.typeOptions.normalGroup') : val;
      },
    },
    {
      field: 'state',
      label: t('iot.link.group.deviceGroup.state'),
      render: (val) => {
        return val ? t('common.yes') : t('common.no');
      },
    },
    {
      field: 'description',
      label: t('iot.link.group.deviceGroup.description'),
    },
    {
      field: 'parentId',
      label: t('iot.link.group.deviceGroup.editDeviceGroup.parentNode'),
    },
    {
      field: 'sortValue',
      label: t('iot.link.group.deviceGroup.deviceGroupDetail.sortValueNum'),
    },
    {
      field: 'remark',
      label: t('iot.link.group.deviceGroup.remark'),
    },
  ];
};

// 分组详情下方设备列表信息字段
export const getGroupDeviceBaseTableColumn = (): FormSchema[] => {
  const defaultColumns = [
    {
      title: t('iot.link.device.device.deviceName'),
      width: 150,
      dataIndex: 'deviceName',
    },
    {
      title: t('iot.link.device.device.deviceStatus'),
      width: 150,
      dataIndex: 'deviceStatus',
      customRender: ({ record }) => {
        const text =
          record.deviceStatus === 0
            ? t('iot.link.device.device.deviceAllStatus.notActivat')
            : record.deviceStatus === 1
            ? t('iot.link.device.device.deviceAllStatus.active')
            : t('iot.link.device.device.deviceAllStatus.lock');
        return <span>{text}</span>;
      },
    },
    {
      title: t('iot.link.device.device.product'),
      width: 150,
      dataIndex: 'productIdentification',
    },
    {
      title: t('iot.link.device.device.deviceIdentification'),
      width: 150,
      dataIndex: 'deviceIdentification',
    },
    {
      title: t('iot.link.device.device.remark'),
      width: 150,
      dataIndex: 'remark',
      edit: true,
      editRow: true,
      editComponent: 'Input',
      editComponentProps: {
        placeholder: t('common.inputText'),
      },
      editRule: false,
    },
  ];
  return defaultColumns;
};
