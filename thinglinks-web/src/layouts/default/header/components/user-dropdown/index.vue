<template>
  <div>
    <Dropdown :overlayClassName="`${prefixCls}-dropdown-overlay`" placement="bottomLeft">
      <span :class="[prefixCls, `${prefixCls}--${theme}`]" class="flex">
        <AvatarPreview
          :errorTxt="getUserInfo?.nickName?.substring(0, 1)"
          :fileId="getUserInfo?.avatarId"
          :style="{ 'margin-right': '0.5rem' }"
        />
        <span :class="`${prefixCls}__info hidden md:block`">
          <span :class="`${prefixCls}__name  `" class="truncate">
            {{ getUserInfo.nickName }}
          </span>
        </span>
      </span>

      <template #overlay>
        <Menu @click="handleMenuClick">
          <MenuItem
            key="profile"
            :text="t('layout.header.dropdownProfile')"
            icon="ant-design:user-outlined"
          />
          <MenuItem
            key="registerTenant"
            :text="t('layout.header.registeredEnterprise')"
            icon="ant-design:bank-filled"
          />
          <MenuItem
            key="switchCompany"
            :text="t('layout.header.switchingCompanies')"
            icon="ant-design:swap-outlined"
          />
          <MenuDivider v-if="getShowDoc" />
          <MenuItem
            v-if="getShowDoc"
            key="doc"
            :text="t('layout.header.dropdownItemDoc')"
            icon="ion:document-text-outline"
          />
          <MenuItem
            v-if="getShowDoc"
            key="vbenDoc"
            icon="ion:document-text-outline"
            :text="t('layout.header.dropdownItemDocFrontEnd')"
          />
          <MenuDivider v-if="getShowDoc" />
          <MenuItem
            v-if="getUseLockPage"
            key="lock"
            :text="t('layout.header.tooltipLock')"
            icon="ion:lock-closed-outline"
          />
          <MenuItem
            key="logout"
            :text="t('layout.header.dropdownItemLoginOut')"
            icon="ion:power-outline"
          />
        </Menu>
      </template>
    </Dropdown>
    <LockAction @register="register" />
    <SwitchTenant v-if="isMultiTenant" @register="registerSwitchModal" />
    <SwitchCompany v-else @register="registerSwitchModal" />
  </div>
</template>
<script lang="ts">
  // components
  import { Dropdown, Menu } from 'ant-design-vue';
  import { useRouter } from 'vue-router';
  import type { MenuInfo } from 'ant-design-vue/lib/menu/src/interface';

  import { computed, defineComponent, h, ref } from 'vue';

  import { DOC_URL, VBEN_DOC_URL } from '/@/settings/siteSetting';

  import { useUserStore } from '/@/store/modules/user';
  import { useDictStoreWithOut } from '/@/store/modules/dict';
  import { clearDictCache } from '/@/api/thinglinks/common/general';
  import { useHeaderSetting } from '/@/hooks/setting/useHeaderSetting';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useModal } from '/@/components/Modal';
  import { AvatarPreview } from '/@/components/AvatarPreview';
  import { useGlobSetting } from '/@/hooks/setting';
  import { MultiTenantTypeEnum } from '/@/enums/biz/tenant';

  import { propTypes } from '/@/utils/propTypes';
  import { openWindow } from '/@/utils';

  import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';

  type MenuEvent =
    | 'logout'
    | 'doc'
    | 'lock'
    | 'profile'
    | 'vbenDoc'
    | 'registerTenant'
    | 'switchCompany';

  export default defineComponent({
    name: 'UserDropdown',
    components: {
      Dropdown,
      Menu,
      AvatarPreview,
      MenuItem: createAsyncComponent(() => import('./DropMenuItem.vue')),
      MenuDivider: Menu.Divider,
      LockAction: createAsyncComponent(() => import('../lock/LockModal.vue')),
      SwitchTenant: createAsyncComponent(() => import('../tenant/SwitchTenant.vue')),
      SwitchCompany: createAsyncComponent(() => import('../company/SwitchCompany.vue')),
    },
    props: {
      theme: propTypes.oneOf(['dark', 'light']),
    },
    setup() {
      const { prefixCls } = useDesign('header-user-dropdown');
      const { t } = useI18n();
      const { getShowDoc, getUseLockPage } = useHeaderSetting();
      const userStore = useUserStore();
      const { replace } = useRouter();
      const avatarUrl = ref<string>('');
      const globSetting = useGlobSetting();

      const isMultiTenant = computed(
        () => globSetting.multiTenantType !== MultiTenantTypeEnum.NONE,
      );

      const getUserInfo = computed(() => {
        return userStore.getUserInfo;
      });
      const [register, { openModal }] = useModal();
      const [registerSwitchModal, { openModal: openSwitchModal }] = useModal();

      function handleLock() {
        openModal(true);
      }

      //  login out
      function handleLoginOut() {
        const { createConfirm } = useMessage();
        const { t } = useI18n();
        createConfirm({
          iconType: 'warning',
          title: () => h('span', t('sys.app.logoutTip')),
          content: () => h('span', t('sys.app.logoutMessage')),
          onOk: async () => {
            await userStore.logout(true);
            // 登出不走 location.reload(其他大刷新路径都走);手动清字典内存缓存,
            // 防止下个登录用户(不同租户/应用)继承上一用户的字典残留
            clearDictCache();
            useDictStoreWithOut().clearAllDicts();
          },
        });
      }

      // open doc
      function openDoc(flag: boolean) {
        if (flag) {
          openWindow(DOC_URL);
        } else {
          openWindow(VBEN_DOC_URL);
        }
      }

      function openProfile() {
        replace({
          name: 'profileIndex',
        });
      }
      function registerTenant() {
        replace({
          name: 'myTenant',
        });
      }

      function handleSwitchCompany() {
        openSwitchModal(true, {});
      }

      function handleMenuClick(e: MenuInfo) {
        switch (e.key as MenuEvent) {
          case 'profile':
            openProfile();
            break;
          case 'registerTenant':
            registerTenant();
            break;
          case 'switchCompany':
            handleSwitchCompany();
            break;
          case 'logout':
            handleLoginOut();
            break;
          case 'doc':
            openDoc(true);
            break;
          case 'vbenDoc':
            openDoc(false);
            break;
          case 'lock':
            handleLock();
            break;
        }
      }

      return {
        prefixCls,
        t,
        getUserInfo,
        handleMenuClick,
        getShowDoc,
        avatarUrl,
        register,
        registerSwitchModal,
        isMultiTenant,
        getUseLockPage,
      };
    },
  });
</script>
<style lang="less">
  @prefix-cls: ~'@{namespace}-header-user-dropdown';

  .@{prefix-cls} {
    height: @header-height;
    padding: 0 0 0 10px;
    padding-right: 10px;
    overflow: hidden;
    font-size: 12px;
    cursor: pointer;
    align-items: center;

    img {
      width: 24px;
      height: 24px;
      margin-right: 12px;
    }

    &__header {
      border-radius: 50%;
    }

    &__name {
      font-size: 14px;
    }

    &--dark {
      &:hover {
        background-color: @header-dark-bg-hover-color;
      }
    }

    &--light {
      &:hover {
        background-color: @header-light-bg-hover-color;
      }

      .@{prefix-cls}__name {
        color: @text-color-base;
      }

      .@{prefix-cls}__desc {
        color: @header-light-desc-color;
      }
    }

    &-dropdown-overlay {
      .ant-dropdown-menu-item {
        min-width: 160px;
      }
    }
  }
</style>
