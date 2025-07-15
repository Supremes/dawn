cp /mnt/d/programing/aurora/aurora-springboot/target/aurora-springboot-0.0.1.jar /mnt/d/programing/aurora/release/
SOURCE_PATH=/mnt/d/programing/aurora/release/
SERVER_NAME=aurora
TAG=latest
SERVER_PORT=8080
CID=$(docker ps | grep "$SERVER_NAME" | awk '{print $1}')
IID=$(docker images | grep "$SERVER_NAME" | awk '{print $3}')
if [ -n "$CID" ]; then
  echo "存在容器$SERVER_NAME,CID-$CID"
  docker stop $CID
  echo "成功停止容器$SERVER_NAME,CID-$CID"
  docker rm $CID
  echo "成功删除容器$SERVER_NAME,CID-$CID"
fi
if [ -n "$IID" ]; then
  echo "存在镜像$SERVER_NAME:$TAG,IID=$IID"
  docker rmi $IID
  echo "成功删除镜像$SERVER_NAME:$TAG,IID=$IID"
fi
echo "开始构建镜像$SERVER_NAME:$TAG"
cd $SOURCE_PATH
docker build -t $SERVER_NAME:$TAG .
echo "成功构建镜像$SERVER_NAME:$TAG"
docker run --restart=always --name aurora --network=release_custom-network -d -p 8080:8080 -p 5005:5005 aurora:latest
echo "成功创建并运行容器$SERVER_NAME"