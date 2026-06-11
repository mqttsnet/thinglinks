<script lang="ts" setup>
  import type { CardSize } from 'ant-design-vue/es/card/Card';
  import type { SizeType } from 'ant-design-vue/es/config-provider';

  import { computed, ref } from 'vue';
  import { RouterLink } from 'vue-router';

  import {
    Card,
    Descriptions,
    DescriptionsItem,
    Empty,
    List,
    ListItem,
    ListItemMeta,
    Spin,
    Table,
    TableColumn,
  } from 'ant-design-vue';

  import { BasicTitle } from '/@/components/Basic';
  import { DocTypeEnum } from '/@/enums/sopEnum';

  import { commonParams, resultData } from '../schema/content';
  import ApiParamTable from './api-param-table.vue';
  import { getDocDetail } from '/@/api/open/doc/openApi';
  import { DocInfoViewVO } from '/@/api/devOperation/sop/model/sopDocInfoModel';

  const docDetail = ref<DocInfoViewVO>({
    docInfoView: {},
    docInfoConfig: {},
  });
  const loading = ref<boolean>(false);
  const loaded = ref<boolean>(false);

  async function loadContent(docId: string) {
    try {
      loading.value = true;
      const result = await getDocDetail(docId);
      // 后端偶尔会返回 null（文档已下线 / 数据缺失），保持初始结构避免 v-show 链路里 ?.length 抛错
      docDetail.value = result || { docInfoView: {}, docInfoConfig: {} };
      loaded.value = true;
    } finally {
      loading.value = false;
    }
  }

  function reset() {
    docDetail.value = { docInfoView: {}, docInfoConfig: {} };
    loaded.value = false;
  }

  const dataNodeType = ref('Object');

  const isDoc = computed(() => {
    return docDetail.value?.docInfoView?.type === DocTypeEnum.DOC;
  });

  const showUrl = computed(() => {
    return (
      docDetail.value.docInfoConfig?.openProdUrl?.length > 0 &&
      docDetail.value.docInfoConfig?.openSandboxUrl?.length > 0
    );
  });

  const showProdUrl = computed(() => {
    return docDetail.value.docInfoConfig?.openProdUrl?.length > 0;
  });

  const showSandBoxUrl = computed(() => {
    return docDetail.value.docInfoConfig?.openSandboxUrl?.length > 0;
  });

  const showDoc = computed(() => {
    return docDetail.value?.docInfoView?.url?.length > 0;
  });

  const isMarkdown = computed(() => {
    return docDetail.value?.docInfoView?.type === DocTypeEnum.MARKDOWN;
  });

  const size = ref<CardSize>('small');
  const tableSize = ref<SizeType>('small');

  defineExpose({ loadContent, reset });
</script>
<template>
  <Spin :spinning="loading" wrapperClassName="open-doc-content-wrapper">
    <!-- 1) 空状态：还没选过任何 API（loaded=false），引导用户去左边树点选 -->
    <div v-if="!loaded" class="open-doc-empty">
      <Empty description="请从左侧选择一个具体的 API 接口查看详情">
        <template #image>
          <img
            src="https://gw.alipayobjects.com/zos/antfincdn/ZHrcdLPrvN/empty.svg"
            alt="empty"
            class="empty-img"
          />
        </template>
      </Empty>
    </div>
    <!-- 2) 选了节点但后端返回空（!showDoc 即 docInfoView.url 为空）：可能是后端没数据或文档已下线 -->
    <div v-else-if="!showDoc" class="open-doc-empty">
      <Empty description="该文档暂无可展示的内容（文档可能未发布或已被同步覆盖）" />
    </div>
    <!-- 3) Markdown 类型 -->
    <div v-else-if="isMarkdown" class="open-doc-empty">
      <Empty description="Markdown 类型文档预览暂未实现，请前往「帮助文档」查看" />
    </div>
    <!-- 4) 标准 API 文档 -->
    <div v-else v-show="!isDoc">
      <Card :size="size">
        <BasicTitle span :normal="false" h1>
          {{ `${docDetail.docInfoView.url}(${docDetail.docInfoView.docTitle})` }}
        </BasicTitle>
        <List item-layout="vertical">
          <ListItem key="1">
            <div class="mt-2" v-if="docDetail.docInfoView.description">
              <ListItemMeta>
                <template #title>
                  <BasicTitle span h2>接口说明</BasicTitle>
                </template>
                <template #description>
                  <div class="api-description" v-html="docDetail.docInfoView.description"></div>
                </template>
              </ListItemMeta>

              <Descriptions :size="size" bordered>
                <DescriptionsItem :label-style="{ width: '200px' }" label="接口名称">
                  {{ docDetail.docInfoView.url }}
                </DescriptionsItem>
                <DescriptionsItem label="版本号" :label-style="{ width: '200px' }">
                  {{ docDetail.docInfoView.version }}
                </DescriptionsItem>
              </Descriptions>

              <ListItem key="2">
                <ListItemMeta>
                  <template #title>
                    <BasicTitle span h2>请求地址</BasicTitle>
                  </template>
                </ListItemMeta>
                <Descriptions :size="size" v-show="showUrl" :column="1" bordered>
                  <DescriptionsItem :label-style="{ width: '200px' }" v-show="showProdUrl">
                    <template #label>
                      <div class="cell-item">生产环境</div>
                    </template>
                    {{ docDetail.docInfoConfig.openProdUrl }}
                  </DescriptionsItem>
                  <DescriptionsItem :label-style="{ width: '200px' }" v-show="showSandBoxUrl">
                    <template #label>
                      <div class="cell-item">沙箱环境</div>
                    </template>
                    {{ docDetail.docInfoConfig.openSandboxUrl }}
                  </DescriptionsItem>
                </Descriptions>
              </ListItem>

              <ListItem key="3">
                <ListItemMeta>
                  <template #title>
                    <BasicTitle span h2>公共请求参数</BasicTitle>
                  </template>
                </ListItemMeta>
                <Table :size="tableSize" :data-source="commonParams" bordered :pagination="false">
                  <TableColumn key="name" data-index="name" title="名称" width="200px" />
                  <TableColumn key="type" data-index="type" title="类型" width="100px" />
                  <TableColumn key="required" data-index="required" title="必须" width="50px">
                    <template #default="{ record }">
                      <span :class="record.required ? 'danger' : ''">
                        {{ record.required ? `是` : `否` }}
                      </span>
                    </template>
                  </TableColumn>
                  <TableColumn
                    key="maxLength"
                    data-index="maxLength"
                    title="最大长度"
                    width="80px"
                  />
                  <TableColumn ellipsis key="description" data-index="description" title="描述">
                    <template #default="{ record }">
                      <span v-if="record.name === 'sign'">
                        商户请求参数的签名串，详见
                        <RouterLink :to="{ path: '/doc/sign' }" target="_blank">
                          签名算法
                        </RouterLink>
                      </span>
                      <span v-else>
                        {{ record.description }}
                      </span>
                    </template>
                  </TableColumn>

                  <TableColumn key="example" data-index="example" title="示例值" width="200px" />
                </Table>
              </ListItem>

              <ListItem key="4">
                <ListItemMeta>
                  <template #title>
                    <BasicTitle span h2>业务请求参数</BasicTitle>
                  </template>
                </ListItemMeta>
                <ApiParamTable :data-source="docDetail.docInfoView.requestParams" />
              </ListItem>

              <ListItem key="5">
                <ListItemMeta>
                  <template #title>
                    <BasicTitle span h2>公共返回参数</BasicTitle>
                  </template>
                </ListItemMeta>
                <Table
                  :size="tableSize"
                  :data-source="resultData"
                  bordered
                  highlight-current-row
                  :pagination="false"
                >
                  <TableColumn title="名称" key="name" data-index="name" width="200px" />
                  <TableColumn title="类型" key="type" data-index="type" width="100px">
                    <template #default="{ record }">
                      <span v-if="record.name === 'data'">
                        {{ dataNodeType }}
                      </span>
                      <span v-else>
                        {{ record.type }}
                      </span>
                    </template>
                  </TableColumn>
                  <TableColumn title="描述" key="description" data-index="description">
                    <template #default="{ record }">
                      <span v-if="record.name === 'code'">
                        网关返回码，详见
                        <RouterLink :to="{ path: '/doc/code' }" target="_blank">
                          公共错误码
                        </RouterLink>
                      </span>
                      <span v-else>
                        {{ record.description }}
                      </span>
                    </template>
                  </TableColumn>
                  <TableColumn title="示例值" key="example" data-index="example" width="200px" />
                </Table>
              </ListItem>

              <ListItem key="6">
                <ListItemMeta>
                  <template #title>
                    <BasicTitle span h2>业务返回参数</BasicTitle>
                  </template>
                </ListItemMeta>
                <ApiParamTable :data-source="docDetail.docInfoView.responseParams" />
              </ListItem>
            </div>
          </ListItem>
        </List>
      </Card>
    </div>
  </Spin>
</template>
<style lang="scss" scoped>
  a {
    color: #409eff;
  }

  .danger {
    color: #f56c6c;
  }

  .open-doc-content-wrapper {
    width: 100%;
    min-height: 480px;
  }

  .open-doc-empty {
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 480px;
    padding: 32px;

    .empty-img {
      height: 80px;
    }
  }
</style>
