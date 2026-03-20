import { defineStore } from 'pinia'
import api from '../api'

export const useSearchStore = defineStore('search', {
  state: () => ({
    results: [],
    keyword: '',
    total: 0,
    loading: false
  }),
  actions: {
    async searchProducts(keyword, page = 1, size = 10) {
      this.loading = true
      this.keyword = keyword
      try {
        const res = await api.get('/products/search', {
          params: { keyword, page, size }
        })
        // Backend returns Result<Page<Product>>, axios interceptor might unwrap Result.
        // Assuming api.get returns the data payload directly if success.
        // Page object structure: { content: [], totalElements: 0, ... }
        this.results = res.content || []
        this.total = res.totalElements || 0
      } catch (e) {
        console.error('Search failed', e)
        this.results = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },
    clearSearch() {
      this.results = []
      this.keyword = ''
      this.total = 0
    }
  }
})
