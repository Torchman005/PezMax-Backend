<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="被举报文件id" prop="fileId">
        <el-input
            v-model="queryParams.fileId"
            placeholder="请输入被举报文件id"
            clearable
            @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="举报用户id" prop="userId">
        <el-input
            v-model="queryParams.userId"
            placeholder="请输入举报用户id"
            clearable
            @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="审核结果" prop="result">
        <el-select v-model="queryParams.result" placeholder="请选择审核结果" clearable>
          <el-option label="未审核" value="0" />
          <el-option label="属实" value="1" />
          <el-option label="不属实" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
            type="primary"
            plain
            icon="Plus"
            @click="handleAdd"
            v-hasPermi="['datum:report:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="success"
            plain
            icon="Edit"
            :disabled="single"
            @click="handleUpdate"
            v-hasPermi="['datum:report:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="danger"
            plain
            icon="Delete"
            :disabled="multiple"
            @click="handleDelete"
            v-hasPermi="['datum:report:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="warning"
            plain
            icon="Download"
            @click="handleExport"
            v-hasPermi="['datum:report:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="reportList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="举报id" align="center" prop="reportId" />
      <el-table-column label="被举报文件" align="center" prop="fileId">
        <template #default="{ row }">
          <span>{{ row.fileId }}</span>
          <span v-if="row.fileName"> - {{ row.fileName }}</span>
        </template>
      </el-table-column>
      <el-table-column label="举报用户id" align="center" prop="userId" />
      <el-table-column label="被举报原因" align="center" prop="reason" />
      <el-table-column label="审核结果" align="center" prop="result">
        <template #default="scope">
          <dict-tag :options="resultOptions" :value="scope.row.result" />
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
              v-if="scope.row.result === '0'"
              link
              type="warning"
              icon="Check"
              @click="handleAudit(scope.row)"
              v-hasPermi="['datum:report:audit']"
          >审核</el-button>
          <el-button link type="primary" icon="View" @click="handlePreview(scope.row)">预览</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['datum:report:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['datum:report:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
        v-show="total>0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
    />

    <!-- 添加或修改举报对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="reportRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="被举报文件id" prop="fileId">
              <el-input v-model="form.fileId" placeholder="请输入被举报文件id" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="举报用户id" prop="userId">
              <el-input v-model="form.userId" placeholder="请输入举报用户id" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="被举报原因" prop="reason">
              <el-input v-model="form.reason" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="审核结果" prop="result">
              <el-select v-model="form.result" placeholder="请选择审核结果" clearable>
                <el-option label="未审核" value="0" />
                <el-option label="属实" value="1" />
                <el-option label="不属实" value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 审核举报对话框 -->
    <el-dialog title="举报审核" v-model="auditDialogVisible" width="500px" append-to-body>
      <el-form ref="auditRef" :model="auditForm" :rules="auditRules" label-width="100px">
        <el-form-item label="审核结果" prop="result">
          <el-radio-group v-model="auditForm.result">
            <el-radio label="1">属实</el-radio>
            <el-radio label="2">不属实</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核备注" prop="remark">
          <el-input type="textarea" v-model="auditForm.remark" placeholder="请输入审核备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitAudit">确 定</el-button>
          <el-button @click="auditDialogVisible = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 文件预览对话框（含举报原因与审核操作） -->
    <el-dialog title="文件预览" v-model="previewOpen" width="80%" top="4vh" append-to-body @close="closePreview">
      <div class="preview-toolbar">
        <div class="preview-info">
          <span>预览：{{ previewRow?.fileName || previewRow?.fileUrl }}</span>
          <el-tag v-if="previewType==='pdf'" type="info" style="margin-left:8px;">PDF</el-tag>
          <el-tag v-else-if="previewType==='jpg' || previewType==='jpeg'" type="success" style="margin-left:8px;">JPG</el-tag>
        </div>
        <div class="preview-actions">
          <el-button size="small" type="danger" @click="handlePreviewAudit(true)" :loading="previewLoading">举报属实</el-button>
          <el-button size="small" type="success" @click="handlePreviewAudit(false)" :loading="previewLoading">举报不属实</el-button>
        </div>
      </div>
      <!-- 举报原因显示 -->
      <div class="preview-report-reason" v-if="previewRow?.reason">
        <span class="reason-label">举报原因：</span>
        <span class="reason-text">{{ previewRow.reason }}</span>
      </div>
      <div class="preview-controls">
        <el-button size="small" @click="prevPreviewPage" :disabled="previewPage<=1">上一页</el-button>
        <el-button size="small" @click="nextPreviewPage" :disabled="previewType !== 'pdf'">下一页</el-button>
        <span v-if="previewType==='pdf'" style="margin-left:12px;">第{{ previewPage }}页</span>
      </div>
      <div class="preview-container">
        <template v-if="previewType === 'pdf'">
          <iframe :src="previewUrl" frameborder="0" width="100%" height="100%"></iframe>
        </template>
        <template v-else-if="previewType === 'jpg' || previewType === 'jpeg'">
          <img :src="previewUrl" class="file-preview-image" />
        </template>
        <template v-else>
          <div class="preview-empty">仅支持 JPG/PDF 文件预览</div>
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="Report">
import { ref, reactive, toRefs, getCurrentInstance, computed, nextTick } from 'vue'
import { listReport, getReport, delReport, addReport, updateReport, auditReport } from "@/api/datum/report"
import { getFile } from "@/api/datum/file"

const { proxy } = getCurrentInstance()

const reportList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const auditDialogVisible = ref(false)
const auditRef = ref(null)
const fileNameCache = reactive({})

// 审核结果字典
const resultOptions = ref([
  { label: '未审核', value: '0' },
  { label: '属实', value: '1' },
  { label: '不属实', value: '2' }
])

// 审核表单
const auditForm = reactive({
  reportId: null,
  result: '1',
  remark: ''
})

const auditRules = {
  result: [{ required: true, message: '请选择审核结果', trigger: 'change' }]
}

// 预览相关状态
const previewOpen = ref(false)
const previewRow = ref(null)
const previewPage = ref(1)
const previewType = ref("")
const previewLoading = ref(false)

const previewUrl = computed(() => {
  if (!previewRow.value?.fileUrl) {
    return ""
  }
  const base = previewRow.value.fileUrl
  if (previewType.value === "pdf") {
    return `${base}#page=${previewPage.value}`
  }
  return base
})

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    fileId: null,
    userId: null,
    reason: null,
    result: null,
  },
  rules: {
    fileId: [
      { required: true, message: "被举报文件id不能为空", trigger: "blur" }
    ],
    userId: [
      { required: true, message: "举报用户id不能为空", trigger: "blur" }
    ],
    reason: [
      { required: true, message: "被举报原因不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询举报列表 */
async function getList() {
  loading.value = true
  try {
    const response = await listReport(queryParams.value)
    reportList.value = response.rows || []
    total.value = response.total
    await loadReportFileNames(reportList.value)
  } finally {
    loading.value = false
  }
}
//filename查询/
//fc/
async function loadReportFileNames(rows) {
  const idsToLoad = [...new Set(rows.map(row => row.fileId).filter(Boolean))].filter(fileId => !fileNameCache[fileId])
  if (idsToLoad.length) {
    await Promise.all(idsToLoad.map(fileId =>
      getFile(fileId)
        .then(res => {
          fileNameCache[fileId] = res?.data?.fileName || ''
        })
        .catch(() => {
          fileNameCache[fileId] = ''
        })
    ))
  }
  rows.forEach(row => {
    row.fileName = fileNameCache[row.fileId] || ''
  })
}

// 取消按钮
function cancel() {
  open.value = false
  reset()
}

// 表单重置
function reset() {
  form.value = {
    reportId: null,
    fileId: null,
    userId: null,
    reason: null,
    result: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null
  }
  proxy.resetForm("reportRef")
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.reportId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加举报"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _reportId = row.reportId || ids.value
  getReport(_reportId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改举报"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["reportRef"].validate(valid => {
    if (valid) {
      if (form.value.reportId != null) {
        updateReport(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addReport(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _reportIds = row.reportId || ids.value
  proxy.$modal.confirm('是否确认删除举报编号为"' + _reportIds + '"的数据项？').then(function() {
    return delReport(_reportIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('datum/report/export', {
    ...queryParams.value
  }, `report_${new Date().getTime()}.xlsx`)
}

/** 打开审核弹窗 */
function handleAudit(row) {
  auditForm.reportId = row.reportId
  auditForm.result = '1'
  auditForm.remark = ''
  auditDialogVisible.value = true
}

/** 提交审核 */
function submitAudit() {
  auditRef.value.validate(valid => {
    if (valid) {
      auditReport({
        reportId: auditForm.reportId,
        result: auditForm.result,
        remark: auditForm.remark
      }).then(() => {
        proxy.$modal.msgSuccess("审核成功")
        auditDialogVisible.value = false
        getList()
      }).catch(() => {})
    }
  })
}

// ========== 文件预览功能 ==========

/** 判断文件是否可预览 */
function isPreviewable(row) {
  const url = row.fileUrl || ''
  const format = (row.fileFormat || row.fileUrl || "").toString().toLowerCase()
  return format.includes("pdf") || format.includes("jpg") || format.includes("jpeg")
}

/** 获取预览类型 */
function getPreviewType(row) {
  const format = (row.fileFormat || row.fileUrl || "").toString().toLowerCase()
  if (format.includes("pdf")) {
    return "pdf"
  }
  if (format.includes("jpg") || format.includes("jpeg")) {
    return "jpg"
  }
  return ""
}

/** 打开预览弹窗 */
function openPreview(row) {
  previewRow.value = row
  previewType.value = getPreviewType(row)
  previewPage.value = 1
  previewOpen.value = true
}

/** 预览按钮操作 */
function handlePreview(row) {
  // 先加载文件信息获取 fileUrl
  if (!row.fileUrl) {
    getFile(row.fileId).then(res => {
      const fileData = res?.data || {}
      row.fileUrl = fileData.fileUrl
      row.fileFormat = fileData.fileFormat
      row.fileName = row.fileName || fileData.fileName
      if (!isPreviewable(row)) {
        proxy.$modal.msgWarning("当前仅支持 JPG/PDF 文件预览")
        return
      }
      openPreview(row)
    }).catch(() => {
      proxy.$modal.msgError("获取文件信息失败")
    })
  } else {
    if (!isPreviewable(row)) {
      proxy.$modal.msgWarning("当前仅支持 JPG/PDF 文件预览")
      return
    }
    openPreview(row)
  }
}

/** 关闭预览 */
function closePreview() {
  previewOpen.value = false
  previewRow.value = null
  previewPage.value = 1
  previewType.value = ""
}

/** 上一页 */
function prevPreviewPage() {
  if (previewPage.value > 1) {
    previewPage.value -= 1
  }
}

/** 下一页 */
function nextPreviewPage() {
  if (previewType.value === "pdf") {
    previewPage.value += 1
  }
}

/** 预览弹窗中审核：举报属实 / 举报不属实 */
function handlePreviewAudit(pass) {
  if (!previewRow.value) {
    return
  }
  previewLoading.value = true
  // pass=true: 举报属实 → 后端将 file_status 改为 2(未通过)
  // pass=false: 举报不属实 → 后端将 file_status 改为 1(通过)
  const reportResult = pass ? '1' : '2'

  auditReport({
    reportId: previewRow.value.reportId,
    result: reportResult,
    remark: previewRow.value.remark || ''
  }).then(() => {
    proxy.$modal.msgSuccess(pass ? "举报属实，文件状态已标记为未通过" : "举报不属实，文件状态已恢复为通过")
    closePreview()
    getList()
  }).catch(() => {
    proxy.$modal.msgError("操作失败")
  }).finally(() => {
    previewLoading.value = false
  })
}

getList()
</script>

<style scoped>
.preview-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}
.preview-actions {
  display: flex;
  gap: 8px;
}
.preview-report-reason {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 12px;
  padding: 10px 14px;
  background: #fdf6ec;
  border: 1px solid #faecd8;
  border-radius: 4px;
  color: #e6a23c;
  font-size: 14px;
  word-break: break-all;
}
.reason-label {
  font-weight: 600;
  white-space: nowrap;
  color: #e6a23c;
}
.reason-text {
  color: #303133;
}
.preview-controls {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  margin-bottom: 12px;
}
.preview-container {
  width: 100%;
  min-height: 60vh;
  height: calc(80vh - 120px);
  background: #fff;
  border: 1px solid #ebeef5;
  padding: 12px;
  overflow: hidden;
}
.preview-container iframe,
.preview-container img {
  display: block;
  width: 100%;
  height: 100%;
  border: none;
}
.file-preview-image {
  object-fit: contain;
}
.preview-empty {
  color: #999;
  text-align: center;
  padding: 24px;
}
</style>
