#!/bin/bash

# SSL证书生成脚本
# 为Fatcat项目生成自签名SSL证书

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SSL_DIR="$(dirname "$SCRIPT_DIR")/ssl"

# 创建SSL目录
mkdir -p "$SSL_DIR"

# 证书信息
COUNTRY="CN"
STATE="Beijing"
CITY="Beijing"
ORGANIZATION="Fatcat"
ORGANIZATIONAL_UNIT="IT Department"
COMMON_NAME="localhost"
EMAIL="admin@fatcat.local"

echo "生成SSL证书..."

# 生成私钥
openssl genrsa -out "$SSL_DIR/fatcat.key" 2048

# 生成证书签名请求
openssl req -new -key "$SSL_DIR/fatcat.key" -out "$SSL_DIR/fatcat.csr" -subj "/C=$COUNTRY/ST=$STATE/L=$CITY/O=$ORGANIZATION/OU=$ORGANIZATIONAL_UNIT/CN=$COMMON_NAME/emailAddress=$EMAIL"

# 生成自签名证书
openssl x509 -req -days 365 -in "$SSL_DIR/fatcat.csr" -signkey "$SSL_DIR/fatcat.key" -out "$SSL_DIR/fatcat.crt"

# 生成组合证书文件
cat "$SSL_DIR/fatcat.crt" "$SSL_DIR/fatcat.key" > "$SSL_DIR/fatcat.pem"

# 设置适当的权限
chmod 600 "$SSL_DIR"/*.key
chmod 644 "$SSL_DIR"/*.crt "$SSL_DIR"/*.pem

echo "SSL证书生成完成！"
echo "证书文件位置: $SSL_DIR"
echo "  - 私钥: fatcat.key"
echo "  - 证书: fatcat.crt"
echo "  - 组合文件: fatcat.pem"
echo ""
echo "注意: 这是自签名证书，仅用于开发和测试环境"
