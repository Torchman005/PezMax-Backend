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
      <el-table-column label="被举报原因" align="center" show-overflow-tooltip>
        <template #default="scope">
          <span>{{ scope.row.reason && scope.row.reason.length > 15 ? scope.row.reason.substring(0, 15) + '...' : scope.row.reason }}</span>
        </template>
      </el-table-column>
      <el-table-column label="时间" align="center" width="110">
        <template #default="scope">
          <span>{{ formatDateTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="审核结果" align="center" prop="result">
        <template #default="scope">
          <dict-tag :options="resultOptions" :value="scope.row.result" />
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="280">
        <template #default="scope">
          <el-button
              v-if="scope.row.result === '0'"
              link
              type="danger"
              icon="Check"
              @click="handlePass(scope.row)"
              v-hasPermi="['datum:report:audit']"
          >通过</el-button>
          <el-button
              v-if="scope.row.result === '0'"
              link
              type="success"
              icon="Close"
              @click="handleReject(scope.row)"
              v-hasPermi="['datum:report:audit']"
          >拒绝</el-button>
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

    <!-- 举报回执对话框 -->
    <el-dialog title="举报回执" v-model="auditDialogVisible" width="500px" append-to-body>
      <el-form ref="auditRef" :model="auditForm" :rules="auditRules" label-width="100px">
        <el-form-item label="回执内容" prop="receipt">
          <el-input type="textarea" :rows="4" v-model="auditForm.receipt" :placeholder="auditForm.defaultPlaceholder" />
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
      <!-- 回执内容输入 -->
      <div class="preview-receipt-input">
        <el-input
          v-model="previewReceipt"
          type="textarea"
          :rows="2"
          placeholder="请输入回执内容（通过默认：资料已下架；拒绝默认：举报未通过，请联系管理员）"
        />
      </div>
      <div class="preview-toolbar">
        <div class="preview-info">
          <span>预览：{{ previewRow?.fileName || previewRow?.fileUrl }}</span>
          <el-tag v-if="previewType==='pdf'" type="info" style="margin-left:8px;">PDF</el-tag>
          <el-tag v-else-if="previewType==='image'" type="success" style="margin-left:8px;">{{ previewFormatLabel }}</el-tag>
        </div>
        <div class="preview-actions">
          <el-button size="small" type="danger" @click="handlePreviewAudit(true)" :loading="previewLoading">通过</el-button>
          <el-button size="small" type="success" @click="handlePreviewAudit(false)" :loading="previewLoading">拒绝</el-button>
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
        <template v-else-if="previewType === 'image'">
          <img :src="previewUrl" class="file-preview-image" />
        </template>
        <template v-else>
          <div class="preview-empty">仅支持图片（JPG/PNG/GIF/WebP/BMP/SVG/ICO）和 PDF 文件预览</div>
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="Report">
import { ref, reactive, toRefs, getCurrentInstance, computed, nextTick } from 'vue'
import { listReport, getReport, delReport, addReport, updateReport, auditReport } from "@/api/datum/report"
import { getFile } from "@/api/datum/file"
import { rewriteMinioUrl } from "@/utils/validate"

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

/** 格式化日期，只显示月日时（MM-DD HH:MM） */
function formatDateTime(dateStr) {
  if (!dateStr) return '-'
  return dateStr.substring(5, 16)
}

// 审核结果字典
const resultOptions = ref([
  { label: '未审核', value: '0' },
  { label: '属实', value: '1' },
  { label: '不属实', value: '2' }
])

// 回执表单
const auditForm = reactive({
  reportId: null,
  result: '1',
  receipt: '',
  defaultPlaceholder: ''
})

const auditRules = {
  receipt: [{ required: true, message: '回执内容不能为空', trigger: 'blur' }]
}

// 预览相关状态
const previewOpen = ref(false)
const previewRow = ref(null)
const previewPage = ref(1)
const previewType = ref("")
const previewFormatLabel = ref("")
const previewLoading = ref(false)
const previewReceipt = ref("")

const previewUrl = computed(() => {
  if (!previewRow.value?.fileUrl) {
    return ""
  }
  const base = rewriteMinioUrl(previewRow.value.fileUrl)
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

/** 通过按钮（result=1 属实） */
function handlePass(row) {
  auditForm.reportId = row.reportId
  auditForm.result = '1'
  auditForm.receipt = '资料已下架'
  auditForm.defaultPlaceholder = '通过默认回执：资料已下架'
  auditDialogVisible.value = true
}

/** 拒绝按钮（result=2 不属实） */
function handleReject(row) {
  auditForm.reportId = row.reportId
  auditForm.result = '2'
  auditForm.receipt = '举报未通过，请联系管理员'
  auditForm.defaultPlaceholder = '拒绝默认回执：举报未通过，请联系管理员'
  auditDialogVisible.value = true
}

/** 提交审核回执 */
function submitAudit() {
  auditRef.value.validate(valid => {
    if (valid) {
      auditReport({
        reportId: auditForm.reportId,
        result: auditForm.result,
        remark: auditForm.receipt || ''
      }).then(() => {
        proxy.$modal.msgSuccess("审核完成")
        auditDialogVisible.value = false
        getList()
      }).catch(() => {})
    }
  })
}

// ========== 文件预览功能 ==========

const IMAGE_FORMATS = ['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp', 'svg', 'ico']

/** 判断文件是否可预览 */
function isPreviewable(row) {
  const format = (row.fileFormat || row.fileUrl || "").toString().toLowerCase()
  if (format.includes("pdf")) return true
  return IMAGE_FORMATS.some(f => format.includes(f))
}

/** 获取预览类型 */
function getPreviewType(row) {
  const format = (row.fileFormat || row.fileUrl || "").toString().toLowerCase()
  if (format.includes("pdf")) {
    return "pdf"
  }
  if (IMAGE_FORMATS.some(f => format.includes(f))) {
    return "image"
  }
  return ""
}

/** 获取预览格式标签 */
function getPreviewFormatLabel(row) {
  const format = (row.fileFormat || row.fileUrl || "").toString().toLowerCase()
  const found = IMAGE_FORMATS.find(f => format.includes(f))
  return found ? found.toUpperCase() : "IMG"
}

/** 打开预览弹窗 */
function openPreview(row) {
  previewRow.value = row
  previewType.value = getPreviewType(row)
  previewFormatLabel.value = getPreviewFormatLabel(row)
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
        proxy.$modal.msgWarning("当前仅支持图片（JPG/PNG/GIF/WebP/BMP/SVG/ICO）和 PDF 文件预览")
        return
      }
      openPreview(row)
    }).catch(() => {
      proxy.$modal.msgError("获取文件信息失败")
    })
  } else {
    if (!isPreviewable(row)) {
      proxy.$modal.msgWarning("当前仅支持图片（JPG/PNG/GIF/WebP/BMP/SVG/ICO）和 PDF 文件预览")
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
  previewFormatLabel.value = ""
  previewReceipt.value = ""
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

/** 预览弹窗中审核 */
function handlePreviewAudit(pass) {
  if (!previewRow.value) {
    return
  }
  const reportResult = pass ? '1' : '2'
  const defaultReceipt = pass ? '资料已下架' : '举报未通过，请联系管理员'
  const remark = previewReceipt.value || defaultReceipt

  previewLoading.value = true
  auditReport({
    reportId: previewRow.value.reportId,
    result: reportResult,
    remark
  }).then(() => {
    proxy.$modal.msgSuccess(pass ? "举报已通过，资料已下架" : "举报已拒绝")
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
.preview-receipt-input {
  margin-bottom: 12px;
  padding: 12px;
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}
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
