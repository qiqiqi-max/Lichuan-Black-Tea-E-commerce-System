<template>
  <div class="cart-container">
    <h2>购物车</h2>
    <el-table :data="cartStore.items" style="width: 100%">
      <el-table-column prop="name" label="商品" />
      <el-table-column prop="price" label="单价" />
      <el-table-column prop="quantity" label="数量" />
      <el-table-column label="操作">
        <template #default="scope">
          <el-button type="danger" link @click="handleRemove(scope.row)">移除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="footer">
      <span>总计: <span class="total">¥ {{ cartStore.totalAmount }}</span></span>
      <el-button type="primary" @click="checkout" :disabled="cartStore.items.length === 0">去结算</el-button>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useCartStore } from '../stores/cart'
import { useUserStore } from '../stores/user'
import { useRouter } from 'vue-router'
import api from '../api'
import { ElMessage } from 'element-plus'

const cartStore = useCartStore()
const userStore = useUserStore()
const router = useRouter()

onMounted(async () => {
  if (userStore.token && userStore.user) {
    try {
      const res = await api.get(`/cart?userId=${userStore.user.username}`)
      // Sync backend cart to local store
      // We should probably clear local and replace, or merge.
      // For simplicity, let's replace.
      cartStore.clearCart()
      res.forEach(item => {
        // Backend returns CartItemDTO.
        // We need to map it to store format.
        // Store expects: { id, name, price, coverImg, quantity }
        cartStore.addToCart({
            id: item.productId,
            name: item.productName,
            price: item.price,
            coverImg: item.coverImg
        }, item.quantity)
      })
    } catch (e) {
      console.error('Failed to sync cart', e)
    }
  }
})

const handleRemove = async (item) => {
    try {
        if (userStore.user) {
             // Use POST with body to avoid URL encoding issues with special chars in product name
             await api.post(`/cart/remove?userId=${userStore.user.username}`, {
                 productName: item.name
             })
        }
        cartStore.removeFromCart(item.name)
        ElMessage.success('已移除')
    } catch (e) {
        ElMessage.error('移除失败')
    }
}

const checkout = async () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  const orderData = {
    userId: userStore.user.id,
    totalAmount: cartStore.totalAmount,
    address: '默认收货地址', // Simplified
    items: cartStore.items.map(i => ({
      productId: i.id,
      productName: i.name,
      price: i.price,
      quantity: i.quantity
    }))
  }

  try {
    await api.post('/orders', orderData)
    ElMessage.success('下单成功')
    cartStore.clearCart()
    router.push('/orders')
  } catch (e) {
    // handled
  }
}
</script>

<style scoped>
.cart-container {
  max-width: 800px;
  margin: 40px auto;
  padding: 20px;
  background: #fff;
}
.footer {
  margin-top: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.total {
  color: #f56c6c;
  font-size: 20px;
  font-weight: bold;
}
</style>
