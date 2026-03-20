# 技术架构文档 - 利川红茶电商助农系统

## 1. 系统总体架构
采用 **前后端分离 (B/S)** 架构。
- **前端**: Vue 3 单页应用 (SPA)。
- **后端**: Spring Boot RESTful API。
- **数据库**: H2 Database (开发/演示环境)，兼容 MySQL。

## 2. 技术栈详细选型

### 2.1 前端 (Frontend)
- **框架**: Vue 3 (Composition API, Script Setup)
- **构建工具**: Vite
- **UI 组件库**: Element Plus
- **数据可视化**: ECharts (用于后台仪表盘)
- **状态管理**: Pinia (用于购物车、用户信息)
- **资源管理**: 本地静态资源 (/public/images/)，拒绝外部 CDN 依赖
- **路由**: Vue Router 4
- **HTTP 客户端**: Axios
- **样式**: SCSS / CSS

### 2.2 后端 (Backend)
- **核心框架**: Spring Boot 2.7.x
- **ORM**: Spring Data JPA
- **数据库**: H2 Database (In-Memory, 配置 MySQL Dialect)
- **工具库**: Lombok (简化样板代码)
- **构建工具**: Maven

## 3. 数据库设计 (ERD)

### 3.1 实体关系
- **User** (1) <---> (N) **Order**
- **Order** (1) <---> (N) **OrderItem**
- **Product** (1) <---> (N) **OrderItem**

### 3.2 表结构定义

#### User (用户表)
| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT | PK, Auto Inc |
| username | VARCHAR | 唯一, 登录名 |
| password | VARCHAR | 密码 |
| role | VARCHAR | USER / ADMIN |
| nickname | VARCHAR | 昵称 |
| phone | VARCHAR | 手机号 |

#### Product (商品表 - 利川红茶)
| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT | PK, Auto Inc |
| name | VARCHAR | 商品名称 |
| description | TEXT | 商品详情 |
| price | DECIMAL | 价格 |
| stock | INT | 库存 |
| sales | INT | **销量** (实时更新) |
| cover_img | VARCHAR | 封面图 URL |
| category | VARCHAR | 分类 (冷后浑, 玛瑙红) |
| origin | VARCHAR | **产地** (核心字段, 如: 毛坝镇) |
| farmer_name | VARCHAR | **农户** (助农体现) |

#### Order (订单表)
| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT | PK, Auto Inc |
| order_no | VARCHAR | 订单号 (UUID) |
| user_id | BIGINT | FK -> User.id |
| total_amount | DECIMAL | 订单总金额 |
| status | VARCHAR | WAIT_PAY, PAID, SHIPPED, DONE, CANCELLED |
| address | VARCHAR | 收货地址 |
| receiver_name | VARCHAR | 收货人姓名 |
| receiver_phone | VARCHAR | 收货人电话 |
| remark | VARCHAR | 订单备注 |
| create_time | DATETIME | 下单时间 |

#### OrderItem (订单明细表)
| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT | PK, Auto Inc |
| order_id | BIGINT | FK -> Order.id |
| product_id | BIGINT | FK -> Product.id |
| product_name | VARCHAR | 冗余商品名 (含规格) |
| price | DECIMAL | 购买时单价 |
| quantity | INT | 购买数量 |

## 4. API 接口设计

### 4.1 Auth
- `POST /api/login`: 登录
- `POST /api/register`: 注册

### 4.2 Product
- `GET /api/products`: 获取商品列表 (参数: page, size, search)
- `GET /api/products/{id}`: 获取详情
- `POST /api/products`: 新增商品 (Admin)
- `PUT /api/products/{id}`: 更新商品 (Admin)
- `DELETE /api/products/{id}`: 删除商品 (Admin)

### 4.3 Order
- `POST /api/orders`: 创建订单
- `POST /api/orders/{id}/pay`: 支付订单 (模拟支付，更新状态为 PAID)
- `GET /api/orders/my`: 获取当前用户订单
- `GET /api/admin/orders`: 获取所有订单 (Admin)
- `PUT /api/admin/orders/{id}/ship`: 发货 (Admin)
- `PUT /api/admin/orders/{id}`: 编辑订单信息 (Admin)

### 4.4 Dashboard (Admin)
- `GET /api/admin/dashboard/stats`: 获取仪表盘统计数据（今日订单、销量、趋势图等）

### 4.5 User (Admin)
- `GET /api/admin/users`: 获取用户列表
- `POST /api/admin/users`: 新增用户
- `PUT /api/admin/users/{id}`: 更新用户
- `DELETE /api/admin/users/{id}`: 删除用户

### 4.6 Cart
- `POST /api/cart/add`: 添加商品到购物车
- `POST /api/cart/remove`: 移除购物车商品 (通过 Body 传递 productName)
- `GET /api/cart`: 获取购物车列表
- `DELETE /api/cart/clear`: 清空购物车

## 5. 项目目录结构规范
```
backend/
  src/main/java/com/lichuan/tea/
    controller/  (AuthController, ProductController, OrderController, DashboardController, UserController, CartController)
    entity/
    repository/
    service/
    dto/         (DashboardStatsDTO, CartItemDTO, RegisterRequest)
    config/      (Cors, WebMvc)
    common/      (Result, Exception)
frontend/
  src/
    api/
    assets/
    components/
    router/
    stores/
    views/
      admin/     (Dashboard, ProductManage, OrderManage, UserManage, AdminLayout)
      Layout.vue
```
