<template>
  <PageWrapper title="" contentFullHeight>
    <div class="quick-form">
      <a-form
        ref="formRef"
        name="advanced_search"
        class="ant-advanced-search-form"
        :model="formState"
        :labelCol="{ span: 9 }"
        @finish="finish"
      >
        <a-row>
          <a-col :span="8">
            <a-form-item
              :name="`appId`"
              :label="t('iot.link.product.product.appId')"
              :rules="[
                {
                  required: true,
                  message: t('common.chooseText') + `${t('iot.link.product.product.appId')}`,
                },
              ]"
            >
              <a-select
                v-model:value="formState.appId"
                :options="getDictList('LINK_APPLICATION_SCENARIO')"
                :fieldNames="dictFieldNames"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item
              :name="`productName`"
              :label="t('iot.link.product.product.productName')"
              :rules="[
                {
                  required: true,
                  message: t('common.inputText') + `${t('iot.link.product.product.productName')}`,
                },
              ]"
            >
              <a-input
                v-model:value="formState.productName"
                :placeholder="
                  t('common.inputText') + `${t('iot.link.product.product.productName')}`
                "
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row>
          <a-col :span="8">
            <a-form-item
              :name="`productType`"
              :label="t('iot.link.product.product.productType')"
              :rules="[
                {
                  required: true,
                  message: t('common.chooseText') + `${t('iot.link.product.product.productType')}`,
                },
              ]"
            >
              <a-select
                v-model:value="formState.productType"
                :options="getDictList('LINK_PRODUCT_TYPE')"
                :fieldNames="dictFieldNames"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item
              :name="`manufacturerId`"
              :label="t('iot.link.product.product.manufacturerId')"
              :rules="[
                {
                  required: true,
                  message:
                    t('common.inputText') + `${t('iot.link.product.product.manufacturerId')}`,
                },
              ]"
            >
              <a-input
                v-model:value="formState.manufacturerId"
                :placeholder="
                  t('common.inputText') + `${t('iot.link.product.product.manufacturerId')}`
                "
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item
              :name="`manufacturerName`"
              :label="t('iot.link.product.product.manufacturerName')"
              :rules="[
                {
                  required: true,
                  message:
                    t('common.inputText') + `${t('iot.link.product.product.manufacturerName')}`,
                },
              ]"
            >
              <a-input
                v-model:value="formState.manufacturerName"
                :placeholder="
                  t('common.inputText') + `${t('iot.link.product.product.manufacturerName')}`
                "
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row>
          <a-col :span="8">
            <a-form-item
              :name="`model`"
              :label="t('iot.link.product.product.model')"
              :rules="[
                {
                  required: true,
                  message: t('common.inputText') + `${t('iot.link.product.product.model')}`,
                },
              ]"
            >
              <a-input
                v-model:value="formState.model"
                :placeholder="t('common.inputText') + `${t('iot.link.product.product.model')}`"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item
              :name="`dataFormat`"
              :label="t('iot.link.product.product.dataFormat')"
              :rules="[
                {
                  required: true,
                  message: t('common.chooseText') + `${t('iot.link.product.product.dataFormat')}`,
                },
              ]"
            >
              <a-select
                disabled
                v-model:value="formState.dataFormat"
                :options="getDictList('LINK_PRODUCT_DATA_FORMAT')"
                :fieldNames="dictFieldNames"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item
              :name="`deviceType`"
              :label="t('iot.link.product.product.deviceType')"
              :rules="[
                {
                  required: true,
                  message: t('common.inputText') + `${t('iot.link.product.product.deviceType')}`,
                },
              ]"
            >
              <a-input
                v-model:value="formState.deviceType"
                :placeholder="t('common.inputText') + `${t('iot.link.product.product.deviceType')}`"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row>
          <a-col :span="8">
            <a-form-item
              :name="`protocolType`"
              :label="t('iot.link.product.product.protocolType')"
              :rules="[
                {
                  required: true,
                  message: t('common.chooseText') + `${t('iot.link.product.product.protocolType')}`,
                },
              ]"
            >
              <a-select
                v-model:value="formState.protocolType"
                :options="getDictList('LINK_PRODUCT_PROTOCOL_TYPE')"
                :fieldNames="dictFieldNames"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item
              :name="`productStatus`"
              :label="t('iot.link.product.product.productStatus')"
              :rules="[
                {
                  required: false,
                  message:
                    t('common.chooseText') + `${t('iot.link.product.product.productStatus')}`,
                },
              ]"
            >
              <a-select
                v-model:value="formState.productStatus"
                :options="getDictList('LINK_PRODUCT_STATUS')"
                :fieldNames="dictFieldNames"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row>
          <a-col :span="24">
            <a-form-item
              :name="`remark`"
              :labelCol="{ span: 3 }"
              :label="t('iot.link.product.product.remark')"
              :rules="[
                {
                  required: false,
                  message: t('common.inputText') + `${t('iot.link.product.product.remark')}`,
                },
              ]"
            >
              <a-textarea
                v-model:value="formState.remark"
                :placeholder="t('common.inputText') + `${t('iot.link.product.product.remark')}`"
                :auto-size="{ minRows: 2, maxRows: 5 }"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-card
          hoverable
          :title="t('iot.link.product.product.quick.service')"
          v-for="(item, index) in formState.services"
          :key="index"
        >
          <template #extra>
            <MinusCircleOutlined
              @click="delProductService(index)"
              v-if="formState.services.length > 1 && index != formState.services?.length - 1"
            />
            <PlusCircleOutlined
              @click="addProductService"
              v-if="index == formState.services?.length - 1"
            />
          </template>
          <a-row>
            <a-col :span="8">
              <a-form-item
                :name="['services', index, 'serviceCode']"
                :label="t('iot.link.productService.productService.serviceCode')"
                :rules="[
                  {
                    required: true,
                    message:
                      t('common.inputText') +
                      `${t('iot.link.productService.productService.serviceCode')}`,
                  },
                ]"
              >
                <a-input
                  v-model:value="item.serviceCode"
                  :placeholder="
                    t('common.inputText') +
                    `${t('iot.link.productService.productService.serviceCode')}`
                  "
                />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item
                :name="['services', index, 'serviceName']"
                :label="t('iot.link.productService.productService.serviceName')"
                :rules="[
                  {
                    required: true,
                    message:
                      t('common.inputText') +
                      `${t('iot.link.productService.productService.serviceName')}`,
                  },
                ]"
              >
                <a-input
                  v-model:value="item.serviceName"
                  :placeholder="
                    t('common.inputText') +
                    `${t('iot.link.productService.productService.serviceName')}`
                  "
                />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item
                :name="['services', index, 'serviceType']"
                :label="t('iot.link.productService.productService.serviceType')"
                :rules="[
                  {
                    required: true,
                    message:
                      t('common.inputText') +
                      `${t('iot.link.productService.productService.serviceType')}`,
                  },
                ]"
              >
                <a-select
                  v-model:value="item.serviceType"
                  :options="getDictList('LINK_PRODUCT_SERVICE_TYPE')"
                  :fieldNames="dictFieldNames"
                />
              </a-form-item>
            </a-col>
          </a-row>
          <a-row>
            <a-col :span="8">
              <a-form-item
                :name="['services', index, 'serviceStatus']"
                :label="t('iot.link.productService.productService.serviceStatus')"
                :rules="[
                  {
                    required: false,
                    message:
                      t('common.inputText') +
                      `${t('iot.link.productService.productService.serviceStatus')}`,
                  },
                ]"
              >
                <a-select
                  v-model:value="item.serviceStatus"
                  :options="getDictList('LINK_PRODUCT_STATUS')"
                  :fieldNames="dictFieldNames"
                />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item
                :name="['services', index, 'description']"
                :label="t('iot.link.productService.productService.description')"
                :rules="[
                  {
                    required: false,
                    message:
                      t('common.inputText') +
                      `${t('iot.link.productService.productService.description')}`,
                  },
                ]"
              >
                <a-input
                  v-model:value="item.description"
                  :placeholder="
                    t('common.inputText') +
                    `${t('iot.link.productService.productService.description')}`
                  "
                />
              </a-form-item>
            </a-col>
          </a-row>
          <a-row>
            <a-col :span="24">
              <a-form-item
                :labelCol="{ span: 3 }"
                :name="['services', index, 'remark']"
                :label="t('iot.link.productService.productService.remark')"
                :rules="[
                  {
                    required: false,
                    message:
                      t('common.inputText') +
                      `${t('iot.link.productService.productService.remark')}`,
                  },
                ]"
              >
                <a-textarea
                  v-model:value="item.remark"
                  :placeholder="
                    t('common.inputText') + `${t('iot.link.productService.productService.remark')}`
                  "
                  :auto-size="{ minRows: 2, maxRows: 5 }"
                />
              </a-form-item>
            </a-col>
          </a-row>
          <a-card
            hoverable
            :title="t('iot.link.product.product.quick.serviceAttributes')"
            v-for="(arr, idx) in item.properties"
            :key="idx"
          >
            <template #extra>
              <MinusCircleOutlined
                @click="delProductServicePropertiesOrCommands(index, 'properties', idx)"
                v-if="item.properties.length > 1 && idx != item.properties.length - 1"
              />
              <PlusCircleOutlined
                @click="addProductServiceProperties(index)"
                v-if="idx == item.properties.length - 1"
              />
            </template>
            <a-row>
              <a-col :span="8">
                <a-form-item
                  :name="['services', index, 'properties', idx, 'propertyCode']"
                  :label="t('iot.link.productProperty.productProperty.propertyCode')"
                  :rules="[
                    {
                      required: true,
                      message:
                        t('common.inputText') +
                        `${t('iot.link.productProperty.productProperty.propertyCode')}`,
                    },
                  ]"
                >
                  <a-input
                    v-model:value="arr.propertyCode"
                    :placeholder="
                      t('common.inputText') +
                      `${t('iot.link.productProperty.productProperty.propertyCode')}`
                    "
                  />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item
                  :name="['services', index, 'properties', idx, 'propertyName']"
                  :label="t('iot.link.productProperty.productProperty.propertyName')"
                  :rules="[
                    {
                      required: true,
                      message:
                        t('common.inputText') +
                        `${t('iot.link.productProperty.productProperty.propertyName')}`,
                    },
                  ]"
                >
                  <a-input
                    v-model:value="arr.propertyName"
                    :placeholder="
                      t('common.inputText') +
                      `${t('iot.link.productProperty.productProperty.propertyName')}`
                    "
                  />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item
                  :name="['services', index, 'properties', idx, 'datatype']"
                  :label="t('iot.link.productProperty.productProperty.datatype')"
                  :rules="[
                    {
                      required: true,
                      message:
                        t('common.chooseText') +
                        `${t('iot.link.productProperty.productProperty.datatype')}`,
                    },
                  ]"
                >
                  <a-select
                    v-model:value="arr.datatype"
                    :options="getDictList('LINK_PRODUCT_SERVICE_PROPERTY_DATA_TYPE')"
                    :fieldNames="dictFieldNames"
                  />
                </a-form-item>
              </a-col>
            </a-row>
            <a-row>
              <a-col :span="8">
                <a-form-item
                  :name="['services', index, 'properties', idx, 'icon']"
                  :label="t('iot.link.productProperty.productProperty.icon')"
                  :rules="[
                    {
                      required: false,
                      message:
                        t('common.inputText') +
                        `${t('iot.link.productProperty.productProperty.icon')}`,
                    },
                  ]"
                >
                  <a-input
                    v-model:value="arr.icon"
                    :placeholder="
                      t('common.inputText') +
                      `${t('iot.link.productProperty.productProperty.icon')}`
                    "
                  />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item
                  :name="['services', index, 'properties', idx, 'description']"
                  :label="t('iot.link.productProperty.productProperty.description')"
                  :rules="[
                    {
                      required: false,
                      message:
                        t('common.inputText') +
                        `${t('iot.link.productProperty.productProperty.description')}`,
                    },
                  ]"
                >
                  <a-input
                    v-model:value="arr.description"
                    :placeholder="
                      t('common.inputText') +
                      `${t('iot.link.productProperty.productProperty.description')}`
                    "
                  />
                </a-form-item>
              </a-col>
            </a-row>
            <a-row>
              <a-col :span="8" v-if="['string'].indexOf(arr.datatype) !== -1">
                <a-form-item
                  :name="['services', index, 'properties', idx, 'enumlist']"
                  :label="t('iot.link.productProperty.productProperty.enumlist')"
                  :rules="[
                    {
                      required: true,
                      message:
                        t('common.inputText') +
                        `${t('iot.link.productProperty.productProperty.enumlist')}`,
                    },
                  ]"
                >
                  <a-input
                    v-model:value="arr.enumlist"
                    :placeholder="
                      t('common.inputText') +
                      `${t('iot.link.productProperty.productProperty.enumlist')}`
                    "
                  />
                </a-form-item>
              </a-col>
              <a-col :span="8" v-if="['int', 'decimal'].indexOf(arr.datatype) !== -1">
                <a-form-item
                  :name="['services', index, 'properties', idx, 'min']"
                  :label="t('iot.link.productProperty.productProperty.min')"
                  :rules="[
                    {
                      required: true,
                      message:
                        t('common.inputText') +
                        `${t('iot.link.productProperty.productProperty.min')}`,
                    },
                  ]"
                >
                  <a-input
                    v-model:value="arr.min"
                    :placeholder="
                      t('common.inputText') + `${t('iot.link.productProperty.productProperty.min')}`
                    "
                  />
                </a-form-item>
              </a-col>
              <a-col :span="8" v-if="['int', 'decimal'].indexOf(arr.datatype) !== -1">
                <a-form-item
                  :name="['services', index, 'properties', idx, 'max']"
                  :label="t('iot.link.productProperty.productProperty.max')"
                  :rules="[
                    {
                      required: true,
                      message:
                        t('common.inputText') +
                        `${t('iot.link.productProperty.productProperty.max')}`,
                    },
                  ]"
                >
                  <a-input
                    v-model:value="arr.max"
                    :placeholder="
                      t('common.inputText') + `${t('iot.link.productProperty.productProperty.max')}`
                    "
                  />
                </a-form-item>
              </a-col>
              <a-col :span="8" v-if="['string', 'DateTime'].indexOf(arr.datatype) !== -1">
                <a-form-item
                  :name="['services', index, 'properties', idx, 'maxlength']"
                  :label="t('iot.link.productProperty.productProperty.maxlength')"
                  :rules="[
                    {
                      required: true,
                      message:
                        t('common.inputText') +
                        `${t('iot.link.productProperty.productProperty.maxlength')}`,
                    },
                  ]"
                >
                  <a-input
                    v-model:value="arr.maxlength"
                    :placeholder="
                      t('common.inputText') +
                      `${t('iot.link.productProperty.productProperty.maxlength')}`
                    "
                  />
                </a-form-item>
              </a-col>
            </a-row>
            <a-row>
              <a-col :span="8">
                <a-form-item
                  :name="['services', index, 'properties', idx, 'method']"
                  :label="t('iot.link.productProperty.productProperty.method')"
                  :rules="[
                    {
                      required: true,
                      message:
                        t('common.inputText') +
                        `${t('iot.link.productProperty.productProperty.method')}`,
                    },
                  ]"
                >
                  <a-radio-group v-model:value="arr.method" button-style="solid">
                    <a-radio-button
                      :value="data.key"
                      :key="data.key"
                      v-for="data in getDictList('ACCESS_MODE')"
                      >{{ data.name }}</a-radio-button
                    >
                  </a-radio-group>
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item
                  :name="['services', index, 'properties', idx, 'required']"
                  :label="t('iot.link.productProperty.productProperty.required')"
                  :rules="[
                    {
                      required: true,
                      message:
                        t('common.inputText') +
                        `${t('iot.link.productProperty.productProperty.required')}`,
                    },
                  ]"
                >
                  <a-radio-group v-model:value="arr.required" button-style="solid">
                    <a-radio-button
                      :value="data.key"
                      :key="data.key"
                      v-for="data in getDictList('LINK_PRODUCT_SERVICE_REQUIRED')"
                      >{{ data.name }}</a-radio-button
                    >
                  </a-radio-group>
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item
                  :name="['services', index, 'properties', idx, 'step']"
                  :label="t('iot.link.productProperty.productProperty.step')"
                  :rules="[
                    {
                      required: true,
                      message:
                        t('common.inputText') +
                        `${t('iot.link.productProperty.productProperty.step')}`,
                    },
                  ]"
                >
                  <a-input
                    v-model:value="arr.step"
                    :placeholder="
                      t('common.inputText') +
                      `${t('iot.link.productProperty.productProperty.step')}`
                    "
                  />
                </a-form-item>
              </a-col>
            </a-row>
            <a-row>
              <a-col :span="8">
                <a-form-item
                  :name="['services', index, 'properties', idx, 'unit']"
                  :label="t('iot.link.productProperty.productProperty.unit')"
                  :rules="[
                    {
                      required: true,
                      message:
                        t('common.inputText') +
                        `${t('iot.link.productProperty.productProperty.unit')}`,
                    },
                  ]"
                >
                  <a-input
                    v-model:value="arr.unit"
                    :placeholder="
                      t('common.inputText') +
                      `${t('iot.link.productProperty.productProperty.unit')}`
                    "
                  />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item
                  :name="['services', index, 'properties', idx, 'remark']"
                  :label="t('iot.link.productProperty.productProperty.remark')"
                  :rules="[
                    {
                      required: false,
                      message:
                        t('common.inputText') +
                        `${t('iot.link.productProperty.productProperty.remark')}`,
                    },
                  ]"
                >
                  <a-input
                    v-model:value="arr.remark"
                    :placeholder="
                      t('common.inputText') +
                      `${t('iot.link.productProperty.productProperty.remark')}`
                    "
                  />
                </a-form-item>
              </a-col>
            </a-row>
          </a-card>
          <a-card
            hoverable
            :title="t('iot.link.product.product.quick.serviceCommands')"
            v-for="(arr, idx) in item.commands"
          >
            <template #extra>
              <MinusCircleOutlined
                @click="delProductServicePropertiesOrCommands(index, 'commands', idx)"
                v-if="item.commands.length > 1 && idx != item.commands.length - 1"
              />
              <PlusCircleOutlined
                @click="addProductServiceCommands(index)"
                v-if="idx == item.commands.length - 1"
              />
            </template>
            <a-row>
              <a-col :span="8">
                <a-form-item
                  :name="['services', index, 'commands', idx, 'commandCode']"
                  :label="t('iot.link.productCommand.productCommand.commandCode')"
                  :rules="[
                    {
                      required: true,
                      message:
                        t('common.inputText') +
                        `${t('iot.link.productCommand.productCommand.commandCode')}`,
                    },
                  ]"
                >
                  <a-input
                    v-model:value="arr.commandCode"
                    :placeholder="
                      t('common.inputText') +
                      `${t('iot.link.productCommand.productCommand.commandCode')}`
                    "
                  />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item
                  :name="['services', index, 'commands', idx, 'commandName']"
                  :label="t('iot.link.productCommand.productCommand.commandName')"
                  :rules="[
                    {
                      required: true,
                      message:
                        t('common.inputText') +
                        `${t('iot.link.productCommand.productCommand.commandName')}`,
                    },
                  ]"
                >
                  <a-input
                    v-model:value="arr.commandName"
                    :placeholder="
                      t('common.inputText') +
                      `${t('iot.link.productCommand.productCommand.commandName')}`
                    "
                  />
                </a-form-item>
              </a-col>
            </a-row>
            <a-row>
              <a-col :span="8">
                <a-form-item
                  :name="['services', index, 'commands', idx, 'description']"
                  :label="t('iot.link.productCommand.productCommand.description')"
                  :rules="[
                    {
                      required: false,
                      message:
                        t('common.inputText') +
                        `${t('iot.link.productCommand.productCommand.description')}`,
                    },
                  ]"
                >
                  <a-input
                    v-model:value="arr.description"
                    :placeholder="
                      t('common.inputText') +
                      `${t('iot.link.productCommand.productCommand.description')}`
                    "
                  />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item
                  :name="['services', index, 'commands', idx, 'remark']"
                  :label="t('iot.link.productCommand.productCommand.remark')"
                  :rules="[
                    {
                      required: false,
                      message:
                        t('common.inputText') +
                        `${t('iot.link.productCommand.productCommand.remark')}`,
                    },
                  ]"
                >
                  <a-input
                    v-model:value="arr.remark"
                    :placeholder="
                      t('common.inputText') +
                      `${t('iot.link.productCommand.productCommand.remark')}`
                    "
                  />
                </a-form-item>
              </a-col>
            </a-row>
            <a-card
              hoverable
              :title="t('iot.link.product.product.quick.serviceRequestCommand')"
              v-for="(col, num) in arr.requests"
            >
              <template #extra>
                <MinusCircleOutlined
                  @click="delProductServiceCommandsRequestsOrResponses(index, 'requests', idx, num)"
                  v-if="arr.requests.length > 1 && num != arr.requests.length - 1"
                />
                <PlusCircleOutlined
                  @click="addProductServiceCommandsRequests(index, idx)"
                  v-if="num == arr.requests.length - 1"
                />
              </template>
              <a-row>
                <a-col :span="8">
                  <a-form-item
                    :name="['services', index, 'commands', idx, 'requests', num, 'parameterCode']"
                    :label="t('iot.link.productCommandRequest.productCommandRequest.parameterCode')"
                    :rules="[
                      {
                        required: true,
                        message:
                          t('common.inputText') +
                          `${t(
                            'iot.link.productCommandRequest.productCommandRequest.parameterCode',
                          )}`,
                      },
                    ]"
                  >
                    <a-input
                      v-model:value="col.parameterCode"
                      :placeholder="
                        t('common.inputText') +
                        `${t('iot.link.productCommandRequest.productCommandRequest.parameterCode')}`
                      "
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8">
                  <a-form-item
                    :name="['services', index, 'commands', idx, 'requests', num, 'parameterName']"
                    :label="t('iot.link.productCommandRequest.productCommandRequest.parameterName')"
                    :rules="[
                      {
                        required: true,
                        message:
                          t('common.inputText') +
                          `${t(
                            'iot.link.productCommandRequest.productCommandRequest.parameterName',
                          )}`,
                      },
                    ]"
                  >
                    <a-input
                      v-model:value="col.parameterName"
                      :placeholder="
                        t('common.inputText') +
                        `${t('iot.link.productCommandRequest.productCommandRequest.parameterName')}`
                      "
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8">
                  <a-form-item
                    :name="[
                      'services',
                      index,
                      'commands',
                      idx,
                      'requests',
                      num,
                      'parameterDescription',
                    ]"
                    :label="
                      t('iot.link.productCommandRequest.productCommandRequest.parameterDescription')
                    "
                    :rules="[
                      {
                        required: false,
                        message:
                          t('common.inputText') +
                          `${t(
                            'iot.link.productCommandRequest.productCommandRequest.parameterDescription',
                          )}`,
                      },
                    ]"
                  >
                    <a-input
                      v-model:value="col.parameterDescription"
                      :placeholder="
                        t('common.inputText') +
                        `${t(
                          'iot.link.productCommandRequest.productCommandRequest.parameterDescription',
                        )}`
                      "
                    />
                  </a-form-item>
                </a-col>
              </a-row>
              <a-row>
                <a-col :span="8">
                  <a-form-item
                    :name="['services', index, 'commands', idx, 'requests', num, 'datatype']"
                    :label="t('iot.link.productCommandRequest.productCommandRequest.datatype')"
                    :rules="[
                      {
                        required: true,
                        message:
                          t('common.chooseText') +
                          `${t('iot.link.productCommandRequest.productCommandRequest.datatype')}`,
                      },
                    ]"
                  >
                    <a-select
                      v-model:value="col.datatype"
                      :options="getDictList('LINK_PRODUCT_SERVICE_PROPERTY_DATA_TYPE')"
                      :fieldNames="dictFieldNames"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8">
                  <a-form-item
                    :name="['services', index, 'commands', idx, 'requests', num, 'required']"
                    :label="t('iot.link.productCommandRequest.productCommandRequest.required')"
                    :rules="[
                      {
                        required: true,
                        message:
                          t('common.inputText') +
                          `${t('iot.link.productCommandRequest.productCommandRequest.required')}`,
                      },
                    ]"
                  >
                    <a-radio-group v-model:value="col.required" button-style="solid">
                      <a-radio-button
                        :value="data.key"
                        :key="data.key"
                        v-for="data in getDictList('LINK_PRODUCT_SERVICE_REQUIRED')"
                        >{{ data.name }}</a-radio-button
                      >
                    </a-radio-group>
                  </a-form-item>
                </a-col>
                <a-col :span="8">
                  <a-form-item
                    :name="['services', index, 'commands', idx, 'requests', num, 'step']"
                    :label="t('iot.link.productCommandRequest.productCommandRequest.step')"
                    :rules="[
                      {
                        required: true,
                        message:
                          t('common.inputText') +
                          `${t('iot.link.productCommandRequest.productCommandRequest.step')}`,
                      },
                    ]"
                  >
                    <a-input
                      v-model:value="col.step"
                      :placeholder="
                        t('common.inputText') +
                        `${t('iot.link.productCommandRequest.productCommandRequest.step')}`
                      "
                    />
                  </a-form-item>
                </a-col>
              </a-row>
              <a-row>
                <a-col :span="8">
                  <a-form-item
                    :name="['services', index, 'commands', idx, 'requests', num, 'enumlist']"
                    :label="t('iot.link.productCommandRequest.productCommandRequest.enumlist')"
                    :rules="[
                      {
                        required: true,
                        message:
                          t('common.inputText') +
                          `${t('iot.link.productCommandRequest.productCommandRequest.enumlist')}`,
                      },
                    ]"
                  >
                    <a-input
                      v-model:value="col.enumlist"
                      :placeholder="
                        t('common.inputText') +
                        `${t('iot.link.productCommandRequest.productCommandRequest.enumlist')}`
                      "
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8" v-if="['int', 'decimal'].indexOf(col.datatype) !== -1">
                  <a-form-item
                    :name="['services', index, 'commands', idx, 'requests', num, 'min']"
                    :label="t('iot.link.productCommandRequest.productCommandRequest.min')"
                    :rules="[
                      {
                        required: true,
                        message:
                          t('common.inputText') +
                          `${t('iot.link.productCommandRequest.productCommandRequest.min')}`,
                      },
                    ]"
                  >
                    <a-input
                      v-model:value="col.min"
                      :placeholder="
                        t('common.inputText') +
                        `${t('iot.link.productCommandRequest.productCommandRequest.min')}`
                      "
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8" v-if="['int', 'decimal'].indexOf(col.datatype) !== -1">
                  <a-form-item
                    :name="['services', index, 'commands', idx, 'requests', num, 'max']"
                    :label="t('iot.link.productCommandRequest.productCommandRequest.max')"
                    :rules="[
                      {
                        required: true,
                        message:
                          t('common.inputText') +
                          `${t('iot.link.productCommandRequest.productCommandRequest.max')}`,
                      },
                    ]"
                  >
                    <a-input
                      v-model:value="col.max"
                      :placeholder="
                        t('common.inputText') +
                        `${t('iot.link.productCommandRequest.productCommandRequest.max')}`
                      "
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8" v-if="['string'].indexOf(col.datatype) !== -1">
                  <a-form-item
                    :name="['services', index, 'commands', idx, 'requests', num, 'maxlength']"
                    :label="t('iot.link.productCommandRequest.productCommandRequest.maxlength')"
                    :rules="[
                      {
                        required: true,
                        message:
                          t('common.inputText') +
                          `${t('iot.link.productCommandRequest.productCommandRequest.maxlength')}`,
                      },
                    ]"
                  >
                    <a-input
                      v-model:value="col.maxlength"
                      :placeholder="
                        t('common.inputText') +
                        `${t('iot.link.productCommandRequest.productCommandRequest.maxlength')}`
                      "
                    />
                  </a-form-item>
                </a-col>
              </a-row>
              <a-row>
                <a-col :span="8">
                  <a-form-item
                    :name="['services', index, 'commands', idx, 'requests', num, 'unit']"
                    :label="t('iot.link.productCommandRequest.productCommandRequest.unit')"
                    :rules="[
                      {
                        required: true,
                        message:
                          t('common.inputText') +
                          `${t('iot.link.productCommandRequest.productCommandRequest.unit')}`,
                      },
                    ]"
                  >
                    <a-input
                      v-model:value="col.unit"
                      :placeholder="
                        t('common.inputText') +
                        `${t('iot.link.productCommandRequest.productCommandRequest.unit')}`
                      "
                    />
                  </a-form-item>
                </a-col>

                <a-col :span="8">
                  <a-form-item
                    :name="['services', index, 'commands', idx, 'requests', num, 'remark']"
                    :label="t('iot.link.productCommandRequest.productCommandRequest.remark')"
                    :rules="[
                      {
                        required: false,
                        message:
                          t('common.inputText') +
                          `${t('iot.link.productCommandRequest.productCommandRequest.remark')}`,
                      },
                    ]"
                  >
                    <a-input
                      v-model:value="col.remark"
                      :placeholder="
                        t('common.inputText') +
                        `${t('iot.link.productCommandRequest.productCommandRequest.remark')}`
                      "
                    />
                  </a-form-item>
                </a-col>
              </a-row>
            </a-card>
            <a-card
              hoverable
              :title="t('iot.link.product.product.quick.serviceResponseCommand')"
              v-for="(col, num) in arr.responses"
            >
              <template #extra>
                <MinusCircleOutlined
                  @click="
                    delProductServiceCommandsRequestsOrResponses(index, 'responses', idx, num)
                  "
                  v-if="arr.responses.length > 1 && num != arr.responses.length - 1"
                />
                <PlusCircleOutlined
                  @click="addProductServiceCommandsResponses(index, idx)"
                  v-if="num == arr.responses.length - 1"
                />
              </template>
              <a-row>
                <a-col :span="8">
                  <a-form-item
                    :name="['services', index, 'commands', idx, 'responses', num, 'parameterCode']"
                    :label="
                      t('iot.link.productCommandResponse.productCommandResponse.parameterCode')
                    "
                    :rules="[
                      {
                        required: true,
                        message:
                          t('common.inputText') +
                          `${t(
                            'iot.link.productCommandResponse.productCommandResponse.parameterCode',
                          )}`,
                      },
                    ]"
                  >
                    <a-input
                      v-model:value="col.parameterCode"
                      :placeholder="
                        t('common.inputText') +
                        `${t(
                          'iot.link.productCommandResponse.productCommandResponse.parameterCode',
                        )}`
                      "
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8">
                  <a-form-item
                    :name="['services', index, 'commands', idx, 'responses', num, 'parameterName']"
                    :label="
                      t('iot.link.productCommandResponse.productCommandResponse.parameterName')
                    "
                    :rules="[
                      {
                        required: true,
                        message:
                          t('common.inputText') +
                          `${t(
                            'iot.link.productCommandResponse.productCommandResponse.parameterName',
                          )}`,
                      },
                    ]"
                  >
                    <a-input
                      v-model:value="col.parameterName"
                      :placeholder="
                        t('common.inputText') +
                        `${t(
                          'iot.link.productCommandResponse.productCommandResponse.parameterName',
                        )}`
                      "
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8">
                  <a-form-item
                    :name="[
                      'services',
                      index,
                      'commands',
                      idx,
                      'responses',
                      num,
                      'parameterDescription',
                    ]"
                    :label="
                      t(
                        'iot.link.productCommandResponse.productCommandResponse.parameterDescription',
                      )
                    "
                    :rules="[
                      {
                        required: false,
                        message:
                          t('common.inputText') +
                          `${t(
                            'iot.link.productCommandResponse.productCommandResponse.parameterDescription',
                          )}`,
                      },
                    ]"
                  >
                    <a-input
                      v-model:value="col.parameterDescription"
                      :placeholder="
                        t('common.inputText') +
                        `${t(
                          'iot.link.productCommandResponse.productCommandResponse.parameterDescription',
                        )}`
                      "
                    />
                  </a-form-item>
                </a-col>
              </a-row>
              <a-row>
                <a-col :span="8">
                  <a-form-item
                    :name="['services', index, 'commands', idx, 'responses', num, 'parameterCode']"
                    :label="
                      t('iot.link.productCommandResponse.productCommandResponse.parameterCode')
                    "
                    :rules="[
                      {
                        required: true,
                        message:
                          t('common.inputText') +
                          `${t(
                            'iot.link.productCommandResponse.productCommandResponse.parameterCode',
                          )}`,
                      },
                    ]"
                  >
                    <a-input
                      v-model:value="col.parameterCode"
                      :placeholder="
                        t('common.inputText') +
                        `${t(
                          'iot.link.productCommandResponse.productCommandResponse.parameterCode',
                        )}`
                      "
                    />
                  </a-form-item>
                </a-col>
              </a-row>
              <a-row>
                <a-col :span="8">
                  <a-form-item
                    :name="['services', index, 'commands', idx, 'responses', num, 'datatype']"
                    :label="t('iot.link.productCommandResponse.productCommandResponse.datatype')"
                    :rules="[
                      {
                        required: true,
                        message:
                          t('common.chooseText') +
                          `${t('iot.link.productCommandResponse.productCommandResponse.datatype')}`,
                      },
                    ]"
                  >
                    <a-select
                      v-model:value="col.datatype"
                      :options="getDictList('LINK_PRODUCT_SERVICE_PROPERTY_DATA_TYPE')"
                      :fieldNames="dictFieldNames"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8">
                  <a-form-item
                    :name="['services', index, 'commands', idx, 'responses', num, 'required']"
                    :label="t('iot.link.productCommandResponse.productCommandResponse.required')"
                    :rules="[
                      {
                        required: true,
                        message:
                          t('common.inputText') +
                          `${t('iot.link.productCommandResponse.productCommandResponse.required')}`,
                      },
                    ]"
                  >
                    <a-radio-group v-model:value="col.required" button-style="solid">
                      <a-radio-button
                        :value="data.key"
                        :key="data.key"
                        v-for="data in getDictList('LINK_PRODUCT_SERVICE_REQUIRED')"
                        >{{ data.name }}</a-radio-button
                      >
                    </a-radio-group>
                  </a-form-item>
                </a-col>
                <a-col :span="8">
                  <a-form-item
                    :name="['services', index, 'commands', idx, 'responses', num, 'step']"
                    :label="t('iot.link.productCommandResponse.productCommandResponse.step')"
                    :rules="[
                      {
                        required: true,
                        message:
                          t('common.inputText') +
                          `${t('iot.link.productCommandResponse.productCommandResponse.step')}`,
                      },
                    ]"
                  >
                    <a-input
                      v-model:value="col.step"
                      :placeholder="
                        t('common.inputText') +
                        `${t('iot.link.productCommandResponse.productCommandResponse.step')}`
                      "
                    />
                  </a-form-item>
                </a-col>
              </a-row>
              <a-row>
                <a-col :span="8">
                  <a-form-item
                    :name="['services', index, 'commands', idx, 'responses', num, 'enumlist']"
                    :label="t('iot.link.productCommandResponse.productCommandResponse.enumlist')"
                    :rules="[
                      {
                        required: true,
                        message:
                          t('common.inputText') +
                          `${t('iot.link.productCommandResponse.productCommandResponse.enumlist')}`,
                      },
                    ]"
                  >
                    <a-input
                      v-model:value="col.enumlist"
                      :placeholder="
                        t('common.inputText') +
                        `${t('iot.link.productCommandResponse.productCommandResponse.enumlist')}`
                      "
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8" v-if="['int', 'decimal'].indexOf(col.datatype) !== -1">
                  <a-form-item
                    :name="['services', index, 'commands', idx, 'responses', num, 'min']"
                    :label="t('iot.link.productCommandResponse.productCommandResponse.min')"
                    :rules="[
                      {
                        required: true,
                        message:
                          t('common.inputText') +
                          `${t('iot.link.productCommandResponse.productCommandResponse.min')}`,
                      },
                    ]"
                  >
                    <a-input
                      v-model:value="col.min"
                      :placeholder="
                        t('common.inputText') +
                        `${t('iot.link.productCommandResponse.productCommandResponse.min')}`
                      "
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8" v-if="['int', 'decimal'].indexOf(col.datatype) !== -1">
                  <a-form-item
                    :name="['services', index, 'commands', idx, 'responses', num, 'max']"
                    :label="t('iot.link.productCommandResponse.productCommandResponse.max')"
                    :rules="[
                      {
                        required: true,
                        message:
                          t('common.inputText') +
                          `${t('iot.link.productCommandResponse.productCommandResponse.max')}`,
                      },
                    ]"
                  >
                    <a-input
                      v-model:value="col.max"
                      :placeholder="
                        t('common.inputText') +
                        `${t('iot.link.productCommandResponse.productCommandResponse.max')}`
                      "
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8" v-if="['string'].indexOf(col.datatype) !== -1">
                  <a-form-item
                    :name="['services', index, 'commands', idx, 'responses', num, 'maxlength']"
                    :label="t('iot.link.productCommandResponse.productCommandResponse.maxlength')"
                    :rules="[
                      {
                        required: true,
                        message:
                          t('common.inputText') +
                          `${t(
                            'iot.link.productCommandResponse.productCommandResponse.maxlength',
                          )}`,
                      },
                    ]"
                  >
                    <a-input
                      v-model:value="col.maxlength"
                      :placeholder="
                        t('common.inputText') +
                        `${t('iot.link.productCommandResponse.productCommandResponse.maxlength')}`
                      "
                    />
                  </a-form-item>
                </a-col>
              </a-row>
              <a-row>
                <a-col :span="8">
                  <a-form-item
                    :name="['services', index, 'commands', idx, 'responses', num, 'unit']"
                    :label="t('iot.link.productCommandResponse.productCommandResponse.unit')"
                    :rules="[
                      {
                        required: true,
                        message:
                          t('common.inputText') +
                          `${t('iot.link.productCommandResponse.productCommandResponse.unit')}`,
                      },
                    ]"
                  >
                    <a-input
                      v-model:value="col.unit"
                      :placeholder="
                        t('common.inputText') +
                        `${t('iot.link.productCommandResponse.productCommandResponse.unit')}`
                      "
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8">
                  <a-form-item
                    :name="['services', index, 'commands', idx, 'responses', num, 'remark']"
                    :label="t('iot.link.productCommandResponse.productCommandResponse.remark')"
                    :rules="[
                      {
                        required: false,
                        message:
                          t('common.inputText') +
                          `${t('iot.link.productCommandResponse.productCommandResponse.remark')}`,
                      },
                    ]"
                  >
                    <a-input
                      v-model:value="col.remark"
                      :placeholder="
                        t('common.inputText') +
                        `${t('iot.link.productCommandResponse.productCommandResponse.remark')}`
                      "
                    />
                  </a-form-item>
                </a-col>
              </a-row>
            </a-card>
          </a-card>
        </a-card>
        <div class="submit-box">
          <a-button type="primary" html-type="submit">{{ t('common.title.confirm') }}</a-button>
        </div>
      </a-form>
    </div>
    <div class="fix-priview" @click="showData">预览</div>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, ref, reactive, computed, h } from 'vue';
  import { useUserStore } from '/@/store/modules/user';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { PageWrapper } from '/@/components/Page';
  import { Card, Row, Col, Form, Button, Radio, Modal } from 'ant-design-vue';
  import { PlusCircleOutlined, MinusCircleOutlined } from '@ant-design/icons-vue';
  import { useDict } from '/@/components/Dict';
  import { ProductQuickSaveVO } from '/@/api/iot/link/product/model/productModel';
  import { quick } from '/@/api/iot/link/product/product';
  import type { FormInstance } from 'ant-design-vue';
  import { JsonPreview } from '/@/components/CodeEditor';
  const { getDictList } = useDict();
  export default defineComponent({
    name: 'ProductDetail',
    components: {
      [Form.name]: Form,
      [Form.Item.name]: Form.Item,
      ACard: Card,
      ARow: Row,
      ACol: Col,
      AButton: Button,
      PageWrapper,
      PlusCircleOutlined,
      MinusCircleOutlined,
      ARadioGroup: Radio.Group,
      ARadioButton: Radio.Button,
    },
    emits: [],
    setup() {
      // 是否显示密码明文
      const { t } = useI18n();
      const formRef = ref<FormInstance>();
      const { createMessage } = useMessage();
      const userStore = useUserStore();
      const getUserInfo = computed(() => {
        return userStore.getUserInfo;
      });
      const createdOrgId = getUserInfo.value?.baseEmployee?.createdOrgId;
      console.log(createdOrgId, 'createdOrgId');
      // 字典选项字段映射配置
      const dictFieldNames = { label: 'name', value: 'key' };
      const formState = reactive<ProductQuickSaveVO>({
        appId: '',
        deviceType: '',
        manufacturerId: '',
        manufacturerName: '',
        model: '',
        // productIdentification: "",
        productName: '',
        protocolType: '',
        remark: '',
        productStatus: '0',
        productType: '',
        dataFormat: 'JSON',
        services: [
          {
            serviceCode: '',
            serviceName: '',
            serviceType: '',
            serviceStatus: '0',
            description: '',
            remark: '',
            properties: [
              {
                createdOrgId: createdOrgId,
                datatype: '',
                description: '',
                enumlist: '',
                icon: '',
                max: '',
                maxlength: '',
                method: '',
                min: '',
                propertyCode: '',
                propertyName: '',
                remark: '',
                required: '1',
                serviceId: null,
                step: '',
                unit: '',
              },
            ],
            commands: [
              {
                commandCode: '',
                commandName: '',
                createdOrgId: createdOrgId,
                description: '',
                remark: '',
                requests: [
                  {
                    commandId: null,
                    createdOrgId: createdOrgId,
                    datatype: '',
                    enumlist: '',
                    max: '',
                    maxlength: '',
                    min: '',
                    parameterCode: '',
                    parameterDescription: '',
                    parameterName: '',
                    remark: '',
                    required: '1',
                    serviceId: null,
                    step: '',
                    unit: '',
                  },
                ],
                responses: [
                  {
                    commandId: null,
                    createdOrgId: createdOrgId,
                    datatype: '',
                    enumlist: '',
                    max: '',
                    maxlength: '',
                    min: '',
                    parameterCode: '',
                    parameterDescription: '',
                    parameterName: '',
                    remark: '',
                    required: '1',
                    serviceId: null,
                    step: '',
                    unit: '',
                  },
                ],
                serviceId: null,
              },
            ],
          },
        ],
      });
      // 删除产品服务
      function delProductService(index: number) {
        formState.services?.splice(index, 1);
      }
      // 添加产品服务
      function addProductService() {
        formState.services?.push({
          serviceCode: '',
          serviceName: '',
          serviceType: '',
          serviceStatus: '0',
          description: '',
          remark: '',
          properties: [
            {
              createdOrgId: createdOrgId,
              datatype: '',
              description: '',
              enumlist: '',
              icon: '',
              max: '',
              maxlength: '',
              method: '',
              min: '',
              propertyCode: '',
              propertyName: '',
              remark: '',
              required: '1',
              serviceId: null,
              step: '',
              unit: '',
            },
          ],
          commands: [
            {
              commandCode: '',
              commandName: '',
              createdOrgId: createdOrgId,
              description: '',
              remark: '',
              requests: [
                {
                  commandId: null,
                  createdOrgId: createdOrgId,
                  datatype: '',
                  enumlist: '',
                  max: '',
                  maxlength: '',
                  min: '',
                  parameterCode: '',
                  parameterDescription: '',
                  parameterName: '',
                  remark: '',
                  required: '1',
                  serviceId: null,
                  step: '',
                  unit: '',
                },
              ],
              responses: [
                {
                  commandId: null,
                  createdOrgId: createdOrgId,
                  datatype: '',
                  enumlist: '',
                  max: '',
                  maxlength: '',
                  min: '',
                  parameterCode: '',
                  parameterDescription: '',
                  parameterName: '',
                  remark: '',
                  required: '1',
                  serviceId: null,
                  step: '',
                  unit: '',
                },
              ],
              serviceId: null,
            },
          ],
        });
      }
      // 删除产品服务属性或者产品服务命令
      function delProductServicePropertiesOrCommands(index: number, type: string, idx: number) {
        formState.services[index][type].splice(idx, 1);
      }
      // 添加产品服务属性
      function addProductServiceProperties(index: number) {
        formState.services[index].properties.push({
          createdOrgId: createdOrgId,
          datatype: '',
          description: '',
          enumlist: '',
          icon: '',
          max: '',
          maxlength: '',
          method: '',
          min: '',
          propertyCode: '',
          propertyName: '',
          remark: '',
          required: '1',
          serviceId: null,
          step: '',
          unit: '',
        });
      }
      // 添加产品服务命令
      function addProductServiceCommands(index: number) {
        formState.services[index].commands.push({
          commandCode: '',
          commandName: '',
          createdOrgId: createdOrgId,
          description: '',
          remark: '',
          requests: [
            {
              commandId: null,
              createdOrgId: createdOrgId,
              datatype: '',
              enumlist: '',
              max: '',
              maxlength: '',
              min: '',
              parameterCode: '',
              parameterDescription: '',
              parameterName: '',
              remark: '',
              required: '1',
              serviceId: null,
              step: '',
              unit: '',
            },
          ],
          responses: [
            {
              commandId: null,
              createdOrgId: createdOrgId,
              datatype: '',
              enumlist: '',
              max: '',
              maxlength: '',
              min: '',
              parameterCode: '',
              parameterDescription: '',
              parameterName: '',
              remark: '',
              required: '1',
              serviceId: null,
              step: '',
              unit: '',
            },
          ],
          serviceId: null,
        });
      }
      // 删除产品服务请求或下发命令
      function delProductServiceCommandsRequestsOrResponses(
        index: number,
        type: string,
        idx: number,
        num: number,
      ) {
        formState.services[index].commands[idx][type].splice(num, 1);
      }
      // 添加产品服务请求命令
      function addProductServiceCommandsRequests(index: number, idx: number) {
        formState.services[index].commands[idx].requests.push({
          commandId: null,
          createdOrgId: createdOrgId,
          datatype: '',
          enumlist: '',
          max: '',
          maxlength: '',
          min: '',
          parameterCode: '',
          parameterDescription: '',
          parameterName: '',
          remark: '',
          required: '1',
          serviceId: null,
          step: '',
          unit: '',
        });
      }
      // 添加产品服务请求命令
      function addProductServiceCommandsResponses(index: number, idx: number) {
        formState.services[index].commands[idx].responses.push({
          commandId: null,
          createdOrgId: createdOrgId,
          datatype: '',
          enumlist: '',
          max: '',
          maxlength: '',
          min: '',
          parameterCode: '',
          parameterDescription: '',
          parameterName: '',
          remark: '',
          required: '1',
          serviceId: null,
          step: '',
          unit: '',
        });
      }
      // 提交表单
      async function finish(values) {
        console.log(values);
        try {
          let res = await quick(values);
          console.log(res);
          if (res.code == 0) {
            createMessage.success(t(`common.tips.confirmSuccess`));
            formRef.value.resetFields();
          } else {
            createMessage.error(res.message || t(`common.tips.confirmFail`));
          }
        } catch (error) {
          console.error('快捷生成失败:', error);
          createMessage.error(error?.message || t(`common.tips.confirmFail`));
        }
      }
      function showData() {
        Modal.confirm({
          title: '',
          width: '800px',
          content: h('div', { style: { maxHeight: '60vh', overflowY: 'auto' } }, [
            h(JsonPreview, { data: formState }),
          ]),
          icon: null,
          okText: t('common.okText'),
          cancelText: t('common.title.copy'),
          okButtonProps: {
            type: 'primary',
          },
          cancelButtonProps: {
            type: 'default',
          },
          onOk() {
            // 点击知道了按钮，关闭弹窗
            return Promise.resolve();
          },
          onCancel() {
            // 点击复制按钮
            const textToCopy = JSON.stringify(formState, null, 2);
            navigator.clipboard
              .writeText(textToCopy)
              .then(() => {
                createMessage.success(t('common.tips.copySuccess'));
              })
              .catch(() => {
                createMessage.error(t('common.tips.copyFail'));
              });
            // 阻止弹窗关闭
            return Promise.reject();
          },
        });
      }
      return {
        t,
        formState,
        getDictList,
        dictFieldNames,
        delProductService,
        addProductService,
        delProductServicePropertiesOrCommands,
        addProductServiceProperties,
        addProductServiceCommands,
        delProductServiceCommandsRequestsOrResponses,
        addProductServiceCommandsRequests,
        addProductServiceCommandsResponses,
        finish,
        formRef,
        showData,
      };
    },
  });
</script>
<style lang="less" scope>
  .quick-form {
    background: #fff;
    padding: 16px;
    margin: 16px;

    .ant-card-bordered {
      border: 1px solid #000;
    }
  }

  .ant-card + .ant-card {
    margin-top: 24px;
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
  }</style
>../../../../../api/iot/link/product/model/productModel../../../../../api/iot/link/product/product
