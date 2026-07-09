# 技术架构文档

## 项目：利川红茶助农平台
版本：`v0.6`  
更新时间：`2026-05-17`

---

## 1. 架构总览

系统采用前后端分离架构：
- 前端：`Vue 3 + Vite + Element Plus + Pinia + Vue Router + Axios`
- 后端：`Spring Boot 2.7.18 + Spring Data JPA`
- 数据库：`MySQL 8`
- 缓存：`Redis`（已完成配置，当前业务使用较少）

请求链路：
1. 前端调用 `/api/*`
2. Vite 代理转发到 `http://localhost:8080`
3. 后端统一返回 `Result<T>`（文件下载接口除外）

当前认证模型：
- 登录返回 `mock-token-{userId}` 与 `user`
- 前端通过 Axios 拦截器注入 `X-User-Id`
- 后端按 `X-User-Id + role` 进行权限判断
- 购物车接口使用 `userId` 查询参数（历史实现，未统一为请求头）
- 启动时通过 `CommandLineRunner` 自动初始化默认用户、样例商品与茶农故事

## 2. 运行与配置

### 2.1 配置文件
- `backend/src/main/resources/application.yml`
  - 默认激活：`dev`
  - 端口：`8080`
  - 上传限制：`5MB`
- `application-dev.yml`
  - MySQL：`lichuan_tea_db`
  - JPA：`ddl-auto=update`、`show-sql=false`
  - Redis：`localhost:6379`
- `application-prod.yml`
  - 端口：`80`
  - JPA：`ddl-auto=validate`

### 2.2 代理与端口
- 前端默认端口：`5173`（冲突自动增量）
- 后端默认端口：`8080`
- 代理配置：`frontend/vite.config.js` 中 `/api -> http://localhost:8080`

### 2.3 文件目录
- 商品图片：`backend/uploads/product-images`
- 茶农故事图片：`backend/uploads/farmer-stories`

## 3. 后端分层设计

- `controller`：HTTP 入口与参数接收
- `service`：业务规则、权限边界、状态流转
- `repository`：JPA 查询与分页检索
- `entity`：表结构映射
- `dto`：分页/保存请求/统计返回
- `config`：CORS、Redis、Web 配置
- `common`：统一返回结构 `Result`

核心控制器：
- `AuthController`
- `ProductController`
- `OrderController`
- `AfterSalesController`
- `CartController`
- `SpecialRecommendationController`
- `FlashSaleController`
- `FarmerStoryController`
- `DashboardController`
- `UserController`
- `AdminController`

## 4. 前端模块设计

- `views/`：商城端页面（首页、详情、搜索、购物车、订单、故事详情）
- `views/admin/`：后台页面（仪表盘、商品、订单、售后、推荐、秒杀、故事、用户）
- `stores/`：用户/购物车/搜索状态（Pinia，含持久化插件）
- `api/`：Axios 实例、请求头注入、统一错误提示
- `router/`：路由守卫与角色访问控制

实现要点：
- `/admin/orders` 对 `ADMIN/FARMER` 复用同一页面
- 后台菜单文案按角色动态切换（如“订单管理/订单发货”）

## 5. 数据模型

核心表（对应实体）：
- `users`
- `farmer_profiles`
- `products`
- `orders`
- `order_items`
- `cart_items`
- `after_sales_requests`
- `special_recommendations`
- `flash_sales`
- `farmer_story`

关键关系：
- `Product -> FarmerProfile`（多对一）
- `Order -> OrderItem`（一对多）
- `OrderItem -> FarmerProfile`（多对一）
- `SpecialRecommendation -> Product`（多对一）
- `FlashSale -> Product`（多对一）

## 6. 关键业务实现

### 6.1 下单与价格口径
- 接口：`POST /api/orders`
- 后端逐条校验商品与数量
- 命中有效秒杀时按 `seckillPrice` 结算
- `Order.totalAmount` 后端计算并落库
- 下单时写入 `OrderItem.farmer`，支持后续农户权限过滤

### 6.2 茶农订单可见性
- 茶农查询订单时：
1. `currentUser.id -> farmer_profile.id`
2. 查茶农名下商品 ID
3. 通过 `order_item.product_id IN (...)` 反查订单

### 6.3 售后权限与流转
- 用户只能对本人订单发起售后
- 管理侧权限：`ADMIN` 全量；`FARMER` 仅自己商品相关订单
- 审核动作：`APPROVE / REJECT`
- 状态更新：`APPROVED -> PROCESSING -> COMPLETED`

### 6.4 图片上传、读取与兜底
- 上传接口：
  - `POST /api/products/upload-image`
  - `POST /api/farmer-stories/upload-image`
- 读取接口：
  - `GET /api/products/image/{fileName}`
  - `GET /api/farmer-stories/image/{fileName}`
- 读取策略：
  - 主目录读取
  - 多历史目录兼容
  - 缺图时返回 fallback 图片，避免前端 404

### 6.5 仪表盘双视角
- 管理员：今日订单、总销售额、总用户数、待发货、近 7 日销售、最近订单
- 茶农：本人相关今日订单、销售额、待发货、近 7 日销售、最近订单

## 7. 接口分组（摘要）

- 认证：`/api/login`、`/api/register`
- 商品：`/api/products/*`
- 订单：`/api/orders/*`、`/api/admin/orders`
- 购物车：`/api/cart/*`
- 售后：`/api/after-sales/*`
- 运营：`/api/special-recommendations/*`、`/api/flash-sales/*`
- 茶农故事：`/api/farmer-stories/*`
- 仪表盘：`/api/admin/dashboard/*`
- 用户管理：`/api/admin/users/*`

## 8. 运维脚本与启动

目录：`backend/scripts`

- `start-dev.ps1`
  - 可检测端口占用
  - 支持 `-KillPortOwner` 自动释放端口并启动
- `repair-missing-product-images.ps1`
  - 扫描商品图片缺失
  - 支持 dry-run 与 `-Apply` 批量补图
- `repair-missing-farmer-story-images.ps1`
  - 扫描茶农故事图片缺失
  - 支持 dry-run 与 `-Apply` 批量补图

## 9. 当前风险与技术债

- 认证授权仍为简化方案，缺少统一鉴权中间层
- 密码明文存储，需升级 `BCrypt`
- CORS 仍为宽松配置，生产环境需白名单化
- 全局异常分层与标准错误码体系尚未完备
- 自动化测试覆盖率不足

## 10. 演进建议

1. 引入 `Spring Security + JWT + BCrypt`
2. 建立统一权限注解与鉴权拦截器
3. 增加 Swagger/Knife4j 接口文档
4. 补齐关键链路自动化测试（下单、发货、售后、权限）
