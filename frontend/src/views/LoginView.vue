<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>欢迎登录</h2>
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin">登录</el-button>
          <el-button @click="$router.push('/')">返回首页</el-button>
        </el-form-item>
      </el-form>
      <div style="margin-top: 10px; color: #666; font-size: 12px;">
        测试账号: admin / 123456 (管理员), user / 123456 (用户)
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import api from '../api'
import { ElMessage } from 'element-plus'

const form = ref({ username: '', password: '' })
const router = useRouter()
const userStore = useUserStore()

const handleLogin = async () => {
  try {
    const res = await api.post('/login', form.value)
    userStore.login(res.user, res.token)
    ElMessage.success('登录成功')
    if (res.user.role === 'ADMIN') {
      router.push('/admin')
    } else {
      router.push('/')
    }
  } catch (e) {
    // handled by interceptor
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f7fa;
}
.login-card {
  width: 400px;
}
</style>
