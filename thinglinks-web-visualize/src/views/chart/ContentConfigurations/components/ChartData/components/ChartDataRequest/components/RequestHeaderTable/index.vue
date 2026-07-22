<template>
  <n-table class="go-request-header-table-box" :single-line="false" size="small">
    <thead>
      <tr>
        <th></th>
        <th>Key</th>
        <th>Value</th>
        <th>操作</th>
        <th>结果</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="(item, index) in tableArray.content" :key="index">
        <td>
          {{ index + 1 }}
        </td>
        <td>
          <n-input v-model:value="item.key" :disabled="editDisabled" type="text" size="small" @blur="blur" />
        </td>
        <td>
          <n-input v-model:value="item.value" :disabled="editDisabled" type="text" size="small" @blur="blur" />
        </td>
        <td>
          <div style="width: 80px">
            <n-button class="go-ml-2" type="primary" size="small" ghost :disabled="editDisabled" @click="add(index)"
              >+</n-button
            >
            <n-button
              class="go-ml-2"
              type="warning"
              size="small"
              ghost
              :disabled="index === 0 && editDisabled"
              @click="remove(index)"
            >
              -
            </n-button>
          </div>
        </td>
        <td>
          <n-button v-if="item.error" class="go-ml-2" text type="error"> 格式错误 </n-button>
          <n-button v-else class="go-ml-2" text type="primary"> 格式通过 </n-button>
        </td>
      </tr>
    </tbody>
  </n-table>
</template>

<script setup lang="ts">
import { PropType, reactive, ref, toRefs, watch } from 'vue'
import { RequestParamsObjType } from '@/enums/httpEnum'

const emits = defineEmits(['update'])

const props = defineProps({
  target: {
    type: Object as PropType<RequestParamsObjType>,
    required: true,
    default: () => {}
  },
  editDisabled: {
    type: Boolean,
    required: false,
    default: false
  }
})

// 错误标识
const error = ref(false)

// 默认表格
const defaultItem = {
  key: '',
  value: '',
  error: false
}
const tableArray = reactive<{
  content: typeof defaultItem[]
}>({ content: [] })

// 失焦
const blur = () => {
  let successNum = 0
  tableArray.content.forEach(item => {
    if ((item.key !== '' && item.value == '') || (item.key === '' && item.value !== '')) {
      // 错误
      item.error = true
    } else {
      // 正确
      successNum++
      item.error = false
    }
  })
  // 验证是否全部通过
  if (successNum == tableArray.content.length) {
    // 转换数据成对象
    const updateObj: any = {}
    tableArray.content.forEach((e: typeof defaultItem) => {
      if (e.key) updateObj[e.key] = e.value
    })
    emits('update', updateObj)
  }
}

// 新增
const add = (index: number) => {
  tableArray.content.splice(index + 1, 0, {
    key: '',
    value: '',
    error: false
  })
}

// 减少
const remove = (index: number) => {
  if (tableArray.content.length !== 1) {
    tableArray.content.splice(index, 1)
  }
  blur()
}

// 监听选项
watch(
  () => props.target,
  (target: RequestParamsObjType) => {
    tableArray.content = []
    for (const k in target) {
      tableArray.content.push({
        key: k,
        value: target[k],
        error: false
      })
    }
    // 默认值
    if (!tableArray.content.length) tableArray.content = [JSON.parse(JSON.stringify(defaultItem))]
  },
  {
    immediate: true,
    deep: true
  }
)
</script>

<style lang="scss" scoped>
@include go('request-header-table-box') {
  background-color: rgba(0, 0, 0, 0);

  :deep(th),
  :deep(td) {
    background-color: rgba(0, 0, 0, 0);
  }
}
</style>
