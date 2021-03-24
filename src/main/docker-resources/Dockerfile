FROM adoptopenjdk/openjdk11:jdk-11.0.10_9-alpine-slim
ARG JAR_FILE=@project.artifactId@-@project.version@.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
