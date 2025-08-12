# mysql

```shell
docker run --name mysql --restart=always -p 3306:3306 -e MYSQL_ROOT_PASSWORD=123456 -d mysql //启动MySQL
```

```shell
docker run --name mysql --restart=always -p 3306:3306 -e MYSQL_ROOT_PASSWORD=123456 -v /mnt/d/programing/dawn/dawn.sql:/docker-entrypoint-initdb.d/init.sql --network custom-network -d mysql
```

# redis
```shell
docker run --name redis  --restart=always -p 6379:6379 --network custom-network -d redis --requirepass "123456"
```



# Elasticsearch

```shell
docker run --name elasticsearch -p 9200:9200 \
 -p 9300:9300 \
 -e "discovery.type=single-node" \
 -e ES_JAVA_OPTS="-Xms64m -Xmx128m" \
  -v /home/elasticsearch/config/elasticsearch.yml:/usr/shellare/elasticsearch/config/elasticsearch.yml \
 -v /home/elasticsearch/data:/usr/shellare/elasticsearch/data \
 -v /home/elasticsearch/plugins:/usr/shellare/elasticsearch/plugins \
 -d elasticsearch:7.9.2
```


# nginx

```shell
docker run --name nginx --restart=always -p 80:80 -d -v /mnt/d/programing/dawn/nginx/nginx.conf:/etc/nginx/nginx.conf -v /mnt/d/programing/dawn/release/vue:/usr/local/vue nginx 
```