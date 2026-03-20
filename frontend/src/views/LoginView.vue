<template>
  <div class="login-container">
    <div class="login-overlay"></div>
    <div class="login-content">
      <el-card class="login-card">
        <div class="card-header">
          <h2>欢迎品鉴 · 利川红茶</h2>
          <p class="subtitle">源自高山 · 匠心制作 · 助农增收</p>
        </div>
        
        <el-form :model="form" class="login-form" size="large">
          <el-form-item>
            <el-input 
              v-model="form.username" 
              placeholder="请输入用户名/手机号" 
              class="custom-input"
            />
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
            <el-button type="primary" class="login-btn" @click="handleLogin" :loading="loading">
              登 录
            </el-button>
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
          <div class="test-account">
            测试账号: admin / 123456 (管理员) &nbsp; user / 123456 (用户)
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { login } from '../api/auth'
import { ElMessage } from 'element-plus'

const form = reactive({ username: '', password: '' })
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)

const handleLogin = async () => {
  if (!form.username || !form.password) {
    return ElMessage.warning('请输入用户名和密码')
  }
  loading.value = true
  try {
    const res = await login(form)
    userStore.setUser(res.user, res.token, res.farmerProfile)
    ElMessage.success('登录成功')

    if (res.user.role === 'ADMIN') {
      router.push('/admin')
    } else if (res.user.role === 'FARMER') {
      // Check farmer audit status
      if (res.farmerProfile && res.farmerProfile.auditStatus !== 'APPROVED') {
        ElMessage.info('您的农户身份正在审核中，请耐心等待')
        router.push('/') // Or a dedicated 'pending' page
      } else {
        router.push('/farmer')
      }
    } else {
      router.push('/')
    }
  } catch (e) {
    // Error is handled by the global error handler in api/index.js
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
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4); /* 暗化背景 */
  backdrop-filter: blur(3px); /* 轻微模糊 */
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
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
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
  box-shadow: 0 0 0 1px #D2691E inset !important;
}

.login-btn {
  width: 100%;
  border-radius: 20px;
  height: 40px;
  font-size: 16px;
  font-weight: bold;
  letter-spacing: 4px;
  background: linear-gradient(to right, #8B4513, #D2691E);
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
  color: #D2691E;
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
  color: #8B4513;
  font-size: 13px;
  margin-bottom: 8px;
  font-weight: bold;
}

.test-account {
  font-size: 12px;
  color: #999;
  background: #f5f7fa;
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 响应式调整 */
@media (max-width: 480px) {
  .login-card {
    width: 90%;
    margin: 0 20px;
  }
}
</style>
