PROJECT_PATH="$(cd "./../.." && pwd)"
RELEASE_PATH=$PROJECT_PATH/release
echo "Project path: $PROJECT_PATH"
echo "Release path: $RELEASE_PATH"

cd $PROJECT_PATH/aurora-springboot
mvn clean package
cd $RELEASE_PATH
cp ../aurora-springboot/target/aurora-springboot-0.0.1.jar .
docker compose up -d --build fatcat
echo "成功重新创建并运行主程序fatcat"