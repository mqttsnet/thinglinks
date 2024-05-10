<template>
  <div class="app-container">
    <el-form ref="queryForm" :inline="true" :model="queryParams" label-width="68px">
      <el-form-item label="产品名称" prop="productName">
        <el-input v-model="queryParams.productName" clearable placeholder="请输入产品名称" size="small"
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="厂商ID" prop="manufacturerId">
        <el-input v-model="queryParams.manufacturerId" clearable placeholder="请输入厂商ID" size="small"
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="厂商名称" prop="manufacturerName">
        <el-input v-model="queryParams.manufacturerName" clearable placeholder="请输入厂商名称" size="small"
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="产品型号" prop="model">
        <el-input v-model="queryParams.model" clearable placeholder="请输入产品型号" size="small"
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item>
        <el-button icon="el-icon-search" size="mini" type="primary" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button v-hasPermi="['link:product:add']" icon="el-icon-plus" plain size="mini" type="primary"
                   @click="handleAdd">
          新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['link:product:edit']" :disabled="single" icon="el-icon-document-copy" plain size="mini"
                   type="success" @click="handleUpdate">
          修改
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['link:product:remove']" :disabled="multiple" icon="el-icon-delete" plain size="mini"
                   type="danger" @click="handleDelete">
          删除
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['link:product:import']" icon="el-icon-upload2" plain size="mini" type="info"
                   @click="handleImport">
          导入
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['link:product:export']" icon="el-icon-download" plain size="mini" type="warning"
                   @click="handleExport">
          导出
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['link:product:generate']" icon="el-icon-s-promotion" plain size="mini" type="primary"
                   @click="onQuick">
          快捷生成
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button :disabled="multiple" icon="el-icon-refresh" plain size="mini" type="primary"
                   @click="productEmpowerment">
          产品赋能
        </el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="productList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55"/>
      <el-table-column align="center" label="id" prop="id"/>
      <el-table-column align="center" label="应用ID" prop="appId" width="180">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.link_application_type" :value="scope.row.appId"/>
        </template>
      </el-table-column>
      <el-table-column align="center" label="产品名称" prop="productName" width="180"/>
      <el-table-column align="center" label="产品标识" prop="productIdentification" width="180"/>
      <el-table-column align="center" label="厂商ID" prop="manufacturerId" width="180"/>
      <el-table-column align="center" label="厂商名称" prop="manufacturerName" width="180"/>
      <el-table-column align="center" label="产品型号" prop="model" width="180"/>
      <el-table-column align="center" label="设备类型" prop="deviceType" width="180"/>
      <el-table-column align="center" label="协议类型" prop="protocolType" width="180">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.link_device_protocol_type" :value="scope.row.protocolType"/>
        </template>
      </el-table-column>
      <el-table-column align="center" label="状态" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.business_data_status" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column align="center" label="产品描述" prop="remark" width="180"/>
      <el-table-column align="center" fixed="right" label="操作" width="200">
        <template slot-scope="scope">
          <el-tooltip class="item" content="复制" effect="light" placement="top">
            <el-button v-hasPermi="['link:product:edit']" circle icon="el-icon-document-copy" size="mini" type="primary"
                       @click="handleUpdate(scope.row)">
            </el-button>
          </el-tooltip>
          <el-tooltip class="item" content="删除" effect="light" placement="top">
            <el-button v-hasPermi="['link:product:remove']" circle icon="el-icon-delete" size="mini" type="primary"
                       @click="handleDelete(scope.row)">
            </el-button>
          </el-tooltip>
          <el-tooltip class="item" content="产品赋能" effect="light" placement="top">
            <el-button circle icon="el-icon-refresh" size="mini" type="primary"
                       @click="productEmpowerment(scope.row)">
            </el-button>
          </el-tooltip>
          <el-tooltip class="item" content="产品详情" effect="light" placement="top">
            <el-button v-hasPermi="['link:product:detail']" circle icon="el-icon-s-operation" size="mini" type="primary"
                       @click="handleProductDetail(scope.row)"></el-button>
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :limit.sync="queryParams.pageSize" :page.sync="queryParams.pageNum" :total="total"
                @pagination="getList"/>

    <!-- 添加或修改产品管理对话框 -->
    <el-dialog :close-on-click-modal="false" :title="title" :visible.sync="open" append-to-body width="48%">
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-row>
          <el-col :span="11">
            <el-form-item label="应用ID" prop="appId">
              <el-col :span="22">
                <el-select v-model="form.appId" placeholder="请选择应用ID" @change="changeApp">
                  <el-option v-for="dict in dict.type.link_application_type" :key="dict.value" :label="dict.label"
                             :value="dict.value"/>
                </el-select>
              </el-col>
              <el-col :span="2" style="padding-left: 5px">
                <el-tooltip class="item" content="应用ID需全局唯一，应用ID创建后无法变更" effect="light"
                            placement="right-start">
                  <i class="el-icon-question"/>
                </el-tooltip>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="产品模型模板" prop="templateIdentification">
              <el-col :span="22">
                <el-select v-model="form.templateIdentification" placeholder="请选择产品模型模板">
                  <el-option v-for="item in templateList" :key="item.templateIdentification" :label="item.templateName"
                             :value="item.templateIdentification"/>
                </el-select>
              </el-col>
              <el-col :span="2" style="padding-left: 5px">
                <el-tooltip class="item" content="标准化的物模型可以沉淀为平台资产，供用户快速创建产品Profile"
                            effect="light"
                            placement="right-start">
                  <i class="el-icon-question"/>
                </el-tooltip>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="11">
            <el-form-item label="产品名称" prop="productName">
              <el-col :span="22">
                <el-input v-model="form.productName" placeholder="请输入产品名称"/>
              </el-col>
              <el-col :span="2" style="padding-left: 5px">
                <el-tooltip class="item" content="自定义，支持中文、英文大小写、数字、下划线和中划线" effect="light"
                            placement="right-start">
                  <i class="el-icon-question"/>
                </el-tooltip>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="协议类型" prop="protocolType">
              <el-col :span="22">
                <el-select v-model="form.protocolType" placeholder="请选择协议类型">
                  <el-option v-for="dict in dict.type.link_device_protocol_type" :key="dict.value" :label="dict.label"
                             :value="dict.value"></el-option>
                </el-select>
              </el-col>
              <el-col :span="2" style="padding-left: 5px">
                <el-tooltip class="item" content="默认为MQTT无需修改。" effect="light" placement="right-start">
                  <i class="el-icon-question"/>
                </el-tooltip>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="11">
            <el-form-item label="产品类型" prop="productType">
              <el-col :span="22">
                <el-select v-model="form.productType" placeholder="请选择产品类型">
                  <el-option v-for="dict in dict.type.link_product_type" :key="dict.value" :label="dict.label"
                             :value="dict.value"></el-option>
                </el-select>
              </el-col>
              <el-col :span="2" style="padding-left: 5px">
                <el-tooltip class="item" content="支持以下两种产品类型•0：普通产品，需直连设备。•1：网关产品，可挂载子设备。"
                            effect="light"
                            placement="right-start">
                  <i class="el-icon-question"/>
                </el-tooltip>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="厂商ID" prop="manufacturerId">
              <el-col :span="22">
                <el-input v-model="form.manufacturerId" placeholder="请输入厂商ID"/>
              </el-col>
              <el-col :span="2" style="padding-left: 5px">
                <el-tooltip class="item" content="支持英文大小写，数字，下划线和中划线" effect="light"
                            placement="right-start">
                  <i class="el-icon-question"/>
                </el-tooltip>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="11">
            <el-form-item label="厂商名称" prop="manufacturerName">
              <el-col :span="22">
                <el-input v-model="form.manufacturerName" placeholder="请输入厂商名称"/>
              </el-col>
              <el-col :span="2" style="padding-left: 5px">
                <el-tooltip class="item" content="支持英文大小写，数字，下划线和中划线" effect="light"
                            placement="right-start">
                  <i class="el-icon-question"/>
                </el-tooltip>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="产品型号" prop="model">
              <el-col :span="22">
                <el-input v-model="form.model" placeholder="请输入产品型号"/>
              </el-col>
              <el-col :span="2" style="padding-left: 5px">
                <el-tooltip class="item" content="建议包含字母或数字来保证可扩展性。支持英文大小写、数字、下划线和中划线"
                            effect="light"
                            placement="right-start">
                  <i class="el-icon-question"/>
                </el-tooltip>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="11">
            <el-form-item label="数据格式" prop="dataFormat">
              <el-col :span="22">
                <el-input v-model="form.dataFormat" placeholder="请输入数据格式"/>
              </el-col>
              <el-col :span="2" style="padding-left: 5px">
                <el-tooltip class="item" content="默认为JSON无需修改。" effect="light" placement="right-start">
                  <i class="el-icon-question"/>
                </el-tooltip>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="设备类型" prop="deviceType">
              <el-col :span="22">
                <el-input v-model="form.deviceType" placeholder="请选择设备类型"/>
              </el-col>
              <el-col :span="2" style="padding-left: 5px">
                <el-tooltip class="item" content="支持英文大小写、数字、下划线和中划线" effect="light"
                            placement="right-start">
                  <i class="el-icon-question"/>
                </el-tooltip>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="11">
            <el-form-item label="状态" prop="status">
              <el-col :span="22">
                <el-select v-model="form.status" placeholder="请选择状态">
                  <el-option v-for="dict in dict.type.business_data_status" :key="dict.value" :label="dict.label"
                             :value="dict.value"></el-option>
                </el-select>
              </el-col>
              <el-col :span="2" style="padding-left: 5px">
                <el-tooltip class="item" content="字典值：默认启用、停用" effect="light" placement="right-start">
                  <i class="el-icon-question"/>
                </el-tooltip>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="22">
            <el-form-item label="产品描述" prop="remark">
              <el-input v-model="form.remark" placeholder="请输入产品描述" type="textarea"/>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 产品模型导入对话框 -->
    <el-dialog :close-on-click-modal="false" :title="upload.title" :visible.sync="upload.open" append-to-body
               width="400px">
      <el-upload ref="upload"
                 :action="upload.url + '?updateSupport=' + upload.updateSupport + '&appId=' + upload.appId + '&templateIdentification=' + upload.templateIdentification + '&status=' + upload.status"
                 :auto-upload="false" :disabled="upload.isUploading" :headers="upload.headers" :limit="1"
                 :on-change="fileUploadChanges" :on-progress="handleFileUploadProgress" :on-success="handleFileSuccess"
                 accept=".json" drag>
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">
          将文件拖到此处,或<em>点击上传</em>,<span>仅允许导入json格式文件。</span>
        </div>

        <div slot="tip" class="el-upload__tip text-center">
          <div slot="tip" class="el-upload__tip_inner">
            <el-col :span="12">
              <el-select v-model="upload.appId" placeholder="请选择应用ID" style="width:95%;margin-bottom: 15px;">
                <el-option v-for="dict in dict.type.link_application_type" :key="dict.value" :label="dict.label"
                           :value="dict.value"></el-option>
              </el-select>
            </el-col>
            <el-col :span="12">
              <el-input v-model="upload.templateIdentification" placeholder="请输入产品模型模板标识" style="width:95%"/>
            </el-col>
            <el-col :span="12" hidden="hidden">
              <el-input v-model="upload.status" hidden="hidden" value="0"/>
            </el-col>
          </div>
          <div slot="tip" class="el-upload__tip text-center">
            <div slot="tip" class="el-upload__tip">
              <el-checkbox v-model="upload.updateSupport"/>
              是否更新已经存在的产品模型数据
            </div>
            <span>仅允许导入json格式文件。</span>
            <el-link :underline="false" style="font-size: 12px; vertical-align: baseline" type="primary"
                     @click="importTemplate">下载模板
            </el-link>
          </div>
        </div>
      </el-upload>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitFileForm">确 定</el-button>
        <el-button @click="upload.open = false">取 消</el-button>
      </div>
    </el-dialog>
    <!-- 快捷生成 -->
    <el-dialog :close-on-click-modal="false" :visible.sync="dialogquick.visiblequick" class="quickproductbox"
               title="产品模型信息" width="900px" @close="closequick('QuickForm')">
      <el-scrollbar style="height: 500px">
        <el-form ref="QuickForm" :model="dialogquick.form" :rules="dialogquick.rules" inline label-width="110px">
          <div class="disply">
            <div class="small">
              <el-form-item label="应用ID" prop="appId">
                <el-col :span="22">
                  <el-select v-model="dialogquick.form.appId" placeholder="请选择应用ID" @change="changeApp">
                    <el-option v-for="dict in dict.type.link_application_type" :key="dict.value" :label="dict.label"
                               :value="dict.value"/>
                  </el-select>
                </el-col>
                <el-col :span="2" style="padding-left: 5px">
                  <el-tooltip class="item" content="应用ID需全局唯一，应用ID创建后无法变更" effect="light"
                              placement="right-start">
                    <i class="el-icon-question"/>
                  </el-tooltip>
                </el-col>
              </el-form-item>
            </div>
            <div class="small">
              <el-form-item label="产品名称" prop="productName">
                <el-col :span="22">
                  <el-input v-model="dialogquick.form.productName" autocomplete="off" placeholder="请输入产品名称"/>
                </el-col>
                <el-col :span="2" style="padding-left: 5px">
                  <el-tooltip content="自定义，支持中文、英文大小写、数字、下划线和中划线，长度[2,64]。" effect="light"
                              placement="right">
                    <i class="el-icon-question"/>
                  </el-tooltip>
                </el-col>
              </el-form-item>
            </div>
            <div class="small">
              <el-form-item label="产品类型" prop="productType">
                <el-col :span="22">
                  <el-select v-model="dialogquick.form.productType" placeholder="请选择产品类型">
                    <el-option v-for="dict in dict.type.link_product_type" :key="dict.value" :label="dict.label"
                               :value="dict.value"/>
                  </el-select>
                </el-col>
                <el-col :span="2" style="padding-left: 5px">
                  <el-tooltip class="item" content="支持以下两种产品类型•普通产品：需直连设备。•网关产品：可挂载子设备。"
                              effect="light"
                              placement="right-start">
                    <i class="el-icon-question"/>
                  </el-tooltip>
                </el-col>
              </el-form-item>
            </div>
            <div class="small">
              <el-form-item label="厂商ID" prop="manufacturerId">
                <el-col :span="22">
                  <el-input v-model="dialogquick.form.manufacturerId" autocomplete="off" placeholder="请输入厂商ID"/>
                </el-col>
                <el-col :span="2" style="padding-left: 5px">
                  <el-tooltip content="支持英文大小写，数字，下划线和中划线，长度[2,50]。" effect="light" placement="right">
                    <i class="el-icon-question"/>
                  </el-tooltip>
                </el-col>
              </el-form-item>
            </div>
            <div class="small">
              <el-form-item label="厂商名称" prop="manufacturerName">
                <el-col :span="22">
                  <el-input v-model="dialogquick.form.manufacturerName" autocomplete="off"
                            placeholder="请输入厂商名称"/>
                </el-col>
                <el-col :span="2" style="padding-left: 5px">
                  <el-tooltip content="支持中文、英文大小写、数字、下划线和中划线，长度[2,64]。" effect="light"
                              placement="right">
                    <i class="el-icon-question"/>
                  </el-tooltip>
                </el-col>
              </el-form-item>
            </div>
            <div class="small">
              <el-form-item label="产品型号" prop="model">
                <el-col :span="22">
                  <el-input v-model="dialogquick.form.model" autocomplete="off" placeholder="请输入产品型号"/>
                </el-col>
                <el-col :span="2" style="padding-left: 5px">
                  <el-tooltip content="建议包含字母或数字来保证可扩展性。支持英文大小写、数字、下划线和中划线，长度[2,50]。"
                              effect="light" placement="right">
                    <i class="el-icon-question"/>
                  </el-tooltip>
                </el-col>
              </el-form-item>
            </div>
            <div class="small">
              <el-form-item label="数据格式" prop="dataFormat">
                <el-col :span="22">
                  <el-input v-model="dialogquick.form.dataFormat" placeholder="请输入数据格式"/>
                </el-col>
                <el-col :span="2" style="padding-left: 5px">
                  <el-tooltip content="默认为JSON无需修改" effect="light" placement="right">
                    <i class="el-icon-question"/>
                  </el-tooltip>
                </el-col>
              </el-form-item>
            </div>
            <div class="small">
              <el-form-item label="设备类型" prop="deviceType">
                <el-col :span="22">
                  <el-input v-model="dialogquick.form.deviceType" placeholder="请输入设备类型"/>
                </el-col>
                <el-col :span="2" style="padding-left: 5px">
                  <el-tooltip class="item" content="支持英文大小写、数字、下划线和中划线，长度[3,50]。" effect="light"
                              placement="right-start">
                    <i class="el-icon-question"/>
                  </el-tooltip>
                </el-col>
              </el-form-item>
            </div>
            <div class="small">
              <el-form-item label="协议类型" prop="protocolType">
                <el-col :span="22">
                  <el-select v-model="dialogquick.form.protocolType" placeholder="请选择协议类型">
                    <el-option v-for="dict in dict.type.link_device_protocol_type" :key="dict.value" :label="dict.label"
                               :value="dict.value"/>
                  </el-select>
                </el-col>
                <el-col :span="2" style="padding-left: 5px">
                  <el-tooltip class="item" content="设备接入平台的协议类型，默认为MQTT无需修改。" effect="light"
                              placement="right-start">
                    <i class="el-icon-question"/>
                  </el-tooltip>
                </el-col>
              </el-form-item>
            </div>
            <div class="small">
              <el-form-item label="产品描述" prop="remark">
                <el-col :span="22">
                  <el-input v-model="dialogquick.form.remark" autocomplete="off" placeholder="请输入产品描述"/>
                </el-col>
                <el-col :span="2" style="padding-left: 5px">
                  <el-tooltip content="产品描述信息。" effect="light" placement="right">
                    <i class="el-icon-question"/>
                  </el-tooltip>
                </el-col>
              </el-form-item>
            </div>
            <div class="small">
              <el-form-item label="产品模板" prop="templateIdentification">
                <el-col :span="22">
                  <el-select v-model="dialogquick.form.templateIdentification" placeholder="请选择产品模型模板">
                    <el-option v-for="item in templateList" :key="item.templateIdentification"
                               :label="item.templateName"
                               :value="item.templateIdentification"/>
                  </el-select>
                </el-col>
                <el-col :span="2" style="padding-left: 5px">
                  <el-tooltip class="item" content="标准化的物模型可以沉淀为平台资产，供用户快速创建产品Profile"
                              effect="light"
                              placement="right-start">
                    <i class="el-icon-question"/>
                  </el-tooltip>
                </el-col>
              </el-form-item>
            </div>
            <!-- services数组 -->
            <div v-for="(item, index) in dialogquick.form.services" :key="index" class="onebox">
              <div class="small">
                <el-form-item :prop="'services.' + index + '.serviceId'" :rules="dialogquick.rules.serviceId"
                              label="服务名称">
                  <el-col :span="22">
                    <el-input v-model="item.serviceId" autocomplete="off" placeholder="请输入服务名称"/>
                  </el-col>
                  <el-col :span="2" style="padding-left: 5px">
                    <el-tooltip
                      content="支持英文小写、数字及下划线，全部小写命名，禁止出现英文大写，多个单词用下划线，分隔长度[2,50]"
                      effect="light"
                      placement="right">
                      <i class="el-icon-question"/>
                    </el-tooltip>
                  </el-col>
                </el-form-item>
              </div>
              <div class="small">
                <el-form-item :prop="'services.' + index + '.description'" :rules="dialogquick.rules.description"
                              label="服务的描述信息">
                  <el-col :span="22">
                    <el-input v-model="item.description" autocomplete="off" placeholder="请输入服务的描述信息"/>
                  </el-col>
                  <el-col :span="2" style="padding-left: 5px">
                    <el-tooltip content="文本描述，不影响实际功能，可配置为空。" effect="light" placement="right">
                      <i class="el-icon-question"/>
                    </el-tooltip>
                  </el-col>
                </el-form-item>
              </div>
              <div class="small">
                <el-form-item :prop="'services.' + index + '.commands'" :rules="dialogquick.rules.commands"
                              label="指令">
                  <el-col :span="22">
                    <el-input v-model="item.commands" autocomplete="off" placeholder="请输入指令"/>
                  </el-col>
                  <el-col :span="2" style="padding-left: 5px">
                    <el-tooltip content=" 指示设备可以执行的命令，如果本服务无命令则配置为[]" effect="light"
                                placement="right">
                      <i class="el-icon-question"/>
                    </el-tooltip>
                  </el-col>
                </el-form-item>
              </div>
              <div class="btn-box">
                <i v-if="index === dialogquick.form.services.length - 1" class="el-icon-circle-plus-outline"
                   @click="addonebox"></i>
                <i v-else class="el-icon-remove-outline" @click="deleteonebox(index)"></i>
              </div>
              <!--  properties数组-->
              <div v-for="(proItem, proindex) in item.properties" :key="proindex" class="twobox">
                <div class="small">
                  <el-form-item :prop="'services.' + index + '.properties.' + proindex + '.datatype'"
                                :rules="dialogquick.rules.datatype" label="指示数据类型">
                    <el-col :span="22">
                      <el-select v-model="proItem.datatype" placeholder="请选择指示数据类型">
                        <el-option v-for="(item, index) in dict.type.link_product_datatype" :key="index"
                                   :label="item.label" :value="item.value">
                        </el-option>
                      </el-select>
                    </el-col>
                    <el-col :span="2" style="padding-left: 5px">
                      <el-tooltip content="取值范围：string、brnary、int、bool、decimal（float和double都可以使用此类型）、timestamp、json、上报数据时，复杂类型数据格式如下：
                      json：自定义json结构体，平台不理解只透传" effect="light" placement="right">
                        <i class="el-icon-question"/>
                      </el-tooltip>
                    </el-col>
                  </el-form-item>
                </div>
                <div class="small">
                  <el-form-item :prop="'services.' + index + '.properties.' + proindex + '.description'"
                                :rules="dialogquick.rules.descriptions" label="属性描述">
                    <el-col :span="22">
                      <el-input v-model="proItem.description" autocomplete="off" placeholder="请输入描述"/>
                    </el-col>
                    <el-col :span="2" style="padding-left: 5px">
                      <el-tooltip content="属性描述,不影响实际功能,可配置为空字符串" effect="light" placement="right">
                        <i class="el-icon-question"/>
                      </el-tooltip>
                    </el-col>
                  </el-form-item>
                </div>
                <div class="small">
                  <el-form-item :prop="'services.' + index + '.properties.' + proindex + '.enumlist'"
                                :rules="dialogquick.rules.enumlist" label="指示枚举值">
                    <el-col :span="22">
                      <el-input v-model="proItem.enumlist" autocomplete="off" placeholder="请输入指示枚举值"/>
                    </el-col>
                    <el-col :span="2" style="padding-left: 5px">
                      <el-tooltip
                        content="如开关状态status可有如下取值'enumList' : ['OPEN','CLOSE'] 目前本字段是非功能性字段，仅起到描述作用。建议准确定义。"
                        effect="light" placement="right">
                        <i class="el-icon-question"/>
                      </el-tooltip>
                    </el-col>
                  </el-form-item>
                </div>
                <div class="small">
                  <el-form-item :prop="'services.' + index + '.properties.' + proindex + '.max'"
                                :rules="dialogquick.rules.max" label="指示最大值">
                    <el-col :span="22">
                      <el-input v-model="proItem.max" autocomplete="off" placeholder="最大值"
                                @change="maxValue(proItem.max, $event)">
                        <el-button slot="prepend" icon="el-icon-minus" @click="handleMinus('max', index)"></el-button>
                        <el-button slot="append" icon="el-icon-plus" @click="handlePlus('max', index)"></el-button>
                      </el-input>
                    </el-col>
                    <el-col :span="2" style="padding-left: 5px">
                      <el-tooltip
                        content="指示最大值,支持长度不超过50的数字。 仅当dataType为int、decimal时生效，逻辑小于等于。"
                        effect="light"
                        placement="right">
                        <i class="el-icon-question"/>
                      </el-tooltip>
                    </el-col>
                  </el-form-item>
                </div>
                <div class="small">
                  <el-form-item :prop="'services.' + index + '.properties.' + proindex + '.maxlength'"
                                :rules="dialogquick.rules.maxlength" label="字符串长度">
                    <el-col :span="22">
                      <el-input v-model="proItem.maxlength" autocomplete="off" placeholder="请输入指示字符串长度"
                                @change="maxValue(proItem.maxlength, $event)">
                        <el-button slot="prepend" icon="el-icon-minus" @click="handleMinus('maxlength', index)">
                        </el-button>
                        <el-button slot="append" icon="el-icon-plus" @click="handlePlus('maxlength', index)">
                        </el-button>
                      </el-input>
                    </el-col>
                    <el-col :span="2" style="padding-left: 5px">
                      <el-tooltip
                        content="指示字符串长度。 仅当dataType为string、binary时生效。（string最长为4093，binary最长为 16000 字节）"
                        effect="light" placement="right">
                        <i class="el-icon-question"/>
                      </el-tooltip>
                    </el-col>
                  </el-form-item>
                </div>
                <div class="small">
                  <el-form-item :prop="'services.' + index + '.properties.' + proindex + '.min'"
                                :rules="dialogquick.rules.min" label="指示最小值">
                    <el-col :span="22">
                      <el-input v-model="proItem.min" autocomplete="off" placeholder="最小值">
                        <el-button slot="prepend" icon="el-icon-minus" @click="handleMinus('min', index)"></el-button>
                        <el-button slot="append" icon="el-icon-plus" @click="handlePlus('min', index)"></el-button>
                      </el-input>
                    </el-col>
                    <el-col :span="2" style="padding-left: 5px">
                      <el-tooltip content="指示最小值。仅当dataType为int、decimal时生效，逻辑大于等于。" effect="light"
                                  placement="right">
                        <i class="el-icon-question"/>
                      </el-tooltip>
                    </el-col>
                  </el-form-item>
                </div>
                <div class="small">
                  <el-form-item :prop="'services.' + index + '.properties.' + proindex + '.step'"
                                :rules="dialogquick.rules.step" label="指示步长">
                    <el-col :span="22">
                      <el-input v-model="proItem.step" autocomplete="off" placeholder="请输入指示步长">
                        <el-button slot="prepend" icon="el-icon-minus" @click="handleMinus('step', index)"></el-button>
                        <el-button slot="append" icon="el-icon-plus" @click="handlePlus('step', index)"></el-button>
                      </el-input>
                    </el-col>
                    <el-col :span="2" style="padding-left: 5px">
                      <el-tooltip content="指示步长，即合法数字间隔" effect="light" placement="right">
                        <i class="el-icon-question"/>
                      </el-tooltip>
                    </el-col>
                  </el-form-item>
                </div>
                <div class="small">
                  <el-form-item :prop="'services.' + index + '.properties.' + proindex + '.required'"
                                :rules="dialogquick.rules.required" label="属性是否必填">
                    <el-col :span="22">
                      <el-select v-model="proItem.required" placeholder="请选择是否必填">
                        <el-option v-for="dict in dict.type.link_product_isRequired" :key="dict.value"
                                   :label="dict.label" :value="parseInt(dict.value)"/>
                      </el-select>
                    </el-col>
                    <el-col :span="2" style="padding-left: 5px">
                      <el-tooltip
                        content="指示本条属性是否必填，取值为0或1，默认取值1（必填）。目前本字段是非功能性字段，仅起到描述作用。"
                        effect="light"
                        placement="right">
                        <i class="el-icon-question"/>
                      </el-tooltip>
                    </el-col>
                  </el-form-item>
                </div>
                <div class="small">
                  <el-form-item :prop="'services.' + index + '.properties.' + proindex + '.propertyCode'"
                                :rules="dialogquick.rules.propertyCode" label="指示属性编码">
                    <el-col :span="22">
                      <el-input v-model="proItem.propertyCode" autocomplete="off" placeholder="请输入指示属性编码"/>
                    </el-col>
                    <el-col :span="2" style="padding-left: 5px">
                      <el-tooltip
                        content="指示属性编码。支持英文小写、数字及下划线，全部小写命名，禁止出现英文大写，多个单词用下划线，分隔长度[2,50]"
                        effect="light"
                        placement="right">
                        <i class="el-icon-question"/>
                      </el-tooltip>
                    </el-col>
                  </el-form-item>
                </div>
                <div class="small">
                  <el-form-item :prop="'services.' + index + '.properties.' + proindex + '.propertyName'"
                                :rules="dialogquick.rules.propertyName" label="指示属性名称">
                    <el-col :span="22">
                      <el-input v-model="proItem.propertyName" autocomplete="off" placeholder="请输入指示属性名称"/>
                    </el-col>
                    <el-col :span="2" style="padding-left: 5px">
                      <el-tooltip content="指示属性名称" effect="light"
                                  placement="right">
                        <i class="el-icon-question"/>
                      </el-tooltip>
                    </el-col>
                  </el-form-item>
                </div>
                <div class="small">
                  <el-form-item :prop="'services.' + index + '.properties.' + proindex + '.unit'"
                                :rules="dialogquick.rules.unit" label="指示单位">
                    <el-col :span="22">
                      <el-input v-model="proItem.unit" autocomplete="off" placeholder="请输入指示单位"/>
                    </el-col>
                    <el-col :span="2" style="padding-left: 5px">
                      <el-tooltip
                        content="指示单位,支持长度不超过50。 取值根据参数确定，如：• 温度单位：“C”或“K” • 百分比单位：“%” • 压强单位：“Pa”或“kPa”"
                        effect="light" placement="right">
                        <i class="el-icon-question"/>
                      </el-tooltip>
                    </el-col>
                  </el-form-item>
                </div>
                <div class="small">
                  <el-form-item :prop="'services.' + index + '.properties.' + proindex + '.method'"
                                :rules="dialogquick.rules.method" label="指示访问模式">
                    <el-col :span="22">
                      <!--                      <el-input v-model="proItem.method" autocomplete="off" placeholder="请输入指示访问模式" />-->
                      <el-select v-model="proItem.method" placeholder="请选择访问模式">
                        <el-option v-for="dict in methodlist" :key="dict.value" :label="dict.label"
                                   :value="dict.value"/>
                      </el-select>
                    </el-col>
                    <el-col :span="2" style="padding-left: 5px">
                      <el-tooltip content="	指示访问模式。R:可读；W:可写；E属性值更改时上报数据,取值范围：R、RW、RE、RWE"
                                  effect="light"
                                  placement=" right">
                        <i class="el-icon-question"/>
                      </el-tooltip>
                    </el-col>

                  </el-form-item>
                </div>
                <div class="btn-box">
                  <i v-if="proindex === item.properties.length - 1" class="el-icon-circle-plus-outline"
                     @click="addtwobox(index)"></i>
                  <i v-else class="el-icon-remove-outline" @click="deletetwobox(index, proindex)"></i>
                </div>
              </div>
            </div>
          </div>
        </el-form>
      </el-scrollbar>
      <div slot="footer" class="dialog-footer" style="margin-top: 10px; display: flex; justify-content: flex-end">
        <el-button type="primary" @click="onSave">保存</el-button>
        <el-button type="" @click="dialogquick.visiblequick = false">取消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  addProduct,
  delProduct,
  generateProductJson,
  getProduct,
  listProduct,
  productEmpowerment,
  updateProduct,
} from "@/api/link/product/product";
import {listProductTemplate} from "@/api/link/product/productTemplate";
import {getToken} from "@/utils/auth";

export default {
  name: "Product",
  dicts: [
    "link_application_type",
    "link_product_device_type",
    "link_product_type",
    "link_device_protocol_type",
    "link_product_datatype",
    "link_product_isRequired",
    "business_data_status"
  ],
  data() {
    return {
      //文件上传
      file: {},
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 产品管理表格数据
      productList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        productName: null,
        manufacturerId: null,
        manufacturerName: null,
        model: null,
      },
      // 产品模板
      templateList: [],
      // 产品模型导入参数
      upload: {
        // 是否显示弹出层（产品模型导入）
        open: false,
        // 弹出层标题（产品模型导入）
        title: "",
        // 是否禁用上传
        isUploading: false,
        // 应用ID
        appId: "",
        // 产品模型模板
        templateIdentification: "",
        // 状态
        status: "0",
        // 是否更新已经存在的用户数据
        updateSupport: 0,
        // 设置上传的请求头部
        headers: {Authorization: "Bearer " + getToken()},
        // 上传的地址
        url:
          process.env.VUE_APP_BASE_API + "/link/product/importProductJsonFile",
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        appId: [
          {required: true, message: "应用ID不能为空", trigger: "change"},
        ],
        productName: [
          {required: true, message: "产品名称不能为空", trigger: "blur"},
          {min: 2, max: 64, message: '产品名称长度必须介于 2 和 64 之间', trigger: 'blur'},
          {
            //pattern: /^(?!_)(?!.*?_$)(?!-)(?!.*?-$)[\u4e00-\u9fa5a-zA-Z0-9_-]+$/,
            //message: "中文、英文大小写、数字、下划线和中划线，不能以下划线中划线开头和结尾，长度[2,64]",
            pattern: /^[\u4e00-\u9fa5a-zA-Z0-9_-]+$/,
            message: "中文、英文大小写、数字、下划线和中划线，长度[2,64]",
            trigger: "blur"
          }
        ],
        productIdentification: [
          {required: true, message: "产品标识不能为空", trigger: "blur"},
          {min: 2, max: 50, message: '产品标识长度必须介于 2 和 50 之间', trigger: 'blur'},
          {
            pattern: /^[a-zA-Z0-9_-]+$/,
            message: "英文大小写、数字、下划线和中划线，长度[2,50]",
            trigger: "blur"
          }
        ],
        productType: [
          {required: true, message: "产品类型不能为空", trigger: "change"},
        ],
        manufacturerId: [
          {required: true, message: "厂商ID不能为空", trigger: "blur"},
          {min: 2, max: 50, message: '厂商ID长度必须介于 2 和 50 之间', trigger: 'blur'},
          {
            pattern: /^[a-zA-Z0-9_-]+$/,
            message: "英文大小写、数字、下划线和中划线，长度[2,50]",
            trigger: "blur"
          }
        ],
        manufacturerName: [
          {required: true, message: "厂商名称不能为空", trigger: "blur"},
          {min: 2, max: 64, message: '厂商名称长度必须介于 2 和 64 之间', trigger: 'blur'},
          {
            pattern: /^[\u4e00-\u9fa5a-zA-Z0-9_-]+$/,
            message: "中文、英文大小写、数字、下划线和中划线，长度[2,64]",
            trigger: "blur"
          }
        ],
        model: [
          {required: true, message: "产品型号不能为空", trigger: "blur"},
          {min: 2, max: 50, message: '产品型号长度必须介于 2 和 50 之间', trigger: 'blur'},
          {
            pattern: /^[a-zA-Z0-9_-]+$/,
            message: "英文大小写、数字、下划线和中划线，长度[2,50]",
            trigger: "blur"
          }
        ],
        dataFormat: [
          {required: true, message: "数据格式不能为空", trigger: "change"},
        ],
        deviceType: [
          {required: true, message: "设备类型不能为空", trigger: "change"},
          {min: 3, max: 50, message: '设备类型长度必须介于 3 和 50 之间', trigger: 'blur'},
          {
            pattern: /^[a-zA-Z0-9_-]+$/,
            message: "英文大小写、数字、下划线和中划线，长度[3,50]",
            trigger: "blur"
          }
        ],
        protocolType: [
          {required: true, message: "设备接入平台的协议类型不能为空", trigger: "change"},
        ],
        status: [
          {required: true, message: "状态不能为空", trigger: "change"},
        ],
      },
      //快捷生成
      content: {},
      dialogquick: {
        visiblequick: false,
        form: {
          appId: "",
          dataFormat: "JSON",
          deviceType: "",
          manufacturerId: "",
          manufacturerName: "",
          model: "",
          productName: "",
          productType: "",
          protocolType: "MQTT",
          remark: "",
          templateIdentification: "",
          services: [
            {
              commands: "",
              description: "",
              serviceId: "",
              properties: [
                {
                  datatype: "",
                  description: "",
                  enumlist: "",
                  max: '',
                  maxlength: 0,
                  min: '',
                  name: "",
                  required: 0,
                  step: 0,
                  unit: "",
                  method: "",
                },
              ],
            },
          ],
        },
        rules: {
          appId: [
            {required: true, message: "应用ID不能为空", trigger: "change"},
          ],
          productName: [
            {required: true, message: "产品名称不能为空", trigger: "blur"},
            {min: 2, max: 64, message: '产品名称长度必须介于 2 和 64 之间', trigger: 'blur'},
            {
              pattern: /^[\u4e00-\u9fa5a-zA-Z0-9_-]+$/,
              message: "中文、英文大小写、数字、下划线和中划线，长度[2,64]",
              trigger: "blur"
            }
          ],
          productType: [{required: true, message: "产品类型不能为空", trigger: "change"}],
          manufacturerId: [
            {required: true, message: "厂商ID不能为空", trigger: "blur"},
            {min: 2, max: 50, message: '厂商ID长度必须介于 2 和 50 之间', trigger: 'blur'},
            {
              pattern: /^[a-zA-Z0-9_-]+$/,
              message: "英文大小写、数字、下划线和中划线，长度[2,50]",
              trigger: "blur"
            }
          ],
          manufacturerName: [
            {required: true, message: "厂商名称不能为空", trigger: "blur"},
            {min: 2, max: 64, message: '厂商名称长度必须介于 2 和 64 之间', trigger: 'blur'},
            {
              pattern: /^[\u4e00-\u9fa5a-zA-Z0-9_-]+$/,
              message: "中文、英文大小写、数字、下划线和中划线，长度[2,64]",
              trigger: "blur"
            }
          ],
          dataFormat: [{required: true, message: "数据格式不能为空", trigger: "change"}],
          deviceType: [
            {required: true, message: "设备类型不能为空", trigger: "blur"},
            {min: 2, max: 50, message: '设备类型长度必须介于 3 和 50 之间', trigger: 'blur'},
            {
              pattern: /^[a-zA-Z0-9_-]+$/,
              message: "英文大小写、数字、下划线和中划线，长度[3,50]",
              trigger: "blur"
            }
          ],
          model: [
            {required: true, message: "产品型号不能为空", trigger: "blur"},
            {min: 2, max: 50, message: '产品型号长度必须介于 2 和 50 之间', trigger: 'blur'},
            {
              pattern: /^[a-zA-Z0-9_-]+$/,
              message: "英文大小写、数字、下划线和中划线，长度[2,50]",
              trigger: "blur"
            }
          ],
          protocolType: [{required: true, message: "协议类型不能为空", trigger: "change"}],
          status: [{required: true, message: "请输入状态", trigger: "blur"}],
          version: [{required: true, message: "请输入版本", trigger: "blur"}],
          remark: [{message: "请输入备注", trigger: "blur"}],
          //services
          serviceId: [
            {required: true, message: "服务名称不能为空", trigger: "blur"},
            {min: 2, max: 50, message: '服务名称长度必须介于 2 和 50 之间', trigger: 'blur'},
            {
              pattern: /^[a-z0-9_]+$/,
              message: "英文小写、数字、下划线，长度[2,50]",
              trigger: "blur"
            }
          ],
          description: [{required: false, message: "服务描述不能为空", trigger: "blur"},],
          commands: [{required: true, message: "指令不能为空", trigger: "blur"},],
          statuss: [{required: true, message: "请输入状态", trigger: "blur"},],
          //properties
          datatype: [{required: true, message: "指示数据类型不能为空", trigger: "change"},],
          descriptions: [{message: "请输入属性描述", trigger: "blur"},],
          enumlist: [{required: true, message: "指示枚举值不能为空", trigger: "blur"},],
          maxlength: [
            {required: true, message: "字符串长度不能为空", trigger: "blur"},
            {
              pattern: /^[0-9]+$/,
              message: "请输入合法数字",
              trigger: "blur"
            }
          ],
          max: [
            {
              pattern: /^[0-9]+$/,
              message: "请输入合法数字",
              trigger: "blur"
            }
          ],
          min: [
            {
              pattern: /^[0-9]+$/,
              message: "请输入合法数字",
              trigger: "blur"
            }
          ],
          step: [
            {required: true, message: "请输入指示步长", trigger: "blur"},
            {
              pattern: /^[0-9]+$/,
              message: "请输入合法数字",
              trigger: "blur"
            }
          ],
          required: [{required: true, message: "是否必须不能为空", trigger: "blur"},],
          propertyCode: [{required: true, message: "指示属性编码不能为空", trigger: "blur"}],
          propertyName: [{required: true, message: "指示属性名称不能为空", trigger: "blur"}],
          method: [{required: true, message: "指示访问模式不能为空", trigger: " change"}],
        },
      },
      methodlist: [
        {value: "R", label: "R"},
        {value: "RW", label: "RW"},
        {value: "RE", label: "RE"},
        {value: "RWE", label: "RWE"}
      ],
    };
  },
  created() {
    this.getList();
  },
  watch: {},
  methods: {
    //产品赋能
    async productEmpowerment(row) {
      const ids = row.id || this.ids;
      const isConfirm = await this.$modal.confirm('是否赋能"' + ids + '"的数据项？');
      if (isConfirm) {
        const {code, msg} = await productEmpowerment(ids);
        if (code === 200) {
          this.$modal.msgSuccess(msg);
          this.getList()
        } else {
          this.$modal.msgWarning(msg);
        }
      }
    },
    //加减操作
    handleMinus(value, index) {
      if (value === 'max') {
        this.dialogquick.form.services[index].properties[index].max--
        if (this.dialogquick.form.services[index].properties[index].max < 0) {
          this.dialogquick.form.services[index].properties[index].max = 0
        }
      } else if (value === 'maxlength') {
        this.dialogquick.form.services[index].properties[index].maxlength--
        if (this.dialogquick.form.services[index].properties[index].maxlength < 0) {
          this.dialogquick.form.services[index].properties[index].maxlength = 0
        }
      } else if (value === 'min') {
        this.dialogquick.form.services[index].properties[index].min--
        if (this.dialogquick.form.services[index].properties[index].min < 0) {
          this.dialogquick.form.services[index].properties[index].min = 0
        }
      } else if (value === 'step') {
        this.dialogquick.form.services[index].properties[index].step--
        if (this.dialogquick.form.services[index].properties[index].step < 0) {
          this.dialogquick.form.services[index].properties[index].step = 0
        }
      }
    },
    handlePlus(value, index) {
      if (value === 'max') {
        this.dialogquick.form.services[index].properties[index].max++
      } else if (value === 'maxlength') {
        this.dialogquick.form.services[index].properties[index].maxlength++
      } else if (value === 'min') {
        this.dialogquick.form.services[index].properties[index].min++
      } else if (value === 'step') {
        this.dialogquick.form.services[index].properties[index].step++
      }
    },
    //最大值||最大长度极值判定
    maxValue(value, event) {
      console.log(value, event);
      if (value < 0) {
        this.dialogquick.form.services.properties.max = 0;
        this.$message.error('最小为0')
      }
    },
    //关闭快捷生成弹窗是 同时关闭form的表单验证
    closequick(QuickForm) {
      this.$refs[QuickForm].resetFields();
    },
    //点击快捷生成
    onQuick() {
      this.dialogquick.visiblequick = true;
    },
    onSave() {
      this.$refs.QuickForm.validate((valid) => {
        if (!valid) {
          this.$toast.fail("有信息未填写");
          return;
        }
        this.isLoading = true;
        // this.dialogquick.form.productType = Number(
        //   this.dialogquick.form.productType
        // );
        this.dialogquick.form.services.forEach((item) => {
          // item.commands=item.commands&&item.commands.split(',');
          //TODO 事件先清空
          item.commands = []
          item.properties.forEach((itempro) => {
            itempro.maxlength = Number(itempro.maxlength);
            itempro.required = Number(itempro.required);
            itempro.step = Number(itempro.step);
          });
        });
        let obj = JSON.stringify(this.dialogquick.form)
        this.content = obj
        let data = {
          appId: this.dialogquick.form.appId,
          templateIdentification: this.dialogquick.form.templateIdentification,
          status: 0,
          content: this.content
        }
        generateProductJson(data).then(res => {
          console.log(res);
          this.$message.success('保存成功')
          this.getList();
        }).catch((res, err) => {
          console.log(res);
          this.$message.error(res.msg || res.message);
        });
        //this.$message.success('保存成功')
        this.dialogquick.visiblequick = false;
      });
    },
    addonebox() {
      this.dialogquick.form.services.push({
        commands: "",
        description: "",
        serviceId: "",
        status: "",
        properties: [
          {
            datatype: "",
            description: "",
            enumlist: "",
            max: "",
            maxlength: 0,
            min: "",
            name: "",
            required: 0,
            step: 0,
            unit: "",
            method: "",
          },
        ],
      });
    },
    deleteonebox(i) {
      this.dialogquick.form.services.splice(i, 1);
    },
    addtwobox(i) {
      this.dialogquick.form.services.forEach((item, index) => {
        if (index === i) {
          item.properties.push({
            datatype: "",
            description: "",
            enumlist: "",
            max: "",
            maxlength: 0,
            min: "",
            name: "",
            required: 0,
            step: 0,
            unit: "",
            method: "",
          });
        }
      });
    },
    deletetwobox(i, proi) {
      this.dialogquick.form.services.forEach((item, index) => {
        if (index === i) {
          item.properties.splice(proi, 1);
        }
      });
    },
    /** 查询产品管理列表 */
    getList() {
      this.loading = true;
      listProduct(this.queryParams).then((response) => {
        this.productList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    // 切换应用
    changeApp(row) {
      this.templateList = [];
      this.getTemplateList(row);
    },
    // 查询模板列表
    getTemplateList(appId) {
      let params = {
        appId: appId,
        status: 0
      };
      listProductTemplate(params).then(res => {
        this.templateList = res.rows;
      })
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        id: null,
        appId: null,
        templateIdentification: null,
        productName: null,
        productIdentification: null,
        productType: null,
        manufacturerId: null,
        manufacturerName: null,
        model: null,
        dataFormat: "JSON",
        deviceType: null,
        protocolType: "MQTT",
        status: null,
        remark: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null,
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      console.log(selection)
      this.ids = selection.map((item) => item.id);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加产品";
    },
    /** 复制按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids;
      getProduct(id).then((response) => {
        this.form = response.data;
        this.form.id = null;
        this.form.productIdentification = null;
        this.open = true;
        this.title = "复制产品";
        if (!this.isEmptyStr(response.data.appId) && this.templateList.length == 0) {
          this.getTemplateList(response.data.appId)
        }
      });
    },
    isEmptyStr(s) {
      if (s == undefined || s == null || s == '') {
        return true
      }
      return false
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate((valid) => {
        if (valid) {
          if (this.form.id != null) {
            updateProduct(this.form).then((response) => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addProduct(this.form).then((response) => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal
        .confirm('是否确认删除产品管理编号为"' + ids + '"的数据项？')
        .then(function () {
          return delProduct(ids);
        })
        .then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        })
        .catch(() => {
        });
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download(
        "link/product/export",
        {
          ...this.queryParams,
        },
        `link_product_${new Date().getTime()}.json`
      );
    },
    /** 导入按钮操作 */
    handleImport() {
      this.upload.title = "产品模型导入";
      this.upload.open = true;
    },
    /** 下载模板操作 */
    importTemplate() {
      this.download(
        "link/product/importTemplate",
        {
          ...this.queryParams,
        },
        `product_${new Date().getTime()}.json`
      );
    },
    // 文件上传中处理
    handleFileUploadProgress(event, file, fileList) {
      this.upload.isUploading = true;
    },
    // 文件上传成功处理
    handleFileSuccess(response, file, fileList) {
      this.upload.open = false;
      this.upload.isUploading = false;
      this.$refs.upload.clearFiles();
      this.$alert(response.msg, "导入结果", {dangerouslyUseHTMLString: true});
      this.getList();
    },
    //文件状态改变
    fileUploadChanges(file, fileList) {
      this.file = file
    },
    // 提交上传文件
    submitFileForm() {
      this.$refs.upload.submit();
    },
    handleProductDetail: function (row) {
      const productId = row.id;
      this.$router.push("/link/product-detail/product/" + productId);
    },
  },
};
</script>
<style lang="scss" scoped>
.el-input {
  width: 200px;
}

.el-select {
  width: 200px;
}

.el-dialog {
  width: 300px;
}

.disply {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;

  .small {
    width: calc(50% - 5px);
  }
}

.onebox {
  position: relative;
  border: 1px solid rgb(192, 191, 191);
  border-radius: 4px;
  display: flex;
  flex-wrap: wrap;
  padding: 29px;
  justify-content: space-between;

  .small {
    width: calc(50% - 5px);
  }
}

.twobox {
  position: relative;
  border: 1px solid rgb(192, 191, 191);
  border-radius: 4px;
  display: flex;
  flex-wrap: wrap;
  padding: 29px;
  justify-content: space-between;
  margin-top: 10px;

  .small {
    width: calc(50% - 5px);
  }
}

.btn-box {
  position: absolute;
  top: 10px;
  right: 10px;
}

.el-scrollbar__wrap {
  overflow-y: hidden !important;
}

.uploadfile ::v-deep .el-dialog__header {
  border-bottom: 1px solid #606266;
}

.uploadfile ::v-deep .el-dialog__footer {
  border-top: 1px solid #606266;
}

.el-upload__tip_inner {
  width: 100%;
  display: flex;
  justify-content: space-around;
}
</style>
