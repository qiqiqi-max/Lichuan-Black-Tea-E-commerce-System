import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useCartStore = defineStore('cart', () => {
  const items = ref([])

  const totalAmount = computed(() => {
    return items.value.reduce((sum, item) => sum + Number(item.price) * Number(item.quantity), 0).toFixed(2)
  })

  const totalCount = computed(() => {
    return items.value.reduce((sum, item) => sum + Number(item.quantity), 0)
  })

  function getCartItemKey(product) {
    return `${product.productId}_${product.spec}`
  }

  function addToCart(product, quantity = 1) {
    const key = getCartItemKey(product)
    const existing = items.value.find(i => getCartItemKey(i) === key)
    const addQuantity = product.quantity || quantity

    if (existing) {
      existing.quantity += addQuantity
    } else {
      items.value.push({ ...product, quantity: addQuantity })
    }
  }

  function removeFromCart(product) {
    const key = getCartItemKey(product)
    const index = items.value.findIndex(i => getCartItemKey(i) === key)
    if (index > -1) {
      items.value.splice(index, 1)
    }
  }

  function clearCart() {
    items.value = []
  }

  return { items, totalAmount, totalCount, addToCart, removeFromCart, clearCart }
}, {
  persist: {
    storage: sessionStorage,
  },
})
