<template>
  <div class="page-container">
    <div class="toolbar">
      <el-row :gutter="20" style="margin-bottom: 16px">
        <el-col :span="7">
          <el-input v-model="query.productName" placeholder="商品名称" clearable @keyup.enter="handleSearch" />
        </el-col>
        <el-col :span="5">
          <el-select v-model="query.status" clearable placeholder="状态" style="width: 100%">
            <el-option label="未开始" value="UPCOMING" />
            <el-option label="进行中" value="ONGOING" />
            <el-option label="已结束" value="ENDED" />
            <el-option label="已禁用" value="DISABLED" />
          </el-select>
        </el-col>
        <el-col :span="12">
          <el-button type="primary" @click="handleSearch" :loading="loading">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" icon="Plus" @click="handleAdd">新增秒杀活动</el-button>
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
      <el-table-column label="商品名称" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">{{ row.product?.name || '-' }}</template>
      </el-table-column>
      <el-table-column label="原价" width="110">
        <template #default="{ row }">¥{{ row.product?.price }}</template>
      </el-table-column>
      <el-table-column label="秒杀价" width="130">
        <template #default="{ row }">¥{{ row.seckillPrice }}</template>
      </el-table-column>
      <el-table-column prop="startTime" label="开始时间" width="170" />
      <el-table-column prop="endTime" label="结束时间" width="170" />
      <el-table-column label="启用" width="100">
        <template #default="{ row }">
          <el-switch :model-value="row.enabled" @change="(value) => updateRow(row, { enabled: value })" />
        </template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="statusType(row)">{{ statusText(row) }}</el-tag>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑秒杀活动' : '新增秒杀活动'" width="620px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="关联商品" prop="productId">
          <el-select v-model="form.productId" filterable placeholder="请选择商品" style="width: 100%">
            <el-option
              v-for="item in products"
              :key="item.id"
              :label="`${item.name} (原价 ¥${item.price})`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="秒杀价" prop="seckillPrice">
          <el-input-number v-model="form.seckillPrice" :precision="2" :step="0.1" :min="0.01" />
          <span class="form-tip" v-if="selectedProductPrice">原价：¥{{ selectedProductPrice }}</span>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="选择开始时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            placeholder="选择结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
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
import { computed, onMounted, reactive, ref } from 'vue'
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
  status: ''
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  productId: null,
  seckillPrice: null,
  startTime: '',
  endTime: '',
  enabled: true
})

const rules = {
  productId: [{ required: true, message: '请选择关联商品', trigger: 'change' }],
  seckillPrice: [{ required: true, message: '请输入秒杀价', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const selectedProductPrice = computed(() => {
  const selected = products.value.find((item) => item.id === form.productId)
  return selected?.price ?? null
})

const parseTime = (value) => {
  if (!value) return 0
  const normalized = value.includes('T') ? value : value.replace(' ', 'T')
  const ts = new Date(normalized).getTime()
  return Number.isNaN(ts) ? 0 : ts
}

const statusText = (row) => {
  if (!row.enabled) return '已禁用'
  const now = Date.now()
  const start = parseTime(row.startTime)
  const end = parseTime(row.endTime)
  if (now < start) return '未开始'
  if (now >= end) return '已结束'
  return '进行中'
}

const statusType = (row) => {
  const text = statusText(row)
  if (text === '进行中') return 'success'
  if (text === '未开始') return 'warning'
  if (text === '已禁用') return 'info'
  return 'danger'
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
    const res = await api.get('/flash-sales/manage-page', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        productName: query.productName || undefined,
        status: query.status || undefined
      }
    })
    list.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    ElMessage.error(e.message || '获取秒杀活动失败')
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
  query.status = ''
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
    seckillPrice: null,
    startTime: '',
    endTime: '',
    enabled: true
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    productId: row.product?.id || null,
    seckillPrice: row.seckillPrice,
    startTime: row.startTime,
    endTime: row.endTime,
    enabled: !!row.enabled
  })
  dialogVisible.value = true
}

const validateBusiness = (payload) => {
  const selected = products.value.find((item) => item.id === payload.productId)
  if (!selected) {
    throw new Error('关联商品不存在')
  }
  if (Number(payload.seckillPrice) >= Number(selected.price)) {
    throw new Error('秒杀价必须小于商品原价')
  }
  if (parseTime(payload.startTime) >= parseTime(payload.endTime)) {
    throw new Error('结束时间必须晚于开始时间')
  }
}

const buildPayload = (source) => ({
  productId: source.productId,
  seckillPrice: source.seckillPrice,
  startTime: source.startTime,
  endTime: source.endTime,
  enabled: source.enabled !== false
})

const submitForm = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    const payload = buildPayload(form)
    validateBusiness(payload)
    if (isEdit.value) {
      await api.put(`/flash-sales/${form.id}`, payload)
      ElMessage.success('更新成功')
    } else {
      await api.post('/flash-sales', payload)
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
    seckillPrice: row.seckillPrice,
    startTime: row.startTime,
    endTime: row.endTime,
    enabled: patch.enabled ?? row.enabled ?? true
  }
  try {
    validateBusiness(payload)
    await api.put(`/flash-sales/${row.id}`, payload)
    ElMessage.success('更新成功')
    fetchList()
  } catch (e) {
    ElMessage.error(e.message || '更新失败')
    fetchList()
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该秒杀活动吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      await api.delete(`/flash-sales/${row.id}`)
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

.form-tip {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}
</style>
