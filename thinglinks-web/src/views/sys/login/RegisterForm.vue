<template>
  <template v-if="getShow">
    <LoginFormTitle class="enter-x" />
    <Form ref="formRef" :model="formData" :rules="getFormRules" class="p-4 enter-x">
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
  import LoginFormTitle from './LoginFormTitle.vue';
  import { Button, Checkbox, Form, Input } from 'ant-design-vue';
  import { StrengthMeter } from '/@/components/StrengthMeter';
  import { CountdownInput } from '/@/components/CountDown';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { LoginStateEnum, useFormRules, useFormValid, useLoginState } from './useLogin';
  import { registerByMobile, sendSmsCode } from '/@/api/thinglinks/common/oauth';
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
    mobile: '',
    code: '',
    policy: false,
  });

  const { getFormRules } = useFormRules(formData);
  const { validForm } = useFormValid(formRef);

  const getShow = computed(() => unref(getLoginState) === LoginStateEnum.REGISTER);

  async function handleSendCode() {
    const form = unref(formRef);
    try {
      const data = await form.validateFields(['mobile']);
      // templateCode 参数需要 提前在消息模板中配置
      await sendSmsCode(data.mobile, MsgTemplateCodeEnum.REGISTER_SMS);
      return true;
    } catch (e) {
      return false;
    }
  }

  async function handleRegister() {
    const data = await validForm();
    if (!data) return;
    try {
      data.key = MsgTemplateCodeEnum.REGISTER_SMS;
      const username = await registerByMobile(data);
      createMessage.success(`注册成功,请使用${username}登录系统`);
      handleBackLogin();
    } catch (error) {
      createMessage.error('注册失败');
    }
  }
</script>
