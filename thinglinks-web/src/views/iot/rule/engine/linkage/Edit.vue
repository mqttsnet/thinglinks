<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModel"
    :title="t(`common.title.${type}`)"
    width="860px"
    :maskClosable="false"
    :keyboard="true"
    :destroyOnClose="true"
    wrapClassName="linkage-edit-flexy-wrap"
    @ok="onSubmit"
  >
    <div class="flexy-body">
      <!-- ─────── 顶部规则信息条(紧凑单行) ─────── -->
      <div class="flexy-header">
        <div class="header-icon-wrap">
          <ThunderboltOutlined class="header-icon" />
        </div>
        <div class="header-content">
          <div class="header-line">
            <span class="rule-name">{{ headerName }}</span>
          </div>
          <div class="header-hint">{{ t('iot.link.engine.linkage.editHint') }}</div>
        </div>
      </div>

      <!-- ─────── 1:基础信息 ─────── -->
      <div class="flexy-card">
        <div class="step-header">
          <span class="step-no">1</span>
          <span class="step-title-text">{{ t('iot.link.engine.linkage.basicInfo') }}</span>
        </div>
        <BasicForm @register="registerBasicForm" />
      </div>

      <!-- ─────── 2:生效时间 ─────── -->
      <div class="flexy-card">
        <div class="step-header">
          <span class="step-no">2</span>
          <span class="step-title-text">{{ t('iot.link.engine.linkage.createdTime') }}</span>
        </div>
        <BasicForm @register="registerEffectiveForm" />
      </div>
    </div>
  </BasicModal>
</template>

<script lang="ts">
  import { computed, defineComponent, ref, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { ActionEnum } from '/@/enums/commonEnum';
  import { ThunderboltOutlined } from '@ant-design/icons-vue';
  import { save, update } from '../../../../../api/iot/rule/engine/linkage/linkage';
  import { editBasicFormSchema, editEffectiveFormSchema } from './linkage.data';
  import { useUserStore } from '/@/store/modules/user';

  export default defineComponent({
    name: '规则联动详情',
    components: { BasicModal, BasicForm, ThunderboltOutlined },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const { t } = useI18n();
      const type = ref<ActionEnum>(ActionEnum.ADD);
      const { createMessage } = useMessage();
      const userStore = useUserStore();
      const editRuleName = ref<string>('');

      /** 头部标题:编辑态显示规则名,新增态显示动作文案 */
      const headerName = computed(() =>
        unref(type) === ActionEnum.ADD ? t('common.title.add') : editRuleName.value || '—',
      );

      const weekOptions = [
        { label: t('antd.DatePicker.lang.shortWeekDaysAll[0]'), value: 'monday' },
        { label: t('antd.DatePicker.lang.shortWeekDaysAll[1]'), value: 'tuesday' },
        { label: t('antd.DatePicker.lang.shortWeekDaysAll[2]'), value: 'wednesday' },
        { label: t('antd.DatePicker.lang.shortWeekDaysAll[3]'), value: 'thursday' },
        { label: t('antd.DatePicker.lang.shortWeekDaysAll[4]'), value: 'friday' },
        { label: t('antd.DatePicker.lang.shortWeekDaysAll[5]'), value: 'saturday' },
        { label: t('antd.DatePicker.lang.shortWeekDaysAll[6]'), value: 'sunday' },
      ];

      // 基础信息与生效时间拆成两个表单,与 flexy 分卡布局一一对应;
      // 校验时两个表单都要过,提交时字段合并组装
      const [registerBasicForm, basicFormMethods] = useForm({
        name: 'LinkageEditBasic',
        labelWidth: 100,
        showActionButtonGroup: false,
        disabled: (_) => unref(type) === ActionEnum.VIEW,
        baseColProps: { span: 12 },
        schemas: editBasicFormSchema(),
      });

      const [registerEffectiveForm, effectiveFormMethods] = useForm({
        name: 'LinkageEditEffective',
        labelWidth: 100,
        showActionButtonGroup: false,
        disabled: (_) => unref(type) === ActionEnum.VIEW,
        baseColProps: { span: 12 },
        schemas: editEffectiveFormSchema(weekOptions),
      });

      const [registerModel, { setModalProps: setProps, closeModal: close }] = useModalInner(
        async (data) => {
          setProps({ confirmLoading: false });
          type.value = data?.type || ActionEnum.ADD;
          await basicFormMethods.resetFields();
          await effectiveFormMethods.resetFields();
          editRuleName.value = '';

          if (unref(type) !== ActionEnum.ADD) {
            // 编辑：解析 appointContent 并按表单分片填充
            const record = { ...(data?.record || {}) };
            editRuleName.value = record.ruleName || '';
            let appointContent: any = {};
            try {
              appointContent = record.appointContent ? JSON.parse(record.appointContent) : {};
            } catch {
              appointContent = {};
            }
            const timeframe = appointContent.timeframe || { startTime: '', endTime: '' };

            await basicFormMethods.setFieldsValue({
              id: record.id,
              ruleName: record.ruleName || '',
              appId: record.appId || '',
              remark: record.remark || '',
              status: record.status ?? 0,
              appointContent: appointContent ?? {},
            });
            await effectiveFormMethods.setFieldsValue({
              effectiveType: record.effectiveType ?? 0,
              frequency: appointContent.frequency ?? null,
              week: (appointContent.week ?? []).filter((w) => w.checked).map((w) => w.eg),
              timeframeStart: timeframe.startTime || '',
              timeframeEnd: timeframe.endTime || '',
            });
          } else {
            await basicFormMethods.setFieldsValue({ status: 0 });
          }
        },
      );

      async function onSubmit() {
        // 两个分卡表单都通过校验后再合并提交
        const basicParams = await basicFormMethods.validate();
        const effectiveParams = await effectiveFormMethods.validate();
        const params: any = { ...basicParams, ...effectiveParams };

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
        headerName,
        registerModel,
        registerBasicForm,
        registerEffectiveForm,
        onSubmit,
      };
    },
  });
</script>

<!-- BasicModal teleport 到 body,scoped 选择器命中不到弹窗内容,
     故意用带 .linkage-edit-flexy-wrap 命名空间的全局样式(flexy 弹窗惯例,同 PublishModal) -->
<style lang="less">
  .linkage-edit-flexy-wrap {
    .flexy-body {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    .flexy-header {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 14px 16px;
      background: fade(@primary-color, 4%);
      border: 1px solid fade(@primary-color, 10%);
      border-radius: 12px;

      .header-icon-wrap {
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
        width: 40px;
        height: 40px;
        border-radius: 10px;
        background: fade(@primary-color, 10%);

        .header-icon {
          font-size: 20px;
          color: @primary-color;
        }
      }

      .header-content {
        min-width: 0;
      }

      .header-line {
        display: flex;
        align-items: center;
        gap: 8px;

        .rule-name {
          font-size: 15px;
          font-weight: 600;
          color: @text-color-base;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }

      .header-hint {
        margin-top: 2px;
        font-size: 12px;
        color: @text-color-secondary;
      }
    }

    .flexy-card {
      padding: 16px 20px 4px;
      background: @component-background;
      border: 1px solid @border-color-base;
      border-radius: 12px;

      .step-header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 14px;

        .step-no {
          display: flex;
          align-items: center;
          justify-content: center;
          width: 20px;
          height: 20px;
          border-radius: 50%;
          background: fade(@primary-color, 10%);
          color: @primary-color;
          font-size: 12px;
          font-weight: 700;
        }

        .step-title-text {
          font-size: 14px;
          font-weight: 600;
          color: @text-color-base;
        }
      }
    }
  }
</style>
