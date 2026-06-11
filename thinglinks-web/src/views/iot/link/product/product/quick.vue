<template>
  <PageWrapper title="" contentFullHeight>
    <div class="quick-form">
      <!-- 产品基本信息 -->
      <BasicForm
        @register="registerProductForm"
        @field-value-change="(key, value) => handleProductFieldChange(key, value)"
      />

      <!-- 服务列表 -->
      <Card
        hoverable
        :title="t('iot.link.product.product.quick.service')"
        v-for="(service, serviceIndex) in formData.services"
        :key="'service-' + serviceIndex"
      >
        <template #extra>
          <MinusCircleOutlined
            @click="delService(serviceIndex)"
            v-if="formData.services.length > 1 && serviceIndex != formData.services.length - 1"
          />
          <PlusCircleOutlined
            @click="addService"
            v-if="serviceIndex == formData.services.length - 1"
          />
        </template>

        <!-- 服务基本信息 -->
        <BasicForm
          :schemas="serviceSchemas"
          :showActionButtonGroup="false"
          :labelWidth="120"
          :baseColProps="{ span: 8 }"
          @register="(methods) => handleFormRegister('service', serviceIndex, methods)"
          @field-value-change="(key, value) => handleServiceFieldChange(serviceIndex, key, value)"
        />

        <!-- 服务属性 -->
        <Card
          hoverable
          :title="t('iot.link.product.product.quick.serviceAttributes')"
          v-for="(_, propertyIndex) in service.properties"
          :key="'property-' + serviceIndex + '-' + propertyIndex"
        >
          <template #extra>
            <MinusCircleOutlined
              @click="delProperty(serviceIndex, propertyIndex)"
              v-if="service.properties.length > 1 && propertyIndex != service.properties.length - 1"
            />
            <PlusCircleOutlined
              @click="addProperty(serviceIndex)"
              v-if="propertyIndex == service.properties.length - 1"
            />
          </template>

          <BasicForm
            :schemas="propertySchemas"
            :showActionButtonGroup="false"
            :labelWidth="120"
            :baseColProps="{ span: 8 }"
            @register="
              (methods) =>
                handleFormRegister('property', `${serviceIndex}-${propertyIndex}`, methods)
            "
            @field-value-change="
              (key, value) => handlePropertyFieldChange(serviceIndex, propertyIndex, key, value)
            "
          />
        </Card>

        <!-- 服务命令 -->
        <Card
          hoverable
          :title="t('iot.link.product.product.quick.serviceCommands')"
          v-for="(command, commandIndex) in service.commands"
          :key="'command-' + serviceIndex + '-' + commandIndex"
        >
          <template #extra>
            <MinusCircleOutlined
              @click="delCommand(serviceIndex, commandIndex)"
              v-if="service.commands.length > 1 && commandIndex != service.commands.length - 1"
            />
            <PlusCircleOutlined
              @click="addCommand(serviceIndex)"
              v-if="commandIndex == service.commands.length - 1"
            />
          </template>

          <BasicForm
            :schemas="commandSchemas"
            :showActionButtonGroup="false"
            :labelWidth="120"
            :baseColProps="{ span: 8 }"
            @register="
              (methods) => handleFormRegister('command', `${serviceIndex}-${commandIndex}`, methods)
            "
            @field-value-change="
              (key, value) => handleCommandFieldChange(serviceIndex, commandIndex, key, value)
            "
          />

          <!-- 请求参数 -->
          <Card
            hoverable
            :title="t('iot.link.product.product.quick.serviceRequestCommand')"
            v-for="(_, requestIndex) in command.requests"
            :key="'request-' + serviceIndex + '-' + commandIndex + '-' + requestIndex"
          >
            <template #extra>
              <MinusCircleOutlined
                @click="delRequest(serviceIndex, commandIndex, requestIndex)"
                v-if="command.requests.length > 1 && requestIndex != command.requests.length - 1"
              />
              <PlusCircleOutlined
                @click="addRequest(serviceIndex, commandIndex)"
                v-if="requestIndex == command.requests.length - 1"
              />
            </template>

            <BasicForm
              :schemas="requestSchemas"
              :showActionButtonGroup="false"
              :labelWidth="120"
              :baseColProps="{ span: 8 }"
              @register="
                (methods) =>
                  handleFormRegister(
                    'request',
                    `${serviceIndex}-${commandIndex}-${requestIndex}`,
                    methods,
                  )
              "
              @field-value-change="
                (key, value) =>
                  handleRequestFieldChange(serviceIndex, commandIndex, requestIndex, key, value)
              "
            />
          </Card>

          <!-- 响应参数 -->
          <Card
            hoverable
            :title="t('iot.link.product.product.quick.serviceResponseCommand')"
            v-for="(_, responseIndex) in command.responses"
            :key="'response-' + serviceIndex + '-' + commandIndex + '-' + responseIndex"
          >
            <template #extra>
              <MinusCircleOutlined
                @click="delResponse(serviceIndex, commandIndex, responseIndex)"
                v-if="command.responses.length > 1 && responseIndex != command.responses.length - 1"
              />
              <PlusCircleOutlined
                @click="addResponse(serviceIndex, commandIndex)"
                v-if="responseIndex == command.responses.length - 1"
              />
            </template>

            <BasicForm
              :schemas="responseSchemas"
              :showActionButtonGroup="false"
              :labelWidth="120"
              :baseColProps="{ span: 8 }"
              @register="
                (methods) =>
                  handleFormRegister(
                    'response',
                    `${serviceIndex}-${commandIndex}-${responseIndex}`,
                    methods,
                  )
              "
              @field-value-change="
                (key, value) =>
                  handleResponseFieldChange(serviceIndex, commandIndex, responseIndex, key, value)
              "
            />
          </Card>
        </Card>
      </Card>

      <div class="submit-box">
        <a-button type="primary" @click="handleSubmit">{{ t('common.title.confirm') }}</a-button>
      </div>
    </div>
    <div class="fix-priview" @click="showData">预览</div>
  </PageWrapper>
</template>

<script setup lang="ts">
  import { reactive, computed, h, nextTick } from 'vue';
  import { useUserStore } from '/@/store/modules/user';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { PageWrapper } from '/@/components/Page';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { Modal, Card } from 'ant-design-vue';
  import { PlusCircleOutlined, MinusCircleOutlined } from '@ant-design/icons-vue';
  import { quick } from '/@/api/iot/link/product/product';
  import { JsonPreview } from '/@/components/CodeEditor';
  import {
    getProductFormSchema,
    getServiceFormSchema,
    getPropertyFormSchema,
    getCommandFormSchema,
    getRequestFormSchema,
    getResponseFormSchema,
    createDefaultService,
    createDefaultProperty,
    createDefaultCommand,
    createDefaultRequest,
    createDefaultResponse,
  } from './quick.data';

  defineOptions({
    name: 'ProductQuick',
  });

  const { t } = useI18n();
  const { createMessage } = useMessage();
  const userStore = useUserStore();

  const getUserInfo = computed(() => {
    return userStore.getUserInfo;
  });

  const createdOrgId = getUserInfo.value?.baseEmployee?.createdOrgId;

  // ========== 表单数据集中管理 ==========
  // 独立的响应式数据对象，存储所有表单数据
  const formData = reactive<{
    // 产品基本信息
    appId: string;
    productName: string;
    productType: string;
    manufacturerId: string;
    manufacturerName: string;
    model: string;
    dataFormat: string;
    deviceType: string;
    protocolType: string;
    productStatus: string;
    remark: string;
    // 服务列表
    services: any[];
  }>({
    appId: '',
    productName: '',
    productType: '',
    manufacturerId: '',
    manufacturerName: '',
    model: '',
    dataFormat: 'JSON',
    deviceType: '',
    protocolType: '',
    productStatus: '0',
    remark: '',
    services: [createDefaultService(createdOrgId)],
  });

  // ========== 表单实例存储（用于校验） ==========
  const formInstances = reactive<{
    product: any;
    service: Map<number, any>;
    property: Map<string, any>;
    command: Map<string, any>;
    request: Map<string, any>;
    response: Map<string, any>;
  }>({
    product: null,
    service: new Map(),
    property: new Map(),
    command: new Map(),
    request: new Map(),
    response: new Map(),
  });

  // 处理表单注册事件
  function handleFormRegister(type: string, key: number | string, methods: any) {
    if (type === 'product') {
      formInstances.product = methods;
    } else {
      formInstances[type].set(key, methods);
    }
  }

  // ========== 字段值变化处理 ==========
  // 产品表单字段变化
  function handleProductFieldChange(key: string, value: any) {
    (formData as any)[key] = value;
  }

  // 服务表单字段变化
  function handleServiceFieldChange(serviceIndex: number, key: string, value: any) {
    if (formData.services[serviceIndex]) {
      formData.services[serviceIndex][key] = value;
    }
  }

  // 属性表单字段变化
  function handlePropertyFieldChange(
    serviceIndex: number,
    propertyIndex: number,
    key: string,
    value: any,
  ) {
    if (formData.services[serviceIndex]?.properties?.[propertyIndex]) {
      formData.services[serviceIndex].properties[propertyIndex][key] = value;
    }
  }

  // 命令表单字段变化
  function handleCommandFieldChange(
    serviceIndex: number,
    commandIndex: number,
    key: string,
    value: any,
  ) {
    if (formData.services[serviceIndex]?.commands?.[commandIndex]) {
      formData.services[serviceIndex].commands[commandIndex][key] = value;
    }
  }

  // 请求参数表单字段变化
  function handleRequestFieldChange(
    serviceIndex: number,
    commandIndex: number,
    requestIndex: number,
    key: string,
    value: any,
  ) {
    if (formData.services[serviceIndex]?.commands?.[commandIndex]?.requests?.[requestIndex]) {
      formData.services[serviceIndex].commands[commandIndex].requests[requestIndex][key] = value;
    }
  }

  // 响应参数表单字段变化
  function handleResponseFieldChange(
    serviceIndex: number,
    commandIndex: number,
    responseIndex: number,
    key: string,
    value: any,
  ) {
    if (formData.services[serviceIndex]?.commands?.[commandIndex]?.responses?.[responseIndex]) {
      formData.services[serviceIndex].commands[commandIndex].responses[responseIndex][key] = value;
    }
  }

  // 产品基本信息表单
  const [registerProductForm, { validate: validateProduct, setFieldsValue: setProductFields }] =
    useForm({
      name: 'ProductQuickForm',
      labelWidth: 120,
      schemas: getProductFormSchema(),
      showActionButtonGroup: false,
      baseColProps: { span: 8 },
    });

  // 设置产品表单默认值
  nextTick(() => {
    setProductFields({
      dataFormat: 'JSON',
      productStatus: '0',
    });
  });

  // 获取嵌套表单的 Schema
  const serviceSchemas = getServiceFormSchema();
  const propertySchemas = getPropertyFormSchema();
  const commandSchemas = getCommandFormSchema();
  const requestSchemas = getRequestFormSchema();
  const responseSchemas = getResponseFormSchema();

  // ========== 服务操作 ==========
  function addService() {
    formData.services.push(createDefaultService(createdOrgId));
  }

  function delService(index: number) {
    formData.services.splice(index, 1);
    // 清理相关的表单实例引用
    formInstances.service.delete(index);
    // 重新映射后续索引（可选，因为 Vue 会重新渲染并触发 register）
  }

  // ========== 属性操作 ==========
  function addProperty(serviceIndex: number) {
    formData.services[serviceIndex].properties.push(createDefaultProperty(createdOrgId));
  }

  function delProperty(serviceIndex: number, propertyIndex: number) {
    formData.services[serviceIndex].properties.splice(propertyIndex, 1);
    formInstances.property.delete(`${serviceIndex}-${propertyIndex}`);
  }

  // ========== 命令操作 ==========
  function addCommand(serviceIndex: number) {
    formData.services[serviceIndex].commands.push(createDefaultCommand(createdOrgId));
  }

  function delCommand(serviceIndex: number, commandIndex: number) {
    formData.services[serviceIndex].commands.splice(commandIndex, 1);
    formInstances.command.delete(`${serviceIndex}-${commandIndex}`);
  }

  // ========== 请求参数操作 ==========
  function addRequest(serviceIndex: number, commandIndex: number) {
    formData.services[serviceIndex].commands[commandIndex].requests.push(
      createDefaultRequest(createdOrgId),
    );
  }

  function delRequest(serviceIndex: number, commandIndex: number, requestIndex: number) {
    formData.services[serviceIndex].commands[commandIndex].requests.splice(requestIndex, 1);
    formInstances.request.delete(`${serviceIndex}-${commandIndex}-${requestIndex}`);
  }

  // ========== 响应参数操作 ==========
  function addResponse(serviceIndex: number, commandIndex: number) {
    formData.services[serviceIndex].commands[commandIndex].responses.push(
      createDefaultResponse(createdOrgId),
    );
  }

  function delResponse(serviceIndex: number, commandIndex: number, responseIndex: number) {
    formData.services[serviceIndex].commands[commandIndex].responses.splice(responseIndex, 1);
    formInstances.response.delete(`${serviceIndex}-${commandIndex}-${responseIndex}`);
  }

  // ========== 校验所有表单 ==========
  async function validateAllForms() {
    const errors: string[] = [];

    // 1. 校验产品表单
    try {
      await validateProduct();
    } catch (e: any) {
      errors.push('产品基本信息校验失败');
      throw e;
    }

    // 2. 校验所有服务表单
    for (let si = 0; si < formData.services.length; si++) {
      const serviceForm = formInstances.service.get(si);
      if (serviceForm?.validate) {
        try {
          await serviceForm.validate();
        } catch (e: any) {
          errors.push(`服务 ${si + 1} 校验失败`);
          throw e;
        }
      }

      // 校验属性表单
      const service = formData.services[si];
      for (let pi = 0; pi < service.properties.length; pi++) {
        const propertyForm = formInstances.property.get(`${si}-${pi}`);
        if (propertyForm?.validate) {
          try {
            await propertyForm.validate();
          } catch (e: any) {
            errors.push(`服务 ${si + 1} 属性 ${pi + 1} 校验失败`);
            throw e;
          }
        }
      }

      // 校验命令表单
      for (let ci = 0; ci < service.commands.length; ci++) {
        const commandForm = formInstances.command.get(`${si}-${ci}`);
        if (commandForm?.validate) {
          try {
            await commandForm.validate();
          } catch (e: any) {
            errors.push(`服务 ${si + 1} 命令 ${ci + 1} 校验失败`);
            throw e;
          }
        }

        const command = service.commands[ci];
        // 校验请求参数表单
        for (let ri = 0; ri < command.requests.length; ri++) {
          const requestForm = formInstances.request.get(`${si}-${ci}-${ri}`);
          if (requestForm?.validate) {
            try {
              await requestForm.validate();
            } catch (e: any) {
              errors.push(`服务 ${si + 1} 命令 ${ci + 1} 请求参数 ${ri + 1} 校验失败`);
              throw e;
            }
          }
        }

        // 校验响应参数表单
        for (let rsi = 0; rsi < command.responses.length; rsi++) {
          const responseForm = formInstances.response.get(`${si}-${ci}-${rsi}`);
          if (responseForm?.validate) {
            try {
              await responseForm.validate();
            } catch (e: any) {
              errors.push(`服务 ${si + 1} 命令 ${ci + 1} 响应参数 ${rsi + 1} 校验失败`);
              throw e;
            }
          }
        }
      }
    }

    return true;
  }

  // ========== 组装提交数据 ==========
  function assembleSubmitData() {
    // 从 formData 中提取产品基本信息
    const {
      appId,
      productName,
      productType,
      manufacturerId,
      manufacturerName,
      model,
      dataFormat,
      deviceType,
      protocolType,
      productStatus,
      remark,
      services,
    } = formData;

    // 辅助函数：从表单实例获取最新值并合并
    const getFormValues = (formInstance: any) => {
      return formInstance?.getFieldsValue ? formInstance.getFieldsValue() : {};
    };

    // 处理属性数据
    const processProperty = (prop: any, propertyForm: any) => {
      const propertyValues = getFormValues(propertyForm);
      return {
        ...prop,
        ...propertyValues,
        createdOrgId,
        // 处理图标字段，将文件对象数组转换为 id 字符串
        icon: Array.isArray(prop?.icon)
          ? prop?.icon?.map((item: any) => item?.id)?.join(',') ?? ''
          : '',
      };
    };

    // 处理请求/响应参数数据
    const processParameter = (param: any, paramForm: any) => ({
      ...param,
      ...getFormValues(paramForm),
      createdOrgId,
    });

    return {
      appId,
      productName,
      productType,
      manufacturerId,
      manufacturerName,
      model,
      dataFormat,
      deviceType,
      protocolType,
      productStatus,
      remark,
      services: services.map((service, si) => {
        const serviceForm = formInstances.service.get(si);
        return {
          ...service,
          ...getFormValues(serviceForm),
          serviceStatus: service.serviceStatus || '0',
          createdOrgId,
          properties: service.properties.map((prop: any, pi: number) => {
            const propertyForm = formInstances.property.get(`${si}-${pi}`);
            return processProperty(prop, propertyForm);
          }),
          commands: service.commands.map((cmd: any, ci: number) => {
            const commandForm = formInstances.command.get(`${si}-${ci}`);
            return {
              ...cmd,
              ...getFormValues(commandForm),
              createdOrgId,
              requests: cmd.requests.map((req: any, ri: number) => {
                const requestForm = formInstances.request.get(`${si}-${ci}-${ri}`);
                return processParameter(req, requestForm);
              }),
              responses: cmd.responses.map((res: any, rsi: number) => {
                const responseForm = formInstances.response.get(`${si}-${ci}-${rsi}`);
                return processParameter(res, responseForm);
              }),
            };
          }),
        };
      }),
    };
  }

  // ========== 提交表单 ==========
  async function handleSubmit() {
    try {
      // 先校验所有表单
      await validateAllForms();

      // 组装提交数据
      const submitData = assembleSubmitData();

      const res = await quick(submitData);
      if (res) {
        createMessage.success(t(`common.tips.confirmSuccess`));
      } else {
        createMessage.error(res.message || t(`common.tips.confirmFail`));
      }
    } catch (error: any) {
      console.error('快捷生成失败:', error);
    }
  }

  // ========== 预览数据 ==========
  // 复制数据到剪贴板
  function copyToClipboard(data: any) {
    const textToCopy = JSON.stringify(data, null, 2);
    navigator.clipboard
      .writeText(textToCopy)
      .then(() => {
        createMessage.success(t('common.tips.copySuccess'));
      })
      .catch(() => {
        createMessage.error(t('common.tips.copyFail'));
      });
  }

  async function showData() {
    try {
      // 直接使用 formData 组装预览数据（不需要校验）
      const previewData = assembleSubmitData();
      console.log('previewData:', previewData);

      let isCopyClick = false;

      Modal.confirm({
        title: '数据预览',
        width: '800px',
        content: h('div', { style: { maxHeight: '60vh', overflowY: 'auto' } }, [
          h(JsonPreview, { data: previewData }),
        ]),
        icon: null,
        okText: t('common.okText'),
        cancelText: t('common.title.copy'),
        okButtonProps: {
          type: 'primary',
        },
        cancelButtonProps: {
          onClick: (e: Event) => {
            e.stopPropagation();
            isCopyClick = true;
            copyToClipboard(previewData);
            // 重置标志
            setTimeout(() => {
              isCopyClick = false;
            }, 100);
          },
        },
        onCancel() {
          if (isCopyClick) {
            // 是复制按钮点击，阻止关闭
            return Promise.reject();
          }
          // 是关闭按钮，正常关闭
          return Promise.resolve();
        },
      });
    } catch (error) {
      console.error('预览失败:', error);
      createMessage.error('获取预览数据失败');
    }
  }
</script>

<style lang="less" scoped>
  .quick-form {
    background: #fff;
    padding: 16px;
    margin: 16px;
  }

  .submit-box {
    text-align: center;
    margin-top: 24px;
  }

  .fix-priview {
    position: fixed;
    right: 100px;
    bottom: 100px;
    width: 40px;
    text-align: center;
    line-height: 40px;
    border-radius: 50%;
    background-color: rgba(0, 0, 0, 0.45);
    cursor: pointer;
    color: #fff;

    &:hover {
      background-color: rgba(0, 0, 0, 0.85);
    }
  }
</style>

<!-- 使用非 scoped 样式确保生产环境下样式不会被优化掉 -->
<style lang="less">
  .quick-form {
    .ant-card {
      border: 1px solid #000 !important;

      & + .ant-card {
        margin-top: 24px;
      }

      // 确保 extra 区域正常显示
      .ant-card-head {
        .ant-card-extra {
          display: flex;
          align-items: center;
          gap: 8px;
        }
      }
    }
  }
</style>
