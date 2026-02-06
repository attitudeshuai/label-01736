# 研究生培养科研管理系统

## How to Run

### Docker 方式（推荐）

```bash
# 克隆项目后，在项目根目录执行
docker-compose up --build -d

# 等待服务启动完成（约2-3分钟）
# 查看日志确认启动状态
docker-compose logs -f

# 停止服务
docker-compose down

# 停止并清除数据
docker-compose down -v
```

### 本地开发方式

#### 1. 数据库准备
```bash
# 启动 MySQL 8.0，创建数据库
mysql -u root -p
CREATE DATABASE research_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 执行建表脚本
mysql -u root -p research_db < backend/src/main/resources/schema.sql
```

#### 2. 启动后端
```bash
cd backend
# 修改 application.yml 中的数据库连接配置
mvn spring-boot:run
```

#### 3. 启动前端
```bash
cd frontend-admin
npm install
npm run dev
```

## Services

| 服务 | 端口 | 说明 |
|------|------|------|
| frontend-admin | 8081 | 管理后台前端 |
| backend | 8080 | 后端 API 服务 |
| mysql | 3306 | MySQL 数据库 |

访问地址：http://localhost:8081

## 测试账号

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | admin | admin123 | 系统管理员，拥有所有权限 |
| 导师 | teacher01 | admin123 | 导师账号，可查看学生数据、审阅周报 |
| 学生 | student01 | admin123 | 学生账号，可管理自己的论文阅读、成果、周报 |

## 题目内容

为实现在校研究生日常科研的精细化、规范化管理，提出建设面向在校研究生培养阶段的日常科研工作信息化管理系统，以提升在校研究生培养过程中涉及的论文阅读记录、成果登记、周报等内容信息的管理水平。 现要求设计并开发实现研究生培养科研管理系统，应包括（但不限于）以下功能：

1. **论文阅读**：实现对研究生日常科研论文阅读的记录，论文信息应包含：阅读人、阅读时间、论文题目、关键字、来源、作者、第一单位、论文工作介绍、本文最大创新点或贡献、你的思考或本文有哪些缺点、论文原文（以附件形式上传），等。

2. **成果登记**：成果包括发表论文、专利、软件著作权等。

3. **周报信息**：完成研究生每周周报的填写，主要包括本周内开展的功能（按类别填写）、当前进展情况，下周计划安排。

4. **用户管理**：提供管理员、导师、学生等用户的管理功能。

技术栈要求：前端 Vue，后端 Spring Boot，数据库 MySQL，Docker 打包

---

## 系统功能

### 用户角色权限

| 功能模块 | 管理员 | 导师 | 学生 |
|---------|--------|------|------|
| 用户管理 | ✅ 增删改查 | ❌ | ❌ |
| 操作日志 | ✅ 查看 | ❌ | ❌ |
| 论文阅读 | ✅ 查看所有 | ✅ 查看学生 | ✅ 管理自己 |
| 成果登记 | ✅ 查看所有 | ✅ 查看学生 | ✅ 管理自己 |
| 周报管理 | ✅ 查看所有 | ✅ 查看/审阅学生 | ✅ 填写/提交 |

### 功能模块

#### 1. 论文阅读管理
- 记录论文阅读信息（题目、作者、来源、关键字等）
- 记录创新点、思考与缺点分析
- 支持论文原文附件上传
- 按关键字、题目、作者搜索

#### 2. 成果登记
- 支持三种成果类型：论文、专利、软著
- 记录发表/授权信息
- 成果统计展示
- 支持附件上传

#### 3. 周报管理
- 周报填写（本周工作、当前进展、下周计划）
- 周报状态流转：草稿 → 已提交 → 已审阅
- 导师审阅并添加评语
- 历史周报查询

#### 4. 用户管理（管理员）
- 用户增删改查
- 角色分配（管理员/导师/学生）
- 导师-学生关系绑定
- 账号启用/禁用

#### 5. 操作日志（管理员）
- 记录关键业务操作
- 日志查询与筛选

## 技术架构

### 后端技术栈
- Java 17
- Spring Boot 3.2.5
- MyBatis-Plus 3.5.5
- MySQL 8.0
- JWT 认证 (jjwt 0.12.5)
- AOP 操作日志

### 前端技术栈
- Vue 3.4
- Vite 5
- Element Plus 2.6
- Pinia 状态管理
- Vue Router 4
- Axios
- SCSS

### 部署架构
- Docker 容器化
- Docker Compose 编排
- Nginx 反向代理

## 项目结构

```
├── backend/                    # 后端项目
│   ├── src/main/java/         # Java 源码
│   │   └── com/graduate/research/
│   │       ├── annotation/    # 自定义注解
│   │       ├── aspect/        # AOP 切面
│   │       ├── common/        # 公共类
│   │       ├── config/        # 配置类
│   │       ├── controller/    # 控制器
│   │       ├── dto/           # 数据传输对象
│   │       ├── entity/        # 实体类
│   │       ├── exception/     # 异常处理
│   │       ├── interceptor/   # 拦截器
│   │       ├── mapper/        # MyBatis Mapper
│   │       ├── service/       # 服务层
│   │       └── util/          # 工具类
│   ├── src/main/resources/    # 资源文件
│   ├── Dockerfile
│   └── pom.xml
├── frontend-admin/            # 前端项目
│   ├── src/
│   │   ├── api/              # API 接口
│   │   ├── router/           # 路由配置
│   │   ├── stores/           # Pinia 状态
│   │   ├── styles/           # 样式文件
│   │   └── views/            # 页面组件
│   ├── Dockerfile
│   └── package.json
├── docs/                      # 文档
├── docker-compose.yml         # Docker 编排
├── .gitignore
└── README.md
```
