<template>
  <div class="order-container">
    <h2>我的订单</h2>
    <el-table :data="orders" style="width: 100%">
      <el-table-column prop="orderNo" label="订单号" width="280" />
      <el-table-column prop="createTime" label="时间" />
      <el-table-column prop="totalAmount" label="金额" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'
import { useUserStore } from '../stores/user'

const orders = ref([])
const userStore = useUserStore()

onMounted(async () => {
  api.defaults.headers.common['Authorization'] = userStore.token
  orders.value = await api.get('/orders/my')
})

const statusType = (status) => {
  if (status === 'DONE') return 'success'
  if (status === 'SHIPPED') return 'primary'
  return 'warning'
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
