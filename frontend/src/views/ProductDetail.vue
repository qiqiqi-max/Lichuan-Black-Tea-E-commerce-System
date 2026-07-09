<template>
  <div class="detail-container">
    <div class="back-link" @click="$router.push('/')">
      <el-icon><ArrowLeft /></el-icon>
      返回首页
    </div>

    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="10" animated />
      <p style="margin-top: 20px; color: #999">正在加载商品信息...</p>
    </div>

    <div v-else-if="error" class="error-state">
      <el-empty :description="error">
        <el-button type="primary" @click="$router.push('/')">返回商品列表</el-button>
      </el-empty>
    </div>

    <div v-else-if="product" class="product-main-wrapper">
      <div class="product-main">
        <div class="product-gallery">
          <div class="main-img-box">
            <img :src="selectedImage || product.coverImg" class="main-img" @error="handleImageError" />
          </div>
          <div class="thumb-list">
            <div
              v-for="img in thumbnailImages"
              :key="img"
              class="thumb-item"
              :class="{ active: selectedImage === img }"
              @click="selectThumbnail(img)"
            >
              <img :src="img" @error="handleImageError" />
            </div>
          </div>
        </div>

        <div class="product-info">
          <h1 class="product-title">{{ product.name }}</h1>
          <div class="product-origin-tag">
            <el-icon><Location /></el-icon>
            产地：{{ product.origin }}
            <span class="divider">|</span>
            <el-icon><User /></el-icon>
            农户：{{ product.farmerName }}
          </div>

          <div class="price-box" :class="{ 'flash-active': hasActiveFlashSale }">
            <div class="price-main">
              <span v-if="hasActiveFlashSale" class="flash-tag">限时秒杀</span>
              <span class="currency">¥</span>
              <span class="amount">{{ formatPrice(currentPrice) }}</span>
              <span v-if="hasActiveFlashSale" class="origin-amount">原价 ¥{{ formatPrice(product.price) }}</span>
            </div>
            <div class="price-extra">
              <span class="stock-tag" v-if="(selectedSpec ? selectedSpec.stock : product.stock) > 0">
                库存：{{ selectedSpec ? selectedSpec.stock : product.stock }}
              </span>
              <span class="stock-tag out" v-else>暂时缺货</span>
              <span v-if="hasActiveFlashSale" class="flash-countdown">剩余：{{ flashCountdownText }}</span>
            </div>
          </div>

          <div class="sku-section">
            <span class="label">规格：</span>
            <div class="sku-options">
              <div
                v-for="spec in specs"
                :key="spec.name"
                class="sku-item"
                :class="{ active: selectedSpec && selectedSpec.name === spec.name }"
                @click="selectSpec(spec)"
              >
                {{ spec.name }}
                <span class="spec-tag" v-if="spec.tag">{{ spec.tag }}</span>
              </div>
            </div>
            <div class="current-spec-desc" v-if="selectedSpec">{{ selectedSpec.desc }}</div>
          </div>

          <div class="action-section">
            <el-input-number v-model="quantity" :min="1" :max="selectedSpec ? selectedSpec.stock : 0" size="large" />
            <button class="add-cart-btn" @click="handleAddToCart" :disabled="!selectedSpec || selectedSpec.stock <= 0">
              加入购物车
            </button>
            <button class="buy-now-btn" @click="showPaymentDialog" :disabled="!selectedSpec || selectedSpec.stock <= 0">
              立即购买
            </button>
          </div>

          <div class="service-promise">
            <div class="promise-item"><el-icon><CircleCheck /></el-icon> 产地直供</div>
            <div class="promise-item"><el-icon><CircleCheck /></el-icon> 手工炒制</div>
            <div class="promise-item"><el-icon><CircleCheck /></el-icon> 破损包赔</div>
          </div>
        </div>
      </div>

      <div class="product-story">
        <div class="story-header">
          <h2>好茶背后的故事</h2>
          <p>每一片茶叶都来自利川高山云雾，带着茶农的心血与温度。</p>
        </div>

        <div class="story-content">
          <div class="story-block">
            <h3>高山云雾出好茶</h3>
            <p>{{ product.story || '暂无产品故事，欢迎在后台商品管理中补充内容。' }}</p>
          </div>

          <div class="farmer-highlight">
            <div class="farmer-avatar">
              <img :src="getFarmerImage(product.farmerName)" alt="茶农" @error="handleFarmerImgError" />
            </div>
            <div class="farmer-quote">
              <p>“做茶是一件慢活，认真对待每一片叶子，茶汤自然会回报你。”</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="error-state">
      <el-empty description="未找到该商品信息">
        <el-button type="primary" @click="$router.push('/')">返回商品列表</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { computed, h, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../api'
import { useCartStore } from '../stores/cart'
import { useUserStore } from '../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Location, User, CircleCheck } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const product = ref(null)
const loading = ref(true)
const error = ref(null)
const quantity = ref(1)
const cartStore = useCartStore()
const userStore = useUserStore()

const specs = ref([])
const selectedSpec = ref(null)
const selectedImage = ref('')

const nowTs = ref(Date.now())
let countdownTimer = null

const selectSpec = (spec) => {
  selectedSpec.value = spec
}

const collectImageCandidates = (value, collector) => {
  if (!value) return
  if (Array.isArray(value)) {
    value.forEach((item) => collectImageCandidates(item, collector))
    return
  }
  if (typeof value === 'string') {
    value
      .split(',')
      .map((s) => s.trim())
      .filter(Boolean)
      .forEach((s) => collector.push(s))
  }
}

const thumbnailImages = computed(() => {
  if (!product.value) return []
  const collector = []
  collectImageCandidates(product.value.coverImg, collector)
  collectImageCandidates(product.value.images, collector)
  collectImageCandidates(product.value.detailImages, collector)
  collectImageCandidates(product.value.galleryImages, collector)
  const deduped = [...new Set(collector)]
  return deduped.length > 0 ? deduped : ['/images/default-product.jpg']
})

watch(
  thumbnailImages,
  (images) => {
    if (!images.length) {
      selectedImage.value = '/images/default-product.jpg'
      return
    }
    if (!selectedImage.value || !images.includes(selectedImage.value)) {
      selectedImage.value = images[0]
    }
  },
  { immediate: true }
)

const selectThumbnail = (img) => {
  selectedImage.value = img
}

const farmerImageMap = {
  王大伯: '/images/farmers/farmer1.1.jpg',
  李阿姨: '/images/farmers/farmer2.1.jpg',
  张师傅: '/images/farmers/farmer3.1.jpg',
  刘叔: '/images/farmers/farmer4.1.jpg'
}

const getFarmerImage = (name) => farmerImageMap[name] || '/images/common/头像.jpg'

const handleFarmerImgError = (e) => {
  e.target.src = '/images/common/头像.jpg'
}

const handleImageError = (e) => {
  e.target.src = '/images/default-product.jpg'
}

const parseTimestamp = (timeString) => {
  if (!timeString) return 0
  const normalized = timeString.includes('T') ? timeString : timeString.replace(' ', 'T')
  const ts = new Date(normalized).getTime()
  return Number.isNaN(ts) ? 0 : ts
}

const formatCountdown = (endTime) => {
  const remain = parseTimestamp(endTime) - nowTs.value
  if (remain <= 0) return '已结束'

  const totalSeconds = Math.floor(remain / 1000)
  const days = Math.floor(totalSeconds / 86400)
  const hours = Math.floor((totalSeconds % 86400) / 3600)
  const minutes = Math.floor((totalSeconds % 3600) / 60)
  const seconds = totalSeconds % 60

  if (days > 0) {
    return `${days}天 ${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
  }
  return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
}

const formatPrice = (value) => Number(value || 0).toFixed(2)

const hasActiveFlashSale = computed(() => {
  return Boolean(product.value?.flashSaleActive && product.value?.flashSalePrice != null)
})

const currentPrice = computed(() => {
  if (selectedSpec.value?.price != null) {
    return Number(selectedSpec.value.price)
  }
  return Number(product.value?.price || 0)
})

const flashCountdownText = computed(() => {
  if (!hasActiveFlashSale.value) return ''
  return formatCountdown(product.value?.flashSaleEndTime)
})

onMounted(async () => {
  nextTick(() => {
    window.scrollTo({ top: 0, behavior: 'auto' })
  })

  countdownTimer = setInterval(() => {
    nowTs.value = Date.now()
  }, 1000)

  const id = route.params.id
  loading.value = true
  error.value = null
  product.value = null

  try {
    const res = await api.get(`/products/${id}`, { skipErrorMessage: true })
    if (res) {
      product.value = res
      if (!product.value.coverImg) product.value.coverImg = '/images/default-product.jpg'
      if (!product.value.origin) product.value.origin = '利川'
      if (!product.value.farmerName) product.value.farmerName = product.value.farmer?.realName || '助农合作社'

      const effectivePrice = hasActiveFlashSale.value
        ? Number(product.value.flashSalePrice)
        : Number(product.value.price || 0)

      specs.value = [
        {
          name: '默认规格',
          price: effectivePrice,
          stock: product.value.stock || 0,
          desc: '该商品当前在售规格',
          tag: hasActiveFlashSale.value ? '秒杀价' : ''
        }
      ]
      selectedSpec.value = specs.value[0]
    }
  } catch (e) {
    product.value = null
    error.value = e.message && e.message !== 'Error' ? e.message : '抱歉，该商品已下架或不存在'
    cartStore.removeFromCart({ productId: id, spec: null })
  } finally {
    loading.value = false
  }
})

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
})

const showPaymentDialog = () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  const amount = (Number(selectedSpec.value.price) * quantity.value).toFixed(2)

  ElMessageBox({
    title: '支付确认',
    message: h('div', { style: 'text-align: center' }, [
      h('p', { style: 'font-size: 16px; margin-bottom: 10px; color: #333;' }, [
        '支付金额：',
        h('span', { style: 'color: #f56c6c; font-weight: bold; font-size: 20px;' }, `¥${amount}`)
      ]),
      h('img', {
        src: '/images/common/wechat-qrcode.jpg',
        style: 'width: 200px; height: 200px; margin: 10px auto; display: block; border: 1px solid #eee; padding: 5px;'
      }),
      h('p', { style: 'color: #666; margin-top: 10px; font-size: 14px;' }, '请使用微信扫码支付，支持助农')
    ]),
    confirmButtonText: '我已支付',
    cancelButtonText: '取消',
    showCancelButton: true,
    closeOnClickModal: false,
    center: true
  })
    .then(async () => {
      try {
        const p = product.value
        const spec = selectedSpec.value
        const orderData = {
          totalAmount: (Number(spec.price) * quantity.value).toFixed(2),
          address: '默认收货地址',
          items: [
            {
              productId: p.id,
              productName: `${p.name} ${spec.name}`,
              price: spec.price,
              quantity: quantity.value
            }
          ]
        }

        const createdOrder = await api.post('/orders', orderData)
        if (createdOrder && createdOrder.id) {
          await api.post(`/orders/${createdOrder.id}/pay`)
        }

        ElMessage.success('支付成功，感谢支持')
        router.push('/orders')
      } catch (e) {
        ElMessage.error(e.message || '支付处理异常')
      }
    })
    .catch(() => {
      ElMessage.info('支付已取消')
    })
}

const handleAddToCart = async () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  const p = product.value
  const spec = selectedSpec.value

  try {
    await api.post(`/cart/add?userId=${userStore.user.id}`, {
      productId: p.id,
      productName: p.name,
      spec: spec.name,
      price: spec.price,
      quantity: quantity.value,
      coverImg: p.coverImg
    })

    cartStore.addToCart(
      {
        productId: p.id,
        name: p.name,
        spec: spec.name,
        price: spec.price,
        coverImg: p.coverImg
      },
      quantity.value
    )

    ElMessage.success(`已加入购物车：${spec.name}`)
  } catch (e) {
    ElMessage.error('加入失败，请稍后重试')
  }
}
</script>

<style scoped>
.detail-container {
  max-width: 1100px;
  margin: 40px auto;
  padding: 0 20px;
  min-height: 80vh;
}

.back-link {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
  color: #666;
  margin-bottom: 20px;
  font-size: 14px;
}

.back-link:hover {
  color: #8b4513;
}

.loading-state,
.error-state {
  padding: 80px 0;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.product-main {
  display: flex;
  gap: 50px;
  margin-bottom: 60px;
}

.product-gallery {
  flex: 1;
}

.main-img-box {
  width: 100%;
  height: 450px;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #eee;
  margin-bottom: 20px;
}

.main-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumb-list {
  display: flex;
  gap: 15px;
}

.thumb-item {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid transparent;
  cursor: pointer;
}

.thumb-item.active {
  border-color: #8b4513;
}

.thumb-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  flex: 1;
}

.product-title {
  font-size: 28px;
  color: #3e2b26;
  margin-bottom: 15px;
}

.product-origin-tag {
  display: flex;
  align-items: center;
  color: #888;
  font-size: 14px;
  margin-bottom: 25px;
}

.product-origin-tag .el-icon {
  margin-right: 4px;
}

.divider {
  margin: 0 15px;
  color: #ddd;
}

.price-box {
  background: #fff8f0;
  padding: 20px;
  border-radius: 10px;
  margin-bottom: 30px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.price-box.flash-active {
  border: 1px solid #f3c08d;
  background: linear-gradient(180deg, #fff4e8 0%, #fff9f3 100%);
}

.price-main {
  display: flex;
  align-items: baseline;
  gap: 8px;
  flex-wrap: wrap;
}

.flash-tag {
  display: inline-block;
  padding: 4px 10px;
  font-size: 12px;
  border-radius: 999px;
  color: #fff;
  background: linear-gradient(90deg, #ef4444 0%, #f97316 100%);
}

.currency {
  color: #d35400;
  font-size: 20px;
  font-weight: bold;
}

.amount {
  color: #d35400;
  font-size: 36px;
  font-weight: bold;
  margin-right: 2px;
}

.origin-amount {
  color: #9a9a9a;
  font-size: 15px;
  text-decoration: line-through;
}

.price-extra {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.stock-tag {
  background: #e8f5e9;
  color: #2e7d32;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.stock-tag.out {
  background: #ffebee;
  color: #c62828;
}

.flash-countdown {
  color: #8a4819;
  background: #fff2e3;
  border: 1px solid #f5c69f;
  border-radius: 4px;
  padding: 2px 8px;
  font-size: 12px;
  font-weight: 600;
}

.sku-section {
  margin-bottom: 30px;
}

.sku-section .label {
  display: block;
  margin-bottom: 10px;
  color: #666;
}

.sku-options {
  display: flex;
  gap: 15px;
}

.sku-item {
  border: 1px solid #ddd;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
  color: #555;
  transition: all 0.3s;
}

.sku-item:hover {
  border-color: #8b4513;
  color: #8b4513;
}

.sku-item.active {
  border-color: #8b4513;
  color: #8b4513;
  background: #fff8f0;
  font-weight: bold;
}

.spec-tag {
  font-size: 10px;
  background: #a0522d;
  color: #fff;
  padding: 1px 4px;
  border-radius: 2px;
  margin-left: 5px;
  vertical-align: text-top;
}

.current-spec-desc {
  margin-top: 10px;
  font-size: 12px;
  color: #8b4513;
  background: #fff8f0;
  padding: 5px 10px;
  border-radius: 4px;
  display: inline-block;
}

.action-section {
  display: flex;
  gap: 20px;
  margin-bottom: 40px;
}

.add-cart-btn,
.buy-now-btn {
  flex: 1;
  border: none;
  border-radius: 30px;
  font-size: 16px;
  cursor: pointer;
  transition: opacity 0.2s;
}

.add-cart-btn {
  background: linear-gradient(135deg, #8b4513 0%, #a0522d 100%);
  color: #fff;
}

.buy-now-btn {
  background: #fff0e0;
  color: #8b4513;
  border: 1px solid #8b4513;
}

.add-cart-btn:disabled,
.buy-now-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.service-promise {
  display: flex;
  gap: 30px;
  border-top: 1px solid #eee;
  padding-top: 20px;
  color: #666;
  font-size: 14px;
}

.promise-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.product-story {
  background: #fff;
  padding: 40px;
  border-radius: 12px;
  border: 1px solid #eee;
}

.story-header {
  text-align: center;
  margin-bottom: 40px;
}

.story-header h2 {
  font-size: 24px;
  color: #8b4513;
  margin-bottom: 10px;
}

.story-header p {
  color: #888;
}

.story-content {
  max-width: 800px;
  margin: 0 auto;
}

.story-block {
  margin-bottom: 40px;
}

.story-block h3 {
  font-size: 18px;
  margin-bottom: 15px;
  color: #333;
}

.story-block p {
  line-height: 1.8;
  color: #555;
  margin-bottom: 15px;
}

.farmer-highlight {
  display: flex;
  align-items: center;
  gap: 30px;
  background: #fff8f0;
  padding: 30px;
  border-radius: 12px;
}

.farmer-avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
}

.farmer-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.farmer-quote p {
  font-size: 16px;
  font-style: italic;
  color: #8b4513;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .product-main {
    flex-direction: column;
  }

  .main-img-box {
    height: 300px;
  }

  .farmer-highlight {
    flex-direction: column;
    text-align: center;
  }
}
</style>
