<template>
  <div class="home-container">
    <!-- 1. Banner (全屏人文关怀) -->
    <el-carousel height="90vh" :interval="5000" arrow="hover" :autoplay="true" indicator-position="none" class="banner-carousel">
      <el-carousel-item v-for="(img, index) in bannerImages" :key="index">
        <div class="banner-wrapper">
          <div class="banner-bg">
            <img :src="img" alt="利川红茶" class="banner-img" />
            <div class="banner-overlay"></div>
          </div>
          <div class="banner-content">
            <h1 class="animate-title">利川红茶 · 茶农用心种出的好茶</h1>
            <p class="animate-subtitle">高山云雾中长出的宝贝，直连茶农与您</p>
            <el-button class="explore-btn" @click="scrollToSection('products')">
              去尝尝
              <i class="el-icon-right"></i>
            </el-button>
          </div>
          <div class="scroll-indicator" @click="scrollToSection('feature')">
            <span>往下看</span>
            <div class="arrow-down">↓</div>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>

    <!-- 2. 助农模块 (交错式布局) -->
    <section class="section feature-section" id="feature">
      <div class="container">
        <div class="staggered-layout">
          <div class="staggered-image-box" data-scroll>
             <img src="/images/banner/banner2.png" alt="助农采茶" />
             <div class="accent-block"></div>
          </div>
          <div class="staggered-text-box" data-scroll>
             <span class="sub-header">HELP FARMERS</span>
             <h2 class="section-title">助农直供 · 手心里的温度</h2>
             <p class="desc-text">
               我们直接与利川毛坝、忠路等茶农合作，每一叶茶都来自茶农王大伯、李阿姨的双手。
               这里没有中间商，只有最朴实的茶农和最地道的好茶。
               买茶就是帮茶农增收，您的每一次支持，都能让大山里的笑容更灿烂一点。
             </p>
             <div class="tags-group">
                <span class="tag">#茶农直供</span>
                <span class="tag">#爱心助农</span>
             </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 3. 冷后浑科普 (朴实科普) -->
    <section class="section science-section">
      <div class="container">
         <div class="science-window">
            <div class="microscope-view">
               <img src="/images/common/lenhouhun.jpg" alt="冷后浑茶汤" />
               <div class="label-tag">真实茶汤实拍</div>
            </div>
            <div class="data-panel">
               <h2 class="section-title">冷后浑 · 大山的礼物</h2>
               <p class="science-desc">
                 茶凉了汤色还红亮、香气更浓，这就是利川红的“冷后浑”。
                 茶农说，这是好茶才有的标志，是高山云雾给我们的馈赠。
                 不用复杂的化学名词，只看这一杯琥珀色的茶汤，就知道是好东西。
               </p>
               <div class="data-grid">
                  <div class="data-item">
                     <span class="value">纯天然</span>
                     <span class="label">无添加</span>
                  </div>
                  <div class="data-item">
                     <span class="value">高海拔</span>
                     <span class="label">云雾茶</span>
                  </div>
               </div>
            </div>
         </div>
      </div>
    </section>

    <!-- 4. 热门推荐 (主流电商网格) -->
    <section class="section products-section" id="products">
      <div class="container">
        <div class="section-header">
          <span class="sub-header">HOT SALE</span>
          <h2 class="section-title">大家都在喝的好茶</h2>
        </div>
        
        <div class="product-grid">
          <div class="product-item" v-for="p in products" :key="p.id" @click="$router.push({ name: 'ProductDetail', params: { id: p.id } })">
             <div class="p-image">
                <img :src="p.coverImg" loading="lazy" @error="handleImageError" />
                <div class="quick-buy-overlay">
                   <button class="quick-btn" @click.stop="addToCart(p)" :disabled="p.stock <= 0">
                     {{ p.stock > 0 ? '加入购物车' : '卖光了' }}
                   </button>
                </div>
             </div>
             <div class="p-info">
                <h3 class="p-name">{{ p.name }}</h3>
                <div class="p-tags">
                   <span class="tag-origin">{{ p.origin }}</span>
                   <span class="tag-farmer">{{ p.farmerName }}家</span>
                </div>
                <div class="p-meta">
                   <span class="p-price">¥{{ p.price }}</span>
                   <span class="p-sales">已售 {{ p.sales }}</span>
                </div>
             </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 5. 农户故事 (沉浸式温暖) -->
    <section class="section farmers-section">
      <div class="section-header text-center">
        <h2 class="section-title">听听茶农心里话</h2>
      </div>
      <el-carousel :interval="6000" height="500px" indicator-position="outside" arrow="hover">
         <el-carousel-item v-for="(farmer, index) in farmers" :key="index">
            <div class="farmer-immersive" :style="{ backgroundImage: `url(${farmer.image})` }">
               <div class="farmer-overlay"></div>
               <div class="farmer-bubble">
                  <div class="bubble-content">
                     <div class="quote-icon">“</div>
                     <p class="story-text">{{ farmer.story }}</p>
                     <div class="farmer-sign">
                        <span class="name">{{ farmer.name }}</span>
                        <span class="loc">{{ farmer.location }}</span>
                     </div>
                  </div>
               </div>
            </div>
         </el-carousel-item>
      </el-carousel>
    </section>

    <!-- 6. 页脚 -->
    <footer class="home-footer">
      <div class="container">
        <div class="footer-content">
           <div class="brand-col">
              <h3>利川红茶助农平台</h3>
              <p>传承当地文化，温暖每一位茶客</p>
           </div>
           <div class="link-col">
              <h4>欢迎来做客</h4>
              <p>地址：湖北省恩施州利川市</p>
              <p>电话：0718-1234567</p>
           </div>
           <div class="social-col">
              <span>微信公众号</span>
              <span>抖音号</span>
           </div>
        </div>
        <div class="footer-copy">
          <p>© 2026 利川红茶助农平台 | 买好茶，助茶农</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '../stores/cart'
import { useUserStore } from '../stores/user'
import api from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()
const products = ref([])

// Banner Images (真实高清图)
const bannerImages = [
  '/images/banner/banner1.jpg', // Tea Garden
  '/images/banner/banner2.png', // Picking Tea
  '/images/banner/banner3.png', // Tea Leaves
  '/images/banner/banner4.jpg'  // Tea Processing
]

// Mock Farmers
const farmers = [
  { name: '王大伯', location: '利川市毛坝镇', story: '种茶30年，天不亮就上山，只为采最新鲜的芽。看着大家喜欢喝我种的茶，心里比吃蜜还甜。', image: '/images/farmers/farmer1.1.jpg' },
  { name: '李大姐', location: '利川市忠路镇', story: '手工炒茶，手艺传自奶奶，一锅一锅用心炒。虽然手粗了，但茶香了，这就值了。', image: '/images/farmers/farmer2.1.jpg' },
  { name: '张叔', location: '利川市柏杨坝', story: '感谢平台帮我们卖茶，以前愁销路，现在愁茶不够卖。欢迎大家来利川玩，我请大家喝新茶！', image: '/images/farmers/farmer3.1.jpg' },
  { name: '刘爷爷', location: '利川市沙溪乡', story: '做了一辈子的发酵茶，这手艺不能丢。看到年轻人也爱喝红茶，我觉得这手艺能传下去。', image: '/images/farmers/farmer4.1.jpg' },
  { name: '陈大姐', location: '利川市文斗乡', story: '我们坚持有机种植，不打农药，虽然产量低点，但大家喝着放心，我也安心。', image: '/images/farmers/farmer5.1.jpg' },
  { name: '赵小哥', location: '利川市团堡镇', story: '大学毕业回乡创业，想用新方法帮乡亲们把茶叶卖出去。利川红，值得被更多人知道。', image: '/images/farmers/farmer6.1.jpg' }
]

// Mock Products for fallback
const mockProducts = [
  { id: 101, name: '利川红·冷后浑（特级）', price: 268.00, stock: 900, sales: 120, coverImg: '/images/products/product2.jpg', origin: '利川毛坝', farmerName: '王大伯' },
  { id: 102, name: '利川红·玛瑙红（袋泡）', price: 58.00, stock: 800, sales: 450, coverImg: '/images/products/product11.1.jpg', origin: '利川沙溪', farmerName: '李阿姨' },
  { id: 103, name: '恩施玉露·利川红礼盒', price: 388.00, stock: 950, sales: 50, coverImg: '/images/products/product6.jpg', origin: '利川忠路', farmerName: '张大姐' },
  { id: 104, name: '利川工夫红茶（经典罐）', price: 188.00, stock: 1200, sales: 200, coverImg: '/images/products/product3.jpg', origin: '利川文斗', farmerName: '刘叔' },
  { id: 105, name: '高山有机利川红', price: 398.00, stock: 50, sales: 950, coverImg: '/images/products/product5.jpg', origin: '利川柏杨', farmerName: '陈伯' },
  { id: 106, name: '冷后浑精品装', price: 498.00, stock: 20, sales: 980, coverImg: '/images/products/product13.1.jpg', origin: '利川毛坝', farmerName: '王大伯' },
  { id: 107, name: '家庭分享装', price: 168.00, stock: 2000, sales: 300, coverImg: '/images/products/product7.1.jpg', origin: '利川团堡', farmerName: '赵婶' },
  { id: 108, name: '礼盒双罐装', price: 588.00, stock: 100, sales: 900, coverImg: '/images/products/product12.jpg', origin: '利川忠路', farmerName: '周哥' },
]

onMounted(async () => {
  try {
    const apiProducts = await api.get('/products')
    // Ensure API products have 'id' and other required fields
    const mappedApiProducts = apiProducts.map(p => ({
      ...p,
      id: p.id || p.productId, // Handle potential field mismatch
      coverImg: p.coverImg || '/images/products/product1.jpg', // Fallback image
      origin: p.origin || '利川',
      farmerName: p.farmerName || '助农合作社',
      sales: p.sales != null ? p.sales : 0 // Use real sales from backend, default to 0
    }))
    
    // 合并 API 数据和 mock 数据，优先使用 API 数据，并根据 id 去重
    const allProducts = [...mappedApiProducts, ...mockProducts]
    const uniqueProducts = Array.from(new Map(allProducts.map(item => [item.id, item])).values())
    
    products.value = uniqueProducts
  } catch (e) {
    products.value = mockProducts
  }
})

const scrollToSection = (id) => {
  const el = document.getElementById(id)
  if (el) el.scrollIntoView({ behavior: 'smooth' })
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
/* 
  Style Guide: Warm, Earthy, Human-centric
  Colors: Beige (#FFF8F0), Russet (#8B4513), Warm Grey (#5A4A42)
*/

.home-container {
  background-color: #FFF8F0;
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
  color: #5A4A42;
  overflow-x: hidden;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.section {
  padding: 80px 0;
}

/* Typography */
h1, h2, h3 {
  font-weight: 700;
  color: #3E2B26;
}

.section-title {
  font-size: 32px;
  margin-bottom: 20px;
  color: #8B4513;
  text-align: left;
}

.sub-header {
  display: block;
  font-size: 14px;
  letter-spacing: 1px;
  color: #A08060;
  margin-bottom: 5px;
  font-weight: 600;
}

.desc-text {
  font-size: 16px;
  line-height: 1.8;
  color: #666;
  margin-bottom: 25px;
}

/* Banner */
.banner-wrapper {
  height: 100%;
  position: relative;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
}

.banner-bg {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
}

.banner-img {
  width: 100%; height: 100%; object-fit: cover;
  animation: slowZoom 20s infinite alternate;
}

.banner-overlay {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0,0,0,0.3);
}

.banner-content {
  position: relative;
  z-index: 2;
  text-align: center;
  color: #fff;
  padding: 0 20px;
}

.animate-title {
  font-size: 48px;
  margin-bottom: 20px;
  text-shadow: 0 2px 10px rgba(0,0,0,0.3);
}

.animate-subtitle {
  font-size: 20px;
  margin-bottom: 40px;
  font-weight: 300;
  opacity: 0.9;
}

.explore-btn {
  background: #8B4513;
  color: #fff;
  border: none;
  padding: 15px 40px;
  border-radius: 30px;
  font-size: 18px;
  transition: all 0.3s;
}

.explore-btn:hover {
  background: #A0522D;
  transform: translateY(-2px);
}

.scroll-indicator {
  position: absolute;
  bottom: 30px;
  left: 50%;
  transform: translateX(-50%);
  color: #fff;
  text-align: center;
  cursor: pointer;
  z-index: 2;
  opacity: 0.8;
}

.arrow-down { font-size: 24px; animation: bounce 2s infinite; }

/* Feature Section (Staggered) */
.staggered-layout {
  display: flex;
  align-items: center;
  gap: 60px;
  background: #fff;
  padding: 40px;
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(139, 69, 19, 0.05);
}

.staggered-image-box {
  flex: 1;
  position: relative;
  height: 400px;
  border-radius: 12px;
  overflow: hidden;
}

.staggered-image-box img {
  width: 100%; height: 100%; object-fit: cover;
}

.staggered-text-box {
  flex: 1;
}

.tags-group .tag {
  display: inline-block;
  margin-right: 15px;
  font-size: 14px;
  color: #8B4513;
  background: #FFF0E0;
  padding: 5px 12px;
  border-radius: 15px;
}

/* Science Section */
.science-window {
  display: flex;
  background: #fff;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(139, 69, 19, 0.05);
}

.microscope-view {
  flex: 1;
  position: relative;
  height: 400px;
}

.microscope-view img {
  width: 100%; height: 100%; object-fit: cover;
}

.label-tag {
  position: absolute;
  bottom: 20px;
  left: 20px;
  background: rgba(0,0,0,0.6);
  color: #fff;
  padding: 5px 15px;
  border-radius: 20px;
  font-size: 12px;
}

.data-panel {
  flex: 1;
  padding: 60px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: #FAF5F0;
}

.science-desc {
  font-size: 16px;
  line-height: 1.8;
  color: #666;
  margin-bottom: 30px;
}

.data-grid {
  display: flex;
  gap: 40px;
}

.data-item {
  text-align: center;
}

.data-item .value {
  display: block;
  font-size: 24px;
  color: #8B4513;
  font-weight: 700;
  margin-bottom: 5px;
}

.data-item .label {
  font-size: 14px;
  color: #999;
}

/* Product Grid */
.products-section {
  background: #FFF8F0;
}

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

.quick-buy-overlay {
  position: absolute;
  bottom: 0; left: 0; width: 100%;
  padding: 15px;
  background: linear-gradient(to top, rgba(0,0,0,0.5), transparent);
  opacity: 0;
  transition: opacity 0.3s;
  display: flex;
  justify-content: center;
}

.product-item:hover .quick-buy-overlay {
  opacity: 1;
}

.quick-btn {
  background: #8B4513;
  color: #fff;
  border: none;
  padding: 8px 20px;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
}

.p-info {
  padding: 20px;
}

.p-name {
  font-size: 16px;
  margin-bottom: 10px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
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
.p-sales { font-size: 12px; color: #aaa; }

/* Farmer Immersive */
.farmers-section .section-header { margin-bottom: 40px; }

.farmer-immersive {
  height: 100%;
  background-size: cover;
  background-position: center;
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
}

.farmer-overlay {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0,0,0,0.4);
}

.farmer-bubble {
  position: relative;
  z-index: 2;
  background: rgba(255, 255, 255, 0.95);
  padding: 40px;
  border-radius: 12px;
  max-width: 600px;
  text-align: center;
  box-shadow: 0 20px 50px rgba(0,0,0,0.2);
}

.quote-icon {
  font-size: 48px;
  color: #8B4513;
  line-height: 1;
  margin-bottom: 15px;
  font-family: serif;
}

.story-text {
  font-size: 18px;
  line-height: 1.6;
  color: #555;
  margin-bottom: 25px;
  font-style: italic;
}

.farmer-sign .name {
  display: block;
  font-weight: 700;
  font-size: 16px;
  color: #333;
}

.farmer-sign .loc {
  font-size: 12px;
  color: #888;
}

/* Footer */
.home-footer {
  background: #3E2B26;
  color: #D7CCC8;
  padding: 60px 0 30px;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  margin-bottom: 40px;
  flex-wrap: wrap;
  gap: 30px;
}

.brand-col h3 { color: #fff; margin-bottom: 10px; font-size: 20px; }
.link-col h4 { color: #A08060; margin-bottom: 15px; font-size: 14px; text-transform: uppercase; }
.link-col p { margin-bottom: 8px; font-size: 14px; }
.social-col span { display: block; margin-bottom: 10px; cursor: pointer; }
.social-col span:hover { color: #fff; }

.footer-copy {
  border-top: 1px solid rgba(255,255,255,0.1);
  padding-top: 20px;
  text-align: center;
  font-size: 12px;
  opacity: 0.6;
}

@keyframes slowZoom {
  from { transform: scale(1); }
  to { transform: scale(1.1); }
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% { transform: translateY(0); }
  40% { transform: translateY(-10px); }
  60% { transform: translateY(-5px); }
}

@media (max-width: 768px) {
  .staggered-layout { flex-direction: column; padding: 20px; }
  .staggered-image-box { height: 250px; width: 100%; }
  .science-window { flex-direction: column; }
  .data-panel { padding: 30px; }
  .banner-content h1 { font-size: 32px; }
  .footer-content { flex-direction: column; text-align: center; }
  .link-col, .social-col { text-align: center; }
}
</style>
