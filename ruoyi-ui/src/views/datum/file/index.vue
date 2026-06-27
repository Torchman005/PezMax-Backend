<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="文件ID" prop="fileId">
        <el-input
            v-model="queryParams.fileId"
            placeholder="请输入文件ID"
            clearable
            @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="文件名称" prop="fileName">
        <el-input
            v-model="queryParams.fileName"
            placeholder="请输入文件名称"
            clearable
            @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item v-if="reviewMode" label="上传者ID" prop="userId">
        <el-input
            v-model="queryParams.userId"
            placeholder="请输入上传者ID"
            clearable
            @keyup.enter="handleQuery"
        />
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
            v-hasPermi="['datum:file:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="success"
            plain
            icon="Edit"
            :disabled="single"
            @click="handleUpdate"
            v-hasPermi="['datum:file:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="danger"
            plain
            icon="Delete"
            :disabled="multiple"
            @click="handleDelete"
            v-hasPermi="['datum:file:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="warning"
            plain
            icon="Download"
            @click="handleExport"
            v-hasPermi="['datum:file:export']"
        >导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          :type="reviewMode ? 'success' : 'info'"
          plain
          icon="Check"
          @click="toggleReviewMode"
        >{{ reviewMode ? '退出审核' : '审核' }}</el-button>
      </el-col>
      <el-col v-if="reviewMode" :span="1.5">
        <el-button
          type="success"
          plain
          icon="Finished"
          :disabled="!queryParams.userId"
          @click="handleApproveUserPending"
        >通过该用户全部未审核</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="fileList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="文件ID" align="center" prop="fileId" width="90" />
      <el-table-column label="上传用户ID" align="center" prop="userId" width="110" />
      <el-table-column label="文件名称" align="center" prop="fileName" min-width="140" show-overflow-tooltip />
      <el-table-column label="文件URL" align="center" prop="fileUrl" min-width="180" show-overflow-tooltip />
      <el-table-column label="大小" align="center" width="100">
        <template #default="scope">
          {{ formatFileSize(scope.row.fileSize) }}
        </template>
      </el-table-column>
      <el-table-column label="格式" align="center" prop="fileFormat" width="80" />
      <el-table-column label="年份" align="center" prop="fileYear" width="80" />
      <el-table-column label="文件类型" align="center" width="100">
        <template #default="scope">
          <el-tag :type="fileTypeTagType(scope.row.fileType)">{{ fileTypeLabel(scope.row.fileType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="科目" align="center" prop="fileSubject" width="100" />
      <el-table-column label="审核人" align="center" prop="reviewer" width="100" />
      <el-table-column label="状态" align="center" width="90">
        <template #default="scope">
          <el-tag :type="fileStatusTagType(scope.row.fileStatus)">{{ fileStatusLabel(scope.row.fileStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" show-overflow-tooltip />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="250">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['datum:file:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['datum:file:remove']">删除</el-button>
          <el-button link type="primary" icon="View" @click="handlePreview(scope.row, scope.$index)">预览</el-button>
          <el-button link type="primary" icon="Download" @click="handleDownload(scope.row)">下载</el-button>
          <el-button v-if="reviewMode" link type="success" icon="Check" @click="handlePass(scope.row)">通过</el-button>
          <el-button v-if="reviewMode" link type="danger" icon="Close" @click="handleReject(scope.row)">拒绝</el-button>
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

    <el-dialog :title="title" v-model="open" width="560px" append-to-body>
      <el-form ref="fileRef" :model="form" :rules="rules" label-width="100px">
        <template v-if="!form.fileId">
          <el-form-item label="选择文件">
            <el-upload
                ref="uploadRef"
                :limit="1"
                :auto-upload="true"
                :http-request="handleMinioUpload"
                :on-exceed="handleUploadExceed"
                :disabled="uploading"
            >
              <el-button type="primary" :loading="uploading">选择文件并上传</el-button>
            </el-upload>
            <div class="el-upload__tip">选择后自动上传至 MinIO 存储桶根目录（{{ minioBucketHint }}）</div>
          </el-form-item>
          <el-form-item label="文件URL" prop="fileUrl">
            <el-input v-model="form.fileUrl" placeholder="上传成功后自动填写" readonly />
          </el-form-item>
        </template>
        <template v-else>
          <el-form-item label="文件URL">
            <el-input v-model="form.fileUrl" readonly type="textarea" :rows="2" />
          </el-form-item>
        </template>
        <el-form-item label="文件名称" prop="fileName">
          <el-input v-model="form.fileName" placeholder="上传后自动填写，可修改" />
        </el-form-item>
        <el-form-item label="上传用户ID" prop="userId">
          <el-input v-model="form.userId" placeholder="默认为1，可自定义" />
        </el-form-item>
        <el-form-item label="文件大小">
          <span>{{ formatFileSize(form.fileSize) }}</span>
        </el-form-item>
        <el-form-item label="文件格式" prop="fileFormat">
          <el-input v-model="form.fileFormat" placeholder="上传后自动填写" :readonly="!form.fileId" />
        </el-form-item>
        <el-form-item label="文件年份" prop="fileYear">
          <el-input v-model="form.fileYear" placeholder="如：2024" />
        </el-form-item>
        <el-form-item label="文件类型" prop="fileType">
          <el-input v-model="form.fileType" placeholder="1-期末 2-期中 3-资料 4-补考 5-其他学校" />
        </el-form-item>
        <el-form-item label="科目" prop="fileSubject">
          <el-input v-model="form.fileSubject" placeholder="请输入科目" />
        </el-form-item>
        <el-form-item label="审核人" prop="reviewer">
          <el-input
              v-model="form.reviewer"
              :disabled="!form.fileId"
              :placeholder="!form.fileId ? '当前登录用户ID（自动）' : '请输入审核人'"
          />
        </el-form-item>
        <el-form-item v-if="form.fileId" label="删除标记" prop="delFlag">
          <el-input v-model="form.delFlag" placeholder="0-未删除，1-已删除" />
        </el-form-item>
        <el-form-item label="文件状态" prop="fileStatus">
          <el-input
              v-model="form.fileStatus"
              :disabled="!form.fileId"
              :placeholder="!form.fileId ? '1-通过（自动）' : '0-未审核 1-通过 2-未通过 3-被举报'"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm" :loading="uploading">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="文件预览" v-model="previewOpen" width="80%" top="4vh" append-to-body>
      <div class="preview-toolbar">
        <div class="preview-info">
          <span>预览：{{ previewRow?.fileName || previewRow?.fileUrl }}</span>
          <el-tag v-if="previewType==='pdf'" type="info" style="margin-left:8px;">PDF</el-tag>
          <el-tag v-else-if="previewType==='image'" type="success" style="margin-left:8px;">{{ previewFormatLabel }}</el-tag>
        </div>
        <div class="preview-actions">
          <el-button size="small" type="success" @click="auditCurrent(true)" :loading="previewLoading">通过</el-button>
          <el-button size="small" type="danger" @click="auditCurrent(false)" :loading="previewLoading">拒绝</el-button>
        </div>
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

<script setup name="File">
import { listFile, getFile, delFile, addFile, updateFile, uploadDatumFile, approvePendingByUserId } from "@/api/datum/file"
import useUserStore from "@/store/modules/user"
import { rewriteMinioUrl } from "@/utils/validate"

const { proxy } = getCurrentInstance()
const userStore = useUserStore()

const fileList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const uploading = ref(false)
const uploadRef = ref(null)
const reviewMode = ref(false)
//预览相关
// fc//
const previewOpen = ref(false)
const previewRow = ref(null)
const previewPage = ref(1)
const previewType = ref("")
const previewFormatLabel = ref("")
const previewLoading = ref(false)
const currentPreviewIndex = ref(-1)

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

//与后端 application.yml 中 minio.bucketName 一致，便于提示（仅展示） /
//fc/
const minioBucketHint = "ptmj"

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    fileId: null,
    fileName: null,
    userId: null,
    fileStatus: null,
  },
  rules: {
    fileName: [
      { required: true, message: "文件名称不能为空", trigger: "blur" }
    ],
    fileUrl: [
      {
        validator: (rule, value, callback) => {
          if (!data.form.fileId && !value) {
            callback(new Error("请先选择文件并完成上传"))
          } else {
            callback()
          }
        },
        trigger: "change"
      }
    ],
    fileSubject: [
      { required: true, message: "科目不能为空", trigger: "blur" }
    ],
    userId: [
      { required: true, message: "用户ID不能为空", trigger: "blur" },
      { pattern: /^\d+$/, message: "用户ID必须为数字", trigger: "blur" }
    ],
  }
})
//fc
// 文件大小计算/
const { queryParams, form, rules } = toRefs(data)

function formatFileSize(bytes) {
  if (bytes == null || bytes === "") {
    return "—"
  }
  const n = Number(bytes)
  if (Number.isNaN(n) || n < 0) {
    return "—"
  }
  if (n < 1024) {
    return n + " B"
  }
  if (n < 1024 * 1024) {
    return (n / 1024).toFixed(1) + " KB"
  }
  if (n < 1024 * 1024 * 1024) {
    return (n / 1024 / 1024).toFixed(1) + " MB"
  }
  return (n / 1024 / 1024 / 1024).toFixed(2) + " GB"
}

const fileStatusMap = { 0: "未审核", 1: "通过", 2: "未通过", 3: "被举报" }
const fileStatusTagMap = { 0: "info", 1: "success", 2: "danger", 3: "warning" }
const fileTypeMap = { 1: "期末", 2: "期中", 3: "资料", 4: "补考", 5: "其他学校" }
const fileTypeTagMap = { 1: "", 2: "success", 3: "info", 4: "warning", 5: "" }

function fileStatusLabel(status) {
  return fileStatusMap[status] ?? status
}
function fileStatusTagType(status) {
  return fileStatusTagMap[status] ?? "info"
}
function fileTypeLabel(type) {
  return fileTypeMap[type] ?? type
}
function fileTypeTagType(type) {
  return fileTypeTagMap[type] ?? ""
}
//fc
// 文件后缀识别/
function getList() {
  loading.value = true
  const q = { ...queryParams.value }
  normalizeIdQuery(q, "fileId")
  normalizeIdQuery(q, "userId")
  return listFile(q).then(response => {
    fileList.value = response.rows
    total.value = response.total
    return response
  }).catch(() => {
    fileList.value = []
    total.value = 0
    return Promise.resolve({ rows: [], total: 0 })
  }).finally(() => {
    loading.value = false
  })
}

function normalizeIdQuery(query, field) {
  if (typeof query[field] === "string") {
    query[field] = query[field].trim()
  }
  if (query[field] === "" || query[field] === undefined) {
    query[field] = null
  } else if (query[field] != null) {
    const idNum = Number(query[field])
    query[field] = Number.isInteger(idNum) && idNum > 0 ? idNum : null
  }
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    fileId: null,
    userId: null,
    fileName: null,
    fileUrl: null,
    fileSize: null,
    fileFormat: null,
    fileYear: null,
    fileType: null,
    fileSubject: null,
    reviewer: null,
    fileStatus: null,
    delFlag: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null
  }
  uploading.value = false
  nextTick(() => {
    uploadRef.value?.clearFiles()
    proxy.resetForm("fileRef")
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.fileId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

//新增上传：审核人为当前登录昵称、状态为 1-通过（与后端强制一致，界面只读） /

function applyUploadFormDefaults() {
  form.value.reviewer = userStore.id || ""
  form.value.fileStatus = 1
  form.value.userId = 1
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "添加试卷文件"
  nextTick(() => {
    nextTick(() => {
      applyUploadFormDefaults()
    })
  })
}

function handleUpdate(row) {
  reset()
  const _fileId = row.fileId || ids.value
  getFile(_fileId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改试卷文件"
  })
}

function handleUploadExceed() {
  proxy.$modal.msgWarning("请先移除已选文件再重新选择")
}

function handleMinioUpload(options) {
  uploading.value = true
  const fd = new FormData()
  fd.append("file", options.file)
  uploadDatumFile(fd).then(res => {
    const d = res.data || {}
    form.value.fileName = d.fileName
    form.value.fileUrl = d.fileUrl
    form.value.fileSize = d.fileSize
    form.value.fileFormat = d.fileFormat
    if (!form.value.fileId) {
      applyUploadFormDefaults()
    }
    options.onSuccess(res)
    proxy.$modal.msgSuccess("已上传至 MinIO")
    proxy.$refs["fileRef"]?.validateField("fileUrl")
  }).catch(() => {
    options.onError(new Error("上传失败"))
  }).finally(() => {
    uploading.value = false
  })
}

function submitForm() {
  proxy.$refs["fileRef"].validate(valid => {
    if (valid) {
      if (form.value.fileId != null) {
        updateFile(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addFile(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const _fileIds = row.fileId || ids.value
  proxy.$modal.confirm('是否确认删除试卷文件编号为"' + _fileIds + '"的数据项？').then(function() {
    return delFile(_fileIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download('datum/file/export', {
    ...queryParams.value
  }, `file_${new Date().getTime()}.xlsx`)
}

function handleDownload(row) {
  // 下载路径默认为 D:/download，用户可在浏览器下载对话框中选择保存位置
  if (row.fileUrl) {
    window.open(row.fileUrl, '_blank')
  } else {
    proxy.$modal.msgError("文件URL不存在")
  }
}

function handleReviewModeChange() {
  if (reviewMode.value) {
    queryParams.value.fileStatus = 0
  } else {
    queryParams.value.fileStatus = null
    queryParams.value.userId = null
  }
  getList()
}

function toggleReviewMode() {
  reviewMode.value = !reviewMode.value
  handleReviewModeChange()
}

function handleApproveUserPending() {
  const userId = Number(String(queryParams.value.userId || "").trim())
  if (!Number.isInteger(userId) || userId <= 0) {
    proxy.$modal.msgWarning("请输入有效的上传者ID")
    return
  }
  proxy.$modal.confirm(`是否确认通过上传者ID为 "${userId}" 的全部未审核文件？`).then(() => {
    return approvePendingByUserId(userId)
  }).then(response => {
    const count = response.count ?? response.data ?? 0
    proxy.$modal.msgSuccess(`已通过 ${count} 个文件`)
    queryParams.value.fileStatus = 0
    queryParams.value.pageNum = 1
    getList()
  }).catch(() => {})
}

const IMAGE_FORMATS = ['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp', 'svg', 'ico']

function isPreviewable(row) {
  const format = (row.fileFormat || row.fileUrl || "").toString().toLowerCase()
  if (format.includes("pdf")) return true
  return IMAGE_FORMATS.some(f => format.includes(f))
}

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

function getPreviewFormatLabel(row) {
  const format = (row.fileFormat || row.fileUrl || "").toString().toLowerCase()
  const found = IMAGE_FORMATS.find(f => format.includes(f))
  return found ? found.toUpperCase() : "IMG"
}

function openPreview(row, index) {
  previewRow.value = row
  previewType.value = getPreviewType(row)
  previewFormatLabel.value = getPreviewFormatLabel(row)
  previewPage.value = 1
  previewOpen.value = true
  currentPreviewIndex.value = index
}

function handlePreview(row, index) {
  if (!isPreviewable(row)) {
    proxy.$modal.msgWarning("当前仅支持图片（JPG/PNG/GIF/WebP/BMP/SVG/ICO）和 PDF 文件预览")
    return
  }
  const idx = typeof index === "number" ? index : fileList.value.findIndex(item => item.fileId === row.fileId)
  openPreview(row, idx)
}

function closePreview() {
  previewOpen.value = false
  previewRow.value = null
  previewPage.value = 1
  previewType.value = ""
  previewFormatLabel.value = ""
  currentPreviewIndex.value = -1
}

function prevPreviewPage() {
  if (previewPage.value > 1) {
    previewPage.value -= 1
  }
}

function nextPreviewPage() {
  if (previewType.value === "pdf") {
    previewPage.value += 1
  }
}

function getNextPreviewableIndex(startIndex) {
  for (let i = startIndex; i < fileList.value.length; i++) {
    if (isPreviewable(fileList.value[i])) {
      return i
    }
  }
  return -1
}

function handleReview() {
  const startIndex = getNextPreviewableIndex(0)
  if (startIndex < 0) {
    proxy.$modal.msgInfo("当前列表中没有可预览的 JPG/PDF 文件")
    return
  }
  handlePreview(fileList.value[startIndex], startIndex)
}

function auditCurrent(pass) {
  if (!previewRow.value) {
    return
  }
  previewLoading.value = true
  updateFile({ ...previewRow.value, reviewer: userStore.id, fileStatus: pass ? 1 : 2 }).then(() => {
    proxy.$modal.msgSuccess(pass ? "审核通过" : "审核拒绝")
    return getList()
  }).then(() => {
    const nextIndex = getNextPreviewableIndex(currentPreviewIndex.value + 1)
    if (nextIndex >= 0) {
      openPreview(fileList.value[nextIndex], nextIndex)
    } else {
      closePreview()
    }
  }).finally(() => {
    previewLoading.value = false
  })
}

//// 已完成简易审核 /
//fc/
function handlePass(row) {
  updateFile({ ...row, reviewer: userStore.id, fileStatus: 1 }).then(() => {
    proxy.$modal.msgSuccess("审核通过")
    getList()
  })
}

function handleReject(row) {
  updateFile({ ...row, reviewer: userStore.id, fileStatus: 2 }).then(() => {
    proxy.$modal.msgSuccess("审核拒绝")
    getList()
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
