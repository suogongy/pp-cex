#!/bin/bash

# 钱包服务启动脚本

# 设置环境变量
export JAVA_OPTS="-Xms512m -Xmx1024m -Djava.security.egd=file:/dev/./urandom"
export SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-dev}

# 检查Java版本
java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$java_version" -lt 17 ]; then
    echo "错误: 需要Java 17或更高版本"
    exit 1
fi

# 检查配置文件
if [ ! -f "target/wallet-service-1.0.0.jar" ]; then
    echo "错误: JAR文件不存在，请先构建项目"
    echo "运行命令: mvn clean package -DskipTests"
    exit 1
fi

# 创建日志目录
mkdir -p logs

# 启动服务
echo "正在启动钱包服务..."
echo "环境: $SPRING_PROFILES_ACTIVE"
echo "JVM参数: $JAVA_OPTS"

nohup java $JAVA_OPTS -jar target/wallet-service-1.0.0.jar > logs/wallet-service.log 2>&1 &

# 等待服务启动
sleep 10

# 检查服务状态
if curl -f http://localhost:8083/api/v1/wallet/health > /dev/null 2>&1; then
    echo "钱包服务启动成功!"
    echo "健康检查: http://localhost:8083/api/v1/wallet/health"
    echo "服务端口: 8083"
    echo "日志文件: logs/wallet-service.log"
else
    echo "钱包服务启动失败，请检查日志文件"
    exit 1
fi