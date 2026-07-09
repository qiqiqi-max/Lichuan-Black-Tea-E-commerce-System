<template>
  <div class="story-detail-page">
    <div class="container">
      <div class="top-bar">
        <button type="button" class="back-btn" @click="goBack">返回首页</button>
      </div>

      <el-skeleton v-if="loading" :rows="8" animated />

      <el-empty v-else-if="!story" description="未找到该茶农故事">
        <el-button class="home-btn" @click="goHome">回到首页</el-button>
      </el-empty>

      <article v-else class="story-detail-card">
        <div class="story-cover-wrap">
          <img :src="story.imageUrl" :alt="story.farmerName" @error="handleImageError" />
        </div>
        <div class="story-content">
          <div class="header">
            <h1 class="name">{{ story.farmerName }}</h1>
            <span class="region">{{ story.region }}</span>
          </div>
          <p class="tagline">{{ story.tagline }}</p>
          <p class="summary">{{ story.summary }}</p>
          <div class="divider"></div>
          <div class="full-content">{{ story.content || story.summary }}</div>
        </div>
      </article>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../api'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const story = ref(null)

const normalizeStory = (data) => {
  if (!data) return null
  return {
    id: data.id,
    farmerName: data.farmerName || '茶农',
    region: data.region || '利川',
    imageUrl: data.imageUrl || '/images/farmers/farmer1.1.jpg',
    tagline: data.tagline || '扎根茶山，守护每一片茶叶的品质',
    summary: data.summary || '他们在山间四季劳作，只为把真实、安心的利川红茶送到你手中。',
    content: data.content || ''
  }
}

const fetchDetail = async () => {
  const id = route.params.id
  if (!id) {
    story.value = null
    return
  }

  loading.value = true
  try {
    const data = await api.get(`/farmer-stories/${id}`)
    story.value = normalizeStory(data)
  } catch (e) {
    story.value = null
    ElMessage.error(e.message || '获取茶农故事详情失败')
  } finally {
    loading.value = false
  }
}

const handleImageError = (e) => {
  e.target.src = '/images/farmers/farmer1.1.jpg'
}

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
    return
  }
  goHome()
}

const goHome = () => {
  router.push('/')
}

onMounted(fetchDetail)
watch(() => route.params.id, fetchDetail)
</script>

<style scoped>
.story-detail-page {
  min-height: 100%;
  padding: 36px 0 48px;
  background: linear-gradient(180deg, #f5f1e8 0%, #f0ebdf 100%);
}

.container {
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 20px;
}

.top-bar {
  margin-bottom: 14px;
}

.back-btn,
.home-btn {
  border: none;
  border-radius: 999px;
  padding: 10px 18px;
  background: linear-gradient(90deg, #607f68 0%, #45604f 100%);
  color: #fff;
  cursor: pointer;
}

.story-detail-card {
  border-radius: 18px;
  overflow: hidden;
  background: linear-gradient(180deg, #fcfffb 0%, #f4f8ef 100%);
  border: 1px solid #d8e1d2;
  box-shadow: 0 14px 30px rgba(74, 95, 80, 0.14);
  display: grid;
  grid-template-columns: 44% 56%;
}

.story-cover-wrap {
  min-height: 420px;
}

.story-cover-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.story-content {
  padding: 26px 28px;
}

.header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
}

.name {
  margin: 0;
  color: #354f3d;
  font-size: 32px;
}

.region {
  font-size: 13px;
  background: #e6efe2;
  border: 1px solid #d1dfcb;
  border-radius: 999px;
  padding: 4px 10px;
  color: #5f745f;
}

.tagline {
  margin: 16px 0 10px;
  font-size: 16px;
  color: #546b57;
  font-weight: 700;
  line-height: 1.7;
}

.summary {
  margin: 0;
  color: #5d705f;
  line-height: 1.8;
  background: #eef5eb;
  border: 1px solid #d9e5d5;
  border-radius: 10px;
  padding: 10px 12px;
}

.divider {
  height: 1px;
  margin: 16px 0;
  background: #d8e1d2;
}

.full-content {
  color: #465a4a;
  line-height: 1.9;
  white-space: pre-line;
}

@media (max-width: 900px) {
  .story-detail-card {
    grid-template-columns: 1fr;
  }

  .story-cover-wrap {
    min-height: 240px;
  }

  .story-content {
    padding: 18px 16px 20px;
  }

  .name {
    font-size: 26px;
  }
}
</style>
