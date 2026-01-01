# 利川红茶电商助农系统 (Lichuan Black Tea E-commerce System)

本项目是基于 Spring Boot 3.x 和 Vue 3 的前后端分离电商系统，旨在通过互联网技术助力利川红茶销售，增加农户收入。

## 1. 技术栈
- **后端**: Spring Boot 3, Spring Data JPA, H2 Database, Lombok
- **前端**: Vue 3, Vite, Element Plus, Pinia, Axios, Vue Router

## 2. 目录结构
- `backend/`: 后端 Java 项目
- `frontend/`: 前端 Vue 项目
- `product_requirements_document.md`: 产品需求文档
- `technical_architecture_document.md`: 技术架构文档

## 3. 快速开始

### 3.1 后端启动
1. 确保已安装 JDK 17+ 和 Maven。
2. 进入 `backend` 目录。
3. 运行命令: `mvn spring-boot:run`
4. 服务将在 `http://localhost:8080` 启动。
5. H2 控制台: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:teadb`, User: `sa`, Password: 空)

### 3.2 前端启动
1. 确保已安装 Node.js (建议 v16+)。
2. 进入 `frontend` 目录。
3. 安装依赖: `npm install`
4. 启动开发服务器: `npm run dev`
5. 访问 `http://localhost:5173`。

## 4. 测试账号
- **管理员**: `admin` / `123456`
- **普通用户**: `user` / `123456`

## 5. 功能模块
- **首页**: 轮播图、助农商品展示（产地、农户）。
- **商品详情**: 商品介绍、加入购物车。
- **购物车**: 结算下单。
- **我的订单**: 查看订单状态。
- **后台管理**: 管理员登录后可管理商品（上架/下架）和订单（发货）。
