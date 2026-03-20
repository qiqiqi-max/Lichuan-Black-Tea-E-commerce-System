<template>
  <div class="farmer-layout">
    <el-aside width="200px" class="sidebar">
      <div class="logo">
        <h4>利川红茶 · 农户中心</h4>
      </div>
      <el-menu
        :default-active="$route.path"
        class="farmer-menu"
        router
      >
        <el-menu-item index="/farmer/dashboard">
          <i class="el-icon-odometer"></i>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/farmer/products">
          <i class="el-icon-shopping-bag-1"></i>
          <span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="/farmer/orders">
          <i class="el-icon-document"></i>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/farmer/profile">
          <i class="el-icon-user"></i>
          <span>个人资料</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-main class="main-content">
      <router-view />
    </el-main>
  </div>
</template>

<script setup>
</script>

<style scoped>
.farmer-layout {
  display: flex;
  height: 100vh;
}
.sidebar {
  background-color: #2c3e50;
  color: #fff;
}
.logo {
  padding: 20px;
  text-align: center;
  background-color: #1f2c3a;
}
.farmer-menu {
  border-right: none;
  background-color: #2c3e50;
}
.farmer-menu .el-menu-item {
  color: #bdc3c7;
}
.farmer-menu .el-menu-item.is-active {
  background-color: #3498db !important;
  color: #fff;
}
.main-content {
  padding: 20px;
  background-color: #f4f7f6;
}
</style>
