<template>
  <div class="product-manage">
    <!-- Toolbar -->
    <div class="toolbar">
      <el-button type="primary" icon="Plus" @click="handleAdd">新增商品</el-button>
    </div>

    <!-- Table -->
    <el-table v-loading="loading" :data="products" border stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="图片" width="100">
        <template #default="{ row }">
          <el-image :src="row.coverImg" style="width: 60px; height: 60px" fit="cover" :preview-src-list="[row.coverImg]" />
        </template>
      </el-table-column>
      <el-table-column prop="name" label="商品名称" min-width="150" show-overflow-tooltip />
      <el-table-column prop="price" label="价格" width="100">
        <template #default="{ row }">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="100" />
      <el-table-column prop="origin" label="产地" width="120" />
      <el-table-column prop="farmerName" label="农户" width="120" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link icon="Edit" @click="handleEdit(row)">编辑</el-button>
          <el-button type="danger" link icon="Delete" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Pagination -->
    <div class="pagination">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="10"
        v-model:current-page="currentPage"
        @current-change="fetchProducts"
      />
    </div>

    <!-- Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑商品' : '新增商品'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :precision="2" :step="0.1" :min="0" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="form.stock" :min="0" />
        </el-form-item>
        <el-form-item label="图片URL" prop="coverImg">
          <el-input v-model="form.coverImg" placeholder="https://..." />
        </el-form-item>
        <el-form-item label="产地" prop="origin">
          <el-input v-model="form.origin" />
        </el-form-item>
        <el-form-item label="农户" prop="farmerName">
          <el-input v-model="form.farmerName" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" />
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
import { ref, reactive, onMounted } from 'vue'
import api from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'

const loading = ref(false)
const products = ref([])
const total = ref(0)
const currentPage = ref(1)
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
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
}

const fetchProducts = async (page = 1) => {
  loading.value = true
  try {
    // Assuming backend supports pagination on /api/products/search or list
    // If not, using list and client side pagination or just search endpoint
    // Using search endpoint for pagination support
    const res = await api.get('/products/search', { params: { page, size: 10 } })
    products.value = res.content
    total.value = res.totalElements
    currentPage.value = page
  } catch (e) {
    // Fallback to list if search fails or not fully implemented
    const res = await api.get('/products')
    products.value = res
    total.value = res.length
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, { id: null, name: '', price: 0, stock: 0, coverImg: '', origin: '', farmerName: '', description: '' })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该商品吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      await api.delete(`/products/${row.id}`)
      ElMessage.success('删除成功')
      fetchProducts(currentPage.value)
    } catch (e) {
      ElMessage.error('删除失败')
    }
  })
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          await api.put(`/products/${form.id}`, form)
          ElMessage.success('更新成功')
        } else {
          await api.post('/products', form)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        fetchProducts(currentPage.value)
      } catch (e) {
        ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
      }
    }
  })
}

onMounted(() => {
  fetchProducts()
})
</script>

<style scoped>
.toolbar {
  margin-bottom: 20px;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
