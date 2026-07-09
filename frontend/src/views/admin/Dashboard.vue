<template>
  <div class="dashboard-container" v-loading="loading">
    <el-row :gutter="20" class="mb-4">
      <el-col :span="6" v-for="item in visibleStats" :key="item.key">
        <el-card shadow="hover" class="stat-card clickable-card" @click="handleStatClick(item)">
          <div class="stat-icon" :style="{ backgroundColor: item.color }">
            <el-icon><component :is="item.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-title">{{ item.title }}</div>
            <div class="stat-value">{{ item.value }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mb-4">
      <el-col :span="24">
        <el-card shadow="hover" class="clickable-card" @click="router.push('/admin/orders')">
          <template #header>
            <div class="card-header">
              <div>
                <span class="chart-title">{{ userStore.role === 'FARMER' ? '我的销售数据' : '全平台数据大盘' }}</span>
                <span class="chart-subtitle">每日销售额统计（点击可查看订单）</span>
              </div>
            </div>
          </template>
          <div ref="chartRef" style="width: 100%; height: 500px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>最近订单</span>
          <el-button text @click="router.push('/admin/orders')">查看全部</el-button>
        </div>
      </template>
      <el-table :data="recentOrders" stripe style="width: 100%" @row-click="router.push('/admin/orders')">
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column prop="user" label="用户" width="140" />
        <el-table-column prop="totalAmount" label="金额" width="120">
          <template #default="{ row }">¥{{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="170" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, markRaw, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import api from '../../api'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { Box, Money, ShoppingCart, User } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const chartRef = ref(null)
let chartInstance = null
const loading = ref(false)

const stats = ref([
  { key: 'todayOrders', title: '今日订单', value: '0', icon: markRaw(ShoppingCart), color: '#409EFF', path: '/admin/orders' },
  { key: 'totalSales', title: '总销售额', value: '¥ 0.00', icon: markRaw(Money), color: '#67C23A', path: '/admin/orders' },
  { key: 'totalUsers', title: '总用户数', value: '0', icon: markRaw(User), color: '#E6A23C', path: '/admin/users' },
  {
    key: 'pendingOrders',
    title: '待发货',
    value: '0',
    icon: markRaw(Box),
    color: '#F56C6C',
    path: '/admin/orders',
    query: { status: 'WAIT_SHIP' }
  }
])

const visibleStats = computed(() => {
  if (userStore.role === 'FARMER') {
    return stats.value.filter(s => s.key !== 'totalUsers')
  }
  return stats.value
})

const recentOrders = ref([])
const chartData = ref({ dates: [], sales: [] })

const handleStatClick = (item) => {
  if (item.query) {
    router.push({ path: item.path, query: item.query })
    return
  }
  router.push(item.path)
}

const statusType = (status) => {
  const map = {
    WAIT_PAY: 'info',
    PAID: 'warning',
    WAIT_SHIP: 'warning',
    SHIPPED: 'primary',
    DONE: 'success',
    COMPLETED: 'success',
    CANCELLED: 'info'
  }
  return map[status] || 'info'
}

const statusText = (status) => {
  const map = {
    WAIT_PAY: '待支付',
    PAID: '已支付',
    WAIT_SHIP: '待发货',
    SHIPPED: '已发货',
    DONE: '已完成',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return map[status] || status
}

const updateStat = (key, value) => {
  const target = stats.value.find(s => s.key === key)
  if (target) target.value = value
}

const fetchStats = async () => {
  loading.value = true
  try {
    const res = await api.get('/admin/dashboard/stats')
    updateStat('todayOrders', String(res.todayOrders ?? 0))
    updateStat('totalSales', `¥ ${(Number(res.totalSales ?? 0)).toFixed(2)}`)
    updateStat('pendingOrders', String(res.pendingOrders ?? 0))
    updateStat('totalUsers', String(res.totalUsers ?? 0))

    recentOrders.value = res.recentOrders || []
    chartData.value = {
      dates: (res.last7DaysSales || []).map(i => i.date),
      sales: (res.last7DaysSales || []).map(i => Number(i.sales || 0))
    }
    initChart()
  } catch (e) {
    ElMessage.error('获取统计数据失败')
  } finally {
    loading.value = false
  }
}

const initChart = () => {
  if (!chartRef.value) return
  if (chartInstance) chartInstance.dispose()
  chartInstance = echarts.init(chartRef.value)

  const dates = chartData.value.dates
  const sales = chartData.value.sales

  if (!sales.length || sales.every(v => v === 0)) {
    chartInstance.setOption({
      title: {
        text: '暂无销售数据',
        left: 'center',
        top: 'center',
        textStyle: { color: '#999' }
      }
    })
    return
  }

  chartInstance.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>销售额: <span style="color:#8B4513;font-weight:bold">¥{c}</span>'
    },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLine: { lineStyle: { color: '#999' } }
    },
    yAxis: {
      type: 'value',
      axisLabel: { formatter: '¥{value}' },
      splitLine: { lineStyle: { type: 'dashed' } }
    },
    series: [
      {
        name: '销售额',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: { width: 4, color: '#8B4513' },
        itemStyle: { color: '#8B4513', borderColor: '#fff', borderWidth: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(139, 69, 19, 0.45)' },
            { offset: 1, color: 'rgba(139, 69, 19, 0.05)' }
          ])
        },
        data: sales
      }
    ]
  })
}

const resizeChart = () => {
  chartInstance?.resize()
}

onMounted(async () => {
  await nextTick()
  await fetchStats()
  window.addEventListener('resize', resizeChart)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeChart)
  chartInstance?.dispose()
})
</script>

<style scoped>
.chart-title {
  font-size: 18px;
  font-weight: bold;
  color: #8B4513;
  margin-right: 10px;
}

.chart-subtitle {
  font-size: 12px;
  color: #999;
}

.mb-4 {
  margin-bottom: 20px;
}

.clickable-card {
  cursor: pointer;
  transition: all 0.3s;
}

.clickable-card:hover {
  box-shadow: 0 8px 25px rgba(139, 69, 19, 0.15);
  transform: translateY(-4px);
  border-color: #8B4513;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 10px;
}

:deep(.el-card__body) {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 10px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
  margin-right: 15px;
}

.stat-info {
  flex: 1;
}

.stat-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
