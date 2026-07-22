<template>
  <div>
    <slot></slot>
  </div>
</template>
<script>
  // 自定义组件
  import { Form } from 'ant-design-vue';
  export default {
    setup() {
      const formItemContext = Form.useInjectFormItemContext();
      console.log('formItemContext', formItemContext);
    },
  };
</script>
