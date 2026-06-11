<template>
  <section class="home-hero">
    <!-- Layer 1: 大 banner 横铺 + 身份 profile 卡（合并问候/标题/日期） -->
    <a-row :gutter="[16, 16]" class="home-hero__banner-row">
      <a-col :xs="24" :sm="24" :md="16" :lg="16" :xl="16">
        <WelcomeBanner :apps-count="appsAvailable" @click="openAppSwitch" />
      </a-col>
      <a-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
        <div class="home-hero__profile-card">
          <div class="home-hero__profile-top">
            <AvatarPreview
              :errorTxt="initial"
              :fileId="userInfo?.avatarId"
              :size="44"
              class="home-hero__profile-avatar"
            />
            <div class="home-hero__profile-welcome">
              {{ t(greetingKey) }}
            </div>
            <WeatherChip class="home-hero__profile-weather" />
          </div>
          <div class="home-hero__profile-name">{{ displayName }}</div>
          <div class="home-hero__profile-meta">
            <span class="home-hero__profile-role">{{ roleText }}</span>
            <span class="home-hero__profile-sep">·</span>
            <span class="home-hero__profile-date">{{ dateText }}</span>
          </div>
        </div>
      </a-col>
    </a-row>

    <!-- Layer 3: 4 张 MetricCard -->
    <a-row :gutter="[16, 16]" class="home-hero__metrics">
      <a-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <MetricCard
          :label="t('workbench.hero.roleTitle')"
          :value="roleMetric.value"
          :sub="roleMetric.sub"
          icon="ant-design:safety-outlined"
          icon-color="#5d87ff"
          icon-bg="#ecf2ff"
        />
      </a-col>
      <a-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <MetricCard
          :label="t('workbench.hero.orgTitle')"
          :value="orgMetric.value"
          :sub="orgMetric.sub"
          icon="ant-design:cluster-outlined"
          icon-color="#7c5cfc"
          icon-bg="#f3eeff"
        />
      </a-col>
      <a-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <MetricCard
          :label="t('workbench.hero.appsTitle')"
          :value="appsCount"
          :sub="appsSub"
          icon="ant-design:appstore-outlined"
          icon-color="#13deb9"
          icon-bg="#e6f9f4"
        />
      </a-col>
      <a-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <MetricCard
          :label="t('workbench.hero.onlineTitle')"
          :value="lastLoginText"
          :sub="agoText"
          icon="ant-design:login-outlined"
          icon-color="#ffae1f"
          icon-bg="#fff5e0"
        />
      </a-col>
    </a-row>
  </section>
</template>

<script lang="ts" setup>
  import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useUserStore } from '/@/store/modules/user';
  import { AvatarPreview } from '/@/components/AvatarPreview';
  import MetricCard from '../components/MetricCard.vue';
  import WelcomeBanner from '../components/WelcomeBanner.vue';
  import WeatherChip from '../components/WeatherChip.vue';
  import { useGreeting } from '../hooks/useGreeting';
  import { useAppSwitchController } from '../hooks/useAppSwitchController';
  import { anyonePage as queryLoginLogPage } from '/@/api/basic/system/baseLoginLog';

  interface Props {
    appsTotal?: number;
    appsAvailable?: number;
  }
  const props = withDefaults(defineProps<Props>(), { appsTotal: 0, appsAvailable: 0 });

  const { t } = useI18n();
  const userStore = useUserStore();
  const { greetingKey, dateText, weekKey } = useGreeting();
  const appSwitch = useAppSwitchController();

  const userInfo = computed(() => userStore.getUserInfo);
  const displayName = computed(
    () => userInfo.value?.nickName || userInfo.value?.username || userInfo.value?.email || '-',
  );
  const initial = computed(() => (displayName.value || 'U').slice(0, 1).toUpperCase());

  const roleMetric = computed(() => {
    const roles = userStore.getRoleList || [];
    if (roles.length === 0) {
      return { value: t('workbench.hero.roleFallback'), sub: '' };
    }
    return {
      value: String(roles[0]),
      sub: roles.length > 1 ? `+${roles.length - 1}` : '',
    };
  });

  const roleText = computed(() => {
    const roles = userStore.getRoleList || [];
    return roles.length > 0 ? String(roles[0]) : t('workbench.hero.roleFallback');
  });

  const orgMetric = computed(() => {
    const info: any = userInfo.value || {};
    const echo = info.echoMap || {};
    const empEcho = info.baseEmployee?.echoMap || {};
    const orgName =
      echo.createdOrgId?.name ||
      empEcho.createdOrgId?.name ||
      info.tenantName ||
      echo.tenantId?.name ||
      empEcho.tenantId?.name ||
      '';
    if (!orgName) {
      return { value: t('workbench.hero.orgFallback'), sub: '' };
    }
    const tenantName = info.tenantName || echo.tenantId?.name || empEcho.tenantId?.name || '';
    return {
      value: orgName,
      sub: tenantName && tenantName !== orgName ? tenantName : '',
    };
  });

  const appsCount = computed(() => (props.appsAvailable ? `${props.appsAvailable}` : '-'));
  const appsSub = computed(() => {
    if (!props.appsTotal || props.appsTotal === props.appsAvailable) {
      return t('workbench.hero.appsSub');
    }
    return `/ ${props.appsTotal}`;
  });

  const now = ref<number>(Date.now());
  const fetchedLoginAt = ref<number | null>(null);
  let timer: ReturnType<typeof setInterval> | null = null;

  async function loadLatestLogin() {
    try {
      const res: any = await queryLoginLogPage({ model: {}, current: 1, size: 10 });
      const list: any[] = res?.records || [];
      if (!list.length) return;
      const toMs = (r: any): number => {
        const raw = r?.createdTime || r?.loginDate;
        if (!raw) return 0;
        const s = typeof raw === 'string' ? raw.replace(' ', 'T') : raw;
        const ms = typeof s === 'number' ? s : new Date(s).getTime();
        return Number.isFinite(ms) ? ms : 0;
      };
      const latest = list.reduce<number>((max, r) => Math.max(max, toMs(r)), 0);
      if (latest > 0) fetchedLoginAt.value = latest;
    } catch (_) {
      /* ignore */
    }
  }

  onMounted(() => {
    timer = setInterval(() => (now.value = Date.now()), 30_000);
    loadLatestLogin();
  });
  onBeforeUnmount(() => {
    if (timer) clearInterval(timer);
  });

  const loginAtMs = computed<number | null>(() => {
    if (fetchedLoginAt.value) return fetchedLoginAt.value;
    const info: any = userInfo.value || {};
    const raw = info.lastLoginTime ?? info.baseEmployee?.lastLoginTime;
    if (!raw) return null;
    const ms = typeof raw === 'number' ? raw : new Date(raw).getTime();
    return Number.isFinite(ms) ? ms : null;
  });

  const lastLoginText = computed(() => {
    if (!loginAtMs.value) return '-';
    const d = new Date(loginAtMs.value);
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    const hh = String(d.getHours()).padStart(2, '0');
    const mi = String(d.getMinutes()).padStart(2, '0');
    return `${mm}-${dd} ${hh}:${mi}`;
  });

  const agoText = computed(() => {
    if (!loginAtMs.value) return '';
    const delta = Math.max(0, now.value - loginAtMs.value);
    const min = Math.floor(delta / 60_000);
    if (min < 1) return t('workbench.hero.ago.just');
    if (min < 60) return t('workbench.hero.ago.min', { n: min });
    const h = Math.floor(min / 60);
    if (h < 24) return t('workbench.hero.ago.hour', { n: h });
    const d = Math.floor(h / 24);
    return t('workbench.hero.ago.day', { n: d });
  });

  function openAppSwitch() {
    appSwitch.open();
  }
</script>

<style lang="less" scoped>
  .home-hero {
    margin-bottom: 16px;
  }

  .home-hero__banner-row {
    margin-bottom: 4px;
  }

  .home-hero__profile-card {
    display: flex;
    flex-direction: column;
    gap: 10px;
    height: 100%;
    min-height: 180px;
    padding: 22px 24px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 1px 3px rgba(20, 37, 66, 0.03), 0 6px 24px rgba(20, 37, 66, 0.04);
  }

  .home-hero__profile-top {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .home-hero__profile-weather {
    margin-left: auto;
  }

  .home-hero__profile-avatar {
    flex-shrink: 0;
  }

  .home-hero__profile-welcome {
    font-size: 13px;
    color: #8c97a5;
    font-weight: 500;
  }

  .home-hero__profile-name {
    font-size: 20px;
    font-weight: 700;
    color: #2a3547;
    letter-spacing: -0.3px;
    line-height: 1.25;
    margin-top: auto;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .home-hero__profile-meta {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 12px;
    color: #8c97a5;
    font-weight: 500;
    flex-wrap: wrap;
  }

  .home-hero__profile-sep {
    color: #d0d6df;
  }

  .home-hero__metrics {
    margin-top: 12px;
  }

  @media (max-width: 992px) {
    .home-hero__profile-card {
      min-height: 120px;
      padding: 18px 20px;
    }
    .home-hero__profile-name {
      font-size: 18px;
    }
  }
</style>
