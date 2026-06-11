<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModel"
    :title="t(`common.title.${type}`)"
    :maskClosable="false"
    :destroyOnClose="true"
    @ok="handleSubmit"
    :keyboard="true"
  >
    <BasicForm @register="registerForm">
      <!--
        产品选择 ── 通用 IotProductPicker(Flexy 卡片化弹窗,与 ACL/桥接同款)。
        @change 时拉取该产品的已发布版本列表,清空旧的 boundProductVersionNo 并默认选中 activeVersionNo。
        ⚠ 不直接 model[field] = v:走 setFieldsValue 标准 API,触发 antdv Form 内部 validate() 取值同步。
      -->
      <template #productIdentification="{ model, field }">
        <IotProductPicker
          :modelValue="model[field]"
          @update:modelValue="(v) => onProductIdentificationUpdate(v, model, field)"
          @change="onProductIdentificationChange"
        />
      </template>

      <!-- 设备绑定版本 ── 列出该产品可绑定版本;选完产品默认 activeVersionNo -->
      <template #boundProductVersionNo="{ model, field }">
        <a-select
          :value="model[field]"
          :options="versionOptions"
          :loading="versionLoading"
          :disabled="!model.productIdentification"
          :placeholder="t('iot.link.device.device.shadow.versionPlaceholder')"
          show-search
          option-filter-prop="label"
          option-label-prop="value"
          allowClear
          style="width: 100%"
          @change="(v) => onBoundVersionChange(v, model, field)"
        >
          <template #suffixIcon><BranchesOutlined /></template>
          <template #option="{ value, statusText, isActive }">
            <div class="ver-opt">
              <span class="ver-no">{{ value }}</span>
              <a-tag v-if="statusText" class="ver-status-tag" :color="versionStatusColor(statusText)">
                {{ statusText }}
              </a-tag>
              <a-tag v-if="isActive" class="ver-active-tag" color="orange">
                {{ t('iot.link.device.device.tag.activeVersion') }}
              </a-tag>
            </div>
          </template>
        </a-select>
      </template>

      <template #signKey="{ model, field }">
        <div class="editor_container">
          <FormItem :name="field" :rules="[{ required: true }]">
            <template #label>
              <div>{{ t('iot.link.device.device.signKey') }}</div>
            </template>
            <a-input
              v-model:value="model[field]"
              :rules="[{ required: true }]"
              :placeholder="t('common.inputText')"
            >
              <template #addonAfter>
                <ReloadOutlined @click="handleRandomString(field)" />
              </template>
            </a-input>
          </FormItem>
        </div>
      </template>
      <template #encryptKey="{ model, field }">
        <div class="editor_container">
          <FormItem :name="field" v-if="model.encryptMethod != 0" :rules="[{ required: true }]">
            <template #label>
              <div>{{ t('iot.link.device.device.encryptKey') }}</div>
            </template>
            <a-input v-model:value="model[field]" :placeholder="t('common.inputText')">
              <template #addonAfter>
                <ReloadOutlined @click="handleRandomString(field)" />
              </template>
            </a-input>
          </FormItem>
        </div>
      </template>
      <template #encryptVector="{ model, field }">
        <div class="editor_container">
          <FormItem :name="field" v-if="model.encryptMethod != 0" :rules="[{ required: true }]">
            <template #label>
              <div>{{ t('iot.link.device.device.encryptVector') }}</div>
            </template>
            <a-input
              v-model:value="model[field]"
              :rules="[{ required: true }]"
              :placeholder="t('common.inputText')"
            >
              <template #addonAfter>
                <ReloadOutlined @click="handleRandomString(field)" />
              </template>
            </a-input>
          </FormItem>
        </div>
      </template>

      <!--
        设备标签 ── 多标签输入,交互与色调对齐 ACL IP 白名单。回车 / 逗号 / 空格 自动切分成多个标签。
        后端 deviceTags 为逗号拼接字符串::value 把串拆成数组回显,@change 把数组拼回串走 setFieldsValue;
        @blur 兜底把"输了没回车就点确认"的残留文本也并进去,避免输入丢失。
      -->
      <template #deviceTags="{ model, field }">
        <a-select
          mode="tags"
          :value="splitTags(model[field])"
          :token-separators="[',', '，', ' ']"
          :max-tag-count="6"
          :placeholder="t('common.inputText')"
          allow-clear
          style="width: 100%"
          @change="(v) => onDeviceTagsChange(v, field)"
          @search="onDeviceTagsSearch"
          @blur="() => onDeviceTagsBlur(model, field)"
        >
          <template #suffixIcon><TagsOutlined /></template>
        </a-select>
        <div class="tag-tip">
          <InfoCircleOutlined />
          {{ t('iot.link.device.device.deviceTagsTip') }}
        </div>
      </template>
    </BasicForm>
  </BasicModal>
</template>
<script lang="ts">
  import { Form } from 'ant-design-vue';
  import { defineComponent, ref, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { ActionEnum } from '/@/enums/commonEnum';
  import { DeviceNodeType } from '/@/enums/link/device';
  import {
    save,
    update,
    detailBydeviceIdentification,
  } from '/@/api/iot/link/device/device';
  import { listByProduct } from '/@/api/iot/link/productVersion/productVersion';
  import { query as queryProducts } from '/@/api/iot/link/product/product';
  import {
    ReloadOutlined,
    BranchesOutlined,
    TagsOutlined,
    InfoCircleOutlined,
  } from '@ant-design/icons-vue';
  import { IotProductPicker } from '/@/components/iot/IotProductDevicePicker';
  import { editFormSchema } from './device.data';

  /**
   * 可选版本状态集合 ── 与后端 ProductVersionStatusEnum 对齐:
   * 1=PUBLISHED 已发布、2=CANARY 灰度中、3=SHADOW 影子。
   * DRAFT(0)/ARCHIVED(4) 不让用户绑定。
   */
  const SELECTABLE_VERSION_STATUS = new Set([1, 2, 3]);
  type OptionsItem = { label: string; value: string; disabled?: boolean };
  export default defineComponent({
    name: '编辑设备',
    components: {
      BasicModal,
      BasicForm,
      FormItem: Form.Item,
      ReloadOutlined,
      BranchesOutlined,
      TagsOutlined,
      InfoCircleOutlined,
      IotProductPicker,
    },
    props: {
      api: {
        type: Function as PropType<(arg?: Recordable | string) => Promise<OptionsItem[]>>,
        default: null,
      },
    },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const { t } = useI18n();
      const type = ref<ActionEnum>(ActionEnum.ADD);
      const { createMessage, createConfirm } = useMessage();
      const deviceLocationId = ref('');

      /**
       * 版本下拉选项:
       *   label      ── 完整 versionNo,用于 a-select 搜索 (option-filter-prop="label")
       *   value      ── 真实选中值
       *   statusText ── 状态文案(已发布/灰度中/影子),#option 渲染为彩色 tag
       *   isActive   ── 是否产品 activeVersionNo,#option 渲染"产品最新"高亮 tag
       */
      const versionOptions = ref<
        Array<{ label: string; value: string; status?: number; statusText?: string; isActive?: boolean }>
      >([]);
      const versionLoading = ref(false);

      /**
       * 拉版本列表 + 计算默认选中。
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
          console.warn('[device-edit] 拉取产品版本列表失败', e);
          versionOptions.value = [];
          return undefined;
        } finally {
          versionLoading.value = false;
        }
      }

      function statusToText(status?: number): string {
        if (status === 1) return t('iot.link.device.device.shadow.statusPublished');
        if (status === 2) return t('iot.link.device.device.shadow.statusCanary');
        if (status === 3) return t('iot.link.device.device.shadow.statusShadow');
        return '';
      }

      function versionStatusColor(statusText: string): string {
        // 跟字典 chip 颜色对齐 ── 已发布绿、灰度橙、影子紫
        if (statusText === t('iot.link.device.device.shadow.statusPublished')) return 'green';
        if (statusText === t('iot.link.device.device.shadow.statusCanary')) return 'orange';
        if (statusText === t('iot.link.device.device.shadow.statusShadow')) return 'purple';
        return 'default';
      }

      /** Picker 同步回 form;走 setFieldsValue 强制 antdv form 内部缓存与外部 reactive 双向同步 */
      async function onProductIdentificationUpdate(v: any, _model: Recordable, field: string) {
        await setFieldsValue({ [field]: v ?? '' });
      }

      /** 切产品:清空旧版本 → 拉新产品版本列表 → 默认选中 activeVersionNo(用户大概率不需手动改) */
      async function onProductIdentificationChange(val: any, records: any[]) {
        const productIdentification = Array.isArray(val) ? val[0] : val;
        const picked = Array.isArray(records) ? records[0] : records;
        await setFieldsValue({
          productName: picked?.productName ?? '',
          boundProductVersionNo: '',
        });

        if (productIdentification) {
          const defaultVersion = await loadVersionOptions(productIdentification);
          if (defaultVersion) {
            await setFieldsValue({ boundProductVersionNo: defaultVersion });
          }
        } else {
          versionOptions.value = [];
        }
      }

      /** 版本下拉切换 ── 直接写回表单 */
      async function onBoundVersionChange(v: any, _model: Recordable, field: string) {
        await setFieldsValue({ [field]: v ?? '' });
      }

      /* ---------------- 设备标签 多标签输入(后端 deviceTags 为逗号拼接字符串) ---------------- */
      /** a-select 里尚未提交的搜索文本 ── 防止"输了没回车就点确认"被丢弃 */
      const deviceTagSearch = ref('');
      /** 逗号串 -> 去空标签数组(a-select 回显) */
      function splitTags(v?: string): string[] {
        return v
          ? String(v)
              .split(',')
              .map((s) => s.trim())
              .filter(Boolean)
          : [];
      }
      /** 标签数组 -> 去空去重 */
      function normalizeTags(tags: string[]): string[] {
        const out: string[] = [];
        for (const raw of tags || []) {
          const s = String(raw).trim();
          if (s && !out.includes(s)) out.push(s);
        }
        return out;
      }
      async function onDeviceTagsChange(v: string[], field: string) {
        deviceTagSearch.value = '';
        await setFieldsValue({ [field]: normalizeTags(v).join(',') });
      }
      function onDeviceTagsSearch(v: string) {
        deviceTagSearch.value = v;
      }
      /** 失焦兜底:把还没回车提交的文本也并进去,避免输入丢失 */
      async function onDeviceTagsBlur(model: Recordable, field: string) {
        const pending = deviceTagSearch.value.trim();
        deviceTagSearch.value = '';
        if (!pending) return;
        await setFieldsValue({
          [field]: normalizeTags([...splitTags(model[field]), pending]).join(','),
        });
      }

      const [registerForm, { setFieldsValue, resetFields, validate, resetSchema }] = useForm({
        name: 'DeviceEdit',
        labelWidth: 120,
        schemas: editFormSchema(type),
        showActionButtonGroup: false,
        disabled: (_) => {
          return unref(type) === ActionEnum.VIEW;
        },
        baseColProps: { span: 11 },
        actionColOptions: {
          span: 22,
        },
      });

      const [registerModel, { setModalProps: setProps, closeModal: close }] = useModalInner(
        async (data) => {
          setProps({ confirmLoading: false, width: 1000 });
          type.value = data?.type || ActionEnum.ADD;
          await resetSchema(editFormSchema(type));
          await resetFields();
          // 重置版本下拉,避免上次编辑残留
          versionOptions.value = [];

          if (unref(type) !== ActionEnum.ADD) {
            // 设备详情统一走 deviceIdentification（业务唯一标识），不再依赖内部主键 id。
            // 调用方 detail.vue 把整个 deviceDetail 作为 record 传过来，里面已带 deviceIdentification。
            const recordIdentification =
              data?.record?.deviceIdentification || data?.record?.id;
            let record = await detailBydeviceIdentification(recordIdentification);
            if (record.deviceLocationResultVO) {
              deviceLocationId.value = record.deviceLocationResultVO.id;
              if (record.deviceLocationResultVO.provinceCode) {
                record.area = [
                  record.deviceLocationResultVO.provinceCode,
                  record.deviceLocationResultVO.cityCode,
                  record.deviceLocationResultVO.regionCode,
                ];
              }
              if (record.deviceLocationResultVO.latitude) {
                record.map = [
                  record.deviceLocationResultVO.longitude,
                  record.deviceLocationResultVO.latitude,
                ];
              }
              record.address = record.deviceLocationResultVO?.fullName || '';
            }
            record.authMode = String(record.authMode);
            record.nodeType = String(record.nodeType);
            record.deviceStatus = String(record.deviceStatus);
            record.encryptMethod = String(record.encryptMethod);
            record.protocolType = record.productResultVO?.protocolType;
            record.productName = record.productResultVO?.productName;
            record.productIdentification = record.productResultVO?.productIdentification;
            // 编辑/查看场景:基于已有 productIdentification 拉版本列表回显 boundProductVersionNo,
            // 若设备已有绑定值则保留;否则默认产品 activeVersionNo
            if (record.productIdentification) {
              const defaultVersion = await loadVersionOptions(
                record.productIdentification,
                record.boundProductVersionNo,
              );
              if (!record.boundProductVersionNo && defaultVersion) {
                record.boundProductVersionNo = defaultVersion;
              }
            }
            if (record.nodeType == DeviceNodeType.SUB_DEVICE && record.gatewayId) {
              // 父网关查询：仅用于回显"网关设备"下拉框的当前选中项。
              // 网关被删除时接口会 404 / 返回空，这里容错降级为占位符，不阻塞编辑流程。
              try {
                const gatewayObj = await getDeviceDetail(record.gatewayId);
                record.gatewayName = gatewayObj?.deviceName || '选择网关设备';
              } catch (e) {
                console.warn(
                  '[device-edit] 父网关查询失败（可能已删除）',
                  record.gatewayId,
                  e,
                );
                record.gatewayName = '选择网关设备';
              }
            }
            await setFieldsValue(record);
          }

          // if (unref(type) !== ActionEnum.VIEW) {
          //   let validateApi = Api[VALIDATE_API[unref(type)]];
          //   await getValidateRules(validateApi, customFormSchemaRules(type)).then(async (rules) => {
          //     rules && rules.length > 0 && (await updateSchema(rules));
          //   });
          // }
        },
      );

      const getDeviceDetail = async (deviceIdentification) => {
        if (!deviceIdentification || deviceIdentification === 'all') {
          return {};
        }
        return await detailBydeviceIdentification(deviceIdentification);
      };

      async function handleSubmit() {
        try {
          const params = await validate();
          console.log(params, 'pm');
          setProps({ confirmLoading: true });
          let obj = { ...params };

          let deviceLocationUpdateVO = {
            provinceCode: obj.area ? obj.area[0] : undefined,
            cityCode: obj.area ? obj.area[1] : undefined,
            regionCode: obj.area ? obj.area[2] : undefined,
            fullName: obj.address,
            longitude: obj.map ? obj.map[0] : undefined,
            latitude: obj.map ? obj.map[1] : undefined,
          };

          // if (obj.map) {
          if (obj.id) {
            obj.deviceLocationUpdateVO = {
              ...deviceLocationUpdateVO,
              id: deviceLocationId.value,
            };
          } else {
            obj.deviceLocationSaveVO = deviceLocationUpdateVO;
          }
          // }
          console.log(obj);
          // return false;
          if (unref(type) !== ActionEnum.VIEW) {
            if (unref(type) === ActionEnum.EDIT) {
              obj.id = obj.id;
              await update(obj);
            } else {
              delete obj.id;
              await save(obj);
            }
            createMessage.success(t(`common.tips.${type.value}Success`));
          }
          close();
          emit('success');
        } finally {
          setProps({ confirmLoading: false });
        }
      }
      const generateRandomString = () => {
        const chars = 'abcdefghijklmnopqrstuvwxyz0123456789';
        let result = '';
        for (let i = 0; i < 16; i++) {
          result += chars[Math.floor(Math.random() * chars.length)];
        }
        return result;
      };
      function handleRandomString(field) {
        createConfirm({
          iconType: 'warning',
          content: '是否随机生成一个 16 位字符？',
          onOk: async () => {
            // 生成随机字符串
            const randomString = generateRandomString();
            if (field == 'signKey') {
              await setFieldsValue({ signKey: randomString });
            } else if (field == 'encryptKey') {
              await setFieldsValue({ encryptKey: randomString });
            } else {
              await setFieldsValue({ encryptVector: randomString });
            }
          },
        });
      }
      return {
        type,
        t,
        registerModel,
        registerForm,
        handleSubmit,
        handleRandomString,
        // 产品 + 版本号联动
        versionOptions,
        versionLoading,
        onProductIdentificationUpdate,
        onProductIdentificationChange,
        onBoundVersionChange,
        versionStatusColor,
        // 设备标签 多标签输入
        splitTags,
        onDeviceTagsChange,
        onDeviceTagsSearch,
        onDeviceTagsBlur,
      };
    },
  });
</script>
<style lang="less" scoped>
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

  .editor_container {
    display: flex;
    align-items: center;
  }

  /* 设备标签提示 ── 与 ACL IP 白名单同款色调:灰字 + 琥珀色图标 */
  .tag-tip {
    margin-top: 4px;
    font-size: 12px;
    color: #8c8c8c;
    display: flex;
    align-items: center;
    gap: 4px;

    :deep(svg) {
      color: #faad14;
    }
  }

  :deep(.ant-col .ant-form-item-label) {
    width: 120px;
  }

  :deep(.ant-col .ant-form-item-control) {
    flex: 1;
  }

  :deep(.ant-row .ant-form-item) {
    flex: 1;
  }
</style>
../../../../../api/iot/link/device/device
