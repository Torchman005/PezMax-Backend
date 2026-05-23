<p align="center">
	<img alt="logo" src="https://github.com/Torchman005/PezMax-Backend/blob/main/ruoyi-ui/src/assets/logo/icon.ico">
</p>
<h1 align="center" style="margin: 30px 0 30px; font-weight: bold;">PezMax Backend v1.0.0</h1>
<h4 align="center">基于 Spring Boot 3/4 + JDK 17 的全栈开发框架（PezMax 项目后端）</h4>

<p align="center">
	<a href="https://gitee.com/y_project/RuoYi-Vue/stargazers"><img src="https://gitee.com/y_project/RuoYi-Vue/badge/star.svg?theme=dark"></a>
	<a href="https://gitee.com/y_project/RuoYi-Vue"><img src="https://img.shields.io/badge/PezMax-v1.0.0-brightgreen.svg"></a>
	<a href="https://gitee.com/y_project/RuoYi-Vue/blob/master/LICENSE"><img src="https://img.shields.io/github/license/mashape/apistatus.svg"></a>
</p>

## 平台简介

PezMax Backend 是 [PezMax](https://github.com/itJinYu-toolkit/PezMax) 生态系统的后端服务核心。它基于成熟的 RuoYi-Vue 架构进行深度定制，专为 PezMax 桌面端（Vue + Electron）提供高性能、高可靠的数据支持。

*   **核心引擎**: Spring Boot 4.0.3 / JDK 17
*   **权限安全**: Spring Security + JWT + Redis
*   **对象存储**: 集成 MinIO 支持海量文件存储
*   **文档处理**: 内置 LibreOffice 服务，支持文档预览与转换
*   **容器化**: 完整的 Docker Compose 一键部署方案

## 核心业务功能 (ptmj-datum)

1.  **书签管理**: 收藏、分类、分享网页书签，支持封面自动抓取。
2.  **文件管理**: 多格式文件上传、下载、在线预览，支持大文件切片。
3.  **用户体系**: 完善的用户等级、积分、个人空间管理。
4.  **互动通知**: 系统消息、审核结果实时通知。
5.  **数据统计**: 热门资源排行、用户活跃度统计。

## 技术栈

| 技术 | 说明 |
| :--- | :--- |
| Spring Boot | 核心应用框架 |
| Spring Security | 安全与权限控制 |
| MyBatis | 持久层框架 |
| MySQL 8.0 | 关系型数据库 |
| Redis | 缓存与消息队列 |
| MinIO | 分布式对象存储 |
| LibreOffice | 文档处理引擎 |
| Druid | 数据库连接池 |
| Docker | 容器化部署 |

## 快速开始

### 1. Docker Compose 一键部署 (推荐)

项目根目录已提供完整的部署脚本：

```bash
# 启动所有服务 (MySQL, Redis, MinIO, Server)
docker compose up -d

# 查看运行状态
docker compose ps

# 查看后端日志
docker compose logs -f server
```

**端口开放说明**:
- 后端 API: `8080` (必须)
- MinIO API: `9000` (必须)
- MinIO Console: `9001` (管理用)
- MySQL: `3306` (仅调试)
- Redis: `6379` (仅调试)

### 2. 本地开发调试

1.  **环境要求**: JDK 17, Maven 3.6+, MySQL 8.0, Redis.
2.  **数据库初始化**: 执行 `sql/pezmax.sql` 脚本。
3.  **修改配置**: 编辑 `ruoyi-admin/src/main/resources/application-druid.yml` 修改数据库连接。
4.  **启动服务**: 运行 `com.ruoyi.RuoYiApplication`。

## 项目结构

```text
pezmax-backend
├── ptmj-datum      // PezMax 核心业务模块 (书签、文件、用户等)
├── ruoyi-admin      // 后端 Web 入口、Controller
├── ruoyi-common     // 公共工具类、通用组件
├── ruoyi-framework  // 框架配置、权限拦截 (Spring Security)
├── ruoyi-generator  // 代码生成工具
├── ruoyi-quartz     // 定时任务
├── ruoyi-system     // 系统基础管理 (用户、角色、菜单、字典)
├── compose.yaml     // Docker Compose 定义
└── Dockerfile       // 后端镜像构建文件
```

## 注意事项

- **匿名访问**: 书签和文件的查询接口（list, getInfo, tree）已添加 `@Anonymous` 注解，支持未登录状态下访问。
- **文件存储**: 默认使用 MinIO，请确保 `minio` 容器状态正常且桶策略已配置为公开读。
- **构建规范**: 使用 Docker 构建时，必须执行 `mvn clean package` 以确保依赖更新。

## 演示与文档

- **演示地址**: http://vue.ruoyi.vip (基座演示)
- **开发文档**: http://doc.ruoyi.vip

---

<p align="center">感谢 RuoYi 开源社区提供的卓越底座支持。</p>
