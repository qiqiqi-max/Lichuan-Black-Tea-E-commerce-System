import request from './index'

export const getProductDetail = (id) => {
  return request.get(`/products/${id}`)
}
