# ===== 构建阶段 =====
FROM maven:3.9.8-eclipse-temurin-21 AS builder

WORKDIR /build

# 先只拷贝所有pom.xml（利用Docker层缓存，依赖不变时不用重新下载）
COPY pom.xml .
COPY paland-admin/pom.xml paland-admin/
COPY paland-common/pom.xml paland-common/
COPY paland-system/pom.xml paland-system/
COPY paland-quartz/pom.xml paland-quartz/

# 后续新增子模块时，在这里追加一行，例如：
# COPY paland-common/pom.xml paland-common/

# 提前下载依赖，源码变动时这一层可以复用缓存
RUN mvn -B dependency:go-offline -DskipTests

# 拷贝全部源码
COPY . .

# 整体构建（多模块项目必须在根目录构建，子模块间的依赖才能正确解析）
RUN mvn -B clean package -DskipTests -pl paland-admin -am

# ===== 运行阶段 =====
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# 非root用户运行，提升安全性
RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=builder /build/paland-admin/target/*.jar app.jar

RUN chown spring:spring app.jar
USER spring

EXPOSE 18888

ENTRYPOINT ["java", "-jar", "app.jar"]