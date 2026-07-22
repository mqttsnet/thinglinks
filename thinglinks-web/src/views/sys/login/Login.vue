<template>
  <div :class="prefixCls" class="relative w-full h-full px-4 wrap">
    <div class="flex items-center absolute right-4 top-4">
      <AppDarkModeToggle v-if="!sessionTimeout" class="enter-x mr-2" />
      <AppLocalePicker
        v-if="!sessionTimeout && showLocale"
        :show-text="false"
        class="text-white enter-x xl:text-gray-600"
      />
    </div>

    <div class="container relative h-full py-2 mx-auto sm:px-10">
      <div class="flex h-full login-content">
        <div class="flex w-full h-full py-5 xl:h-auto xl:py-0 xl:my-0 xl:w-6/12 login">
          <div
            :class="[
              `${prefixCls}-form`,
              'relative w-full px-5 py-8 mx-auto my-auto rounded-md xl:ml-16 xl:bg-transparent',
              'sm:px-8 xl:p-4 xl:shadow-none sm:w-3/4 lg:w-2/4 enter-x login-form-wrap',
            ]"
          >
            <div class="login-wrap">
              <LoginForm />
              <ForgetPasswordForm />
              <RegisterForm />
              <MobileForm />
              <EmailRegisterForm />
            </div>
          </div>
        </div>
      </div>
      <div class="copyright" style="bottom: 0; position: absolute; margin: 0 auto; width: 100%">
        <div style="padding: 20px 0">
          <a href="https://beian.miit.gov.cn" target="_blank">
            <img src="../../../assets/images/gongan.png" /><span
              >Copyright © 2019 至今 mqttsnet All Rights Reserved.</span
            >
          </a>
          |
          <a
            href="http://beian.miit.gov.cn"
            style="display: inline-block; text-decoration: none; height: 20px; line-height: 20px"
            target="_blank"
          >
            <img src="../../../assets/images/gongan.png" style="float: left" />
            <p
              style="
                float: left;
                height: 20px;
                line-height: 20px;
                margin: 0px 0px 0px 5px;
                color: #939393;
              "
              >豫ICP备2021021629号</p
            >
          </a>
        </div>
      </div>
    </div>
  </div>
</template>
<script lang="ts" setup>
  import { AppDarkModeToggle, AppLocalePicker } from '/@/components/Application';
  import LoginForm from './LoginForm.vue';
  import ForgetPasswordForm from './ForgetPasswordForm.vue';
  import RegisterForm from './RegisterForm.vue';
  import MobileForm from './MobileForm.vue';
  import EmailRegisterForm from './EmailRegisterForm.vue';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useLocaleStore } from '/@/store/modules/locale';

  defineProps({
    sessionTimeout: {
      type: Boolean,
    },
  });

  const { prefixCls } = useDesign('login');
  const localeStore = useLocaleStore();
  const showLocale = localeStore.getShowPicker;
</script>
<style lang="less">
  @prefix-cls: ~'@{namespace}-login';
  @logo-prefix-cls: ~'@{namespace}-app-logo';
  @countdown-prefix-cls: ~'@{namespace}-countdown-input';
  @dark-bg: #293146;

  html[data-theme='dark'] {
    .@{prefix-cls} {
      background-color: @dark-bg;

      .ant-input,
      .ant-input-password {
        background-color: #232a3b;
      }

      .ant-btn:not(.ant-btn-link):not(.ant-btn-primary) {
        border: 1px solid #4a5569;
      }

      &-form {
        background: transparent !important;
      }

      .app-iconify {
        color: #fff;
      }
    }

    .login-wrap {
      width: 100%;
      box-sizing: border-box;
      padding: 20px 15px;
      border-radius: 10px;
      backdrop-filter: blur(20px);
      background-color: rgba(90, 90, 90, 0.6);
      margin: 0;

      .ant-form {
        .ant-form-item {
          display: flex;
          align-items: center;
          width: 100%;
          margin-bottom: 14px;

          .ant-form-item-label {
            width: 80px;
            font-weight: 700;

            .label {
              color: white;
            }
          }

          .ant-input,
          .ant-input-lg,
          .ant-input-password {
            width: 100%;
            border-radius: 5px;
            backdrop-filter: blur(20px);
            background-color: rgba(90, 90, 90, 0.5);
          }

          .code-input {
            width: 68%;
            border-radius: 5px 0 0 5px;
            @media (max-width: @screen-sm) {
              width: 50%;
              min-width: 50%;
            }
          }

          .code-image {
            border-radius: 0 5px 5px 0;
          }

          .remember-me {
            margin-left: 15px;
          }
        }

        .ant-btn {
          border-radius: 20px;
        }

        .ant-row .operation {
          text-align: center;
        }
      }
    }

    input.fix-auto-fill,
    .fix-auto-fill input {
      -webkit-text-fill-color: #c9d1d9 !important;
      box-shadow: inherit !important;
    }

    .wrap {
      background-image: url('../../../assets/images/login/darkLoginBg.png');
      background-size: cover;
    }

    .login-content {
      display: flex;
      justify-content: flex-end;

      .login {
        display: flex;
        justify-content: flex-end;

        .login-form-wrap {
          margin-right: 0;
        }
      }
    }
  }

  .@{prefix-cls} {
    min-height: 100%;
    overflow: hidden;

    @media (max-width: @screen-xl) {
      background-color: #293146;
    }

    .@{prefix-cls}-form {
      width: 70%;
    }

    &::before {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background-position: 100%;
      background-repeat: no-repeat;
      background-size: auto 100%;
      content: '';
      @media (max-width: @screen-xl) {
        display: none;
      }
    }

    .@{logo-prefix-cls} {
      position: absolute;
      top: 12px;
      height: 30px;

      &__title {
        font-size: 16px;
        color: #fff;
      }

      img {
        width: 32px;
      }
    }

    .container {
      .@{logo-prefix-cls} {
        display: flex;
        width: 70%;
        height: 80px;

        &__title {
          font-size: 24px;
          color: #fff;
        }

        img {
          width: 48px;
        }
      }
    }

    .code-input {
      display: inline-block;
      vertical-align: middle;
    }

    .code-image {
      display: inline-block;
      width: 115px;
      height: 42px;
      vertical-align: top;
      cursor: pointer;
    }

    &-sign-in-way {
      .anticon {
        font-size: 22px;
        color: #888;
        cursor: pointer;

        &:hover {
          color: @primary-color;
        }
      }
    }

    input:not([type='checkbox']) {
      @media (max-width: @screen-xl) {
        min-width: 320px;
      }

      @media (max-width: @screen-lg) {
        min-width: 260px;
      }

      @media (max-width: @screen-md) {
        min-width: 240px;
      }

      @media (max-width: @screen-sm) {
        min-width: 160px;
      }
    }

    .@{countdown-prefix-cls} input {
      min-width: unset;
    }

    .ant-divider-inner-text {
      font-size: 12px;
      color: @text-color-secondary;
    }
  }

  .copyright {
    color: #939393;
    text-align: center;

    a {
      display: inline-block;
      margin-right: 10px;
      margin-left: 10px;
      color: #939393;
      vertical-align: bottom;

      img {
        float: left;
        margin-right: 5px;
      }
    }

    span {
      color: #939393;
      vertical-align: bottom;
    }

    i {
      margin: 0 4px;
    }
  }

  .wrap {
    background-image: url('../../../assets/images/login/loginBg.png');
    background-size: cover;
  }

  .login-content {
    display: flex;
    justify-content: flex-end;

    .login {
      display: flex;
      justify-content: flex-end;

      .login-form-wrap {
        margin-right: 0;
      }
    }
  }

  .login-wrap {
    width: 100%;
    box-sizing: border-box;
    padding: 20px;
    border-radius: 10px;
    backdrop-filter: blur(20px);
    background-color: rgba(255, 255, 255, 0.6);
    margin: 0;

    .ant-form {
      .ant-form-item {
        display: flex;
        align-items: center;
        width: 100%;
        margin-bottom: 14px;

        .ant-form-item-label {
          width: 80px;
          font-weight: 700;
        }

        .ant-input,
        .ant-input-lg,
        .ant-input-password {
          width: 100%;
          border-radius: 5px;
          backdrop-filter: blur(20px);
          background-color: rgba(255, 255, 255, 0.5);
        }

        .code-input {
          width: 68%;
          border-radius: 5px 0 0 5px;

          @media (max-width: @screen-sm) {
            width: 50%;
            min-width: 50%;
          }
        }

        .code-image {
          border-radius: 0 5px 5px 0;
        }

        .remember-me {
          margin-left: 15px;
        }
      }

      .ant-btn {
        border-radius: 20px;
      }

      .ant-row .operation {
        text-align: center;
      }
    }
  }
</style>
