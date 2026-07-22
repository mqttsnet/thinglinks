import { useDict } from '/@/components/Dict';
import { DictEnum } from '/@/enums/commonEnum';
import { DescItem } from '/@/components/Description/index';
import { useI18n } from '/@/hooks/web/useI18n';

const { getDictLabel } = useDict();
const { t } = useI18n();

export function getUpgradeTaskSchema(): DescItem[] {
  return [
    {
      label: t('iot.link.ota.otaUpgradeTasks.taskName'),
      field: 'taskName',
    },
    {
      label: t('iot.link.ota.otaUpgradeTasks.upgradeId'),
      field: 'upgradeId',
    },
    {
      label: t('iot.link.ota.otaUpgradeTasks.taskStatus'),
      field: 'taskStatus',
      render: (curVal) => {
        return getDictLabel(DictEnum.LINK_OTA_TASK_STATUS, curVal);
      },
    },
    {
      label: t('iot.link.ota.otaUpgradeTasks.upgradeMode'),
      field: 'upgradeMethod',
      render: (curVal) => {
        return getDictLabel(DictEnum.LINK_OTA_UPGRADE_METHOD, curVal);
      },
    },
    {
      label: t('iot.link.ota.otaUpgradeTasks.scheduledStartTime'),
      field: 'scheduledStartTime',
    },
    {
      label: t('iot.link.ota.otaUpgradeTasks.scheduledEndTime'),
      field: 'scheduledEndTime',
    },
    {
      label: t('iot.link.ota.otaUpgradeTasks.sourceVersions'),
      field: 'sourceVersions',
    },
    {
      label: t('iot.link.ota.otaUpgradeTasks.appConfirmationRequired'),
      field: 'appConfirmationRequired',
      render: (curVal) => {
        return curVal ? t('common.yes') : t('common.no');
      },
    },
    {
      label: t('iot.link.ota.otaUpgradeTasks.upgradeRate'),
      field: 'upgradeRate',
    },
    {
      label: t('iot.link.ota.otaUpgradeTasks.deviceUpgradeTimeout'),
      field: 'deviceUpgradeTimeout',
    },
    {
      label: t('iot.link.ota.otaUpgradeTasks.maxRetryCount'),
      field: 'maxRetryCount',
    },
    {
      label: t('iot.link.ota.otaUpgradeTasks.retryIntervalMinutes'),
      field: 'retryIntervalMinutes',
    },

    {
      label: t('iot.link.ota.otaUpgradeTasks.description'),
      field: 'description',
    },
    {
      label: t('iot.link.ota.otaUpgradeTasks.remark'),
      field: 'remark',
    },
    {
      label: t('thinglinks.common.createdTime'),
      field: 'createdTime',
    },
  ];
}

export function getUpgradePackageSchema(): DescItem[] {
  return [
    {
      label: t('iot.link.ota.otaUpgrades.appId'),
      field: 'appId',
    },
    {
      label: t('iot.link.ota.otaUpgrades.packageName'),
      field: 'packageName',
    },
    {
      label: t('iot.link.ota.otaUpgrades.packageType'),
      field: 'packageType',
      render: (curVal) => {
        return getDictLabel(DictEnum.LINK_OTA_PACKAGES_TYPE, curVal);
      },
    },
    {
      label: t('iot.link.ota.otaUpgrades.productIdentification'),
      field: 'productIdentification',
    },
    {
      label: t('iot.link.ota.otaUpgrades.version'),
      field: 'version',
    },
    {
      label: t('iot.link.ota.otaUpgrades.status'),
      field: 'status',
      render: (curVal) => {
        return getDictLabel(DictEnum.LINK_OTA_PACKAGES_STATUS, curVal);
      },
    },
    {
      label: t('iot.link.ota.otaUpgrades.upgradePackage'),
      field: 'fileLocation',
      slot: 'fileLocation',
    },
    {
      label: t('iot.link.ota.otaUpgrades.signMethod'),
      field: 'signMethod',
      render: (curVal) => {
        return getDictLabel(DictEnum.LINK_OTA_PACKAGES_SIGN_METHOD, curVal);
      },
    },
    {
      label: t('iot.link.ota.otaUpgrades.description'),
      field: 'description',
    },
    {
      label: t('iot.link.ota.otaUpgrades.customInfo'),
      field: 'customInfo',
    },
    {
      label: t('iot.link.ota.otaUpgrades.remark'),
      field: 'remark',
    },
  ];
}
