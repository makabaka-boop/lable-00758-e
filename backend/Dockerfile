# 多阶段构建 - 支持 ARM 和 X86
FROM --platform=$BUILDPLATFORM maven:3.8-openjdk-11 AS builder

WORKDIR /app
COPY settings.xml /root/.m2/settings.xml
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests -B

# 运行阶段 - 使用跨平台镜像
FROM eclipse-temurin:11-jre

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

RUN mkdir -p /app/uploads/videos

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
