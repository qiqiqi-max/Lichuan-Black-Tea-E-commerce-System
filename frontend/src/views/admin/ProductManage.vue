<template>
  <div class="page-container">
    <div class="toolbar">
      <el-row :gutter="20" style="margin-bottom: 16px">
        <el-col :span="6">
          <el-input v-model="query.name" placeholder="商品名称" clearable @keyup.enter="handleSearch" />
        </el-col>
        <el-col :span="6">
          <el-input v-model="query.origin" placeholder="产地" clearable @keyup.enter="handleSearch" />
        </el-col>
        <el-col :span="6" v-if="userStore.role === 'ADMIN'">
          <el-input v-model="query.farmerName" placeholder="农户" clearable @keyup.enter="handleSearch" />
        </el-col>
        <el-col :span="6">
          <el-button type="primary" @click="handleSearch" :loading="loading">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" icon="Plus" @click="handleAdd">新增商品</el-button>
        </el-col>
      </el-row>
    </div>

    <el-table v-loading="loading" :data="products" border stripe style="width: 100%; margin-bottom: 16px">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="图片" width="100">
        <template #default="{ row }">
          <el-image :src="row.coverImg" style="width: 60px; height: 60px" fit="cover" :preview-src-list="[row.coverImg]" />
        </template>
      </el-table-column>
      <el-table-column prop="name" label="商品名称" min-width="180" show-overflow-tooltip />
      <el-table-column label="价格" width="110">
        <template #default="{ row }">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="100" />
      <el-table-column prop="origin" label="产地" width="140" />
      <el-table-column label="农户" width="140" v-if="userStore.role === 'ADMIN'">
        <template #default="{ row }">{{ row.farmer?.realName || '-' }}</template>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑商品' : '新增商品'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :precision="2" :step="0.1" :min="0" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="form.stock" :min="0" />
        </el-form-item>
        <el-form-item label="商品图片" prop="coverImg">
          <el-upload
            :show-file-list="false"
            accept=".jpg,.jpeg,.png,.webp,image/jpeg,image/png,image/webp"
            :before-upload="beforeImageUpload"
            :http-request="uploadProductImage"
          >
            <el-button type="primary">选择并上传图片</el-button>
          </el-upload>
          <div class="upload-tip">支持 jpg/jpeg/png/webp，大小不超过 5MB</div>
          <div v-if="form.coverImg" class="upload-preview">
            <el-image :src="form.coverImg" style="width: 120px; height: 120px" fit="cover" :preview-src-list="[form.coverImg]" />
          </div>
        </el-form-item>
        <el-form-item label="产地" prop="origin">
          <el-input v-model="form.origin" :disabled="userStore.role === 'FARMER'" />
        </el-form-item>
        <el-form-item label="农户" prop="farmerName" v-if="userStore.role === 'ADMIN'">
          <el-input v-model="form.farmerName" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" />
        </el-form-item>
        <el-form-item label="产品故事" prop="story">
          <el-input v-model="form.story" type="textarea" :rows="4" placeholder="请输入商品故事" />
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
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()

const loading = ref(false)
const products = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)

const query = reactive({
  name: '',
  origin: '',
  farmerName: ''
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  name: '',
  price: 0,
  stock: 0,
  coverImg: '',
  origin: '',
  farmerName: '',
  description: '',
  story: ''
})

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
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

const uploadProductImage = async (options) => {
  const body = new FormData()
  body.append('file', options.file)
  try {
    const imageUrl = await api.post('/products/upload-image', body, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    form.coverImg = imageUrl
    ElMessage.success('图片上传成功')
    options.onSuccess && options.onSuccess({ url: imageUrl })
  } catch (error) {
    options.onError && options.onError(error)
  }
}

const fetchProducts = async () => {
  loading.value = true
  try {
    const res = await api.get('/products/manage-page', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        name: query.name || undefined,
        origin: query.origin || undefined,
        farmerName: query.farmerName || undefined
      }
    })
    products.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    ElMessage.error(e.message || '获取商品失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  fetchProducts()
}

const handleReset = () => {
  query.name = ''
  query.origin = ''
  query.farmerName = ''
  pageNum.value = 1
  fetchProducts()
}

const handlePageChange = (page) => {
  pageNum.value = page
  fetchProducts()
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    name: '',
    price: 0,
    stock: 0,
    coverImg: '',
    origin: '',
    farmerName: '',
    description: '',
    story: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, {
    ...row,
    farmerName: row.farmerName || row.farmer?.realName || ''
  })
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该商品吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      await api.delete(`/products/${row.id}`)
      ElMessage.success('删除成功')
      fetchProducts()
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
      if (isEdit.value) {
        await api.put(`/products/${form.id}`, form)
        ElMessage.success('更新成功')
      } else {
        await api.post('/products', form)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      fetchProducts()
    } catch (e) {
      ElMessage.error(e.message || (isEdit.value ? '更新失败' : '创建失败'))
    }
  })
}

onMounted(fetchProducts)
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

.upload-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

.upload-preview {
  margin-top: 10px;
}
</style>
