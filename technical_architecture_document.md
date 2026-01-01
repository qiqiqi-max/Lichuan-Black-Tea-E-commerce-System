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
- **状态管理**: Pinia (用于购物车、用户信息)
- **路由**: Vue Router 4
- **HTTP 客户端**: Axios
- **样式**: SCSS / CSS

### 2.2 后端 (Backend)
- **核心框架**: Spring Boot 3.x
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
| status | VARCHAR | WAIT_PAY, WAIT_SHIP, SHIPPED, DONE |
| address | VARCHAR | 收货地址 |
| create_time | DATETIME | 下单时间 |

#### OrderItem (订单明细表)
| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT | PK, Auto Inc |
| order_id | BIGINT | FK -> Order.id |
| product_id | BIGINT | FK -> Product.id |
| product_name | VARCHAR | 冗余商品名 (快照) |
| price | DECIMAL | 购买时单价 |
| quantity | INT | 购买数量 |

## 4. API 接口设计

### 4.1 Auth
- `POST /api/login`: 登录
- `POST /api/register`: 注册

### 4.2 Product
- `GET /api/products`: 获取商品列表 (参数: page, size, search)
- `GET /api/products/{id}`: 获取详情
- `POST /api/admin/products`: 新增商品 (Admin)
- `PUT /api/admin/products/{id}`: 更新商品 (Admin)
- `DELETE /api/admin/products/{id}`: 删除商品 (Admin)

### 4.3 Order
- `POST /api/orders`: 创建订单
- `GET /api/orders/my`: 获取当前用户订单
- `GET /api/admin/orders`: 获取所有订单 (Admin)
- `PUT /api/admin/orders/{id}/ship`: 发货 (Admin)

## 5. 项目目录结构规范
```
backend/
  src/main/java/com/lichuan/tea/
    controller/
    entity/
    repository/
    service/
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
      admin/
      Layout.vue
```
