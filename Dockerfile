# 使用OpenJDK 11作为基础镜像
FROM openjdk:11-jdk-slim

# 设置工作目录
WORKDIR /app

# 复制pom.xml文件
COPY pom.xml .

# 下载依赖
RUN apt-get update && apt-get install -y maven
RUN mvn dependency:go-offline

# 复制项目源码
COPY src/ src/

# 构建项目
RUN mvn clean package -DskipTests

# 暴露端口
EXPOSE 8080

# 运行项目
CMD ["java", "-jar", "target/distributed-system-1.0.0.jar"]