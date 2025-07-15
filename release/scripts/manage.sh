#!/bin/bash

# Fatcat项目管理脚本
# 用于管理Docker容器的启动、停止、重启等操作

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

# 切换到项目目录
cd "$PROJECT_DIR"

# 显示帮助信息
show_help() {
    echo -e "${BLUE}Fatcat 项目管理脚本${NC}"
    echo ""
    echo "用法: $0 [命令]"
    echo ""
    echo "可用命令:"
    echo "  start     - 启动所有服务"
    echo "  stop      - 停止所有服务"
    echo "  restart   - 重启所有服务"
    echo "  status    - 查看服务状态"
    echo "  logs      - 查看日志 [服务名]"
    echo "  build     - 构建应用镜像"
    echo "  clean     - 清理停止的容器和未使用的镜像"
    echo "  reset     - 重置所有数据（危险操作）"
    echo "  backup    - 备份数据库"
    echo "  help      - 显示此帮助信息"
}

# 检查环境
check_environment() {
    if [ ! -f ".env" ]; then
        echo -e "${YELLOW}警告: .env 文件不存在，使用默认配置${NC}"
    fi
    
    if [ ! -f "docker-compose.yaml" ]; then
        echo -e "${RED}错误: docker-compose.yaml 文件不存在${NC}"
        exit 1
    fi
}

# 启动服务
start_services() {
    echo -e "${GREEN}启动 Fatcat 服务...${NC}"
    docker-compose up -d
    echo -e "${GREEN}服务启动完成！${NC}"
    show_status
}

# 停止服务
stop_services() {
    echo -e "${YELLOW}停止 Fatcat 服务...${NC}"
    docker-compose down
    echo -e "${GREEN}服务已停止${NC}"
}

# 重启服务
restart_services() {
    echo -e "${YELLOW}重启 Fatcat 服务...${NC}"
    docker-compose restart
    echo -e "${GREEN}服务重启完成！${NC}"
    show_status
}

# 查看状态
show_status() {
    echo -e "${BLUE}服务状态:${NC}"
    docker-compose ps
}

# 查看日志
show_logs() {
    if [ -n "$1" ]; then
        echo -e "${BLUE}查看 $1 服务日志:${NC}"
        docker-compose logs -f "$1"
    else
        echo -e "${BLUE}查看所有服务日志:${NC}"
        docker-compose logs -f
    fi
}

# 构建应用
build_app() {
    echo -e "${GREEN}构建 Fatcat 应用镜像...${NC}"
    docker-compose build fatcat
    echo -e "${GREEN}构建完成！${NC}"
}

# 清理资源
clean_resources() {
    echo -e "${YELLOW}清理 Docker 资源...${NC}"
    docker-compose down --remove-orphans
    docker system prune -f
    echo -e "${GREEN}清理完成！${NC}"
}

# 重置数据
reset_data() {
    echo -e "${RED}警告: 此操作将删除所有数据！${NC}"
    read -p "确认要重置所有数据吗？ (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo -e "${YELLOW}重置数据...${NC}"
        docker-compose down -v
        docker system prune -f --volumes
        echo -e "${GREEN}数据重置完成！${NC}"
    else
        echo -e "${GREEN}操作已取消${NC}"
    fi
}

# 备份数据库
backup_database() {
    echo -e "${GREEN}备份数据库...${NC}"
    BACKUP_FILE="backup/fatcat_backup_$(date +%Y%m%d_%H%M%S).sql"
    mkdir -p backup
    
    docker-compose exec mysql mysqldump -u root -p${MYSQL_ROOT_PASSWORD:-123456} ${MYSQL_DATABASE:-fatcat} > "$BACKUP_FILE"
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}数据库备份完成: $BACKUP_FILE${NC}"
    else
        echo -e "${RED}数据库备份失败${NC}"
    fi
}

# 主逻辑
main() {
    check_environment
    
    case "${1:-help}" in
        start)
            start_services
            ;;
        stop)
            stop_services
            ;;
        restart)
            restart_services
            ;;
        status)
            show_status
            ;;
        logs)
            show_logs "$2"
            ;;
        build)
            build_app
            ;;
        clean)
            clean_resources
            ;;
        reset)
            reset_data
            ;;
        backup)
            backup_database
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            echo -e "${RED}未知命令: $1${NC}"
            show_help
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"
