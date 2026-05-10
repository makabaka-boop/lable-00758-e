# 短视频浏览系统

## 自动化测试方案

本项目已实现完整的前后端自动化测试方案，支持通过 Docker 一键执行所有测试。

### 快速开始 - 一键运行所有测试

在项目根目录执行以下命令即可运行所有测试：

```bash
# 运行所有测试（后端 + 前端）
./run-tests.sh
```

### 单独运行测试

```bash
# 仅运行后端测试
./run-backend-tests.sh

# 仅运行前端测试
./run-frontend-tests.sh
```

### 使用 Docker Compose 直接运行

```bash
# 运行后端测试
docker-compose -f docker-compose.test.yml up --build --abort-on-container-exit backend-test

# 运行前端测试
docker-compose -f docker-compose.test.yml up --build --abort-on-container-exit frontend-test

# 清理测试环境
docker-compose -f docker-compose.test.yml down -v
```

### 本地直接运行测试（不使用 Docker）

#### 后端测试

```bash
cd backend
mvn test
```

#### 前端测试

```bash
cd frontend
npm install
npm run test

# 生成测试覆盖率报告
npm run test:coverage
```

### 测试覆盖范围

#### 后端测试（Spring Boot + JUnit 5 + Mockito）

##### 单元测试
- **UserServiceTest** (`backend/src/test/java/com/shortvideo/service/UserServiceTest.java`)
  - 登录功能（成功/失败/密码错误）
  - 用户注册（成功/用户名已存在/默认昵称）
  - 用户查询（存在/不存在）

- **VideoServiceTest** (`backend/src/test/java/com/shortvideo/service/VideoServiceTest.java`)
  - 视频上传（成功/文件校验/类型校验/大小校验/标题校验）
  - 视频列表获取（带/不带用户ID）
  - 点赞功能（首次点赞/取消点赞）
  - 收藏功能（首次收藏/取消收藏）
  - 评论功能（添加评论/获取评论列表）
  - 播放量统计
  - 视频删除

- **JwtUtilTest** (`backend/src/test/java/com/shortvideo/util/JwtUtilTest.java`)
  - Token 生成与解析
  - Token 有效性验证
  - 管理员 Token 测试

##### 接口层测试
- **UserControllerTest** (`backend/src/test/java/com/shortvideo/controller/UserControllerTest.java`)
  - 登录接口（成功/失败/参数校验）
  - 管理员登录测试

- **VideoControllerTest** (`backend/src/test/java/com/shortvideo/controller/VideoControllerTest.java`)
  - 视频列表接口
  - 点赞/收藏接口
  - 评论接口
  - 视频上传/删除接口
  - 参数校验测试
  - 异常处理测试

##### 拦截器与异常处理测试
- **JwtInterceptorTest** (`backend/src/test/java/com/shortvideo/interceptor/JwtInterceptorTest.java`)
  - OPTIONS 请求放行
  - Token 验证（无Token/格式错误/无效Token/有效Token）
  - 用户ID注入测试

- **GlobalExceptionHandlerTest** (`backend/src/test/java/com/shortvideo/exception/GlobalExceptionHandlerTest.java`)
  - 业务异常处理
  - 参数校验异常处理
  - 文件上传异常处理
  - 未知系统异常处理

#### 前端测试（Vue 3 + Vitest + Vue Test Utils）

##### API 封装测试
- **api.test.js** (`frontend/src/api/api.test.js`)
  - 所有 API 方法的调用验证
  - 请求参数传递测试

##### 路由测试
- **router.test.js** (`frontend/src/router/router.test.js`)
  - 路由配置验证
  - 重定向规则测试

##### 组件测试
- **Login.test.js** (`frontend/src/views/Login.test.js`)
  - 表单渲染测试
  - 前端表单校验
  - 登录功能（普通用户/管理员）
  - 错误处理（登录失败/网络错误）
  - 加载状态测试

- **Admin.test.js** (`frontend/src/views/Admin.test.js`)
  - 权限控制（未登录/非管理员/管理员）
  - 视频列表加载
  - 视频删除功能
  - 退出登录功能
  - 上传弹窗控制

- **Mobile.test.js** (`frontend/src/views/Mobile.test.js`)
  - 视频列表加载
  - 点赞功能（未登录提示/已登录点赞）
  - 收藏功能（未登录提示/已登录收藏）
  - 评论功能（查看/发表）
  - 登录状态显示

### 测试技术栈

| 层级 | 技术 |
|------|------|
| 后端单元测试 | JUnit 5 + Mockito |
| 后端接口测试 | Spring Boot Test + MockMvc |
| 测试数据库 | H2 内存数据库 |
| 前端单元测试 | Vitest |
| 前端组件测试 | Vue Test Utils |
| 测试环境 | jsdom |
| 容器化 | Docker + Docker Compose |

### 测试报告

- 后端测试报告：`backend/target/surefire-reports/`
- 前端覆盖率报告：`frontend/coverage/`

---

## How to Run

### 使用 Docker Compose 运行（推荐）

```bash
# 克隆项目后，在根目录执行
docker-compose up --build -d

# 查看运行状态
docker-compose ps

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down

# 清理数据重新初始化
docker-compose down -v && docker-compose up --build -d
```

### 访问地址

| 页面 | 地址 |
|------|------|
| 移动端前台 | http://localhost:8081/mobile |
| PC端管理后台 | http://localhost:8081/admin |
| 登录页面 | http://localhost:8081/login |

## Services

| 服务 | 端口 | 说明 |
|------|------|------|
| Frontend (Vue 3) | 8081 | 前端服务，包含移动端和管理后台 |
| Backend (Spring Boot) | 8080 | 后端 API 服务 |
| MySQL | 3307 | 数据库服务（映射到主机 3307 端口） |

## 测试账号

| 角色 | 用户名 | 密码 | 用途 |
|------|--------|------|------|
| 管理员 | admin | admin123 | PC 端后台管理 |
| 普通用户 | user | user123 | 移动端浏览视频 |

## 题目内容

开发一个移动 web 端的短视频浏览系统，类似抖音、快手的功能。在 PC 端的管理后台可以上传短视频，在移动端用户前台可以刷短视频观看，短视频支持点赞、收藏和评论功能。数据库使用本地 MySQL 数据库，后端使用 Java，前端使用 Vue。

---

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + Vite + Vue Router + Axios |
| 后端 | Spring Boot 2.7 + Spring Data JPA + Spring Validation |
| 安全 | JWT 认证 + BCrypt 密码加密 |
| 数据库 | MySQL 8.0 |
| 容器化 | Docker + Docker Compose |

## 功能特性

### PC 端管理后台
- 管理员登录认证
- 上传短视频（支持标题、描述、文件校验）
- 视频列表管理
- 删除视频

### 移动端用户前台
- 用户登录/退出
- 上下滑动浏览短视频（支持触摸和鼠标滚轮）
- 视频自动播放
- 点赞功能
- 收藏功能
- 评论功能

## 安全特性

- **JWT 认证**: 登录返回 Token，需认证接口自动验证
- **密码加密**: 使用 BCrypt 加密存储
- **操作日志**: 记录关键操作（登录、上传、点赞、收藏、评论、删除）
- **参数校验**: 前后端双重校验
- **文件上传校验**: 校验文件类型（MP4/WebM/OGG）、大小（最大 100MB）
- **统一异常处理**: 友好的错误提示，隐藏敏感信息

## API 接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/user/login | 用户登录 | 否 |
| GET | /api/video/list | 获取视频列表 | 否 |
| GET | /api/video/comments/{id} | 获取评论列表 | 否 |
| POST | /api/video/view/{id} | 增加播放量 | 否 |
| POST | /api/video/like | 点赞/取消点赞 | 是 |
| POST | /api/video/favorite | 收藏/取消收藏 | 是 |
| POST | /api/video/comment | 发表评论 | 是 |
| POST | /api/video/upload | 上传视频 | 是 |
| GET | /api/video/admin/list | 管理员获取视频列表 | 是 |
| DELETE | /api/video/admin/{id} | 删除视频 | 是 |

## 项目结构

```
.
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/com/shortvideo/
│   │   ├── Application.java    # 启动类
│   │   ├── annotation/         # 自定义注解
│   │   │   └── Log.java        # 操作日志注解
│   │   ├── aspect/             # 切面
│   │   │   └── LogAspect.java  # 日志切面
│   │   ├── config/             # 配置类
│   │   │   └── WebConfig.java  # Web 配置
│   │   ├── controller/         # 控制器
│   │   ├── dto/                # 数据传输对象
│   │   ├── entity/             # 实体类
│   │   ├── exception/          # 异常处理
│   │   │   ├── BusinessException.java
│   │   │   └── GlobalExceptionHandler.java
│   │   ├── interceptor/        # 拦截器
│   │   │   └── JwtInterceptor.java
│   │   ├── repository/         # 数据访问层
│   │   ├── service/            # 业务逻辑层
│   │   └── util/               # 工具类
│   │       └── JwtUtil.java
│   ├── src/main/resources/
│   │   ├── application.yml     # 应用配置
│   │   └── data.sql            # 初始化数据
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                   # Vue 3 前端
│   ├── src/
│   │   ├── api/index.js        # API 封装
│   │   ├── router/             # 路由配置
│   │   ├── views/
│   │   │   ├── Admin.vue       # 管理后台
│   │   │   ├── Login.vue       # 登录页
│   │   │   └── Mobile.vue      # 移动端视频浏览
│   │   ├── App.vue
│   │   ├── main.js
│   │   └── style.css
│   ├── Dockerfile
│   ├── nginx.conf
│   └── package.json
├── docker-compose.yml
└── README.md
```

## 数据库表结构

| 表名 | 说明 |
|------|------|
| users | 用户表 |
| videos | 视频表 |
| video_likes | 点赞记录表 |
| video_favorites | 收藏记录表 |
| comments | 评论表 |
| operation_logs | 操作日志表 |

## Docker 配置

- 多阶段构建，支持 ARM 和 X86 架构
- 前端：Node 18 构建 + Nginx 运行
- 后端：Maven 构建 + Eclipse Temurin JRE 11 运行
- 数据库：MySQL 8.0，支持健康检查
- 数据持久化：MySQL 数据和视频文件使用 Docker Volume
