import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
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
        {
          path: 'farmer-story/:id',
          name: 'FarmerStoryDetail',
          component: () => import('../views/FarmerStoryDetail.vue')
        },
        { path: 'cart', component: () => import('../views/CartView.vue') },
        { path: 'search', component: () => import('../views/SearchView.vue') },
        { path: 'orders', component: () => import('../views/OrderView.vue') }
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
      path: '/admin',
      component: () => import('../views/admin/AdminLayout.vue'),
      meta: { requiresAuth: true },
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
          path: 'after-sales',
          name: 'AfterSaleManage',
          component: () => import('../views/admin/AfterSaleManage.vue'),
          meta: { title: '售后管理' }
        },
        {
          path: 'special-recommendations',
          name: 'SpecialRecommendManage',
          component: () => import('../views/admin/SpecialRecommendManage.vue'),
          meta: { title: '特别推荐管理', requiresRole: 'ADMIN' }
        },
        {
          path: 'flash-sales',
          name: 'FlashSaleManage',
          component: () => import('../views/admin/FlashSaleManage.vue'),
          meta: { title: '限时特价秒杀', requiresRole: 'ADMIN' }
        },
        {
          path: 'farmer-stories',
          name: 'FarmerStoryManage',
          component: () => import('../views/admin/FarmerStoryManage.vue'),
          meta: { title: '茶农故事管理', requiresRole: 'ADMIN' }
        },
        {
          path: 'users',
          name: 'UserManage',
          component: () => import('../views/admin/UserManage.vue'),
          meta: { title: '用户管理', requiresRole: 'ADMIN' }
        }
      ]
    }
  ]
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const isAuthenticated = !!userStore.token
  const userRole = userStore.role

  if (to.matched.some((record) => record.meta.requiresAuth)) {
    if (!isAuthenticated) {
      next({ path: '/login', query: { redirect: to.fullPath } })
    } else {
      const requiredRole = to.matched.find((record) => record.meta.requiresRole)?.meta.requiresRole
      if (requiredRole && userRole !== requiredRole) {
        if (to.path !== '/admin/dashboard') {
          next('/admin/dashboard')
        } else {
          next()
        }
      } else {
        next()
      }
    }
  } else {
    next()
  }
})

export default router
