<template>
  <PageWrapper dense contentFullHeight>
    <Tabs class="sop-app-tabs bg-card px-4" v-model:active-key="activeKey" @change="handleClick">
      <TabPane :key="item.id" v-for="item in tabsData">
        <template #tab>
          <div class="flex items-center">
            <span>{{ item.appName }}</span>
            <DeleteOutlined
              style="margin: 0; margin-left: 12px"
              @click.stop="handleDeleteApp(item)"
            />
          </div>
        </template>
      </TabPane>
      <template #rightExtra>
        <a-button type="primary" v-hasAnyPermission="[permCode.addApp]" @click="handleAddApp">
          {{ t('devOperation.sop.sopDocInfo.add') }}
        </a-button>
      </template>
    </Tabs>
    <!-- tabsData 为空时给一个明确的空状态，否则用户只看到一个空白条，会以为是页面坏了 -->
    <div v-if="!tabsData.length" class="sop-app-empty bg-card">
      <Empty :description="t('devOperation.sop.sopDocInfo.noAppEmpty')">
        <a-button type="primary" v-hasAnyPermission="[permCode.addApp]" @click="handleAddApp">
          {{ t('devOperation.sop.sopDocInfo.add') }}
        </a-button>
      </Empty>
    </div>
    <BasicTable v-else @register="registerTable" @fetch-success="onFetchSuccess">
      <template #toolbar>
        <a-button type="primary" v-hasAnyPermission="[permCode.sync]" @click="handleSyncAppDoc">
          {{ t('devOperation.sop.sopDocInfo.syncAll') }}
        </a-button>
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'action'">
          <TableAction
            :actions="[
              {
                label: t('devOperation.sop.sopDocInfo.publish'),
                color: 'success',
                auth: [permCode.publish],
                ifShow: () => {
                  return record.isFolder === 1;
                },
                popConfirm: {
                  title:
                    t('devOperation.sop.sopDocInfo.confirm1') +
                    t('devOperation.sop.sopDocInfo.publish') +
                    `[${record.docTitle}]` +
                    t('devOperation.sop.sopDocInfo.confirm2'),
                  confirm: handleFolderPublish.bind(null, record),
                },
              },
              {
                label: t('devOperation.sop.sopDocInfo.offline'),
                color: 'error',
                auth: [permCode.offline],
                ifShow: () => {
                  return record.isFolder === 1;
                },
                popConfirm: {
                  title:
                    t('devOperation.sop.sopDocInfo.confirm1') +
                    t('devOperation.sop.sopDocInfo.offline') +
                    `[${record.docTitle}]` +
                    t('devOperation.sop.sopDocInfo.confirm2'),
                  confirm: handleFolderOffline.bind(null, record),
                },
              },

              {
                label: record.isPublish
                  ? t('devOperation.sop.sopDocInfo.offline')
                  : t('devOperation.sop.sopDocInfo.publish'),
                color: record.isPublish === 1 ? 'error' : 'success',
                auth: [permCode.publish, permCode.offline],
                ifShow: () => {
                  return record.isFolder === 0;
                },
                popConfirm: {
                  title:
                    t('devOperation.sop.sopDocInfo.confirm1') +
                    t('common.title.edit') +
                    `[${record.docTitle}]` +
                    t('devOperation.sop.sopDocInfo.confirm3'),
                  confirm: handlePublishOrOffline.bind(null, record),
                },
              },

              {
                label: t('devOperation.sop.sopDocInfo.sync'),
                auth: [permCode.sync],
                popConfirm: {
                  title: record.isFolder
                    ? t('devOperation.sop.sopDocInfo.confirm1') +
                      t('devOperation.sop.sopDocInfo.sync') +
                      `[${record.docTitle}]` +
                      t('devOperation.sop.sopDocInfo.confirm2')
                    : t('devOperation.sop.sopDocInfo.confirm1') +
                      t('devOperation.sop.sopDocInfo.sync') +
                      `[${record.docTitle}]？`,
                  confirm: handleSync.bind(null, record),
                },
              },
            ]"
            :stopButtonPropagation="true"
          />
        </template>
      </template>
    </BasicTable>
    <EditModal @register="registerModal" @success="handleAddSuccess" />
  </PageWrapper>
</template>
<script lang="ts">
  import { Empty, TabPane, Tabs } from 'ant-design-vue';
  import { DeleteOutlined } from '@ant-design/icons-vue';
  import { defineComponent, onMounted, ref } from 'vue';
  import { useTabs } from '/@/hooks/web/useTabs';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { PageWrapper } from '/@/components/Page';
  import { useModal } from '/@/components/Modal';
  import { tree, syncAppDoc, publish, syncDoc } from '/@/api/devOperation/sop/sopDocInfo';
  import { columns, searchFormSchema } from './sopDocInfo.data';
  import EditModal from './Edit.vue';
  import { useLoading } from '/@/components/Loading';
  import { PermCode } from '/@/enums/perm';
  import { query, remove } from '/@/api/devOperation/sop/sopDocApp';
  import { SopDocInfoResultVO } from '/@/api/devOperation/sop/model/sopDocInfoModel';

  export default defineComponent({
    // 若需要开启页面缓存，请将此参数跟菜单名保持一致
    name: '文档管理',
    components: {
      BasicTable,
      PageWrapper,
      TableAction,
      EditModal,
      Empty,
      TabPane,
      Tabs,
      DeleteOutlined,
    },
    setup() {
      const { refreshPage } = useTabs();
      const { t } = useI18n();
      const { createMessage, createConfirm } = useMessage();
      const [registerModal, { openModal }] = useModal();
      const [openFullLoading, closeFullLoading] = useLoading({
        // text: '加载中...',
      });
      const activeKey = ref<string>('');
      const tabsData = ref<any[]>([]);
      const permCode = PermCode.devOperation.sop.sopDocInfo;

      // 表格 api 包一层：未选择应用（activeKey 为空，比如租户还没建任何文档应用）时短路返回空数组，
      // 不要把 docAppId='' 发到后端 —— 后端 @RequestParam Long docAppId 解析空串会抛
      // "缺少必须的[Long]类型的参数[docAppId]"，前端就会弹红条吓到用户。
      // 注意：useTable 的 beforeFetch 不支持取消请求（见 useDataSource.ts，返回 falsy 会回退到原 params），
      // 所以必须在 api 这一层拦掉。
      const treeApi = (params: any) => {
        if (!activeKey.value) {
          return Promise.resolve([] as SopDocInfoResultVO[]);
        }
        return tree(params);
      };

      // 表格
      const [registerTable, { getForm, reload, expandAll }] = useTable({
        title: t('devOperation.sop.sopDocInfo.table.title'),
        api: treeApi,
        columns: columns(),
        formConfig: {
          name: 'SopDocInfoSearch',
          labelWidth: 120,
          schemas: searchFormSchema(),
          autoSubmitOnEnter: true,
          resetButtonOptions: {
            preIcon: 'ant-design:rest-outlined',
          },
          submitButtonOptions: {
            preIcon: 'ant-design:search-outlined',
          },
        },
        // 延迟请求
        immediate: false,
        beforeFetch: (data: Recordable) => {
          data.docAppId = activeKey.value;
          return data;
        },
        afterFetch: async (list: any[]) => {
          const form = await getForm();
          const { docTitle: keyword } = await form.getFieldsValue();
          if (!keyword) {
            return list;
          }

          return searchRow(keyword.toLowerCase(), list);
        },
        isTreeTable: true,
        defaultExpandAllRows: true,
        pagination: false,
        useSearchForm: true,
        showTableSetting: true,
        bordered: true,
        rowKey: 'id',
        rowSelection: {
          type: 'checkbox',
          columnWidth: 40,
        },
        actionColumn: {
          width: 200,
          title: t('common.column.action'),
          dataIndex: 'action',
        },
      });

      async function onFetchSuccess() {
        expandAll();
      }

      const folderHandler = (row: SopDocInfoResultVO) => {
        return row.isFolder === 1;
      };

      const searchHandler = (searchText: string, row: SopDocInfoResultVO) => {
        return (
          (row.docName && row.docName.toLowerCase().includes(searchText)) ||
          (row.docTitle && row.docTitle.toLowerCase().includes(searchText))
        );
      };

      const searchRow = (search: string, rows: SopDocInfoResultVO[]) => {
        const ret: SopDocInfoResultVO[] = [];
        for (const row of rows) {
          // 找到分类
          if (folderHandler(row)) {
            if (searchHandler(search, row)) {
              ret.push(row);
            } else {
              // 分类名字没找到，需要从子文档中找
              const children = row.children || [];
              const searchedChildren = searchRow(search, children);
              // 如果子文档中有
              if (searchedChildren && searchedChildren.length > 0) {
                const rowCopy = Object.assign({}, row);
                rowCopy.children = searchedChildren;
                ret.push(rowCopy);
              }
            }
          } else {
            // 不是分类且被找到
            if (searchHandler(search, row)) {
              ret.push(row);
            }
          }
        }
        return ret;
      };

      const activeTab = (id: string) => {
        activeKey.value = id;

        reload();
      };

      const loadApp = async (appId: string) => {
        // 加 try/catch + 类型保护：query 失败时不要静默吞掉让用户看到一片空白还以为页面坏了；
        // 接口偶尔被网关拦截 / 后端字段命名变化，类型保护能让前端不至于把 undefined 当数组用直接抛 TypeError。
        let list: SopDocAppResultVO[] = [];
        try {
          const resp = await query({});
          if (Array.isArray(resp)) {
            list = resp;
          } else if (resp && Array.isArray((resp as any).records)) {
            // 兼容后端某些场景下返回分页对象 { records, total } 的情况
            list = (resp as any).records;
          }
        } catch (e: any) {
          createMessage.error(
            t('devOperation.sop.sopDocInfo.loadAppFailed') + (e?.message ? `: ${e.message}` : ''),
          );
        }
        tabsData.value = list;

        const length = tabsData.value.length;
        if (length > 0) {
          let targetId;
          for (const id of tabsData.value) {
            if (id === appId) {
              targetId = id;
              break;
            }
          }
          if (!targetId) {
            targetId = tabsData.value[0].id;
          }
          activeKey.value = targetId;
          activeTab(targetId);
        } else {
          // 空数据时显式重置，避免上一次切的应用 ID 残留导致表格 reload 命中错的 docAppId
          activeKey.value = '';
        }
      };

      onMounted(() => {
        loadApp('0');
      });

      // 同步
      const handleSync = async (row: any, e: Event) => {
        e?.stopPropagation();
        openFullLoading();
        try {
          await syncDoc({ id: row.id });
          createMessage.success(t('devOperation.developer.defGenTableColumn.syncSuccess'));
        } finally {
          closeFullLoading();
        }
      };
      // 发布
      const handleFolderPublish = async (row: any, e: Event) => {
        e?.stopPropagation();

        const result = await publish(row.id, 1);
        if (result) {
          createMessage.success(t('devOperation.sop.sopDocInfo.publishSuccess'));
          reload();
        } else {
          createMessage.error(t('devOperation.sop.sopDocInfo.publishFail'));
        }
      };

      // 下线
      const handleFolderOffline = async (row: any, e: Event) => {
        e?.stopPropagation();

        const result = await publish(row.id, 0);
        if (result) {
          createMessage.success(t('devOperation.sop.sopDocInfo.offlineSuccess'));
          reload();
        } else {
          createMessage.error(t('devOperation.sop.sopDocInfo.offlineFail'));
        }
      };

      const handlePublishOrOffline = async (row: any, e: Event) => {
        e?.stopPropagation();

        const result = await publish(row.id, row.isPublish === 1 ? 0 : 1);
        if (result) {
          createMessage.success(t('devOperation.sop.sopDocInfo.statusSuccess'));
          reload();
        } else {
          createMessage.error(t('devOperation.sop.sopDocInfo.statusFail'));
        }
      };

      // 打开新增表单
      const handleSyncAppDoc = () => {
        if (!activeKey.value) {
          // 还没有任何应用就点"同步所有"，后端会因为 id 为空抛同样的"缺少必须参数"错误。提前给个友好提示。
          createMessage.warning(t('devOperation.sop.sopDocInfo.pleaseCreateAppFirst'));
          return;
        }
        createConfirm({
          iconType: 'warning',
          title: t('common.tips.tips'),
          content: t('devOperation.sop.sopDocInfo.syncTips'),
          onOk: async () => {
            openFullLoading();
            try {
              const result = await syncAppDoc({ id: activeKey.value });
              if (result) {
                createMessage.success(
                  t('devOperation.developer.defGenTable.synchronizationSuccess'),
                );
                reload();
              } else {
                createMessage.error(t('devOperation.developer.defGenTable.synchronizationError'));
              }
            } finally {
              closeFullLoading();
            }
          },
        });
      };

      const handleClick = (key: number | string) => {
        activeTab(key as string);
      };

      function handleAddApp() {
        openModal(true, {});
      }

      function handleAddSuccess(appId: string) {
        loadApp(appId);
      }

      const handleDeleteApp = async ({ id, appName }: { id: string; appName: string }) => {
        createConfirm({
          iconType: 'warning',
          title: t('common.tips.tips'),
          content: t('devOperation.sop.sopDocInfo.deleteTips', { value: appName }),
          onOk: async () => {
            await remove([id]);
            createMessage.success(t('common.tips.deleteSuccess'));
            refreshPage();
          },
        });
      };

      return {
        t,
        registerTable,
        registerModal,
        handleAddSuccess,
        permCode,
        handleSyncAppDoc,
        handleAddApp,
        handleClick,
        handlePublishOrOffline,
        handleFolderOffline,
        handleFolderPublish,
        handleSync,
        activeKey,
        tabsData,
        onFetchSuccess,
        handleDeleteApp,
      };
    },
  });
</script>
<style lang="less" scoped>
  ::v-deep(.sop-app-tabs.ant-tabs) {
    padding-left: 1rem;
    padding-right: 1rem;
  }

  ::v-deep(.sop-app-tabs) {
    .ant-tabs-nav {
      margin: 0;
    }
  }

  .sop-app-empty {
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 320px;
    margin-top: 1rem;
    border-radius: 4px;
    padding: 32px;
  }
</style>
