# 多阶段构建：Maven 编译打包，JRE 运行
FROM maven:3.8.6-openjdk-8 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q package -DskipTests

FROM eclipse-temurin:8-jre
WORKDIR /app
COPY --from=build /build/target/calculator-backend.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "/app/app.jar"]
