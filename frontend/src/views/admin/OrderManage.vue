<template>
  <div class="order-manage">
    <!-- Filter -->
    <div class="toolbar">
      <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 200px">
        <el-option label="待付款" value="WAIT_PAY" />
        <el-option label="待发货" value="WAIT_SHIP" />
        <el-option label="已发货" value="SHIPPED" />
        <el-option label="已完成" value="COMPLETED" />
        <el-option label="已取消" value="CANCELLED" />
      </el-select>
    </div>

    <!-- Table -->
    <el-table v-loading="loading" :data="filteredOrders" border stripe style="width: 100%">
      <el-table-column prop="orderNo" label="订单号" width="220" />
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column label="总金额" width="120">
        <template #default="{ row }">¥{{ row.totalAmount }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="180">
        <template #default="{ row }">
          <el-button type="primary" link icon="Edit" @click="handleEdit(row)">编辑</el-button>
          <el-button 
            v-if="row.status === 'WAIT_SHIP'" 
            type="primary" 
            size="small" 
            @click="handleShip(row)"
          >
            发货
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Edit Dialog -->
    <el-dialog v-model="dialogVisible" title="编辑订单" width="600px">
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
            <el-option label="待付款" value="WAIT_PAY" />
            <el-option label="待发货" value="WAIT_SHIP" />
            <el-option label="已发货" value="SHIPPED" />
            <el-option label="已完成" value="COMPLETED" />
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
import { Edit } from '@element-plus/icons-vue'

const loading = ref(false)
const orders = ref([])
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
    const res = await api.get('/admin/orders')
    orders.value = res
  } catch (e) {
    ElMessage.error('获取订单失败')
  } finally {
    loading.value = false
  }
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
    if (valid) {
      try {
        await api.put(`/admin/orders/${form.id}`, form)
        ElMessage.success('订单修改成功')
        dialogVisible.value = false
        fetchOrders()
      } catch (e) {
        ElMessage.error('修改失败: ' + (e.response?.data?.msg || e.message))
      }
    }
  })
}

const handleShip = async (row) => {
  try {
    await api.put(`/admin/orders/${row.id}/ship`)
    ElMessage.success('发货成功')
    fetchOrders()
  } catch (e) {
    ElMessage.error('发货失败')
  }
}

const statusType = (status) => {
  const map = { WAIT_PAY: 'info', WAIT_SHIP: 'danger', SHIPPED: 'primary', COMPLETED: 'success', CANCELLED: 'info' }
  return map[status] || 'info'
}

const statusText = (status) => {
  const map = { WAIT_PAY: '待付款', WAIT_SHIP: '待发货', SHIPPED: '已发货', COMPLETED: '已完成', CANCELLED: '已取消' }
  return map[status] || status
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.toolbar {
  margin-bottom: 20px;
}
.text-gray {
  color: #999;
  font-size: 12px;
}
</style>
