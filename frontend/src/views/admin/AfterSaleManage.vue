<template>
  <div class="page-container">
    <div class="toolbar">
      <el-row :gutter="20" style="margin-bottom: 16px">
        <el-col :span="7">
          <el-input
            v-model="query.keyword"
            placeholder="搜索订单号/售后单号"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-col>
        <el-col :span="4">
          <el-input v-model="query.filterUserId" placeholder="用户ID" clearable @keyup.enter="handleSearch" />
        </el-col>
        <el-col :span="4">
          <el-select v-model="query.type" clearable placeholder="售后类型" style="width: 100%">
            <el-option label="退款" value="REFUND" />
            <el-option label="退货退款" value="RETURN_REFUND" />
            <el-option label="换货" value="EXCHANGE" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="query.status" clearable placeholder="处理状态" style="width: 100%">
            <el-option label="待审核" value="PENDING_REVIEW" />
            <el-option label="已同意" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
            <el-option label="处理中" value="PROCESSING" />
            <el-option label="已完成" value="COMPLETED" />
          </el-select>
        </el-col>
        <el-col :span="5">
          <el-button type="primary" @click="handleSearch" :loading="loading">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-col>
      </el-row>
    </div>

    <el-table v-loading="loading" :data="list" border stripe style="width: 100%; margin-bottom: 16px">
      <el-table-column prop="id" label="售后单号" width="90" />
      <el-table-column prop="orderNo" label="订单号" min-width="180" show-overflow-tooltip />
      <el-table-column prop="userId" label="用户ID" width="90" />
      <el-table-column label="类型" width="120">
        <template #default="{ row }">{{ typeText(row.type) }}</template>
      </el-table-column>
      <el-table-column prop="reason" label="申请原因" min-width="150" show-overflow-tooltip />
      <el-table-column prop="description" label="问题描述" min-width="180" show-overflow-tooltip />
      <el-table-column prop="contactPhone" label="联系方式" width="130" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="申请时间" width="170" />
      <el-table-column label="备注" min-width="160" show-overflow-tooltip>
        <template #default="{ row }">{{ row.processRemark || row.reviewRemark || '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="230" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 'PENDING_REVIEW'" type="success" link @click="review(row, 'APPROVE')">同意</el-button>
          <el-button v-if="row.status === 'PENDING_REVIEW'" type="danger" link @click="review(row, 'REJECT')">拒绝</el-button>
          <el-button v-if="row.status === 'APPROVED'" type="primary" link @click="updateStatus(row, 'PROCESSING')">处理中</el-button>
          <el-button v-if="row.status === 'APPROVED' || row.status === 'PROCESSING'" type="warning" link @click="updateStatus(row, 'COMPLETED')">
            完成
          </el-button>
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
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import api from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)

const query = reactive({
  keyword: '',
  filterUserId: '',
  status: '',
  type: ''
})

const fetchRequests = async () => {
  loading.value = true
  try {
    const res = await api.get('/after-sales/manage-page', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        keyword: query.keyword || undefined,
        filterUserId: query.filterUserId ? Number(query.filterUserId) : undefined,
        status: query.status || undefined,
        type: query.type || undefined
      }
    })
    list.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    ElMessage.error(e.message || '获取售后申请失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  fetchRequests()
}

const handleReset = () => {
  query.keyword = ''
  query.filterUserId = ''
  query.status = ''
  query.type = ''
  pageNum.value = 1
  fetchRequests()
}

const handlePageChange = (page) => {
  pageNum.value = page
  fetchRequests()
}

const review = async (row, action) => {
  const title = action === 'APPROVE' ? '同意申请' : '拒绝申请'
  try {
    const { value } = await ElMessageBox.prompt('请输入审核备注（可选）', title, {
      inputPlaceholder: '审核备注',
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: ''
    })
    await api.put(`/after-sales/${row.id}/review`, {
      action,
      remark: value || ''
    })
    ElMessage.success('操作成功')
    fetchRequests()
  } catch (e) {
    if (e === 'cancel' || e === 'close') return
    ElMessage.error(e.message || '操作失败')
  }
}

const updateStatus = async (row, status) => {
  const title = status === 'PROCESSING' ? '标记为处理中' : '标记为已完成'
  try {
    const { value } = await ElMessageBox.prompt('请输入处理备注（可选）', title, {
      inputPlaceholder: '处理备注',
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: ''
    })
    await api.put(`/after-sales/${row.id}/status`, {
      status,
      remark: value || ''
    })
    ElMessage.success('状态已更新')
    fetchRequests()
  } catch (e) {
    if (e === 'cancel' || e === 'close') return
    ElMessage.error(e.message || '更新失败')
  }
}

const typeText = (type) => {
  const map = {
    REFUND: '退款',
    RETURN_REFUND: '退货退款',
    EXCHANGE: '换货'
  }
  return map[type] || type
}

const statusText = (status) => {
  const map = {
    PENDING_REVIEW: '待审核',
    APPROVED: '已同意',
    REJECTED: '已拒绝',
    PROCESSING: '处理中',
    COMPLETED: '已完成'
  }
  return map[status] || status
}

const statusType = (status) => {
  const map = {
    PENDING_REVIEW: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
    PROCESSING: 'primary',
    COMPLETED: 'info'
  }
  return map[status] || 'info'
}

onMounted(fetchRequests)
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
