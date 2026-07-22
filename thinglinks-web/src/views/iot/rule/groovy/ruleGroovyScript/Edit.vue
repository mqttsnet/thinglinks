<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModel"
    :title="t(`common.title.${type}`)"
    :maskClosable="false"
    :width="1100"
    @ok="handleSubmit"
    :keyboard="true"
  >
    <BasicForm @register="registerForm">
      <!--
        productIdentification ── 通用 IotProductPicker,选中产品标识存入 productIdentification,
        切产品时清空并重载版本下拉、清空主题模式。
      -->
      <template #productIdentification="{ model, field }">
        <IotProductPicker
          :modelValue="model[field]"
          :disabled="isFieldLocked"
          @update:modelValue="(v) => onProductUpdate(v, field)"
          @change="onProductChange"
        />
      </template>

      <!--
        objectVersion ── 产品版本级联(a-select,选项 listByProduct 过滤 versionStatus∈[1,2,3])。
      -->
      <template #objectVersion="{ model, field }">
        <a-select
          :value="model[field]"
          :options="versionOptions"
          :loading="versionLoading"
          :disabled="isFieldLocked || !model.productIdentification"
          :placeholder="t('iot.rule.groovy.ruleGroovyScript.topicInbound.versionPlaceholder')"
          show-search
          option-filter-prop="label"
          option-label-prop="value"
          allowClear
          style="width: 100%"
          @change="(v) => onFieldUpdate(v, field)"
        >
          <template #suffixIcon><BranchesOutlined /></template>
          <template #option="{ value, statusText, isActive }">
            <div class="ver-opt">
              <span class="ver-no">{{ value }}</span>
              <a-tag
                v-if="statusText"
                class="ver-status-tag"
                :color="versionStatusColor(statusText)"
              >
                {{ statusText }}
              </a-tag>
              <a-tag v-if="isActive" class="ver-active-tag" color="orange">
                {{ t('iot.rule.groovy.ruleGroovyScript.topicInbound.activeVersion') }}
              </a-tag>
            </div>
          </template>
        </a-select>
      </template>

      <!--
        topicPattern ── ProductTopicPicker,:productIdentification 绑定当前产品标识
        (基础预定义 topic + 自定义通配 + / #)。
      -->
      <template #topicPattern="{ model, field }">
        <ProductTopicPicker
          :modelValue="model[field]"
          :productIdentification="model.productIdentification || ''"
          :disabled="isFieldLocked"
          @update:modelValue="(v) => onTopicUpdate(v, field)"
        />
      </template>

      <template #scriptContent="{ model, field }">
        <div class="script-editor">
          <div class="script-editor-toolbar">
            <ScriptTemplatePicker
              :channel-code="model.channelCode"
              :current-value="model[field]"
              :disabled="isFieldLocked"
              @fill="(content) => (model[field] = content)"
            />
          </div>
          <codemirror
            v-model="model[field]"
            :autofocus="true"
            :extensions="scriptExtensions"
            :indent-with-tab="true"
            :style="{ height: '600px' }"
            :tab-size="2"
            placeholder="groovy 脚本"
          />
        </div> </template
    ></BasicForm>
  </BasicModal>
</template>
<script lang="ts">
  import { computed, defineComponent, ref, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { ActionEnum, VALIDATE_API } from '/@/enums/commonEnum';
  import { Api, save, update } from '/@/api/iot/rule/groovy/ruleGroovyScript';
  import { getValidateRules } from '/@/api/thinglinks/common/formValidateService';
  import {
    customFormSchemaRules,
    editFormSchema,
    TOPIC_INBOUND_TRANSFORM,
  } from './ruleGroovyScript.data';
  import { listByProduct } from '/@/api/iot/link/productVersion/productVersion';
  import { query as queryProducts } from '/@/api/iot/link/product/product';
  import { IotProductPicker } from '/@/components/iot/IotProductDevicePicker';
  import { ProductTopicPicker } from '/@/components/iot/ProductTopicPicker';
  import { ScriptTemplatePicker } from '/@/components/iot/ScriptTemplatePicker';
  import { BranchesOutlined } from '@ant-design/icons-vue';
  import { Codemirror } from 'vue-codemirror';
  import { java } from '@codemirror/lang-java';
  import { oneDark } from '@codemirror/theme-one-dark';

  /**
   * 可绑定的产品版本状态集合 ── 与后端 ProductVersionStatusEnum 对齐:
   * 1=PUBLISHED 已发布、2=CANARY 灰度中、3=SHADOW 影子;DRAFT(0)/ARCHIVED(4) 不可选。
   */
  const SELECTABLE_VERSION_STATUS = new Set([1, 2, 3]);

  export default defineComponent({
    name: '编辑规则脚本',
    components: {
      BasicModal,
      BasicForm,
      Codemirror,
      IotProductPicker,
      ProductTopicPicker,
      ScriptTemplatePicker,
      BranchesOutlined,
    },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const { t } = useI18n();
      const type = ref<ActionEnum>(ActionEnum.ADD);
      const { createMessage } = useMessage();
      const scriptExtensions = [java(), oneDark];

      // slot 内组件不会自动继承 BasicForm 的 disabled / dynamicDisabled,需手动绑定。
      // 仅 VIEW(查看)模式锁定;ADD / EDIT 均可编辑,唯一性由后端在新增 + 编辑时统一校验。
      const isFieldLocked = computed(() => unref(type) === ActionEnum.VIEW);

      // ============================== 产品版本级联 ==============================
      const versionOptions = ref<
        Array<{
          label: string;
          value: string;
          status?: number;
          statusText?: string;
          isActive?: boolean;
        }>
      >([]);
      const versionLoading = ref(false);

      function statusToText(status?: number): string {
        if (status === 1) return t('iot.rule.groovy.ruleGroovyScript.topicInbound.statusPublished');
        if (status === 2) return t('iot.rule.groovy.ruleGroovyScript.topicInbound.statusCanary');
        if (status === 3) return t('iot.rule.groovy.ruleGroovyScript.topicInbound.statusShadow');
        return '';
      }

      function versionStatusColor(statusText: string): string {
        if (statusText === t('iot.rule.groovy.ruleGroovyScript.topicInbound.statusPublished'))
          return 'green';
        if (statusText === t('iot.rule.groovy.ruleGroovyScript.topicInbound.statusCanary'))
          return 'orange';
        if (statusText === t('iot.rule.groovy.ruleGroovyScript.topicInbound.statusShadow'))
          return 'purple';
        return 'default';
      }

      /**
       * 拉取产品已发布版本 + 计算默认选中。
       * 默认优先级:编辑回显值(仍可选) > 产品 activeVersionNo > 第一项。
       */
      async function loadVersionOptions(
        productIdentification: string,
        currentVersionNo?: string,
      ): Promise<string | undefined> {
        if (!productIdentification) {
          versionOptions.value = [];
          return undefined;
        }
        versionLoading.value = true;
        try {
          const [list, productList] = await Promise.all([
            listByProduct(productIdentification),
            queryProducts({ productIdentification } as any),
          ]);
          const product = Array.isArray(productList) ? productList[0] : productList;
          const activeVersionNo = product?.activeVersionNo;

          versionOptions.value = (list || [])
            .filter((v) => v.versionNo && SELECTABLE_VERSION_STATUS.has(v.versionStatus ?? -1))
            .map((v) => ({
              value: v.versionNo!,
              label: v.versionNo!,
              status: v.versionStatus,
              statusText: statusToText(v.versionStatus),
              isActive: v.versionNo === activeVersionNo,
            }));

          if (currentVersionNo && versionOptions.value.find((o) => o.value === currentVersionNo)) {
            return currentVersionNo;
          }
          if (activeVersionNo && versionOptions.value.find((o) => o.value === activeVersionNo)) {
            return activeVersionNo;
          }
          return versionOptions.value[0]?.value;
        } catch (e) {
          console.warn('[groovy-edit] 拉取产品版本列表失败', e);
          versionOptions.value = [];
          return undefined;
        } finally {
          versionLoading.value = false;
        }
      }

      // ============================== Picker / 版本 同步 handler ==============================
      /** 产品选中同步到 productIdentification(走 setFieldsValue 触发 antdv form 内部缓存同步) */
      async function onProductUpdate(v: any, field: string) {
        await setFieldsValue({ [field]: v ?? '' });
      }

      /** 切产品:清空版本 + 主题 → 拉新产品版本列表 → 默认选中 activeVersionNo */
      async function onProductChange(val: any) {
        const productIdentification = Array.isArray(val) ? val[0] : val;
        await setFieldsValue({ objectVersion: '', topicPattern: '' });
        if (productIdentification) {
          const defaultVersion = await loadVersionOptions(productIdentification);
          if (defaultVersion) {
            await setFieldsValue({ objectVersion: defaultVersion });
          }
        } else {
          versionOptions.value = [];
        }
      }

      /** 通用写回 ── 字典下拉 / 版本下拉 / 文本输入统一走 setFieldsValue,保证 antdv form 内部缓存同步 */
      async function onFieldUpdate(v: any, field: string) {
        await setFieldsValue({ [field]: v ?? '' });
      }

      /** 主题模式选中同步到 topicPattern */
      async function onTopicUpdate(v: any, field: string) {
        await setFieldsValue({ [field]: v ?? '' });
      }

      const [
        registerForm,
        { setFieldsValue, resetFields, updateSchema, validate, resetSchema, getFieldsValue },
      ] = useForm({
        name: 'RuleGroovyScriptEdit',
        labelWidth: 150,
        schemas: editFormSchema(type),
        showActionButtonGroup: false,
        disabled: (_) => {
          return unref(type) === ActionEnum.VIEW;
        },
        baseColProps: { span: 12 },
      });

      const [registerModel, { setModalProps: setProps, closeModal: close }] = useModalInner(
        async (data) => {
          type.value = data?.type || ActionEnum.ADD;
          setProps({ confirmLoading: false });
          await resetSchema(editFormSchema(type));
          await resetFields();
          // 重置版本下拉,避免上次编辑残留
          versionOptions.value = [];

          if (unref(type) !== ActionEnum.ADD) {
            // 赋值：appId / channelCode 等 ApiSelect 要求 string 值；null/undefined 时跳过避免变 'null'/'undefined' 字面量
            const record = { ...data?.record };
            const stringify = (v: any) => (v == null ? v : String(v));
            record.appId = stringify(record?.appId);
            record.channelCode = stringify(record?.channelCode);
            record.productIdentification = stringify(record?.productIdentification);
            // 编辑/查看:基于已绑定产品(productIdentification)拉版本列表回显 objectVersion
            if (record.productIdentification) {
              const defaultVersion = await loadVersionOptions(
                record.productIdentification,
                record.objectVersion,
              );
              if (!record.objectVersion && defaultVersion) {
                record.objectVersion = defaultVersion;
              }
            }
            await setFieldsValue(record);
          }

          if (unref(type) !== ActionEnum.VIEW) {
            let validateApi = Api[VALIDATE_API[unref(type)]];
            // customFormSchemaRules 需要 getFieldsValue 作为第二参数（用于 async validator 读其它字段）
            await getValidateRules(validateApi, customFormSchemaRules(type, getFieldsValue)).then(
              async (rules) => {
                rules && rules.length > 0 && (await updateSchema(rules));
              },
            );
          }
        },
      );

      async function handleSubmit() {
        try {
          const params = await validate();
          // 规则脚本现仅「设备上行转换脚本」一种类型,scriptType 固定写入常量(表单不再暴露该字段)。
          params.scriptType = TOPIC_INBOUND_TRANSFORM;
          setProps({ confirmLoading: true });

          if (unref(type) !== ActionEnum.VIEW) {
            if (unref(type) === ActionEnum.EDIT) {
              await update(params);
            } else {
              params.id = null;
              await save(params);
            }
            createMessage.success(t(`common.tips.${type.value}Success`));
          }
          close();
          emit('success');
        } finally {
          setProps({ confirmLoading: false });
        }
      }

      return {
        type,
        t,
        scriptExtensions,
        registerModel,
        registerForm,
        handleSubmit,
        isFieldLocked,
        versionOptions,
        versionLoading,
        versionStatusColor,
        onProductUpdate,
        onProductChange,
        onFieldUpdate,
        onTopicUpdate,
      };
    },
  });
</script>
<style lang="less" scoped>
  /* 脚本编辑区:模板填充工具条 + 编辑器 */
  .script-editor {
    width: 100%;
  }

  .script-editor-toolbar {
    display: flex;
    justify-content: flex-end;
    margin-bottom: 6px;
  }

  /* 版本下拉 #option 项:版本号 + 状态 tag + 产品最新 tag */
  .ver-opt {
    display: flex;
    align-items: center;
    gap: 6px;

    .ver-no {
      font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
      font-size: 13px;
      color: #2a3547;
    }

    .ver-status-tag,
    .ver-active-tag {
      margin-right: 0;
      font-size: 11px;
      line-height: 1.4;
    }
  }
</style>
