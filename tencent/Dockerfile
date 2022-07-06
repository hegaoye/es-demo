FROM openjdk:11-jdk
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
VOLUME /tmp
ADD tencent/build/libs/*.jar app.jar
RUN sh -c 'touch /app.jar'
ENV JAVA_OPTS="-Xms128m -Xmx512m"
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /app.jar --spring.config.location=/default.yml -Djava.security.egd=file:/dev/./urandom" ]
EXPOSE 8080