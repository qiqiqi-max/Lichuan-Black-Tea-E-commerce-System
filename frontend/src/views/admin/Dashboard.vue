<template>
  <div class="admin-container">
    <el-tabs>
      <el-tab-pane label="商品管理">
        <el-button type="primary" @click="showAddProduct = true">新增商品</el-button>
        <el-table :data="products" style="width: 100%; margin-top: 20px">
          <el-table-column prop="id" label="ID" width="50" />
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="price" label="价格" />
          <el-table-column prop="origin" label="产地" />
          <el-table-column prop="farmerName" label="农户" />
          <el-table-column label="操作">
            <template #default="{ row }">
              <el-button type="danger" size="small" @click="deleteProduct(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      
      <el-tab-pane label="订单管理">
        <el-table :data="orders" style="width: 100%">
          <el-table-column prop="orderNo" label="订单号" />
          <el-table-column prop="totalAmount" label="金额" />
          <el-table-column prop="status" label="状态" />
          <el-table-column label="操作">
            <template #default="{ row }">
              <el-button v-if="row.status === 'WAIT_SHIP'" type="success" size="small" @click="shipOrder(row.id)">发货</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- Add Product Dialog -->
    <el-dialog v-model="showAddProduct" title="新增商品">
      <el-form :model="newProduct" label-width="80px">
        <el-form-item label="名称"><el-input v-model="newProduct.name" /></el-form-item>
        <el-form-item label="价格"><el-input v-model="newProduct.price" /></el-form-item>
        <el-form-item label="产地"><el-input v-model="newProduct.origin" /></el-form-item>
        <el-form-item label="农户"><el-input v-model="newProduct.farmerName" /></el-form-item>
        <el-form-item label="分类"><el-input v-model="newProduct.category" /></el-form-item>
        <el-form-item label="图片"><el-input v-model="newProduct.coverImg" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddProduct = false">取消</el-button>
        <el-button type="primary" @click="createProduct">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../../api'
import { ElMessage } from 'element-plus'

const products = ref([])
const orders = ref([])
const showAddProduct = ref(false)
const newProduct = ref({ name: '', price: 0, origin: '', farmerName: '', category: '', coverImg: 'https://placehold.co/400x400' })

const loadData = async () => {
  products.value = await api.get('/products')
  orders.value = await api.get('/admin/orders')
}

onMounted(loadData)

const createProduct = async () => {
  await api.post('/admin/products', newProduct.value)
  ElMessage.success('添加成功')
  showAddProduct.value = false
  loadData()
}

const deleteProduct = async (id) => {
  await api.delete(`/admin/products/${id}`)
  ElMessage.success('删除成功')
  loadData()
}

const shipOrder = async (id) => {
  await api.put(`/admin/orders/${id}/ship`)
  ElMessage.success('已发货')
  loadData()
}
</script>

<style scoped>
.admin-container {
  padding: 20px;
  background: #fff;
}
</style>
