import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '/api',
  timeout: 5000
})

api.interceptors.response.use(
  response => {
    const res = response.data;
    // Assuming backend returns { code, data, message }
    if (res.code === 200 || response.status === 200) {
      return res.data; // For successful biz logic
    } else {
      if (!response.config.skipErrorMessage) {
        ElMessage.error(res.message || '操作失败');
      }
      return Promise.reject(new Error(res.message || 'Error'));
    }
  },
  error => {
    if (error.response && error.response.data && error.response.data.message) {
      ElMessage.error(error.response.data.message);
      return Promise.reject(new Error(error.response.data.message));
    }
    ElMessage.error(error.message || '网络错误');
    return Promise.reject(error);
  }
);

export default api
