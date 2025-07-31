PROJECT_PATH="$(cd "./../.." && pwd)"
RELEASE_PATH=$PROJECT_PATH/release
echo "Project path: $PROJECT_PATH"
echo "Release path: $RELEASE_PATH"
cd $PROJECT_PATH/aurora-vue/aurora-admin
npm install
npm run build

cd $PROJECT_PATH/aurora-vue/aurora-blog
npm install
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
cp -r $PROJECT_PATH/aurora-vue/aurora-admin/dist/* "$RELEASE_PATH/vue/admin"
cp -r $PROJECT_PATH/aurora-vue/aurora-blog/dist/* "$RELEASE_PATH/vue/blog"
echo "successfully copied vue files to $RELEASE_PATH/vue"