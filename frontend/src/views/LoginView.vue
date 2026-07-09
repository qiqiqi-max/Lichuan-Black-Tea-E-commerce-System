<template>
  <div class="login-container">
    <div class="login-overlay"></div>
    <div class="login-content">
      <el-card class="login-card">
        <div class="card-header">
          <h2>欢迎登录 · 利川红茶</h2>
          <p class="subtitle">源自高山 · 匠心制作 · 助农增收</p>
        </div>

        <el-form :model="form" class="login-form" size="large">
          <el-form-item>
            <el-input v-model="form.username" placeholder="请输入用户名/手机号" class="custom-input" />
          </el-form-item>
          <el-form-item>
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              show-password
              class="custom-input"
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" class="login-btn" @click="handleLogin" :loading="loading">登录</el-button>
          </el-form-item>

          <div class="form-footer">
            <el-button link @click="$router.push('/')" style="color: #666">返回首页</el-button>
            <div class="register-link">
              还没有账号？
              <router-link to="/register">立即注册</router-link>
            </div>
          </div>
        </el-form>

        <div class="footer-tip">
          <div class="divider"></div>
          <p class="slogan">地道恩施利川红 · 助农直达消费者</p>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { useCartStore } from '../stores/cart'
import api from '../api'
import { ElMessage } from 'element-plus'

const form = ref({ username: '', password: '' })
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const loading = ref(false)

const handleLogin = async () => {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  loading.value = true
  try {
    const res = await api.post('/login', form.value)
    cartStore.clearCart()
    userStore.login(res.user, res.token)
    ElMessage.success('登录成功')
    if (res.user.role === 'ADMIN' || res.user.role === 'FARMER') {
      router.push('/admin')
    } else {
      router.push('/')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  width: 100vw;
  background-image: url('/images/banner/banner1.jpg');
  background-size: cover;
  background-position: center;
  overflow: hidden;
}

.login-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(3px);
  z-index: 1;
}

.login-content {
  position: relative;
  z-index: 2;
  animation: fadeIn 0.8s ease-out;
}

.login-card {
  width: 420px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  border: none;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  padding: 10px;
}

.card-header {
  text-align: center;
  margin-bottom: 30px;
  padding-top: 10px;
}

.card-header h2 {
  font-size: 26px;
  color: #333;
  margin: 0 0 10px 0;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  font-weight: 600;
}

.subtitle {
  color: #8B4513;
  font-size: 14px;
  letter-spacing: 2px;
  margin: 0;
  font-weight: 500;
}

.login-form {
  padding: 0 20px;
}

.custom-input :deep(.el-input__wrapper) {
  border-radius: 20px;
  padding-left: 15px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #d2691e inset !important;
}

.login-btn {
  width: 100%;
  border-radius: 20px;
  height: 40px;
  font-size: 16px;
  font-weight: bold;
  letter-spacing: 4px;
  background: linear-gradient(to right, #8b4513, #d2691e);
  border: none;
  margin-top: 10px;
  transition: all 0.3s;
}

.login-btn:hover {
  opacity: 0.9;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(210, 105, 30, 0.3);
}

.form-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 5px;
  font-size: 14px;
}

.register-link {
  color: #666;
}

.register-link a {
  color: #d2691e;
  text-decoration: none;
  font-weight: bold;
  margin-left: 5px;
}

.register-link a:hover {
  text-decoration: underline;
}

.footer-tip {
  margin-top: 30px;
  text-align: center;
}

.divider {
  height: 1px;
  background: linear-gradient(to right, transparent, #eee, transparent);
  margin-bottom: 15px;
}

.slogan {
  color: #8b4513;
  font-size: 13px;
  letter-spacing: 1px;
}
</style>
