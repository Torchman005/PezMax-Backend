<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="通知标题" prop="title">
        <el-input
          v-model="queryParams.title"
          placeholder="请输入通知标题"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="展示形态" prop="displayMode">
        <el-select v-model="queryParams.displayMode" placeholder="全部" clearable style="width: 160px">
          <el-option label="弹窗" value="0" />
          <el-option label="滚动字幕" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="故障/维护时间">
        <el-date-picker
          v-model="faultMaintRange"
          type="datetimerange"
          value-format="YYYY-MM-DD HH:mm:ss"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          clearable
          style="width: 340px"
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
          v-hasPermi="['system:notification:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:notification:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:notification:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['system:notification:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="notificationList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="notifyId" width="70" />
      <el-table-column label="类型" align="center" prop="notifyType" width="100">
        <template #default="scope">
          {{ notifyTypeLabel(scope.row.notifyType) }}
        </template>
      </el-table-column>
      <el-table-column label="标题" align="center" prop="title" min-width="140" show-overflow-tooltip />
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag :type="statusTagType(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="排序" align="center" prop="sort" width="70" />
      <el-table-column label="展示形态" align="center" prop="displayMode" width="100">
        <template #default="scope">
          {{ displayModeLabel(scope.row.displayMode) }}
        </template>
      </el-table-column>
      <el-table-column label="故障起止" align="center" min-width="170">
        <template #default="scope">
          <span v-if="scope.row.notifyType === '2'">
            {{ parseTime(scope.row.faultStartTime, '{y}-{m}-{d} {h}:{i}') }}
            ~
            {{ scope.row.faultEndTime ? parseTime(scope.row.faultEndTime, '{y}-{m}-{d} {h}:{i}') : '未结束' }}
          </span>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column label="维护起止" align="center" min-width="170">
        <template #default="scope">
          <span v-if="scope.row.notifyType === '3'">
            {{ parseTime(scope.row.maintenanceStartTime, '{y}-{m}-{d} {h}:{i}') }}
            ~
            {{ scope.row.maintenanceEndTime ? parseTime(scope.row.maintenanceEndTime, '{y}-{m}-{d} {h}:{i}') : '未结束' }}
          </span>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column label="发布时间" align="center" min-width="170">
        <template #default="scope">
          <span v-if="scope.row.notifyType === '5'">
            {{ scope.row.publishStart ? parseTime(scope.row.publishStart, '{y}-{m}-{d} {h}:{i}') : '立即' }}
            ~
            {{ scope.row.publishEnd ? parseTime(scope.row.publishEnd, '{y}-{m}-{d} {h}:{i}') : '未结束' }}
          </span>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" show-overflow-tooltip />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="160">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:notification:edit']">修改</el-button>
          <el-button link type="warning" icon="Close" @click="handleEndNow(scope.row)" v-hasPermi="['system:notification:edit']">立即结束</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:notification:remove']">删除</el-button>
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

    <el-dialog :title="title" v-model="open" width="640px" append-to-body>
      <el-form ref="notificationRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="通知类型" prop="notifyType">
          <el-select v-model="form.notifyType" placeholder="请选择" style="width: 100%">
            <el-option label="版本更新" value="1" />
            <el-option label="系统故障" value="2" />
            <el-option label="系统维护" value="3" />
            <el-option label="资料下架" value="4" />
            <el-option label="日常滚动" value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="通知标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入通知标题" />
        </el-form-item>
        <el-form-item label="通知正文">
          <editor v-model="form.content" :min-height="192"/>
        </el-form-item>
        <el-form-item label="配置状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="'0'">启用</el-radio>
            <el-radio :value="'1'">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序/优先级" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :step="1" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="展示形态" prop="displayMode">
          <el-radio-group v-model="form.displayMode">
            <el-radio :value="'0'">弹窗</el-radio>
            <el-radio :value="'1'">滚动字幕</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.notifyType === '2'" label="故障开始时间" prop="faultStartTime">
          <el-date-picker v-model="form.faultStartTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="故障开始" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="form.notifyType === '2'" label="故障结束时间" prop="faultEndTime">
          <el-date-picker v-model="form.faultEndTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="留空表示未结束" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="form.notifyType === '3'" label="维护开始时间" prop="maintenanceStartTime">
          <el-date-picker v-model="form.maintenanceStartTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="维护开始" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="form.notifyType === '3'" label="维护结束时间" prop="maintenanceEndTime">
          <el-date-picker v-model="form.maintenanceEndTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="留空表示未结束" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="form.notifyType === '3'" label="提前提醒(分钟)" prop="remindBeforeMinutes">
          <el-input-number v-model="form.remindBeforeMinutes" :min="0" :step="1" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="form.notifyType === '4'" label="下架用户ID" prop="uploadUserId">
          <el-input v-model="form.uploadUserId" placeholder="上传用户 id" />
        </el-form-item>
        <el-form-item v-if="form.notifyType === '4'" label="资料ID" prop="materialId">
          <el-input v-model="form.materialId" placeholder="资料 id" />
        </el-form-item>
        <el-form-item v-if="form.notifyType === '4'" label="资料标题快照" prop="materialTitleSnapshot">
          <el-input v-model="form.materialTitleSnapshot" type="textarea" placeholder="下架时资料标题" />
        </el-form-item>
        <el-form-item v-if="form.notifyType === '5'" label="展示开始" prop="publishStart">
          <el-date-picker v-model="form.publishStart" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="空则立即开始" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="form.notifyType === '5'" label="展示结束" prop="publishEnd">
          <el-date-picker v-model="form.publishEnd" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="空则未结束" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="form.notifyType === '5'" label="滚动间隔(秒)" prop="scrollTimeInterval">
          <el-input-number v-model="form.scrollTimeInterval" :min="1" :step="1" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Notification">
import { listNotification, getNotification, delNotification, addNotification, updateNotification } from "@/api/datum/notification"

const { proxy } = getCurrentInstance()

const notificationList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
/** 搜索：故障/维护时间区间（写入 queryParams.params） */
const faultMaintRange = ref(null)

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    title: null,
    displayMode: null,
    params: {
      faultMaintBegin: null,
      faultMaintEnd: null
    }
  },
  rules: {
    notifyType: [
      { required: true, message: "通知类型不能为空", trigger: "change" }
    ],
    title: [
      { required: true, message: "通知标题不能为空", trigger: "blur" }
    ],
    status: [
      { required: true, message: "配置状态不能为空", trigger: "change" }
    ],
    sort: [
      { required: true, message: "排序/优先级不能为空", trigger: "change" }
    ],
    displayMode: [
      { required: true, message: "展示形态不能为空", trigger: "change" }
    ],
    faultStartTime: [
      { validator: validateStartTime, trigger: ["blur", "change"] }
    ],
    maintenanceStartTime: [
      { validator: validateStartTime, trigger: ["blur", "change"] }
    ],
    publishStart: [
      { validator: validateStartTime, trigger: ["blur", "change"] }
    ],
    faultEndTime: [
      { validator: validateEndTime, trigger: ["blur", "change"] }
    ],
    maintenanceEndTime: [
      { validator: validateEndTime, trigger: ["blur", "change"] }
    ],
    publishEnd: [
      { validator: validateEndTime, trigger: ["blur", "change"] }
    ]
  }
})

const { queryParams, form, rules } = toRefs(data)

const dateTimePattern = /^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/

function formatDateTime(date) {
  const pad = value => String(value).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

function parseDateTimeString(value) {
  if (!value || typeof value !== 'string') return null

  const match = value.match(
      /^(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2}):(\d{2})$/
  )
  if (!match) return null

  const [, y, m, d, h, i, s] = match
  const result = new Date(
      Number(y),
      Number(m) - 1,
      Number(d),
      Number(h),
      Number(i),
      Number(s)
  )

  return Number.isNaN(result.getTime()) ? null : result
}

function isValidDateTimeString(value) {
  return dateTimePattern.test(value) && parseDateTimeString(value) !== null
}

function validateStartTime(rule, value, callback) {
  if (value == null || value === '') {
    return callback()
  }
  if (!dateTimePattern.test(value)) {
    return callback(new Error('时间格式应为 YYYY-MM-DD HH:mm:ss'))
  }
  // 故障开始时间不受"不能早于当前时间"限制
  if (form.value.notifyType !== '2') {
    const selected = parseDateTimeString(value)
    if (!selected) {
      return callback(new Error('时间格式不正确'))
    }
    const now = new Date()
    if (selected.getTime() < now.getTime() - 5000) {
      return callback(new Error('开始时间不能早于当前时间'))
    }
  }
  callback()
}

function validateEndTime(rule, value, callback) {
  if (value == null || value === '') {
    return callback()
  }
  if (!dateTimePattern.test(value)) {
    return callback(new Error('时间格式应为 YYYY-MM-DD HH:mm:ss'))
  }
  const selected = parseDateTimeString(value)
  if (!selected) {
    return callback(new Error('时间格式不正确'))
  }
  if (form.value.notifyType === '2') {
    // 故障结束时间需要比故障开始时间晚
    const startTime = parseDateTimeString(form.value.faultStartTime)
    if (startTime && selected <= startTime) {
      return callback(new Error('故障结束时间需要晚于故障开始时间'))
    }
  } else if (form.value.notifyType === '3') {
    // 维护结束时间需要比维护开始时间晚
    const startTime = parseDateTimeString(form.value.maintenanceStartTime)
    if (startTime && selected <= startTime) {
      return callback(new Error('维护结束时间需要晚于维护开始时间'))
    }
  } else {
    const now = new Date()
    if (selected < now) {
      return callback(new Error('结束时间不能早于当前时间'))
    }
  }
  callback()
}

function normalizeDateTimes() {
  const now = new Date()
  const nowStr = formatDateTime(now)
  const nextDayStr = formatDateTime(new Date(now.getTime() + 24 * 60 * 60 * 1000))

  if (form.value.notifyType === '2') {
    // 故障开始时间不受限制，仅保留原始值
    // 若故障结束时间无效或早于/等于开始时间，则自动设为开始时间+24h
    const faultStart = parseDateTimeString(form.value.faultStartTime)
    if (faultStart) {
      const faultEnd = parseDateTimeString(form.value.faultEndTime)
      if (!faultEnd || faultEnd <= faultStart) {
        form.value.faultEndTime = formatDateTime(new Date(faultStart.getTime() + 24 * 60 * 60 * 1000))
      }
    }
  }

  if (form.value.notifyType === '3') {
    if (!isValidDateTimeString(form.value.maintenanceStartTime) || parseDateTimeString(form.value.maintenanceStartTime) < now) {
      form.value.maintenanceStartTime = nowStr
    }
    const maintStart = parseDateTimeString(form.value.maintenanceStartTime)
    if (maintStart) {
      const maintEnd = parseDateTimeString(form.value.maintenanceEndTime)
      if (!maintEnd || maintEnd <= maintStart) {
        form.value.maintenanceEndTime = formatDateTime(new Date(maintStart.getTime() + 24 * 60 * 60 * 1000))
      }
    }
  }

  if (form.value.notifyType === '5') {
    if (!isValidDateTimeString(form.value.publishStart) || parseDateTimeString(form.value.publishStart) < now) {
      form.value.publishStart = nowStr
    }
    if (!isValidDateTimeString(form.value.publishEnd) || parseDateTimeString(form.value.publishEnd) < now) {
      form.value.publishEnd = nextDayStr
    }
  }
}

function notifyTypeLabel(v) {
  const m = { 1: "版本更新", 2: "系统故障", 3: "系统维护", 4: "资料下架", 5: "日常滚动" }
  return m[v] ?? (v ?? "—")
}

function displayModeLabel(v) {
  if (v === "0" || v === 0) return "弹窗"
  if (v === "1" || v === 1) return "滚动字幕"
  return v ?? "—"
}

function statusLabel(v) {
  if (v === "0" || v === 0) return "启用"
  if (v === "1" || v === 1) return "禁用"
  return v ?? "—"
}

function statusTagType(v) {
  if (v === "0" || v === 0) return "success"
  if (v === "1" || v === 1) return "danger"
  return "info"
}

function syncFaultMaintParams() {
  const p = queryParams.value.params || {}
  if (faultMaintRange.value && faultMaintRange.value.length === 2) {
    p.faultMaintBegin = faultMaintRange.value[0]
    p.faultMaintEnd = faultMaintRange.value[1]
  } else {
    p.faultMaintBegin = null
    p.faultMaintEnd = null
  }
  queryParams.value.params = p
}

function getList() {
  loading.value = true
  syncFaultMaintParams()
  listNotification(queryParams.value).then(response => {
    notificationList.value = response.rows
    total.value = response.total
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    notifyId: null,
    notifyType: null,
    title: null,
    content: null,
    status: "0",
    sort: 0,
    displayMode: "0",
    faultStartTime: null,
    faultEndTime: null,
    maintenanceStartTime: null,
    maintenanceEndTime: null,
    remindBeforeMinutes: null,
    uploadUserId: null,
    materialId: null,
    materialTitleSnapshot: null,
    publishStart: null,
    publishEnd: null,
    scrollTimeInterval: 30,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null
  }
  proxy.resetForm("notificationRef")
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  faultMaintRange.value = null
  queryParams.value.title = null
  queryParams.value.displayMode = null
  queryParams.value.params = { faultMaintBegin: null, faultMaintEnd: null }
  proxy.resetForm("queryRef")
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.notifyId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "添加通知"
}

function handleUpdate(row) {
  reset()
  const _notifyId = row.notifyId || ids.value
  getNotification(_notifyId).then(response => {
    form.value = response.data
    if (form.value.status == null) form.value.status = "0"
    if (form.value.displayMode == null) form.value.displayMode = "0"
    if (form.value.sort == null) form.value.sort = 0
    open.value = true
    title.value = "修改通知"
  })
}

function submitForm() {
  proxy.$refs["notificationRef"].validate(valid => {
    if (valid) {
      normalizeDateTimes()
      if (form.value.notifyId != null) {
        updateNotification(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addNotification(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleEndNow(row) {
  const nowStr = formatDateTime(new Date())
  const payload = {
    notifyId: row.notifyId
  }
  if (row.notifyType === '2') {
    payload.faultEndTime = nowStr
  } else if (row.notifyType === '3') {
    payload.maintenanceEndTime = nowStr
  } else {
    payload.publishEnd = nowStr
  }

  proxy.$modal.confirm('是否立即结束该通知？').then(() => {
    return updateNotification(payload)
  }).then(() => {
    proxy.$modal.msgSuccess('通知已结束')
    getList()
  }).catch(() => {})
}

function handleDelete(row) {
  const _notifyIds = row.notifyId || ids.value
  proxy.$modal.confirm('是否确认删除通知编号为"' + _notifyIds + '"的数据项？').then(function() {
    return delNotification(_notifyIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  syncFaultMaintParams()
  proxy.download('system/notification/export', {
    ...queryParams.value
  }, `notification_${new Date().getTime()}.xlsx`)
}

getList()
</script>
