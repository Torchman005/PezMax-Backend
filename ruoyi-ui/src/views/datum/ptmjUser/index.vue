<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="用户账号" prop="userName">
        <el-input
            v-model="queryParams.userName"
            placeholder="请输入用户账号"
            clearable
            @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="账号状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择账号状态" clearable>
          <el-option label="正常" value="1" />
          <el-option label="封禁" value="0" />
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
            v-hasPermi="['datum:user:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="success"
            plain
            icon="Edit"
            :disabled="single"
            @click="handleUpdate"
            v-hasPermi="['datum:user:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="danger"
            plain
            icon="Delete"
            :disabled="multiple"
            @click="handleDelete"
            v-hasPermi="['datum:user:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="warning"
            plain
            icon="Download"
            @click="handleExport"
            v-hasPermi="['datum:user:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="userList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="用户id" align="center" prop="userId" />
      <el-table-column label="用户账号" align="center" prop="userName" />
      <el-table-column label="头像" align="center" width="80">
        <template #default="scope">
          <el-image
            :src="getAvatarSrc(scope.row)"
            style="width: 40px; height: 40px; border-radius: 50%"
            fit="cover"
            :preview-src-list="[getAvatarSrc(scope.row)]"
            preview-teleported
            hide-on-click-modal
            @error="onTableAvatarError(scope.row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="上传文件数量" align="center" prop="count" />
      <el-table-column label="账号状态" align="center" prop="status">
        <template #default="scope">
          <el-tag :type="scope.row.status === '1' ? 'success' : 'danger'">
            {{ scope.row.status === '1' ? '正常' : '封禁' }}
          </el-tag>
        </template>
      </el-table-column>
      <!-- <el-table-column label="创建者" align="center" prop="creatBy" /> -->
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="220">
        <template #default="scope">
          <el-button
              v-if="scope.row.status === '1'"
              link
              type="danger"
              icon="Lock"
              @click="handleLock(scope.row)"
              v-hasPermi="['datum:user:edit']"
          >封禁</el-button>
          <el-button
              v-if="scope.row.status === '0'"
              link
              type="success"
              icon="Unlock"
              @click="handleUnlock(scope.row)"
              v-hasPermi="['datum:user:edit']"
          >解封</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['datum:user:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['datum:user:remove']">删除</el-button>
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

    <!-- 添加或修改平台用户对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="userRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="用户账号" prop="userName">
              <el-input v-model="form.userName" placeholder="请输入用户账号" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="密码" prop="password">
              <el-input v-model="form.password" placeholder="请输入密码" type="password" show-password />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="头像地址" prop="avatar">
              <el-input v-model="form.avatar" placeholder="请输入头像地址" />
              <el-image
                :src="form.avatar || defAva"
                style="width: 60px; height: 60px; border-radius: 50%; margin-top: 8px"
                fit="cover"
                :preview-src-list="[form.avatar || defAva]"
                preview-teleported
                hide-on-click-modal
                @error="onFormAvatarError"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="上传文件数量" prop="count">
              <el-input v-model="form.count" placeholder="请输入上传文件数量" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="创建者" prop="creatBy">
              <el-input v-model="form.creatBy" placeholder="请输入创建者" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="账号状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择账号状态">
                <el-option label="正常" value="1"></el-option>
                <el-option label="封禁" value="0"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
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

<script setup name="User">
import { ref, reactive, toRefs, getCurrentInstance } from 'vue'
import { listUser, getUser, delUser, addUser, updateUser, changeUserStatus } from "@/api/datum/user"
import defAva from '@/assets/images/luobo.jpg'

const { proxy } = getCurrentInstance()

const userList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const formAvatarError = ref(false)
const tableAvatarErrors = reactive(new Set())

/** 获取表格头像src（空或加载失败显示默认头像） */
function getAvatarSrc(row) {
  if (tableAvatarErrors.has(row.userId)) return defAva
  return row.avatar || defAva
}

/** 表格头像加载失败 */
function onTableAvatarError(row) {
  tableAvatarErrors.add(row.userId)
}

/** 表单头像加载失败 */
function onFormAvatarError() {
  formAvatarError.value = true
}

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    userName: null,
    status: null,
  },
  rules: {
    userName: [
      { required: true, message: "用户账号不能为空", trigger: "blur" }
    ],
    password: [
      { required: true, message: "密码不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询平台用户列表 */
function getList() {
  loading.value = true
  listUser(queryParams.value).then(response => {
    userList.value = response.rows
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
    userId: null,
    userName: null,
    password: null,
    avatar: null,
    count: null,
    status: '1',   // 默认正常
    creatBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null
  }
  formAvatarError.value = false
  proxy.resetForm("userRef")
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
  ids.value = selection.map(item => item.userId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加平台用户"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _userId = row.userId || ids.value
  getUser(_userId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改平台用户"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["userRef"].validate(valid => {
    if (valid) {
      if (form.value.userId != null) {
        updateUser(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        }).catch(() => {
          proxy.$modal.msgError("修改失败")
        })
      } else {
        addUser(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        }).catch(() => {
          proxy.$modal.msgError("新增失败")
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _userIds = row.userId || ids.value
  proxy.$modal.confirm('是否确认删除平台用户编号为"' + _userIds + '"的数据项？').then(function() {
    return delUser(_userIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('datum/user/export', {
    ...queryParams.value
  }, `user_${new Date().getTime()}.xlsx`)
}

/** 封禁用户 */
function handleLock(row) {
  proxy.$modal.confirm('是否确认封禁用户"' + row.userName + '"? 封禁后该用户将无法登录。').then(() => {
    return changeUserStatus({ userId: row.userId, status: '0' })
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("封禁成功")
  }).catch(() => {})
}

/** 解封用户 */
function handleUnlock(row) {
  proxy.$modal.confirm('是否确认解封用户"' + row.userName + '"?').then(() => {
    return changeUserStatus({ userId: row.userId, status: '1' })
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("解封成功")
  }).catch(() => {})
}

getList()
</script>