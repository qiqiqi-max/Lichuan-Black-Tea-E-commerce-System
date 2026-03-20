<template>
  <div class="detail-container">
    <div class="back-link" @click="$router.push('/')">
      <el-icon><ArrowLeft /></el-icon> 返回首页
    </div>
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="10" animated />
    </div>
    <div v-else-if="error" class="error-state">
      <el-empty :description="error">
        <el-button type="primary" @click="fetchProductData">重试</el-button>
      </el-empty>
    </div>
    <div v-else-if="product" class="product-main-wrapper">
      <div class="product-main">
        <div class="product-gallery">
          <img :src="product.image || '/images/default-product.jpg'" class="main-img" />
        </div>
        <div class="product-info">
          <h1 class="product-title">{{ product.name }}</h1>
          <div class="product-origin-tag" v-if="farmer">
            <el-icon><User /></el-icon> 
            农户：<a href="#" @click.prevent="showFarmerStory">{{ farmer.farmName || '助农合作社' }}</a>
          </div>
          <div class="price-box">
            <span class="currency">¥</span>
            <span class="amount">{{ product.price }}</span>
          </div>
          <div class="action-section">
            <el-input-number v-model="quantity" :min="1" :max="product.stock" size="large" />
            <button class="add-cart-btn" @click="handleAddToCart">加入购物车</button>
          </div>
        </div>
      </div>
      <div class="product-story">
        <h3>商品描述</h3>
        <p>{{ product.description }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useCartStore } from '../stores/cart';
import { useUserStore } from '../stores/user';
import { getProductDetail } from '../api/product';
import { getFarmerProfile } from '../api/farmer';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowLeft, Location, User, CircleCheck } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const cartStore = useCartStore();
const userStore = useUserStore();

const product = ref(null);
const farmer = ref(null);
const loading = ref(true);
const error = ref(null);
const quantity = ref(1);

const fetchProductData = async () => {
  loading.value = true;
  error.value = null;
  try {
    const productId = route.params.id;
    const productData = await getProductDetail(productId);
    product.value = productData;

    if (productData && productData.farmerId) {
      const farmerData = await getFarmerProfile(productData.farmerId);
      farmer.value = farmerData;
    }
  } catch (err) {
    error.value = '无法加载商品详情，请稍后再试';
    console.error(err);
  } finally {
    loading.value = false;
  }
};

onMounted(fetchProductData);

const showFarmerStory = () => {
  if (!farmer.value) {
    return ElMessage.info('暂无该农户的详细信息');
  }
  ElMessageBox.alert(farmer.value.intro || '这位农户很勤劳，但还没来得及写简介。', `来自 ${farmer.value.farmName || '未知茶园'} 的故事`, {
    confirmButtonText: '关闭',
  });
};

const handleAddToCart = () => {
  if (!userStore.isLoggedIn) {
    router.push('/login');
    return;
  }
  cartStore.addToCart(product.value, quantity.value);
  ElMessage.success('已成功加入购物车');
};
</script>

<style scoped>
/* Basic styles, can be expanded */
.detail-container { max-width: 1100px; margin: 40px auto; padding: 0 20px; }
.back-link { margin-bottom: 20px; cursor: pointer; }
.loading-state, .error-state { text-align: center; padding: 50px 0; }
.product-main { display: flex; gap: 50px; }
.product-gallery { flex: 1; }
.main-img { width: 100%; border-radius: 12px; }
.product-info { flex: 1; }
.product-title { font-size: 28px; }
.price-box { font-size: 24px; color: red; margin: 20px 0; }
.action-section { display: flex; gap: 20px; }
.product-story { margin-top: 40px; }
</style>
