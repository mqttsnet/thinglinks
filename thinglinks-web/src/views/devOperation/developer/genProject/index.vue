<template>
  <PageWrapper
    contentClass="flex"
    dense
    fixedHeight
    :title="t('devOperation.developer.genProject.table.title')"
  >
    <div class="bg-white md:w-2/3 m-4 p-4 mr-2 overflow-hidden">
      <BasicForm @register="registerForm" />
      <div class="flex justify-center">
        <a-button @click="resetFields">{{ t('devOperation.developer.genProject.clear') }}</a-button>
        <a-button class="!ml-4" @click="resetForm"> {{ t('common.resetText') }}</a-button>
        <a-button class="!ml-4" type="primary" @click="handleSubmit">{{
          t('devOperation.developer.genProject.locallyGenerated')
        }}</a-button>
        <a-button class="!ml-4" type="primary" @click="handleDownload">{{
          t('devOperation.developer.genProject.downloadNow')
        }}</a-button>
      </div>
      <BasicTitle line span>{{ t('devOperation.developer.genProject.precautions') }}</BasicTitle>
      <Alert :message="t('devOperation.developer.genProject.precautions')" show-icon>
        <template #description>
          <p>
            {{ t('devOperation.developer.genProject.measures[0]') }}
          </p>
          <p> {{ t('devOperation.developer.genProject.measures[1]') }} </p>
          <p>
            {{ t('devOperation.developer.genProject.measures[2]') }}
          </p>
          <p>
            {{ t('devOperation.developer.genProject.measures[3]') }}
          </p>
          <p> {{ t('devOperation.developer.genProject.measures[4]') }} </p>
        </template>
      </Alert>
    </div>

    <div class="bg-white m-4 p-4 ml-2 overflow-hidden md:w-1/3">
      <Tabs v-model:activeKey="activeKey">
        <TabPane v-for="item in tabList" :key="item.key">
          <template #tab>
            {{ item.name }}
          </template>
          <ThumbUrl :file-url="imgModules[item.key]" height="100%" width="100%" />
        </TabPane>
      </Tabs>
    </div>

    <template #extra>
      <a-button @click="resetFields">{{ t('devOperation.developer.genProject.clear') }}</a-button>
      <a-button @click="resetForm">{{ t('common.resetText') }}</a-button>
      <a-button type="primary" @click="handleSubmit">{{
        t('devOperation.developer.genProject.locallyGenerated')
      }}</a-button>
      <a-button type="primary" @click="handleDownload">{{
        t('devOperation.developer.genProject.downloadNow')
      }}</a-button>
    </template>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, onMounted, ref } from 'vue';
  import { Alert, Tabs } from 'ant-design-vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { PageWrapper } from '/@/components/Page';
  import ThumbUrl from '/@/components/Upload/src/ThumbUrl.vue';
  import { BasicTitle } from '/@/components/Basic';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { Api, download, generator, getDef } from '/@/api/devOperation/developer/defGenProject';
  import { getValidateRules } from '/@/api/thinglinks/common/formValidateService';
  import { customFormSchemaRules, editFormSchema } from './genProject.data';
  import { useLoading } from '/@/components/Loading';
  import { blobToObj } from '/@/utils/thinglinks/common';
  import { downloadFile } from '/@/utils/file/download.ts';

  interface TabModel {
    name: string;
    key: string;
  }

  const images = import.meta.globEager('../../../../assets/project/*.{png,jpg}');
  const imgModules = {};

  Object.keys(images).forEach((key) => {
    const mod = images[key].default || {};
    const name = key.substring(key.lastIndexOf('/') + 1, key.lastIndexOf('.'));
    imgModules[name] = mod;
  });

  export default defineComponent({
    name: '项目生成',
    components: {
      ThumbUrl,
      BasicForm,
      PageWrapper,
      Tabs,
      TabPane: Tabs.TabPane,
      BasicTitle,
      Alert,
    },
    setup() {
      const { t } = useI18n();
      const { createMessage } = useMessage();
      const activeKey = ref<string>('project_type');

      const tabList = ref<TabModel[]>([
        { key: 'project_type', name: t('devOperation.developer.genProject.type') },
        { key: 'project_outputDir', name: t('devOperation.developer.genProject.outputDir') },
        { key: 'project_author', name: t('devOperation.developer.genProject.author') },
        {
          key: 'project_projectPrefix',
          name: t('devOperation.developer.genProject.projectPrefix'),
        },
        { key: 'project_serviceName', name: t('devOperation.developer.genProject.serviceName') },
        { key: 'project_parent', name: t('devOperation.developer.genProject.parent') },
        { key: 'project_moduleName', name: t('devOperation.developer.genProject.moduleName') },
        { key: 'project_groupId', name: 'groupId' },
        { key: 'project_utilParent', name: t('devOperation.developer.genProject.utilParent') },
        { key: 'project_utilGroupId', name: 'utilGroupId' },
        { key: 'project_version', name: t('devOperation.developer.genProject.version') },
        { key: 'project_description', name: t('devOperation.developer.genProject.description') },
        { key: 'project_serverPort', name: t('devOperation.developer.genProject.serverPort') },
      ]);

      const [openFullLoading, closeFullLoading] = useLoading({
        tip: t('common.loadingText'),
      });

      function changeTab(key) {
        activeKey.value = key;
      }

      const [registerForm, { setFieldsValue, updateSchema, validate, resetFields }] = useForm({
        labelWidth: 120,
        schemas: editFormSchema(changeTab),
        name: 'project',
        showActionButtonGroup: false,
        baseColProps: { span: 12 },
        actionColOptions: {
          span: 23,
        },
      });

      onMounted(async () => {
        await load();
      });

      const load = async () => {
        try {
          openFullLoading();
          const record = await getDef();
          await setFieldsValue(record);

          let validateApi = Api.Generator;
          await getValidateRules(validateApi, customFormSchemaRules()).then(async (rules) => {
            rules && rules.length > 0 && (await updateSchema(rules));
          });
        } finally {
          closeFullLoading();
        }
      };

      async function resetForm() {
        await load();
      }

      async function handleSubmit() {
        try {
          openFullLoading();
          const params = await validate();
          await generator(params);
          createMessage.success(t('devOperation.developer.genProject.generated'));
        } finally {
          closeFullLoading();
        }
      }

      async function handleDownload() {
        try {
          openFullLoading();
          const params = await validate();
          const response = await download(params);
          if (response) {
            downloadFile(response);
            createMessage.success(t('common.tips.downloadSuccess'));
          } else {
            createMessage.error('下载失败，请认真检查【生成信息】是否填写完整并保存成功！');
          }
        } catch (e: any) {
          const obj = (await blobToObj(e?.response?.data)) as any;
          createMessage.error(obj.msg);
        } finally {
          closeFullLoading();
        }
      }

      return {
        t,
        activeKey,
        registerForm,
        tabList,
        imgModules,
        handleSubmit,
        resetForm,
        resetFields,
        handleDownload,
      };
    },
  });
</script>
