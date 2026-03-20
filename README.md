# 利川红茶电商助农系统 (Lichuan Black Tea E-commerce System)

本项目是基于 Spring Boot 和 Vue 3 开发的前后端分离电商助农系统，旨在推广利川红茶，连接农户与消费者。

## 1. 技术栈
- **前端**: Vue 3, Vite, Element Plus, Pinia, Axios, Vue Router, ECharts
- **后端**: Spring Boot 2.7.x (适配 Java 11), Spring Data JPA, H2 Database
- **数据库**: H2 内存数据库 (应用启动时自动初始化数据)

## 2. 功能模块
- **前台商城**:
    - 首页轮播图与商品展示（突出产地和农户信息，全本地高清素材）
    - 商品详情页：支持查看农户故事、选择规格（标准装/礼盒装）
    - **实时销量**: 商品销量随订单实时增加，数据真实可信
    - **购物车功能**: 支持多规格商品独立管理，实时同步后端，持久化存储
    - **支付模拟**: 真实还原扫码支付流程（模拟），支持订单二次支付
    - 用户订单中心：查看订单状态、物流进度
- **后台管理**:
    - **仪表盘**: 实时数据可视化（今日订单、销量趋势图等）
    - **商品管理**: 增删改查商品信息，支持设置初始销量
    - **订单管理**: 订单查看、发货、编辑
    - **用户管理**: 用户列表、新增、编辑、删除
- **认证授权**:
    - 用户注册/登录 (基于 Token)
    - 角色权限控制 (USER / ADMIN)

## 3. 快速开始

### 3.1 环境要求
- JDK 11+
- Node.js 16+
- Maven 3.6+

### 3.2 启动后端
1. 进入 `backend` 目录。
2. 运行命令: `mvn spring-boot:run`
3. 服务将启动在 `http://localhost:8080`。
4. H2 控制台: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:teadb`, User: `sa`, Password: 空)

### 3.3 启动前端
1. 进入 `frontend` 目录。
2. 安装依赖: `npm install`
3. 启动开发服务器: `npm run dev`
4. 访问地址: `http://localhost:5173`

## 4. 测试账号
为了方便演示，系统启动时会自动创建以下账号：

| 角色 | 用户名 | 密码 | 权限 |
| --- | --- | --- | --- |
| **管理员** | `admin` | `123456` | 商品管理、订单发货、仪表盘 |
| **普通用户** | `user` | `123456` | 浏览、购物、查看订单 |

## 5. 项目结构
```
├── backend/            # 后端工程
│   ├── src/main/java/com/lichuan/tea/
│   │   ├── controller/ # 控制层
│   │   ├── entity/     # 实体类 (User, Product, Order)
│   │   ├── repository/ # 数据访问层
│   │   ├── dto/        # 数据传输对象
│   │   └── config/     # 配置类 (CORS)
│   └── src/main/resources/application.yml
├── frontend/           # 前端工程
│   ├── src/
│   │   ├── api/        # Axios 封装
│   │   ├── stores/     # Pinia 状态管理
│   │   ├── views/      # 页面组件
│   │   └── components/ # 公共组件
└── product_requirements_document.md # 产品需求文档
```

## 6. 注意事项
- 本项目使用 H2 内存数据库，重启服务后数据会重置。
- 前端配置了代理 `/api` 转发到 `localhost:8080`，请确保后端正常运行。
- 后台仪表盘数据基于真实订单实时计算，初次启动可能为空。
