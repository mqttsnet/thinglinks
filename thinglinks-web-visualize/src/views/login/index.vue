<template>
  <!-- 登录 -->
  <div class="go-login-box">
    <div class="go-login-box-bg">
      <aside class="bg-slot"></aside>
      <aside class="bg-img-box">
        <transition-group name="list-complete">
          <template v-for="item in bgList" :key="item">
            <div class="bg-img-box-li list-complete-item">
              <n-collapse-transition :appear="true" :show="showBg">
                <img :src="getImageUrl(item, 'chart/charts')" alt="chart" />
              </n-collapse-transition>
            </div>
          </template>
        </transition-group>
      </aside>
    </div>
    <layout-header>
      <template #left></template>
      <template #right>
        <go-lang-select></go-lang-select>
        <go-theme-select></go-theme-select>
      </template>
    </layout-header>
    <div class="go-login">
      <div class="go-login-carousel">
        <n-carousel
          autoplay
          dot-type="line"
          :interval="Number(carouselInterval)"
        >
          <img
            v-for="(item, i) in carouselImgList"
            :key="i"
            class="go-login-carousel-img"
            :src="getImageUrl(item, 'login')"
            alt="image"
          />
        </n-carousel>
      </div>
      <div class="login-account">
        <div class="login-account-container">
          <n-collapse-transition :appear="true" :show="show">
            <n-card class="login-account-card" :title="'thinglinks 大屏可视化系统'">
              <div class="login-account-top">
                <img
                  class="login-account-top-logo"
                  src="~@/assets/images/login/input.png"
                  alt="展示图片"
                />
              </div>
              <n-form
                ref="formRef"
                label-placement="left"
                size="large"
                :model="formInline"
                :rules="rules"
                @keypress.enter="handleSubmit"
              >
                <n-form-item path="username">
                  <n-input
                    v-model:value="formInline.username"
                    type="text"
                    maxlength="16"
                    :placeholder="$t('global.form_account')"
                  >
                    <template #prefix>
                      <n-icon size="18">
                        <PersonOutlineIcon></PersonOutlineIcon>
                      </n-icon>
                    </template>
                  </n-input>
                </n-form-item>
                <n-form-item path="password">
                  <n-input
                    v-model:value="formInline.password"
                    type="password"
                    maxlength="16"
                    show-password-on="click"
                    :placeholder="$t('global.form_password')"
                  >
                    <template #prefix>
                      <n-icon size="18">
                        <LockClosedOutlineIcon></LockClosedOutlineIcon>
                      </n-icon>
                    </template>
                  </n-input>
                </n-form-item>
                <n-form-item>
                    <div class="flex justify-between">
                      <div class="flex-initial auth-code">
                        <n-input
                        v-model:value="formInline.code"
                        type="text"
                        maxlength="16"
                        :placeholder="'请输入验证码'"
                      >
                        <template #prefix>
                          <n-icon size="18">
                            <PersonOutlineIcon></PersonOutlineIcon>
                          </n-icon>
                        </template>
                      </n-input>
                        <img
                          v-if="authCodeSrc"
                          :src="authCodeSrc"
                          alt="captcha"
                          class="code-image"
                          @click="buildCaptcha"
                        />
                        <img
                          v-else
                          alt="captcha"
                          class="code-image"
                          src="@/assets/images/tips/captcha_404.png"
                          @click="buildCaptcha"
                          />
                      </div>
                    </div>
                  </n-form-item>
                <n-form-item>
                  <n-button
                    type="primary"
                    @click="handleSubmit"
                    size="large"
                    :loading="loading"
                    block
                    >
                      {{ $t('login.form_button') }}
                    </n-button>
                </n-form-item>
              </n-form>
            </n-card>
          </n-collapse-transition>
        </div>
      </div>
    </div>

    <div class="go-login-box-footer">
      <layout-footer></layout-footer>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { reactive, ref, onMounted, onUnmounted } from 'vue'
import shuffle from 'lodash/shuffle'
import { carouselInterval } from '@/settings/designSetting'
import { GoThemeSelect } from '@/components/GoThemeSelect'
import { GoLangSelect } from '@/components/GoLangSelect'
import { LayoutHeader } from '@/layout/components/LayoutHeader'
import { LayoutFooter } from '@/layout/components/LayoutFooter'
import { icon } from '@/plugins'
// api
import { loadCaptcha } from '@/api/thinglinks/common/oauth'
import type { LoginParamVO } from '@/api/thinglinks/common/model/userModel'
// utils
import { randomNum } from '@/utils'
// hooks
import { useGlobSetting } from '@/hooks/setting'
// store
import { useUserStore } from '@/store/modules/user/user'

const { PersonOutlineIcon, LockClosedOutlineIcon } = icon.ionicons5

const formRef = ref()
const loading = ref(false)
const show = ref(false)
const showBg = ref(false)
const userStore = useUserStore()
const globSetting = useGlobSetting()

const t = window['$t']

const authCodeSrc = ref('') // 验证码
const authCodekey = randomNum(24, 16)
let showTimer: ReturnType<typeof setTimeout> | undefined
let showBackgroundTimer: ReturnType<typeof setTimeout> | undefined
onMounted(() => {
  buildCaptcha()
  showTimer = setTimeout(() => {
    show.value = true
  }, 300)
  showBackgroundTimer = setTimeout(() => {
    showBg.value = true
  }, 100)
})

// 生成验证码
const buildCaptcha = async () => {
  try {
    authCodeSrc.value = ''

    let res: ArrayBuffer
    try {
      res = await loadCaptcha(authCodekey)
    } catch (error) {
      if (String(error).includes('429')) {
        window['$message'].error('获取验证码过于频繁，请1分钟后再试')
      } else {
        window['$message'].error('加载验证码失败')
      }
      authCodeSrc.value = ''
      return ''
    }
    if (res.byteLength <= 100) {
      window['$message'].error('系统维护中，请稍微再试~')
      return ''
    }
    authCodeSrc.value =
      'data:image/png;base64,' + btoa(new Uint8Array(res).reduce((data, byte) => data + String.fromCharCode(byte), ''))
  } catch (error) {
    console.error(error)
    authCodeSrc.value = ''
    return ''
  }
}

const formInline = reactive<LoginParamVO>({
  username: '',
  password: '',
  code: '',
  grantType: globSetting.showCaptcha === 'true' ? 'CAPTCHA' : 'PASSWORD',
  key: authCodekey
})

const rules = {
  username: {
    required: true,
    message: t('global.form_account'),
    trigger: 'blur'
  },
  password: {
    required: true,
    message: t('global.form_password'),
    trigger: 'blur'
  },
  code: {
    required: true,
    message: t('global.form_password'),
    trigger: 'blur'
  },
  grantType: {
    required: true
  },
  key: {
    required: true
  }
}

// 定时器
let shuffleTiming: ReturnType<typeof setInterval> | undefined

// 轮播图
const carouselImgList = ['one', 'two', 'three']

// 背景图
const bgList = ref(['bar_y', 'bar_x', 'line_gradient', 'line', 'funnel', 'heatmap', 'map', 'pie', 'radar'])

// 处理url获取
const getImageUrl = (name: string, folder: string) => {
  return new URL(`../../assets/images/${folder}/${name}.png`, import.meta.url).href
}

// 打乱
const shuffleHandle = () => {
  if (shuffleTiming) clearInterval(shuffleTiming)
  shuffleTiming = setInterval(() => {
    bgList.value = shuffle(bgList.value)
  }, carouselInterval)
}

// 点击事件
const handleSubmit = (e: Event) => {
  // e.preventDefault()
  formRef.value.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      try {
        await userStore.login({ ...formInline })
        loading.value = false
      } catch (err) {
        loading.value = false
      }
    } else {
      window['$message'].error(`${t('login.login_message')}!`)
    }
  })
}

onMounted(() => {
  shuffleHandle()
})

onUnmounted(() => {
  if (shuffleTiming) clearInterval(shuffleTiming)
  if (showTimer) clearTimeout(showTimer)
  if (showBackgroundTimer) clearTimeout(showBackgroundTimer)
})
</script>

<style lang="scss" scoped>
$width: 450px;
$go-login-height: 100vh;
$account-img-height: 210px;
$footer-height: 50px;
$carousel-width: 30%;
$carousel-image-height: 60vh;

* {
  box-sizing: border-box;
}

@include go(login-box) {
  height: $go-login-height;
  overflow: hidden;
  @include background-image('background-image');

  &-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 40px;
    height: $--header-height;
  }

  &-divider {
    margin: 0;
    padding-top: 0;
  }

  @include go(login) {
    z-index: 2;
    display: flex;
    justify-content: space-around;
    align-items: center;
    margin-top: -$--header-height;
    height: $go-login-height;
    width: 100vw;

    &-carousel {
      width: $carousel-width;
      margin-top: 100px;
      min-width: 500px;

      &-img {
        display: block;
        margin: 0 auto;
        height: $carousel-image-height;
      }
    }

    .login-account {
      display: flex;
      flex-direction: column;
      margin: 0 160px;

      &-container {
        width: $width;
      }

      &-card {
        @extend .go-background-filter;
        @include fetch-bg-color('filter-color');
        box-shadow: 0 0 20px 5px rgba(40, 40, 40, 0.3);
        text-align: center;
      }

      &-top {
        padding-top: 10px;
        text-align: center;
        height: $account-img-height;
        margin-bottom: 20px;
      }
    }
  }

  &-footer {
    z-index: 2;
    position: fixed;
    width: 100%;
    bottom: 0;
  }

  &-bg {
    z-index: 0;
    position: fixed;
    display: flex;
    justify-content: space-around;
    align-items: center;
    width: 100vw;
    height: 100vh;
    background: url('@/assets/images/login/login-bg.png') no-repeat 0 -120px;

    .bg-slot {
      width: $carousel-width;
    }

    .bg-img-box {
      position: relative;
      display: flex;
      flex-wrap: wrap;
      width: 770px;
      margin-right: -20px;

      &-li {
        img {
          margin-right: 20px;
          margin-top: 20px;
          width: 230px;
          border-radius: 2 * $--border-radius-base;
          opacity: 0.9;
        }
      }
    }
  }
}

@media only screen and (max-width: 1200px) {

  .bg-img-box,
  .bg-slot,
  .go-login-carousel {
    display: none !important;
  }

  .go-login-box-footer {
    position: relative;
  }
}

.auth-code {
  display: flex;

  .n-input {
    min-width: 90%;
  }

  img {
    width: 105px;
    height: 40px;
    border-radius: 0 3px 3px 0;
  }
}</style>
