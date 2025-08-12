# Fatcat 部署目录

这是 Fatcat 博客平台的 Docker 部署目录，经过完整重构，提供了清晰的目录结构和完善的管理工具。

## 🗂️ 目录结构说明

```
release/
├── 🐳 Docker 配置
│   ├── docker-compose.yaml     # 主要的容器编排配置
│   ├── Dockerfile             # 应用容器构建文件
│   ├── .env                   # 正式环境变量配置文件
│   ├── .env.template          # 环境变量模板文件
│   └── .env.example          # 环境变量示例文件
│
├── 📁 配置文件 (config/)
│   ├── mysql/
│   │   └── my.cnf            # MySQL 数据库配置
│   ├── nginx/
│   │   └── nginx.conf        # Web 服务器配置
│   └── elasticsearch/
│       └── elasticsearch.yml  # 搜索引擎配置
│
├── 🛠️ 管理脚本 (scripts/)
│   ├── manage.sh             # 🎯 主管理脚本（推荐使用）
│   ├── backup.sh             # 💾 数据备份脚本
│   ├── restore.sh            # 🔄 数据恢复脚本
│   ├── generate-ssl.sh       # 🔒 SSL证书生成脚本
│   ├── fatcat-start.sh       # ▶️ 项目启动脚本
│   ├── replace-vue.sh        # 🔁 前端文件替换脚本
│   └── restart.sh            # 🔄 重启脚本
│
├── 📂 静态文件和资源
│   ├── vue/                  # 前端构建文件
│   │   ├── admin/           # 管理后台文件
│   │   └── blog/            # 博客前台文件
│   ├── ssl/                 # SSL证书存放目录
│   ├── logs/                # 日志文件目录
│   └── tmp-files/           # 临时文件目录
│
├── 🗄️ 数据文件
│   ├── dawn.sql           # 数据库初始化脚本
│   └── backups/             # 备份文件目录（运行时创建）
│
└── 📝 文档
    ├── README-Docker.md     # 详细的Docker使用说明
    └── .gitignore          # Git忽略文件配置
```

## 🚀 快速开始

### 1️⃣ 基础启动

```bash
# 进入部署目录
cd release/

# 使用管理脚本启动所有服务
./scripts/manage.sh start

# 检查服务状态
./scripts/manage.sh status
```

### 2️⃣ 高级管理

```bash
# 查看所有可用命令
./scripts/manage.sh help

# 查看实时日志
./scripts/manage.sh logs

# 备份数据
./scripts/backup.sh

# 恢复数据
./scripts/restore.sh
```

## 🔧 重构改进

### ✅ 结构化组织
- **配置集中化**: 所有配置文件统一放在 `config/` 目录
- **脚本模块化**: 管理脚本按功能分类，便于维护
- **资源分离**: 日志、备份、SSL证书等分别存放

### ✅ 安全增强
- **环境变量**: 敏感信息通过 `.env` 文件管理
- **SSL支持**: 提供证书生成脚本
- **权限控制**: 脚本文件已设置适当执行权限

### ✅ 运维友好
- **一键管理**: `manage.sh` 脚本提供所有常用操作
- **自动备份**: 完整的备份恢复机制
- **健康检查**: 所有服务都配置了健康监测

### ✅ 开发友好
- **无资源限制**: 移除了CPU和内存限制，适合开发环境
- **清晰命名**: 所有容器和服务使用 `fatcat-` 前缀
- **详细文档**: 提供完整的使用说明

## 🌟 主要特性

- **🐳 Docker 容器化**: 完整的微服务架构
- **🔄 自动恢复**: 服务异常时自动重启
- **💾 数据持久化**: 使用Docker卷保证数据安全
- **🔍 健康监测**: 实时监控所有服务状态
- **🛡️ 安全配置**: 支持HTTPS和安全认证
- **📊 日志管理**: 集中化日志收集和查看
- **⚡ 性能优化**: 缓存、索引等性能优化配置

## 📞 获取帮助

- **详细文档**: 查看 `README-Docker.md`
- **脚本帮助**: 运行 `./scripts/manage.sh help`
- **故障排除**: 检查日志文件或运行状态检查

---

*此目录已完成重构优化，提供了生产级的部署解决方案。*
