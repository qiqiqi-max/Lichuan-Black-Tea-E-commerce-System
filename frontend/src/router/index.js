import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../views/Layout.vue'
import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: Layout,
      children: [
        { path: '', component: HomeView },
        { 
          path: 'product/:id', 
          name: 'ProductDetail',
          component: () => import('../views/ProductDetail.vue') 
        },
        { path: 'cart', component: () => import('../views/CartView.vue') },
        { path: 'search', component: () => import('../views/SearchView.vue') },
        { path: 'orders', component: () => import('../views/OrderView.vue') } // Added OrderView
      ]
    },
    {
      path: '/login',
      component: LoginView
    },
    {
      path: '/register',
      component: () => import('../views/RegisterView.vue')
    },
    {
      path: '/farmer',
      component: () => import('../views/farmer/FarmerLayout.vue'),
      meta: { requiresAuth: true, role: 'FARMER' },
      children: [
        { path: '', redirect: '/farmer/dashboard' },
        {
          path: 'dashboard',
          name: 'FarmerDashboard',
          component: () => import('../views/farmer/Dashboard.vue'),
          meta: { title: '农户仪表盘' }
        },
        {
          path: 'products',
          name: 'FarmerProductManage',
          component: () => import('../views/farmer/ProductManage.vue'),
          meta: { title: '商品管理' }
        },
        {
          path: 'orders',
          name: 'FarmerOrderManage',
          component: () => import('../views/farmer/OrderManage.vue'),
          meta: { title: '订单管理' }
        },
        {
          path: 'profile',
          name: 'FarmerProfile',
          component: () => import('../views/farmer/Profile.vue'),
          meta: { title: '个人资料' }
        }
      ]
    },
    {
      path: '/admin',
      component: () => import('../views/admin/AdminLayout.vue'),
      meta: { requiresAuth: true, role: 'ADMIN' },
      children: [
        {
          path: '',
          redirect: '/admin/dashboard'
        },
        {
          path: 'dashboard',
          name: 'AdminDashboard',
          component: () => import('../views/admin/Dashboard.vue'),
          meta: { title: '仪表盘' }
        },
        {
          path: 'products',
          name: 'ProductManage',
          component: () => import('../views/admin/ProductManage.vue'),
          meta: { title: '商品管理' }
        },
        {
          path: 'orders',
          name: 'OrderManage',
          component: () => import('../views/admin/OrderManage.vue'),
          meta: { title: '订单管理' }
        },
        {
          path: 'users',
          name: 'UserManage',
          component: () => import('../views/admin/UserManage.vue'),
          meta: { title: '用户管理' }
        }
      ]
    }
  ]
})

export default router
