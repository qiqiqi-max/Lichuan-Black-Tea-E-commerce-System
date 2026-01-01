<template>
  <el-container>
    <el-header class="header">
      <div class="logo">利川红茶助农平台</div>
      <el-menu mode="horizontal" router :default-active="$route.path">
        <el-menu-item index="/">首页</el-menu-item>
        <el-menu-item index="/cart">购物车 ({{ cartStore.totalCount }})</el-menu-item>
        <el-menu-item index="/orders" v-if="userStore.token">我的订单</el-menu-item>
        <el-menu-item index="/login" v-if="!userStore.token">登录</el-menu-item>
        <el-sub-menu index="user" v-else>
          <template #title>{{ userStore.user.nickname || userStore.user.username }}</template>
          <el-menu-item @click="logout">退出</el-menu-item>
          <el-menu-item v-if="userStore.user.role === 'ADMIN'" index="/admin">后台管理</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-header>
    <el-main>
      <router-view />
    </el-main>
    <el-footer class="footer">
      © 2026 利川红茶助农项目 - 毕业设计
    </el-footer>
  </el-container>
</template>

<script setup>
import { useUserStore } from '../stores/user'
import { useCartStore } from '../stores/cart'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const cartStore = useCartStore()
const router = useRouter()

const logout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #dcdfe6;
  background: #fff;
}
.logo {
  font-size: 20px;
  font-weight: bold;
  color: #c0392b; /* Tea Red */
}
.footer {
  text-align: center;
  color: #909399;
  padding: 20px;
}
</style>
