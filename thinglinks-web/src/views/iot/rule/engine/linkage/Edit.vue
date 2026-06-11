<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModel"
    :title="t(`common.title.${type}`)"
    :maskClosable="false"
    :keyboard="true"
    :destroyOnClose="true"
    @ok="onSubmit"
  >
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts">
  import { defineComponent, ref, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { ActionEnum } from '/@/enums/commonEnum';
  import { save, update } from '../../../../../api/iot/rule/engine/linkage/linkage';
  import { editFormSchemaLinkage } from './linkage.data';
  import { useUserStore } from '/@/store/modules/user';

  export default defineComponent({
    name: '规则联动详情',
    components: { BasicModal, BasicForm },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const { t } = useI18n();
      const type = ref<ActionEnum>(ActionEnum.ADD);
      const { createMessage } = useMessage();
      const userStore = useUserStore();

      const weekOptions = [
        { label: t('antd.DatePicker.lang.shortWeekDaysAll[0]'), value: 'monday' },
        { label: t('antd.DatePicker.lang.shortWeekDaysAll[1]'), value: 'tuesday' },
        { label: t('antd.DatePicker.lang.shortWeekDaysAll[2]'), value: 'wednesday' },
        { label: t('antd.DatePicker.lang.shortWeekDaysAll[3]'), value: 'thursday' },
        { label: t('antd.DatePicker.lang.shortWeekDaysAll[4]'), value: 'friday' },
        { label: t('antd.DatePicker.lang.shortWeekDaysAll[5]'), value: 'saturday' },
        { label: t('antd.DatePicker.lang.shortWeekDaysAll[6]'), value: 'sunday' },
      ];

      const [registerForm, { setFieldsValue, resetFields, validate, resetSchema }] = useForm({
        name: 'LinkageEdit',
        labelWidth: 120,
        showActionButtonGroup: false,
        disabled: (_) => unref(type) === ActionEnum.VIEW,
        baseColProps: { span: 11 },
        actionColOptions: { span: 22 },
        schemas: editFormSchemaLinkage(weekOptions),
      });

      const [registerModel, { setModalProps: setProps, closeModal: close }] = useModalInner(
        async (data) => {
          setProps({ confirmLoading: false, width: 1000 });
          type.value = data?.type || ActionEnum.ADD;
          await resetSchema(editFormSchemaLinkage(weekOptions));
          await resetFields();

          if (unref(type) !== ActionEnum.ADD) {
            // 编辑：解析 appointContent 并填充
            const record = { ...(data?.record || {}) };
            let appointContent: any = {};
            try {
              appointContent = record.appointContent ? JSON.parse(record.appointContent) : {};
            } catch {
              appointContent = {};
            }
            const timeframe = appointContent.timeframe || { startTime: '', endTime: '' };

            await setFieldsValue({
              id: record.id,
              ruleName: record.ruleName || '',
              appId: record.appId || '',
              effectiveType: record.effectiveType ?? 0,
              frequency: appointContent.frequency ?? null,
              timeframeStart: timeframe.startTime || '',
              timeframeEnd: timeframe.endTime || '',
              remark: record.remark || '',
              status: record.status ?? 0,
              week: appointContent.week.filter((w) => w.checked).map((w) => w.eg) ?? [],
              appointContent: appointContent ?? {},
            });
          } else {
            await setFieldsValue({ status: 0 });
          }
        },
      );

      async function onSubmit() {
        const params = await validate();

        // 组装 appointContent
        const appointContent = {
          ...params.appointContent,
          frequency: params.frequency ?? null,
          week: weekOptions.map((x: any) => ({
            name: x.label,
            eg: x.value,
            checked: params.week?.some((v: any) => x.value === v) || false,
          })),
          timeframe: {
            startTime: params.timeframeStart || '',
            endTime: params.timeframeEnd || '',
          },
        };

        const submitParams: any = {
          ...params,
          appointContent: JSON.stringify(appointContent),
          status: Number(params.status),
        };

        delete submitParams.frequency;
        delete submitParams.week;
        delete submitParams.timeframeStart;
        delete submitParams.timeframeEnd;

        if (unref(type) === ActionEnum.EDIT) {
          submitParams.id = params.id;
          await update(submitParams);
        } else {
          // 新增需要补充组织ID
          const info = userStore.getUserInfo;
          submitParams.createdOrgId = info?.baseEmployee?.createdOrgId;
          await save(submitParams);
        }

        createMessage.success(t(`common.tips.${type.value}Success`));
        close();
        emit('success');
      }

      return {
        t,
        type,
        registerModel,
        registerForm,
        onSubmit,
      };
    },
  });
</script>

<style lang="less" scoped>
  .week-checkboxes {
    display: flex;
    gap: 12px;
    flex-wrap: wrap;
  }
</style>
