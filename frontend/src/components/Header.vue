<template>
  <header class="app-header" :class="{ 'is-scrolled': isScrolled }">
    <div class="header-container">
      <div class="brand" @click="$router.push('/')">
        <svg class="brand-icon" viewBox="0 0 24 24" width="32" height="32" fill="none" stroke="#8B4513" stroke-width="2">
          <path d="M18 8h1a4 4 0 0 1 0 8h-1M2 8h16v9a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V8z" />
          <line x1="6" y1="1" x2="6" y2="4" />
          <line x1="10" y1="1" x2="10" y2="4" />
          <line x1="14" y1="1" x2="14" y2="4" />
        </svg>
        <h1 class="brand-name">利川红茶助农平台</h1>
      </div>

      <nav class="nav-actions">
        <div class="search-bar">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索冷后浑、高山红茶、毛坝产地等"
            class="search-input"
            @keyup.enter="handleSearch"
            clearable
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearch" class="search-btn" />
            </template>
          </el-input>
        </div>

        <router-link to="/" class="nav-link" active-class="active">首页</router-link>

        <div class="nav-item cart-item" @click="$router.push('/cart')">
          <div class="icon-wrapper">
            <svg viewBox="0 0 24 24" width="22" height="22" stroke="currentColor" stroke-width="2" fill="none">
              <circle cx="9" cy="21" r="1"></circle>
              <circle cx="20" cy="21" r="1"></circle>
              <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path>
            </svg>
            <span v-if="cartStore.totalCount > 0" class="badge">{{ cartStore.totalCount }}</span>
          </div>
          <span class="nav-text">购物车</span>
        </div>

        <template v-if="userStore.token">
          <router-link to="/orders" class="nav-link" active-class="active">我的订单</router-link>

          <el-dropdown trigger="hover" class="user-dropdown">
            <div class="user-profile">
              <el-avatar :size="36" :src="userStore.user.avatar || '/images/common/头像.jpg'" />
              <span class="username">{{ userStore.user.username || userStore.user.nickname }}</span>
              <el-icon><CaretBottom /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/orders')">我的订单</el-dropdown-item>
                <el-dropdown-item
                  v-if="userStore.user.role === 'ADMIN' || userStore.user.role === 'FARMER'"
                  @click="$router.push('/admin')"
                >
                  {{ userStore.user.role === 'ADMIN' ? '进入总后台' : '进入茶园工作台' }}
                </el-dropdown-item>
                <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>

        <template v-else>
          <div class="auth-buttons">
            <el-button type="primary" class="login-btn" @click="$router.push('/login')">登录</el-button>
            <router-link to="/register" class="register-link">注册</router-link>
          </div>
        </template>
      </nav>

      <div class="mobile-toggle" @click="mobileMenuOpen = !mobileMenuOpen">
        <svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" stroke-width="2" fill="none">
          <line x1="3" y1="12" x2="21" y2="12"></line>
          <line x1="3" y1="6" x2="21" y2="6"></line>
          <line x1="3" y1="18" x2="21" y2="18"></line>
        </svg>
      </div>
    </div>

    <el-drawer v-model="mobileMenuOpen" direction="rtl" size="70%" :with-header="false">
      <div class="mobile-menu">
        <div class="mobile-brand">利川红茶</div>
        <router-link to="/" class="mobile-link" @click="mobileMenuOpen = false">首页</router-link>
        <router-link to="/cart" class="mobile-link" @click="mobileMenuOpen = false">
          购物车<span v-if="cartStore.totalCount">（{{ cartStore.totalCount }}）</span>
        </router-link>
        <template v-if="userStore.token">
          <router-link to="/orders" class="mobile-link" @click="mobileMenuOpen = false">我的订单</router-link>
          <div class="mobile-link logout" @click="logout(); mobileMenuOpen = false">退出登录</div>
        </template>
        <template v-else>
          <router-link to="/login" class="mobile-link" @click="mobileMenuOpen = false">登录</router-link>
          <router-link to="/register" class="mobile-link" @click="mobileMenuOpen = false">注册</router-link>
        </template>
      </div>
    </el-drawer>
  </header>
  <div class="header-spacer"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '../stores/user'
import { useCartStore } from '../stores/cart'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CaretBottom, Search } from '@element-plus/icons-vue'

const userStore = useUserStore()
const cartStore = useCartStore()
const router = useRouter()
const isScrolled = ref(false)
const mobileMenuOpen = ref(false)
const searchKeyword = ref('')

const handleSearch = () => {
  if (!searchKeyword.value.trim()) return
  router.push({ path: '/search', query: { keyword: searchKeyword.value } })
}

const handleScroll = () => {
  isScrolled.value = window.scrollY > 50
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

const logout = () => {
  cartStore.clearCart()
  userStore.logout()
  ElMessage.success('退出成功')
  router.push('/login')
}
</script>

<style scoped>
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 70px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  z-index: 1000;
  transition: all 0.3s ease;
}

.app-header.is-scrolled {
  height: 60px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.brand-name {
  font-family: "Noto Serif SC", serif;
  font-size: 22px;
  font-weight: 700;
  color: #8B4513;
  margin: 0;
  white-space: nowrap;
}

.search-bar {
  margin-right: 20px;
}

.search-input {
  width: 300px;
}

:deep(.el-input-group__append) {
  background-color: #8B4513 !important;
  border-color: #8B4513 !important;
  color: white !important;
  box-shadow: none !important;
}

:deep(.el-input-group__append:hover) {
  background-color: #A0522D !important;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 30px;
}

.nav-link {
  text-decoration: none;
  color: #5A4A42;
  font-size: 15px;
  font-weight: 500;
  transition: color 0.3s;
}

.nav-link:hover,
.nav-link.active {
  color: #8B4513;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
  color: #5A4A42;
  transition: color 0.3s;
}

.nav-item:hover {
  color: #8B4513;
}

.cart-item .icon-wrapper {
  position: relative;
}

.badge {
  position: absolute;
  top: -5px;
  right: -5px;
  background: #c0392b;
  color: white;
  font-size: 10px;
  min-width: 16px;
  height: 16px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 10px;
  border-radius: 20px;
  transition: background 0.3s;
}

.user-profile:hover {
  background: #f5f5f5;
}

.username {
  font-size: 14px;
  color: #333;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.auth-buttons {
  display: flex;
  align-items: center;
  gap: 15px;
}

.login-btn {
  background: #8B4513;
  border-color: #8B4513;
  border-radius: 20px;
  padding: 8px 24px;
  font-weight: 600;
}

.login-btn:hover {
  background: #A0522D;
  border-color: #A0522D;
}

.register-link {
  text-decoration: none;
  color: #666;
  font-size: 14px;
}

.register-link:hover {
  color: #8B4513;
}

.mobile-toggle {
  display: none;
  cursor: pointer;
  color: #333;
}

.header-spacer {
  height: 70px;
}

@media (max-width: 768px) {
  .nav-actions {
    display: none;
  }

  .mobile-toggle {
    display: block;
  }

  .mobile-menu {
    display: flex;
    flex-direction: column;
    gap: 20px;
    padding: 20px;
  }

  .mobile-brand {
    font-size: 20px;
    font-weight: bold;
    color: #8B4513;
    margin-bottom: 20px;
  }

  .mobile-link {
    font-size: 16px;
    color: #333;
    text-decoration: none;
    padding: 10px 0;
    border-bottom: 1px solid #eee;
  }

  .logout {
    color: #c0392b;
  }
}
</style>
