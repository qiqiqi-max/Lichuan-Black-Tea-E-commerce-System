import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'

const api = axios.create({
  baseURL: '/api',
  timeout: 5000
})

api.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.user && userStore.user.id) {
      config.headers['X-User-Id'] = userStore.user.id
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

api.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res.data
    } else {
      if (!response.config.skipErrorMessage) {
        ElMessage.error(res.msg || 'Error')
      }
      return Promise.reject(new Error(res.msg || 'Error'))
    }
  },
  error => {
    if (!error.config || !error.config.skipErrorMessage) {
      ElMessage.error(error.message)
    }
    return Promise.reject(error)
  }
)

export default api
