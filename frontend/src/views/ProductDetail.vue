<template>
  <div class="detail-container" v-if="product">
    <el-row :gutter="40">
      <el-col :span="10">
        <img :src="product.coverImg" class="detail-img" />
      </el-col>
      <el-col :span="14">
        <h1>{{ product.name }}</h1>
        <div class="price">¥ {{ product.price }}</div>
        <div class="meta">
          <p><strong>产地：</strong>{{ product.origin }}</p>
          <p><strong>农户：</strong>{{ product.farmerName }}</p>
          <p><strong>分类：</strong>{{ product.category }}</p>
        </div>
        <div class="desc">{{ product.description }}</div>
        
        <div class="actions">
          <el-input-number v-model="quantity" :min="1" :max="product.stock" />
          <el-button type="primary" size="large" @click="handleAddToCart" style="margin-left: 20px">加入购物车</el-button>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '../api'
import { useCartStore } from '../stores/cart'
import { ElMessage } from 'element-plus'

const route = useRoute()
const product = ref(null)
const quantity = ref(1)
const cartStore = useCartStore()

onMounted(async () => {
  product.value = await api.get(`/products?id=${route.params.id}`) 
  // API returns list for search, but I didn't implement getById strictly in controller (oops).
  // Actually ProductController has list(search). It doesn't have getById. 
  // I need to fix Backend Controller or just filter here if list returns all.
  // Wait, I implemented `list(@RequestParam(required = false) String search)`.
  // I did NOT implement `getById` in backend in previous step! 
  // I will fetch all and find one for now, or fix backend later.
  // Let's assume I fix backend or just fetch all.
  const all = await api.get('/products')
  product.value = all.find(p => p.id == route.params.id)
})

const handleAddToCart = () => {
  cartStore.addToCart(product.value, quantity.value)
  ElMessage.success('已加入购物车')
}
</script>

<style scoped>
.detail-container {
  max-width: 1000px;
  margin: 40px auto;
  padding: 20px;
  background: #fff;
}
.detail-img {
  width: 100%;
}
.price {
  font-size: 24px;
  color: #f56c6c;
  margin: 20px 0;
}
.meta p {
  color: #666;
}
.desc {
  margin: 20px 0;
  line-height: 1.6;
}
</style>
