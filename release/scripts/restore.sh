#!/bin/bash

# Fatcat数据恢复脚本
# 用于从备份恢复MySQL数据库

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
BACKUP_DIR="$PROJECT_DIR/backups"

# 显示帮助信息
show_help() {
    echo "Fatcat 数据恢复脚本"
    echo ""
    echo "用法: $0 [备份文件]"
    echo ""
    echo "示例:"
    echo "  $0 mysql_20240715_143000.sql.gz"
    echo "  $0 /path/to/backup.sql"
    echo ""
    echo "如果不指定备份文件，将显示可用的备份列表"
}

# 列出可用备份
list_backups() {
    echo "可用的数据库备份文件:"
    echo "========================="
    
    if [ -d "$BACKUP_DIR" ]; then
        find "$BACKUP_DIR" -name "mysql_*.sql.gz" -o -name "*.sql" | sort -r | while read file; do
            size=$(ls -lh "$file" | awk '{print $5}')
            date=$(stat -c %y "$file" | cut -d' ' -f1,2 | cut -d'.' -f1)
            echo "  $(basename "$file") ($size, $date)"
        done
    else
        echo "  备份目录不存在: $BACKUP_DIR"
    fi
    
    echo ""
    echo "使用方法: $0 <备份文件名>"
}

# 恢复数据库
restore_database() {
    local backup_file="$1"
    
    # 检查文件是否存在
    if [ ! -f "$backup_file" ]; then
        # 尝试在备份目录中查找
        if [ -f "$BACKUP_DIR/$backup_file" ]; then
            backup_file="$BACKUP_DIR/$backup_file"
        else
            echo "❌ 备份文件不存在: $backup_file"
            exit 1
        fi
    fi
    
    echo "恢复数据库从: $backup_file"
    
    # 加载环境变量
    if [ -f "$PROJECT_DIR/.env" ]; then
        source "$PROJECT_DIR/.env"
    fi
    
    # 设置默认值
    MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD:-123456}
    MYSQL_DATABASE=${MYSQL_DATABASE:-fatcat}
    
    # 确认操作
    echo ""
    echo "⚠️  警告: 此操作将覆盖当前数据库中的所有数据！"
    echo "数据库: $MYSQL_DATABASE"
    echo "备份文件: $backup_file"
    echo ""
    read -p "确认要继续吗？ (y/N): " -n 1 -r
    echo
    
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "操作已取消"
        exit 0
    fi
    
    echo "开始恢复数据库..."
    
    # 检查Docker服务是否运行
    if ! docker-compose -f "$PROJECT_DIR/docker-compose.yaml" ps mysql | grep -q "Up"; then
        echo "启动MySQL服务..."
        docker-compose -f "$PROJECT_DIR/docker-compose.yaml" up -d mysql
        
        echo "等待MySQL服务启动..."
        sleep 10
        
        # 等待MySQL准备就绪
        for i in {1..30}; do
            if docker-compose -f "$PROJECT_DIR/docker-compose.yaml" exec mysql mysqladmin ping -u root -p"$MYSQL_ROOT_PASSWORD" >/dev/null 2>&1; then
                break
            fi
            echo "等待MySQL启动... ($i/30)"
            sleep 2
        done
    fi
    
    # 恢复数据库
    if [[ "$backup_file" == *.gz ]]; then
        echo "解压并恢复压缩的备份文件..."
        zcat "$backup_file" | docker-compose -f "$PROJECT_DIR/docker-compose.yaml" exec -T mysql mysql -u root -p"$MYSQL_ROOT_PASSWORD" "$MYSQL_DATABASE"
    else
        echo "恢复备份文件..."
        cat "$backup_file" | docker-compose -f "$PROJECT_DIR/docker-compose.yaml" exec -T mysql mysql -u root -p"$MYSQL_ROOT_PASSWORD" "$MYSQL_DATABASE"
    fi
    
    if [ $? -eq 0 ]; then
        echo "✅ 数据库恢复完成！"
        echo ""
        echo "建议重启应用服务以确保数据一致性:"
        echo "  cd $PROJECT_DIR && ./scripts/manage.sh restart"
    else
        echo "❌ 数据库恢复失败"
        exit 1
    fi
}

# 主逻辑
main() {
    case "${1:-help}" in
        -h|--help|help)
            show_help
            ;;
        "")
            list_backups
            ;;
        *)
            restore_database "$1"
            ;;
    esac
}

# 执行主函数
main "$@"
