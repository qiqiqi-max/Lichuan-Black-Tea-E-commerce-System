# Restart Backend and Verify Fixes

## 1. Restart Backend Service
Since the backend code (`ProductController.java`) was modified to handle 404 errors, the Spring Boot application needs to be restarted to apply these changes.
- **Action**: Run `mvn spring-boot:run` in the `backend` directory.

## 2. Verify Frontend Behavior
Once the backend is running, you can verify the fixes in the browser:
- **Valid Product**: Visit a valid product (e.g., `/product/101`).
  - **Expected**: Loading skeleton -> Product details displayed.
- **Invalid Product**: Visit a non-existent product (e.g., `/product/9999`).
  - **Expected**: Loading skeleton -> "未找到该商品信息" (Frontend fallback) or "该商品不存在或已下架" (Backend 404 message) -> "返回商品列表" button.

## 3. Frontend Hot-Reload
The frontend (`ProductDetail.vue`) changes are automatically applied by Vite (Terminal 4 is already running), so no restart is needed for the frontend.
