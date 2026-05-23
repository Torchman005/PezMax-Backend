<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="所属用户ID" prop="userId">
        <el-input
          v-model="queryParams.userId"
          placeholder="请输入所属用户ID"
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
          v-hasPermi="['bookmark:bookmark:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['bookmark:bookmark:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['bookmark:bookmark:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['bookmark:bookmark:export']"
        >导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          :type="reviewMode ? 'success' : 'info'"
          plain
          icon="Check"
          @click="handleReview"
          v-hasPermi="['bookmark:bookmark:edit']"
        >{{ reviewMode ? '退出审核' : '审核' }}</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="bookmarkList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" align="center" prop="id" />
      <el-table-column label="所属用户ID" align="center" prop="userId" />
      <el-table-column label="目标链接完整URL" align="center" prop="url" min-width="180" show-overflow-tooltip>
        <template #default="scope">
          <el-link type="primary" :href="scope.row.url" target="_blank">{{ scope.row.url }}</el-link>
        </template>
      </el-table-column>
      <el-table-column label="网页标题" align="center" prop="title" show-overflow-tooltip />
      <el-table-column label="网页摘要/描述" align="center" prop="description" show-overflow-tooltip />
      <el-table-column label="网页封面图/Favicon URL" align="center" prop="coverImage" width="100">
        <template #default="scope">
          <image-preview :src="scope.row.coverImage" :width="50" :height="50"/>
        </template>
      </el-table-column>
      <el-table-column label="资源类型" align="center" prop="resourceType" width="130">
        <template #default="scope">
          <el-tag
            :type="getResourceTypeTag(scope.row.resourceType).type"
            :style="getResourceTypeTag(scope.row.resourceType).style"
          >{{ getResourceTypeTag(scope.row.resourceType).label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="所属专栏/系列名称" align="center" prop="collection" />
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template #default="scope">
          <el-tag v-if="scope.row.status == 0" type="success">正常</el-tag>
          <el-tag v-else type="info">未启用</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="用户自定义备注" align="center" prop="remark" show-overflow-tooltip />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['bookmark:bookmark:edit']" :disabled="reviewMode">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['bookmark:bookmark:remove']" :disabled="reviewMode">删除</el-button>
          <template v-if="reviewMode">
            <el-button link type="success" icon="Select" @click="handlePass(scope.row)">通过</el-button>
            <el-button link type="danger" icon="CloseBold" @click="handleReject(scope.row)">拒绝</el-button>
          </template>
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

    <!-- 添加或修改外部书签对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="bookmarkRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="所属用户ID" prop="userId">
              <el-input v-model="form.userId" placeholder="请输入所属用户ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="目标链接完整URL" prop="url">
              <el-input v-model="form.url" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="网页标题" prop="title">
              <el-input v-model="form.title" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="网页摘要/描述" prop="description">
              <el-input v-model="form.description" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="网页封面图/Favicon URL" prop="coverImage">
              <image-upload v-model="form.coverImage"/>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="关联科目或分类" prop="subject">
              <el-input v-model="form.subject" placeholder="请输入关联科目或分类" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="所属专栏/系列名称" prop="collection">
              <el-input v-model="form.collection" placeholder="请输入所属专栏/系列名称" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="删除标记：0-未删除，1-已删除" prop="delFlag">
              <el-input v-model="form.delFlag" placeholder="请输入删除标记：0-未删除，1-已删除" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="用户自定义备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
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
  </div>
</template>

<script setup name="Bookmark">
import { listBookmark, getBookmark, delBookmark, addBookmark, updateBookmark } from "@/api/bookmark/bookmark"

const { proxy } = getCurrentInstance()

const bookmarkList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    userId: null,
    url: null,
    title: null,
    description: null,
    status: null,
  },
  rules: {
    userId: [
      { required: true, message: "所属用户ID不能为空", trigger: "blur" }
    ],
    url: [
      { required: true, message: "目标链接完整URL不能为空", trigger: "blur" }
    ],
    title: [
      { required: true, message: "网页标题不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

const reviewMode = ref(false)

/** 审核模式切换 */
function handleReview() {
  reviewMode.value = !reviewMode.value
  if (reviewMode.value) {
    queryParams.value.status = 1
  } else {
    queryParams.value.status = null
  }
  handleQuery()
}

/** 审核通过 */
function handlePass(row) {
  updateBookmark({ ...row, status: 0 }).then(() => {
    proxy.$modal.msgSuccess("审核通过")
    getList()
  })
}

/** 审核拒绝 */
function handleReject(row) {
  updateBookmark({ ...row, status: 1 }).then(() => {
    proxy.$modal.msgSuccess("已拒绝")
    getList()
  })
}

/** 查询外部书签列表 */
function getList() {
  loading.value = true
  listBookmark(queryParams.value).then(response => {
    bookmarkList.value = response.rows
    total.value = response.total
    loading.value = false
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
    id: null,
    userId: null,
    url: null,
    title: null,
    description: null,
    coverImage: null,
    subject: null,
    resourceType: null,
    collection: null,
    status: null,
    delFlag: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null
  }
  proxy.resetForm("bookmarkRef")
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
  ids.value = selection.map(item => item.id)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加外部书签"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _id = row.id || ids.value
  getBookmark(_id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改外部书签"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["bookmarkRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateBookmark(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addBookmark(form.value).then(() => {
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
  const _ids = row.id || ids.value
  proxy.$modal.confirm('是否确认删除外部书签编号为"' + _ids + '"的数据项？').then(function() {
    return delBookmark(_ids)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('/datum/bookmark/export', {
    ...queryParams.value
  }, `bookmark_${new Date().getTime()}.xlsx`)
}

/** 资源类型映射：course/blog/paper/tool/entertainment/other */
const resourceTypeMap = {
  course:        { label: '网课/视频',  type: 'primary',   style: '' },
  blog:          { label: '博客/文章',  type: 'success',   style: '' },
  paper:         { label: '学术/论文',  type: 'warning',   style: '' },
  tool:          { label: '工具/开源',  type: 'info',      style: '' },
  entertainment: { label: '娱乐/音乐/资源', type: 'danger', style: '' },
  other:         { label: '其他',       type: '',          style: 'border: 1px solid #909399; color: #909399;' }
}

function getResourceTypeTag(resourceType) {
  return resourceTypeMap[resourceType] || { label: resourceType || '-', type: '', style: '' }
}

getList()
</script>
