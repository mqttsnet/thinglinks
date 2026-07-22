import { isDevMode } from '/@/utils/env';
import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';
import FullScreen from './FullScreen.vue';

export const UserDropDown = createAsyncComponent(() => import('./user-dropdown/index.vue'), {
  loading: true,
});

export const LayoutBreadcrumb = createAsyncComponent(() => import('./Breadcrumb.vue'));

// export const Notify = createAsyncComponent(
//   isDevMode() ? () => import('./notify/index.vue') : () => import('./notifyWs/index.vue'),
// );
export const Notify = createAsyncComponent(() => import('./notifyWs/index.vue'));

export const ErrorAction = createAsyncComponent(() => import('./ErrorAction.vue'));

export const AppSwitchDropdown = createAsyncComponent(() => import('./AppSwitchDropdown.vue'));

export { FullScreen };
