<template>
  <div class="home-container">
    <el-carousel height="60vh" :interval="5000" arrow="hover" indicator-position="outside" class="banner-carousel">
      <el-carousel-item v-for="(img, index) in bannerImages" :key="index">
        <div class="banner-wrapper">
          <img :src="img" alt="利川红茶" class="banner-img" />
          <div class="banner-overlay"></div>
          <div class="banner-content">
            <h1>利川红茶 · 茶农直供</h1>
            <p>一杯好茶，连接茶农与茶客</p>
            <el-button class="explore-btn" @click="scrollToSection('flash-sale')">立即选购</el-button>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>

    <section class="section intro-section">
      <div class="container">
        <h2 class="section-title">为什么选择我们</h2>
        <div class="value-grid">
          <article class="value-card" v-for="item in valueCards" :key="item.title">
            <div class="value-icon-wrap">
              <el-icon class="value-icon">
                <component :is="item.icon" />
              </el-icon>
            </div>
            <h3 class="value-title">{{ item.title }}</h3>
            <p class="value-desc">{{ item.desc }}</p>
          </article>
        </div>
      </div>
    </section>

    <section class="section flash-section" id="flash-sale">
      <div class="container">
        <div class="section-header">
          <div>
            <span class="section-eyebrow section-eyebrow-flash">活动主推</span>
            <h2 class="section-title">限时特价秒杀</h2>
            <p class="section-subtitle">每日限量，错过当天就要再等一季茶香</p>
          </div>
        </div>
        <el-empty v-if="!activeFlashSales.length" description="当前暂无秒杀活动" />
        <article v-else-if="activeFlashSales.length === 1" class="flash-hero-card" @click="goToProduct(activeFlashSales[0].product.id)">
          <div class="flash-hero-image-box">
            <img :src="activeFlashSales[0].product.coverImg" loading="lazy" @error="handleImageError" />
            <span class="flash-badge hero-badge">今日主推秒杀</span>
          </div>
          <div class="flash-hero-info">
            <div class="hero-kicker">
              <span class="hero-kicker-tag">限时特价</span>
              <span class="hero-kicker-text">当日活动</span>
            </div>
            <h3 class="flash-name hero-name">{{ activeFlashSales[0].product.name }}</h3>
            <p class="flash-origin hero-origin">{{ activeFlashSales[0].product.origin }} · {{ activeFlashSales[0].product.farmerName }}</p>
            <p class="hero-slogan">{{ getFlashHighlight(activeFlashSales[0]) }}</p>
            <div class="flash-price-row hero-price-row">
              <span class="hero-price-label">秒杀价</span>
              <span class="flash-price">¥{{ activeFlashSales[0].seckillPrice }}</span>
              <span class="origin-price">原价 ¥{{ activeFlashSales[0].product.price }}</span>
            </div>
            <div class="countdown hero-countdown">
              <span class="hero-countdown-label">倒计时</span>
              <strong>{{ formatCountdown(activeFlashSales[0].endTime) }}</strong>
            </div>
            <button class="flash-buy-btn hero-buy-btn" @click.stop="goToProduct(activeFlashSales[0].product.id)">立即抢购</button>
          </div>
        </article>
        <div v-else class="flash-grid">
          <div class="flash-card" v-for="item in activeFlashSales" :key="item.id" @click="goToProduct(item.product.id)">
            <div class="flash-image-box">
              <img :src="item.product.coverImg" loading="lazy" @error="handleImageError" />
              <span class="flash-badge">限时秒杀</span>
            </div>
            <div class="flash-info">
              <h3 class="flash-name">{{ item.product.name }}</h3>
              <p class="flash-origin">{{ item.product.origin }} · {{ item.product.farmerName }}</p>
              <p class="flash-brief">{{ getProductTeaser(item.product) }}</p>
              <div class="flash-price-row">
                <span class="flash-price">¥{{ item.seckillPrice }}</span>
                <span class="origin-price">¥{{ item.product.price }}</span>
              </div>
              <div class="countdown">剩余时间：{{ formatCountdown(item.endTime) }}</div>
              <button class="flash-buy-btn" @click.stop="goToProduct(item.product.id)">立即抢购</button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="section special-section" id="special">
      <div class="container">
        <div class="section-header">
          <div>
            <span class="section-eyebrow section-eyebrow-special">精选专栏</span>
            <h2 class="section-title">特别推荐</h2>
            <p class="section-subtitle">本周精选茶品，来自茶山一线的口碑直供</p>
          </div>
        </div>
        <el-empty v-if="!specialRecommendations.length" description="暂无特别推荐商品" />
        <div v-else class="product-grid special-grid">
          <div class="product-item special-item" v-for="item in specialRecommendations" :key="item.id" @click="goToProduct(item.product.id)">
            <span class="special-corner">精选</span>
            <div class="p-image">
              <img :src="item.product.coverImg" loading="lazy" @error="handleImageError" />
            </div>
            <div class="p-info">
              <h3 class="p-name">{{ item.product.name }}</h3>
              <div class="p-tags">
                <span class="tag-origin">{{ item.product.origin }}</span>
                <span class="tag-farmer">{{ item.product.farmerName }} 直供</span>
              </div>
              <p class="p-brief">{{ getProductTeaser(item.product) }}</p>
              <div class="p-meta">
                <div class="p-price-group">
                  <span class="price-label">精选价</span>
                  <span class="p-price">¥{{ item.product.price }}</span>
                </div>
                <button class="quick-btn" @click.stop="addToCart(item.product)">加入购物车</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="section all-products-section" id="hot-products">
      <div class="container">
        <div class="section-header">
          <div>
            <span class="section-eyebrow section-eyebrow-all">全品浏览</span>
            <h2 class="section-title">全部茶品</h2>
            <p class="section-subtitle">完整茶品清单，按销量与品质持续更新</p>
          </div>
        </div>
        <el-empty v-if="!hotProducts.length" description="暂无商品" />
        <div v-else class="product-grid all-grid">
          <div class="product-item all-item" v-for="p in hotProducts" :key="p.id" @click="goToProduct(p.id)">
            <div class="p-image">
              <img :src="p.coverImg" loading="lazy" @error="handleImageError" />
              <button class="quick-btn image-quick-btn" @click.stop="addToCart(p)">加入购物车</button>
            </div>
            <div class="p-info">
              <h3 class="p-name">{{ p.name }}</h3>
              <div class="p-tags">
                <span class="tag-origin">{{ p.origin }}</span>
                <span class="tag-farmer">{{ p.farmerName }}</span>
              </div>
              <p class="p-brief">{{ getProductTeaser(p) }}</p>
              <div class="p-meta">
                <span class="p-price">¥{{ p.price }}</span>
              </div>
              <div class="p-actions">
                <span class="p-sales">已售 {{ p.sales || 0 }}</span>
                <button class="ghost-btn" @click.stop="goToProduct(p.id)">查看详情</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="section farmers-section tea-story-section">
      <div class="container">
        <div class="section-header story-header">
          <div>
            <span class="section-eyebrow section-eyebrow-story">源头人物</span>
            <h2 class="section-title">茶农故事</h2>
            <p class="story-subtitle">看见每一片茶叶背后的坚守与温度</p>
          </div>
        </div>
        <el-empty v-if="!farmerStories.length" description="暂无茶农故事" />
        <el-carousel v-else :interval="5000" arrow="always" indicator-position="none" height="420px" class="story-carousel">
          <el-carousel-item v-for="story in farmerStories" :key="story.id">
            <article class="story-card">
              <div class="story-image-wrap">
                <img :src="story.imageUrl" :alt="story.farmerName" loading="lazy" @error="handleStoryImageError" />
              </div>
              <div class="story-content">
                <div class="story-content-body">
                  <div class="story-meta">
                    <h3 class="story-name">{{ story.farmerName }}</h3>
                    <span class="story-region">{{ story.region }}</span>
                  </div>
                  <p class="story-role">利川茶山守艺人</p>
                  <p class="story-tagline">{{ story.tagline }}</p>
                  <p class="story-summary">{{ story.summary }}</p>
                  <div class="story-chip-group">
                    <span class="story-chip">茶农直供</span>
                    <span class="story-chip">真实可溯源</span>
                  </div>
                </div>
                <div class="story-content-footer">
                  <button type="button" class="story-btn" @click.stop="goToFarmerStoryDetail(story.id)">
                    了解茶农的故事
                  </button>
                </div>
              </div>
            </article>
          </el-carousel-item>
        </el-carousel>
      </div>
    </section>

    <footer class="home-footer">
      <div class="container footer-inner">
        <div class="footer-main">
          <h3>利川红茶助农平台</h3>
          <p>让每一杯好茶都连接真实茶山，让每一次购买都成为对茶农的支持。</p>
        </div>
        <div class="footer-col">
          <h4>平台理念</h4>
          <ul class="footer-list">
            <li>茶农直供</li>
            <li>真实可溯源</li>
            <li>品质安心</li>
          </ul>
        </div>
        <div class="footer-col">
          <h4>助农价值</h4>
          <ul class="footer-list">
            <li>稳定增收</li>
            <li>乡村产业共建</li>
            <li>传承地方茶文化</li>
          </ul>
        </div>
      </div>
      <div class="footer-bottom">© 2026 利川红茶助农平台｜一杯好茶，温暖一方茶山</div>
    </footer>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '../stores/cart'
import { useUserStore } from '../stores/user'
import api from '../api'
import { ElMessage } from 'element-plus'
import { Goods, Location, CircleCheck } from '@element-plus/icons-vue'

const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const hotProducts = ref([])
const specialRecommendations = ref([])
const flashSales = ref([])
const farmerStories = ref([])
const nowTs = ref(Date.now())

let timer = null

const valueCards = [
  {
    title: '茶农直供',
    desc: '从利川茶山直连消费者，减少中间环节，让每一份好茶都更新鲜、更真实。',
    icon: Goods
  },
  {
    title: '真实可溯源',
    desc: '产地、农户、故事清晰可见，让你看得见来源、读得懂品质、买得更安心。',
    icon: Location
  },
  {
    title: '助农有温度',
    desc: '每一次下单，都是对茶农收入和乡村产业的支持，让一杯茶承载更多价值与心意。',
    icon: CircleCheck
  }
]

const bannerImages = ['/images/banner/banner1.jpg', '/images/banner/banner2.png', '/images/banner/banner3.png', '/images/banner/banner4.jpg']

const parseTimestamp = (timeString) => {
  if (!timeString) return 0
  const normalized = timeString.includes('T') ? timeString : timeString.replace(' ', 'T')
  const ts = new Date(normalized).getTime()
  return Number.isNaN(ts) ? 0 : ts
}

const formatCountdown = (endTime) => {
  const remain = parseTimestamp(endTime) - nowTs.value
  if (remain <= 0) {
    return '已结束'
  }

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

const normalizeProduct = (product) => {
  if (!product) return null
  return {
    ...product,
    id: product.id || product.productId,
    coverImg: product.coverImg || '/images/products/product1.jpg',
    origin: product.origin || '利川',
    farmerName: product.farmerName || product.farmer?.realName || '助农合作社',
    sales: product.sales != null ? product.sales : 0
  }
}

const normalizeStory = (story) => {
  if (!story) return null
  return {
    ...story,
    id: story.id,
    farmerName: story.farmerName || '茶农',
    region: story.region || '利川',
    imageUrl: story.imageUrl || '/images/farmers/farmer1.1.jpg',
    tagline: story.tagline || '扎根茶山，守护每一片茶叶的品质',
    summary: story.summary || '他们在山间四季劳作，只为把真实、安心的利川红茶送到你手中。'
  }
}

const getFlashHighlight = (item) => {
  const product = item?.product
  if (!product) {
    return '当季鲜采，茶香浓郁，入口甘润回甜。'
  }

  const text = product.story || product.description || product.summary || product.tagline
  if (text) {
    return String(text).replace(/\s+/g, ' ').trim()
  }

  const origin = product.origin || '利川'
  const farmerName = product.farmerName || '合作茶农'
  return `${origin}茶山直供，${farmerName}手作精选，口感醇厚回甘。`
}

const getProductTeaser = (product) => {
  if (!product) return '来自利川茶山的一手茶品，香气清扬，回甘自然。'
  const text = product.story || product.description || product.summary || product.tagline
  if (text) {
    return String(text).replace(/\s+/g, ' ').trim()
  }
  return `${product.origin || '利川'}高山茶园采摘，${product.farmerName || '合作茶农'}直供，茶汤清润回甘。`
}

const activeFlashSales = computed(() => {
  return flashSales.value.filter((item) => parseTimestamp(item.endTime) > nowTs.value)
})

const fetchHotProducts = async () => {
  try {
    const data = await api.get('/products')
    hotProducts.value = data
      .map((item) => normalizeProduct(item))
      .filter((item) => item && item.id)
      .sort((a, b) => (b.sales || 0) - (a.sales || 0))
  } catch (e) {
    hotProducts.value = []
  }
}

const fetchSpecialRecommendations = async () => {
  try {
    const data = await api.get('/special-recommendations/active')
    specialRecommendations.value = data
      .map((item) => ({
        ...item,
        product: normalizeProduct(item.product)
      }))
      .filter((item) => item.product && item.product.id)
  } catch (e) {
    specialRecommendations.value = []
  }
}

const fetchFlashSales = async () => {
  try {
    const data = await api.get('/flash-sales/active')
    flashSales.value = data
      .map((item) => ({
        ...item,
        product: normalizeProduct(item.product)
      }))
      .filter((item) => item.product && item.product.id)
  } catch (e) {
    flashSales.value = []
  }
}

const fetchFarmerStories = async () => {
  try {
    const data = await api.get('/farmer-stories/active')
    farmerStories.value = data
      .map((item) => normalizeStory(item))
      .filter((item) => item && item.id)
  } catch (e) {
    farmerStories.value = []
  }
}

const fetchHomeData = async () => {
  await Promise.all([fetchHotProducts(), fetchSpecialRecommendations(), fetchFlashSales(), fetchFarmerStories()])
}

const goToProduct = (id) => {
  router.push({ name: 'ProductDetail', params: { id } })
}

const goToFarmerStoryDetail = (id) => {
  if (!id) return
  router.push({ path: `/farmer-story/${id}` })
}

const handleImageError = (e) => {
  e.target.src = '/images/default-product.jpg'
}

const handleStoryImageError = (e) => {
  e.target.src = '/images/farmers/farmer1.1.jpg'
}

const scrollToSection = (id) => {
  const el = document.getElementById(id)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth' })
  }
}

const addToCart = async (product) => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    await api.post(`/cart/add?userId=${userStore.user.id}`, {
      productId: product.id,
      productName: product.name,
      spec: '默认规格',
      price: product.price,
      quantity: 1,
      coverImg: product.coverImg
    })

    cartStore.addToCart(
      {
        productId: product.id,
        name: product.name,
        spec: '默认规格',
        price: product.price,
        coverImg: product.coverImg
      },
      1
    )

    ElMessage.success('已加入购物车')
  } catch (e) {
    ElMessage.error(e.message || '加入购物车失败')
  }
}

onMounted(async () => {
  await fetchHomeData()
  timer = setInterval(() => {
    nowTs.value = Date.now()
  }, 1000)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
})
</script>

<style scoped>
.home-container {
  --bg-base: #f6f1e8;
  --bg-light: #fcf8f2;
  --bg-card: #fffdf8;
  --border-soft: #e9decf;
  --card-border: #e6dbc8;
  --card-radius: 18px;
  --card-shadow: 0 14px 30px rgba(84, 62, 39, 0.12);
  --card-shadow-hover: 0 20px 34px rgba(84, 62, 39, 0.18);
  --text-main: #3d2d20;
  --text-sub: #6f5b47;
  --brand-brown: #8b5a2b;
  --brand-green: #4e6a57;
  background: linear-gradient(180deg, #f8f3ea 0%, #f4eee3 100%);
  color: var(--text-main);
  position: relative;
}

.home-container::before {
  content: '';
  position: fixed;
  inset: 0;
  pointer-events: none;
  background:
    radial-gradient(1000px 520px at 15% -8%, rgba(172, 136, 94, 0.14), transparent 60%),
    radial-gradient(840px 420px at 85% 8%, rgba(90, 123, 104, 0.12), transparent 62%),
    linear-gradient(120deg, rgba(139, 90, 43, 0.035) 0%, transparent 35%);
  z-index: 0;
}

.home-container > * {
  position: relative;
  z-index: 1;
}

.banner-carousel {
  margin-bottom: 22px;
}

.banner-carousel :deep(.el-carousel__container) {
  border-radius: 0 0 26px 26px;
  overflow: hidden;
}

.banner-wrapper {
  position: relative;
  height: 100%;
}

.banner-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.banner-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(105deg, rgba(40, 28, 16, 0.58) 0%, rgba(53, 35, 20, 0.34) 52%, rgba(86, 58, 35, 0.26) 100%);
}

.banner-content {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  color: #fff;
  text-align: center;
  width: min(92%, 780px);
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.35);
}

.banner-content h1 {
  font-size: 46px;
  margin-bottom: 12px;
  letter-spacing: 1px;
}

.banner-content p {
  margin: 0;
  font-size: 16px;
  opacity: 0.95;
}

.explore-btn {
  margin-top: 18px;
  background: linear-gradient(90deg, #9a6735 0%, #7d4f28 100%);
  color: #fff;
  border: none;
  border-radius: 999px;
  padding: 10px 24px;
  box-shadow: 0 10px 24px rgba(53, 34, 20, 0.28);
}

.container {
  max-width: 1240px;
  margin: 0 auto;
  padding: 0 24px;
}

.section {
  padding: 62px 0;
  position: relative;
  overflow: hidden;
}

.section > .container {
  position: relative;
  z-index: 1;
}

.intro-section {
  background: linear-gradient(180deg, #fcf9f3 0%, #f9f3ea 100%);
}

.intro-section::after,
.special-section::after,
.all-products-section::after,
.tea-story-section::after {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    radial-gradient(420px 180px at 90% 0%, rgba(191, 163, 126, 0.08), transparent 70%),
    radial-gradient(260px 120px at 8% 100%, rgba(113, 143, 125, 0.07), transparent 70%);
}

.special-section {
  background: linear-gradient(180deg, #fbf7f1 0%, #f9f4ec 100%);
}

.all-products-section {
  background: linear-gradient(180deg, #f8f3ea 0%, #f6efe4 100%);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24px;
}

.section-header > div {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.section-eyebrow {
  display: inline-flex;
  width: fit-content;
  font-size: 12px;
  letter-spacing: 0.8px;
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid transparent;
}

.section-eyebrow-flash {
  color: #9f3518;
  background: #ffe5cd;
  border-color: #f2c59c;
}

.section-eyebrow-special {
  color: #82542b;
  background: #f9ebd8;
  border-color: #e7c8a6;
}

.section-eyebrow-all {
  color: #6f5947;
  background: #f3eadf;
  border-color: #e0d1be;
}

.section-eyebrow-story {
  color: #486149;
  background: #e8efe5;
  border-color: #ccd9c7;
}

.section-title {
  font-size: 31px;
  color: var(--text-main);
  margin: 0;
  font-weight: 700;
  letter-spacing: 0.6px;
  position: relative;
  padding-left: 16px;
  line-height: 1.25;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 5px;
  height: 24px;
  border-radius: 999px;
  background: linear-gradient(180deg, #b7885a 0%, #7f5430 100%);
}

.section-subtitle {
  margin: 0;
  color: #7d6955;
  font-size: 14px;
  letter-spacing: 0.15px;
  line-height: 1.6;
}

.intro-text {
  color: #555;
  line-height: 1.8;
}

.value-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.value-card {
  background: linear-gradient(180deg, #fffdf8 0%, #fbf6ee 100%);
  border: 1px solid var(--card-border);
  border-radius: var(--card-radius);
  padding: 22px 20px;
  box-shadow: var(--card-shadow);
  transition: transform 0.25s, box-shadow 0.25s, border-color 0.25s;
}

.value-card:hover {
  transform: translateY(-5px);
  border-color: #d5b596;
  box-shadow: var(--card-shadow-hover);
}

.value-icon-wrap {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, #f4e8d8 0%, #efe0cd 100%);
  color: var(--brand-brown);
  margin-bottom: 12px;
  border: 1px solid #e7d7c1;
}

.value-icon {
  font-size: 23px;
}

.value-title {
  margin: 0 0 9px;
  font-size: 19px;
  color: var(--text-main);
}

.value-desc {
  margin: 0;
  color: var(--text-sub);
  line-height: 1.75;
  font-size: 14px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}

.product-item {
  border: 1px solid var(--card-border);
  border-radius: var(--card-radius);
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.25s, box-shadow 0.25s, border-color 0.25s;
  background: linear-gradient(180deg, #fffdf9 0%, #faf5ec 100%);
  box-shadow: var(--card-shadow);
}

.product-item:hover {
  transform: translateY(-5px);
  border-color: #d5b99b;
  box-shadow: var(--card-shadow-hover);
}

.p-image {
  aspect-ratio: 5 / 4;
  min-height: 210px;
  overflow: hidden;
  position: relative;
}

.p-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.35s ease;
}

.product-item:hover .p-image img {
  transform: scale(1.04);
}

.p-info {
  padding: 15px 16px 16px;
}

.p-name {
  margin: 0 0 8px;
  font-size: 18px;
  color: #3d2d20;
  font-weight: 600;
  line-height: 1.45;
  min-height: 50px;
}

.p-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 9px;
}

.tag-origin,
.tag-farmer {
  background: #f5ecdf;
  padding: 3px 9px;
  border-radius: 999px;
  font-size: 12px;
  color: #7a6048;
  border: 1px solid #eadbc8;
}

.p-brief {
  margin: 0 0 12px;
  color: #7a6550;
  font-size: 13px;
  line-height: 1.62;
  min-height: 42px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.p-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.p-price {
  color: #a6511f;
  font-size: 25px;
  font-weight: 700;
  line-height: 1;
  letter-spacing: 0.2px;
}

.p-price-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.price-label {
  width: fit-content;
  font-size: 11px;
  color: #8f6236;
  background: #f8e8d4;
  border: 1px solid #ebc9a6;
  border-radius: 999px;
  padding: 2px 8px;
}

.p-sales {
  color: #8f7b67;
  font-size: 12px;
}

.p-actions {
  margin-top: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.quick-btn {
  border: none;
  border-radius: 999px;
  padding: 8px 13px;
  background: linear-gradient(90deg, #9a6735 0%, #7e5028 100%);
  color: #fff;
  cursor: pointer;
  box-shadow: 0 8px 16px rgba(99, 61, 31, 0.24);
  font-size: 12px;
}

.image-quick-btn {
  position: absolute;
  right: 10px;
  bottom: 10px;
  z-index: 2;
  padding: 7px 12px;
  opacity: 0;
  transform: translateY(8px);
  pointer-events: none;
  transition: opacity 0.22s ease, transform 0.22s ease, box-shadow 0.22s ease;
}

.all-item:hover .image-quick-btn,
.all-item:focus-within .image-quick-btn {
  opacity: 1;
  transform: translateY(0);
  pointer-events: auto;
}

.ghost-btn {
  border: 1px solid #d7c1aa;
  border-radius: 999px;
  padding: 7px 13px;
  background: #fff8ef;
  color: #7f5a39;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.25s;
}

.ghost-btn:hover {
  border-color: #b9875a;
  color: #6d4728;
  background: #fff2e3;
}

.special-grid {
  grid-template-columns: repeat(auto-fill, minmax(290px, 1fr));
}

.special-item {
  border: 1px solid #e5c9a9;
  background: linear-gradient(180deg, #fffdf9 0%, #fff6ea 100%);
  position: relative;
}

.special-item:hover {
  border-color: #d7a672;
  box-shadow: 0 22px 34px rgba(114, 75, 42, 0.2);
}

.special-corner {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 2;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  color: #fff;
  background: linear-gradient(90deg, #c58b54 0%, #a66833 100%);
  box-shadow: 0 8px 14px rgba(122, 76, 40, 0.26);
}

.special-item .p-name {
  color: #3c2a19;
}

.all-grid {
  grid-template-columns: repeat(auto-fill, minmax(236px, 1fr));
}

.all-item {
  border-color: #e7ddd0;
  background: linear-gradient(180deg, #fffefb 0%, #f8f3ea 100%);
  box-shadow: 0 10px 22px rgba(92, 71, 47, 0.1);
}

.all-item .p-name {
  font-size: 17px;
}

.all-item .p-brief {
  color: #7f6d5d;
}

.flash-section {
  background: linear-gradient(180deg, #fdf2e6 0%, #fbf4e9 100%);
  border-top: 1px solid #ead4ba;
  border-bottom: 1px solid #ead4ba;
}

.flash-section::after {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    radial-gradient(300px 140px at 8% 22%, rgba(244, 178, 118, 0.16), transparent 68%),
    radial-gradient(220px 120px at 92% 88%, rgba(248, 179, 128, 0.11), transparent 68%);
}

.flash-section .section-title {
  color: #c2410c;
  font-weight: 800;
  letter-spacing: 1px;
}

.flash-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 18px;
}

.flash-hero-card {
  display: grid;
  grid-template-columns: 46% 54%;
  border: 1px solid #e7b687;
  border-radius: 18px;
  overflow: hidden;
  background: linear-gradient(180deg, #fffefb 0%, #fff3e5 100%);
  box-shadow: 0 15px 30px rgba(145, 91, 50, 0.18);
  cursor: pointer;
  transition: transform 0.25s, box-shadow 0.25s;
}

.flash-hero-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 18px 34px rgba(145, 91, 50, 0.2);
}

.flash-hero-image-box {
  position: relative;
  min-height: 252px;
}

.flash-hero-image-box img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.35s ease;
}

.flash-hero-card:hover .flash-hero-image-box img {
  transform: scale(1.04);
}

.flash-hero-info {
  padding: 20px 22px 22px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 7px;
}

.hero-kicker {
  display: flex;
  align-items: center;
  gap: 8px;
}

.hero-kicker-tag {
  font-size: 12px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(90deg, #d84c2f 0%, #ef7a32 100%);
  border-radius: 999px;
  padding: 4px 10px;
  box-shadow: 0 8px 14px rgba(216, 76, 47, 0.22);
}

.hero-kicker-text {
  color: #936f51;
  font-size: 12px;
}

.hero-name {
  margin: 0;
  font-size: 25px;
  line-height: 1.36;
}

.hero-origin {
  margin: 0;
}

.hero-slogan {
  margin: 0;
  color: #6d5846;
  font-size: 13px;
  line-height: 1.65;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.hero-price-row {
  margin: 1px 0 0;
  padding: 0;
  background: transparent;
  border: none;
  border-radius: 0;
  gap: 10px;
}

.hero-price-label {
  color: #9c6137;
  font-size: 12px;
  padding: 3px 8px;
  border-radius: 999px;
  border: 1px solid #efc9a4;
  background: #ffebd8;
  font-weight: 700;
  margin-bottom: 6px;
}

.hero-price-row .flash-price {
  font-size: 34px;
}

.hero-price-row .origin-price {
  margin-bottom: 5px;
}

.hero-countdown {
  margin: 0;
  font-size: 14px;
  padding: 8px 12px;
  background: linear-gradient(90deg, #fff0de 0%, #ffe8d2 100%);
  border: 1px solid #f2c7a0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.hero-countdown-label {
  color: #8b5f3a;
  font-size: 12px;
}

.hero-countdown strong {
  font-size: 15px;
  color: #b7481b;
  letter-spacing: 0.4px;
}

.hero-buy-btn {
  width: fit-content;
  min-width: 138px;
  height: 40px;
  font-size: 14px;
  margin-top: 2px;
  border-radius: 999px;
  padding: 10px 20px;
}

.hero-badge {
  left: 12px;
  top: 12px;
  padding: 5px 12px;
  font-size: 12px;
}

.flash-card {
  background: linear-gradient(180deg, #fffdfa 0%, #fff6ee 100%);
  border: 1px solid #efcaa5;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.25s, box-shadow 0.25s, border-color 0.25s;
  box-shadow: 0 12px 24px rgba(146, 95, 53, 0.12);
}

.flash-card:hover {
  transform: translateY(-6px);
  border-color: #e8ac76;
  box-shadow: 0 20px 34px rgba(146, 95, 53, 0.2);
}

.flash-image-box {
  position: relative;
  height: 200px;
}

.flash-image-box img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.flash-card:hover .flash-image-box img {
  transform: scale(1.03);
}

.flash-badge {
  position: absolute;
  left: 10px;
  top: 10px;
  background: linear-gradient(90deg, #ef4444 0%, #f97316 100%);
  color: #fff;
  padding: 5px 12px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 700;
}

.flash-info {
  padding: 14px;
}

.flash-name {
  margin: 0 0 8px;
  font-size: 17px;
  color: #3d2d20;
  font-weight: 600;
}

.flash-origin {
  margin: 0 0 10px;
  color: #7a634f;
  font-size: 13px;
}

.flash-brief {
  margin: -2px 0 10px;
  color: #85674f;
  font-size: 12px;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.flash-price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 8px;
  padding: 6px 8px;
  background: #fff1e6;
  border: 1px solid #ffd1a8;
  border-radius: 8px;
}

.flash-price {
  color: #ef4444;
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
}

.origin-price {
  color: #999;
  text-decoration: line-through;
  font-size: 14px;
}

.countdown {
  margin-bottom: 10px;
  color: #7c4a20;
  font-size: 13px;
  background: #fff2e2;
  border: 1px solid #efcaa5;
  border-radius: 8px;
  padding: 8px 10px;
  font-weight: 700;
}

.flash-buy-btn {
  width: 100%;
  border: none;
  border-radius: 8px;
  padding: 10px;
  background: linear-gradient(90deg, #ef4444 0%, #f97316 100%);
  color: #fff;
  cursor: pointer;
  font-weight: 700;
  box-shadow: 0 10px 18px rgba(206, 82, 34, 0.25);
}

.tea-story-section {
  background: linear-gradient(180deg, #eef2eb 0%, #edf1e8 100%);
  border-top: 1px solid #dce4d7;
  border-bottom: 1px solid #dce4d7;
}

.story-header {
  align-items: flex-end;
}

.story-subtitle {
  margin: 10px 0 0;
  color: #5d705f;
  font-size: 14px;
  letter-spacing: 0.2px;
}

.story-carousel {
  margin-top: 10px;
}

.story-carousel :deep(.el-carousel__container) {
  height: 420px !important;
}

.story-carousel :deep(.el-carousel__arrow) {
  background: rgba(72, 99, 81, 0.86);
  width: 36px;
  height: 36px;
  top: 48%;
  border: 1px solid rgba(216, 229, 212, 0.5);
  box-shadow: 0 10px 18px rgba(51, 77, 62, 0.3);
}

.story-card {
  height: 100%;
  min-height: 420px;
  display: grid;
  grid-template-columns: 46% 54%;
  gap: 20px;
  border: 1px solid #d8e1d2;
  border-radius: var(--card-radius);
  background: linear-gradient(180deg, #fcfffb 0%, #f3f8ef 100%);
  overflow: visible;
  box-sizing: border-box;
  box-shadow: 0 14px 30px rgba(74, 95, 80, 0.16);
  transition: transform 0.25s, box-shadow 0.25s;
}

.story-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 22px 34px rgba(74, 95, 80, 0.2);
}

.story-image-wrap {
  height: 100%;
  overflow: hidden;
  border-radius: var(--card-radius) 0 0 var(--card-radius);
}

.story-image-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.story-content {
  padding: 20px 22px 20px 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 100%;
  min-height: 0;
  box-sizing: border-box;
}

.story-content-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
  overflow: hidden;
  flex: 1;
  min-height: 0;
}

.story-meta {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 10px;
}

.story-name {
  margin: 0;
  color: #33483a;
  font-size: 24px;
}

.story-region {
  color: #5f745f;
  font-size: 13px;
  background: #e6efe2;
  border-radius: 999px;
  padding: 4px 10px;
  border: 1px solid #d1dfcb;
}

.story-role {
  margin: 0;
  color: #6a7f6b;
  font-size: 12px;
  letter-spacing: 0.2px;
}

.story-tagline {
  margin: 0;
  color: #5c6f5f;
  font-weight: 700;
  line-height: 1.55;
  padding: 10px 12px;
  background: #eef5eb;
  border: 1px solid #d9e5d5;
  border-radius: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.story-summary {
  margin: 0;
  color: #4f6253;
  line-height: 1.75;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.story-chip-group {
  margin-top: 4px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.story-chip {
  font-size: 11px;
  color: #57705a;
  background: #e9f0e7;
  border: 1px solid #d4e1d1;
  border-radius: 999px;
  padding: 3px 9px;
}

.story-content-footer {
  margin-top: 14px;
  min-height: 64px;
  padding-top: 14px;
  padding-bottom: 8px;
  border-top: 1px solid #cfddca;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  flex-shrink: 0;
}

.story-btn {
  border: none;
  background: linear-gradient(90deg, #5d7b65 0%, #45604f 100%);
  color: #fff;
  border-radius: 999px;
  padding: 11px 20px;
  min-width: 168px;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.2px;
  line-height: 1;
  box-shadow: 0 10px 20px rgba(62, 89, 70, 0.22);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.story-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 12px 24px rgba(62, 89, 70, 0.28);
}

.home-footer {
  background: linear-gradient(170deg, #395041 0%, #2f4135 52%, #28372d 100%);
  color: #dce8d8;
  padding: 42px 0 0;
  border-top: 1px solid rgba(180, 201, 173, 0.26);
}

.footer-inner {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr;
  gap: 28px;
  align-items: flex-start;
}

.footer-main {
  max-width: 620px;
}

.footer-main h3 {
  margin: 0 0 10px;
  color: #f6fbf4;
  font-size: 25px;
  letter-spacing: 0.5px;
}

.footer-main p {
  margin: 0;
  line-height: 1.75;
  color: #cadac6;
  font-size: 14px;
  max-width: 92%;
}

.footer-col h4 {
  margin: 0 0 12px;
  font-size: 15px;
  letter-spacing: 0.2px;
  color: #f2faf0;
}

.footer-list {
  margin: 0;
  padding: 0;
  list-style: none;
}

.footer-list li {
  margin: 6px 0 0;
  font-size: 13px;
  color: #c6d6c1;
  line-height: 1.55;
  position: relative;
  padding-left: 12px;
}

.footer-list li::before {
  content: '·';
  position: absolute;
  left: 0;
  top: 0;
  color: #d8e7d4;
}

.footer-bottom {
  margin-top: 28px;
  text-align: center;
  padding: 15px 16px;
  font-size: 12px;
  color: #c3d4bf;
  border-top: 1px solid rgba(197, 217, 192, 0.28);
  background: rgba(22, 34, 27, 0.28);
  letter-spacing: 0.2px;
}

@media (max-width: 768px) {
  .banner-carousel :deep(.el-carousel__container) {
    border-radius: 0 0 18px 18px;
  }

  .banner-content h1 {
    font-size: 30px;
  }

  .banner-content p {
    font-size: 14px;
  }

  .section {
    padding: 42px 0;
  }

  .container {
    padding: 0 16px;
  }

  .section-eyebrow {
    font-size: 11px;
    padding: 3px 8px;
  }

  .section-title {
    font-size: 24px;
  }

  .section-subtitle {
    font-size: 13px;
  }

  .value-grid {
    grid-template-columns: 1fr;
  }

  .p-image {
    min-height: 188px;
  }

  .p-brief {
    min-height: auto;
    -webkit-line-clamp: 3;
  }

  .p-actions {
    margin-top: 8px;
  }

  .image-quick-btn {
    opacity: 1;
    transform: none;
    pointer-events: auto;
  }

  .story-card {
    grid-template-columns: 1fr;
    min-height: 560px;
  }

  .flash-hero-card {
    grid-template-columns: 1fr;
  }

  .flash-hero-image-box {
    min-height: 180px;
  }

  .flash-hero-info {
    padding: 16px;
  }

  .hero-name {
    font-size: 21px;
  }

  .hero-price-row .flash-price {
    font-size: 30px;
  }

  .story-image-wrap {
    height: 210px;
    border-radius: var(--card-radius) var(--card-radius) 0 0;
  }

  .story-content {
    padding: 14px 16px 18px;
  }

  .story-name {
    font-size: 20px;
  }

  .story-summary {
    -webkit-line-clamp: 3;
  }

  .story-content-footer {
    margin-top: 12px;
    min-height: 60px;
    padding-top: 12px;
    padding-bottom: 6px;
  }

  .story-carousel :deep(.el-carousel__container) {
    height: 560px !important;
  }

  .footer-inner {
    grid-template-columns: 1fr;
    gap: 20px;
  }
}

@media (min-width: 769px) and (max-width: 1024px) {
  .section {
    padding: 52px 0;
  }

  .value-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .flash-hero-card {
    grid-template-columns: 48% 52%;
  }

  .flash-hero-image-box {
    min-height: 230px;
  }

  .footer-inner {
    grid-template-columns: 1.6fr 1fr 1fr;
    gap: 22px;
  }
}
</style>


