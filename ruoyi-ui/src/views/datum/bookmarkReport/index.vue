<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="被举报书签ID" prop="bookmarkId">
        <el-input
            v-model="queryParams.bookmarkId"
            placeholder="请输入被举报书签ID"
            clearable
            @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="举报用户ID" prop="userId">
        <el-input
            v-model="queryParams.userId"
            placeholder="请输入举报用户ID"
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
            v-hasPermi="['datum:bookmarkReport:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="success"
            plain
            icon="Edit"
            :disabled="single"
            @click="handleUpdate"
            v-hasPermi="['datum:bookmarkReport:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="danger"
            plain
            icon="Delete"
            :disabled="multiple"
            @click="handleDelete"
            v-hasPermi="['datum:bookmarkReport:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="warning"
            plain
            icon="Download"
            @click="handleExport"
            v-hasPermi="['datum:bookmarkReport:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="reportList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="举报ID" align="center" prop="reportId" />
      <el-table-column label="被举报书签" align="center" prop="bookmarkId">
        <template #default="{ row }">
          <span>{{ row.bookmarkId }}</span>
          <span v-if="row.bookmarkTitle"> - {{ row.bookmarkTitle }}</span>
        </template>
      </el-table-column>
      <el-table-column label="举报用户ID" align="center" prop="userId" />
      <el-table-column label="举报原因" align="center" prop="reason" show-overflow-tooltip />
      <el-table-column label="审核结果" align="center" prop="result">
        <template #default="scope">
          <dict-tag :options="resultOptions" :value="scope.row.result" />
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" show-overflow-tooltip />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
              v-if="scope.row.result === '0'"
              link
              type="warning"
              icon="Check"
              @click="handleAudit(scope.row)"
              v-hasPermi="['datum:bookmarkReport:audit']"
          >审核</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['datum:bookmarkReport:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['datum:bookmarkReport:remove']">删除</el-button>
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

    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="reportRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="被举报书签ID" prop="bookmarkId">
              <el-input v-model="form.bookmarkId" placeholder="请输入被举报书签ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="举报用户ID" prop="userId">
              <el-input v-model="form.userId" placeholder="请输入举报用户ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="举报原因" prop="reason">
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

    <el-dialog title="书签举报审核" v-model="auditDialogVisible" width="500px" append-to-body>
      <el-form ref="auditRef" :model="auditForm" :rules="auditRules" label-width="100px">
        <el-form-item label="审核结果" prop="result">
          <el-radio-group v-model="auditForm.result">
            <el-radio label="1">属实（状态改为未通过）</el-radio>
            <el-radio label="2">不属实（状态恢复为通过）</el-radio>
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
  </div>
</template>

<script setup name="BookmarkReport">
import { ref, reactive, toRefs, getCurrentInstance } from 'vue'
import { listBookmarkReport, getBookmarkReport, delBookmarkReport, addBookmarkReport, updateBookmarkReport, auditBookmarkReport } from "@/api/datum/bookmarkReport"
import { getBookmark } from "@/api/bookmark/bookmark"

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
const bookmarkTitleCache = reactive({})

const resultOptions = ref([
  { label: '未审核', value: '0' },
  { label: '属实', value: '1' },
  { label: '不属实', value: '2' }
])

const auditForm = reactive({
  reportId: null,
  result: '1',
  remark: ''
})

const auditRules = {
  result: [{ required: true, message: '请选择审核结果', trigger: 'change' }]
}

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    bookmarkId: null,
    userId: null,
    reason: null,
    result: null,
  },
  rules: {
    bookmarkId: [
      { required: true, message: "被举报书签ID不能为空", trigger: "blur" }
    ],
    userId: [
      { required: true, message: "举报用户ID不能为空", trigger: "blur" }
    ],
    reason: [
      { required: true, message: "举报原因不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

async function getList() {
  loading.value = true
  try {
    const response = await listBookmarkReport(queryParams.value)
    reportList.value = response.rows || []
    total.value = response.total
    await loadBookmarkTitles(reportList.value)
  } finally {
    loading.value = false
  }
}

async function loadBookmarkTitles(rows) {
  const idsToLoad = [...new Set(rows.map(row => row.bookmarkId).filter(Boolean))].filter(id => !bookmarkTitleCache[id])
  if (idsToLoad.length) {
    await Promise.all(idsToLoad.map(id =>
      getBookmark(id)
        .then(res => {
          bookmarkTitleCache[id] = res?.data?.title || ''
        })
        .catch(() => {
          bookmarkTitleCache[id] = ''
        })
    ))
  }
  rows.forEach(row => {
    row.bookmarkTitle = bookmarkTitleCache[row.bookmarkId] || ''
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    reportId: null,
    bookmarkId: null,
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

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.reportId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "添加书签举报"
}

function handleUpdate(row) {
  reset()
  const _reportId = row.reportId || ids.value
  getBookmarkReport(_reportId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改书签举报"
  })
}

function submitForm() {
  proxy.$refs["reportRef"].validate(valid => {
    if (valid) {
      if (form.value.reportId != null) {
        updateBookmarkReport(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addBookmarkReport(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const _reportIds = row.reportId || ids.value
  proxy.$modal.confirm('是否确认删除书签举报编号为"' + _reportIds + '"的数据项？').then(function() {
    return delBookmarkReport(_reportIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download('datum/bookmarkReport/export', {
    ...queryParams.value
  }, `bookmarkReport_${new Date().getTime()}.xlsx`)
}

function handleAudit(row) {
  auditForm.reportId = row.reportId
  auditForm.result = '1'
  auditForm.remark = ''
  auditDialogVisible.value = true
}

function submitAudit() {
  auditRef.value.validate(valid => {
    if (valid) {
      auditBookmarkReport({
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

getList()
</script>
