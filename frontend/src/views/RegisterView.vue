<template>
  <div class="login-container">
    <div class="login-overlay"></div>
    <div class="login-content">
      <el-card class="login-card">
        <div class="card-header">
          <h2>加入我们 · 品鉴利川红茶</h2>
          <p class="subtitle">源自高山 · 匠心制作 · 助农增收</p>
        </div>
        
        <el-form :model="form" :rules="rules" ref="formRef" class="login-form" size="large">
          <el-form-item prop="username">
            <el-input 
              v-model="form.username" 
              placeholder="请输入用户名" 
              class="custom-input"
            />
          </el-form-item>
          <el-form-item prop="phone">
            <el-input 
              v-model="form.phone" 
              placeholder="请输入手机号"
              class="custom-input"
            />
          </el-form-item>

          <el-form-item label="注册为" prop="role">
            <el-radio-group v-model="form.role" class="role-selector">
              <el-radio-button label="USER">普通用户</el-radio-button>
              <el-radio-button label="FARMER">我是茶农</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-form-item prop="password">
            <el-input 
              v-model="form.password" 
              type="password" 
              placeholder="请输入密码" 
              show-password
              class="custom-input"
            />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input 
              v-model="form.confirmPassword" 
              type="password" 
              placeholder="请再次输入密码" 
              show-password
              class="custom-input"
            />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" class="login-btn" @click="handleRegister" :loading="loading">
              立即注册
            </el-button>
          </el-form-item>
          
          <div class="form-footer">
            <div class="login-link">
              已有账号？
              <router-link to="/login">去登录</router-link>
            </div>
          </div>
        </el-form>
        
        <div class="footer-tip">
          <div class="divider"></div>
          <p class="slogan">地道恩施利川红 · 直连农户与消费者</p>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '../api/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  phone: '',
  password: '',
  confirmPassword: '',
  role: 'USER' // Default role
})

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validatePass2, trigger: 'blur' }
  ]
}

const handleRegister = () => {
  if (!formRef.value) return
  formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const { confirmPassword, ...registerData } = form
        await register(registerData)
        ElMessage.success('注册成功，即将跳转登录')
        setTimeout(() => {
          router.push('/login')
        }, 3000)
      } catch (e) {
        // error handled by interceptor
      } finally {
        loading.value = false
      }
    } else {
      return false
    }
  })
}
</script>

<style scoped>
/* Styles remain unchanged */
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
  width: 450px; /* 略宽于登录卡片以容纳更多字段 */
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  border: none;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  padding: 10px;
}

.card-header {
  text-align: center;
  margin-bottom: 25px;
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
  margin-top: 5px;
  transition: all 0.3s;
}

.login-btn:hover {
  opacity: 0.9;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(210, 105, 30, 0.3);
}

.form-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 5px;
  font-size: 14px;
}

.login-link {
  color: #666;
}

.login-link a {
  color: #D2691E;
  text-decoration: none;
  font-weight: bold;
  margin-left: 5px;
}

.login-link a:hover {
  text-decoration: underline;
}

.footer-tip {
  margin-top: 20px;
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
