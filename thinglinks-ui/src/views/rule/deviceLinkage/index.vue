<template>
  <div class="wrap">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['rule:deviceLinkage:edit']"
          icon="el-icon-plus"
          plain
          size="mini"
          type="primary"
          @click="addRule"
          >规则创建
        </el-button>
      </el-col>
      <right-toolbar
        :showSearch.sync="showSearch"
        @queryTable="getList"
      ></right-toolbar>
    </el-row>
    <!-- <div class="defalut" v-if="ruleList.length">
      <div>
        <i class="el-icon-warning"></i>
        <span>暂无数据</span>
      </div>
    </div> -->
    <el-table v-loading="loading" :data="ruleList">
      <el-table-column align="center" width="60" label="id" prop="id" />
      <el-table-column align="center" label="规则名称" prop="ruleName" />
      <el-table-column align="center" label="规则标识" prop="ruleIdentification"/>
      <el-table-column align="center" label="appId" prop="appId" width="180" />
      <el-table-column align="center" label="规则描述" prop="remark" width="280"
      />
      <el-table-column fixed="right" label="操作" width="180">
        <template slot-scope="scope">
          <span style="margin-right: 10px">
            <el-tooltip
              class="item"
              content="修改"
              effect="light"
              placement="top"
            >
              <el-button
                v-hasPermi="['rule:deviceLinkage:edit']"
                circle
                icon="el-icon-edit"
                size="mini"
                type="primary"
                @click="handleUpdate(scope.row)"
              ></el-button>
            </el-tooltip>
          </span>
          <span style="margin-right: 10px">
            <el-tooltip
              class="item"
              content="删除"
              effect="light"
              placement="top"
            >
              <el-button
                v-hasPermi="['rule:deviceLinkage:remove']"
                circle
                icon="el-icon-delete"
                size="mini"
                type="primary"
                @click="handleDelete(scope.row)"
              ></el-button>
            </el-tooltip>
          </span>
          <span style="margin-right: 10px">
            <el-tooltip
              class="item"
              content="设备详情"
              effect="light"
              placement="top"
            >
              <el-button
                v-hasPermi="['rule:deviceLinkage:edit']"
                circle
                icon="el-icon-s-operation"
                size="mini"
                type="primary"
                @click="handleDetail(scope.row)"
              ></el-button>
            </el-tooltip>
          </span>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :limit.sync="queryParams.pageSize"
      :page.sync="queryParams.pageNum"
      :total="total"
      @pagination="getList"
    />
  </div>
</template>

<script>
import { listRule, deleteRule } from "@/api/rule/deviceLinkage";
export default {
  name: "index.vue",
  data() {
    return {
      ruleList: [],
      total: 0,
      queryParams: {
        page: 1,
        pageSize: 10,
      },
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      listRule(this.queryParams).then((response) => {
        this.ruleList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    addRule() {
      this.$router.push({
        name: "addRule",
      });
    },
    handleUpdate(rows) {
      this.$router.push({
        name: "addRule",
        query: {
          id: rows.id,
        },
      });
    },
    handleDelete(rows) {
      this.$confirm("此操作将永久删除该记录, 是否继续?", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      }).then(() => {
        deleteRule(rows.id).then((res) => {
          this.$modal.msgSuccess("删除成功");
          this.getList();
        });
      });
    },
    handleDetail(rows) {
      this.$router.push({
        name: "addRule",
        query: {
          id: rows.id,
        },
      });
    },
  },
};
</script>

<style scoped lang="scss">
.wrap {
  width: 100%;
  height: 100%;
  padding: 15px 30px;
  box-sizing: border-box;

  .defalut {
    width: 100%;
    height: 500px;
    margin: 15px 0 0 0;
    display: flex;
    justify-content: center;
    align-items: center;
    border: 1px solid #eef0f5;
    background: #eef0f5;
    color: rgb(110, 104, 104);

    div {
      height: 20%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: space-around;

      i {
        font-size: 40px;
      }
    }
  }
}
</style>
