<template>
  <Header :class="getHeaderClass" :style="getHeaderStyle">
    <!-- left start -->
    <div :class="`${prefixCls}-left`">
      <!-- logo -->
      <AppLogo
        v-if="getShowHeaderLogo || getIsMobile"
        :class="`${prefixCls}-logo ${getMenuType}-logo`"
        :style="getLogoWidth"
        :theme="getHeaderTheme"
      />
      <LayoutTrigger
        v-if="
          (getShowContent && getShowHeaderTrigger && !getSplit && !getIsMixSidebar) || getIsMobile
        "
        :sider="false"
        :theme="getHeaderTheme"
      />
      <!-- 面包屑 -->
      <LayoutBreadcrumb v-if="getShowContent && getShowBread" :theme="getHeaderTheme" />
    </div>
    <!-- left end -->

    <!-- menu start -->
    <div v-if="getShowTopMenu && !getIsMobile" :class="`${prefixCls}-menu`">
      <LayoutMenu
        :isHorizontal="true"
        :menuMode="getMenuMode"
        :splitType="getSplitType"
        :theme="getHeaderTheme"
      />
    </div>
    <!-- menu-end -->

    <!-- action  -->
    <div :class="`${prefixCls}-action`">
      <div :class="`${prefixCls}-action__item tips-item`">
        <!-- 样式引用： https://zhuanlan.zhihu.com/p/372052468 -->
        <div v-if="globSetting.tips" class="ad">
          <i class="iconfont">&#xe633;</i>
          <p :title="globSetting.tips" class="content">
            <span>{{ globSetting.tips }} </span>
          </p>
        </div>
      </div>
      <AppSwitchDropdown
        :class="`${prefixCls}-action__item ${prefixCls}-action__app-switch`"
        :theme="getHeaderTheme"
      />

      <AppSearch v-if="getShowSearch" :class="`${prefixCls}-action__item `" />

      <ErrorAction v-if="getUseErrorHandle" :class="`${prefixCls}-action__item error-action`" />

      <Notify v-if="getShowNotice" :class="`${prefixCls}-action__item notify-item`" />

      <FullScreen v-if="getShowFullScreen" :class="`${prefixCls}-action__item fullscreen-item`" />

      <AppLocalePicker
        v-if="getShowLocalePicker"
        :class="`${prefixCls}-action__item`"
        :reload="true"
        :showText="false"
      />

      <UserDropDown :class="`${prefixCls}-action__userinfo`" :theme="getHeaderTheme" />

      <SettingDrawer
        v-if="getShowSetting"
        :class="`${prefixCls}-action__item ${prefixCls}-action__setting`"
      />
    </div>
  </Header>
</template>
<script lang="ts">
  import { computed, defineComponent, unref } from 'vue';

  import { propTypes } from '/@/utils/propTypes';

  import { Layout } from 'ant-design-vue';
  import { AppLocalePicker, AppLogo, AppSearch } from '/@/components/Application';
  import LayoutMenu from '../menu/index.vue';
  import LayoutTrigger from '../trigger/index.vue';

  import { useGlobSetting } from '/@/hooks/setting';
  import { useHeaderSetting } from '/@/hooks/setting/useHeaderSetting';
  import { useMenuSetting } from '/@/hooks/setting/useMenuSetting';
  import { useRootSetting } from '/@/hooks/setting/useRootSetting';

  import { MenuModeEnum, MenuSplitTyeEnum } from '/@/enums/menuEnum';
  import { SettingButtonPositionEnum } from '/@/enums/appEnum';

  import {
    AppSwitchDropdown,
    ErrorAction,
    FullScreen,
    LayoutBreadcrumb,
    Notify,
    UserDropDown,
  } from './components';
  import { useAppInject } from '/@/hooks/web/useAppInject';
  import { useDesign } from '/@/hooks/web/useDesign';

  import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';
  import { useLocale } from '/@/locales/useLocale';

  export default defineComponent({
    name: 'LayoutHeader',
    components: {
      Header: Layout.Header,
      AppLogo,
      LayoutTrigger,
      LayoutBreadcrumb,
      LayoutMenu,
      UserDropDown,
      AppLocalePicker,
      FullScreen,
      Notify,
      AppSearch,
      ErrorAction,
      AppSwitchDropdown,
      SettingDrawer: createAsyncComponent(() => import('/@/layouts/default/setting/index.vue'), {
        loading: true,
      }),
    },
    props: {
      fixed: propTypes.bool,
    },
    setup(props) {
      const globSetting = useGlobSetting();
      const { prefixCls } = useDesign('layout-header');
      const {
        getShowTopMenu,
        getShowHeaderTrigger,
        getSplit,
        getIsMixMode,
        getMenuWidth,
        getIsMixSidebar,
        getMenuType,
        getRealWidth,
      } = useMenuSetting();
      const { getUseErrorHandle, getShowSettingButton, getSettingButtonPosition } =
        useRootSetting();

      const {
        getHeaderTheme,
        getShowFullScreen,
        getShowNotice,
        getShowContent,
        getShowBread,
        getShowHeaderLogo,
        getShowHeader,
        getShowSearch,
      } = useHeaderSetting();

      const { getShowLocalePicker } = useLocale();

      const { getIsMobile } = useAppInject();

      const getHeaderStyle = computed(() => {
        if (!unref(getIsMixMode)) {
          return {};
        }

        const left = `${unref(getRealWidth)}px`;
        return {
          left,
          // 加1像素是因为 vben 作者为何会在 .thinglinks-layout-header样式中增加1像素的左偏移： margin-left: -1px;
          width: `calc(100% - ${left} + 1px)`,
          transition: 'all 0.2s',
        };
      });

      const getHeaderClass = computed(() => {
        const theme = unref(getHeaderTheme);
        return [
          prefixCls,
          {
            [`${prefixCls}--fixed`]: props.fixed,
            [`${prefixCls}--mobile`]: unref(getIsMobile),
            [`${prefixCls}--${theme}`]: theme,
          },
        ];
      });

      const getShowSetting = computed(() => {
        if (!unref(getShowSettingButton)) {
          return false;
        }
        const settingButtonPosition = unref(getSettingButtonPosition);

        if (settingButtonPosition === SettingButtonPositionEnum.AUTO) {
          return unref(getShowHeader);
        }
        return settingButtonPosition === SettingButtonPositionEnum.HEADER;
      });

      const getLogoWidth = computed(() => {
        if (!unref(getIsMixMode) || unref(getIsMobile)) {
          return {};
        }
        const width = unref(getMenuWidth) < 180 ? 180 : unref(getMenuWidth);
        return { width: `${width}px` };
      });

      const getSplitType = computed(() => {
        return unref(getSplit) ? MenuSplitTyeEnum.TOP : MenuSplitTyeEnum.NONE;
      });

      const getMenuMode = computed(() => {
        return unref(getSplit) ? MenuModeEnum.HORIZONTAL : null;
      });

      return {
        prefixCls,
        getHeaderClass,
        getShowHeaderLogo,
        getHeaderTheme,
        getShowHeaderTrigger,
        getIsMobile,
        getShowBread,
        getShowContent,
        getSplitType,
        getSplit,
        getMenuMode,
        getShowTopMenu,
        getShowLocalePicker,
        getMenuType,
        getShowFullScreen,
        getShowNotice,
        getUseErrorHandle,
        getLogoWidth,
        getIsMixSidebar,
        getShowSettingButton,
        getShowSetting,
        globSetting,
        getShowSearch,
        getHeaderStyle,
      };
    },
  });
</script>
<style lang="less">
  @import './index.less';

  @prefix-cls: ~'@{namespace}-layout-header';

  .@{prefix-cls} {
    .iconfont {
      font-family: icon-horn !important;
      font-size: 16px;
      font-style: normal;
      -webkit-font-smoothing: antialiased;
      -webkit-text-stroke-width: 0.2px;
      -moz-osx-font-smoothing: grayscale;
    }

    .ad {
      // 核心代码
      @keyframes marquee {
        0% {
          transform: translateX(0);
        }

        100% {
          transform: translateX(-100%);
        }
      }
      // @keyframes blink {
      //   50% {
      //     color: transparent;
      //   }
      // }

      max-width: 400px;
      background-color: #fff;
      border-radius: 8px;
      box-sizing: border-box;
      height: 40px;
      line-height: 40px;
      padding: 0 20px;
      display: flex;
      align-items: center;
      justify-content: flex-start;
      font-size: 16px;
      cursor: pointer;
      box-shadow: 2px 1px 8px 1px rgb(228 232 235);

      i {
        color: #ff6146;
        font-size: 20px;
        margin-right: 10px;
      }

      .content {
        flex: 1;
        overflow: hidden;
        height: 100%;
        margin: auto;

        span {
          display: block;
          width: auto;
          white-space: nowrap;
          color: red;
          // animation: marquee 10s linear infinite, blink 1.5s linear infinite;
          animation: marquee 10s linear infinite;

          // background-image: -webkit-linear-gradient(bottom, red, #fd8403, yellow);
          // -webkit-background-clip: text;
          // background-clip: text;
          // -webkit-text-fill-color: transparent;
          // padding-left: 105%;
          // padding-right: 120%;
          &:hover {
            animation-play-state: paused;
          }
        }
      }
    }

    &--dark {
      .ad {
        background: transparent;
        box-shadow: 2px 1px 8px 1px @border-color-base;
      }
    }
  }
</style>
