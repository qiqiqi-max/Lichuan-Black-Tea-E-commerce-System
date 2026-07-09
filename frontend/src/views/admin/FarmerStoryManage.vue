<template>
  <div class="page-container">
    <div class="toolbar">
      <el-row :gutter="20" style="margin-bottom: 16px">
        <el-col :span="6">
          <el-input v-model="query.farmerName" placeholder="茶农姓名" clearable @keyup.enter="handleSearch" />
        </el-col>
        <el-col :span="6">
          <el-input v-model="query.region" placeholder="所在地区" clearable @keyup.enter="handleSearch" />
        </el-col>
        <el-col :span="4">
          <el-select v-model="query.status" clearable placeholder="状态" style="width: 100%">
            <el-option label="启用" :value="true" />
            <el-option label="禁用" :value="false" />
          </el-select>
        </el-col>
        <el-col :span="8">
          <el-button type="primary" @click="handleSearch" :loading="loading">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" icon="Plus" @click="handleAdd">新增茶农故事</el-button>
        </el-col>
      </el-row>
    </div>

    <el-table v-loading="loading" :data="list" border stripe style="width: 100%; margin-bottom: 16px">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="图片" width="100">
        <template #default="{ row }">
          <el-image
            :src="row.imageUrl || '/images/farmers/farmer1.1.jpg'"
            style="width: 60px; height: 60px"
            fit="cover"
            :preview-src-list="[row.imageUrl || '/images/farmers/farmer1.1.jpg']"
          />
        </template>
      </el-table-column>
      <el-table-column prop="farmerName" label="茶农姓名" width="130" />
      <el-table-column prop="region" label="地区" width="160" />
      <el-table-column prop="tagline" label="一句话标签" min-width="180" show-overflow-tooltip />
      <el-table-column prop="sortOrder" label="排序" width="90" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-switch :model-value="row.status" @change="(v) => handleStatusChange(row, v)" />
        </template>
      </el-table-column>
      <el-table-column prop="createdTime" label="创建时间" width="170" />
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑茶农故事' : '新增茶农故事'" width="680px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="茶农姓名" prop="farmerName">
          <el-input v-model="form.farmerName" />
        </el-form-item>
        <el-form-item label="所在地区" prop="region">
          <el-input v-model="form.region" />
        </el-form-item>
        <el-form-item label="图片" prop="imageUrl">
          <el-upload
            :show-file-list="false"
            accept=".jpg,.jpeg,.png,.webp,image/jpeg,image/png,image/webp"
            :before-upload="beforeImageUpload"
            :http-request="uploadImage"
          >
            <el-button type="primary">选择并上传图片</el-button>
          </el-upload>
          <div class="upload-tip">支持 jpg/jpeg/png/webp，大小不超过 5MB</div>
          <div v-if="form.imageUrl" class="upload-preview">
            <el-image :src="form.imageUrl" style="width: 120px; height: 120px" fit="cover" :preview-src-list="[form.imageUrl]" />
          </div>
        </el-form-item>
        <el-form-item label="一句话标签" prop="tagline">
          <el-input v-model="form.tagline" maxlength="120" show-word-limit />
        </el-form-item>
        <el-form-item label="简介摘要" prop="summary">
          <el-input v-model="form.summary" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="详细故事" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="5" maxlength="3000" show-word-limit />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :step="1" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="form.status" />
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
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)

const query = reactive({
  farmerName: '',
  region: '',
  status: null
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  farmerName: '',
  region: '',
  imageUrl: '',
  tagline: '',
  summary: '',
  content: '',
  sortOrder: 0,
  status: true
})

const rules = {
  farmerName: [{ required: true, message: '请输入茶农姓名', trigger: 'blur' }],
  region: [{ required: true, message: '请输入地区', trigger: 'blur' }],
  tagline: [{ required: true, message: '请输入一句话标签', trigger: 'blur' }],
  summary: [{ required: true, message: '请输入简介摘要', trigger: 'blur' }]
}

const ALLOWED_IMAGE_EXTENSIONS = ['jpg', 'jpeg', 'png', 'webp']
const ALLOWED_IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/webp']
const MAX_IMAGE_SIZE = 5 * 1024 * 1024

const beforeImageUpload = (file) => {
  const name = file.name || ''
  const extension = name.includes('.') ? name.split('.').pop().toLowerCase() : ''
  const isAllowedExtension = ALLOWED_IMAGE_EXTENSIONS.includes(extension)
  const isAllowedType = !file.type || ALLOWED_IMAGE_TYPES.includes(file.type)
  if (!isAllowedExtension || !isAllowedType) {
    ElMessage.error('仅支持 jpg、jpeg、png、webp 格式图片')
    return false
  }
  if (file.size > MAX_IMAGE_SIZE) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

const uploadImage = async (options) => {
  const body = new FormData()
  body.append('file', options.file)
  try {
    const imageUrl = await api.post('/farmer-stories/upload-image', body, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    form.imageUrl = imageUrl
    ElMessage.success('图片上传成功')
    options.onSuccess && options.onSuccess({ url: imageUrl })
  } catch (error) {
    options.onError && options.onError(error)
  }
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await api.get('/farmer-stories/manage-page', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        farmerName: query.farmerName || undefined,
        region: query.region || undefined,
        status: query.status === null ? undefined : query.status
      }
    })
    list.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    ElMessage.error(e.message || '获取茶农故事列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  fetchList()
}

const handleReset = () => {
  query.farmerName = ''
  query.region = ''
  query.status = null
  pageNum.value = 1
  fetchList()
}

const handlePageChange = (page) => {
  pageNum.value = page
  fetchList()
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    farmerName: '',
    region: '',
    imageUrl: '',
    tagline: '',
    summary: '',
    content: '',
    sortOrder: 0,
    status: true
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    farmerName: row.farmerName || '',
    region: row.region || '',
    imageUrl: row.imageUrl || '',
    tagline: row.tagline || '',
    summary: row.summary || '',
    content: row.content || '',
    sortOrder: row.sortOrder ?? 0,
    status: row.status !== false
  })
  dialogVisible.value = true
}

const buildPayload = (source) => ({
  farmerName: source.farmerName,
  region: source.region,
  imageUrl: source.imageUrl,
  tagline: source.tagline,
  summary: source.summary,
  content: source.content,
  sortOrder: source.sortOrder ?? 0,
  status: source.status !== false
})

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      const payload = buildPayload(form)
      if (isEdit.value) {
        await api.put(`/farmer-stories/${form.id}`, payload)
        ElMessage.success('更新成功')
      } else {
        await api.post('/farmer-stories', payload)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      fetchList()
    } catch (e) {
      ElMessage.error(e.message || (isEdit.value ? '更新失败' : '创建失败'))
    }
  })
}

const handleStatusChange = async (row, status) => {
  try {
    await api.put(`/farmer-stories/${row.id}/status`, { status })
    ElMessage.success('状态更新成功')
    row.status = status
  } catch (e) {
    ElMessage.error(e.message || '状态更新失败')
    fetchList()
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除茶农故事「${row.farmerName}」吗？`, '提示', { type: 'warning' }).then(async () => {
    try {
      await api.delete(`/farmer-stories/${row.id}`)
      ElMessage.success('删除成功')
      fetchList()
    } catch (e) {
      ElMessage.error(e.message || '删除失败')
    }
  })
}

onMounted(fetchList)
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.toolbar {
  background: #f5f7fa;
  border-radius: 4px;
  padding: 16px;
  margin-bottom: 16px;
}

.pagination-wrap {
  text-align: right;
}

.upload-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

.upload-preview {
  margin-top: 10px;
}
</style>
