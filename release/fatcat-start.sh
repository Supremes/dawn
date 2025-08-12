SOURCE_PATH=$PWD
cd ../dawn-springboot
mvn package
cd $SOURCE_PATH
cp ../dawn-springboot/target/dawn-springboot-0.0.1.jar .
docker compose up -d --build fatcat
echo "成功重新创建并运行主程序fatcat"