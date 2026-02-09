FROM maven:3-eclipse-temurin-17 AS backend
VOLUME /tmp
COPY dawn-springboot-1.0.jar blog.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005","-jar","/blog.jar"]

FROM nginx:1.25.3-alpine AS frontend
COPY config/nginx/nginx.conf /etc/nginx/nginx.conf
COPY vue/blog /usr/local/vue/blog
COPY vue/admin /usr/local/vue/admin

FROM elasticsearch:7.17.15 AS search
RUN bin/elasticsearch-plugin install --batch https://get.infini.cloud/elasticsearch/analysis-ik/7.17.15
