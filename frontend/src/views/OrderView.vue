<template>
  <div class="order-container">
    <h2>我的订单</h2>

    <el-table v-loading="orderLoading" :data="orders" style="width: 100%; margin-bottom: 24px;">
      <el-table-column prop="orderNo" label="订单号" min-width="200" />
      <el-table-column prop="createTime" label="下单时间" width="170" />
      <el-table-column prop="totalAmount" label="金额" width="120">
        <template #default="{ row }">¥{{ row.totalAmount }}</template>
      </el-table-column>
      <el-table-column prop="status" label="订单状态" width="120">
        <template #default="{ row }">
          <el-tag :type="orderStatusType(row.status)">{{ orderStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="售后状态" width="140">
        <template #default="{ row }">
          <el-tag v-if="latestAfterSalesByOrderId[row.id]" :type="afterSalesStatusType(latestAfterSalesByOrderId[row.id].status)">
            {{ afterSalesStatusText(latestAfterSalesByOrderId[row.id].status) }}
          </el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="220">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 'WAIT_PAY'"
            type="primary"
            size="small"
            @click="handlePay(row)"
          >
            去支付
          </el-button>
          <el-button
            v-if="canApplyAfterSales(row)"
            type="warning"
            plain
            size="small"
            :disabled="hasActiveAfterSales(row.id)"
            @click="openAfterSalesDialog(row)"
          >
            申请售后
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <h2>我的售后申请</h2>
    <el-table v-loading="afterSalesLoading" :data="afterSalesList" style="width: 100%">
      <el-table-column prop="orderNo" label="订单号" min-width="190" />
      <el-table-column prop="type" label="类型" width="120">
        <template #default="{ row }">{{ afterSalesTypeText(row.type) }}</template>
      </el-table-column>
      <el-table-column prop="reason" label="申请原因" min-width="180" show-overflow-tooltip />
      <el-table-column prop="contactPhone" label="联系方式" width="140" />
      <el-table-column prop="status" label="状态" width="130">
        <template #default="{ row }">
          <el-tag :type="afterSalesStatusType(row.status)">{{ afterSalesStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="申请时间" width="170" />
      <el-table-column label="处理备注" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">{{ row.processRemark || row.reviewRemark || '-' }}</template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="申请售后" width="520px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="订单号">
          <el-input v-model="form.orderNo" disabled />
        </el-form-item>
        <el-form-item label="售后类型" prop="type">
          <el-select v-model="form.type" style="width: 100%">
            <el-option label="退款" value="REFUND" />
            <el-option label="退货退款" value="RETURN_REFUND" />
            <el-option label="换货" value="EXCHANGE" />
          </el-select>
        </el-form-item>
        <el-form-item label="申请原因" prop="reason">
          <el-input v-model="form.reason" placeholder="请填写申请原因" />
        </el-form-item>
        <el-form-item label="问题描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请描述问题详情" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="form.contactName" placeholder="可选" />
        </el-form-item>
        <el-form-item label="联系方式" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="手机号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitAfterSales">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, h, onMounted, reactive, ref } from 'vue'
import api from '../api'
import { useUserStore } from '../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()

const orderLoading = ref(false)
const afterSalesLoading = ref(false)
const submitting = ref(false)

const orders = ref([])
const afterSalesList = ref([])

const dialogVisible = ref(false)
const currentOrder = ref(null)
const formRef = ref(null)

const form = reactive({
  orderId: null,
  orderNo: '',
  type: 'REFUND',
  reason: '',
  description: '',
  contactName: '',
  contactPhone: ''
})

const rules = {
  type: [{ required: true, message: '请选择售后类型', trigger: 'change' }],
  reason: [{ required: true, message: '请填写申请原因', trigger: 'blur' }],
  description: [{ required: true, message: '请填写问题描述', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请填写联系方式', trigger: 'blur' }]
}

const latestAfterSalesByOrderId = computed(() => {
  const map = {}
  afterSalesList.value.forEach(item => {
    if (!map[item.orderId]) {
      map[item.orderId] = item
    }
  })
  return map
})

const fetchOrders = async () => {
  orderLoading.value = true
  try {
    orders.value = await api.get('/orders/my')
  } finally {
    orderLoading.value = false
  }
}

const fetchAfterSales = async () => {
  afterSalesLoading.value = true
  try {
    afterSalesList.value = await api.get('/after-sales/my')
  } catch (e) {
    // keep page usable even if after-sales endpoint is not ready
    afterSalesList.value = []
  } finally {
    afterSalesLoading.value = false
  }
}

onMounted(async () => {
  await Promise.all([fetchOrders(), fetchAfterSales()])
})

const canApplyAfterSales = (order) => {
  return ['PAID', 'WAIT_SHIP', 'SHIPPED', 'DONE'].includes(order.status)
}

const hasActiveAfterSales = (orderId) => {
  const active = ['PENDING_REVIEW', 'APPROVED', 'PROCESSING']
  return afterSalesList.value.some(i => i.orderId === orderId && active.includes(i.status))
}

const openAfterSalesDialog = (order) => {
  currentOrder.value = order
  form.orderId = order.id
  form.orderNo = order.orderNo
  form.type = 'REFUND'
  form.reason = ''
  form.description = ''
  form.contactName = userStore.user?.nickname || ''
  form.contactPhone = userStore.user?.phone || ''
  dialogVisible.value = true
}

const resetForm = () => {
  formRef.value?.clearValidate()
}

const submitAfterSales = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      await api.post('/after-sales', {
        orderId: form.orderId,
        type: form.type,
        reason: form.reason,
        description: form.description,
        contactName: form.contactName,
        contactPhone: form.contactPhone
      })
      ElMessage.success('售后申请已提交')
      dialogVisible.value = false
      await fetchAfterSales()
    } catch (e) {
      ElMessage.error(e.message || '提交失败')
    } finally {
      submitting.value = false
    }
  })
}

const handlePay = (order) => {
  ElMessageBox({
    title: '支付确认',
    message: h('div', { style: 'text-align: center' }, [
      h('p', { style: 'font-size: 16px; margin-bottom: 10px; color: #333;' }, [
        '支付金额：',
        h('span', { style: 'color: #f56c6c; font-weight: bold; font-size: 20px;' }, `¥${order.totalAmount}`)
      ]),
      h('img', {
        src: '/images/common/wechat-qrcode.jpg',
        style: 'width: 200px; height: 200px; margin: 10px auto; display: block; border: 1px solid #eee; padding: 5px;'
      }),
      h('p', { style: 'color: #666; margin-top: 10px; font-size: 14px;' }, '请使用微信扫码支付，支持助农！')
    ]),
    confirmButtonText: '我已支付',
    cancelButtonText: '取消',
    showCancelButton: true,
    closeOnClickModal: false,
    center: true
  }).then(async () => {
    try {
      await api.post(`/orders/${order.id}/pay`)
      ElMessage.success('支付成功')
      await fetchOrders()
    } catch (e) {
      ElMessage.error(e.message || '支付失败')
    }
  }).catch(() => {
    ElMessage.info('支付已取消')
  })
}

const orderStatusType = (status) => {
  if (status === 'DONE') return 'success'
  if (status === 'SHIPPED') return 'primary'
  if (status === 'WAIT_PAY') return 'warning'
  if (status === 'PAID' || status === 'WAIT_SHIP') return ''
  return 'info'
}

const orderStatusText = (status) => {
  const map = {
    WAIT_PAY: '待支付',
    PAID: '已支付',
    WAIT_SHIP: '待发货',
    SHIPPED: '已发货',
    DONE: '已完成',
    CANCELLED: '已取消'
  }
  return map[status] || status
}

const afterSalesTypeText = (type) => {
  const map = {
    REFUND: '退款',
    RETURN_REFUND: '退货退款',
    EXCHANGE: '换货'
  }
  return map[type] || type
}

const afterSalesStatusType = (status) => {
  const map = {
    PENDING_REVIEW: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
    PROCESSING: 'primary',
    COMPLETED: 'info'
  }
  return map[status] || 'info'
}

const afterSalesStatusText = (status) => {
  const map = {
    PENDING_REVIEW: '待审核',
    APPROVED: '已同意',
    REJECTED: '已拒绝',
    PROCESSING: '处理中',
    COMPLETED: '已完成'
  }
  return map[status] || status
}
</script>

<style scoped>
.order-container {
  max-width: 1100px;
  margin: 40px auto;
  padding: 20px;
  background: #fff;
}

h2 {
  margin: 0 0 16px;
  color: #333;
}
</style>
