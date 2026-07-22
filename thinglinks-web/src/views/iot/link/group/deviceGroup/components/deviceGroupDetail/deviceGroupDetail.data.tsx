import { Tag, Tooltip } from 'ant-design-vue';
import { useI18n } from '/@/hooks/web/useI18n';

const { t } = useI18n();

/**
 * 判断关联记录里的设备是否已被删除（孤儿行）。
 * 后端接口返回的关联表中，若设备已被删除会出现：deviceId 为空 且 deviceName 为空 的情况，
 * 此时不能再让用户点"查看 / 编辑"跳转设备详情，否则会触发 "Missing required param 'id'"。
 */
export const isOrphanDeviceRow = (record: Recordable): boolean => {
  if (!record) return true;
  const noId = !record.deviceId;
  const noName = !record.deviceName;
  // deviceStatus 在已删除场景下后端不会返回 0/1/2，常见为 null/undefined
  const noStatus = record.deviceStatus === null || record.deviceStatus === undefined;
  return noId && noName && noStatus;
};

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
        // 孤儿行优先：设备已被删除，此关联是历史脏数据（新事件链路会自动清理，此处兼容存量）
        if (isOrphanDeviceRow(record)) {
          return (
            <Tooltip title={t('iot.link.device.device.deletedOrphanTip')}>
              <Tag color="error">{t('iot.link.device.device.deviceAllStatus.deleted')}</Tag>
            </Tooltip>
          );
        }
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
