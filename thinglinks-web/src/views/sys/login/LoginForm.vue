<template>
  <template v-if="getShow">
    <LoginFormTitle v-show="getShow" class="enter-x" />
    <Form
      v-show="getShow"
      ref="formRef"
      :model="formData"
      class="p-4 enter-x w-full max-w-full"
      @keypress.enter="handleLogin"
    >
      <FormItem name="grantType" v-show="false">
        <Input v-model:value="formData.grantType" :hidden="true" size="large" />
      </FormItem>
      <FormItem name="key" v-show="false">
        <Input v-model:value="formData.key" :hidden="true" size="large" />
      </FormItem>
      <FormItem class="enter-x" name="username" :rules="formRules.username">
        <Input
          v-model:value="formData.username"
          :placeholder="t('sys.login.accountPlaceholder')"
          class="fix-auto-fill"
          size="large"
          name="username"
          autocomplete="username"
        />
      </FormItem>
      <FormItem class="enter-x" name="password" :rules="formRules.password">
        <InputPassword
          v-model:value="formData.password"
          :placeholder="t('sys.login.passwordPlaceholder')"
          size="large"
          name="password"
          autocomplete="current-password"
          visibilityToggle
        />
      </FormItem>

      <FormItem
        v-if="globSetting.showCaptcha === 'true'"
        class="enter-x"
        name="code"
        :rules="formRules.code"
      >
        <ARow :gutter="0" class="w-full" style="flex-wrap: nowrap">
          <ACol :flex="1" class="code-input-col">
            <Input
              v-model:value="formData.code"
              :placeholder="t('sys.login.codePlaceholder')"
              size="large"
              class="w-full code-input-merged"
            />
          </ACol>
          <ACol :flex="0" class="code-image-col">
            <img
              v-if="formState.captchaSrc"
              :src="formState.captchaSrc"
              alt="captcha"
              class="code-image-merged cursor-pointer"
              @click="buildCaptcha"
            />
            <img
              v-else
              alt="captcha"
              class="code-image-merged cursor-pointer"
              src="../../../assets/images/captcha_404.png"
              @click="buildCaptcha"
            />
          </ACol>
        </ARow>
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
      <FormItem>
        <!-- No logic, you need to deal with it yourself -->
        <Checkbox v-model:checked="rememberMe" size="small" class="remember-me">
          {{ t('sys.login.rememberMe') }}
        </Checkbox>
      </FormItem>
      <FormItem class="enter-x">
        <Button :loading="formState.loading" block size="large" type="primary" @click="handleLogin">
          {{ t('sys.login.loginButton') }}
        </Button>
        <!-- <Button size="large" class="mt-4 enter-x" block @click="handleRegister">
          {{ t('sys.login.registerButton') }}
        </Button> -->
      </FormItem>
      <ARow class="enter-x" :gutter="[8, 8]">
        <ACol :xs="24" :sm="8" class="operation text-center">
          <span
            class="cursor-pointer hover:text-primary"
            @click="setLoginState(LoginStateEnum.MOBILE)"
          >
            {{ t('sys.login.mobileSignInFormTitle') }}
          </span>
        </ACol>
        <ACol :xs="0" :sm="1" class="operation text-center hidden sm:block">|</ACol>
        <ACol :xs="24" :sm="8" class="operation text-center">
          <span
            class="cursor-pointer hover:text-primary"
            @click="setLoginState(LoginStateEnum.REGISTER)"
          >
            {{ t('sys.login.mobileSignUpFormTitle') }}
          </span>
        </ACol>
        <ACol :xs="0" :sm="1" class="operation text-center hidden sm:block">|</ACol>
        <ACol :xs="24" :sm="6" class="operation text-center">
          <span
            class="cursor-pointer hover:text-primary"
            @click="setLoginState(LoginStateEnum.REGISTER_EMAIL)"
          >
            {{ t('sys.login.emailSignUpFormTitle') }}
          </span>
        </ACol>
      </ARow>

      <Divider class="enter-x">{{ t('sys.login.otherSignIn') }}</Divider>

      <div :class="`${prefixCls}-sign-in-way`" class="flex justify-evenly enter-x flex-wrap gap-2">
        <img
          src="../../../assets/images/login/WeChat.png"
          :alt="t('sys.login.wechat')"
          class="w-8 h-8 sm:w-10 sm:h-10 cursor-pointer hover:opacity-80 transition-opacity"
        />
        <img
          src="../../../assets/images/login/Twitter.png"
          :alt="t('sys.login.twitter')"
          class="w-8 h-8 sm:w-10 sm:h-10 cursor-pointer hover:opacity-80 transition-opacity"
        />
        <img
          src="../../../assets/images/login/Alipay.png"
          :alt="t('sys.login.alipay')"
          class="w-8 h-8 sm:w-10 sm:h-10 cursor-pointer hover:opacity-80 transition-opacity"
        />
        <img
          src="../../../assets/images/login/SinaMicroblog.png"
          :alt="t('sys.login.weibo')"
          class="w-8 h-8 sm:w-10 sm:h-10 cursor-pointer hover:opacity-80 transition-opacity"
        />
      </div>
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
  import { isDevMode } from '/@/utils/env';

  import { Button, Checkbox, Col, Divider, Form, Input, Row } from 'ant-design-vue';
  import LoginFormTitle from './LoginFormTitle.vue';
  import { BasicDragVerify, DragVerifyActionType } from '/@/components/Verify';

  import { useGlobSetting } from '/@/hooks/setting';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';

  import { useUserStore } from '/@/store/modules/user';
  import { LoginStateEnum, useFormRules, useFormValid, useLoginState } from './useLogin';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { randomNum } from '/@/utils';
  import { MultiTenantTypeEnum } from '/@/enums/biz/tenant';

  import { loadCaptcha } from '/@/api/thinglinks/common/oauth';

  const ACol = Col;
  const ARow = Row;
  const FormItem = Form.Item;
  const InputPassword = Input.Password;
  const { t } = useI18n();
  const { createMessage } = useMessage();
  const { prefixCls } = useDesign('login');
  const userStore = useUserStore();

  const globSetting = useGlobSetting();
  const { setLoginState, getLoginState } = useLoginState();
  const { getFormRules } = useFormRules();

  const formRef = ref();
  const dragVerifyRef = ref<DragVerifyActionType | null>(null);
  const REMEMBER_KEY = 'THINGLINKS_LOGIN_REMEMBER';
  const rememberMe = ref(loadRememberFlag());
  const devFlag = ref<string>('');
  const dragVerifyWidth = ref<number | string>(220);

  function loadRememberFlag(): boolean {
    try {
      const raw = localStorage.getItem(REMEMBER_KEY);
      if (!raw) return false;
      return JSON.parse(raw)?.remember === true;
    } catch {
      return false;
    }
  }
  function loadRememberedUsername(): string {
    try {
      const raw = localStorage.getItem(REMEMBER_KEY);
      if (!raw) return '';
      const data = JSON.parse(raw);
      return data?.remember ? (data.username || '') : '';
    } catch {
      return '';
    }
  }
  function saveRemember(remember: boolean, username: string) {
    try {
      if (remember) {
        localStorage.setItem(REMEMBER_KEY, JSON.stringify({ remember: true, username }));
      } else {
        localStorage.removeItem(REMEMBER_KEY);
      }
    } catch {
      /* ignore quota */
    }
  }

  if (isDevMode()) {
    devFlag.value = '(dev)';
  }
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
    buildCaptcha();
    // 获取 drag-verify 元素的宽度并赋值
    updateDragVerifyWidth();
    // 监听窗口大小变化
    window.addEventListener('resize', handleResize);
  });

  onUnmounted(() => {
    // 移除窗口大小变化监听
    window.removeEventListener('resize', handleResize);
  });

  const formData = reactive({
    username: loadRememberedUsername(),
    password: '',
    code: '',
    grantType: globSetting.showCaptcha === 'true' ? 'CAPTCHA' : 'PASSWORD',
    key: randomNum(24, 16),
    sliderVerified: false,
  });
  const formState = reactive({
    loading: false,
    captchaSrc: '',
    isMultiTenant: globSetting.multiTenantType !== MultiTenantTypeEnum.NONE,
    showCaptcha: globSetting.showCaptcha === 'true',
  });

  const { validForm } = useFormValid(formRef);
  const formRules = computed(() => unref(getFormRules));

  const getShow = computed(() => unref(getLoginState) === LoginStateEnum.LOGIN);

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

  // 生成验证码
  async function buildCaptcha() {
    try {
      formData.code = '';

      const res = await loadCaptcha(formData.key).catch((e) => {
        const { createMessage } = useMessage();
        if (e.toString().indexOf('429') !== -1) {
          createMessage.error(t('sys.login.captchaTooFrequent'));
        } else {
          createMessage.error(t('sys.login.captchaLoadFailed'));
        }
        formState.captchaSrc = '';
      });
      if (res.byteLength <= 100) {
        const { createMessage } = useMessage();
        createMessage.error(t('sys.login.systemMaintenance'));
        return '';
      }
      formState.captchaSrc =
        'data:image/png;base64,' +
        btoa(new Uint8Array(res).reduce((data, byte) => data + String.fromCharCode(byte), ''));
    } catch (error) {
      console.error(error);
      formState.captchaSrc = '';
      return '';
    }
  }

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

  async function handleLogin() {
    const data = await validForm();
    if (!data) return;
    try {
      formState.loading = true;
      const userInfo = await userStore.login(toRaw(data));
      if (userInfo) {
        saveRemember(rememberMe.value, formData.username);
        createMessage.success(t('sys.login.loginSuccessDesc'));
      } else {
        await buildCaptcha();
        // 登录失败时重置滑块验证
        formData.sliderVerified = false;
        dragVerifyRef.value?.resume();
      }
    } catch (error) {
      await buildCaptcha();
      // 登录失败时重置滑块验证
      formData.sliderVerified = false;
      dragVerifyRef.value?.resume();
    } finally {
      formState.loading = false;
    }
  }
</script>
<style lang="less" scoped>
  .code-image {
    object-fit: contain;
    height: 40px;
    border-radius: 5px;
  }

  .code-input-col {
    flex: 1 1 auto;
    min-width: 0;
  }

  .code-image-col {
    flex: 0 0 auto;
    display: flex;
    align-items: center;
  }

  .code-input-merged {
    border-radius: 5px 0 0 5px !important;
  }

  .code-image-merged {
    object-fit: cover;
    height: 40px;
    width: auto;
    min-width: 100px;
    max-width: 120px;
    border-radius: 0 5px 5px 0 !important;
    border: 1px solid #d9d9d9;
    border-left: none;
  }

  @media (max-width: 576px) {
    .code-image,
    .code-image-merged {
      height: 38px;
      min-width: 80px;
      max-width: 100px;
    }
  }
</style>
