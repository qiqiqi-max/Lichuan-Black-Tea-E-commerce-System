<template>
  <el-container class="admin-layout">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="aside">
      <div class="logo-box">
        <span class="logo-icon" v-if="isCollapse">T</span>
        <span class="logo-text" v-else>{{ userStore.role === 'FARMER' ? '我的茶园工作台' : '利川红茶总后台' }}</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="admin-menu"
        :collapse="isCollapse"
        background-color="#2D4035"
        text-color="#E5DED1"
        active-text-color="#B08D57"
        router
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataLine /></el-icon>
          <template #title>{{ userStore.role === 'FARMER' ? '我的仪表盘' : '仪表盘' }}</template>
        </el-menu-item>
        <el-menu-item index="/admin/products">
          <el-icon><Goods /></el-icon>
          <template #title>{{ userStore.role === 'FARMER' ? '我的商品' : '商品管理' }}</template>
        </el-menu-item>
        <el-menu-item index="/admin/orders">
          <el-icon><List /></el-icon>
          <template #title>{{ userStore.role === 'FARMER' ? '订单发货' : '订单管理' }}</template>
        </el-menu-item>
        <el-menu-item index="/admin/after-sales">
          <el-icon><Service /></el-icon>
          <template #title>{{ userStore.role === 'FARMER' ? '售后处理' : '售后管理' }}</template>
        </el-menu-item>
        <el-menu-item index="/admin/special-recommendations" v-if="userStore.role === 'ADMIN'">
          <el-icon><Star /></el-icon>
          <template #title>特别推荐管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/flash-sales" v-if="userStore.role === 'ADMIN'">
          <el-icon><Timer /></el-icon>
          <template #title>限时特价秒杀</template>
        </el-menu-item>
        <el-menu-item index="/admin/farmer-stories" v-if="userStore.role === 'ADMIN'">
          <el-icon><List /></el-icon>
          <template #title>茶农故事管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/users" v-if="userStore.role === 'ADMIN'">
          <el-icon><User /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon class="fold-btn" @click="toggleCollapse">
            <component :is="isCollapse ? 'Expand' : 'Fold'" />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentRouteName }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <span class="welcome-text">欢迎回来，{{ displayName }}</span>
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="avatar-wrapper">
              <el-avatar :size="32" :src="userStore.user.avatar || '/images/common/头像.jpg'" />
              <el-icon><CaretBottom /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="home">返回商城</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { useCartStore } from '../../stores/cart'
import { DataLine, Goods, List, Service, User, Fold, Expand, CaretBottom, Star, Timer } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const isCollapse = ref(false)

const activeMenu = computed(() => route.path)
const currentRouteName = computed(() => route.meta.title || '管理')
const displayName = computed(() => userStore.user?.username || userStore.user?.nickname || '')

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const handleCommand = (cmd) => {
  if (cmd === 'logout') {
    cartStore.clearCart()
    userStore.logout()
    router.push('/login')
    ElMessage.success('已退出登录')
  } else if (cmd === 'home') {
    router.push('/')
  }
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.aside {
  background-color: #2D4035;
  transition: width 0.3s;
  overflow-x: hidden;
}

.logo-box {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-icon {
  font-size: 24px;
}

.admin-menu {
  border-right: none;
}

.header {
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.fold-btn {
  font-size: 20px;
  cursor: pointer;
  color: #666;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.welcome-text {
  font-size: 14px;
  color: #666;
}

.avatar-wrapper {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
}

.main {
  background-color: #f0f2f5;
  padding: 20px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
