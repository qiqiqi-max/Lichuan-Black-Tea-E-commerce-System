<template>
  <div class="page-container">
    <div class="toolbar">
      <el-row :gutter="20" style="margin-bottom: 16px">
        <el-col :span="4">
          <el-input v-model="query.username" placeholder="用户名" clearable @keyup.enter="handleSearch" />
        </el-col>
        <el-col :span="4">
          <el-input v-model="query.nickname" placeholder="昵称" clearable @keyup.enter="handleSearch" />
        </el-col>
        <el-col :span="4">
          <el-input v-model="query.phone" placeholder="手机号" clearable @keyup.enter="handleSearch" />
        </el-col>
        <el-col :span="4">
          <el-select v-model="query.role" clearable placeholder="角色" style="width: 100%">
            <el-option label="普通用户" value="USER" />
            <el-option label="入驻茶农" value="FARMER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-col>
        <el-col :span="8">
          <el-button type="primary" @click="handleSearch" :loading="loading">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" icon="Plus" @click="handleAdd">新增用户</el-button>
        </el-col>
      </el-row>
    </div>

    <el-table v-loading="loading" :data="users" border stripe style="width: 100%; margin-bottom: 16px">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" min-width="150" />
      <el-table-column prop="nickname" label="昵称" min-width="150" />
      <el-table-column prop="phone" label="手机号" min-width="150" />
      <el-table-column prop="role" label="角色" width="120">
        <template #default="{ row }">
          <el-tag :type="roleTagType(row.role)">{{ roleText(row.role) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link icon="Edit" @click="handleEdit(row)">编辑</el-button>
          <el-button type="danger" link icon="Delete" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination
        :current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next, jumper"
        @current-change="handlePageChange"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword" v-if="!isEdit">
          <el-input v-model="form.confirmPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="password" v-if="isEdit">
          <el-input v-model="form.password" type="password" show-password placeholder="留空则不修改" />
        </el-form-item>

        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="普通用户" value="USER" />
            <el-option label="入驻茶农" value="FARMER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import api from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const users = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)

const query = reactive({
  username: '',
  nickname: '',
  phone: '',
  role: ''
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  phone: '',
  role: 'USER'
})

const validatePass = (rule, value, callback) => {
  if (!isEdit.value && !value) {
    callback(new Error('请输入密码'))
  } else if (value && (value.length < 6 || value.length > 20)) {
    callback(new Error('密码长度在 6 到 20 个字符'))
  } else {
    if (form.confirmPassword !== '' && formRef.value) {
      formRef.value.validateField('confirmPassword')
    }
    callback()
  }
}

const validatePass2 = (rule, value, callback) => {
  if (!isEdit.value && !value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const validatePhone = (rule, value, callback) => {
  if (!value) return callback(new Error('请输入手机号'))
  if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号'))
  } else {
    callback()
  }
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ validator: validatePass, trigger: 'blur' }],
  confirmPassword: [{ validator: validatePass2, trigger: 'blur' }],
  phone: [{ required: true, validator: validatePhone, trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const roleText = (role) => {
  switch (role) {
    case 'ADMIN':
      return '管理员'
    case 'FARMER':
      return '入驻茶农'
    default:
      return '普通用户'
  }
}

const roleTagType = (role) => {
  switch (role) {
    case 'ADMIN':
      return 'danger'
    case 'FARMER':
      return 'success'
    default:
      return 'info'
  }
}

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await api.get('/admin/users/page', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        username: query.username || undefined,
        nickname: query.nickname || undefined,
        phone: query.phone || undefined,
        role: query.role || undefined
      }
    })
    users.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    ElMessage.error(e.message || '获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  fetchUsers()
}

const handleReset = () => {
  query.username = ''
  query.nickname = ''
  query.phone = ''
  query.role = ''
  pageNum.value = 1
  fetchUsers()
}

const handlePageChange = (page) => {
  pageNum.value = page
  fetchUsers()
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    username: '',
    password: '',
    confirmPassword: '',
    nickname: '',
    phone: '',
    role: 'USER'
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, { ...row, password: '', confirmPassword: '' })
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除用户 \"${row.username}\" 吗？`, '提示', { type: 'warning' }).then(async () => {
    try {
      await api.delete(`/admin/users/${row.id}`)
      ElMessage.success('删除成功')
      fetchUsers()
    } catch (e) {
      ElMessage.error(e.message || '删除失败')
    }
  })
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      const payload = { ...form }
      delete payload.confirmPassword
      if (isEdit.value) {
        if (!payload.password) delete payload.password
        await api.put(`/admin/users/${form.id}`, payload)
        ElMessage.success('更新成功')
      } else {
        await api.post('/admin/users', payload)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      fetchUsers()
    } catch (e) {
      ElMessage.error(e.message || (isEdit.value ? '更新失败' : '新增失败'))
    }
  })
}

onMounted(fetchUsers)
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.toolbar {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 4px;
  margin-bottom: 16px;
}

.pagination-wrap {
  text-align: right;
}
</style>
