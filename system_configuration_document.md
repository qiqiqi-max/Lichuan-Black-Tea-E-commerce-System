# 系统配置说明文档

项目：利川红茶助农平台  
文档版本：`v1.0`  
更新时间：`2026-05-17`

## 1. 文档说明

本文档用于说明项目当前的系统配置方式、配置文件位置、默认运行参数及开发/生产环境差异。  
配置内容以代码仓库中的实际文件为准，适用于本项目当前前后端分离架构。

## 2. 系统配置概览

- 前端：`Vue 3 + Vite`
- 后端：`Spring Boot 2.7.18`
- 数据库：`MySQL 8`
- 缓存：`Redis`
- 默认开发环境：后端 `dev` 配置

## 3. 配置文件位置

### 3.1 后端配置文件

- `backend/src/main/resources/application.yml`
  - 公共基础配置
  - 指定默认激活环境
  - 配置服务端口与文件上传大小限制
- `backend/src/main/resources/application-dev.yml`
  - 开发环境数据库配置
  - 开发环境 Redis 配置
  - JPA 开发策略
- `backend/src/main/resources/application-prod.yml`
  - 生产环境数据库配置
  - 生产环境端口
  - 生产环境 JPA 校验策略

### 3.2 前端配置文件

- `frontend/vite.config.js`
  - Vite 开发服务器配置
  - `/api` 代理转发配置
  - 路径别名 `@`
- `frontend/package.json`
  - 前端启动、构建、预览脚本
  - 前端依赖版本定义

### 3.3 启动与辅助脚本

- `backend/scripts/start-dev.ps1`
  - 开发环境后端启动脚本
  - 支持端口占用检测
  - 支持自动释放被占用端口
- `init.sql`
  - 数据库初始化脚本

## 4. 后端系统配置

### 4.1 公共配置

文件：`backend/src/main/resources/application.yml`

- 服务端口：`8080`
- Spring 应用名：`tea-backend`
- 默认激活环境：`dev`
- 单文件上传限制：`5MB`
- 单次请求上传总限制：`5MB`

对应配置摘要：

```yaml
server:
  port: 8080

spring:
  application:
    name: tea-backend
  profiles:
    active: dev
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 5MB
```

### 4.2 开发环境配置

文件：`backend/src/main/resources/application-dev.yml`

#### 数据库配置

- 数据库地址：`jdbc:mysql://localhost:3306/lichuan_tea_db`
- 驱动：`com.mysql.cj.jdbc.Driver`
- 用户名：`root`
- 密码：`123456`
- 时区：`Asia/Shanghai`
- 字符集：`utf8`

#### JPA 配置

- 方言：`org.hibernate.dialect.MySQL8Dialect`
- 表结构策略：`update`
- SQL 日志：`false`

#### Redis 配置

- 主机：`localhost`
- 端口：`6379`
- 数据库：`0`
- Redis Repository：关闭

说明：

- `ddl-auto=update` 适合开发环境自动更新表结构。
- Redis 已接入配置，但从当前项目结构看，业务使用较少。

### 4.3 生产环境配置

文件：`backend/src/main/resources/application-prod.yml`

#### 服务配置

- 服务端口：`80`

#### 数据库配置

- 数据库地址变量：`DB_URL`
- 数据库用户名变量：`DB_USERNAME`
- 数据库密码变量：`DB_PASSWORD`

默认回退值：

- `DB_URL` 未设置时，默认连接：`jdbc:mysql://localhost:3306/lichuan_tea_db?...`
- `DB_USERNAME` 未设置时，默认：`root`
- `DB_PASSWORD` 未设置时，默认：`YOUR_PASSWORD`

#### JPA 配置

- 方言：`org.hibernate.dialect.MySQL8Dialect`
- 表结构策略：`validate`
- SQL 日志：`false`

说明：

- 生产环境使用 `validate`，只校验表结构，不自动改表。
- 部署前应显式设置 `DB_PASSWORD`，不要保留默认占位值。

## 5. 前端系统配置

### 5.1 Vite 开发服务器配置

文件：`frontend/vite.config.js`

- 前端开发服务器默认端口通常为：`5173`
- 接口代理前缀：`/api`
- 代理目标：`http://localhost:8080`
- 代理选项：`changeOrigin: true`
- 路径别名：`@ -> frontend/src`

说明：

- 前端本地开发时，请求 `/api/*` 会被自动转发到后端。
- 这意味着前端代码中通常不需要直接写完整后端域名。

### 5.2 前端脚本配置

文件：`frontend/package.json`

可用脚本：

- `npm run dev`
  - 启动前端开发环境
- `npm run build`
  - 构建生产包
- `npm run preview`
  - 预览构建结果

主要依赖：

- `vue`
- `vue-router`
- `pinia`
- `axios`
- `element-plus`
- `echarts`
- `vite`

## 6. 端口与网络配置

当前默认端口分配如下：

- 前端开发端口：`5173`
- 后端开发端口：`8080`
- 后端生产端口：`80`
- Redis 端口：`6379`
- MySQL 端口：`3306`

联调关系如下：

1. 浏览器访问前端 `Vite` 开发服务器。
2. 前端请求 `/api/*`。
3. `Vite` 将请求代理到 `http://localhost:8080`。
4. 后端处理后返回统一结果。

## 7. 文件上传与静态资源配置

后端当前上传限制：

- 单文件最大：`5MB`
- 单请求最大：`5MB`

上传文件目录：

- 商品图片：`backend/uploads/product-images`
- 茶农故事图片：`backend/uploads/farmer-stories`

说明：

- 上传目录属于运行期资源目录，部署时应保证程序具有读写权限。
- 如果部署在服务器，建议将上传目录映射到持久化存储位置。

## 8. 开发环境启动配置

### 8.1 后端启动

推荐方式：

```powershell
cd backend
powershell -ExecutionPolicy Bypass -File .\scripts\start-dev.ps1 -KillPortOwner
```

脚本行为：

- 检查 `8080` 端口是否被占用
- 如传入 `-KillPortOwner`，自动结束占用进程
- 使用 `dev` 环境启动 Spring Boot

等价备用方式：

```bash
cd backend
mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=dev"
```

### 8.2 前端启动

```bash
cd frontend
npm install
npm run dev -- --host 0.0.0.0 --port 5173
```

访问地址：

- 前端：`http://localhost:5173`
- 后端：`http://localhost:8080`

## 9. 生产部署配置建议

- 使用 `application-prod.yml` 作为生产配置基线。
- 通过环境变量设置数据库连接信息，不要把生产密码写死在仓库中。
- 将后端端口、数据库、Redis、上传目录权限纳入部署检查项。
- 若前后端分离部署到不同域名，需同步收紧并调整 CORS 配置。
- 建议为 MySQL、Redis 和上传目录配置备份与持久化策略。

## 10. 当前配置风险与注意事项

- 开发环境数据库账号密码当前为明文默认值：`root / 123456`。
- 生产环境密码虽然支持环境变量，但默认占位值仍需手动替换。
- Redis 已配置接入，但是否启用具体业务能力需要结合代码继续确认。
- 上传目录位于项目内部，若直接删除工作目录，上传资源会一并丢失。
- 当前文档只覆盖仓库内显式配置，不包含操作系统层面的 Java、Node、Maven 安装配置。

## 11. 相关参考文档

仓库内已有与配置相关但不是独立“系统配置说明”的文档：

- `README.md`
- `technical_architecture_document.md`

这两份文档提供了运行说明和技术架构背景，可作为本文档的补充阅读材料。
