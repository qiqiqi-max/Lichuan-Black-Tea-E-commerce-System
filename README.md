# 利川红茶助农平台（Lichuan Tea Platform）

基于 `Spring Boot 2.7 + Vue 3` 的前后端分离电商平台，聚焦“茶农直供、真实可溯源、助农有温度”。

- 文档版本：`v0.6`
- 最后更新：`2026-05-17`

## 1. 当前项目状态（以代码实况为准）

### 1.1 已落地能力
- 用户与权限：注册、登录、角色区分（`USER` / `FARMER` / `ADMIN`）
- 商城链路：商品列表、商品搜索、商品详情、加购、下单、模拟支付
- 订单链路：我的订单、后台订单管理、农户订单发货
- 售后链路：用户发起售后、后台审核、状态流转
- 运营模块：特别推荐、限时秒杀（后台配置 + 前台展示）
- 内容模块：茶农故事（列表、详情、后台管理）
- 仪表盘：管理员与茶农双视角统计
- 系统启动时自动补齐 `admin` / `user` / `farmer` 账号，并自动初始化样例商品、茶农故事
- 文件能力：商品图/故事图上传、读取、缺图回退与修复

### 1.2 最近稳定性改动
- 首页“全部茶品”按接口完整返回展示，不再固定截断 8 条
- 首页商品卡片支持快捷“加入购物车”
- 农户“订单发货”查询改为“按订单明细中是否包含本人商品”反查
- 商品图与故事图支持多目录兼容读取 + fallback 图片，显著降低 404
- 商品详情页对无效商品 ID 增加兜底提示与返回入口

## 2. 技术栈与环境

- 前端：`Vue 3`、`Vite 5`、`Element Plus`、`Pinia`、`Pinia Persistedstate`、`Vue Router`、`Axios`、`ECharts`
- 后端：`Spring Boot 2.7.18`、`Spring Data JPA`、`MySQL 8`、`Redis`（已接入配置，业务可继续扩展）
- 推荐环境：`JDK 11+`、`Maven 3.9+`、`Node.js 18+`

## 3. 快速启动

### 3.1 启动后端（dev）

```bash
cd backend
powershell -ExecutionPolicy Bypass -File .\scripts\start-dev.ps1 -KillPortOwner
```

备用：

```bash
cd backend
mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=dev"
```

默认后端配置：
- 服务端口：`8080`
- 数据库：`jdbc:mysql://localhost:3306/lichuan_tea_db`
- 用户名：`root`
- 密码：`123456`

### 3.2 启动前端

```bash
cd frontend
npm install
npm run dev -- --host 0.0.0.0 --port 5173
```

默认访问：
- 前端：`http://localhost:5173`
- 后端：`http://localhost:8080`

说明：`5173` 被占用时，Vite 会自动切换到 `5174+`。

## 4. 默认测试账号

系统启动时自动补齐（缺失则创建）：
- 管理员：`admin / 123456`
- 普通用户：`user / 123456`
- 茶农：`farmer / 123456`

## 5. 功能模块（当前实现）

### 5.1 前台商城
- 首页：限时秒杀、特别推荐、全部茶品、茶农故事（均为动态接口数据）
- 商品详情：秒杀价展示、倒计时、加购、立即购买
- 搜索页：关键词分页搜索
- 购物车：增删改数量、提交下单
- 订单页：查看订单、模拟支付、申请售后、查看售后进度
- 茶农故事详情：`/farmer-story/:id`

### 5.2 后台管理
- 仪表盘：管理员/茶农双口径统计
- 商品管理：筛选、分页、新增/编辑/删除、图片上传
- 订单管理：管理员看全量；茶农看“包含本人商品”的订单并发货
- 售后管理：审核、处理中、完成等流转
- 特别推荐管理（ADMIN）
- 限时秒杀管理（ADMIN）
- 茶农故事管理（ADMIN）
- 用户管理（ADMIN）

## 6. 接口摘要（按当前代码）

### 6.1 认证
- `POST /api/login`
- `POST /api/register`

### 6.2 商品
- `GET /api/products`
- `GET /api/products/search`
- `GET /api/products/{id}`
- `GET /api/products/manage-page`
- `POST /api/products`
- `PUT /api/products/{id}`
- `DELETE /api/products/{id}`
- `POST /api/products/upload-image`
- `GET /api/products/image/{fileName}`

### 6.3 订单
- `POST /api/orders`
- `GET /api/orders`
- `GET /api/orders/my`
- `GET /api/orders/list`
- `POST /api/orders/{id}/pay`
- `PUT /api/orders/{id}/ship`
- `PUT /api/orders/{id}`
- `GET /api/admin/orders`

### 6.4 购物车（使用 `userId` 查询参数）
- `POST /api/cart/add`
- `DELETE /api/cart/remove`
- `GET /api/cart`
- `DELETE /api/cart/clear`

### 6.5 售后
- `POST /api/after-sales`
- `GET /api/after-sales/my`
- `GET /api/after-sales/manage`
- `GET /api/after-sales/manage-page`
- `PUT /api/after-sales/{id}/review`
- `PUT /api/after-sales/{id}/status`

### 6.6 运营与内容
- `GET /api/special-recommendations/active`
- `GET /api/special-recommendations/manage`
- `GET /api/special-recommendations/manage-page`
- `POST /api/special-recommendations`
- `PUT /api/special-recommendations/{id}`
- `DELETE /api/special-recommendations/{id}`
- `GET /api/flash-sales/active`
- `GET /api/flash-sales/manage`
- `GET /api/flash-sales/manage-page`
- `POST /api/flash-sales`
- `PUT /api/flash-sales/{id}`
- `DELETE /api/flash-sales/{id}`
- `GET /api/farmer-stories/active`
- `GET /api/farmer-stories/{id}`
- `GET /api/farmer-stories/manage-page`
- `POST /api/farmer-stories`
- `PUT /api/farmer-stories/{id}`
- `PUT /api/farmer-stories/{id}/status`
- `DELETE /api/farmer-stories/{id}`
- `POST /api/farmer-stories/upload-image`
- `GET /api/farmer-stories/image/{fileName}`

### 6.7 后台统计与用户
- `GET /api/admin/dashboard/stats`
- `GET /api/admin/users`
- `GET /api/admin/users/page`
- `POST /api/admin/users`
- `PUT /api/admin/users/{id}`
- `DELETE /api/admin/users/{id}`

## 7. 项目结构

```text
backend/
  src/main/java/com/lichuan/tea/
    controller/
    service/
    repository/
    entity/
    dto/
    config/
    common/
  src/main/resources/
    application.yml
    application-dev.yml
    application-prod.yml
  uploads/
    product-images/
    farmer-stories/
  scripts/
    start-dev.ps1
    repair-missing-product-images.ps1
    repair-missing-farmer-story-images.ps1

frontend/
  src/
    api/
    router/
    stores/
    views/
    views/admin/
    components/
```

## 8. 运维脚本

目录：`backend/scripts`

- 一键启动开发后端并可自动释放端口：

```powershell
powershell -ExecutionPolicy Bypass -File .\backend\scripts\start-dev.ps1 -KillPortOwner
```

- 修复商品缺图（先 dry-run，再 `-Apply`）：

```powershell
powershell -ExecutionPolicy Bypass -File .\backend\scripts\repair-missing-product-images.ps1
powershell -ExecutionPolicy Bypass -File .\backend\scripts\repair-missing-product-images.ps1 -Apply
```

- 修复茶农故事缺图（先 dry-run，再 `-Apply`）：

```powershell
powershell -ExecutionPolicy Bypass -File .\backend\scripts\repair-missing-farmer-story-images.ps1
powershell -ExecutionPolicy Bypass -File .\backend\scripts\repair-missing-farmer-story-images.ps1 -Apply
```

## 9. 当前验证结果（2026-03-25）

- 后端：`mvn -q -DskipTests compile` 通过
- 前端：`npm run build` 通过（存在 Vite 大包体积告警，功能不受影响）

## 10. 已知技术债

- 认证授权仍为简化方案：`mock-token + X-User-Id`，未接入 JWT/Spring Security
- 密码目前明文存储，需升级 `BCrypt`
- CORS 当前为宽松策略，生产环境需白名单收敛
- 全局异常处理与错误码规范化仍需完善
- 自动化测试覆盖不足（后端单测、前端 E2E）
