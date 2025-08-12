PROJECT_PATH="$(cd "./../.." && pwd)"
RELEASE_PATH=$PROJECT_PATH/release
echo "Project path: $PROJECT_PATH"
echo "Release path: $RELEASE_PATH"
cd $PROJECT_PATH/dawn-vue/dawn-admin
npm install --no-audit
npm run build

cd $PROJECT_PATH/dawn-vue/dawn-blog
npm install --no-audit
npm run build

# 删除旧的vue目录
if [ -d  "$RELEASE_PATH/vue" ]; then
    echo "rm -rf $RELEASE_PATH/vue"
    rm -rf "$RELEASE_PATH/vue"
else
    echo "vue directory does not exist, no need to delete"
fi

# 复制新的vue目录
mkdir -p "$RELEASE_PATH/vue/admin"
mkdir -p "$RELEASE_PATH/vue/blog"
cp -r $PROJECT_PATH/dawn-vue/dawn-admin/dist/* "$RELEASE_PATH/vue/admin"
cp -r $PROJECT_PATH/dawn-vue/dawn-blog/dist/* "$RELEASE_PATH/vue/blog"
echo "successfully copied vue files to $RELEASE_PATH/vue"

cd $RELEASE_PATH
# 重启nginx服务，会重新读取配置
# 停止并删除nginx容器，然后重新创建
docker compose down nginx
docker compose up -d nginx
echo "Nginx restarted successfully"