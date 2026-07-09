<template>
  <div class="page-container">
    <div class="toolbar">
      <el-row :gutter="20" style="margin-bottom: 16px">
        <el-col :span="7">
          <el-input v-model="query.productName" placeholder="商品名称" clearable @keyup.enter="handleSearch" />
        </el-col>
        <el-col :span="5">
          <el-select v-model="query.enabled" clearable placeholder="状态" style="width: 100%">
            <el-option label="启用" :value="true" />
            <el-option label="禁用" :value="false" />
          </el-select>
        </el-col>
        <el-col :span="12">
          <el-button type="primary" @click="handleSearch" :loading="loading">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" icon="Plus" @click="handleAdd">新增特别推荐</el-button>
        </el-col>
      </el-row>
    </div>

    <el-table v-loading="loading" :data="list" border stripe style="width: 100%; margin-bottom: 16px">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="商品图片" width="100">
        <template #default="{ row }">
          <el-image :src="row.product?.coverImg" style="width: 60px; height: 60px" fit="cover" />
        </template>
      </el-table-column>
      <el-table-column label="商品名称" min-width="220" show-overflow-tooltip>
        <template #default="{ row }">{{ row.product?.name || '-' }}</template>
      </el-table-column>
      <el-table-column label="原价" width="120">
        <template #default="{ row }">¥{{ row.product?.price }}</template>
      </el-table-column>
      <el-table-column label="排序" width="130">
        <template #default="{ row }">
          <el-input-number :model-value="row.sortOrder" :min="0" :step="1" @change="(v) => updateRow(row, { sortOrder: v })" />
        </template>
      </el-table-column>
      <el-table-column label="启用" width="100">
        <template #default="{ row }">
          <el-switch :model-value="row.enabled" @change="(v) => updateRow(row, { enabled: v })" />
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="180" />
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑特别推荐' : '新增特别推荐'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="关联商品" prop="productId">
          <el-select v-model="form.productId" filterable placeholder="请选择商品" style="width: 100%">
            <el-option
              v-for="item in products"
              :key="item.id"
              :label="`${item.name} (¥${item.price})`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序值" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :step="1" />
        </el-form-item>
        <el-form-item label="是否启用" prop="enabled">
          <el-switch v-model="form.enabled" />
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
const products = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)

const query = reactive({
  productName: '',
  enabled: null
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  productId: null,
  sortOrder: 0,
  enabled: true
})

const rules = {
  productId: [{ required: true, message: '请选择关联商品', trigger: 'change' }]
}

const fetchProducts = async () => {
  try {
    products.value = await api.get('/products')
  } catch (e) {
    ElMessage.error(e.message || '获取商品列表失败')
  }
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await api.get('/special-recommendations/manage-page', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        productName: query.productName || undefined,
        enabled: query.enabled === null ? undefined : query.enabled
      }
    })
    list.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    ElMessage.error(e.message || '获取特别推荐失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  fetchList()
}

const handleReset = () => {
  query.productName = ''
  query.enabled = null
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
    productId: null,
    sortOrder: 0,
    enabled: true
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    productId: row.product?.id || null,
    sortOrder: row.sortOrder ?? 0,
    enabled: !!row.enabled
  })
  dialogVisible.value = true
}

const buildPayload = (source) => ({
  productId: source.productId,
  sortOrder: source.sortOrder ?? 0,
  enabled: source.enabled !== false
})

const submitForm = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    const payload = buildPayload(form)
    if (isEdit.value) {
      await api.put(`/special-recommendations/${form.id}`, payload)
      ElMessage.success('更新成功')
    } else {
      await api.post('/special-recommendations', payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchList()
  } catch (e) {
    if (e?.message?.includes('validate')) {
      return
    }
    ElMessage.error(e.message || (isEdit.value ? '更新失败' : '创建失败'))
  }
}

const updateRow = async (row, patch) => {
  const payload = {
    productId: row.product?.id,
    sortOrder: Number.isFinite(patch.sortOrder) ? patch.sortOrder : (row.sortOrder ?? 0),
    enabled: patch.enabled ?? row.enabled ?? true
  }
  try {
    await api.put(`/special-recommendations/${row.id}`, payload)
    ElMessage.success('更新成功')
    fetchList()
  } catch (e) {
    ElMessage.error(e.message || '更新失败')
    fetchList()
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该特别推荐记录吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      await api.delete(`/special-recommendations/${row.id}`)
      ElMessage.success('删除成功')
      fetchList()
    } catch (e) {
      ElMessage.error(e.message || '删除失败')
    }
  })
}

onMounted(async () => {
  await fetchProducts()
  await fetchList()
})
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
</style>
