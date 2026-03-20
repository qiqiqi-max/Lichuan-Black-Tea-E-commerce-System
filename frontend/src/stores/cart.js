import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useCartStore = defineStore('cart', () => {
  const items = ref([])

  const totalAmount = computed(() => {
    return items.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
  })

  const totalCount = computed(() => {
    return items.value.reduce((sum, item) => sum + item.quantity, 0)
  })

  function addToCart(product, quantity = 1) {
    // Generate a unique key based on product ID AND specification name
    // Assuming product.name includes spec info, e.g., "Product Name [Spec Name]"
    // Or we should rely on product.name being unique enough
    // But product.id is just the base product ID.
    // If we use product.id as key, we overwrite different specs!
    // So we must find by name OR generate a composite ID.
    
    // Better approach: Use name as the unique identifier for cart items 
    // since name already includes spec: "利川红 [礼盒装]"
    const existing = items.value.find(i => i.name === product.name)
    
    if (existing) {
      existing.quantity += quantity
      // Update price just in case
      existing.price = product.price
    } else {
      items.value.push({ ...product, quantity })
    }
  }

  function removeFromCart(productName) {
    const index = items.value.findIndex(i => i.name === productName)
    if (index > -1) {
      items.value.splice(index, 1)
    }
  }

  function clearCart() {
    items.value = []
  }

  return { items, totalAmount, totalCount, addToCart, removeFromCart, clearCart }
})
