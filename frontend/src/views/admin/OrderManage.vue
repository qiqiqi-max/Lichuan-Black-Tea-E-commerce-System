<template>
  <div class="order-manage">
    <div class="toolbar">
      <el-row :gutter="20" style="margin-bottom: 20px">
        <el-col :span="6">
          <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 100%" @change="handleStatusChange">
            <el-option label="待支付" value="WAIT_PAY" />
            <el-option label="已支付" value="PAID" />
            <el-option label="待发货" value="WAIT_SHIP" />
            <el-option label="已发货" value="SHIPPED" />
            <el-option label="已完成" value="DONE" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-col>
        <el-col :span="10">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索订单号、地址、用户ID..."
            clearable
            @keyup.enter="handleSearch"
            @clear="handleClear"
          >
            <template #suffix>
              <el-icon class="is-loading" v-if="loading"><Loading /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="8">
          <el-button type="primary" @click="handleSearch" :loading="loading">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-col>
      </el-row>
    </div>

    <el-table v-loading="loading" :data="filteredOrders" border stripe style="width: 100%; margin-bottom: 20px">
      <el-table-column prop="orderNo" label="订单号" width="180" show-overflow-tooltip />
      <el-table-column prop="userId" label="用户ID" width="80" />
      <el-table-column label="总金额" width="100">
        <template #default="{ row }">¥{{ row.totalAmount }}</template>
      </el-table-column>
      <el-table-column prop="address" label="收货地址" min-width="150" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="150" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button
            v-if="row.status === 'WAIT_SHIP' || row.status === 'PAID'"
            type="success"
            link
            size="small"
            @click="handleShip(row)"
          >
            发货
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div style="text-align: right; margin-bottom: 20px">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="handlePageChange"
        @size-change="handlePageSizeChange"
      />
    </div>

    <el-dialog v-model="dialogVisible" title="编辑订单" width="600px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="收货人" prop="receiverName">
              <el-input v-model="form.receiverName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="receiverPhone">
              <el-input v-model="form.receiverPhone" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="收货地址" prop="address">
          <el-input v-model="form.address" />
        </el-form-item>

        <el-form-item label="订单状态" prop="status">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="待支付" value="WAIT_PAY" />
            <el-option label="已支付" value="PAID" />
            <el-option label="待发货" value="WAIT_SHIP" />
            <el-option label="已发货" value="SHIPPED" />
            <el-option label="已完成" value="DONE" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" />
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
import { ref, reactive, computed, onMounted } from 'vue'
import api from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'

const loading = ref(false)
const orders = ref([])
const total = ref(0)

const pageNum = ref(1)
const pageSize = ref(20)

const searchKeyword = ref('')
const statusFilter = ref('')

const dialogVisible = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  receiverName: '',
  receiverPhone: '',
  address: '',
  status: '',
  remark: ''
})

const rules = {
  receiverName: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  receiverPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  address: [{ required: true, message: '请输入收货地址', trigger: 'blur' }]
}

const filteredOrders = computed(() => {
  if (!statusFilter.value) return orders.value
  return orders.value.filter(o => o.status === statusFilter.value)
})

const fetchOrders = async () => {
  loading.value = true
  try {
    const res = await api.get('/orders/list', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        keyword: searchKeyword.value || undefined
      }
    })

    if (res) {
      orders.value = res.records || []
      total.value = res.total || 0
    }
  } catch (e) {
    ElMessage.error('获取订单失败: ' + (e.response?.data?.msg || e.message))
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  fetchOrders()
}

const handleClear = () => {
  searchKeyword.value = ''
  pageNum.value = 1
  fetchOrders()
}

const handleReset = () => {
  searchKeyword.value = ''
  statusFilter.value = ''
  pageNum.value = 1
  pageSize.value = 20
  fetchOrders()
}

const handleStatusChange = () => {
  pageNum.value = 1
}

const handlePageChange = (newPage) => {
  pageNum.value = newPage
  fetchOrders()
}

const handlePageSizeChange = (newSize) => {
  pageSize.value = newSize
  pageNum.value = 1
  fetchOrders()
}

const handleEdit = (row) => {
  Object.assign(form, {
    id: row.id,
    receiverName: row.receiverName || '',
    receiverPhone: row.receiverPhone || '',
    address: row.address || '',
    status: row.status,
    remark: row.remark || ''
  })
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      await api.put(`/orders/${form.id}`, form)
      ElMessage.success('订单修改成功')
      dialogVisible.value = false
      fetchOrders()
    } catch (e) {
      ElMessage.error('修改失败: ' + (e.response?.data?.msg || e.message))
    }
  })
}

const resetForm = () => {
  formRef.value?.clearValidate()
}

const handleShip = async (row) => {
  ElMessageBox.confirm('确认发货？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await api.put(`/orders/${row.id}/ship`)
        ElMessage.success('发货成功')
        fetchOrders()
      } catch (e) {
        ElMessage.error('发货失败: ' + (e.response?.data?.msg || e.message))
      }
    })
    .catch(() => {})
}

const statusType = (status) => {
  const map = {
    WAIT_PAY: 'info',
    WAIT_SHIP: 'danger',
    PAID: 'warning',
    SHIPPED: 'primary',
    DONE: 'success',
    CANCELLED: 'info'
  }
  return map[status] || 'info'
}

const statusText = (status) => {
  const map = {
    WAIT_PAY: '待支付',
    WAIT_SHIP: '待发货',
    PAID: '已支付',
    SHIPPED: '已发货',
    DONE: '已完成',
    CANCELLED: '已取消'
  }
  return map[status] || status
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.order-manage {
  padding: 20px;
}

.toolbar {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.el-pagination {
  text-align: right;
}
</style>
