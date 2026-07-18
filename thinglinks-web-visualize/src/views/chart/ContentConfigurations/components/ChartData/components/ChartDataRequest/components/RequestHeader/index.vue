<template>
  <n-space vertical>
    <div style="width: 600px">
      <n-tabs v-model:value="requestContentType" type="segment" size="small">
        <n-tab :name="RequestContentTypeEnum.DEFAULT" tab="普通请求"> </n-tab>
        <n-tab :name="RequestContentTypeEnum.SQL" tab="SQL 请求"> </n-tab>
      </n-tabs>
    </div>
    <div v-show="requestContentType === RequestContentTypeEnum.DEFAULT">
      <n-tabs type="line" animated v-model:value="tabValue">
        <n-tab v-for="item in RequestParamsTypeEnum" :key="item" :name="item" :tab="item"> {{ item }} </n-tab>
      </n-tabs>

      <!-- 各个页面 -->
      <div class="go-mt-3">
        <div v-if="tabValue !== RequestParamsTypeEnum.BODY">
          <request-header-table :target="requestParams[tabValue]" @update="updateRequestParams"></request-header-table>
        </div>

        <!-- 选择了 body -->
        <div v-else>
          <n-radio-group v-model:value="requestParamsBodyType" name="radiogroup">
            <n-space>
              <n-radio v-for="bodyEnum in RequestBodyEnumList" :key="bodyEnum" :value="bodyEnum">
                {{ bodyEnum }}
              </n-radio>
            </n-space>
          </n-radio-group>

          <!-- 为 none 时 -->
          <n-card class="go-mt-3 go-pb-3" v-if="requestParamsBodyType === RequestBodyEnum['NONE']">
            <n-text depth="3">该接口没有 Body 体</n-text>
          </n-card>

          <!-- 具有对象属性时 -->
          <template
            v-else-if="
              requestParamsBodyType === RequestBodyEnum['FORM_DATA'] ||
              requestParamsBodyType === RequestBodyEnum['X_WWW_FORM_URLENCODED']
            "
          >
            <request-header-table
              class="go-mt-3"
              :target="requestParams[RequestParamsTypeEnum.BODY][requestParamsBodyType]"
              @update="updateRequestBodyTable"
            ></request-header-table>
          </template>

          <!-- json  -->
          <template v-else-if="requestParamsBodyType === RequestBodyEnum['JSON']">
            <monaco-editor
              v-model:modelValue="requestParams[RequestParamsTypeEnum.BODY][requestParamsBodyType]"
              width="600px"
              height="200px"
              language="json"
            />
          </template>

          <!-- xml  -->
          <template v-else-if="requestParamsBodyType === RequestBodyEnum['XML']">
            <monaco-editor
              v-model:modelValue="requestParams[RequestParamsTypeEnum.BODY][requestParamsBodyType]"
              width="600px"
              height="200px"
              language="html"
            />
          </template>
        </div>
      </div>
    </div>
    <div v-show="requestContentType === RequestContentTypeEnum.SQL">
      <template v-if="requestHttpType === RequestHttpEnum.GET">
        <n-text>SQL 类型不支持 Get 请求，请使用其它方式</n-text>
      </template>
      <template v-else>
        <n-tag type="warning">需要后台提供专门处理 sql 的接口</n-tag>
        <setting-item-box name="键名">
          <n-tag type="primary" :bordered="false" style="width: 40px; font-size: 16px"> sql </n-tag>
        </setting-item-box>
        <setting-item-box name="键值">
          <monaco-editor v-model:modelValue="requestSQLContent['sql']" width="600px" height="200px" language="sql" />
        </setting-item-box>
      </template>
    </div>
  </n-space>
</template>

<script setup lang="ts">
import { ref, toRefs, PropType } from 'vue'
import MonacoEditor from '@/components/Pages/MonacoEditor/index.vue'
import { RequestHeaderTable } from '../RequestHeaderTable/index'
import { SettingItemBox, SettingItem } from '@/components/Pages/ChartItemSetting'
import { useTargetData } from '@/views/chart/ContentConfigurations/components/hooks/useTargetData.hook'
import { RequestConfigType } from '@/store/modules/chartEditStore/chartEditStore.d'
import {
  RequestParamsTypeEnum,
  RequestContentTypeEnum,
  RequestParamsObjType,
  RequestBodyEnumList,
  RequestBodyEnum,
  RequestHttpEnum
} from '@/enums/httpEnum'

const props = defineProps({
  targetDataRequest: Object as PropType<RequestConfigType>
})

const { requestHttpType, requestContentType, requestSQLContent, requestParams, requestParamsBodyType } = toRefs(
  props.targetDataRequest as RequestConfigType
)

const tabValue = ref<RequestParamsTypeEnum>(RequestParamsTypeEnum.PARAMS)

// 更新参数表格数据
const updateRequestParams = (paramsObj: RequestParamsObjType) => {
  if (tabValue.value !== RequestParamsTypeEnum.BODY) {
    requestParams.value[tabValue.value] = paramsObj
  }
}

// 更新参数表格数据
const updateRequestBodyTable = (paramsObj: RequestParamsObjType) => {
  if (
    tabValue.value === RequestParamsTypeEnum.BODY &&
    // 仅有两种类型有 body
    (requestParamsBodyType.value === RequestBodyEnum.FORM_DATA ||
      requestParamsBodyType.value === RequestBodyEnum.X_WWW_FORM_URLENCODED)
  ) {
    requestParams.value[RequestParamsTypeEnum.BODY][requestParamsBodyType.value] = paramsObj
  }
}
</script>

<style lang="scss" scoped>
.select-type {
  width: 300px;
}
</style>
