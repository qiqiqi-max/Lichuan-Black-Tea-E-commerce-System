<template>
  <div class="order-container">
    <h2>我的订单</h2>
    <el-table :data="orders" style="width: 100%">
      <el-table-column prop="orderNo" label="订单号" width="280" />
      <el-table-column prop="createTime" label="时间" />
      <el-table-column prop="totalAmount" label="金额">
        <template #default="{ row }">
          ¥{{ row.totalAmount }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button 
            v-if="row.status === 'WAIT_PAY'" 
            type="primary" 
            size="small" 
            @click="handlePay(row)"
          >
            去支付
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted, h } from 'vue'
import api from '../api'
import { useUserStore } from '../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const orders = ref([])
const userStore = useUserStore()

const fetchOrders = async () => {
  api.defaults.headers.common['Authorization'] = userStore.token
  orders.value = await api.get('/orders/my')
}

onMounted(fetchOrders)

const statusType = (status) => {
  if (status === 'DONE' || status === 'PAID') return 'success'
  if (status === 'SHIPPED') return 'primary'
  if (status === 'WAIT_PAY') return 'warning'
  return 'info'
}

const statusText = (status) => {
  const map = {
    'WAIT_PAY': '待支付',
    'PAID': '已支付',
    'SHIPPED': '已发货',
    'DONE': '已完成'
  }
  return map[status] || status
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
      h('p', { style: 'color: #666; margin-top: 10px; font-size: 14px;' }, '请使用微信扫码支付，支持助农！'),
      h('p', { style: 'color: #8B4513; margin-top: 5px; font-weight: bold; font-size: 12px;' }, '此笔订单将直接助农增收，感谢您的支持！')
    ]),
    confirmButtonText: '我已支付',
    cancelButtonText: '取消',
    showCancelButton: true,
    closeOnClickModal: false,
    center: true,
  }).then(async () => {
     try {
         await api.post(`/orders/${order.id}/pay`)
         ElMessage.success('支付成功！')
         fetchOrders() // Refresh list
     } catch (e) {
         ElMessage.error(e.message || '支付失败')
     }
  }).catch(() => {
    ElMessage.info('支付已取消')
  })
}
</script>

<style scoped>
.order-container {
  max-width: 1000px;
  margin: 40px auto;
  padding: 20px;
  background: #fff;
}
</style>
