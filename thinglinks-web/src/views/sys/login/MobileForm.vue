<template>
  <template v-if="getShow">
    <LoginFormTitle class="enter-x" />
    <Form ref="formRef" :model="formData" :rules="getFormRules" class="p-4 enter-x">
      <FormItem name="grantType" v-show="false">
        <Input v-model:value="formData.grantType" :hidden="true" size="large" />
      </FormItem>
      <FormItem class="enter-x" name="mobile">
        <Input
          v-model:value="formData.mobile"
          :placeholder="t('sys.login.mobile')"
          class="fix-auto-fill"
          size="large"
        />
      </FormItem>
      <FormItem class="enter-x" name="code">
        <CountdownInput
          v-model:value="formData.code"
          :placeholder="t('sys.login.smsCode')"
          :sendCodeApi="handleSendCode"
          class="fix-auto-fill"
          size="large"
        />
      </FormItem>
      <FormItem class="enter-x" name="sliderVerified" :rules="formRules.sliderVerified">
        <BasicDragVerify
          ref="dragVerifyRef"
          v-model:value="formData.sliderVerified"
          :height="40"
          :width="dragVerifyWidth"
          :wrap-style="{ borderRadius: '5px', width: '100%' }"
          :bar-style="{ borderRadius: '5px' }"
          :action-style="{ borderRadius: '5px' }"
          :text="t('component.verify.dragText')"
          :success-text="t('component.verify.successText')"
          @success="handleSliderSuccess"
        />
      </FormItem>

      <FormItem class="enter-x">
        <Button :loading="loading" block size="large" type="primary" @click="handleLogin">
          {{ t('sys.login.loginButton') }}
        </Button>
        <Button block class="mt-4" size="large" @click="handleBackLogin">
          {{ t('sys.login.backSignIn') }}
        </Button>
      </FormItem>
    </Form>
  </template>
</template>
<script lang="ts" setup>
  import {
    computed,
    nextTick,
    onMounted,
    onUnmounted,
    reactive,
    ref,
    toRaw,
    unref,
    watch,
  } from 'vue';
  import { Button, Form, Input } from 'ant-design-vue';
  import { CountdownInput } from '/@/components/CountDown';
  import LoginFormTitle from './LoginFormTitle.vue';
  import { BasicDragVerify, DragVerifyActionType } from '/@/components/Verify';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useUserStore } from '/@/store/modules/user';
  import { LoginStateEnum, useFormRules, useFormValid, useLoginState } from './useLogin';
  import { sendSmsCode } from '/@/api/thinglinks/common/oauth';
  import { MsgTemplateCodeEnum } from '/@/enums/commonEnum';
  import { useMessage } from '/@/hooks/web/useMessage';

  const FormItem = Form.Item;
  const { t } = useI18n();
  const userStore = useUserStore();
  const { handleBackLogin, getLoginState } = useLoginState();
  const { createMessage } = useMessage();
  const { getFormRules } = useFormRules();

  const formRef = ref();
  const dragVerifyRef = ref<DragVerifyActionType | null>(null);
  const loading = ref(false);
  const dragVerifyWidth = ref<number | string>(220);

  const formData = reactive({
    mobile: '',
    grantType: 'MOBILE',
    code: '',
    sliderVerified: false,
  });

  const { validForm } = useFormValid(formRef);
  const formRules = computed(() => unref(getFormRules));

  const getShow = computed(() => unref(getLoginState) === LoginStateEnum.MOBILE);

  // 更新滑块验证宽度
  function updateDragVerifyWidth() {
    nextTick(() => {
      const dragVerifyEl = document.querySelector('.drag-verify') as HTMLElement;
      if (dragVerifyEl) {
        dragVerifyWidth.value = dragVerifyEl.offsetWidth;
      }
    });
  }

  // 窗口大小变化监听
  function handleResize() {
    updateDragVerifyWidth();
  }

  onMounted(() => {
    // 获取 drag-verify 元素的宽度并赋值
    updateDragVerifyWidth();
    // 监听窗口大小变化
    window.addEventListener('resize', handleResize);
  });

  onUnmounted(() => {
    // 移除窗口大小变化监听
    window.removeEventListener('resize', handleResize);
  });

  // 监听表单显示状态，当显示时更新滑块验证宽度
  watch(
    () => getShow.value,
    (show) => {
      if (show) {
        updateDragVerifyWidth();
      }
    },
    { immediate: true },
  );

  // 滑块验证成功回调
  function handleSliderSuccess() {
    formData.sliderVerified = true;
    // 清除滑块验证字段的验证错误提示
    nextTick(() => {
      const form = unref(formRef);
      if (form) {
        form.clearValidate(['sliderVerified']);
      }
    });
  }

  async function handleSendCode() {
    const form = unref(formRef);
    try {
      const data = await form.validateFields(['mobile']);
      // templateCode 参数需要 提前在消息模板中配置
      await sendSmsCode(data.mobile, MsgTemplateCodeEnum.MOBILE_LOGIN);
      return true;
    } catch (e) {
      return false;
    }
  }

  async function handleLogin() {
    const data = await validForm();
    if (!data) return;
    try {
      loading.value = true;
      const userInfo = await userStore.login(toRaw(data));
      if (userInfo) {
        createMessage.success(t('sys.login.loginSuccessDesc'));
      } else {
        // 登录失败时重置滑块验证
        formData.sliderVerified = false;
        dragVerifyRef.value?.resume();
      }
    } catch (error) {
      // 登录失败时重置滑块验证
      formData.sliderVerified = false;
      dragVerifyRef.value?.resume();
    } finally {
      loading.value = false;
    }
  }
</script>
<style lang="less" scoped>
  // 确保滑块验证在不同屏幕尺寸下正常显示
  @media (max-width: 576px) {
    :deep(.drag-verify) {
      font-size: 12px;
    }
  }

  :deep(.drag-verify) {
    width: 100%;
  }
</style>
