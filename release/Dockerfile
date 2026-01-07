FROM maven:3-jdk-8-alpine
VOLUME /tmp
COPY dawn-springboot-1.0.jar blog.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005","-jar","/blog.jar"]