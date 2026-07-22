<template>
  <template v-if="getShow">
    <LoginFormTitle class="enter-x" />
    <Form ref="formRef" :model="formData" :rules="getFormRules" class="p-4 enter-x">
      <FormItem class="enter-x" name="email">
        <Input
          v-model:value="formData.email"
          class="fix-auto-fill"
          placeholder="邮箱"
          size="large"
        />
      </FormItem>
      <FormItem class="enter-x" name="code">
        <CountdownInput
          v-model:value="formData.code"
          :sendCodeApi="handleSendCode"
          class="fix-auto-fill"
          placeholder="验证码"
          size="large"
        />
      </FormItem>
      <FormItem class="enter-x" name="password">
        <StrengthMeter
          v-model:value="formData.password"
          :placeholder="t('sys.login.password')"
          size="large"
        />
      </FormItem>
      <FormItem class="enter-x" name="confirmPassword">
        <InputPassword
          v-model:value="formData.confirmPassword"
          :placeholder="t('sys.login.confirmPassword')"
          size="large"
          visibilityToggle
        />
      </FormItem>

      <FormItem class="enter-x" name="policy">
        <!-- No logic, you need to deal with it yourself -->
        <Checkbox v-model:checked="formData.policy" size="small">
          {{ t('sys.login.policy') }}
        </Checkbox>
      </FormItem>

      <Button
        :loading="loading"
        block
        class="enter-x"
        size="large"
        type="primary"
        @click="handleRegister"
      >
        {{ t('sys.login.registerButton') }}
      </Button>
      <Button block class="mt-4 enter-x" size="large" @click="handleBackLogin">
        {{ t('sys.login.backSignIn') }}
      </Button>
    </Form>
  </template>
</template>
<script lang="ts" setup>
  import { computed, reactive, ref, unref } from 'vue';
  import { Button, Checkbox, Form, Input } from 'ant-design-vue';
  import type { RuleObject } from 'ant-design-vue/lib/form/interface';
  import LoginFormTitle from './LoginFormTitle.vue';
  import { StrengthMeter } from '/@/components/StrengthMeter';
  import { CountdownInput } from '/@/components/CountDown';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { LoginStateEnum, useFormValid, useLoginState } from './useLogin';
  import { registerByEmail, sendEmailCode } from '/@/api/thinglinks/common/oauth';
  import { MsgTemplateCodeEnum } from '/@/enums/commonEnum';

  const FormItem = Form.Item;
  const InputPassword = Input.Password;
  const { t } = useI18n();
  const { createMessage } = useMessage();
  const { handleBackLogin, getLoginState } = useLoginState();

  const formRef = ref();
  const loading = ref(false);

  const formData = reactive({
    password: '',
    confirmPassword: '',
    email: '',
    code: '',
    policy: false,
  });

  const getFormRules = {
    code: {
      required: true,
      message: '请填写验证码',
      trigger: 'change',
    },
    email: {
      required: true,
      message: '请填写邮箱',
      trigger: 'change',
    },
    password: {
      required: true,
      message: '请填写密码',
      trigger: 'change',
    },
    confirmPassword: {
      required: true,
      message: '请填写确认密码',
      trigger: 'change',
    },
    policy: {
      required: true,
      message: '请勾选隐私政策',
      validator: async (_: RuleObject, value: boolean) => {
        return !value ? Promise.reject(t('sys.login.policyPlaceholder')) : Promise.resolve();
      },
      trigger: 'change',
    },
  };
  const { validForm } = useFormValid(formRef);

  const getShow = computed(() => unref(getLoginState) === LoginStateEnum.REGISTER_EMAIL);

  async function handleSendCode() {
    const form = unref(formRef);
    try {
      const data = await form.validateFields(['email', 'policy', 'password', 'confirmPassword']);
      // templateCode 参数需要 提前在消息模板中配置
      await sendEmailCode(data.email, MsgTemplateCodeEnum.REGISTER_EMAIL);
      return true;
    } catch (e) {
      return false;
    }
  }

  async function handleRegister() {
    const data = await validForm();
    if (!data) return;
    try {
      data.key = MsgTemplateCodeEnum.REGISTER_EMAIL;
      const username = await registerByEmail(data);
      createMessage.success(`注册成功,请使用${username}登录系统`);
      handleBackLogin();
    } catch (error) {
      createMessage.error('注册失败');
    }
  }
</script>
