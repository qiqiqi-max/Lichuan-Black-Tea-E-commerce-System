<template>
  <el-container class="layout-container">
    <Header />
    <el-main class="main-content">
      <router-view />
    </el-main>
    <el-footer v-if="showLightFooter" class="footer">
      <div class="footer-inner">
        <div class="footer-brand">利川红茶助农平台</div>
        <p class="footer-desc">连接茶山与茶客，让每一份好茶更真实，让每一次消费都更有温度。</p>
        <div class="footer-meta">© 2026 利川红茶助农项目 · 传承非遗 · 助力乡村振兴</div>
      </div>
    </el-footer>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import Header from '../components/Header.vue'

const route = useRoute()
const showLightFooter = computed(() => route.path !== '/')
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  padding: 0;
  flex: 1;
}

.footer {
  height: auto;
  padding: 34px 20px 38px;
  background: linear-gradient(180deg, #efe6d7 0%, #eadfce 100%);
  border-top: 1px solid #dccbb5;
}

.footer-inner {
  max-width: 1240px;
  margin: 0 auto;
  text-align: center;
}

.footer-brand {
  color: #6e4928;
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 0.6px;
}

.footer-desc {
  margin: 10px 0 12px;
  color: #6f5b46;
  font-size: 14px;
  line-height: 1.8;
}

.footer-meta {
  color: #876f54;
  font-size: 13px;
}
</style>
