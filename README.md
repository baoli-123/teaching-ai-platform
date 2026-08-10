# 双语教学资源平台（AI 教学场景解决方案）

一个面向高校师生的 AI 教学资源平台 Demo：涵盖课程资源管理、AI 智能问答、在线考试与学情统计。项目以 RAG 为核心实现 AI 问答，默认使用内置 H2 数据库和示例知识库，开箱即可运行。

## 核心功能

- 课程与教学资源管理：按课程查看课件、文档、视频等资源
- AI 智能问答：基于知识库检索的 RAG 问答，支持 WebSocket 对话
- 在线考试：课程题库、在线答题、自动判分、答题解析
- 学情统计：课程、资源、题目、问答和考试记录统计

## 当前实现范围

当前仓库是可在本地直接运行验证的 MVP 版本，已实现：

- Spring Boot 3.3 + MyBatis-Plus + H2
- 课程资源、题库、考试判分与学情看板
- RAG 知识检索问答与 WebSocket 实时对话
- Vue 3 前端页面与 REST API
- 可选 OpenAI 兼容大模型接口，无 Key 时可使用内置知识库回答

以下能力属于后续迭代方向，当前代码尚未实现，不会在 README 或简历中写成已上线能力：

- Spring Security + JWT + RBAC
- Redis 主从、Redisson 布隆过滤器、Lettuce 读写分离
- Redis + Lua 限流、Sentinel 熔断降级
- 浏览量与学习进度异步同步机制
- 流式首字响应、全链路审计、中英双语 i18n

## 技术栈

- Spring Boot 3.3
- MyBatis-Plus
- H2（Demo 数据库，可切换 MySQL）
- WebSocket
- Vue 3（CDN 引入）
- RAG：本地知识检索 + 可选 OpenAI 兼容大模型接口

## 快速启动

```bash
mvn spring-boot:run
```

启动后访问：

```text
http://localhost:8080
```

H2 控制台：

```text
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:teachingai
用户名: sa
密码: 空
```

## 可选大模型配置

默认在没有 API Key 时使用本地知识库生成回答。如需接入大模型，可配置环境变量：

```bash
OPENAI_API_KEY=你的Key
OPENAI_BASE_URL=https://api.openai.com/v1
```

## 主要接口

- `GET /api/courses`：课程列表
- `GET /api/courses/{courseId}/resources`：课程资源
- `GET /api/courses/{courseId}/questions`：课程题库
- `POST /api/exams/submit`：提交考试答案
- `GET /api/exams/records`：考试记录
- `GET /api/dashboard/stats`：学情统计
- `POST /api/chat`：AI 问答
- `WS /ws/chat`：AI 问答 WebSocket

## 项目结构

```text
src/main/java/com/example/teachingai
├── config          # WebSocket 配置
├── controller      # REST 接口
├── dto             # 请求与响应对象
├── entity          # 数据实体
├── mapper          # MyBatis-Plus Mapper
└── service         # 业务与 RAG 服务
src/main/resources
├── static          # 前端页面
├── application.yml # 应用配置
├── schema.sql      # 表结构
└── data.sql        # 示例数据
```

## 切换 MySQL

项目默认使用 H2 便于本地演示。生产环境可参考 `src/main/resources/application-mysql.yml` 配置 MySQL 数据源，并关闭 H2 控制台。
