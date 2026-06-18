<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    @ok="handleSubmit"
    :title="drawerTitle"
    :show-footer="true"
    width="50%"
  >
    <BasicForm @register="registerForm" />
  </BasicDrawer>
</template>
<script lang="ts">
  import { defineComponent, ref, unref, watch, computed } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { ActionEnum, VALIDATE_API } from '/@/enums/commonEnum';
  import { Api, saveV2, updateV2 } from '/@/api/iot/link/ota/otaUpgradeTasks';
  import { getValidateRules } from '/@/api/thinglinks/common/formValidateService';
  import { customFormSchemaRules, editFormSchema } from './otaUpgradeTasks.data';
  import { getDeviceVersionByProduct } from '/@/api/iot/link/device/device';

  export default defineComponent({
    name: '编辑OTA升级任务',
    components: { BasicDrawer, BasicForm },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const { t } = useI18n();
      const type = ref<string>('');
      const { createMessage, createConfirm } = useMessage();

      // 使用 computed 确保 title 响应式更新
      const drawerTitle = computed(() => {
        const currentType = type.value || ActionEnum.ADD;
        return t(`common.title.${currentType}`);
      });

      const productIdentification = ref('');
      const packageType = ref<number | undefined>(undefined);

      const [
        registerForm,
        { setFieldsValue, resetFields, updateSchema, validate, resetSchema, getFieldsValue },
      ] = useForm({
        name: 'OtaUpgradeTasksEdit',
        // labelWidth: 100,
        schemas: editFormSchema(type, productIdentification, packageType),
        showActionButtonGroup: false,
        disabled: (_) => {
          return unref(type) === ActionEnum.VIEW;
        },
        baseColProps: { span: 24 },
        actionColOptions: {
          span: 22,
        },
      });

      // 更新 sourceVersions 字段的 schema（仅在 productIdentification 变化时触发）
      const updateSourceVersionsSchema = () => {
        const currentProductIdentification = productIdentification.value;
        const isEdit = unref(type) === ActionEnum.EDIT;

        // 如果是编辑模式但没有 productIdentification，只设置 disabled
        if (isEdit && !currentProductIdentification) {
          updateSchema({
            field: 'sourceVersions',
            componentProps: {
              disabled: true,
            },
          });
          return;
        }

        if (!currentProductIdentification) return;

        updateSchema({
          field: 'sourceVersions',
          componentProps: (opt) => {
            const { formModel } = opt;
            const { productIdentification: formProductIdentification } = formModel;
            const currentId = formProductIdentification || currentProductIdentification;
            if (!currentId) return {};
            // 创建包装函数，获取完整数据后根据 packageType 选择字段
            const wrappedApi = async (params: string) => {
              const res = await getDeviceVersionByProduct(params);
              // 根据 packageType 选择返回哪个字段
              // packageType = 1 时返回 fwVersionList, packageType = 0 时返回 swVersionList
              if (res && typeof res === 'object' && !Array.isArray(res)) {
                const versionList = packageType.value === 1 ? res.fwVersionList : res.swVersionList;
                return Array.isArray(versionList) ? versionList : [];
              }
              // 如果返回的是数组，直接返回
              return Array.isArray(res) ? res : [];
            };
            return {
              mode: 'multiple', // 多选模式
              allowClear: true,
              api: wrappedApi,
              params: currentId,
              search: true,
              alwaysLoad: true, // 始终加载数据，确保回显
              disabled: isEdit, // 编辑模式下禁用
              labelField: 'label',
              valueField: 'value',
              // 将字符串数组转换为 { label: string, value: string }[] 格式
              afterFetch: (list: any[]) => {
                if (!list || !Array.isArray(list)) return;
                // 如果已经是对象数组，直接返回
                if (list.length > 0 && typeof list[0] === 'object' && 'label' in list[0]) {
                  return;
                }
                // 如果是字符串数组，转换为对象数组（直接修改原数组）
                const newList = (list as string[]).map((item: string) => ({
                  label: item,
                  value: item,
                }));
                list.length = 0;
                list.push(...newList);
              },
            };
          },
        });
      };

      watch(productIdentification, (val) => {
        setFieldsValue({ productIdentification: val });
        // 当 productIdentification 变化时，更新 sourceVersions 字段的 schema 以触发重新获取
        if (val) {
          updateSourceVersionsSchema();
        }
      });

      const [registerDrawer, { closeDrawer, setDrawerProps }] = useDrawerInner(async (data) => {
        productIdentification.value = '';
        packageType.value = undefined;
        type.value = data?.type || ActionEnum.ADD;
        // 更新 drawer title
        setDrawerProps({ title: drawerTitle.value });
        await resetSchema(editFormSchema(type, productIdentification, packageType, setFieldsValue));
        await resetFields();

        if (unref(type) !== ActionEnum.ADD) {
          // 赋值
          const record = { ...data?.record };
          productIdentification.value = record.otaUpgradesResult?.productIdentification || '';
          packageType.value = record.otaUpgradesResult?.packageType;
          await setFieldsValue({
            ...record,
            targetIds: record.upgradeScope === 1 ? record.targetValueList : [],
            groupIds: record.upgradeScope === 2 ? record.targetValueList : [],
            areaIds:
              record.upgradeScope === 3
                ? record.targetValueList.map((item) => item.split(','))
                : [],
            productIdentification: record.otaUpgradesResult?.productIdentification,
            sourceVersions: record?.sourceVersions ? record?.sourceVersions.split(',') : [],
          });
          // 编辑模式下，设置完值后更新 sourceVersions 的 schema（确保禁用）
          if (unref(type) === ActionEnum.EDIT) {
            updateSourceVersionsSchema();
          }
        }

        if (unref(type) !== ActionEnum.VIEW) {
          let validateApi = Api[VALIDATE_API[unref(type)]];
          await getValidateRules(validateApi, customFormSchemaRules(type, getFieldsValue)).then(
            async (rules) => {
              rules && rules.length > 0 && (await updateSchema(rules));
              // 确保编辑模式下 sourceVersions 字段被禁用（验证规则可能会覆盖 componentProps）
              if (unref(type) === ActionEnum.EDIT && productIdentification.value) {
                updateSourceVersionsSchema();
              }
            },
          );
        }
      });

      // 监听 type 变化，更新 drawer title
      watch(
        () => type.value,
        (newType) => {
          if (newType) {
            setDrawerProps({ title: drawerTitle.value });
          }
        },
      );

      async function handleSubmit() {
        let params: Recordable;
        try {
          params = await validate();
        } catch {
          // 校验未通过:表单已标红错误项,保持抽屉打开,避免抛出未捕获的 promise
          return;
        }
        try {
          const { upgradeScope } = params;
          if (upgradeScope === 0) {
            params['targetValueList'] = [productIdentification.value];
          } else if (upgradeScope === 1) {
            params['targetValueList'] = params['targetIds'];
          } else if (upgradeScope === 2) {
            params['targetValueList'] = params['groupIds'];
          } else if (upgradeScope === 3) {
            params['targetValueList'] = params['areaIds'].map(
              (item) => `${item.provinceCode || item[0]},${item.cityCode || item[1]}`,
            );
          }
          // 将 sourceVersions 数组转换为逗号分隔的字符串
          if (params['sourceVersions'] && Array.isArray(params['sourceVersions'])) {
            params['sourceVersions'] = params['sourceVersions'].join(',');
          }
          delete params['targetIds'];
          delete params['groupIds'];
          delete params['areaIds'];

          if (unref(type) !== ActionEnum.VIEW) {
            if (unref(type) === ActionEnum.EDIT) {
              await updateV2(params);
              closeDrawer();
              emit('success');
            } else {
              params.id = null;
              createConfirm({
                iconType: 'warning',
                content: t('iot.link.ota.otaUpgradeTasks.confirmText'),
                onOk: async () => {
                  try {
                    await saveV2(params);
                    createMessage.success(t(`common.tips.${type.value}Success`));
                    closeDrawer();
                    emit('success');
                  } catch (e) {}
                },
              });
            }
          }
        } finally {
        }
      }

      return { type, t, drawerTitle, registerDrawer, registerForm, handleSubmit };
    },
  });
</script>
<style scoped>
  :deep() .ant-picker {
    width: 100%;
  }
</style>
../../../../../api/iot/link/ota/otaUpgradeTasks
