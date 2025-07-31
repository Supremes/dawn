SOURCE_PATH=$PWD
cd ../aurora-springboot
mvn package
cd $SOURCE_PATH
cp ../aurora-springboot/target/aurora-springboot-0.0.1.jar .
docker compose up -d --build fatcat
echo "成功重新创建并运行主程序fatcat"