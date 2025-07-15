#!/bin/bash

# Fatcat数据备份脚本
# 用于备份MySQL数据库和重要文件

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
BACKUP_DIR="$PROJECT_DIR/backups"

# 创建备份目录
mkdir -p "$BACKUP_DIR"

# 获取当前时间戳
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

# 加载环境变量
if [ -f "$PROJECT_DIR/.env" ]; then
    source "$PROJECT_DIR/.env"
fi

# 设置默认值
MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD:-123456}
MYSQL_DATABASE=${MYSQL_DATABASE:-fatcat}

echo "开始备份 Fatcat 数据..."

# 1. 备份MySQL数据库
echo "备份MySQL数据库..."
DB_BACKUP_FILE="$BACKUP_DIR/mysql_${TIMESTAMP}.sql"

docker-compose -f "$PROJECT_DIR/docker-compose.yaml" exec -T mysql mysqldump \
    -u root -p"$MYSQL_ROOT_PASSWORD" \
    --single-transaction \
    --routines \
    --triggers \
    "$MYSQL_DATABASE" > "$DB_BACKUP_FILE"

if [ $? -eq 0 ]; then
    echo "✅ 数据库备份完成: $DB_BACKUP_FILE"
    # 压缩数据库备份
    gzip "$DB_BACKUP_FILE"
    echo "✅ 数据库备份已压缩: ${DB_BACKUP_FILE}.gz"
else
    echo "❌ 数据库备份失败"
    exit 1
fi

# 2. 备份配置文件
echo "备份配置文件..."
CONFIG_BACKUP_FILE="$BACKUP_DIR/config_${TIMESTAMP}.tar.gz"

tar -czf "$CONFIG_BACKUP_FILE" -C "$PROJECT_DIR" \
    .env \
    docker-compose.yaml \
    config/ \
    scripts/ \
    2>/dev/null || true

echo "✅ 配置文件备份完成: $CONFIG_BACKUP_FILE"

# 3. 备份上传文件（如果存在）
if [ -d "$PROJECT_DIR/uploads" ]; then
    echo "备份上传文件..."
    UPLOADS_BACKUP_FILE="$BACKUP_DIR/uploads_${TIMESTAMP}.tar.gz"
    tar -czf "$UPLOADS_BACKUP_FILE" -C "$PROJECT_DIR" uploads/
    echo "✅ 上传文件备份完成: $UPLOADS_BACKUP_FILE"
fi

# 4. 创建备份清单
MANIFEST_FILE="$BACKUP_DIR/backup_manifest_${TIMESTAMP}.txt"
cat > "$MANIFEST_FILE" << EOF
Fatcat 备份清单
================
备份时间: $(date)
备份版本: $TIMESTAMP

文件列表:
- mysql_${TIMESTAMP}.sql.gz (数据库备份)
- config_${TIMESTAMP}.tar.gz (配置文件备份)
EOF

if [ -f "$BACKUP_DIR/uploads_${TIMESTAMP}.tar.gz" ]; then
    echo "- uploads_${TIMESTAMP}.tar.gz (上传文件备份)" >> "$MANIFEST_FILE"
fi

echo "" >> "$MANIFEST_FILE"
echo "备份文件大小:" >> "$MANIFEST_FILE"
ls -lh "$BACKUP_DIR"/*_${TIMESTAMP}.* >> "$MANIFEST_FILE"

echo "✅ 备份清单创建完成: $MANIFEST_FILE"

# 5. 清理旧备份（保留最近7天）
echo "清理旧备份文件..."
find "$BACKUP_DIR" -name "*.sql.gz" -mtime +7 -delete 2>/dev/null || true
find "$BACKUP_DIR" -name "*.tar.gz" -mtime +7 -delete 2>/dev/null || true
find "$BACKUP_DIR" -name "*.txt" -mtime +7 -delete 2>/dev/null || true

echo ""
echo "🎉 备份完成！"
echo "备份文件位置: $BACKUP_DIR"
echo "备份标识: $TIMESTAMP"

# 显示备份目录内容
echo ""
echo "当前备份文件:"
ls -la "$BACKUP_DIR" | grep "$TIMESTAMP"
