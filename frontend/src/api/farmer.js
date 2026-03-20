import request from './index'

export const getFarmerProfile = (id) => {
  // This might need a dedicated backend endpoint
  // For now, let's assume a public endpoint exists
  return request.get(`/farmer-profiles/${id}`)
}
