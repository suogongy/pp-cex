#!/bin/bash

# User Service 启动脚本

echo "Starting User Service..."

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "Java is not installed. Please install Java 17+ first."
    exit 1
fi

# 检查Java版本
JAVA_VERSION=$(java -version 2>&1 | grep -oP 'version "?(1\.)?\K\d+' | head -1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "Java version 17+ is required. Current version: $JAVA_VERSION"
    exit 1
fi

# 设置JVM参数
JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
JAVA_OPTS="$JAVA_OPTS -Dfile.encoding=UTF-8"
JAVA_OPTS="$JAVA_OPTS -Duser.timezone=Asia/Shanghai"

# 设置环境变量
export SPRING_PROFILES_ACTIVE=dev
export NACOS_SERVER_ADDR=localhost:8848
export MYSQL_HOST=localhost
export MYSQL_PORT=3306
export MYSQL_USERNAME=root
export MYSQL_PASSWORD=password
export REDIS_HOST=localhost
export REDIS_PORT=6379
export JWT_SECRET=your-jwt-secret-key-should-be-at-least-256-bits

# 创建日志目录
mkdir -p logs

echo "Java version: $(java -version)"
echo "JVM options: $JAVA_OPTS"
echo "Spring profile: $SPRING_PROFILES_ACTIVE"

# 启动服务
echo "Starting User Service on port 8001..."
nohup java $JAVA_OPTS -jar target/user-service-1.0.0.jar > logs/user-service.log 2>&1 &

# 等待服务启动
sleep 10

# 检查服务状态
if curl -f http://localhost:8001/user/actuator/health > /dev/null 2>&1; then
    echo "User Service started successfully!"
    echo "Health check: http://localhost:8001/user/actuator/health"
    echo "API documentation: http://localhost:8001/user/doc.html"
else
    echo "Failed to start User Service. Please check logs."
    tail -20 logs/user-service.log
fi