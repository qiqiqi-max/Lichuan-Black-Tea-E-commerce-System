<template>
  <div>
    <el-carousel height="300px">
      <el-carousel-item v-for="item in 3" :key="item" class="carousel-item">
        <div class="carousel-content">
          <h1>利川红 · 红遍天下</h1>
          <p>源自高山，匠心制作，助农增收</p>
        </div>
      </el-carousel-item>
    </el-carousel>

    <div class="product-grid">
      <el-row :gutter="20">
        <el-col :span="6" v-for="p in products" :key="p.id">
          <el-card :body-style="{ padding: '0px' }" class="product-card" @click="$router.push('/product/' + p.id)">
            <img :src="p.coverImg" class="image" />
            <div style="padding: 14px">
              <span class="product-name">{{ p.name }}</span>
              <div class="bottom">
                <div class="price">¥ {{ p.price }}</div>
                <div class="info">
                  <el-tag size="small" type="success">产地: {{ p.origin }}</el-tag>
                  <el-tag size="small" type="warning" style="margin-left: 5px">农户: {{ p.farmerName }}</el-tag>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'

const products = ref([])

onMounted(async () => {
  products.value = await api.get('/products')
})
</script>

<style scoped>
.carousel-item {
  background-color: #c0392b;
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
}
.carousel-content {
  text-align: center;
}
.product-grid {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}
.product-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: transform 0.3s;
}
.product-card:hover {
  transform: translateY(-5px);
}
.image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  display: block;
}
.product-name {
  font-weight: bold;
  font-size: 16px;
}
.bottom {
  margin-top: 13px;
}
.price {
  color: #f56c6c;
  font-size: 18px;
  margin-bottom: 8px;
}
.info {
  display: flex;
  flex-wrap: wrap;
}
</style>
