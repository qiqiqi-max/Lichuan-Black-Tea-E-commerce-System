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
        { path: 'product/:id', component: () => import('../views/ProductDetail.vue') },
        { path: 'cart', component: () => import('../views/CartView.vue') },
        { path: 'orders', component: () => import('../views/OrderView.vue') } // Added OrderView
      ]
    },
    {
      path: '/login',
      component: LoginView
    },
    {
      path: '/admin',
      component: () => import('../views/admin/Dashboard.vue')
    }
  ]
})

export default router
