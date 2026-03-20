<template>
  <el-container class="layout-container">
    <Header />
    <el-main class="main-content">
      <router-view />
    </el-main>
    <el-footer class="footer">
      © 2026 利川红茶助农项目 - 毕业设计 | 传承非遗 · 助力乡村振兴
    </el-footer>
  </el-container>
</template>

<script setup>
import Header from '../components/Header.vue'
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  padding: 0; /* Remove default padding to let header/hero take full width */
  flex: 1;
}

.footer {
  text-align: center;
  color: #909399;
  padding: 30px 20px;
  background-color: #f9f9f9;
  font-size: 14px;
}
</style>
