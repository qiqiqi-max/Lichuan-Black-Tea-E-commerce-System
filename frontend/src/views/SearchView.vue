<template>
  <div class="search-view">
    <div class="container">
      <!-- Header -->
      <div class="search-header">
        <h2 class="search-title">搜索结果：<span class="highlight">"{{ searchStore.keyword }}"</span></h2>
        <span class="result-count">共找到 {{ searchStore.total }} 款相关好茶</span>
      </div>

      <!-- Loading -->
      <div v-if="searchStore.loading" class="loading-state">
        <el-skeleton :rows="3" animated />
      </div>

      <!-- Empty State -->
      <el-empty v-else-if="!searchStore.results.length" description="暂无匹配茶叶，试试其他关键词？">
        <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
      </el-empty>

      <!-- Results Grid -->
      <div v-else class="product-grid">
        <div class="product-item" v-for="p in searchStore.results" :key="p.id" @click="$router.push('/product/' + p.id)">
          <div class="p-image">
            <img :src="p.coverImg || '/images/products/product1.jpg'" loading="lazy" />
            <div class="farmer-tag" v-if="p.farmerName">
              产自{{ p.farmerName }}茶园
            </div>
          </div>
          <div class="p-info">
            <h3 class="p-name">{{ p.name }}</h3>
            <div class="p-tags">
              <span class="tag-origin">{{ p.origin || '利川' }}</span>
              <span class="tag-farmer" v-if="p.farmerName">{{ p.farmerName }}直供</span>
            </div>
            <div class="p-meta">
              <span class="p-price">¥{{ p.price }}</span>
              <button class="add-btn" @click.stop="addToCart(p)">加入购物车</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div class="pagination-wrapper" v-if="searchStore.total > 0">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="searchStore.total"
          :page-size="10"
          v-model:current-page="currentPage"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useSearchStore } from '../stores/search'
import { useRoute, useRouter } from 'vue-router'
import { useCartStore } from '../stores/cart'
import { useUserStore } from '../stores/user'
import api from '../api' // For addToCart if needed, but store handles it usually
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const searchStore = useSearchStore()
const cartStore = useCartStore()
const userStore = useUserStore()
const currentPage = ref(1)

// Initialize search from URL query
onMounted(() => {
  const keyword = route.query.keyword
  if (keyword) {
    searchStore.searchProducts(keyword, 1)
  }
})

// Watch for URL query changes (e.g. searching again from header)
watch(() => route.query.keyword, (newKeyword) => {
  if (newKeyword) {
    currentPage.value = 1
    searchStore.searchProducts(newKeyword, 1)
  }
})

const handlePageChange = (page) => {
  searchStore.searchProducts(searchStore.keyword, page)
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handleImageError = (e) => {
  e.target.src = '/images/default-product.jpg'
}

const addToCart = async (product) => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    await api.post(`/cart/add?userId=${userStore.user.username}`, {
        productId: product.id,
        productName: product.name,
        price: product.price,
        quantity: 1,
        coverImg: product.coverImg
    })
    cartStore.addToCart({
      id: product.id,
      name: product.name,
      price: product.price,
      coverImg: product.coverImg
    })
    ElMessage.success('已加入购物车')
  } catch (e) {
    ElMessage.error('加入购物车失败')
  }
}
</script>

<style scoped>
.search-view {
  padding: 40px 0;
  background-color: #FFF8F0;
  min-height: 80vh;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.search-header {
  margin-bottom: 30px;
  border-bottom: 1px solid #E5DED1;
  padding-bottom: 20px;
}

.search-title {
  font-family: "Noto Serif SC", serif;
  color: #5A4A42;
  font-size: 24px;
}

.highlight {
  color: #8B4513;
}

.result-count {
  font-size: 14px;
  color: #999;
  margin-left: 10px;
}

/* Product Grid (Consistent with Home) */
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 30px;
}

.product-item {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  transition: transform 0.3s, box-shadow 0.3s;
  cursor: pointer;
  border: 1px solid #eee;
}

.product-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 30px rgba(139, 69, 19, 0.1);
}

.p-image {
  position: relative;
  height: 260px;
}

.p-image img {
  width: 100%; height: 100%; object-fit: cover;
}

.farmer-tag {
  position: absolute;
  top: 10px; left: 10px;
  background: rgba(255,255,255,0.9);
  color: #8B4513;
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 20px;
}

.p-info {
  padding: 20px;
}

.p-name {
  font-size: 16px;
  margin-bottom: 10px;
  color: #333;
}

.p-tags {
  margin-bottom: 15px;
  font-size: 12px;
}

.tag-origin { color: #999; margin-right: 10px; }
.tag-farmer { color: #8B4513; background: #FFF0E0; padding: 2px 6px; border-radius: 4px; }

.p-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.p-price { font-size: 18px; color: #D35400; font-weight: 700; }

.add-btn {
  background: #8B4513;
  color: #fff;
  border: none;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s;
}

.add-btn:hover { background: #A0522D; }

.pagination-wrapper {
  margin-top: 50px;
  display: flex;
  justify-content: center;
}
</style>
