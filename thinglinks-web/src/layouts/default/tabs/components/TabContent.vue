<template>
  <Dropdown :dropMenuList="getDropMenuList" :trigger="getTrigger" @menuEvent="handleMenuEvent">
    <div :class="`${prefixCls}__info`" @contextmenu="handleContext" v-if="getIsTabs">
      <!-- 修改tab切换栏样式：增加前缀图标 -->
      <span v-if="showPrefixIcon" :class="`${prefixCls}__prefix-icon`" @click="handleContext">
        <Icon :icon="prefixIconType" />
      </span>
      <Tooltip>
        <template #title>{{ getTitle }}</template>
        <span class="tab-title ml-1">{{ getTitle }}</span>
      </Tooltip>
    </div>
    <span :class="`${prefixCls}__extra-quick`" v-else @click="handleContext">
      <Icon icon="ion:chevron-down" />
    </span>
  </Dropdown>
</template>
<script lang="ts">
  import type { PropType } from 'vue';
  import type { RouteLocationNormalized } from 'vue-router';

  import { defineComponent, computed, unref } from 'vue';
  import { Dropdown } from '/@/components/Dropdown/index';
  import { Icon } from '/@/components/Icon';
  import { Tooltip } from 'ant-design-vue';


  import { TabContentProps } from '../types';

  import { TabsThemeEnum } from '/@/enums/appEnum';
  import { PageEnum } from '/@/enums/pageEnum';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useTabDropdown } from '../useTabDropdown';
  import { useMultipleTabSetting } from '/@/hooks/setting/useMultipleTabSetting';

  export default defineComponent({
    name: 'TabContent',
    components: { Dropdown, Icon, Tooltip },
    props: {
      tabItem: {
        type: Object as PropType<RouteLocationNormalized>,
        default: null,
      },
      isExtra: Boolean,
    },
    setup(props) {
      const { prefixCls } = useDesign('multiple-tabs-content');
      const { t } = useI18n();

      // 配置成菜单后，打开菜单，显示名称未展示为菜单名称
      const getTitle = computed(() => {
        const { tabItem: { meta } = {} } = props;
        return meta && t(meta.title as string);
      });
      // 配置成菜单后，打开菜单，显示名称未展示为菜单名称

      const getIsTabs = computed(() => !props.isExtra);

      // 修改tab切换栏样式：前缀图标类型
      const prefixIconType = computed(() => {
        if (props.tabItem.meta.icon) {
          return props.tabItem.meta.icon;
        } else if (props.tabItem.path?.startsWith(PageEnum.BASE_HOME)) {
          return 'ant-design:home-outlined';
        } else {
          return 'ant-design:code';
        }
      });

      const getTrigger = computed((): ('contextmenu' | 'click' | 'hover')[] =>
        unref(getIsTabs) ? ['contextmenu'] : ['click'],
      );

      const { getDropMenuList, handleMenuEvent, handleContextMenu } = useTabDropdown(
        props as TabContentProps,
        getIsTabs,
      );

      function handleContext(e) {
        props.tabItem && handleContextMenu(props.tabItem)(e);
      }

      const { getTabsTheme } = useMultipleTabSetting();
      // 是否显示图标
      const showPrefixIcon = computed(() =>
        [TabsThemeEnum.SMOOTH, TabsThemeEnum.CARD].includes(unref(getTabsTheme) as TabsThemeEnum),
      );

      return {
        prefixCls,
        getDropMenuList,
        handleMenuEvent,
        handleContext,
        getTrigger,
        getIsTabs,
        getTitle,
        prefixIconType,
        showPrefixIcon,
      };
    },
  });
</script>
<style lang="less" scoped>
.thinglinks-multiple-tabs-content__info{
  display: flex !important;
  align-items: center;
  flex-wrap: nowrap;

  .tab-title{
    display: block;
    max-width: 200px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}
</style>