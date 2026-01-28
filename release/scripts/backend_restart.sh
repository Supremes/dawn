PROJECT_PATH="$(cd "./../.." && pwd)"
RELEASE_PATH=$PROJECT_PATH/release
echo "Project path: $PROJECT_PATH"
echo "Release path: $RELEASE_PATH"

cd $PROJECT_PATH/dawn-springboot
if mvn clean package -T 1C -DskipTests; then
    echo "✅ Maven 构建成功"
else
    echo "❌ Maven 构建失败"
    exit 1
fi

cd $RELEASE_PATH
cp ../dawn-springboot/target/dawn-springboot-1.0.jar .

if docker compose up -d --build dawn; then
    echo "✅ 成功重新创建并运行主程序 dawn"
else
    echo "❌ Docker Compose 启动失败"
    exit 1
fi