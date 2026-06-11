import { useI18n } from '/@/hooks/web/useI18n';
import { useDict } from '/@/components/Dict';
import { DescItem } from '/@/components/Description/index';
import { DictEnum } from '/@/enums/commonEnum';
import { echoMapText } from '/@/utils/echo';
const { getDictLabel } = useDict();

const { t } = useI18n();

export const columns = (): DescItem[] => {
  return [
    {
      label: t('iot.link.ota.otaUpgradeRecords.taskId'),
      field: 'taskId',
    },
    {
      label: t('iot.link.ota.otaUpgradeRecords.deviceIdentification'),
      field: 'deviceIdentification',
    },
    {
      label: t('iot.link.ota.otaUpgradeRecords.upgradeStatus'),
      field: 'upgradeStatus',
      render: (curVal, data) => {
        return `${getDictLabel(DictEnum.LINK_OTA_TASK_RECORD_STATUS, data?.upgradeStatus, '')}`;
      },
    },
    {
      label: t('iot.link.ota.otaUpgradeRecords.sourceVersion'),
      field: 'sourceVersion',
    },
    {
      label: t('iot.link.ota.otaUpgradeRecords.targetVersion'),
      field: 'targetVersion',
    },
    {
      label: t('iot.link.ota.otaUpgradeRecords.appConfirmationStatus'),
      field: 'appConfirmationStatus',
      render: (curVal, data) => {
        return `${getDictLabel(
          DictEnum.LINK_OTA_TASK_RECORD_APP_CONFIRM_STATUS,
          data?.appConfirmationStatus,
          '',
        )}`;
      },
    },
    {
      label: t('iot.link.ota.otaUpgradeRecords.appConfirmationTime'),
      field: 'appConfirmationTime',
    },
    {
      label: t('iot.link.ota.otaUpgradeRecords.commandSendStatus'),
      field: 'commandSendStatus',
      render: (curVal, data) => {
        return `${getDictLabel(
          DictEnum.LINK_OTA_TASK_RECORD_COMMAND_SEND_STATUS,
          data?.commandSendStatus,
          '',
        )}`;
      },
    },
    {
      label: t('iot.link.ota.otaUpgradeRecords.lastCommandSendTime'),
      field: 'lastCommandSendTime',
    },
    {
      label: t('iot.link.ota.otaUpgradeRecords.commandContent'),
      field: 'commandContent',
      enableView: true,
    },
    // {
    //   label: t('iot.link.ota.otaUpgradeRecords.progress'),
    //   field: 'progress',
    // },
    {
      label: t('iot.link.ota.otaUpgradeRecords.errorCode'),
      field: 'errorCode',
    },
    {
      label: t('iot.link.ota.otaUpgradeRecords.errorMessage'),
      field: 'errorMessage',
    },
    {
      label: t('iot.link.ota.otaUpgradeRecords.startTime'),
      field: 'startTime',
    },
    {
      label: t('iot.link.ota.otaUpgradeRecords.endTime'),
      field: 'endTime',
    },
    // {
    //   label: t('iot.link.ota.otaUpgradeRecords.successDetails'),
    //   field: 'successDetails',
    // },
    // {
    //   label: t('iot.link.ota.otaUpgradeRecords.failureDetails'),
    //   field: 'failureDetails',
    // },
    // {
    //   label: t('iot.link.ota.otaUpgradeRecords.logDetails'),
    //   field: 'logDetails',
    // },
    {
      label: t('iot.link.ota.otaUpgradeRecords.remark'),
      field: 'remark',
    },
    {
      label: t('iot.link.ota.otaUpgradeRecords.createdOrgId'),
      field: 'createdOrgId',
      render: (_curVal, data) => echoMapText(data, 'createdOrgId'),
    },
    {
      label: t('thinglinks.common.createdTime'),
      field: 'createdTime',
    },
  ];
};
